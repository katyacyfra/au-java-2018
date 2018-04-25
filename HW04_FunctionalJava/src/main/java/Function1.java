public abstract class Function1<T, R> {
    //apply arg
    public abstract R apply(T arg);

    //composition of functions
    public <V> Function1<T, V> compose(final Function1<? super R, ? extends V> f) {
        return new Function1<T, V>() {
            public V apply(T arg) {
                return f.apply(Function1.this.apply(arg));
            }
        };
    }
}
