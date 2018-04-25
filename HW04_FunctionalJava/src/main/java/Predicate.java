public abstract class Predicate<T> extends Function1<T, Boolean> {

    public Predicate<T> and(final Predicate<? super T> second) {
        return new Predicate<T>() {
            public Boolean apply(T arg) {
                return Predicate.this.apply(arg) && second.apply(arg);
            }
        };
    }

    public Predicate<T> or(final Predicate<? super T> second) {
        return new Predicate<T>() {
            public Boolean apply(T arg) {
                return Predicate.this.apply(arg) || second.apply(arg);
            }
        };
    }

    public Predicate<T> not() {
        return new Predicate<T>() {
            public Boolean apply(T arg) {
                return !Predicate.this.apply(arg);
            }
        };
    }

    public static <S> Predicate<S> ALWAYS_TRUE() {
        return new Predicate<S>() {
            public Boolean apply(S arg) {
                return true;
            }
        };
    }

    public static <S> Predicate<S> ALWAYS_FALSE() {
        return new Predicate<S>() {
            public Boolean apply(S arg) {
                return false;
            }
        };
    }

}