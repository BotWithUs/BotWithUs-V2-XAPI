package net.botwithus.xapi.query.result;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class ResultSet<T> implements Iterable<T> {
    protected final List<T> results;

    public ResultSet(List<T> results) {
        this.results = List.copyOf(results);
    }

    public T first() {
        return results.isEmpty() ? null : results.getFirst();
    }

    public T last() {
        return results.isEmpty() ? null : results.getLast();
    }

    public T random() {
        return results.isEmpty() ? null : results.get(ThreadLocalRandom.current().nextInt(results.size()));
    }

    public Stream<T> stream() {
        return results.stream();
    }

    public int size() {
        return results.size();
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return results.iterator();
    }
}
