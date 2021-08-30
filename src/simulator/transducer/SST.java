package simulator.transducer;

import java.util.HashMap;
import java.util.HashSet;

public class SST extends Transducer{
    
    public SST(String initialState, HashMap<String, Integer> states, HashSet<String> finalStates, HashMap<String, Integer> inputAlphabet, String[] outputAlphabet, Object[][][] transition) {
        super(initialState, states, finalStates, inputAlphabet, outputAlphabet);
    }

}
