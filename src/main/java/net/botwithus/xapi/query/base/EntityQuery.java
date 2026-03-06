package net.botwithus.xapi.query.base;

import java.util.function.Predicate;

public abstract class EntityQuery<T> implements Query<T, net.botwithus.xapi.query.result.EntityResultSet<T>> {
    protected Predicate<T> root = entity -> true;
}
