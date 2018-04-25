import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.ListIterator;

public class Collections {
    public static <T, R> ArrayList<R> map(Function1<? super T, ? extends R> f, Iterable<? extends T> list) {
        ArrayList<R> result = new ArrayList<>();
        for (T element : list) {
            result.add(f.apply(element));
        }
        return result;
    }

    public static <T> ArrayList<T> filter(Predicate<? super T> f, Iterable<? extends T> list) {
        ArrayList<T> result = new ArrayList<>();
        for (T element : list) {
            if (f.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    public static <T> ArrayList<T> takeWhile( Predicate<T> predicate,Iterable<? extends T> list) {
        ArrayList<T> result = new ArrayList<>();

        for (T elem : list) {
            if (predicate.apply(elem)) {
                result.add(elem);
            } else {
                break;
            }
        }
        return result;
    }

    public static <T> ArrayList<T> takeUnless( Predicate<T> predicate, Iterable<? extends T> list) {
        ArrayList<T> result = new ArrayList<>();
        for (T elem : list) {
            if (predicate.not().apply(elem)) {
                result.add(elem);
            } else {
                break;
            }
        }
        return result;
    }


    public static <T, R> R foldl(Function2<R, T, R> f, R acc, Iterable<? extends T> list) {
        R p = acc;
        for (T elem : list)
            p = f.apply(p, elem);
        return p;
    }

    public static <T, R> R foldr(Function2<R, T, R> f, R acc, Iterable<? extends T> list) {
        R p = acc;
        ArrayList<T> a = new ArrayList<T>();
        for (T elem : list)
            a.add(elem);

        ListIterator li = a.listIterator(a.size());

        while (li.hasPrevious()) {
            p = f.apply(p, (T)li.previous());
        }
        return p;
    }

}

