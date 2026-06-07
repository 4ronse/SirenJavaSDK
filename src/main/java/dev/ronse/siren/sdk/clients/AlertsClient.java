package dev.ronse.siren.sdk.clients;

import dev.ronse.siren.sdk.clients.insterfaces.ISirenAlertHandler;
import dev.ronse.siren.sdk.clients.insterfaces.OnAlert;
import dev.ronse.siren.sdk.clients.options.clients.AlertsClientOpts;
import dev.ronse.siren.sdk.model.AlertModel;
import dev.ronse.siren.sdk.wrappers.AlertType;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketOptionBuilder;
import io.socket.emitter.Emitter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;

/**
 * Client for receiving real-time alerts via a persistent Socket.IO connection.
 *
 * <pre>{@code
 * AlertsClient alerts = client.alerts();
 *
 * alerts.onConnect(args -> System.out.println("Connected"));
 * alerts.onAlert(alert -> System.out.println("Alert: " + alert.title()));
 * alerts.onAlert(AlertType.MISSILES, alert -> System.out.println("Missile alert: " + alert.city()));
 *
 * alerts.connect();
 * }</pre>
 */
public final class AlertsClient {

    private final Map<AlertType, List<ISirenAlertHandler>> handlerMap = new HashMap<>();
    private final Set<Class<?>> registeredClasses = new HashSet<>();
    private final Set<Object> registeredObjects = new HashSet<>();

    private final Socket socket;

    private AlertsClient(SirenClient client, @Nullable AlertsClientOpts opts) {
        if(opts == null) opts = AlertsClientOpts.builder().build();

        URI uri = opts.getBaseURL() != null ? opts.getBaseURL() : client.baseURI;

        IO.Options ioOpts = SocketOptionBuilder.builder(opts.getIoOpts())
                .setAuth(Map.of("apiKey", client.apiKey))
                .setQuery(opts.query().toQueryString())
                .build();

        socket = IO.socket(uri, ioOpts);
        socket.on("alert", args -> {
            try {
                JSONArray alerts = (JSONArray) args[0];
                for (int i = 0; i < alerts.length(); i++) {
                    AlertModel alert = client.getObjectMapper().readValue(alerts.getString(i), AlertModel.class);
                    handleAlert(alert);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse alert: " + e.getMessage());
            }
        });
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static AlertsClient fromSirenClient(SirenClient client) {
        return new AlertsClient(client, null);
    }

    @NotNull
    @Contract(value = "_,_ -> new", pure = true)
    static AlertsClient fromSirenClient(SirenClient client, AlertsClientOpts opts) {
        return new AlertsClient(client, opts);
    }

    private void handleAlert(AlertModel alert) {
        getHandlers(null).forEach(handler -> handler.handleAlert(alert));
        getHandlers(alert.type()).forEach(handler -> handler.handleAlert(alert));
    }

    private List<ISirenAlertHandler> getHandlers(AlertType type) {
        return handlerMap.getOrDefault(type, List.of());
    }

    // --------------
    // Wrappers
    // --------------

    /**
     * Opens the Socket.IO connection and begins receiving alerts.
     * Register handlers via {@link #onAlert} before calling this.
     */
    public void connect() {
        socket.connect();
    }

    /**
     * Closes the Socket.IO connection and stops receiving alerts.
     */
    public void disconnect() {
        socket.disconnect();
    }

    /**
     * Closes the Socket.IO connection and stops receiving alerts.
     */
    public void close() {
        socket.close();
    }

    /**
     * Returns whether the socket is currently connected.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    public boolean connected() {
        return socket.connected();
    }

    /**
     * Registers a listener for successful connection events.
     *
     * @param listener the Socket.IO listener to invoke on connect
     */
    public void onConnect(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT, listener);
    }

    /**
     * Registers a listener for disconnection events.
     *
     * @param listener the Socket.IO listener to invoke on disconnect
     */
    public void onDisconnect(Emitter.Listener listener) {
        socket.on(Socket.EVENT_DISCONNECT, listener);
    }

    /**
     * Registers a listener for connection error events.
     *
     * @param listener the Socket.IO listener to invoke on error
     */
    public void onConnectError(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT_ERROR, listener);
    }

    /**
     * Registers a listener for all incoming alerts regardless of type.
     *
     * @param handler the handler to invoke on each alert
     * @see #onAlert(AlertType, ISirenAlertHandler)
     */
    public void onAlert(ISirenAlertHandler handler) {
        onAlert(null, handler);
    }

    /**
     * Registers a listener for alerts of a specific type.
     * Handlers registered with {@link #onAlert(ISirenAlertHandler)} are also invoked
     * alongside type-specific handlers.
     *
     * <pre>{@code
     * alerts.onAlert(AlertType.MISSILES, alert -> System.out.println("Incoming: " + alert.city()));
     * }</pre>
     *
     * @param type    the alert type to filter by
     * @param handler the handler to invoke when an alert of this type is received
     */
    public void onAlert(AlertType type, ISirenAlertHandler handler) {
        handlerMap.computeIfAbsent(type, _t -> new ArrayList<>()).add(handler);
    }

    private record HandlerInvocationTarget(@Nullable Object instance, @NotNull Class<?> clazz) {
        public boolean isStatic() { return instance == null; }
    }

    private void parseAndRegister(HandlerInvocationTarget target) {
        Arrays.stream(target.clazz.getDeclaredMethods())
                .forEach(method -> {
                    // Ensure static match
                    if (Modifier.isStatic(method.getModifiers()) != target.isStatic()) return;

                    OnAlert annotation = method.getAnnotation(OnAlert.class);
                    if (annotation == null) return;

                    Class<?>[] params = method.getParameterTypes();
                    if (params.length > 1 || (params.length == 1 && params[0] != AlertModel.class)) {
                        throw new IllegalArgumentException(
                                method.getName() + " must take either no parameters or a single AlertModel parameter"
                        );
                    }

                    boolean takesAlert = params.length == 1;
                    String filter = annotation.value();

                    ISirenAlertHandler h = model -> {
                        try {
                            method.setAccessible(true);
                            if (takesAlert) method.invoke(target.instance(), model);
                            else method.invoke(target.instance());
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                            throw new RuntimeException("Failed to invoke handler " + method.getName(), e);
                        }
                    };

                    if (filter.isBlank()) onAlert(h);
                    else onAlert(AlertType.fromValue(filter), h);
                });
    }

    /**
     * Registers all non-static handler methods annotated with {@link OnAlert} from the given object instance.
     *
     * @param handler The object instance containing the handler methods.
     * @throws IllegalStateException if the handler instance has already been registered.
     * @throws IllegalArgumentException if an annotated method has invalid parameter types.
     */
    public void registerHandlers(Object handler) {
        if (registeredObjects.contains(handler))
            throw new IllegalStateException("Handler " + handler.hashCode() + " is already registered");

        Class<?> clazz = handler.getClass();
        parseAndRegister(new HandlerInvocationTarget(handler, clazz));
        registeredObjects.add(handler);
    }

    /**
     * Registers all static handler methods annotated with {@link OnAlert} from the given class.
     *
     * @param clazz The class containing the static handler methods.
     * @throws IllegalStateException if the class has already been registered.
     * @throws IllegalArgumentException if an annotated method has invalid parameter types.
     */
    public void registerHandlers(Class<?> clazz) {
        if (registeredClasses.contains(clazz))
            throw new IllegalStateException("Handler " + clazz.getSimpleName() + " is already registered");

        parseAndRegister(new HandlerInvocationTarget(null, clazz));
        registeredClasses.add(clazz);
    }
}
