import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

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

    private Predicate<Integer> positive = new Predicate<Integer>() {
        public Boolean apply(Integer arg) {
            return arg != null && arg > 0;
        }
    };

    @Test
    public void testMap() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        Integer b = 1;
        Integer c = 4;
        Integer d = 9;
        ArrayList<Integer> res = Collections.map(square, a);
        assertEquals(b, res.get(0));
        assertEquals(c, res.get(1));
        assertEquals(d, res.get(2));
    }

    @Test
    public void testFilter() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(-2);
        a.add(3);
        a.add(-4);
        Integer b = 1;
        Integer c = 3;
        ArrayList<Integer> res = Collections.filter(positive, a);
        assertEquals(2, res.size());
        assertEquals(b, res.get(0));
        assertEquals(c, res.get(1));
    }

    @Test
    public void testTakeWhile() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(-3);
        a.add(5);
        Integer b = 1;
        Integer c = 2;
        ArrayList<Integer> res = Collections.takeWhile(positive, a);
        assertEquals(2, res.size());
        assertEquals(b, res.get(0));
        assertEquals(c, res.get(1));
    }

    @Test
    public void testTakeUnless() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(-1);
        a.add(-2);
        a.add(-3);
        a.add(5);
        a.add(5);
        Integer b = -1;
        Integer c = -2;
        Integer d = -3;
        ArrayList<Integer> res = Collections.takeUnless(positive, a);
        assertEquals(3, res.size());
        assertEquals(b, res.get(0));
        assertEquals(c, res.get(1));
        assertEquals(d, res.get(2));
    }

    @Test
    public void testFoldL() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        Integer r = 7;
        assertEquals(r, Collections.foldl(add, 1, a));
    }

    @Test
    public void testFoldR() {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        Integer r = 7;
        assertEquals(r, Collections.foldr(add, 1, a));
    }
}
