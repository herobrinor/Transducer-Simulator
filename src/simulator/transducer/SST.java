package simulator.transducer;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Deterministic streaming string transducers (SST)
 * Formal definition: M = (Q, I, O, X, q, f, t1, t2)
 * where, Q is a finite set of states
 *        I is a finite set of input alphabet
 *        O is a finite set of output alphabet
 *        X is a finite set of variables
 *        q is the initial state
 *        f is a partial output function
 *        t1 is a state-transition function
 *        t2 is a variable-update function
 * f: Q -> (O U X)*
 * t1: Q × I -> Q
 * t2: Q × I × X -> (O U X)*
 */
public class SST extends Transducer{

    // A finite set of variables of the transducer.
    private HashMap<String, Integer> variables;

    // A partial output function of the transducer.
    private String[] partialOutput;

    // A state-transition function of the transducer.
    private String[][] stateTransition;

    // a variable-update function of the transducer.
    private String[][][] variableUpdate;

    public SST(String initialState, HashMap<String, Integer> states, HashMap<String, Integer> inputAlphabet, HashSet<String> outputAlphabet, HashMap<String, Integer> variables, String[] partialOutput, String[][] stateTransition, String[][][] variableUpdate) {
        super(initialState, states, inputAlphabet, outputAlphabet);
        this.variables = variables;
        this.partialOutput = partialOutput;
        this.stateTransition = stateTransition;
        this.variableUpdate = variableUpdate;
    }

    /**
     * Run SST over input string
     * @param inputString input string
     * @return Output string
     */
    public String run(String inputString) {
        // switch to char array
        char[] stringArray = inputString.toCharArray();
        // current state
        String currState = initialState;
        System.out.println("Running chain of states:");
        System.out.printf(currState);
        String output = "";
        int currStateNum;
        int currInputNum;
        int currVarNum;
        char[] outputFormula;
        char[] newValueFormula;
        String newValue;
        String currSymbol;
        HashMap<String, String> variableValue = new HashMap<String, String>();
        

        for (String variable: variables.keySet()) {
            variableValue.put(variable, new String(""));
        }

        for (int currPosition = 0; currPosition < stringArray.length; currPosition++) {
            HashMap<String, String> newVariableValue = new HashMap<String, String>();
            currStateNum = states.get(currState);
            currInputNum = inputAlphabet.get(String.valueOf(stringArray[currPosition]));
            currState = stateTransition[currStateNum][currInputNum];
            for (String variable: variables.keySet()) {
                currVarNum = variables.get(variable);
                if (variableUpdate[currStateNum][currInputNum][currVarNum] == null) {
                    continue;
                }
                newValue = "";
                newValueFormula = variableUpdate[currStateNum][currInputNum][currVarNum].toCharArray();
                for (int i = 0; i < newValueFormula.length; i++) {
                    currSymbol = String.valueOf(newValueFormula[i]);
                    if (variables.containsKey(currSymbol)) {
                        newValue = newValue.concat(variableValue.get(currSymbol));
                    } else { //outputAlphabet.contains(currSymbol)
                        newValue = newValue.concat(currSymbol);
                    }
                }
                newVariableValue.put(variable, newValue);
            }
            variableValue = newVariableValue;
            System.out.printf("->" + currState);
        }

        currStateNum = states.get(currState);
        if (partialOutput[currStateNum] != null && !partialOutput[currStateNum].equals("")) {
            outputFormula = partialOutput[currStateNum].toCharArray();
            for (int i = 0; i < outputFormula.length; i++) {
                currSymbol = String.valueOf(outputFormula[i]);
                if (variables.containsKey(currSymbol)) {
                    output = output.concat(variableValue.get(currSymbol));
                } else { //outputAlphabet.contains(currSymbol)
                    output = output.concat(currSymbol);
                }
            }
        } else {
            output = "";
        }
        System.out.printf("\n");
        System.out.println("Variables:");
        for (String variable : variables.keySet()) {
            if (variableValue.get(variable) != null) {
                System.out.println(variable + ": " + variableValue.get(variable));
            } else {
                System.out.println(variable + ": \u03B5");      //print epslion symbol
            }
        }
        return output;
    }

    /**
     * Check whether input string is vaild
     * @param inputString input string
     * @return validation of input string
     */
    public Boolean vaildInput(String inputString) {
        char [] stringArray = inputString.toCharArray();
        Boolean validation = true;
        //check weather every symbol is in the input Alphabet
        for (char c : stringArray) {
            if (!inputAlphabet.containsKey(String.valueOf(c))) {
                validation = false;
            }
        }
        return validation;
    }
}
