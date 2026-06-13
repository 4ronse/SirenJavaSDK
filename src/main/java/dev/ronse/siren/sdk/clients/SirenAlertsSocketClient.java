package dev.ronse.siren.sdk.clients;

import dev.ronse.siren.sdk.clients.interfaces.ISirenAlertHandler;
import dev.ronse.siren.sdk.clients.interfaces.OnAlert;
import dev.ronse.siren.sdk.clients.options.clients.SirenAlertsSocketClientOpts;
import dev.ronse.siren.sdk.clients.restapi.SirenClient;
import dev.ronse.siren.sdk.internal.JsonSupport;
import dev.ronse.siren.sdk.model.AlertModel;
import dev.ronse.siren.sdk.utils.StringUtils;
import dev.ronse.siren.sdk.wrappers.AlertType;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketOptionBuilder;
import io.socket.emitter.Emitter;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SirenAlertsSocketClient {

    private static final Logger LOGGER = Logger.getLogger(SirenAlertsSocketClient.class.getName());
    private static final int MAX_CITIES_TO_LOG = 5;
    private static final long READ_TIMEOUT_SECONDS = 25;

    private final List<ISirenAlertHandler> catchAllHandlers = new CopyOnWriteArrayList<>();
    private final Map<AlertType, List<ISirenAlertHandler>> handlerMap = new ConcurrentHashMap<>();
    private final Set<Class<?>> registeredClasses = new CopyOnWriteArraySet<>();
    private final Set<Object> registeredObjects = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<>())
    );

    private final Socket socket;
    private final Dispatcher dispatcher;

    public SirenAlertsSocketClient(@NotNull SirenAlertsSocketClientOpts opts) {
        URI uri = opts.socketUri;

        dispatcher = new Dispatcher();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS))
                .build();

        IO.Options ioOpts = SocketOptionBuilder.builder(opts.ioOpts)
                .setAuth(Map.of("apiKey", opts.apiKey))
                .setQuery(opts.queryParametersList.toQueryString())
                .build();
        ioOpts.callFactory = okHttpClient;
        ioOpts.webSocketFactory = okHttpClient;

        socket = IO.socket(uri, ioOpts);
        attachLifecycleLogging();

        socket.on("alert", args -> {
            try {
                JSONArray alerts = (JSONArray) args[0];

                for (int i = 0; i < alerts.length(); i++) {
                    String jsonAlert = alerts.getString(i);
                    AlertModel alert = JsonSupport.getObjectMapper().readValue(jsonAlert, AlertModel.class);

                    LOGGER.finer(() -> {
                        String citiesStr = alert.cities().stream().limit(MAX_CITIES_TO_LOG).collect(Collectors.joining(", "));
                        int diff = alert.cities().size() - MAX_CITIES_TO_LOG;
                        if(diff > 0) citiesStr += " (+" + diff + " More)";

                        return String.format(
                                """
                                New Alert
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
                                alert.cities().size(), citiesStr, StringUtils.truncate(jsonAlert)
                        );
                    });


                    handleAlert(alert);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse alert", e);
            }
        });
    }

    public static SirenAlertsSocketClient builder(Consumer<SirenAlertsSocketClientOpts.Builder> optsBuilderConsumer) {
        var builder = SirenAlertsSocketClientOpts.builder();
        optsBuilderConsumer.accept(builder);
        return new SirenAlertsSocketClient(builder.build());
    }

    private void attachLifecycleLogging() {
        socket.on(Socket.EVENT_CONNECT, args ->
                LOGGER.info("Socket connected"));

        socket.on(Socket.EVENT_DISCONNECT, args ->
                LOGGER.info(() -> "Socket disconnected: " + Arrays.toString(args)));

        socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                LOGGER.log(Level.WARNING, "Socket connect error: {0}", Arrays.toString(args)));
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
        this.close();
    }

    /**
     * Closes the Socket.IO connection and stops receiving alerts.
     */
    public void close() {
        socket.close();
        dispatcher.executorService().shutdownNow();
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