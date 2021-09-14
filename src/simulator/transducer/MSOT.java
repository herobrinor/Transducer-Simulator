package simulator.transducer;

import java.util.HashMap;
import simulator.util.ParseTree;

/**
 * Deterministic MSO transducers (MSOT)
 * Formal definition: M = (I, O, C, ϕ{c}{σ}(x), ϕ{c1,c2}{γ}(x,y))
 * where, I is a finite set of input alphabet
 *        O is a finite set of output alphabet
 *        C is a finite copy set
 *        ϕ{c}(x) are node formulas with one free node variable x
 *        ϕ{c1,c2}{γ}(x,y) are edge formulas with two free node variables x,y
 */
public class MSOT{

    private HashMap<String, Integer> inputAlphabet;
    private HashMap<String, Integer> outputAlphabet;
    private HashMap<String, Integer> copySet;
    private ParseTree[] nodeFormula;
    private ParseTree[][][] edgeFormula;
    private String[][] inputEdgeSet;

    public MSOT(HashMap<String, Integer> inputAlphabet, HashMap<String, Integer> outputAlphabet, HashMap<String, Integer> copySet, ParseTree[] nodeFormula, ParseTree[][][] edgeFormula){
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.copySet = copySet;
        this.nodeFormula = nodeFormula;
        this.edgeFormula = edgeFormula;
    }

    /**
     * Run MSOT over input string
     * @param inputString input string
     * @return Output string
     */
    public String run(String inputString) {
        //TODO
        char [] stringArray = inputString.toCharArray();
        inputEdgeSet = new String[copySet.size()*(inputString.length()+1)][copySet.size()*(inputString.length()+1)];
        Boolean[][] outputNodeSet = new Boolean[copySet.size()][inputString.length()+1];
        String[][] outputEdgeSet = new String[copySet.size()*(inputString.length()+1)][copySet.size()*(inputString.length()+1)];
        //construct string representation and its copy
        for (int i = 0; i < stringArray.length; i++) {
            for (int j = 0; j < copySet.size(); j++) {
                int curr = i+j*copySet.size();
                inputEdgeSet[curr][curr+1] = String.valueOf(stringArray[i]);
            }
        }
        //use node formulas to construct new gragh
        for (int i = 0; i < nodeFormula.length; i++) {
            for (int j = 0; j < inputString.length()+1; j++) {
                if (evaluateNode(i,j)) {
                    outputNodeSet[i][j] = true;
                } else {
                    outputNodeSet[i][j] = false;
                }
            }
        } 

        //use edge formulas to construct new gragh
        for (int i = 0; i < nodeFormula.length; i++) {
            for (int j = i*(inputString.length()+1); j < (i+1)*(inputString.length()+1); j++) {
                
            }
        }        

        String output = "";
        
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

    private Boolean evaluateNode(int copyNum,int nodeNum) {
        ParseTree formula = nodeFormula[copyNum];

        return false;
    }
}
