package dev.ronse.siren;

import dev.ronse.siren.sdk.clients.SirenAlertsSocketClient;
import dev.ronse.siren.sdk.model.AlertModel;
import dev.ronse.siren.sdk.wrappers.AlertType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var alertsClient = SirenAlertsSocketClient.build(opts -> opts
                .apiKey(System.getenv("SIREN_API_KEY"))
                .socketUri(URI.create("https://api.siren.co.il/test"))
        );

        alertsClient.registerHandlers(TestHandler.class);
        alertsClient.connect();

        alertsClient.sendTestAlert(new AlertModel(
                AlertType.fromValue("testType"),
                List.of("Petah Tikvaa"),
                "ALERT",
                "RUN RUN RUN RUN RUN RUN RUN",
                Instant.now(),
                false
        ));
    }

}
