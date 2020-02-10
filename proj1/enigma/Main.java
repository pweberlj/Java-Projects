package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.error;

/** Enigma simulator.
 *  @author Preston Weber
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();

        String settings = _input.nextLine();
        if (!(settings.substring(0, 1).equals("*"))) {
            throw error("No Configuration Present.");
        }

        boolean aRotor;
        String[] rotors = new String[_numRotors + 1];
        String[] config = settings.split(" ");
        for (int a = 0; a < rotors.length; a++) {
            rotors[a] = config[a];
        }

        for (int i = 1; i < rotors.length; i++) {
            aRotor = false;
            for (int j = 0; j < _rotors.size(); j++) {
                if (rotors[i].equals(_rotors.get(j).name())) {
                    aRotor = true;
                }
            }
            if (!aRotor) {
                throw error("Unknown or Duplicated Rotor Name");
            }
        }

        setUp(m, settings);

        while (_input.hasNextLine()) {
            String nextL = _input.nextLine();
            if (nextL.startsWith("*")) {
                for (int i = 1; i < rotors.length; i++) {
                    aRotor = false;
                    for (int j = 0; j < _rotors.size(); j++) {
                        if (rotors[i].equals(_rotors.get(j).name())) {
                            aRotor = true;
                        }
                    }
                    if (!aRotor) {
                        throw error("Unknown or Duplicated Rotor Name");
                    }
                }
                settings = nextL;
                setUp(m, settings);
                continue;
            }
            nextL = nextL.replaceAll(" ", "");
            nextL = m.convert(nextL);
            printMessageLine(nextL);
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine());
            _numRotors = _config.nextInt();
            if (_numRotors <= 0) {
                throw error("Need more rotors.");
            }
            _numPawls  = _config.nextInt();
            if ((_numPawls <= 0) || (_numPawls >= _numRotors)) {
                throw error("Moving Rotors not configured properly.");
            }
            _config.nextLine();
            _reflectorSize = _alphabet.size() / 2;
            _rotors = readRotor();
            return new Machine(_alphabet, _numRotors, _numPawls, _rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor list, reading its description from _config. */
    private List<Rotor> readRotor() {
        try {
            List<Rotor> rotors = new ArrayList<>();
            while (_config.hasNextLine()) {
                String rotorLine = _config.nextLine();
                String[] rotorElements = rotorLine.split(" ");
                String rotorID = "";
                String isMoving = "";
                String notch = "";
                String cycles = "";
                int cycleCnt = 0;

                while (true) {
                    for (int i = 0; i < rotorElements.length; ++i) {
                        if (rotorElements[i].equals("")) {
                            continue;
                        }
                        if (rotorID.equals("")) {
                            rotorID = rotorElements[i];
                            continue;
                        } else if (isMoving.equals("")) {
                            isMoving = Character
                                    .toString(rotorElements[i].charAt(0));
                            notch = rotorElements[i].substring(1);
                            checkMove(isMoving, notch);
                            continue;
                        }
                        cycles = cycles + rotorElements[i] + " ";
                        ++cycleCnt;
                    }
                    if (!isMoving.equals("R") || cycleCnt == _reflectorSize) {
                        break;
                    }
                    rotorLine = _config.nextLine();
                    rotorElements = rotorLine.split(" ");
                }
                if (isMoving.equals("M")) {
                    rotors.add(new MovingRotor(rotorID,
                            new Permutation(cycles, _alphabet), notch));
                } else if (isMoving.equals("N")) {
                    rotors.add(new FixedRotor(rotorID,
                            new Permutation(cycles, _alphabet)));
                } else if (isMoving.equals("R")) {
                    rotors.add(new Reflector(rotorID,
                            new Permutation(cycles, _alphabet)));
                }
            }
            for (int i = 0; i < rotors.size(); i++) {
                if (rotors.get(i).rotates()) {
                    rotors.get(i).setAlphabet(_alphabet);
                }
            }
            return rotors;
        } catch (NoSuchElementException excp) {
            throw error("Bad Rotor Description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] inputParams = settings.split(" ");

        int numRotors = M.numRotors();
        String[] rotors = new String[numRotors];
        for (int i = 0; i < numRotors; ++i) {
            rotors[i] = inputParams[i + 1];
        }

        M.insertRotors(rotors);

        String rotorSetting = inputParams[numRotors + 1];
        M.setRotors(rotorSetting);

        String plugboard = "";
        for (int i = numRotors + 2; i < inputParams.length; ++i) {
            plugboard += inputParams[i] + " ";
        }
        plugboard = plugboard.trim();

        M.setPlugboard(new Permutation(plugboard, _alphabet));
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String split = "";
        int full = msg.length() / 5;
        int partial = msg.length() % 5;

        for (int i = 0; i < full; i++) {
            for (int j = 0; j < 5; j++) {
                if (msg.length() == 1) {
                    split += Character.toString(msg.charAt(0));
                    break;
                }
                split += Character.toString(msg.charAt(0));
                msg = msg.substring(1);
                if (j == 4) {
                    split += " ";
                }
            }
        }
        if (partial > 0) {
            for (int k = 0; k < partial; k++) {
                split += Character.toString(msg.charAt(k));
            }
        }
        _output.println(split);
    }

    /** Checks for proper ASCII approved letters placed at notch
     * and a correct symbol for movement of rotor.
     * @param move = indicates movement
     * @param n = notch location
     */
    void checkMove(String move, String n) {
        String isM = move;
        String notch = n;

        String m = "MNR";
        String[] moves = new String[3];
        for (int l = 0; l < 3; l++) {
            moves[l] = Character.toString(m.charAt(l));
        }
        boolean mCorrect = false;

        for (int x = 0; x < notch.length(); x++) {
            if (!_alphabet.contains(notch.charAt(x))) {
                throw error("Notch not in alphabet.");
            }
        }
        for (int p = 0; p < 3; p++) {
            if (isM.toUpperCase().equals(moves[p])) {
                mCorrect = true;
            }
        }
        if (!mCorrect) {
            throw error("Configuration File has an improper rotor indication.");
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _numPawls;

    /** List of rotors to use in configuration. */
    private List<Rotor> _rotors;

    /** Size of the reflector. */
    private int _reflectorSize;
}
