package simulator.transducer;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Deterministic MSO transducers (MSOT)
 * Formal definition: M = (Q, I, O, q, F, t)
 * where, I is a finite set of input alphabet
 *        O is a finite set of output alphabet
 *        C is a finite copy set
 *        φ^{c}(x) is vertex formulas
 *        φ^{c,d}_{a}(x,y) is edge formulas
 * t: Q × (I U {^, &}) -> O* × Q × {-1, 0, +1}
 */
public class MSOT{
    public MSOT(String initialState, HashMap<String, Integer> states, HashSet<String> finalStates, HashMap<String, Integer> inputAlphabet, String[] outputAlphabet){

    }

    /**
     * Run MSOT over input string
     * @param inputString input string
     * @return Output string
     */
    public String run(String inputString) {
        //TODO
        String output = "";
        
        return output;
    }
}
