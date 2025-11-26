package firfaronde.func;

@FunctionalInterface
public interface Bool<T> {
    boolean accept(T obj);
}