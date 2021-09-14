package simulator.transducer;

import java.util.HashMap;
import simulator.util.Node;

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
    private Node[] nodeFormula;
    private Node[][][] edgeFormula;
    private String[][] inputEdgeSet;

    public MSOT(HashMap<String, Integer> inputAlphabet, HashMap<String, Integer> outputAlphabet, HashMap<String, Integer> copySet, Node[] nodeFormula, Node[][][] edgeFormula){
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
            if (nodeFormula[i].equals(null)) {
                for (int j = 0; j < inputString.length()+1; j++) {
                    outputNodeSet[i][j] = false;
                }
            } else {
                Node root = nodeFormula[i];
                for (int j = 0; j < inputString.length()+1; j++) {
                    if (evaluateNodeFormula(root,i*(inputString.length()+1)+j)) {
                        outputNodeSet[i][j] = true;
                    } else {
                        outputNodeSet[i][j] = false;
                    }
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

    private Boolean evaluateNodeFormula(Node formula, int vertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            return evaluateNodeFormula(formula.getLeftChild(),vertexNum) && evaluateNodeFormula(formula.getRightChild(),vertexNum);
        } else if (data.equals("+")) {
            return evaluateNodeFormula(formula.getLeftChild(),vertexNum) || evaluateNodeFormula(formula.getRightChild(),vertexNum);
        } else if (data.equals("!")) {
            return !evaluateNodeFormula(formula.getLeftChild(),vertexNum);
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            String symbol = data.substring(4, 5);
            if (inputEdgeSet[vertexNum][vertexNum+1].equals(symbol)) {
                return true;
            } else {
                return false;
            }
        } else if (data.matches("#.")) {
            return evaluateNodeFormulaExist(formula.getLeftChild(),vertexNum,data.substring(1, 2));
        } else if (data.matches("$.")) {
            return evaluateNodeFormulaForall(formula.getLeftChild(),vertexNum,data.substring(1, 2));
        }
        return false;
    }

    private Boolean evaluateNodeFormulaExist(Node formula, int vertexNum, String variable) {
        return false;
    }

    private Boolean evaluateNodeFormulaForall(Node formula, int vertexNum, String variable) {
        return false;
    }

    private Boolean evaluateEdgeFormula(Node formula, int vertex1Num, int vertex2Num) {
        String data = formula.getData();
        if (data.equals("*")) {
            return formula.getLeftChild().evaluate() && formula.getRightChild().evaluate();
        } else if (data.equals("+")) {
            return formula.getLeftChild().evaluate() || formula.getRightChild().evaluate();
        } else if (data.equals("!")) {
            return !formula.getLeftChild().evaluate();
        } else if (data.matches("edge\\{.\\}\\(.,.\\)")) {

        } else if (data.matches("next\\{.\\}\\(.,.\\)")) {

        } else if (data.matches("fps\\{.\\}\\(.,.\\)")) {

        } else if (data.matches("ϕ\\{.*,.*\\}\\{.\\}\\(.\\)")) {

        }
        return false;
    }
}
