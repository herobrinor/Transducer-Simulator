package simulator;

import simulator.transducer.*;

import java.util.HashMap;
import java.util.HashSet;

/** 
 * A decoder to tranlate encoding to tranducer recognisable information
*/
public class Decoder {

    public Decoder() {
    }

    /**
     * Decode function for 2DFT
     * encoding format of 2DFT: ({Q},{I},{O},{t:(q,a,b,q,n)},{q},{F})
     * @param encoding Encoding of 2DFT
     * @return An instance of 2DFT
     */
    public TDFT decodeTDFT(String encoding) {
        //split the encoding string into different parts and storing in different arrays or hashmaps
        String[] sets = encoding.split("\\},\\{");
        String initialState = sets[4];
        String[] statesArray = sets[0].substring(2).split(",");
        String[] finalStatesArray = sets[5].substring(0,sets[5].length()-2).split(",");
        String[] inAlpha = sets[1].split(",");
        String[] outAlpha = sets[2].split(",");
        String[] tranFunc = sets[3].split("\\),\\(");
        HashMap<String, Integer> states = new HashMap<String, Integer>();
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        HashSet<String> finalStates = new HashSet<String>();
        HashSet<String> outputAlphabet = new HashSet<String>();
        for (int i = 0; i < statesArray.length; i++) {
            states.put(statesArray[i],i);
        }
        for (int i = 0; i < inAlpha.length; i++) {
            inputAlphabet.put(inAlpha[i],i);
        }
        for (int i = 0; i < outAlpha.length; i++) {
            outputAlphabet.add(outAlpha[i]);
        }
        for (int i = 0; i < finalStatesArray.length; i++) {
            finalStates.add(finalStatesArray[i]);
        }
        inputAlphabet.put("^",inAlpha.length);
        inputAlphabet.put("&",inAlpha.length+1);
        //store transition function
        Object[][][] transition = new Object[statesArray.length][inAlpha.length+2][3];
        String[] singleTrans;
        for (int i = 0; i < tranFunc.length; i++) {
            if (i == 0) {
                singleTrans = tranFunc[i].substring(1).split(",");
            } else if (i == tranFunc.length-1) {
                singleTrans = tranFunc[i].substring(0,tranFunc[i].length()-1).split(",");
            } else {
                singleTrans = tranFunc[i].split(",");
            }           
            int state = states.get(singleTrans[0]);
            int symbol = inputAlphabet.get(singleTrans[1]);
            transition[state][symbol][0] = singleTrans[2];
            transition[state][symbol][1] = singleTrans[3];
            transition[state][symbol][2] = Integer.parseInt(singleTrans[4]);
        }
        //construct an instance of 2DFT
        TDFT transducer = new TDFT(initialState, states, finalStates, inputAlphabet, outputAlphabet, transition);
        return transducer;
    }

    /**
     * Check whether the encoding of 2DFT is vaild
     * @param encoding Encoding of 2DFT
     * @return validation of encoding
     */
    public Boolean vaildTDFT(String encoding) {
        // regular expression of 2DFT encoding
        String pattern = "\\(\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{[A-Za-z0-9](,[A-Za-z0-9])*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{\\([A-Za-z0-9]*(,[A-Za-z0-9^&]*){4}\\)(,\\([A-Za-z0-9]*(,[A-Za-z0-9^&]*){4}\\))*\\},\\{[A-Za-z0-9]*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\}\\)";
        Boolean validation = encoding.matches(pattern);
        return validation;
    }

    /**
     * Decode function for MSOT
     * encoding format of MSOT: ({Q},{I},{O},{t:(q,a,b,q,n)},{q},{F})
     * @param encoding Encoding of MSOT
     * @return An instance of MSOT
     */
    // public MSOT decodeMSOT(String encoding) {
    //     TODO:
    // }

    /**
     * Check whether the encoding of 2DFT is vaild
     * @param encoding Encoding of 2DFT
     * @return validation of encoding
     */
    public Boolean vaildMSOT(String encoding) {
        //TODO: regular expression of MSOT encoding
        String pattern = "";
        Boolean validation = encoding.matches(pattern);
        return validation;
    }

