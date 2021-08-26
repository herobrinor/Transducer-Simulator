package simulator;

import simulator.transducer.*;
import java.util.HashMap;

/** 
 * A decoder to tranlate encoding to tranducer recognisable information
*/
public class Decoder {

    public Decoder() {
    }

    /**
     * Decode function for 2DFT
     * encoding of 2DFT : ({Q},{I},{O},{t:(q,a,b,q,n)},{q},{F})
     * @param encodings Encoding of 2DFT
     * @return An instance of 2DFT
     */
    public TDFT decodeTDFT(String encoding) {
        //split the encoding string into different parts and storing in different arrays or hashmaps
        String[] sets = encoding.split("},{");
        String initialState = sets[4];
        String[] statesArray = sets[0].split(",");
        String[] finalStates = sets[5].split(",");
        String[] inAlpha = sets[1].split(",");
        String[] outputAlphabet = sets[2].split(",");
        String[] tranFunc = sets[3].split("),(");
        HashMap<String, Integer> states = new HashMap<String, Integer>();
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        Object[][] transition = new Object[0][0];
        for (int i = 0; i < statesArray.length; i++) {
            states.put(statesArray[i],i);
        }
        for (int i = 0; i < inAlpha.length; i++) {
            inputAlphabet.put(inAlpha[i],i);
        }
        String[] singleTrans;
        Object[] values = new Object[3];
        for (int i = 0; i < tranFunc.length; i++) {
            if (i == 0) {
                singleTrans = tranFunc[i].substring(1).split(",");
            } else if (i == tranFunc.length-1) {
                singleTrans = tranFunc[i].substring(0,tranFunc[i].length()-1).split(",");
            } else {
                singleTrans = tranFunc[i].split(",");
            }
            values[0] = singleTrans[2];
            values[1] = singleTrans[3];
            values[2] = singleTrans[4];
            transition[states.get(singleTrans[0])][inputAlphabet.get(singleTrans[1])] = values;
        }
        //construct an instance of 2DFT
        TDFT transducer = new TDFT(initialState, states, finalStates, inputAlphabet, outputAlphabet, transition);
        return transducer;
    }
}
