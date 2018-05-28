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

    public static <T> ArrayList<T> takeWhile( Predicate<? super T> predicate,Iterable<? extends T> list) {
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

    public static <T> ArrayList<T> takeUnless( Predicate<? super T> predicate, Iterable<? extends T> list) {
        return takeWhile(predicate.not(),list );
    }


    public static <T, R> R foldl(Function2<? super R, ? super T, ? extends R> f, R acc, Iterable<? extends T> list) {
        for (T el : list) {
            acc = f.apply(acc, el);
        }
        return acc;
    }


    public static <T, R> R foldr(Function2<? super T, ? super R, ? extends R> f, R acc, Collection<? extends T> list) {
        List<T> els = new ArrayList<>();
        els.addAll(list);
        java.util.Collections.reverse(els);
        for (T el : els) {
            acc = f.apply(el, acc);
        }
        return acc;
    }

}

