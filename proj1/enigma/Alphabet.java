package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Preston Weber
 */
class Alphabet {

    /** An alphabet of characters.*/
    private String _chars;
    /** A character list.*/
    private char[] _charAt;

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
        _charAt = new char[_chars.length()];
        for (int i = 0; i < _chars.length(); i++) {
            _charAt[i] += _chars.charAt(i);
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if preprocess(CH) is in this alphabet.
     * EDIT: Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int i = 0; i < _charAt.length; i++) {
            if (_charAt[i] == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _charAt[index];
    }

    /** Returns the index of character CH, which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _chars.indexOf(ch);
    }
}
