package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Preston Weber
 */
class MovingRotor extends Rotor {

    /** Variable notches indicating the positions of the notches. */
    private String _notches;

    /** Private variable alphabet for the alphabet of rotor. */
    private Alphabet _alphabet;

    /** Private variable permutation indicating
     * the permutations of the rotor. */
    private Permutation _permutation;

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _permutation = perm;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        super.set(super.mod(super.setting() + 1));
    }

    @Override
    boolean atNotch() {
        boolean atN = false;

        for (int i = 0; i < _notches.length(); i++) {
            if (_alphabet.toInt(_notches.charAt(i)) == super.setting()) {
                atN = true;
            }
        }
        return atN;
    }

    @Override
    void setAlphabet(Alphabet alphabet) {
        _alphabet = alphabet;
    }
}
