package simulator.transducer;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Deterministic two-way finite state transducers (2DFT)
 * Formal definition: M = (Q, I, O, q, F, t)
 * where, Q is a finite set of states
 *        I is a finite set of input alphabet
 *        O is a finite set of output alphabet
 *        t is a transition function
 *        q is the initial state
 *        F is a subset of Q, a set of final states
 * t: Q × (I U {^, &}) -> O* × Q × {−1, 0, +1}
 */
public class TDFT extends Transducer{
    private Object[][][] transition;

    public TDFT(String initialState, HashMap<String, Integer> states, HashSet<String> finalStates, HashMap<String, Integer> inputAlphabet, String[] outputAlphabet, Object[][][] transition) {
        super(initialState, states, finalStates, inputAlphabet, outputAlphabet);
        this.transition = transition;
    }

    /**
     * Run 2DFT over input string
     * @param inputString input string
     * @return Output string
     */
    public String run(String inputString) {
        // add endmarkers
        String tapeString = "^" + inputString + "&";
        // switch to char array
        char [] stringArray = tapeString.toCharArray();
        // current state
        String currState = initialState;
        // current position on input tape
        int currPosition = 1;
        String output = "";
        while (!finalStates.contains(currState) && currPosition >= 0 && currPosition < stringArray.length) {
            currState = (String) transition[states.get(currState)][inputAlphabet.get(String.valueOf(stringArray[currPosition]))][1];
            output += (String) transition[states.get(currState)][inputAlphabet.get(String.valueOf(stringArray[currPosition]))][0];
            currPosition += (int) transition[states.get(currState)][inputAlphabet.get(String.valueOf(stringArray[currPosition]))][2];
        }
        return output;
    }
}
