package dev.ronse.siren.sdk.utils;

import dev.ronse.siren.sdk.model.PaginatedModel;
import dev.ronse.siren.sdk.model.shared.Pagination;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Paginator<T> {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_TIMEOUT = 25000;

    @FunctionalInterface
    public interface PageFetcher<M extends PaginatedModel<T>, T> {
        M fetch(int offset, int limit) throws IOException;
    }

    public record Page<T> (
            List<T> items,
            int total,
            int limit,
            int offset,
            boolean hasMore
    ) {

        public int pageNumber() {
            return limit == 0 ? 1 : (offset / limit) + 1;
        }

    }

    private final PageFetcher<? extends PaginatedModel<T>, T> fetcher;
    private final int defaultLimit;

    private Paginator(PageFetcher<? extends PaginatedModel<T>, T> fetcher, int defaultLimit) {
        this.fetcher = fetcher;
        this.defaultLimit = defaultLimit;
    }

    public static <T> Paginator<T> of(PageFetcher<? extends PaginatedModel<T>, T> fetcher) {
        return of(fetcher, DEFAULT_LIMIT);
    }

    public static <T> Paginator<T> of(PageFetcher<? extends PaginatedModel<T>, T> fetcher, int defaultLimit) {
        if (defaultLimit <= 0) throw new IllegalArgumentException("defaultLimit must be > 0"); // haha
        return new Paginator<>(fetcher, defaultLimit);
    }

    // Single page fetch

    public Page<T> fetchPage(int offset, int limit) throws IOException {
        PaginatedModel<T> model = fetcher.fetch(offset, limit);
        Pagination p = model.pagination();
        return new Page<>(model.data(), p.total(), p.limit(), p.offset(), p.hasMore());
    }

    public Page<T> fetchPage(int offset) throws IOException {
        return fetchPage(offset, defaultLimit);
    }

    // Fetch all

    public List<T> fetchAll(int pageSize) throws IOException {
        List<T> result = new ArrayList<>();
        int offset = 0;

        Instant endTime = Instant.now().plusMillis(MAX_TIMEOUT);
        boolean isDone = false;

        while (Instant.now().isBefore(endTime)) {
            Page<T> page = fetchPage(offset, pageSize);
            result.addAll(page.items);
            if(!page.hasMore) {
                isDone = true;
                break;
            }
            offset += pageSize;
        }

        if(!isDone) throw new IOException("Timeout reached");
        return  result;
    }

    public List<T> fetchAll() throws IOException {
        return fetchAll(defaultLimit);
    }

    // Lazy itterator and stream

    public Iterator<Page<T>> pageIterator(int pageSize) {
        return new Iterator<>() {
            private int offset = 0;
            private boolean done = false;
            private Page<T> next = null;

            @Override
            public boolean hasNext() {
                if(done) return false;
                if(next != null) return true;
                try {
                    next = fetchPage(offset, pageSize);
                    if(next.items.isEmpty()) {
                        done = true;
                        return false;
                    }

                    return true;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public Page<T> next() {
                if(!hasNext()) throw new NoSuchElementException();
                Page<T> current = next;
                next = null;
                if(!current.hasMore) done = true;
                else offset += pageSize;
                return current;
            }
        };
    }

    public Iterator<Page<T>> pageIterator() {
        return pageIterator(defaultLimit);
    }

    public Stream<Page<T>> streamPages(int pageSize) {
        Iterator<Page<T>> it = pageIterator(pageSize);
        Spliterator<Page<T>> spliterator = Spliterators.spliteratorUnknownSize(
                it, Spliterator.ORDERED | Spliterator.NONNULL
        );
        return StreamSupport.stream(spliterator, false);
    }

    public Stream<T> stream(int pageSize) {
        return streamPages(pageSize).flatMap(page -> page.items.stream());
    }

    public Stream<T> stream() {
        return stream(defaultLimit);
    }

}
