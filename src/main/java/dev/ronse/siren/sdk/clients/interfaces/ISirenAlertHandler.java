package dev.ronse.siren.sdk.clients.interfaces;

import dev.ronse.siren.sdk.model.AlertModel;

@FunctionalInterface
public interface ISirenAlertHandler {
    void handleAlert(AlertModel model);
}
