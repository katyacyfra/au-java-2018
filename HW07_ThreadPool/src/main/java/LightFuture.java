import java.util.function.Function;

public interface LightFuture<T> {

    boolean isReady();

    T get() throws LightExecutionException;

    <S> LightFuture<S> thenApply(Function<T, S> function);

}
