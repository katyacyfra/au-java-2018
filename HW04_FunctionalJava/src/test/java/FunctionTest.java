import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class FunctionTest {

    private Function1<Integer, Integer> square =
            new Function1<Integer, Integer>() {
                public Integer apply(final Integer a) {
                    return a*a;
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

    @Test
    public void testFunction1() {
        Integer a = 2;
        Integer b = 4;
        assertEquals(b, square.apply(a));
    }

    @Test
    public void testFunction2() {
        Integer a = 2;
        Integer b = 3;
        Integer c = 5;
        assertEquals(c, add.apply(a, b));
    }

    @Test
    public void testComposeF1() {
        Integer c = 16;
        assertEquals(c, square.compose(square).apply(2));
    }

    @Test
    public void testComposeF2() {
        Integer c = 25;
        assertEquals(c, add.compose(square).apply(2, 3));
    }

    @Test
    public void testBind1() {
        Integer c = 3;
        assertEquals(c, sub.bind1(5).apply(2));
    }

    @Test
    public void testBind2() {
        Integer c = -3;
        assertEquals(c, sub.bind2(5).apply(2));
    }

    @Test
    public  void testCurry() {
        Integer a = 2;
        Integer b = 3;
        Integer c = 5;
        Integer d = 10;
        assertEquals(c, add.apply(a, b));
        Function1<Integer, Function1<Integer, Integer>> addSix = add.curry();
        assertEquals(d, addSix.apply(6).apply(4));
    }


}