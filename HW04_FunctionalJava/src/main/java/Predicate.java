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

    public static final Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {
        public Boolean apply(Object obj) {
            return true;
        }
    };
    public static final Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {
        public Boolean apply(Object obj) {
            return false;
        }
    };

}