package simulator.transducer;

import java.util.HashMap;

/**
 * It is a parent class for different transducers. It should not be instantiated.
 * It is extended by different transducer classes, and provides common attributes.
 */
public class Transducer {

    // the inital state from which the transducer starts its processing.
    protected String initialState;

    // A set of all the states of the transducer.
    protected String[] states;

    // A set of final states of the transducer.
    protected String[] finalStates;

    // transition function is not set in the transducer class beacause of huge difference between models

    // An input alphabet of the transducer.
    protected HashMap<String, Integer> inputAlphabet;
    
    // An output alphabet of the transducer.
    protected HashMap<String, Integer> outputAlphabet;

    // Left endmarker for the transducer.
    protected String endMarkerL = "^";

    // Right endmarker for the transducer.
    protected String endMarkerR = "&";

    public Transducer(String initialState, String[] states, String[] finalStates, HashMap<String, Integer> inputAlphabet, HashMap<String, Integer> outputAlphabet) {
        this.initialState = initialState;
        this.states = states;
        this.finalStates = finalStates;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
    }
}
