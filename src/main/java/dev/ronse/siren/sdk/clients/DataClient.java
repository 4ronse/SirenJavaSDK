package dev.ronse.siren.sdk.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.ronse.siren.sdk.clients.options.data.CitiesCatalogOpts;
import dev.ronse.siren.sdk.clients.options.data.ShelterSearchOpts;
import dev.ronse.siren.sdk.model.data.CitiesCatalogModel;
import dev.ronse.siren.sdk.model.data.ShelterSearchModel;
import dev.ronse.siren.sdk.model.shared.PagedResponse;
import dev.ronse.siren.sdk.utils.Paginator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public final class DataClient {

    private final SirenClient client;

    private DataClient(SirenClient sirenClient) {
        this.client = sirenClient;
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static DataClient fromSirenClient(SirenClient sirenClient) {
        return new DataClient(sirenClient);
    }

    /**
     * Find shelters near a given coordinate.
     * <p>
     * Results are sorted by distance. By default returns all shelter types within
     * a reasonable radius - use the options to narrow by distance, wheelchair
     * accessibility, or shelter type.
     *
     * <pre>{@code
     * client.data().shelterSearch(new ShelterSearchOpts(32.08, 34.78)
     *         .radiusKm(1.5)
     *         .wheelchairOnly(true));
     * }</pre>
     *
     * @param opts query options - lat/lon are required
     * @throws IOException if the request fails
     */
    public List<ShelterSearchModel> shelterSearch(ShelterSearchOpts opts) throws IOException {
        return client.get("/shelter/search", opts.toQueryParams(), new TypeReference<>() {});
    }

    /**
     * Full cities catalog with optional metadata.
     * <p>
     * By default returns city ID, name, and zone. Use the {@code include} parameter
     * to opt-in to translations, coordinates, countdown times, and polygons.
     *
     * <pre>{@code
     * client.data().cities(new CitiesCatalogOpts()
     *         .search("תל אביב")
     *         .include(CityInclude.TRANSLATIONS, CityInclude.COORDS));
     * }</pre>
     *
     * @param opts query options
     * @throws IOException if the request fails
     */
    public PagedResponse<CitiesCatalogModel> cities(CitiesCatalogOpts opts) throws IOException {
        return client.get("/data/cities", opts.toQueryParams(), new TypeReference<>() {});
    }

    public PagedResponse<CitiesCatalogModel> cities() throws IOException {
        return cities(new CitiesCatalogOpts());
    }

    // Paginators

    public Paginator<CitiesCatalogModel> citiesPaginator(CitiesCatalogOpts opts) {
        return Paginator.of(
                (offset, limit) -> cities(opts.offset(offset).limit(limit))
        );
    }

    public Paginator<CitiesCatalogModel> citiesPaginator() {
        return citiesPaginator(new CitiesCatalogOpts());
    }
}