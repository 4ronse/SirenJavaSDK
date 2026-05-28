package dev.ronse.siren.sdk.clients;

import dev.ronse.siren.sdk.clients.insterfaces.ISirenAlertHandler;
import dev.ronse.siren.sdk.clients.options.clients.AlertsClientOpts;
import dev.ronse.siren.sdk.model.AlertModel;
import dev.ronse.siren.sdk.wrappers.AlertType;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketOptionBuilder;
import io.socket.emitter.Emitter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AlertsClient {

    private final Map<AlertType, List<ISirenAlertHandler>> handlerMap = new HashMap<>();

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

    static AlertsClient fromSirenClient(SirenClient client) {
        return new AlertsClient(client, null);
    }

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

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public boolean connected() {
        return socket.connected();
    }

    public void onConnect(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT, listener);
    }

    public void onDisconnect(Emitter.Listener listener) {
        socket.on(Socket.EVENT_DISCONNECT, listener);
    }

    public void onConnectError(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT_ERROR, listener);
    }

    public void onAlert(ISirenAlertHandler handler) {
        onAlert(null, handler);
    }

    public void onAlert(AlertType type, ISirenAlertHandler handler) {
        handlerMap.computeIfAbsent(type, _t -> new ArrayList<>()).add(handler);
    }
}
