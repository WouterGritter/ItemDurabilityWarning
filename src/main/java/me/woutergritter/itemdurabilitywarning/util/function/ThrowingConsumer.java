package me.woutergritter.itemdurabilitywarning.util.function;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