    /**
     * Decode function for SST
     * encoding format of SST: ({Q},{I},{O},{X},{q},{f:(q,b)},{t1:(q,a,q)},{t2:(q,a,x,b)})
     * @param encoding Encoding of SST
     * @return An instance of SST
     */
    public SST decodeSST(String encoding) {
        //split the encoding string into different parts and storing in different arrays or hashmaps
        String[] sets = encoding.split("\\},\\{");
        String[] statesArray = sets[0].substring(2).split(",");
        String[] inAlpha = sets[1].split(",");
        String[] outAlpha = sets[2].split(",");
        String[] varArray = sets[3].split(",");
        String initialState = sets[4];
        String[] outputFunc = sets[5].split("\\),\\(");
        String[] tranFunc = sets[6].split("\\),\\(");
        String[] updateFunc = sets[7].substring(0,sets[7].length()-2).split("\\),\\(");
        HashMap<String, Integer> states = new HashMap<String, Integer>();
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        HashSet<String> outputAlphabet = new HashSet<String>();
        HashMap<String, Integer> variables = new HashMap<String, Integer>();
        for (int i = 0; i < statesArray.length; i++) {
            states.put(statesArray[i],i);
        }
        for (int i = 0; i < inAlpha.length; i++) {
            inputAlphabet.put(inAlpha[i],i);
        }
        for (int i = 0; i < outAlpha.length; i++) {
            outputAlphabet.add(outAlpha[i]);
        }
        for (int i = 0; i < varArray.length; i++) {
            variables.put(varArray[i],i);
        }

        String[] partialOutput = new String[statesArray.length];
        String[][] stateTransition = new String[statesArray.length][inAlpha.length];
        String[][][] variableUpdate = new String[statesArray.length][inAlpha.length][varArray.length];

        String[] singleTrans;

        if (outputFunc.length == 1) {
            singleTrans = outputFunc[0].substring(1,outputFunc[0].length()-1).split(",");
            int state = states.get(singleTrans[0]);
            partialOutput[state] = singleTrans[1];
        } else {
            for (int i = 0; i < outputFunc.length; i++) {
                if (i == 0) {
                    singleTrans = outputFunc[i].substring(1).split(",");
                } else if (i == outputFunc.length-1) {
                    singleTrans = outputFunc[i].substring(0,outputFunc[i].length()-1).split(",");
                } else {
                    singleTrans = outputFunc[i].split(",");
                }           
                int state = states.get(singleTrans[0]);
                partialOutput[state] = singleTrans[1];
            }
        }
        
        if (tranFunc.length == 1) {
            singleTrans = tranFunc[0].substring(1,tranFunc[0].length()-1).split(",");
            int state = states.get(singleTrans[0]);
            int symbol = inputAlphabet.get(singleTrans[1]);
            stateTransition[state][symbol] = singleTrans[2];
        } else {
            for (int i = 0; i < tranFunc.length; i++) {
                if (i == 0) {
                     singleTrans = tranFunc[i].substring(1).split(",");
                 } else if (i == tranFunc.length-1) {
                    singleTrans = tranFunc[i].substring(0,tranFunc[i].length()-1).split(",");
                } else {
                     singleTrans = tranFunc[i].split(",");
                }      
                int state = states.get(singleTrans[0]);
                int symbol = inputAlphabet.get(singleTrans[1]);
                stateTransition[state][symbol] = singleTrans[2];
            }
        }
        if (updateFunc.length == 1) {
            singleTrans = updateFunc[0].substring(1,updateFunc[0].length()-1).split(",");
            int state = states.get(singleTrans[0]);
            int symbol = inputAlphabet.get(singleTrans[1]);
            int var = variables.get(singleTrans[2]);
            variableUpdate[state][symbol][var] = singleTrans[3];
        } else {
            for (int i = 0; i < updateFunc.length; i++) {
                if (i == 0) {
                    singleTrans = updateFunc[i].substring(1).split(",");
                } else if (i == updateFunc.length-1) {
                     singleTrans = updateFunc[i].substring(0,updateFunc[i].length()-1).split(",");
                 } else {
                     singleTrans = updateFunc[i].split(",");
                }          
                int state = states.get(singleTrans[0]);
                int symbol = inputAlphabet.get(singleTrans[1]);
                int var = variables.get(singleTrans[2]);
                variableUpdate[state][symbol][var] = singleTrans[3];
            }
        }

        SST sst = new SST(initialState, states, inputAlphabet, outputAlphabet, variables, partialOutput, stateTransition, variableUpdate);
        return sst;
    }

