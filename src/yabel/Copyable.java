package yabel;

/**
 * A class that can make a deep copy of itself
 * @author Simon
 *
 * @param <T> the class's own type
 */
public interface Copyable<T> {
    public T copy();
}
