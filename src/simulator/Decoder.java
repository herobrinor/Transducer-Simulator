package simulator;

import simulator.transducer.*;
import simulator.util.ParseTree;

import java.util.Arrays;
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
        String pattern = "\\(\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{[A-Za-z0-9](,[A-Za-z0-9])*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{\\([A-Za-z0-9]*(,[A-Za-z0-9^&-]*){4}\\)(,\\([A-Za-z0-9]*(,[A-Za-z0-9^&-]*){4}\\))*\\},\\{[A-Za-z0-9]*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\}\\)";
        Boolean validation = encoding.matches(pattern);
        return validation;
    }

    /**
     * Decode function for MSOT
     * encoding format of MSOT: ({I},{O},{C},{ϕ{c}(x);},{ϕ{c1,c2}{γ}(x,y);})
     * @param encoding Encoding of MSOT
     * @return An instance of MSOT
     */
    public MSOT decodeMSOT(String encoding) {
        //split the encoding string into different parts and storing in different arrays or hashmaps
        String[] sets = encoding.split("\\},\\{");
        String[] inAlpha = sets[0].substring(2).split(",");
        String[] outAlpha = sets[1].split(",");
        String[] copySetArray = sets[2].split(",");
        String[] nodeFormulaArray = sets[3].split(";");
        String[] edgeFormulaArray = sets[4].substring(0,sets[4].length()-2).split(";");
        HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        HashMap<String, Integer> outputAlphabet = new HashMap<String, Integer>();
        HashMap<String, Integer> copySet = new HashMap<String, Integer>();
        ParseTree[] nodeFormula = new ParseTree[copySetArray.length];
        ParseTree[][][] edgeFormula = new ParseTree[copySetArray.length][copySetArray.length][outAlpha.length];
        for (int i = 0; i < inAlpha.length; i++) {
            inputAlphabet.put(inAlpha[i],i);
        }
        for (int i = 0; i < outAlpha.length; i++) {
            outputAlphabet.put(outAlpha[i],i);
        }
        for (int i = 0; i < copySetArray.length; i++) {
            copySet.put(copySetArray[i],i);
        }
        for (int i = 0; i < nodeFormulaArray.length; i++) {
            String[] formula = nodeFormulaArray[i].split("=");
            int copuSetNum = copySet.get(formula[0].substring(2, formula[0].length()-1));
            nodeFormula[copuSetNum] = new ParseTree(formula[1]);
        }
        for (int i = 0; i < edgeFormulaArray.length; i++) {
            System.out.println(edgeFormulaArray[i]);
            String[] formula = edgeFormulaArray[i].split("=");
            System.out.println(Arrays.toString(formula));
            String[] numInfo = formula[0].split("\\}\\{");
            String[] cSet = numInfo[0].split(",");
            int copuSetNum1 = copySet.get(cSet[0].substring(2));
            int copuSetNum2 = copySet.get(cSet[1]);
            int outputNum = outputAlphabet.get(numInfo[1].substring(0,numInfo[1].length()-1));
            edgeFormula[copuSetNum1][copuSetNum2][outputNum] = new ParseTree(formula[1]);
        }
        //construct an instance of MSOT
        MSOT transducer = new MSOT(inputAlphabet, outputAlphabet, copySet, nodeFormula, edgeFormula);
        return transducer;
    }

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
        // String[] sets = encoding.split("\\},\\{");
        // String[] statesArray = sets[0].substring(2).split(",");
        // String[] inAlpha = sets[1].split(",");
        // String[] outAlpha = sets[2].split(",");
        // String[] tranFunc = sets[3].split("\\),\\(");
        // String initialState = sets[4];
        // String[] finalStatesArray = sets[5].substring(0,sets[5].length()-2).split(",");

        // HashMap<String, Integer> states = new HashMap<String, Integer>();
        // HashMap<String, Integer> inputAlphabet = new HashMap<String, Integer>();
        // HashSet<String> finalStates = new HashSet<String>();
        // HashSet<String> outputAlphabet = new HashSet<String>();
        // for (int i = 0; i < statesArray.length; i++) {
        //     states.put(statesArray[i],i);
        // }
        // for (int i = 0; i < inAlpha.length; i++) {
        //     inputAlphabet.put(inAlpha[i],i);
        // }
        // for (int i = 0; i < outAlpha.length; i++) {
        //     outputAlphabet.add(outAlpha[i]);
        // }
        // for (int i = 0; i < finalStatesArray.length; i++) {
        //     finalStates.add(finalStatesArray[i]);
        // }
        // inputAlphabet.put("^",inAlpha.length);
        // inputAlphabet.put("&",inAlpha.length+1);
        // //store transition function
        // Object[][][] transition = new Object[statesArray.length][inAlpha.length+2][3];
        // String[] singleTrans;
        // for (int i = 0; i < tranFunc.length; i++) {
        //     if (i == 0) {
        //         singleTrans = tranFunc[i].substring(1).split(",");
        //     } else if (i == tranFunc.length-1) {
        //         singleTrans = tranFunc[i].substring(0,tranFunc[i].length()-1).split(",");
        //     } else {
        //         singleTrans = tranFunc[i].split(",");
        //     }           
        //     int state = states.get(singleTrans[0]);
        //     int symbol = inputAlphabet.get(singleTrans[1]);
        //     transition[state][symbol][0] = singleTrans[2];
        //     transition[state][symbol][1] = singleTrans[3];
        //     transition[state][symbol][2] = Integer.parseInt(singleTrans[4]);
        // }

        // String[][] returnInfo = new String[statesArray.length][1];
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
