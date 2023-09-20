package util;

public class ExceptionUtils {
    
    /**
     * Sneaky throw any type of Throwable.
     */
    public static RuntimeException sneakyThrow(Throwable throwable) {
        return (RuntimeException) ExceptionUtils.<RuntimeException>sneakyThrow0(throwable);
    }
    
    /**
     * Sneaky throw any type of Throwable.
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> Throwable sneakyThrow0(Throwable throwable) throws E {
        throw (E) throwable;
    }
    
}
