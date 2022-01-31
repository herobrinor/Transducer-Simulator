package simulator;

import simulator.transducer.*;
import simulator.util.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        inputAlphabet.put("$",inAlpha.length+1);
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
        String pattern = "\\(\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{[A-Za-z0-9](,[A-Za-z0-9])*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\},\\{\\([A-Za-z0-9]*(,[A-Za-z0-9^$-]*){4}\\)(,\\([A-Za-z0-9]*(,[A-Za-z0-9^$-]*){4}\\))*\\},\\{[A-Za-z0-9]*\\},\\{[A-Za-z0-9]*(,[A-Za-z0-9]*)*\\}\\)";
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
                    if (variableUpdate[i][j][index] != null) {
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
    public String fromTDFTtoSST(String encoding) throws Exception{
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
        states.put("m",statesArray.length);
        states.put("qerr",statesArray.length+1);
        states.put("qloop",statesArray.length+2);
        //add endmarker
        inputAlphabet.put("^",inAlpha.length);
        inputAlphabet.put("$",inAlpha.length+1);
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
        // // add new transtion functions to make 2DFT start in left endmarker
        // int initialStateNum = states.get(initialState);
        // transition[initialStateNum][inAlpha.length][0] = "";
        // transition[initialStateNum][inAlpha.length][1] = initialState;
        // transition[initialStateNum][inAlpha.length][2] = 1;
        // // add new transtion functions to make 2DFT end in right endmarker
        // for (int i = 0; i < finalStatesArray.length; i++) {
        //     int state = states.get(finalStatesArray[i]);
        //     for (int j = 0; j < inAlpha.length; j++) {
        //         transition[state][j][0] = "";
        //         transition[state][j][1] = finalStatesArray[i];
        //         transition[state][j][2] = 1;
        //     }
        //     transition[state][inAlpha.length+1][0] = "";
        //     transition[state][inAlpha.length+1][1] = finalStatesArray[i]+"'";
        //     transition[state][inAlpha.length+1][2] = 1;
        // }

        //construct new states in SST
        //use decimal number to represent a N-base number as the states of SST
        //starting state is identity function form Q to Q U {m, qerr, qloop}.
        int base = states.size();
        //store visited states, exploring states, variable-update function, state-transition function and pratial output function
        ArrayList<Integer[]> SSTState = new ArrayList<Integer[]>();
        Queue<Integer[]> stateQueue = new LinkedList<Integer[]>();
        Queue<String[]> sharedVarStateQueue = new LinkedList<String[]>();
        ArrayList<String> partialOutputFunc = new ArrayList<String>();
        ArrayList<Object[][]> stateTransitionFunc =new ArrayList<Object[][]>();
        ArrayList<String[][]> variableUpdateFunc =new ArrayList<String[][]>();
        HashMap<String, Integer> variables = new HashMap<String, Integer>();
        String[] variableArray = new String[base-2];
        HashMap<String, Integer> sharedVariables = new HashMap<String, Integer>();
        String[] sharedVariableArray = new String[2*base-7];// to be base-4 + base-3 before reducing 
        ArrayList<String[]> sharedVariableStates = new ArrayList<String[]>();
        ArrayList<String[][]> sharedVariableUpdateFunc =new ArrayList<String[][]>();
        ArrayList<Integer[]> newStateArrayList = new ArrayList<Integer[]>();
        ArrayList<String[]> newSharedVarStateArrayList = new ArrayList<String[]>();
        //initalise variable set in SST
        char varNum1 = 65;//symbol A
        int count = 0;
        while (count < base-2) {
            String var1 = String.valueOf(varNum1);
            if (outputAlphabet.contains(var1)) {
                varNum1 += 1;
            } else {
                variableArray[count] = var1;
                variables.put(var1, count);
                varNum1 = (char) (varNum1 + 1);
                count += 1;
            }
        }
        //initalise shared variable set in SST
        count = 0;
        while (count < 2*base-7) {
            String var1 = String.valueOf(varNum1);
            if (outputAlphabet.contains(var1)) {
                varNum1 += 1;
            } else {
                sharedVariableArray[count] = var1;
                sharedVariables.put(var1, count);
                varNum1 = (char) (varNum1 + 1);
                count += 1;
            }
        }
        //simulate 2DFT on left endmarker to construct initial state of SST
        Integer[] nextState = new Integer[base-2];
        String[] varUpLEM = new String[variableArray.length];
        // state except m
        for (int i = 0; i < base-3; i++) {
            Object[] transfunc = transition[i][inAlpha.length];
            if (transfunc[2] != null && (int)transfunc[2] == 1) {//careful about 0
                nextState[i] = states.get((String)transfunc[1]);
                varUpLEM[i] = (String)transfunc[0];
            } else {//map to qerr
                nextState[i] = base-2;
                varUpLEM[i] = "";
            }
        }
        // state m
        nextState[base-3] = states.get(initialState);
        varUpLEM[base-3] = "";
        Integer[] firstState = nextState;
        //compute stateTransitionFunc and variable-update function combined with simulating on left endmarker
        String[][] varUp = new String[inAlpha.length][variableArray.length];
        String[][] sharedVarUp = new String[inAlpha.length][sharedVariableArray.length];
        Object[][] stateUp = new Object[inAlpha.length][2];
        String[] sharedVarState = new String[base-3];
        for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
            nextState = new Integer[base-2];
            String[] newSharedVarState = new String[base-3];
            // state except m
            for (int j = 0; j < base-3; j++) {//for every state
                String newVal1 = "";
                Object[] transfunc = transition[j][i];
                //careful about 0
                if (transfunc[2] != null && (int)transfunc[2] == 1) {
                    nextState[j] = states.get((String)transfunc[1]);
                    newVal1 = (String)transfunc[0];
                    varUp[i][j] = newVal1;                      
                } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                    //loop to acquire f(q) which is the forward state from q
                    while (true) {
                        newVal1 += (String)transfunc[0];
                        int leftState = states.get((String)transfunc[1]);
                        newVal1 += varUpLEM[leftState];
                        int backState = firstState[leftState];
                        transfunc = transition[backState][i];
                        if (transfunc[2] != null && (int)transfunc[2] == 1) {
                            newVal1 += transfunc[0];
                            varUp[i][j] = newVal1;
                            nextState[j] = states.get((String)transfunc[1]);
                            break;
                        } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                            continue;
                        } else {
                            varUp[i][j] = "";
                            nextState[j] = base-2;
                            break;
                        }   
                    }
                } else {//map to qerr
                    nextState[j] = base-2;
                    varUp[i][j] = "";
                }
            }
            // state m
            String newVal = varUpLEM[base-3];
            int mState = firstState[base-3];
            Object[] mtransfunc = transition[mState][i];
            //careful about 0
            if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                nextState[base-3] = states.get((String)mtransfunc[1]);
                newVal += (String)mtransfunc[0];
                varUp[i][base-3] = newVal;
            } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                while (true) {
                    newVal = (String)mtransfunc[0];
                    int leftState = states.get(mtransfunc[1]);
                    newVal += varUpLEM[leftState];
                    int backState = firstState[leftState];
                    mtransfunc = transition[backState][i];
                    if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                        newVal += mtransfunc[0];
                        varUp[i][base-3] = newVal;
                        nextState[base-3] = states.get((String)mtransfunc[1]);
                        break;
                    } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                        continue;
                    } else {
                        newVal = "";
                        varUp[i][base-3] = newVal;
                        nextState[base-3] = base-2;
                        break;
                    }
                }
            } else {//map to qerr
                nextState[base-3] = base-2;
                varUp[i][base-3] = "";
            }            

            stateUp[i][0] = nextState;
            stateUp[i][1] = newSharedVarState;
            if (!isInList(SSTState,nextState) && !isInQueue(stateQueue,nextState)) {
                stateQueue.offer(nextState);
                sharedVarStateQueue.offer(newSharedVarState);
                newStateArrayList.add(nextState);
                newSharedVarStateArrayList.add(newSharedVarState);
            } else {
                boolean exist = false;
                for (int index = 0; index < SSTState.size(); index++) {
                    if (Arrays.equals(SSTState.get(index),nextState) && Arrays.equals(sharedVariableStates.get(index),newSharedVarState)) {
                        exist = true;
                    }
                }
                for (int index = 0; index < newStateArrayList.size(); index++) {
                    if (Arrays.equals(newStateArrayList.get(index),nextState) && Arrays.equals(newSharedVarStateArrayList.get(index),newSharedVarState)) {
                        exist = true;
                    }
                }
                if (!exist) {
                    stateQueue.offer(nextState);
                    sharedVarStateQueue.offer(newSharedVarState);
                    newStateArrayList.add(nextState);
                    newSharedVarStateArrayList.add(newSharedVarState);
                }
            }
        }
        SSTState.add(firstState);
        stateTransitionFunc.add(stateUp);
        variableUpdateFunc.add(varUp);
        sharedVariableUpdateFunc.add(sharedVarUp);
        sharedVariableStates.add(sharedVarState);

        //compute partial output func
        String output = variableArray[base-3];
        String finalState = "m";
        int mState = firstState[base-3];
        Object[] mtransfunc = transition[mState][inAlpha.length+1];
        //careful about 0
        if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
            output += (String)mtransfunc[0];
        } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
            while (true) {
                output += (String)mtransfunc[0];
                int leftState = states.get((String)mtransfunc[1]);
                output += variableArray[leftState];
                if (sharedVarState[leftState] != null) {
                    output += sharedVarState[leftState];
                }
                int backState = firstState[leftState];
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
            Integer[] currState = stateQueue.poll();
            SSTState.add(currState);
            sharedVarState = sharedVarStateQueue.poll();
            sharedVariableStates.add(sharedVarState);
            varUp = new String[inAlpha.length][variableArray.length];
            stateUp = new Object[inAlpha.length][2];
            sharedVarUp = new String[inAlpha.length][sharedVariableArray.length];
            int[] prevSharedVarCount = new int[2*base-7];
            for (int i = 0; i < sharedVarState.length; i++) {
                if (sharedVarState[i] != null) {
                    char[] varsCharArray = sharedVarState[i].toCharArray();
                    for (int k = 0; k < varsCharArray.length; k++) {
                        prevSharedVarCount[sharedVariables.get(String.valueOf(varsCharArray[k]))]++;
                    }
                }
            }
            for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
                nextState = new Integer[base-2];
                int[] merging = new int[base-3];
                String[] newSharedVarState = new String[base-3];
                int[] sharedVarCount = new int[2*base-7];
                // state except m
                for (int j = 0; j < base-3; j++) {
                    String newVal1 = "";
                    Object[] transfunc = transition[j][i];
                    //careful about 0
                    if (transfunc[2] != null && (int)transfunc[2] == 1) {
                        nextState[j] = states.get((String)transfunc[1]);
                        newVal1 = (String)transfunc[0];
                        varUp[i][j] = newVal1;                      
                    } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                        Boolean isFirst = true;
                        while (true) {
                            newVal1 += (String)transfunc[0];
                            int leftState = states.get((String)transfunc[1]);
                            if (isFirst) {//check first left state for merging
                                merging[leftState] += 1;
                                isFirst = false;
                            }
                            newVal1 += variableArray[leftState];
                            int backState = currState[leftState];
                            if (backState == j) {//detect loop and map to qerr if found
                                nextState[j] = base-1;
                                varUp[i][j] = "@";
                                break;
                            }
                            if (backState == base-1) {//map to qloop
                                nextState[j] = base-1;
                                varUp[i][j] = "@";
                                break;
                            }
                            if (backState == base-2) {//map to qerr
                                nextState[j] = base-2;
                                varUp[i][j] = "";
                                break;
                            }
                            transfunc = transition[backState][i];
                            if (transfunc[2] != null && (int)transfunc[2] == 1) {
                                newVal1 += transfunc[0];
                                varUp[i][j] = newVal1;
                                nextState[j] = states.get((String)transfunc[1]);
                                break;
                            } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                                continue;
                            } else {
                                newVal1 = "";
                                varUp[i][j] = newVal1;
                                nextState[j] = base-2;
                            }                               
                            break;
                        }
                    } else {//map to qerr
                        nextState[j] = base-2;
                        varUp[i][j] = "";
                    }
                }
                // reduce the tree using number of variables that varUp contains
                // if number of variables > 1 shared variables need to be used
                int[] varCountArray = new int[base-3];
                int maxVarNum = 0;
                for (int j = 0; j < base-3; j++) {
                    char[] vars = varUp[i][j].toCharArray();
                    int varcount = 0;
                    for (int index = 0; index < vars.length; index++) {
                        if (variables.containsKey(String.valueOf(vars[index]))) {
                            varcount++;
                        } else {
                            continue;
                        }
                    }
                    varCountArray[j] = varcount;
                    if (varcount > maxVarNum) {
                        maxVarNum = varcount;
                    }
                }
                if (maxVarNum > 1) {
                    for (int m = 2; m < maxVarNum+1; m++) {
                        for (int j = 0; j < base-3; j++) {
                            if (varCountArray[j] == 1) {//check shared variable dependency when only moving back once
                                int leftState = states.get((String)transition[j][i][1]);
                                if (sharedVarState[leftState] != null) {
                                    newSharedVarState[j] = sharedVarState[leftState];
                                    int position = varUp[i][j].indexOf(variableArray[leftState]);
                                    if (position == 0) {
                                        varUp[i][j] = "";
                                    } else if (position > 0) {
                                        varUp[i][j] = varUp[i][j].substring(0,position);
                                    }
                                    String last = newSharedVarState[j].substring(newSharedVarState[j].length()-1,newSharedVarState[j].length());
                                    sharedVarUp[i][sharedVariables.get(last)] = last + (String)transition[currState[leftState]][i][0];
                                }
                            }
                            if (varCountArray[j] == m) {
                                char[] vars = varUp[i][j].toCharArray();
                                int varcount = 0;
                                for (int index = 0; index < vars.length; index++) {
                                    if (variables.containsKey(String.valueOf(vars[index]))) {
                                        varcount++;
                                    } else {
                                        continue;
                                    }
                                    if (varcount <= 1) {
                                        continue;
                                    } else if (varcount == 2) {//varcount will not go beyond 2 since the loop is in increasing order
                                        int leftState = variables.get(String.valueOf(vars[index]));
                                        int sharedVar = base;
                                        for (int index2 = 0; index2 < sharedVarCount.length; index2++) {
                                            if (sharedVarCount[index2] == 0 && prevSharedVarCount[index2] == 0) {
                                                sharedVar = index2;
                                                break;
                                            }
                                        }
                                        int nextVar = index+1;
                                        for (int k = index+1; k < vars.length; k++) {
                                            if (variables.containsKey(String.valueOf(vars[k]))) {
                                                break;
                                            } else {
                                                nextVar++;
                                            }
                                        }
                                        if (sharedVarState[leftState] != null) {
                                            if (newSharedVarState[j] != null) {
                                                newSharedVarState[j] = sharedVariableArray[sharedVar] + sharedVarState[leftState] + newSharedVarState[j];
                                            } else {
                                                newSharedVarState[j] = sharedVariableArray[sharedVar] + sharedVarState[leftState];
                                            }
                                            sharedVarUp[i][sharedVar] = varUp[i][j].substring(index,index+1);
                                            char[] varsCharArray = sharedVarState[leftState].toCharArray();
                                            Integer[] sVars = new Integer[varsCharArray.length];
                                            for (int k = 0; k < varsCharArray.length; k++) {
                                                sVars[k] = sharedVariables.get(String.valueOf(varsCharArray[k]));
                                            }
                                            if (index+1<nextVar) {
                                                sharedVarUp[i][sVars[varsCharArray.length-1]] = sharedVariableArray[sVars[varsCharArray.length-1]] + varUp[i][j].substring(index+1,nextVar);
                                            }

                                            for (int index2 = 0; index2 < sVars.length; index2++) {
                                                sharedVarCount[sVars[index2]]++;
                                            }
                                            sharedVarCount[sharedVar]++;
                                            for (int k = 0; k < base-3; k++) {
                                                if (k != j) {
                                                    if (varUp[i][k].contains(varUp[i][j].substring(index,nextVar))) {
                                                        if (newSharedVarState[k] != null) {
                                                            newSharedVarState[k] = sharedVariableArray[sharedVar] + sharedVarState[leftState] + newSharedVarState[k];
                                                        } else {
                                                            newSharedVarState[k] = sharedVariableArray[sharedVar] + sharedVarState[leftState];
                                                        }
                                                        varUp[i][k] = varUp[i][k].replace(varUp[i][j].substring(index,nextVar), "");
                                                        for (int index2 = 0; index2 < sVars.length; index2++) {
                                                            sharedVarCount[sVars[index2]]++;
                                                        }
                                                        sharedVarCount[sharedVar]++;
                                                    }
                                                }
                                            }
                                            varUp[i][j] = varUp[i][j].replace(varUp[i][j].substring(index,nextVar), "");      
                                        } else {
                                            if (newSharedVarState[j] != null) {
                                                newSharedVarState[j] = sharedVariableArray[sharedVar] + newSharedVarState[j] + newSharedVarState[j];
                                            } else {
                                                newSharedVarState[j] = sharedVariableArray[sharedVar];
                                            }
                                            sharedVarUp[i][sharedVar] = varUp[i][j].substring(index,nextVar);
                                            sharedVarCount[sharedVar]++;
                                            for (int k = 0; k < base-3; k++) {
                                                if (k != j) {
                                                    if (varUp[i][k].contains(varUp[i][j].substring(index,nextVar))) {
                                                        if (newSharedVarState[k] != null) {
                                                            newSharedVarState[k] = sharedVariableArray[sharedVar] + newSharedVarState[j] + newSharedVarState[k];
                                                        } else {
                                                            newSharedVarState[k] = sharedVariableArray[sharedVar];
                                                        }
                                                        varUp[i][k] = varUp[i][k].replace(varUp[i][j].substring(index,nextVar), "");
                                                        sharedVarCount[sharedVar]++;
                                                    }
                                                }
                                            }
                                            varUp[i][j] = varUp[i][j].replace(varUp[i][j].substring(index,nextVar), "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int j = 0; j < base-3; j++) {
                        if (varCountArray[j] == 1) {//check shared variable dependency when only moving back once
                            int leftState = states.get((String)transition[j][i][1]);
                            if (sharedVarState[leftState] != null) {
                                newSharedVarState[j] = sharedVarState[leftState];
                                int position = varUp[i][j].indexOf(variableArray[leftState]);
                                if (position == 0) {
                                    varUp[i][j] = "";
                                } else if (position > 0) {
                                    varUp[i][j] = varUp[i][j].substring(0,position);
                                }
                                String last = newSharedVarState[j].substring(newSharedVarState[j].length()-1,newSharedVarState[j].length());
                                sharedVarUp[i][sharedVariables.get(last)] = last + (String)transition[currState[leftState]][i][0];
                            }
                        }
                    }
                }
                // compute newSharedVarState
                // for (int j = 0; j < base-3; j++) {
                //     boolean sharedFlag = false;
                //     int sharedPosition = 0;
                //     char[] updateArray = varUp[i][j].toCharArray();
                //     for (int k = 0; k < updateArray.length; k++) {
                //         if (sharedVariables.containsKey(String.valueOf(updateArray[k]))) {
                //             sharedFlag = true;
                //             sharedPosition = k;
                //             break;
                //         }
                //     }
                //     if (sharedFlag) {
                //         newSharedVarState[j] = varUp[i][j].substring(sharedPosition);
                //         varUp[i][j] = varUp[i][j].substring(0, sharedPosition);
                //     }
                // }
                // check for state merging and update shared variables
                for (int j = 0; j < merging.length; j++) {
                    if (merging[j] > 1) {
                        int sharedVar = base;
                        for (int index = 0; index < sharedVarCount.length; index++) {
                            if (sharedVarCount[index] == 0 && prevSharedVarCount[index] == 0) {
                                sharedVar = index;
                                break;
                            }
                        }
                        Integer[] sVars = new Integer[0];
                        if (sharedVarState[j] != null) {//if there are previous shared variables existing
                            char[] varsCharArray = sharedVarState[j].toCharArray();
                            sVars = new Integer[varsCharArray.length];
                            for (int index = 0; index < varsCharArray.length; index++) {
                                sVars[index] = sharedVariables.get(String.valueOf(varsCharArray[index]));
                            }
                        }
                        Boolean isFirst = true;
                        for (int state = 0; state < transition.length; state++) {
                            if (transition[state][i][2] != null && (int)transition[state][i][2] == -1) {
                                int leftState = states.get((String)transition[state][i][1]);
                                if (leftState == j) {
                                    for (int index = 0; index < sVars.length; index++) {
                                        sharedVarCount[sVars[index]]++;
                                    }
                                    sharedVarCount[sharedVar]++;
                                    int position = varUp[i][state].indexOf(variableArray[states.get((String)transition[j][i][1])]);
                                    if (position >= 1) {
                                        varUp[i][state] = varUp[i][state].substring(0,position);
                                    }
                                    if (newSharedVarState[state] != null) {
                                        newSharedVarState[state] = sharedVariableArray[sharedVar] + newSharedVarState[state];
                                    } else {
                                        newSharedVarState[state] = sharedVariableArray[sharedVar] ;
                                    }
                                    if (isFirst) {
                                        if (!newSharedVarState[state].equals(sharedVariableArray[sharedVar])) {
                                            sharedVarUp[i][sharedVar] = variableArray[states.get((String)transition[j][i][1])];
                                        } else {
                                            int backState = currState[j];
                                            Object[] trans = transition[backState][i];
                                            sharedVarUp[i][sharedVar] = variableArray[states.get((String)transition[j][i][1])] + (String)trans[0];
                                        }
                                        isFirst = false;
                                    }
                                }
                            }
                        }
                    }
                }
                // reducing the tree using shared varibales appears only once, update older variable
                for (int j = 0; j < sharedVarCount.length; j++) {
                    if (sharedVarCount[j] == 1) {
                        for (int k = 0; k < base-3; k++) {
                            if (newSharedVarState[k] != null) {
                                if (newSharedVarState[k].contains(sharedVariableArray[j])) {
                                    newSharedVarState[k] = newSharedVarState[k].replace(sharedVariableArray[j], "");
                                    if (newSharedVarState[k].indexOf(sharedVariableArray[j]) == 0) {
                                        varUp[i][k] = varUp[i][k] + sharedVariableArray[j];
                                    } else {
                                        int oldVar = sharedVariables.get(String.valueOf(newSharedVarState[k].charAt(newSharedVarState[k].indexOf(sharedVariableArray[j])-1)));
                                        if (sharedVarUp[i][oldVar] == null) {
                                            sharedVarUp[i][oldVar] = "";
                                        }
                                        sharedVarUp[i][oldVar] += sharedVariableArray[j];
                                    }
                                }
                            }
                        }
                    }
                }
                // reducing the tree using path appears only once
                for (int j = 0; j < sharedVariableArray.length; j++) {
                    if (sharedVarCount[j] > 0) {
                        checkprev:while (true) {
                            String prevShare = null;
                            for (int k = 0; k < newSharedVarState.length; k++) {
                                if (newSharedVarState[k] != null) {
                                    int position = newSharedVarState[k].indexOf(sharedVariableArray[j]);
                                    if (position == -1) {
                                        break;
                                    } else if (position == 0) {
                                        if (prevShare == null) {
                                            prevShare = "";
                                        } else {
                                            if (!prevShare.equals("")) {
                                                break checkprev;
                                            }
                                        }
                                    } else {
                                        if (prevShare == null) {
                                            prevShare = newSharedVarState[k].substring(position-1, position);
                                        } else {
                                            if (!prevShare.equals(newSharedVarState[k].substring(position-1, position))) {
                                                break checkprev;
                                            }
                                        }
                                        
                                    }
                                }
                            }
                            if (prevShare == null || prevShare.equals("")) {
                                break checkprev;
                            } else {
                                int prevVarNum = sharedVariables.get(prevShare);
                                if (sharedVarUp[i][prevVarNum] != null) {
                                    sharedVarUp[i][j] = sharedVarUp[i][prevVarNum] + sharedVarUp[i][j];
                                    sharedVarUp[i][prevVarNum] = "";
                                };

                                for (int k = 0; k < newSharedVarState.length; k++) {
                                    if (newSharedVarState[k] != null && newSharedVarState[k].contains(prevShare+sharedVariableArray[j])) {
                                        newSharedVarState[k] = newSharedVarState[k].replace(prevShare+sharedVariableArray[j], sharedVariableArray[j]);
                                        sharedVarCount[prevVarNum]--;
                                    }
                                }
                            }
                        }
                    }
                }
                // state m
                String newVal = variableArray[base-3];
                mState = currState[base-3];
                if (mState == base-1) {//map to qloop
                    nextState[base-3] = base-1;
                    varUp[i][base-3] = "@";
                } else if (mState == base-2) {//map to qerr
                    nextState[base-3] = base-2;
                    varUp[i][base-3] = "";
                } else {
                    Object[] transfunc = transition[mState][i];
                    if (transfunc[2] != null && (int)transfunc[2] == 1) {
                        nextState[base-3] = states.get((String)transfunc[1]);
                        if ((String)transfunc[0] != null) {
                            newVal += (String)transfunc[0];
                        } else {
                            newVal += "";
                        }
                        varUp[i][base-3] = newVal;
                    } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                        newVal += varUp[i][mState];
                        if (newSharedVarState[mState] != null) {
                            char[] varsCharArray = newSharedVarState[mState].toCharArray();
                            for (int index = 0; index < varsCharArray.length; index++) {
                                int sharedVarNum = sharedVariables.get(String.valueOf(varsCharArray[index]));
                                for (int m = 0; m < newSharedVarState.length; m++) {
                                    if (newSharedVarState[m] != null && newSharedVarState[m].contains(String.valueOf(varsCharArray[index]))) {
                                        newSharedVarState[m] = newSharedVarState[m].replace(String.valueOf(varsCharArray[index]), "");
                                    }
                                }
                                newVal += sharedVarUp[i][sharedVarNum];
                                sharedVarCount[sharedVarNum] = 0;
                                sharedVarUp[i][sharedVarNum] = null;
                            }
                        }
                        nextState[base-3] = nextState[mState];
                        varUp[i][base-3] = newVal;
                        varUp[i][mState] = "";//used once in the whole run
                    } else {//map to qerr
                        nextState[base-3] = base-2;
                        varUp[i][base-3] = "";
                    }
                }

                for (int index = 0; index < newSharedVarState.length; index++) {
                    if (newSharedVarState[index] != null && newSharedVarState[index].equals("")) {
                        newSharedVarState[index] = null;
                    }
                }
                // System.out.println(Arrays.toString(newSharedVarState));
                // System.out.println(Arrays.toString(sharedVarCount));
                //check if this state is duplicated or was explored before
                stateUp[i][0] = nextState;
                stateUp[i][1] = newSharedVarState;
                if (!isInList(SSTState,nextState) && !isInQueue(stateQueue,nextState)) {
                    stateQueue.offer(nextState);
                    sharedVarStateQueue.offer(newSharedVarState);
                    newStateArrayList.add(nextState);
                    newSharedVarStateArrayList.add(newSharedVarState);
                } else {
                    boolean exist = false;
                    for (int index = 0; index < SSTState.size(); index++) {
                        if (Arrays.equals(SSTState.get(index),nextState) && Arrays.equals(sharedVariableStates.get(index),newSharedVarState)) {
                            exist = true;
                        }
                    }
                    for (int index = 0; index < newStateArrayList.size(); index++) {
                        if (Arrays.equals(newStateArrayList.get(index),nextState) && Arrays.equals(newSharedVarStateArrayList.get(index),newSharedVarState)) {
                            exist = true;
                        }
                    }
                    if (!exist) {
                        stateQueue.offer(nextState);
                        sharedVarStateQueue.offer(newSharedVarState);
                        newStateArrayList.add(nextState);
                        newSharedVarStateArrayList.add(newSharedVarState);
                    }
                }
            }
            stateTransitionFunc.add(stateUp);
            variableUpdateFunc.add(varUp);
            sharedVariableUpdateFunc.add(sharedVarUp);
            
            //compute partial output func
            output = variableArray[base-3];
            // System.out.println(output);
            finalState = "m";
            mState = currState[base-3];
            if (mState == base-2) {//qerr
                finalState = "qerr";
                output = "";
            } else if (mState == base-1) {//qloop
                finalState = "qloop";
                output = "@";
            } else {
                Object[] transfunc = transition[mState][inAlpha.length+1];
                //careful about 0
                if (transfunc[2] != null && (int)transfunc[2] == 1) {
                    if (transfunc[0] != null) {
                        output += (String)transfunc[0];
                    }
                    finalState = (String)transfunc[1];
                } else if (transfunc[2] != null && (int)transfunc[2] == -1) {
                    while (true) {
                        output += (String)transfunc[0];
                        int leftState = states.get((String)transfunc[1]);
                        output += variableArray[leftState];
                        if (sharedVarState[leftState] != null) {
                            output += sharedVarState[leftState];
                        }
                        int backState = currState[leftState];
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
            }

            if (finalStates.contains(finalState)) {
                partialOutputFunc.add(output);
            } else if (finalState.equals("qerr")) {
                partialOutputFunc.add(output);
            } else if (finalState.equals("qloop")) {
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
        for (int i = 0; i < sharedVariableArray.length; i++) {
            if (isFirst) {
                SSTencoding += sharedVariableArray[i];
                isFirst = false;
            } else {
                SSTencoding += "," + sharedVariableArray[i];
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
            Object[][] toStates = stateTransitionFunc.get(index);

            for (int i = 0; i < toStates.length; i++) {
                if (toStates[i][0] != null) {
                    int stateNum = 0;
                    for (int j = 0; j < SSTState.size(); j++) {
                        if (Arrays.equals(SSTState.get(j),(Integer[])toStates[i][0]) && Arrays.equals(sharedVariableStates.get(j), (String[])toStates[i][1])) {
                            stateNum = j;
                        }
                    }
                    if (isFirst) {
                        SSTencoding += "(q" + String.valueOf(index) + "," + inAlpha[i] + ",q" + stateNum + ")";
                        isFirst = false;
                    } else {
                        SSTencoding += ",(q" + String.valueOf(index) + "," + inAlpha[i] + ",q" + stateNum + ")";
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
        for (int index = 0; index < SSTState.size(); index++) {
            String[][] update = sharedVariableUpdateFunc.get(index);
            for (int i = 0; i < update.length; i++) {
                for (int j = 0; j < update[i].length; j++) {
                    if (update[i][j] != null) {
                        if (isFirst) {
                            SSTencoding += "(q" + String.valueOf(index) + "," + inAlpha[i] + "," + sharedVariableArray[j] + "," + update[i][j] + ")";
                            isFirst = false;
                        } else {
                            SSTencoding += ",(q" + String.valueOf(index) + "," + inAlpha[i] + "," + sharedVariableArray[j] + "," + update[i][j] + ")";
                        }
                    }
                }
            }
        }
        SSTencoding += "})";

        // for (int index = 0; index < newStateArrayList.size(); index++) {
        //     System.out.println(Arrays.toString(newStateArrayList.get(index)));
        // }
        // for (int index = 0; index < newSharedVarStateArrayList.size(); index++) {
        //     System.out.println(Arrays.toString(newSharedVarStateArrayList.get(index)));
        // }
        // for (int index = 0; index < sharedVariableStates.size(); index++) {
        //     System.out.println(Arrays.toString(SSTState.get(index)));
        // }
        // for (int index = 0; index < sharedVariableStates.size(); index++) {
        //     System.out.println(Arrays.toString(sharedVariableStates.get(index)));
        // }
        // for (int index = 0; index < partialOutputFunc.size(); index++) {
        //     System.out.println(partialOutputFunc.get(index));
        // }
        return SSTencoding;
    }

    /**
     * Translate the encoding from SST to 2DFT
     * @param encoding Encoding of SST
     * @return Encoding of 2DFT
     */
    public String fromSSTtoTDFT(String encoding) throws Exception{
        //store SST
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
                    if (variableUpdate[i][j][index] != null) {
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
        }

        //initalise state representation of SST
        String[][] stateRepresentationArray = new String[inAlpha.length+1][statesArray.length];
        HashMap<String, Integer> stateRepresentation = new HashMap<String, Integer>();
        char varNum1 = 65;//symbol A
        int count = 0;
        for (int i = 0; i < inAlpha.length+1; i++) {
            for (int j = 0; j < statesArray.length; j++) {
                String var1 = String.valueOf(varNum1);
                stateRepresentationArray[i][j] = var1;
                stateRepresentation.put(var1, count);
                varNum1 = (char) (varNum1 + 1);
                count += 1;
            }
        }

        //construct the first transducer A
        String initialStateA = initialState;
        String finalStateA = "f";
        HashMap<String, Integer> statesA = states;
        statesA.put("f", states.size());
        HashMap<String, Integer> inputAlphabetA = inputAlphabet;
        inputAlphabetA.put("^", inAlpha.length);
        inputAlphabetA.put("$", inAlpha.length+1);
        HashSet<String> outputAlphabetA = new HashSet<String>();
        HashSet<String> finalStatesA = new HashSet<String>();
        finalStatesA.add(finalStateA);
        for (int i = 0; i < inAlpha.length+1; i++) {
            for (int j = 0; j < statesArray.length; j++) {
                outputAlphabetA.add(stateRepresentationArray[i][j]);
            }
        }
        Object[][][] transitionA = new Object[statesA.size()][inputAlphabetA.size()][3];
        transitionA[statesA.get(initialStateA)][inAlpha.length] = new Object[]{"",initialStateA,1};
        for (int i = 0; i < stateTransition.length; i++) {
            for (int j = 0; j < stateTransition[i].length; j++) {
                if (stateTransition[i][j] != null && !stateTransition[i][j].equals("")) {
                    transitionA[i][j] = new Object[]{stateRepresentationArray[j][i],stateTransition[i][j],1};
                }
            }
            transitionA[i][stateTransition[i].length+1] = new Object[]{stateRepresentationArray[stateTransition[i].length][i],finalStateA,1};
        }

        //construct the second transducer B
        String initialStateB = "p0";
        String finalStateB = "pf";
        HashMap<String, Integer> statesB = new HashMap<String, Integer>();
        for (int i = 0; i < varArray.length; i++) {
            statesB.put(varArray[i]+"i",2*i);
            statesB.put(varArray[i]+"o",2*i+1);
        }
        statesB.put(initialStateB,2*varArray.length);
        statesB.put(finalStateB,2*varArray.length+1);
        HashMap<String, Integer> inputAlphabetB = new HashMap<String, Integer>();
        for (int i = 0; i < inAlpha.length+1; i++) {
            for (int j = 0; j < statesArray.length; j++) {
                inputAlphabetB.put(stateRepresentationArray[i][j],(i*statesArray.length)+j);
            }
        }
        inputAlphabetB.put("^", (inAlpha.length+1)*statesArray.length);
        inputAlphabetB.put("$", (inAlpha.length+1)*statesArray.length+1);
        HashSet<String> outputAlphabetB = outputAlphabet;
        HashSet<String> finalStatesB = new HashSet<String>();
        finalStatesB.add(finalStateB);

        Object[][][] transitionB = new Object[statesB.size()][inputAlphabetB.size()][3];
        // triplet t = (a, q) (stateRepresentation)
        // If p = p0 and a != $, then we set δ(p0, t) = (ε, p0, +1).
        Object[] initialStateTrans = new Object[]{"",initialStateB,1};
        for (int i = 0; i < inAlpha.length; i++) {
            for (int j = 0; j < statesArray.length; j++) {
                transitionB[2*varArray.length][(i*statesArray.length)+j] = initialStateTrans;
            }
        }

        // If p = p0 and a = $, then if F(q) starts by uX with u in B* and X in χ ,
        // then δ(p, t) = (u, Xi, -1).
        for (int j = 0; j < statesArray.length; j++) {
            char[] pOut = new char[0];
            if (partialOutput[j] != null && !partialOutput[j].equals("")) {
                pOut = partialOutput[j].toCharArray();
            }
            String outString = "";
            String var = "";
            for (int index = 0; index < pOut.length; index++) {
                String symbol = String.valueOf(pOut[index]);
                if (outputAlphabet.contains(symbol)) {
                    outString += symbol;
                } else if (variables.containsKey(symbol)) {
                    var = symbol;
                    break;
                }
            }
            if (var == null || var.equals("")) {
                transitionB[2*varArray.length][(inAlpha.length*statesArray.length)+j] = new Object[]{outString,finalStateB,1};
            } else {
                transitionB[2*varArray.length][(inAlpha.length*statesArray.length)+j] = new Object[]{outString,var+"i",-1};
            }
        }

        // If p = Xi, and t != $ then:
        // - either (q, a)(X) = u in B* and does not contain any variable
        // , and we set δ(p, t) = (u, Xo, +1),
        // - or (q, a)(X) starts by uY with u in B* and Y in χ 
        // , then δ(p, t) = (u, Yi, -1).
        for (int index = 0; index < varArray.length; index++) {
            for (int i = 0; i < inAlpha.length; i++) {
                for (int j = 0; j < statesArray.length; j++) {
                    char[] newVal = variableUpdate[j][i][index].toCharArray();
                    String outString = "";
                    String var = "";
                    for (int k = 0; k < newVal.length; k++) {
                        String symbol = String.valueOf(newVal[k]);
                        if (outputAlphabet.contains(symbol)) {
                            outString += symbol;
                        } else if (variables.containsKey(symbol)) {
                            var = symbol;
                            break;
                        }
                    }
                    if (var == null || var.equals("")) {
                        transitionB[2*index][(i*statesArray.length)+j] = new Object[]{outString,varArray[index]+"o",1};
                    } else {
                        transitionB[2*index][(i*statesArray.length)+j] = new Object[]{outString,var+"i",-1};
                    }
                }
            }
        }

        // If p = Xi, and t = ^ then δ(p, t) = (ε, Xo, +1).
        for (int index = 0; index < varArray.length; index++) {
            for (int j = 0; j < statesArray.length; j++) {
                transitionB[2*index][inputAlphabetB.size()-2] = new Object[]{"",varArray[index]+"o",1};
            }
        }

        // If p = Xo and a != $, then let Y be the unique variable of S such that X
        // appears in ρ(q, a)(Y). Then we have:
        // - either ρ(q, a)(Y) ends by Xu with u in B* and we set δ(p, t) = (u, Yo, +1),
        // - or ρ(q，a)(Y) is of the form (B U χ)*XuX'(B U χ)* and we set δ(p, t) = (u, X'i, -1).
        for (int index = 0; index < varArray.length; index++) {
            for (int i = 0; i < inAlpha.length; i++) {
                for (int j = 0; j < statesArray.length; j++) {
                    for (int k = 0; k < varArray.length; k++) {
                        int position = variableUpdate[j][i][k].indexOf(varArray[index]);
                        if (position != -1) {
                            char[] remainingVal = new char[0];
                            if (position != variableUpdate[j][i][k].length()) {
                                remainingVal = variableUpdate[j][i][k].substring(position+1).toCharArray();
                            }
                            String outString = "";
                            String var = "";
                            for (int l = 0; l < remainingVal.length; l++) {
                                String symbol = String.valueOf(remainingVal[l]);
                                if (outputAlphabet.contains(symbol)) {
                                    outString += symbol;
                                } else if (variables.containsKey(symbol)) {
                                    var = symbol;
                                    break;
                                }
                            }
                            if (var == null || var.equals("")) {
                                transitionB[2*index+1][(i*statesArray.length)+j] = new Object[]{outString,varArray[k]+"o",1};
                            } else {
                                transitionB[2*index+1][(i*statesArray.length)+j] = new Object[]{outString,var+"i",-1};
                            }
                            break;
                        }
                    }
                }
            }
        }

        // If p = Xo, q in Qf and a = $ then:
        // - either F(q) ends by Xu with u in B* and we set δ(p, t) = (u, pf, +1),
        // - or F(q) is of the form (B U χ)*XuX'(B U χ)* and we set B(p; t) = (u, X'i, -1).
        for (int index = 0; index < varArray.length; index++) {
            for (int j = 0; j < statesArray.length; j++) {
                if (partialOutput[j] == null) {
                    continue;
                }
                int position = partialOutput[j].indexOf(varArray[index]);
                if (position != -1) {
                    char[] pOut = new char[0];
                    if (position != partialOutput[j].length()) {
                        pOut = partialOutput[j].substring(position+1).toCharArray();
                    }
                    String outString = "";
                    String var = "";
                    for (int k = 0; k < pOut.length; k++) {
                        String symbol = String.valueOf(pOut[k]);
                        if (outputAlphabet.contains(symbol)) {
                            outString += symbol;
                        } else if (variables.containsKey(symbol)) {
                            var = symbol;
                            break;
                        }
                    }
                    if (var == null || var.equals("")) {
                        transitionB[2*index+1][(inAlpha.length*statesArray.length)+j] = new Object[]{outString,finalStateB,1};
                    } else {
                        transitionB[2*index+1][(inAlpha.length*statesArray.length)+j] = new Object[]{outString,var+"i",-1};
                    }
                    continue;
                }
            }
        }

        // SST sst = new SST(initialState, states, inputAlphabet, outputAlphabet, variables, partialOutput, stateTransition, variableUpdate);
        // TDFT A = new TDFT(initialStateA, statesA, finalStatesA, inputAlphabetA, outputAlphabetA, transitionA);
        // TDFT B = new TDFT(initialStateB, statesB, finalStatesB, inputAlphabetB, outputAlphabetB, transitionB);
        
        //construct the final 2DFT T = A o B
        // state of T is a function f of Q (set of state of A) x a control state C (forwarding or searching or found or returning or error) x state of A x state of B 
        Integer[] initialStateT = new Integer[statesA.size()+3];
        initialStateT[statesA.size()] = 0;
        initialStateT[statesA.size()+1] = statesA.get(initialStateA);
        initialStateT[statesA.size()+2] = statesB.get(initialStateB);
        Integer[] errorStateT = new Integer[statesA.size()+3];
        errorStateT[statesA.size()] = 4;
        HashMap<String, Integer> statesT = new HashMap<String, Integer>();
        statesT.put(Arrays.toString(initialStateT), 0);
        statesT.put(Arrays.toString(errorStateT), 1);
        int stateCount = 2;
        int finalB = statesB.get(finalStateB);
        for (int i = 0; i < statesA.size(); i++) {
            Integer[] finalStateT = new Integer[statesA.size()+3];
            finalStateT[statesA.size()] = 0;
            finalStateT[statesA.size()+1] = i;
            finalStateT[statesA.size()+2] = finalB;
            statesT.put(Arrays.toString(finalStateT), stateCount);
            stateCount++;
        }
        HashMap<String, Integer> inputAlphabetT = inputAlphabet;
        inputAlphabetT.put("^", inAlpha.length);
        inputAlphabetT.put("$", inAlpha.length+1);
        HashSet<String> outputAlphabetT = outputAlphabet;
        HashMap<String, Object[][]> transitionMap = new HashMap<String, Object[][]>();

        // for every state A and B pair
        for (String stateB : statesB.keySet()) {
            for (String stateA : statesA.keySet()) {
                int stateANum = statesA.get(stateA);
                int stateBNum = statesB.get(stateB);
                // create current state 
                Integer[] currState = new Integer[statesA.size()+3];
                currState[statesA.size()] = 0;
                currState[statesA.size()+1] = stateANum;
                currState[statesA.size()+2] = stateBNum;
                // store in state arraylist
                if (statesT.get(Arrays.toString(currState)) == null) {
                    statesT.put(Arrays.toString(currState), stateCount);
                    stateCount++;
                }
                // create new transition function
                Object[][] trans = new Object[inputAlphabetT.size()][3];
                // for every input symbol
                for (int k = 0; k < inputAlphabetT.size(); k++) {
                    // simulate A on this symbol
                    if (transitionA[stateANum][k][2] != null) {
                        // if moving right
                        int inputSymbolNum = 0;
                        if (inputAlphabetB.get((String)transitionA[stateANum][k][0]) == null) {
                            inputSymbolNum = (inAlpha.length+1)*statesArray.length;
                        } else {
                            inputSymbolNum = inputAlphabetB.get((String)transitionA[stateANum][k][0]);
                        }
                        Integer direction = (Integer)transitionB[stateBNum][inputSymbolNum][2];
                        if (direction != null && direction == 1) {
                            // create next state
                            Integer[] nextState = new Integer[statesA.size()+3];
                            nextState[statesA.size()] = 0;
                            nextState[statesA.size()+1] = statesA.get((String)transitionA[stateANum][k][1]);
                            nextState[statesA.size()+2] = statesB.get((String)transitionB[stateBNum][inputSymbolNum][1]);
                            String nextOutput = (String)transitionB[stateBNum][inputSymbolNum][0];
                            // if not exist add to state
                            if (statesT.get(Arrays.toString(nextState)) != null) {
                                trans[k] = new Object[]{nextOutput,statesT.get(Arrays.toString(nextState)),1};
                            } else {
                                trans[k] = new Object[]{nextOutput,stateCount,1};
                                statesT.put(Arrays.toString(nextState), stateCount);
                                stateCount++;
                            }
                        } else if (direction != null && direction == -1) {
                            // if moving left
                            // create next state
                            String nextOutput = (String)transitionB[stateBNum][inputSymbolNum][0];
                            int nextStateBNum = statesB.get((String)transitionB[stateBNum][inputSymbolNum][1]);
                            Integer[] nextState = new Integer[statesA.size()+3];
                            nextState[stateANum] = 0;
                            nextState[statesA.size()] = 1;
                            nextState[statesA.size()+1] = stateANum;
                            nextState[statesA.size()+2] = nextStateBNum;
                            // if not exist add to state
                            if (statesT.get(Arrays.toString(nextState)) != null) {
                                trans[k] = new Object[]{nextOutput,statesT.get(Arrays.toString(nextState)),-1};
                                continue;
                            } else {
                                trans[k] = new Object[]{nextOutput,stateCount,-1};
                                statesT.put(Arrays.toString(nextState), stateCount);
                                stateCount++;
                            }
                            // find previous state of A
                            // add returning state queue
                            Queue<Integer[]> returningQueue =  new LinkedList<Integer[]>();
                            Queue<HashSet<Integer>> returningGroupQueue =  new LinkedList<HashSet<Integer>>();
                            // add next searching state to a queue
                            Queue<Integer[]> searchingQueue =  new LinkedList<Integer[]>();
                            searchingQueue.add(nextState);
                            // add group queue
                            Queue<HashSet<Integer>> groupQueue =  new LinkedList<HashSet<Integer>>();
                            HashSet<Integer> statesInGroup = new HashSet<Integer>();
                            statesInGroup.add(stateANum);
                            groupQueue.add(statesInGroup);
                            // loop to search all possible previous state of A
                            while (!searchingQueue.isEmpty()) {
                                Integer[] searchingState = searchingQueue.poll();                      
                                HashSet<Integer> group = groupQueue.poll();
                                // create new transition function
                                Object[][] searchingTrans = new Object[inputAlphabetT.size()][3];
                                // for all possible previous input symbol except $ (the right endmarker)
                                for (int i = 0; i < inputAlphabetT.size()-1; i++) {
                                    HashSet<Integer> nextStatesInGroup = new HashSet<Integer>();
                                    Integer[] possibleState = new Integer[statesA.size()+3];
                                    possibleState[statesA.size()] = 1;
                                    possibleState[statesA.size()+1] = searchingState[statesA.size()+1];
                                    possibleState[statesA.size()+2] = searchingState[statesA.size()+2];
                                    int possibleStateCount = 0;
                                    HashSet<Integer> possibleGroupCount = new HashSet<Integer>();
                                    int groupNum = 1;
                                    for (int j = 0; j < statesA.size(); j++) {
                                        if (searchingState[stateANum] != null && searchingState[stateANum] == 0) {
                                            // find possible previous states
                                            if ((String)transitionA[j][i][1] != null && group.contains(statesA.get((String)transitionA[j][i][1]))) {
                                                // map to different groups at the first time
                                                possibleState[j] = groupNum;
                                                possibleStateCount++;
                                                possibleGroupCount.add(groupNum);
                                                nextStatesInGroup.add(j);
                                                groupNum++;
                                            }
                                        } else {
                                            // find possible previous states
                                            if ((String)transitionA[j][i][1] != null && group.contains(statesA.get((String)transitionA[j][i][1]))) {
                                                // map to relevant groups
                                                possibleState[j] = searchingState[statesA.get((String)transitionA[j][i][1])];
                                                possibleStateCount++;
                                                possibleGroupCount.add(possibleState[j]);
                                                nextStatesInGroup.add(j);
                                            }
                                        }
                                    }

                                    if (possibleStateCount == 0 && possibleGroupCount.size() == 0) {//map to qerr
                                        searchingTrans[i] = new Object[]{"",1,1};
                                    } else if (possibleGroupCount.size() == 1) {//found and return
                                        // find one source state
                                        int foundInitial = 0;
                                        for (Integer state : nextStatesInGroup) {
                                            foundInitial = state;
                                            break;
                                        }
                                        if (searchingState[stateANum] != null && searchingState[stateANum] == 0) {
                                            // if found in one step
                                            Integer[] foundState = new Integer[statesA.size()+3];
                                            foundState[statesA.size()] = 3;
                                            foundState[statesA.size()+1] = foundInitial;
                                            foundState[statesA.size()+2] = searchingState[statesA.size()+2];
                                            nextStatesInGroup = new HashSet<Integer>();
                                            nextStatesInGroup.add(foundInitial);
                                            // if not exist add to state and returning queue
                                            if (statesT.get(Arrays.toString(foundState)) != null) {
                                                searchingTrans[i] = new Object[]{"",statesT.get(Arrays.toString(foundState)),1};
                                                if (!returningQueue.contains(foundState)) {
                                                    returningQueue.add(foundState);
                                                    returningGroupQueue.add(nextStatesInGroup);
                                                }
                                            } else {
                                                searchingTrans[i] = new Object[]{"",stateCount,1};
                                                statesT.put(Arrays.toString(foundState), stateCount);
                                                stateCount++;
                                                returningQueue.add(foundState);
                                                returningGroupQueue.add(nextStatesInGroup);
                                            }
                                        } else {
                                            // else (found in several steps)
                                            Integer[] foundState = new Integer[statesA.size()+3];
                                            int backState = statesA.get((String)transitionA[foundInitial][i][1]);
                                            foundState[backState] = 0;
                                            foundState[statesA.size()] = 2;
                                            foundState[statesA.size()+1] = searchingState[statesA.size()+1];
                                            foundState[statesA.size()+2] = searchingState[statesA.size()+2];
                                            nextStatesInGroup = new HashSet<Integer>();
                                            nextStatesInGroup.add(backState);
                                            // find state in another group
                                            for (int j = 0; j < searchingState.length-3; j++) {
                                                if (searchingState[j] != null && searchingState[j] != searchingState[backState]) {
                                                    foundState[j] = 1;
                                                    nextStatesInGroup.add(j);
                                                    break;
                                                }
                                            }
                                            // if not exist add to state and returning queue
                                            if (statesT.get(Arrays.toString(foundState)) != null) {
                                                searchingTrans[i] = new Object[]{"",statesT.get(Arrays.toString(foundState)),1};
                                                if (!returningQueue.contains(foundState)) {
                                                    returningQueue.add(foundState);
                                                    returningGroupQueue.add(nextStatesInGroup);
                                                }
                                            } else {
                                                searchingTrans[i] = new Object[]{"",stateCount,1};
                                                statesT.put(Arrays.toString(foundState), stateCount);
                                                stateCount++;
                                                returningQueue.add(foundState);
                                                returningGroupQueue.add(nextStatesInGroup);
                                            }
                                        }
                                    } else {
                                        // continue searching process
                                        if (statesT.get(Arrays.toString(possibleState)) != null) {
                                            searchingTrans[i] = new Object[]{"",statesT.get(Arrays.toString(possibleState)),-1};
                                            if (!searchingQueue.contains(possibleState)) {
                                                searchingQueue.add(possibleState);
                                                groupQueue.add(nextStatesInGroup);
                                            }
                                        } else {
                                            searchingTrans[i] = new Object[]{"",stateCount,-1};
                                            statesT.put(Arrays.toString(possibleState), stateCount);
                                            stateCount++;
                                            searchingQueue.add(possibleState);
                                            groupQueue.add(nextStatesInGroup);
                                        }
                                    }
                                }
                                transitionMap.put(Arrays.toString(searchingState), searchingTrans);
                            }

                            // loop to find all returning states
                            while (!returningQueue.isEmpty()) {
                                Integer[] returningState = returningQueue.poll();
                                HashSet<Integer> group = returningGroupQueue.poll();
                                // create new transition function
                                Object[][] returningTrans = new Object[inputAlphabetT.size()][3];
                                if (returningState[statesA.size()] == 2) {
                                    // for all possible input symbol
                                    input:for (int i = 0; i < inputAlphabetT.size(); i++) {
                                        if (i == inputAlphabetT.size()-2) {// expect left end marker ^
                                            continue;
                                        }
                                        // create next possible state
                                        HashSet<Integer> nextStatesInGroup = new HashSet<Integer>();
                                        Integer[] possibleState = new Integer[statesA.size()+3];
                                        possibleState[statesA.size()] = 2;
                                        possibleState[statesA.size()+1] = returningState[statesA.size()+1];
                                        possibleState[statesA.size()+2] = returningState[statesA.size()+2];
                                        //check merging
                                        Integer leftState = null;
                                        for (Integer checkState : group) {
                                            if (transitionA[checkState][i][2] == null) {
                                                //map to error state
                                                returningTrans[i] = new Object[]{"",1,1};
                                                continue input;
                                            }
                                            Integer rightState = statesA.get((String)transitionA[checkState][i][1]);
                                            possibleState[rightState] = returningState[checkState];
                                            if (returningState[checkState] == 0) {
                                                leftState = checkState;
                                            }
                                            nextStatesInGroup.add(rightState);
                                        }

                                        if (nextStatesInGroup.size() == 1) {
                                            // if merge, found left state back to found mode
                                            Integer[] foundState = new Integer[statesA.size()+3];
                                            foundState[statesA.size()] = 3;
                                            foundState[statesA.size()+1] = leftState;
                                            foundState[statesA.size()+2] = returningState[statesA.size()+2];
                                            // if not exist add to state
                                            if (statesT.get(Arrays.toString(foundState)) != null) {
                                                returningTrans[i] = new Object[]{nextOutput,statesT.get(Arrays.toString(foundState)),1};
                                                if (transitionMap.get(Arrays.toString(foundState)) == null && !returningQueue.contains(foundState)) {
                                                    returningQueue.add(foundState);
                                                    returningGroupQueue.add(nextStatesInGroup);
                                                }
                                            } else {
                                                returningTrans[i] = new Object[]{nextOutput,stateCount,1};
                                                statesT.put(Arrays.toString(foundState), stateCount);
                                                stateCount++;
                                                returningQueue.add(foundState);
                                                returningGroupQueue.add(nextStatesInGroup);
                                            }
                                        } else {
                                            // if not, continue to find back position
                                            if (statesT.get(Arrays.toString(possibleState)) != null) {
                                                returningTrans[i] = new Object[]{"",statesT.get(Arrays.toString(possibleState)),1};
                                                if (transitionMap.get(Arrays.toString(possibleState)) == null && !returningQueue.contains(possibleState)) {
                                                    returningQueue.add(possibleState);
                                                    returningGroupQueue.add(nextStatesInGroup);
                                                }
                                            } else {
                                                returningTrans[i] = new Object[]{"",stateCount,1};
                                                statesT.put(Arrays.toString(possibleState), stateCount);
                                                stateCount++;
                                                returningQueue.add(possibleState);
                                                returningGroupQueue.add(nextStatesInGroup);
                                            }
                                        }
                                    }
                                } else if (returningState[statesA.size()] == 3) {
                                    // switch to forwarding mode
                                    Integer[] forwardingState = new Integer[statesA.size()+3];
                                    forwardingState[statesA.size()] = 0;
                                    forwardingState[statesA.size()+1] = returningState[statesA.size()+1];
                                    forwardingState[statesA.size()+2] = returningState[statesA.size()+2];
                                    Integer stateNum = statesT.get(Arrays.toString(forwardingState));
                                    if (stateNum == null) {
                                        stateNum = stateCount;
                                        statesT.put(Arrays.toString(forwardingState), stateCount);
                                        stateCount++;
                                    }
                                    for (int i = 0; i < inputAlphabetT.size(); i++) {
                                        returningTrans[i] = new Object[]{"",stateNum,-1};
                                    }
                                }
                                transitionMap.put(Arrays.toString(returningState), returningTrans);
                            }
                        }
                    }
                }
                transitionMap.put(Arrays.toString(currState), trans);
            }
        }

        //construct 2DFT encoding for T
        //initialise
        String Tencoding = "(";
        Boolean isFirst = true;// flag for first element in brackets
        //add states set
        Tencoding += "{";
        for (int i = 0; i < statesT.size(); i++) {
            if (isFirst) {
                Tencoding += "q" + String.valueOf(i);
                isFirst = false;
            } else {
                Tencoding +=  "," + "q" + String.valueOf(i);
            }
        }
        Tencoding += "},{";
        //add input and output alphbet
        isFirst = true;
        for (String symbol : inputAlphabetT.keySet()) {
            if (symbol != "^" && symbol != "$") {
                if (isFirst) {
                    Tencoding += symbol;
                    isFirst = false;
                } else {
                    Tencoding +=  "," + symbol;
                }
            }
        }
        Tencoding += "},{";
        isFirst = true;
        for (String symbol : outputAlphabetT) {
            if (isFirst) {
                Tencoding += symbol;
                isFirst = false;
            } else {
                Tencoding +=  "," + symbol;
            }
        }
        Tencoding += "},{";

        //add transition function
        isFirst = true;
        for (String state : statesT.keySet()) {
            int stateNum = statesT.get(state);
            String stateNumString = String.valueOf(stateNum);
            if (transitionMap.get(state) == null) {
                continue;
            }
            Object[][] transition = transitionMap.get(state);
            for (String symbol : inputAlphabetT.keySet()) {
                int symbolNum = inputAlphabetT.get(symbol);
                Object[] singleTransition = transition[symbolNum];
                if (singleTransition[2] != null) {
                    if (isFirst) {
                        Tencoding += "(" + "q" + stateNumString + "," + symbol + "," + (String)singleTransition[0] + "," + "q" + String.valueOf((Integer)singleTransition[1]) + "," + String.valueOf((Integer)singleTransition[2]) + ")";
                        isFirst = false;
                    } else {
                        Tencoding += ",(" + "q" + stateNumString + "," + symbol + "," + (String)singleTransition[0] + "," + "q" + String.valueOf((Integer)singleTransition[1]) + "," + String.valueOf((Integer)singleTransition[2]) + ")";
                    }
                }
            }
        }

        //add initial state
        Tencoding += "},{q0},{";

        //add final state
        isFirst = true;
        for (int i = 0; i < statesA.size(); i++) {
            Integer[] finalStateT = new Integer[statesA.size()+3];
            finalStateT[statesA.size()] = 0;
            finalStateT[statesA.size()+1] = i;
            finalStateT[statesA.size()+2] = finalB;
            if (isFirst) {
                Tencoding += "q" + String.valueOf(statesT.get(Arrays.toString(finalStateT)));
                isFirst = false;
            } else {
                Tencoding +=  ",q" + String.valueOf(statesT.get(Arrays.toString(finalStateT)));
            }
        }
        Tencoding += "})";

        // //construct 2DFT encoding for A
        // //initialise
        // String Aencoding = "(";
        // //add states set
        // Aencoding += "{";
        // isFirst = true;
        // for (String state : statesA.keySet()) {
        //     if (isFirst) {
        //         Aencoding += state;
        //         isFirst = false;
        //     } else {
        //         Aencoding +=  "," + state;
        //     }
        // }
        // Aencoding += "},{";
        // //add input and output alphbet
        // isFirst = true;
        // for (String symbol : inputAlphabetA.keySet()) {
        //     if (symbol != "^" && symbol != "$") {
        //         if (isFirst) {
        //             Aencoding += symbol;
        //             isFirst = false;
        //         } else {
        //             Aencoding +=  "," + symbol;
        //         }
        //     }
        // }
        // Aencoding += "},{";
        // isFirst = true;
        // for (String symbol : outputAlphabetA) {
        //     if (isFirst) {
        //         Aencoding += symbol;
        //         isFirst = false;
        //     } else {
        //         Aencoding +=  "," + symbol;
        //     }
        // }
        // Aencoding += "},{";

        // //add variable sets
        // isFirst = true;
        // for (String state : statesA.keySet()) {
        //     for (String symbol : inputAlphabetA.keySet()) {
        //         int stateNum = statesA.get(state);
        //         int symbolNum = inputAlphabetA.get(symbol);
        //         Object[] singleTransition = transitionA[stateNum][symbolNum];
        //         if (singleTransition[2] != null) {
        //             if (isFirst) {
        //                 Aencoding += "(" + state + "," + symbol + "," + (String)singleTransition[0] + "," + (String)singleTransition[1] + "," + String.valueOf((Integer)singleTransition[2]) + ")";
        //                 isFirst = false;
        //             } else {
        //                 Aencoding += ",(" + state + "," + symbol + "," + (String)singleTransition[0] + "," + (String)singleTransition[1] + "," + String.valueOf((Integer)singleTransition[2]) + ")";
        //             }
        //         }
        //     }
        // }

        // //add initial state
        // Aencoding += "},{"+ initialStateA +"},{";

        // //add final state
        // isFirst = true;
        // for (String state : finalStatesA) {
        //     if (isFirst) {
        //         Aencoding += state;
        //         isFirst = false;
        //     } else {
        //         Aencoding +=  "," + state;
        //     }
        // }
        // Aencoding += "})";

        // //construct 2DFT encoding for B
        // //initialise
        // String Bencoding = "(";
        // isFirst = true;// flag for first element in brackets
        // //add states set
        // Bencoding += "{";
        // for (String state : statesB.keySet()) {
        //     if (isFirst) {
        //         Bencoding += state;
        //         isFirst = false;
        //     } else {
        //         Bencoding +=  "," + state;
        //     }
        // }
        // Bencoding += "},{";
        // //add input and output alphbet
        // isFirst = true;
        // for (String symbol : inputAlphabetB.keySet()) {
        //     if (symbol != "^" && symbol != "$") {
        //         if (isFirst) {
        //             Bencoding += symbol;
        //             isFirst = false;
        //         } else {
        //             Bencoding +=  "," + symbol;
        //         }
        //     }
        // }
        // Bencoding += "},{";
        // isFirst = true;
        // for (String symbol : outputAlphabetB) {
        //     if (isFirst) {
        //         Bencoding += symbol;
        //         isFirst = false;
        //     } else {
        //         Bencoding +=  "," + symbol;
        //     }
        // }
        // Bencoding += "},{";

        // //add variable sets
        // isFirst = true;
        // for (String state : statesB.keySet()) {
        //     for (String symbol : inputAlphabetB.keySet()) {
        //         int stateNum = statesB.get(state);
        //         int symbolNum = inputAlphabetB.get(symbol);
        //         Object[] singleTransition = transitionB[stateNum][symbolNum];
        //         if (singleTransition[2] != null) {
        //             if (isFirst) {
        //                 Bencoding += "(" + state + "," + symbol + "," + (String)singleTransition[0] + "," + (String)singleTransition[1] + "," + String.valueOf((Integer)singleTransition[2]) + ")";
        //                 isFirst = false;
        //             } else {
        //                 Bencoding += ",(" + state + "," + symbol + "," + (String)singleTransition[0] + "," + (String)singleTransition[1] + "," + String.valueOf((Integer)singleTransition[2]) + ")";
        //             }
        //         }
        //     }
        // }

        // //add initial state
        // Bencoding += "},{"+ initialStateB +"},{";

        // //add final state
        // isFirst = true;
        // for (String state : finalStatesB) {
        //     if (isFirst) {
        //         Bencoding += state;
        //         isFirst = false;
        //     } else {
        //         Bencoding +=  "," + state;
        //     }
        // }
        // Bencoding += "})";

        // String output = "Encoding of A:\n" + Aencoding + "\n" + "Encoding of B:\n" + Bencoding + "\n" + "Encoding of T:\n" + Tencoding;
        // return output;
        return Tencoding;
    }

    public void generateSSTGraphPDF(String encoding) throws Exception {
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
                        if (variableUpdate[i][j][index] != null && variableUpdate[i][j][index].contains(varString)) {
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

        String SSTLatex = "\\documentclass[landscape]{slides}\n"
                        + "\\usepackage{tikz}\n"
                        + "\\usepackage[landscape]{geometry}\n"
                        + "\\begin{document}\n"
                        + "\\usetikzlibrary{automata,positioning}\n"
                        + "\\resizebox{\\linewidth}{!}{\n"
                        + "\\begin{tikzpicture}[node distance=4cm,auto]\n";
        
        if (partialOutput[states.get(initialState)] == null || partialOutput[states.get(initialState)].equals("")) {
            SSTLatex += "\\node[state without output,initial] (" + initialState + ") {$" + initialState + "$};\n";
        } else {
            SSTLatex += "\\node[state with output,initial] (" + initialState + ") {$" + initialState + "$ \\nodepart{lower} $" + partialOutput[states.get(initialState)] + "$};\n";
        }
        
        String lastState = initialState;
        for (int index = 0; index < statesArray.length; index++) {
            if (!statesArray[index].equals(initialState)) {
                if (partialOutput[index] == null) {
                    SSTLatex += "\\node[state without output] (" + statesArray[index] + ") [right of=" + lastState + "] {$" + statesArray[index] + "$};\n";
                    lastState = statesArray[index];
                } else {
                    SSTLatex += "\\node[state with output] (" + statesArray[index] + ") [right of=" + lastState + "] {$" + statesArray[index] + "$ \\nodepart{lower} $" + partialOutput[index] + "$};\n";
                    lastState = statesArray[index];
                }
                
            }
        }
        String[][] stsvu = new String[statesArray.length][statesArray.length];
        for (int i = 0; i < stateTransition.length; i++) {
            for (int j = 0; j <stateTransition[i].length; j++) {
                if (stateTransition[i][j] != null) {
                    if (stsvu[i][states.get(stateTransition[i][j])] == null) {
                        stsvu[i][states.get(stateTransition[i][j])] = " " + inAlpha[j];
                    } else {
                        stsvu[i][states.get(stateTransition[i][j])] += " " + inAlpha[j];
                    }
                    for (int k = 0; k < variableUpdate[i][j].length; k++) {
                        if (variableUpdate[i][j][k] != null && !variableUpdate[i][j][k].equals("")) {
                            stsvu[i][states.get(stateTransition[i][j])] += " & " + varArray[k] + ":=" + variableUpdate[i][j][k] + " \\\\ ";
                        }
                    }
                    stsvu[i][states.get(stateTransition[i][j])] += "\\hline ";
                }
            }
        }
        SSTLatex += "\\path[->] ";
        for (int i = 0; i < statesArray.length; i++) {
            SSTLatex += "(" + statesArray[i] + ") ";
            for (int j = 0; j <statesArray.length; j++) {
                if (stsvu[i][j] != null && !stsvu[i][j].equals("")) {
                    if (i == j) {
                        SSTLatex += "edge [loop] node [swap] {\\begin{tabular}{c|c} "
                                  + stsvu[i][j].substring(0, stsvu[i][j].length()-8)
                                  + "\\end{tabular}} (" + statesArray[j] + ")\n";
                    } else {
                        SSTLatex += "edge [bend left] node {\\begin{tabular}{c|c} "
                                  + stsvu[i][j].substring(0, stsvu[i][j].length()-8)
                                  + "\\end{tabular}} (" + statesArray[j] + ")\n";
                    }
                }
            }
        }
        SSTLatex += ";\n"
                  + "\\end{tikzpicture}\n"
                  + "}\n"
                  + "\\end{document}\n";
        
        byte[]sourceByte = SSTLatex.getBytes();
        try {
            Path path = Paths.get("graph");
            Files.createDirectories(path);
            File file = new File("graph", "SSTGraph.tex");
            if(file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(sourceByte);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("latex for a graph of SST is generated in ./graph/SSTGraph.tex");
    }

    public void generateTDFTGraphPDF(String encoding) throws Exception {
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
        inputAlphabet.put("$",inAlpha.length+1);
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

        String SSTLatex = "\\documentclass[landscape]{slides}\n"
                        + "\\usepackage{tikz}\n"
                        + "\\usepackage[landscape]{geometry}\n"
                        + "\\begin{document}\n"
                        + "\\usetikzlibrary{automata,positioning}\n"
                        + "\\resizebox{\\linewidth}{!}{\n"
                        + "\\begin{tikzpicture}[node distance=2.5cm,auto]\n";
        

        SSTLatex += "\\node[state,initial] (" + initialState + ") {$" + initialState + "$};\n";
        
        
        String lastState = initialState;
        for (int index = 0; index < statesArray.length; index++) {
            if (!statesArray[index].equals(initialState)) {
                SSTLatex += "\\node[state] (" + statesArray[index] + ") [right of=" + lastState + "] {$" + statesArray[index] + "$};\n";
                lastState = statesArray[index];
            }
        }

        SSTLatex += "\\path[->] ";
        for (int i = 0; i < statesArray.length; i++) {
            SSTLatex += "(" + statesArray[i] + ") ";
            for (String symbol : inputAlphabet.keySet()) {
                int symbolNum = inputAlphabet.get(symbol);
                if (transition[i][symbolNum][2] != null) {
                    int rightState = states.get((String)transition[i][symbolNum][1]);
                    if (i == rightState) {
                        if (symbol.equals("^")) {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$\\string^" + "|" + "\\varepsilon$"
                                         + "} ()\n";
                            } else {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$\\string^" + "|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} ()\n";
                            }
                        } else if (symbol.equals("$")) {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$\\" + symbol + "|" + "\\varepsilon$"
                                         + "} ()\n";
                            } else {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$\\" + symbol + "|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} ()\n";
                            }
                        } else {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$" + symbol + "|" + "\\varepsilon$"
                                         + "} ()\n";
                            } else {
                                SSTLatex += "edge [loop] node [swap] {"
                                         + "$" + symbol + "|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} ()\n";
                            }
                        }
                    } else {
                        if (symbol.equals("^")) {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [bend left] node {"
                                         + "$\\string^" + "\\|" + "\\varepsilon$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            } else {
                                SSTLatex += "edge [bend left] node {"
                                         + "$\\string^" + "\\|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            }
                        } else if (symbol.equals("$")) {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [bend left] node {"
                                         + "$\\" + symbol + "\\|" + "\\varepsilon$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            } else {
                                SSTLatex += "edge [bend left] node {"
                                         + "$\\" + symbol + "|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            }
                        } else {
                            if (((String)transition[i][symbolNum][0]).equals("")) {
                                SSTLatex += "edge [bend left] node {"
                                         + "$" + symbol + "|" + "\\varepsilon$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            } else {
                                SSTLatex += "edge [bend left] node {"
                                         + "$" + symbol + "|" + (String)transition[i][symbolNum][0] + "$"
                                         + "} (" + (String)transition[i][symbolNum][1] + ")\n";
                            }
                        }
                    }
                }
            }
        }
        SSTLatex += ";\n"
                  + "\\end{tikzpicture}\n"
                  + "}\n"
                  + "\\end{document}\n";
        
        byte[]sourceByte = SSTLatex.getBytes();
        try {
            Path path = Paths.get("graph");
            Files.createDirectories(path);
            File file = new File("graph", "TDFTGraph.tex");
            if(file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(sourceByte);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("latex for a graph of SST is generated in ./graph/TDFTGraph.tex");
    }

    public static boolean isInList(ArrayList<Integer[]> list, Integer[] candidate){
        for(Integer[] item : list){
            if (Arrays.equals(item, candidate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInQueue(Queue<Integer[]> queue, Integer[] candidate){
        for(Integer[] item : queue){
            if (Arrays.equals(item, candidate)) {
                return true;
            }
        }
        return false;
    }
}
