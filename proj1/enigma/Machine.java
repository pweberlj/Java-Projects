package enigma;

import java.util.Collection;
import java.util.Iterator;
import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Preston Weber
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _numFixedRotors = numRotors - pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new Rotor[numRotors()];

        for (int i = 0; i < rotors.length; ++i) {
            Iterator iterator = _allRotors.iterator();
            while (iterator.hasNext()) {
                Rotor rotor = (Rotor) iterator.next();
                if (rotor.name().equals(rotors[i])) {
                    _rotors[i] = rotor;
                    break;
                }
            }
        }
        if (!_rotors[0].reflecting()) {
            throw error("Improper position of Reflector.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i++) {
            _rotors[i + 1].set(setting.charAt(i));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] canAdvance = new boolean[_pawls];
        canAdvance[_pawls - 1] = true;
        int movingRotors = 0;

        for (int i = _pawls - 1; i <= _rotors.length - 2; i++) {
            if (_rotors[i + 1].atNotch() && _rotors[i].rotates()) {
                canAdvance[movingRotors] = true;
                canAdvance[movingRotors + 1] = true;
            }
            movingRotors++;
        }

        int count = 0;
        for (int j = _numFixedRotors; j <= _rotors.length - 1; j++) {
            if (canAdvance[count]) {
                _rotors[j].advance();
            }
            count++;
        }

        int convert = _plugboard.permute(c);
        for (int a = _rotors.length - 1; a >= 0; a--) {
            convert = _rotors[a].convertForward(convert);
        }
        for (int b = 1; b < _rotors.length; b++) {
            convert = _rotors[b].convertBackward(convert);
        }
        convert = _plugboard.permute(convert);
        return convert;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char[] msgConverted = new char[msg.length()];
        for (int i = 0; i < msg.length(); i++) {
            msgConverted[i] = msg.charAt(i);
        }

        int[] index = new int[msgConverted.length];
        for (int a = 0; a < msgConverted.length; a++) {
            index[a] = _alphabet.toInt(msgConverted[a]);
        }

        for (int b = 0; b < index.length; b++) {
            index[b] = convert(index[b]);
        }

        for (int c = 0; c < index.length; c++) {
            msgConverted[c] = _alphabet.toChar(index[c]);
        }

        String converted = "";
        for (int d = 0; d < msgConverted.length; d++) {
            converted += Character.toString(msgConverted[d]);
        }

        return converted;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private final int _numRotors;

    /** Number of pawls. */
    private final int _pawls;

    /** Collection of all the avaliable rotors. */
    private final Collection<Rotor> _allRotors;

    /** Rotor slots. */
    private Rotor[] _rotors;

    /** The plugboard. */
    private Permutation _plugboard;

    /** The number of fixed rotors. */
    private int _numFixedRotors;

    /** Method to find the mod of a given number.
     * @param x the number to be modded.
     * @return the modded number.*/
    private int mod(int x) {
        int modded = x % _alphabet.size();
        if (modded < 0) {
            modded += _alphabet.size();
        }
        return modded;
    }
}
