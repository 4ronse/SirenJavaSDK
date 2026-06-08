package dev.ronse.siren.sdk.clients;

import dev.ronse.siren.sdk.clients.interfaces.ISirenAlertHandler;
import dev.ronse.siren.sdk.clients.interfaces.OnAlert;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Client for receiving real-time alerts via a persistent Socket.IO connection.
 *
 * <pre>{@code
 * AlertsClient alerts = client.alerts();
 *
 * alerts.onConnect(args -> System.out.println("Connected"));
 * alerts.onAlert(alert -> System.out.println("Alert: " + alert.title()));
 * alerts.onAlert(AlertType.MISSILES, alert -> System.out.println("Missile: " + alert.cities()));
 *
 * alerts.connect();
 * }</pre>
 */
public final class AlertsClient {

    private static final Logger LOGGER = Logger.getLogger(AlertsClient.class.getName());
    private static final int MAX_CITIES_TO_LOG = 5;

    // Catch-all handlers (registered via onAlert(handler) or @OnAlert with no filter).
    // Kept separate from handlerMap to avoid using null as a ConcurrentHashMap key,
    // which is not permitted.
    private final List<ISirenAlertHandler> catchAllHandlers = new CopyOnWriteArrayList<>();

    // Type-specific handlers. ConcurrentHashMap so reads from the Socket.IO thread
    // and writes from the registration thread don't race. Values are
    // CopyOnWriteArrayList so iteration during dispatch is over a stable snapshot.
    private final Map<AlertType, List<ISirenAlertHandler>> handlerMap = new ConcurrentHashMap<>();

    // CopyOnWriteArraySet is a direct thread-safe Set from java.util.concurrent.
    // Safe here because Class objects are singletons per classloader —
    // reference equality and equals() are the same thing for Class<?>.
    private final Set<Class<?>> registeredClasses = new CopyOnWriteArraySet<>();

    // CopyOnWriteArraySet would be wrong here — it uses equals(), not reference
    // identity. Two distinct handler instances that happen to override equals()
    // would be treated as duplicates. IdentityHashMap backing guarantees we track
    // the exact object reference, not logical equality.
    private final Set<Object> registeredObjects = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<>())
    );

    private final Socket socket;

    private AlertsClient(SirenClient client, @Nullable AlertsClientOpts opts) {
        if (opts == null) opts = AlertsClientOpts.builder().build();

        URI uri = opts.getBaseURL() != null ? opts.getBaseURL() : client.baseUri();

        IO.Options ioOpts = SocketOptionBuilder.builder(opts.getIoOpts())
                .setAuth(Map.of("apiKey", client.apiKey()))
                .setQuery(opts.query().toQueryString())
                .build();

        socket = IO.socket(uri, ioOpts);
        socket.on("alert", args -> {
            try {
                JSONArray alerts = (JSONArray) args[0];

                for (int i = 0; i < alerts.length(); i++) {
                    String jsonAlert = alerts.getString(i);
                    AlertModel alert = client.getObjectMapper().readValue(jsonAlert, AlertModel.class);

                    LOGGER.finer(() -> {
                        String citiesStr = alert.cities().stream().limit(MAX_CITIES_TO_LOG).collect(Collectors.joining(", "));
                        int diff = alert.cities().size() - MAX_CITIES_TO_LOG;
                        if(diff > 0) citiesStr += " (+" + diff + " More)";

                        return String.format(
                                """
                                IO.Socket / New Alert
                                    Type            : %s
                                    Title           : %s
                                    Instructions    : %s
                                    Received At     : %s
                                    Is Test         : %s
                                    Cities          : %d
                                    Cities List     : %s
                                    Raw JSON        : %s
                                """,
                                alert.type(), alert.title(), alert.instructions(), alert.receivedAt(), alert.isTest() ? "True" : "False",
                                alert.cities().size(), citiesStr, SirenClient.truncate(jsonAlert)
                        );
                    });


                    handleAlert(alert);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse alert", e);
            }
        });
    }

    @NotNull
    @Contract(value = "_ -> new")
    static AlertsClient fromSirenClient(SirenClient client) {
        return new AlertsClient(client, null);
    }

    @NotNull
    @Contract(value = "_,_ -> new")
    static AlertsClient fromSirenClient(SirenClient client, AlertsClientOpts opts) {
        return new AlertsClient(client, opts);
    }

    public void sendTestAlert(AlertModel alert) {
        handleAlert(alert.asTest());
    }

    private void handleAlert(AlertModel alert) {
        catchAllHandlers.forEach(h -> h.handleAlert(alert));
        handlerMap.getOrDefault(alert.type(), List.of()).forEach(h -> h.handleAlert(alert));
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
        catchAllHandlers.add(handler);
    }

    /**
     * Registers a listener for alerts of a specific type.
     * Handlers registered with {@link #onAlert(ISirenAlertHandler)} are also invoked
     * alongside type-specific handlers.
     *
     * <pre>{@code
     * alerts.onAlert(AlertType.MISSILES, alert -> System.out.println("Incoming: " + alert.cities()));
     * }</pre>
     *
     * @param type    the alert type to filter by
     * @param handler the handler to invoke when an alert of this type is received
     */
    public void onAlert(AlertType type, ISirenAlertHandler handler) {
        handlerMap.computeIfAbsent(type, _t -> new CopyOnWriteArrayList<>()).add(handler);
    }

    private record HandlerInvocationTarget(@Nullable Object instance, @NotNull Class<?> clazz) {
        public boolean isStatic() { return instance == null; }
    }

    private void parseAndRegister(HandlerInvocationTarget target) {
        Arrays.stream(target.clazz.getDeclaredMethods())
                .forEach(method -> {
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
                            LOGGER.log(Level.WARNING, "Failed to invoke handler "
                                    + method.getDeclaringClass().getSimpleName() + "#" + method.getName(), e);
                        }
                    };

                    if (filter.isBlank()) onAlert(h);
                    else onAlert(AlertType.fromValue(filter), h);
                });
    }

    /**
     * Registers all non-static handler methods annotated with {@link OnAlert} from the given object instance.
     *
     * @param handler the object instance containing the handler methods
     * @throws IllegalStateException    if the handler instance has already been registered
     * @throws IllegalArgumentException if an annotated method has invalid parameter types
     */
    public void registerHandlers(Object handler) {
        if (!registeredObjects.add(handler))
            throw new IllegalStateException("Handler " + handler.hashCode() + " is already registered");

        parseAndRegister(new HandlerInvocationTarget(handler, handler.getClass()));
    }

    /**
     * Registers all static handler methods annotated with {@link OnAlert} from the given class.
     *
     * @param clazz the class containing the static handler methods
     * @throws IllegalStateException    if the class has already been registered
     * @throws IllegalArgumentException if an annotated method has invalid parameter types
     */
    public void registerHandlers(Class<?> clazz) {
        if (!registeredClasses.add(clazz))
            throw new IllegalStateException("Handler " + clazz.getSimpleName() + " is already registered");

        parseAndRegister(new HandlerInvocationTarget(null, clazz));
    }
}