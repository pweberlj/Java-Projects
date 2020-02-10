package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Preston Weber
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = split(cycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    void addCycle(String cycle) {
        if (cycle.length() == 0) {
            return;
        }

        int newLen = _cycles.length + 1;
        String[] addedCycle = new String[newLen];

        for (int i = 0; i < _cycles.length; i++) {
            addedCycle[i] = _cycles[i];
        }

        addedCycle[_cycles.length] = cycle;
        _cycles = addedCycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Returns the cycles. */
    String[] cycles() {
        return _cycles;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char pIn = _alphabet.toChar(p);
        char pOut = permute(pIn);
        return  _alphabet.toInt(pOut);
    }

    /** Return the result of applying the inverse of this permutation
     *  to C modulo the alphabet size. */
    int invert(int c) {
        char cIn = _alphabet.toChar(c);
        char cOut = invert(cIn);
        return _alphabet.toInt(cOut);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        char result = p;

        if (!derangement()) {
            for (int a = 0; a < _cycles.length; a++) {
                if (_cycles[a].length() == 1) {
                    if (_cycles[a].charAt(0) == p) {
                        return _cycles[a].charAt(0);
                    }
                }
            }
        }

        for (int i = 0; i < _cycles.length; i++) {
            for (int j = 0; j < _cycles[i].length(); j++) {
                if (_cycles[i].charAt(j) == p) {
                    if (j == _cycles[i].length() - 1) {
                        result = _cycles[i].charAt(0);
                    } else {
                        result = _cycles[i].charAt(j + 1);
                    }
                }
            }
        }
        return result;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        char result = c;

        if (!derangement()) {
            for (int a = 0; a < _cycles.length; a++) {
                if (_cycles[a].length() == 1) {
                    if (_cycles[a].charAt(0) == c) {
                        return _cycles[a].charAt(0);
                    }
                }
            }
        }

        for (int i = 0; i < _cycles.length; i++) {
            for (int j = 0; j < _cycles[i].length(); j++) {
                if (_cycles[i].charAt(j) == c) {
                    if (j == 0) {
                        result = _cycles[i].charAt(_cycles[i].length() - 1);
                    } else {
                        result = _cycles[i].charAt(j - 1);
                    }
                }
            }
        }
        return result;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _cycles.length; i++) {
            if (_cycles[i].length() == 1) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles of this permutation. */
    private String[] _cycles;

    /** Private method to split the String and turn it into a String[].
     * @param cycle input String
     * @return String []
     */
    private String[] split(String cycle) {
        String temporary = cycle.trim();
        temporary = temporary.replace(")(", " ");
        temporary = temporary.replace("(", "");
        temporary = temporary.replace(")", "");
        return temporary.split(" ");
    }
}
