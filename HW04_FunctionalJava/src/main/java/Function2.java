public abstract class Function2<T1, T2, R> {
    //apply args
    public abstract R apply(T1 arg1, T2 arg2);


    //composition
    public <V> Function2<T1, T2, V> compose(final Function1<? super R, ? extends V> f) {
        return new Function2<T1, T2, V>() {
            public V apply(T1 arg1, T2 arg2) {
                return f.apply(Function2.this.apply(arg1, arg2));
            }
        };
    }

    //bind first
    public Function1<T2, R> bind1(final T1 arg1) {
        return new Function1<T2, R>() {
            public R apply(T2 arg2) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    //binds second
    public Function1<T1, R> bind2(final T2 arg2) {
        return new Function1<T1, R>() {
            public R apply(T1 arg1) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    //curry
    public Function1<T1, Function1<T2, R>> curry() {
        return new Function1<T1, Function1<T2, R>>() {
            public Function1<T2, R> apply(final T1 arg1) {
                return new Function1<T2, R>() {
                    public R apply(final T2 arg2) {
                        return Function2.this.apply(arg1, arg2);
                    }
                };
            }
        };
    }
}