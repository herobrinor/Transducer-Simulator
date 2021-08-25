package simulator;

import simulator.transducer.*;
import java.util.HashMap;

/** 
 * a decoder to tranlate encoding to tranducer recognisable information
*/
public class Decoder {

    public Decoder() {
    }

    public TDFT decodeTDFT(String encoding) {
        String initialState = "";
        String[] states = new String[0];
        String[] finalStates = new String[0];
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        HashMap<String, Integer> outputAlphabet = new HashMap<String, Integer>();
        Object[][] transition = new Object[0][0];

        TDFT transducer = new TDFT(initialState, states, finalStates, inputAlphabet, outputAlphabet, transition);
        return transducer;
    }
}
