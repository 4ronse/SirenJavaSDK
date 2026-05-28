package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.data.CitiesCatalogOpts;
import dev.ronse.siren.sdk.clients.options.data.ShelterSearchOpts;
import dev.ronse.siren.sdk.model.data.CitiesCatalogModel;
import dev.ronse.siren.sdk.model.data.ShelterSearchModel;

import java.io.IOException;
import java.util.List;

public final class DataClient {

    private final SirenClient sirenClient;

    private DataClient(SirenClient sirenClient) {
        this.sirenClient = sirenClient;
    }

    static DataClient fromSirenClient(SirenClient sirenClient) {
        return new DataClient(sirenClient);
    }

    /**
     * Search for nearby shelters by coordinates.
     *
     * <pre>{@code
     * client.data().shelterSearch(new ShelterSearchOptions(32.08, 34.78)
     *         .radiusKm(1.5)
     *         .wheelchairOnly(true));
     * }</pre>
     */
    public List<ShelterSearchModel> shelterSearch(ShelterSearchOpts options) throws IOException {
        return sirenClient.get("/shelter/search", options.toQueryParams(), new TypeReference<>() {});
    }

    /**
     * Browse the full cities catalog.
     *
     * <pre>{@code
     * client.data().cities(new CitiesCatalogOptions()
     *         .search("תל אביב")
     *         .include(CityInclude.TRANSLATIONS, CityInclude.COORDS));
     * }</pre>
     */
    public CitiesCatalogModel cities(CitiesCatalogOpts options) throws IOException {
        return sirenClient.get("/data/cities", options.toQueryParams(), CitiesCatalogModel.class);
    }

    public CitiesCatalogModel cities() throws IOException {
        return cities(new CitiesCatalogOpts());
    }
}