package simulator;

import simulator.transducer.*;
import simulator.util.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
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
        Node[] nodeFormula = new Node[copySetArray.length];
        Node[][][] edgeFormula = new Node[copySetArray.length][copySetArray.length][outAlpha.length];
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
            Node root = new Node(formula[1]);
            root.parse();
            nodeFormula[copuSetNum] = root;
        }
        for (int i = 0; i < edgeFormulaArray.length; i++) {
            String[] formula = edgeFormulaArray[i].split("=");
            String[] numInfo = formula[0].split("\\}\\{");
            String[] cSet = numInfo[0].split(",");
            int copuSetNum1 = copySet.get(cSet[0].substring(2));
            int copuSetNum2 = copySet.get(cSet[1]);
            int outputNum = outputAlphabet.get(numInfo[1].substring(0,numInfo[1].length()-1));
            Node root = new Node(formula[1]);
            root.parse();
            edgeFormula[copuSetNum1][copuSetNum2][outputNum] = root;
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
     * @throws Exception
     */
    public SST decodeSST(String encoding) throws Exception {
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
            if (singleTrans.length == 3) {
                variableUpdate[state][symbol][var] = "";
            } else {
                variableUpdate[state][symbol][var] = singleTrans[3];
            }
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
                if (singleTrans.length == 3) {
                    variableUpdate[state][symbol][var] = "";
                } else {
                    variableUpdate[state][symbol][var] = singleTrans[3];
                }
            }
        }

        for (int i = 0; i < variableUpdate.length; i++) {
            for (int j = 0; j < variableUpdate[i].length; j++) {
                Boolean[] count = new Boolean[varArray.length];
                for (int k = 0; k < count.length; k++) {
                    count[k] = false;
                }
                for (int index = 0; index < variableUpdate[i][j].length; index++) {
                    for (String varString : variables.keySet()) {
                        if (variableUpdate[i][j][index].contains(varString)) {
                            if (count[variables.get(varString)] == false) {
                                count[variables.get(varString)] = true;
                            } else {
                                throw new Exception("SST is not copyless.");
                            }
                        }
                    }
                }
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
        //store 2DFT
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
        //add state m
        states.put("m",statesArray.length+finalStatesArray.length-1);
        states.put("qerr",statesArray.length+finalStatesArray.length);
        //add endmarker
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
        // add new transtion functions to make 2DFT start in left endmarker
        int initialStateNum = states.get(initialState);
        transition[initialStateNum][inAlpha.length][0] = "";
        transition[initialStateNum][inAlpha.length][1] = initialState;
        transition[initialStateNum][inAlpha.length][2] = 1;
        // add new transtion functions to make 2DFT end in right endmarker
        for (int i = 0; i < finalStatesArray.length; i++) {
            int state = states.get(finalStatesArray[i]);
            for (int j = 0; j < inAlpha.length; j++) {
                transition[state][j][0] = "";
                transition[state][j][1] = finalStatesArray[i];
                transition[state][j][2] = 1;
            }
            transition[state][inAlpha.length+1][0] = "";
            transition[state][inAlpha.length+1][1] = finalStatesArray[i]+"'";
            transition[state][inAlpha.length+1][2] = 1;
        }

        //construct new states in SST
        //use decimal number to represent a N-base number as the states of SST
        //starting state is identity function form Q to Q U {m, qerr}.
        int base = states.size();
        //store visited states, exploring states, variable-update function, state-transition function and pratial output function
        ArrayList<Integer> SSTState = new ArrayList<Integer>();
        Queue<Integer> stateQueue = new LinkedList<Integer>();
        ArrayList<String> partialOutputFunc =new ArrayList<>();
        ArrayList<Integer[]> stateTransitionFunc =new ArrayList<>();
        ArrayList<String[][]> variableUpdateFunc =new ArrayList<>();
        HashMap<String, Integer> variables = new HashMap<String, Integer>();
        String[] variableArray = new String[2*base-3];
        //initalise variable set in SST
        char varNum1 = 65;//symbol A
        char varNum2 = (char) (varNum1 + 1);
        int count = 0;
        while (count < base-2) {
            String var1 = String.valueOf(varNum1);
            String var2 = String.valueOf(varNum2);
            if (outputAlphabet.contains(var1)) {
                varNum1 += 1;
                varNum2 += 1;
            } else {
                if (outputAlphabet.contains(var2)) {
                    varNum2 += 1;
                } else {
                    variableArray[2*count] = var1;
                    variables.put(var1, count);
                    variableArray[2*count+1] = var2;
                    variables.put(var2, count);
                    varNum1 = (char) (varNum2 + 1);
                    varNum2 = (char) (varNum1 + 1);
                    count += 1;
                }
            }
        }
        while (count == base-2) {
            String var1 = String.valueOf(varNum1);
            if (outputAlphabet.contains(var1)) {
                varNum1 += 1;
            } else {
                variableArray[2*count] = var1;
                variables.put(var1, count);
                varNum1 += 1;
                count += 1;
            }
        }
        //simulate 2DFT on left endmarker
        int nextState = 0;
        String[] varUpLEM = new String[variableArray.length];
        // state except m
        for (int i = 0; i < base-2; i++) {
            Object[] transfunc = transition[i][inAlpha.length];
            if (transfunc[2] != null && (int)transfunc[2] == 1) {//careful about 0
                nextState += states.get((String)transfunc[1]) * Math.pow(base, i);
                varUpLEM[2*i] = (String)transfunc[0];
                varUpLEM[2*i+1] = (String)transfunc[0];
            } else {//map to qerr
                nextState += (base-1) * Math.pow(base, i);
                varUpLEM[2*i] = "";
                varUpLEM[2*i+1] = "";
            }
        }
        // state m
        nextState += states.get(initialState) * Math.pow(base, base-2);
        varUpLEM[2*(base-2)] = "";
        int firstState = nextState;
        //compute stateTransitionFunc and variable-update function combined with simulating on left endmarker
        String[][] varUp = new String[inAlpha.length][variableArray.length];
        Integer[] stateUp = new Integer[inAlpha.length];
        for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
            nextState = 0;
            // state except m
            for (int j = 0; j < base-2; j++) {
                String newVal1 = "";
                String newVal2 = "";
                Object[] transfunc = transition[j][i];
                //careful about 0
                if (transfunc[2] != null && (int)transfunc[2] == 1) {
                    nextState += states.get((String)transfunc[1]) * Math.pow(base, j);
                    newVal1 = (String)transfunc[0];
                    newVal2 = (String)transfunc[0];
                    varUp[i][2*j] = newVal1;
                    varUp[i][2*j+1] = newVal2;                        
                } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                    while (true) {
                        newVal1 += (String)transfunc[0];
                        newVal2 += (String)transfunc[0];
                        int leftState = states.get((String)transfunc[1]);
                        newVal1 += varUpLEM[2*leftState];
                        newVal2 += varUpLEM[2*leftState+1];
                        int tempCurrState = firstState;
                        for (int index = 0; index < leftState; index++) {
                            tempCurrState = tempCurrState / base;
                        }
                        int backState = tempCurrState % base;
                        transfunc = transition[backState][i];
                        if (transfunc[2] != null && (int)transfunc[2] == 1) {
                            newVal1 += transfunc[0];
                            newVal2 += transfunc[0];
                            varUp[i][2*j] = newVal1;
                            varUp[i][2*j+1] = newVal2;
                            nextState += states.get((String)transfunc[1]) * Math.pow(base, j);
                            break;
                        } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                            continue;
                        } else {
                            varUp[i][2*j] = "";
                            varUp[i][2*j+1] = "";
                            nextState += (base-1) * Math.pow(base, j);
                            break;
                        }   
                    }
                } else {//map to qerr
                    nextState += (base-1) * Math.pow(base, j);
                    varUp[i][j] = "";
                }
            }
            // state m
            String newVal = varUpLEM[2*(base-2)];
            int tempCurrState = firstState;
            for (int index = 0; index < base-2; index++) {
                tempCurrState = tempCurrState / base;
            }
            int mState = tempCurrState % base;
            Object[] mtransfunc = transition[mState][i];
            //careful about 0
            if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                nextState += states.get(mtransfunc[1]) * Math.pow(base, base-2);
                newVal += (String)mtransfunc[0];
                varUp[i][2*(base-2)] = newVal;
            } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                while (true) {
                    newVal = (String)mtransfunc[0];
                    int leftState = states.get(mtransfunc[1]);
                    newVal += varUpLEM[2*leftState+1];
                    tempCurrState = firstState;
                    for (int index = 0; index < leftState; index++) {
                        tempCurrState = tempCurrState / base;
                    }
                    int backState = tempCurrState % base;
                    mtransfunc = transition[backState][i];
                    if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                        newVal += mtransfunc[0];
                        varUp[i][2*(base-2)] = newVal;
                        nextState += states.get((String)mtransfunc[1]) * Math.pow(base, base-2);
                        break;
                    } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                        continue;
                    } else {
                        newVal = "";
                        varUp[i][2*(base-2)] = newVal;
                        nextState += (base-1) * Math.pow(base, base-2);
                        break;
                    }
                }
            } else {//map to qerr
                nextState += (base-1) * Math.pow(base, base-2);
                varUp[i][base-2] = "";
            }            

            stateUp[i] = nextState;
            if (!SSTState.contains(nextState) && !stateQueue.contains(nextState)) {
                stateQueue.offer(nextState);
            }
        }
        SSTState.add(firstState);
        stateTransitionFunc.add(stateUp);
        variableUpdateFunc.add(varUp);

        //compute partial output func
        String output = variableArray[2*(base-2)];
        String finalState = "m";
        int tempCurrState = firstState;
        for (int index = 0; index < base-2; index++) {
            tempCurrState = tempCurrState / base;
        }
        int mState = tempCurrState % base;
        Object[] mtransfunc = transition[mState][inAlpha.length+1];
        //careful about 0
        if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
            output += (String)mtransfunc[0];
        } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
            while (true) {
                output += (String)mtransfunc[0];
                int leftState = states.get((String)mtransfunc[1]);
                output += variableArray[2*leftState+1];
                tempCurrState = firstState;
                for (int index = 0; index < leftState; index++) {
                    tempCurrState = tempCurrState / base;
                }
                int backState = tempCurrState % base;
                mtransfunc = transition[backState][inAlpha.length+1];
                if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                    output += mtransfunc[0];
                    finalState = (String)mtransfunc[1];
                    break;
                } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                    continue;
                } else {
                    output = "";
                    break;
                }            
            }
        } else {//map to qerr
            finalState = "qerr";
            output = "";
        }

        if (finalStates.contains(finalState)) {
            partialOutputFunc.add(output);
        } else {
            partialOutputFunc.add("");
        }
        //loop to find all states
        while (!stateQueue.isEmpty()) {
            int currState = stateQueue.poll();
            SSTState.add(currState);
            varUp = new String[inAlpha.length][variableArray.length];
            stateUp = new Integer[inAlpha.length];
            for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
                nextState = 0;
                // state except m
                for (int j = 0; j < base-2; j++) {
                    String newVal1 = "";
                    String newVal2 = "";
                    Object[] transfunc = transition[j][i];
                    //careful about 0
                    if (transfunc[2] != null && (int)transfunc[2] == 1) {
                        nextState += states.get((String)transfunc[1]) * Math.pow(base, j);
                        newVal1 = (String)transfunc[0];
                        newVal2 = (String)transfunc[0];
                        varUp[i][2*j] = newVal1;
                        varUp[i][2*j+1] = newVal2;                        
                    } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                        while (true) {
                            newVal1 += (String)transfunc[0];
                            newVal2 += (String)transfunc[0];
                            int leftState = states.get((String)transfunc[1]);
                            newVal1 += variableArray[2*leftState];
                            newVal2 += variableArray[2*leftState+1];
                            tempCurrState = currState;
                            for (int index = 0; index < leftState; index++) {
                                tempCurrState = tempCurrState / base;
                            }
                            int backState = tempCurrState % base;
                            transfunc = transition[backState][i];
                            if (transfunc[2] != null && (int)transfunc[2] == 1) {
                                newVal1 += transfunc[0];
                                newVal2 += transfunc[0];
                                varUp[i][2*j] = newVal1;
                                varUp[i][2*j+1] = newVal2;
                                nextState += states.get((String)transfunc[1]) * Math.pow(base, j);
                                break;
                            } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                                continue;
                            } else {
                                newVal1 = "";
                                newVal2 = "";
                                varUp[i][2*j] = newVal1;
                                varUp[i][2*j+1] = newVal2;
                                nextState += (base-1) * Math.pow(base, j);
                            }                               
                            break;
                        }
                    } else {//map to qerr
                        nextState += (base-1) * Math.pow(base, j);
                        varUp[i][j] = "";
                    }
                }
                // state m
                String newVal = variableArray[2*(base-2)];
                tempCurrState = currState;
                for (int index = 0; index < base-2; index++) {
                    tempCurrState = tempCurrState / base;
                }
                mState = tempCurrState % base;
                Object[] transfunc = transition[mState][i];
                if (transfunc[2] != null && (int)transfunc[2] == 1) {
                    nextState += states.get((String)transfunc[1]) * Math.pow(base, base-2);
                    newVal += (String)transfunc[0];
                    varUp[i][2*(base-2)] = newVal;
                } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                    while (true) {
                        newVal = (String)transfunc[0];
                        int leftState = states.get((String)transfunc[1]);
                        newVal += variableArray[2*leftState+1];
                        tempCurrState = firstState;
                        for (int index = 0; index < leftState; index++) {
                            tempCurrState = tempCurrState / base;
                        }
                        int backState = tempCurrState % base;
                        transfunc = transition[backState][i];
                        if (transfunc[2] != null && (int)transfunc[2] == 1) {
                            newVal += transfunc[0];
                            varUp[i][2*(base-2)] = newVal;
                            nextState += states.get((String)transfunc[1]) * Math.pow(base, base-2);
                            break;
                        } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                            continue;
                        } else {
                            newVal = "";
                            varUp[i][2*(base-2)] = newVal;
                            nextState += (base-1) * Math.pow(base, base-2);
                            break;
                        }
                    }
                } else {//map to qerr
                    nextState += (base-1) * Math.pow(base, base-2);
                    varUp[i][base-2] = "";
                }
                stateUp[i] = nextState;
                if (!SSTState.contains(nextState) && !stateQueue.contains(nextState)) {
                    stateQueue.offer(nextState);
                }
            }
            stateTransitionFunc.add(stateUp);
            variableUpdateFunc.add(varUp);
            //compute partial output func
            output = variableArray[2*(base-2)];
            finalState = "m";
            tempCurrState = currState;
            for (int index = 0; index < base-2; index++) {
                tempCurrState = tempCurrState / base;
            }
            mState = tempCurrState % base;
            Object[] transfunc = transition[mState][inAlpha.length+1];
            //careful about 0
            if (transfunc[2] != null && (int)transfunc[2] == 1) {
                output += (String)transfunc[0];
            } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                while (true) {
                    output += (String)transfunc[0];
                    int leftState = states.get((String)transfunc[1]);
                    output += variableArray[2*leftState+1];
                    tempCurrState = currState;
                    for (int index = 0; index < leftState; index++) {
                        tempCurrState = tempCurrState / base;
                    }
                    int backState = tempCurrState % base;
                    transfunc = transition[backState][inAlpha.length+1];
                    if (transfunc[2] != null && (int)transfunc[2] == 1) {
                        output += transfunc[0];
                        finalState = (String)transfunc[1];
                        break;
                    } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                        continue;
                    } else {
                        output = "";
                        break;
                    }                            
                }
            } else {//map to qerr
                finalState = "qerr";
                output = "";
            }
            if (finalStates.contains(finalState)) {
                partialOutputFunc.add(output);
            } else {
                partialOutputFunc.add("");
            }
        }
        //construct SST encoding
        //initialise
        String SSTencoding = "(";
        Boolean isFirst = true;// flag for first element in brackets
        //add states set
        SSTencoding += "{";
        for (int i = 0; i < SSTState.size(); i++) {
            String state = "q" + String.valueOf(i);
            if (isFirst) {
                SSTencoding += state;
                isFirst = false;
            } else {
                SSTencoding +=  "," + state;
            }
            
        }
        SSTencoding += "},";
        //add input and output alphbet 
        SSTencoding = SSTencoding + "{" + sets[1] + "},{" + sets[2] + "},";
        //add variable sets
        isFirst = true;
        SSTencoding += "{";
        for (int i = 0; i < variableArray.length; i++) {
            if (isFirst) {
                SSTencoding += variableArray[i];
                isFirst = false;
            } else {
                SSTencoding += "," + variableArray[i];
            }
        }
        SSTencoding += "},";
        //add initial state
        SSTencoding += "{q0},";
        //add partial output function
        isFirst = true;
        SSTencoding += "{";
        for (int i = 0; i < SSTState.size(); i++) {
            if (!partialOutputFunc.get(i).equals("")) {
                if (isFirst) {
                    SSTencoding += "(q" + String.valueOf(i) + "," + partialOutputFunc.get(i) + ")";
                    isFirst = false;
                } else {
                    SSTencoding += ",(q" + String.valueOf(i) + "," + partialOutputFunc.get(i) + ")";
                }
                
            }
        }
        SSTencoding += "},";
        //add state-transition function
        SSTencoding += "{";
        isFirst = true;
        for (int index = 0; index < stateTransitionFunc.size(); index++) {
            Integer[] toStates = stateTransitionFunc.get(index);
            for (int i = 0; i < toStates.length; i++) {
                if (toStates[i] != null) {
                    if (isFirst) {
                        SSTencoding += "(q" + String.valueOf(index) + "," + inAlpha[i] + ",q" + SSTState.indexOf(toStates[i]) + ")";
                        isFirst = false;
                    } else {
                        SSTencoding += ",(q" + String.valueOf(index) + "," + inAlpha[i] + ",q" + SSTState.indexOf(toStates[i]) + ")";
                    }
                }
            }   
        }

        SSTencoding += "},";
        //add variable-update function
        SSTencoding += "{";
        isFirst = true;
        for (int index = 0; index < SSTState.size(); index++) {
            String[][] update = variableUpdateFunc.get(index);
            for (int i = 0; i < update.length; i++) {
                for (int j = 0; j < update[i].length; j++) {
                    if (update[i][j] != null) {
                        if (isFirst) {
                            SSTencoding += "(q" + String.valueOf(index) + "," + inAlpha[i] + "," + variableArray[j] + "," + update[i][j] + ")";
                            isFirst = false;
                        } else {
                            SSTencoding += ",(q" + String.valueOf(index) + "," + inAlpha[i] + "," + variableArray[j] + "," + update[i][j] + ")";
                        }
                    }
                }
            }
        }
        SSTencoding += "})";    

        return SSTencoding;
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
