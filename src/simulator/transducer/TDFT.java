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
 * t: Q × (I U {^, &}) -> O* × Q × {-1, 0, +1}
 */
public class TDFT extends Transducer{

    // A transition function of the transducer.
    private Object[][][] transition;

    // A set of final states of the transducer.
    protected HashSet<String> finalStates;

    public TDFT(String initialState, HashMap<String, Integer> states, HashSet<String> finalStates, HashMap<String, Integer> inputAlphabet, HashSet<String> outputAlphabet, Object[][][] transition) {
        super(initialState, states, inputAlphabet, outputAlphabet);
        this.transition = transition;
        this.finalStates = finalStates;
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
        int currStateNum;
        int currInputNum;
        while (!finalStates.contains(currState) && currPosition >= 0 && currPosition < stringArray.length) {
            currStateNum = states.get(currState);
            currInputNum = inputAlphabet.get(String.valueOf(stringArray[currPosition]));
            currState = (String) transition[currStateNum][currInputNum][1];
            output += (String) transition[currStateNum][currInputNum][0];
            currPosition += (int) transition[currStateNum][currInputNum][2];
            if (currPosition < 0 || currPosition >= stringArray.length) {
                output = "";
            }
        }
        return output;
    }

    /**
     * Check whether input string is vaild
     * @param inputString input string
     * @return validation of input string
     */
    public Boolean vaildInput(String inputString) {
        char [] stringArray = inputString.toCharArray();
        Boolean validation = true;
        //check weather every symbol is in the input Alphabet
        for (char c : stringArray) {
            if (!inputAlphabet.containsKey(String.valueOf(c))) {
                validation = false;
            }
        }
        return validation;
    }
}
