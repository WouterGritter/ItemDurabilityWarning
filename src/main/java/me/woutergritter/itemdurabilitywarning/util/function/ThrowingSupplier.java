package me.woutergritter.itemdurabilitywarning.util.function;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
