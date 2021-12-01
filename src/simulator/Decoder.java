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
        states.put("m",statesArray.length+finalStatesArray.length-1);
        states.put("qerr",statesArray.length+finalStatesArray.length);
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
        //starting state is identity function form Q to Q U {m, qerr}.
        int base = states.size();
        //store visited states, exploring states, variable-update function, state-transition function and pratial output function
        ArrayList<Integer[]> SSTState = new ArrayList<Integer[]>();
        Queue<Integer[]> stateQueue = new LinkedList<Integer[]>();
        Queue<String[]> sharedVarStateQueue = new LinkedList<String[]>();
        ArrayList<String> partialOutputFunc = new ArrayList<String>();
        ArrayList<Object[][]> stateTransitionFunc =new ArrayList<Object[][]>();
        ArrayList<String[][]> variableUpdateFunc =new ArrayList<String[][]>();
        HashMap<String, Integer> variables = new HashMap<String, Integer>();
        String[] variableArray = new String[base-1];
        HashMap<String, Integer> sharedVariables = new HashMap<String, Integer>();
        String[] sharedVariableArray = new String[2*base-5];// to be base-3 + base-2 before reducing 
        ArrayList<String[]> sharedVariableStates = new ArrayList<String[]>();
        ArrayList<String[][]> sharedVariableUpdateFunc =new ArrayList<String[][]>();
        ArrayList<Integer[]> newStateArrayList = new ArrayList<Integer[]>();
        ArrayList<String[]> newSharedVarStateArrayList = new ArrayList<String[]>();
        //initalise variable set in SST
        char varNum1 = 65;//symbol A
        int count = 0;
        while (count < base-1) {
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
        while (count < 2*base-5) {
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
        Integer[] nextState = new Integer[base-1];
        String[] varUpLEM = new String[variableArray.length];
        // state except m
        for (int i = 0; i < base-2; i++) {
            Object[] transfunc = transition[i][inAlpha.length];
            if (transfunc[2] != null && (int)transfunc[2] == 1) {//careful about 0
                nextState[i] = states.get((String)transfunc[1]);
                varUpLEM[i] = (String)transfunc[0];
            } else {//map to qerr
                nextState[i] = base-1;
                varUpLEM[i] = "";
            }
        }
        // state m
        nextState[base-2] = states.get(initialState);
        varUpLEM[base-2] = "";
        Integer[] firstState = nextState;
        //compute stateTransitionFunc and variable-update function combined with simulating on left endmarker
        String[][] varUp = new String[inAlpha.length][variableArray.length];
        String[][] sharedVarUp = new String[inAlpha.length][sharedVariableArray.length];
        Object[][] stateUp = new Object[inAlpha.length][2];
        String[] sharedVarState = new String[base-2];
        for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
            nextState = new Integer[base-1];
            String[] newSharedVarState = new String[base-2];
            // state except m
            for (int j = 0; j < base-2; j++) {//for every state
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
                            nextState[j] = base-1;
                            break;
                        }   
                    }
                } else {//map to qerr
                    nextState[j] = base-1;
                    varUp[i][j] = "";
                }
            }
            // state m
            String newVal = varUpLEM[base-2];
            int mState = firstState[base-2];
            Object[] mtransfunc = transition[mState][i];
            //careful about 0
            if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                nextState[base-2] = states.get((String)mtransfunc[1]);
                newVal += (String)mtransfunc[0];
                varUp[i][base-2] = newVal;
            } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                while (true) {
                    newVal = (String)mtransfunc[0];
                    int leftState = states.get(mtransfunc[1]);
                    newVal += varUpLEM[leftState];
                    int backState = firstState[leftState];
                    mtransfunc = transition[backState][i];
                    if (mtransfunc[2] != null && (int)mtransfunc[2] == 1) {
                        newVal += mtransfunc[0];
                        varUp[i][base-2] = newVal;
                        nextState[base-2] = states.get((String)mtransfunc[1]);
                        break;
                    } else if (mtransfunc[2] != null && (int)mtransfunc[2] == -1) {
                        continue;
                    } else {
                        newVal = "";
                        varUp[i][base-2] = newVal;
                        nextState[base-2] = base-1;
                        break;
                    }
                }
            } else {//map to qerr
                nextState[base-2] = base-1;
                varUp[i][base-2] = "";
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
        String output = variableArray[base-2];
        String finalState = "m";
        int mState = firstState[base-2];
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
            int[] prevSharedVarCount = new int[2*base-5];
            for (int i = 0; i < sharedVarState.length; i++) {
                if (sharedVarState[i] != null) {
                    char[] varsCharArray = sharedVarState[i].toCharArray();
                    for (int k = 0; k < varsCharArray.length; k++) {
                        prevSharedVarCount[sharedVariables.get(String.valueOf(varsCharArray[k]))]++;
                    }
                }
            }
            for (int i = 0; i < inAlpha.length; i++) {//for every input Symbol
                nextState = new Integer[base-1];
                int[] merging = new int[base-2];
                String[] newSharedVarState = new String[base-2];
                int[] sharedVarCount = new int[2*base-5];
                // state except m
                for (int j = 0; j < base-2; j++) {
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
                                nextState[j] = base-1;
                            }                               
                            break;
                        }
                    } else {//map to qerr
                        nextState[j] = base-1;
                        varUp[i][j] = "";
                    }
                }
                // reduce the tree using number of variables that varUp contains
                // if number of variables > 1 shared variables need to be used
                int[] varCountArray = new int[base-2];
                int maxVarNum = 0;
                for (int j = 0; j < base-2; j++) {
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
                        for (int j = 0; j < base-2; j++) {
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
                                            for (int k = 0; k < base-2; k++) {
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
                                            for (int k = 0; k < base-2; k++) {
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
                    for (int j = 0; j < base-2; j++) {
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
                // for (int j = 0; j < base-2; j++) {
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
                        for (int k = 0; k < base-2; k++) {
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
                String newVal = variableArray[base-2];
                mState = currState[base-2];
                if (mState == base-1) {
                    nextState[base-2] = base-1;
                    varUp[i][base-2] = "@";
                } else {
                    Object[] transfunc = transition[mState][i];
                    if (transfunc[2] != null && (int)transfunc[2] == 1) {
                        nextState[base-2] = states.get((String)transfunc[1]);
                        if ((String)transfunc[0] != null) {
                            newVal += (String)transfunc[0];
                        } else {
                            newVal += "";
                        }
                        varUp[i][base-2] = newVal;
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
                        nextState[base-2] = nextState[mState];
                        varUp[i][base-2] = newVal;
                        varUp[i][mState] = "";//used once in the whole run
                    } else {//map to qerr
                        nextState[base-2] = base-1;
                        varUp[i][base-2] = "";
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
            output = variableArray[base-2];
            // System.out.println(output);
            finalState = "m";
            mState = currState[base-2];
            if (mState == base-1) {
                finalState = "qerr";
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
    public String fromSSTtoTDFT(String encoding){
        //TODO: translation function
        return "";
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
