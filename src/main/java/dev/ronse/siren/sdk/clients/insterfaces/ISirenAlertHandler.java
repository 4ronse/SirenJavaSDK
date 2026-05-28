package dev.ronse.siren.sdk.clients.insterfaces;

import dev.ronse.siren.sdk.model.AlertModel;

@FunctionalInterface
public interface ISirenAlertHandler {
    void handleAlert(AlertModel model);
}
