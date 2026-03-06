package net.botwithus.xapi.query.base;

import net.botwithus.xapi.query.result.ResultSet;

import java.util.function.Predicate;

public abstract class ItemQuery<I, T extends ItemQuery<I, T>> implements Query<I, ResultSet<I>> {
    protected Predicate<I> root = item -> true;
}
