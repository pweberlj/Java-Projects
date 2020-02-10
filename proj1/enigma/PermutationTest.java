package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Preston Weber
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testAddCycle() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        String [] pAdd = {"PNH", "ABDFIKLZYXW", "JC", "O"};
        String newCycle = "O";
        p.addCycle(newCycle);

        assertArrayEquals(pAdd, p.cycles());

        Permutation x = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        String [] y = {"PNH", "ABDFIKLZYXW", "JC"};
        String z = "";
        x.addCycle(z);

        assertArrayEquals(y, x.cycles());
    }

    @Test
    public void testWrap() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        assertEquals(14, p.wrap(40));
        assertEquals(6, p.wrap(-20));
        assertEquals(0, p.wrap(0));
    }

    @Test
    public void testSize() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        assertEquals(26, p.size());

        Permutation a = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet("ABC"));
        assertEquals(3, a.size());

        Permutation zero = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet(""));
        assertEquals(0, zero.size());
    }

    @Test
    public void testCycles() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        String [] a = {"PNH", "ABDFIKLZYXW", "JC"};
        assertArrayEquals(a, p.cycles());
    }

    @Test
    public void testInvertChar() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        assertEquals(p.invert('B'), 'A');
        assertEquals(p.invert('G'), 'G');

        assertEquals(p.invert('J'), 'C');
    }

    @Test
    public void testPermuteChar() {
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", new Alphabet());
        assertEquals(p.permute('Z'), 'Y');
        assertEquals(p.permute('W'), 'A');

        assertEquals(p.permute('O'), 'O');

        assertEquals(p.permute('C'), 'J');
    }
}
