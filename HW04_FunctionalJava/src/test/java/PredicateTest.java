import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class PredicateTest {
    private Predicate<Object> isNull = new Predicate<Object>() {
        public Boolean apply(Object arg) {
            return arg == null;
        }
    };

    private Predicate<Object> notNull = new Predicate<Object>() {
        public Boolean apply(Object arg) {
            return arg != null;
        }
    };

    private Predicate<Integer> positive = new Predicate<Integer>() {
        public Boolean apply(Integer arg) {
            return arg != null && arg > 0;
        }
    };

    @Test
    public void testPredicate() {
        assertTrue(isNull.apply(null));
        assertFalse(isNull.apply(1));
        assertTrue(notNull.apply(5));
        assertTrue(notNull.apply(true));
        assertTrue(positive.apply(10));
        assertFalse(positive.apply(-222));
    }

    @Test
    public void testAnd() {
        assertTrue(positive.and(notNull).apply(6));
        assertFalse(positive.and(notNull).apply(0));
    }

    @Test
    public void testOr() {
        assertTrue(positive.or(notNull).apply(-4));
        assertFalse(positive.or(isNull).apply(-44));
    }


    @Test
    public void testNot() {
        assertTrue(positive.not().apply(-4));
        assertFalse(positive.not().apply(2));
    }

    @Test
    public void testAlwaysFalse() {
        assertFalse(Predicate.ALWAYS_FALSE.apply(true));
    }

    @Test
    public void testAlwaysTrue() {
        assertTrue(Predicate.ALWAYS_TRUE.apply(false));
    }

    private Predicate<Object> increment = new Predicate<Object>() {
        public Boolean apply(Object arg) {
            Incrementator i = (Incrementator) arg;
            i.incr();
            return true;
        }
    };

    class Incrementator {
        public int i = 0;
        public void incr() {
            i = i + 1;
        }
    }

    @Test
    public void testLazy() {
        Incrementator counter = new Incrementator();
        Predicate<Object> lazyTrue = Predicate.ALWAYS_TRUE.or(increment);
        assertTrue(Predicate.ALWAYS_TRUE.or(increment).apply(counter));
        assertEquals(0, counter.i);
        assertTrue(Predicate.ALWAYS_FALSE.or(increment).apply(counter));
        assertEquals(1, counter.i);
    }
}
