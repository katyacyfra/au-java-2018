import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class CollectionsTest {

    private Function1<Integer, Integer> square =
            new Function1<Integer, Integer>() {
                public Integer apply(final Integer a) {
                    return a * a;
                }
            };

    private static Function2<Integer, Integer, Integer> add =
            new Function2<Integer, Integer, Integer>() {
                public Integer apply(final Integer a, final Integer b) {
                    return a + b;
                }
            };

    private static Function2<Integer, Integer, Integer> sub =
            new Function2<Integer, Integer, Integer>() {
                public Integer apply(final Integer a, final Integer b) {
                    return a - b;
                }
            };

    private Predicate<Integer> positive = new Predicate<Integer>() {
        public Boolean apply(Integer arg) {
            return arg != null && arg > 0;
        }
    };

    @Test
    public void testMap() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        ArrayList<Integer> res = Collections.map(square, a);
        assertEquals(Arrays.asList(1, 4, 9), res);

    }

    @Test
    public void testFilter() {
        List<Integer> a = Arrays.asList(1, -2, 3, -4);
        ArrayList<Integer> res = Collections.filter(positive, a);
        assertEquals(2, res.size());
        assertEquals(Arrays.asList(1, 3), res);
    }

    @Test
    public void testTakeWhile() {
        List<Integer> a = Arrays.asList(1, 2, -3, 5);
        ArrayList<Integer> res = Collections.takeWhile(positive, a);
        assertEquals(2, res.size());
        assertEquals(Arrays.asList(1, 2), res);
    }

    @Test
    public void testTakeUnless() {
        List<Integer> a = Arrays.asList(-1, -2, -3, 5, 5);
        ArrayList<Integer> res = Collections.takeUnless(positive, a);
        assertEquals(3, res.size());
        assertEquals(Arrays.asList(-1, -2, -3), res);
    }

    @Test
    public void testFoldL() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        Integer r = 7;
        assertEquals(r, Collections.foldl(add, 1, a));
    }

    @Test
    public void testFoldR() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        Integer r = 7;
        assertEquals(r, Collections.foldr(add, 1, a));
    }

    @Test
    public void testFoldLeftOrRight() {
        List<Integer> a = Arrays.asList(1, 2, 3, -4);
        int resFoldl = Collections.foldl(sub, 0, a);
        int resFoldr = Collections.foldr(sub, 0, a);
        assertFalse(resFoldl == resFoldr);
        assertEquals(-2, resFoldl);
        assertEquals(6, resFoldr);
    }

    private Function1<Object, Integer> widcardsTester =
            new Function1<Object, Integer>() {
                public Integer apply(final Object a) {
                    return 1;
                }
            };

    @Test
    public void testWildcardsFunction() {
        List<Integer> a = Arrays.asList(-1, -2, -3, 5, 5);
        ArrayList<Number> res = Collections.<Number, Number>map(widcardsTester, a);
        assertEquals(Arrays.asList(1, 1, 1, 1, 1), res);

    }

    private Predicate<Object> notNull = new Predicate<Object>() {
        public Boolean apply(Object arg) {
            return arg != null;
        }
    };

    @Test
    public void testWildcardsPredicate() {
        List<Integer> a = Arrays.asList(null, 5, null);
        ArrayList<Integer> res = Collections.filter(notNull, a);
        assertEquals(1, res.size());

    }


}