    /**
     * Check whether the encoding of SST is vaild
     * @param encoding Encoding of SST
     * @return validation of encoding
     */
    public Boolean vaildSST(String encoding) {
        // regular expression of SST encoding
        String pattern = "\\(\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\}(,\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\}){2},\\{[A-Za-z0-9](,[A-Za-z0-9])*\\},\\{[A-Za-z0-9]*\\},\\{\\([A-Za-z0-9]*,[A-Za-z0-9]*\\)(,\\([A-Za-z0-9]*,[A-Za-z0-9]*\\))*\\},\\{\\([A-Za-z0-9]*(,[A-Za-z0-9]*){2}\\)(,\\([A-Za-z0-9]*(,[A-Za-z0-9]*){2}\\))*\\},\\{\\([A-Za-z0-9]*,[A-Za-z0-9]*,[A-Za-z0-9],[A-Za-z0-9]*\\)(,\\([A-Za-z0-9]*,[A-Za-z0-9]*,[A-Za-z0-9],[A-Za-z0-9]*\\))*\\}\\)";
        Boolean validation = encoding.matches(pattern);
        return validation;
    }

    /**
     * Translate the encoding from 2DFT to SST
     * @param encoding Encoding of 2DFT
     * @return Encoding of SST
     */
    public String fromTDFTtoSST(String encoding){
        //TODO: translation function
        //split the encoding string into different parts and storing in different arrays or hashmaps
        String[] sets = encoding.split("\\},\\{");
        String[] statesArray = sets[0].substring(2).split(",");
        String[] inAlpha = sets[1].split(",");
        String[] outAlpha = sets[2].split(",");
        String[] tranFunc = sets[3].split("\\),\\(");
        String initialState = sets[4];
        String[] finalStatesArray = sets[5].substring(0,sets[5].length()-2).split(",");

        HashMap<String, Integer> states = new HashMap<String, Integer>();
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        HashSet<String> finalStates = new HashSet<String>();
        HashSet<String> outputAlphabet = new HashSet<String>();
        for (int i = 0; i < statesArray.length; i++) {
            states.put(statesArray[i],i);
        }
        for (int i = 0; i < inAlpha.length; i++) {
            inputAlphabet.put(inAlpha[i],i);
        }
        for (int i = 0; i < outAlpha.length; i++) {
            outputAlphabet.add(outAlpha[i]);
        }
        for (int i = 0; i < finalStatesArray.length; i++) {
            finalStates.add(finalStatesArray[i]);
        }
        inputAlphabet.put("^",inAlpha.length);
        inputAlphabet.put("&",inAlpha.length+1);
        //store transition function
        Object[][][] transition = new Object[statesArray.length][inAlpha.length+2][3];
        String[] singleTrans;
        for (int i = 0; i < tranFunc.length; i++) {
            if (i == 0) {
                singleTrans = tranFunc[i].substring(1).split(",");
            } else if (i == tranFunc.length-1) {
                singleTrans = tranFunc[i].substring(0,tranFunc[i].length()-1).split(",");
            } else {
                singleTrans = tranFunc[i].split(",");
            }           
            int state = states.get(singleTrans[0]);
            int symbol = inputAlphabet.get(singleTrans[1]);
            transition[state][symbol][0] = singleTrans[2];
            transition[state][symbol][1] = singleTrans[3];
            transition[state][symbol][2] = Integer.parseInt(singleTrans[4]);
        }

        String[][] returnInfo = new String[statesArray.length][1];
        return "";
    }

    /**
     * Translate the encoding from SST to 2DFT
     * @param encoding Encoding of SST
     * @return Encoding of 2DFT
     */
    public String fromSSTtoTDFT(String encoding){
        //TODO: translation function
        return "";
    }
}
