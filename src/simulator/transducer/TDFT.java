package simulator.transducer;
import java.util.HashMap;

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
    private Object[][] transition;

    public TDFT(String initialState, HashMap<String, Integer> states, String[] finalStates, HashMap<String, Integer> inputAlphabet, String[] outputAlphabet, Object[][] transition) {
        super(initialState, states, finalStates, inputAlphabet, outputAlphabet);
        this.transition = transition;
    }
}
