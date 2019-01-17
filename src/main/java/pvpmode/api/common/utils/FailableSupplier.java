package pvpmode.api.common.utils;

import java.util.function.Supplier;

/**
 * A supplier which works like {@link Supplier}, but is also allowed to throw an
 * exception or error.
 * 
 * @author CraftedMods
 *
 * @param <R>
 *            The type returned by the supplier
 * @param <T>
 *            The error or exception type eventually thrown
 */
@FunctionalInterface
public interface FailableSupplier<R, T extends Throwable>
{

    public R get () throws T;

}
