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
                    if (evaluateNodeFormula(root,inputString.length()+1,i*(inputString.length()+1)+j)) {
                        outputNodeSet[i][j] = true;
                    } else {
                        outputNodeSet[i][j] = false;
                    }
                }
            }
            
        } 

        //use edge formulas to construct new gragh
        for (int i = 0; i < edgeFormula.length; i++) {
            for (int j = 0; j < edgeFormula.length; j++) {
                for (String outputSymbol : outputAlphabet.keySet()) {
                    int outputNum = outputAlphabet.get(outputSymbol);
                    if (edgeFormula[i][j][outputNum].equals(null)) {
                        continue;
                    } else {
                        Node root = edgeFormula[i][j][outputNum];
                        for (int k = 0; k < inputString.length()+1; k++) {
                            for (int l = 0; l < inputString.length()+1; l++) {
                                if (evaluateNodeFormula(root,i*(inputString.length()+1)+k,j*(inputString.length()+1)+l)) {
                                    outputEdgeSet[i*(inputString.length()+1)+k][j*(inputString.length()+1)+l] = outputSymbol;
                                }
                            }
                        }
                    }
                }
                
            }
        }

        //find string representation in the new gragh
        String output = "";
        String prevSymbol;
        String nextSymbol;
        int prevVertex;
        int nextVertex;

        loop:for (int i = 0; i < copySet.size(); i++) {
            for (int j = 0; j < inputString.length()+1; j++) {
                if (outputNodeSet[i][j] == true) {
                    outputNodeSet[i][j] = false;
                    nextVertex = i*(inputString.length()+1)+j;
                    prevVertex = i*(inputString.length()+1)+j;
                    do {
                        nextSymbol = null;
                        for (int k = 0; k < outputEdgeSet.length; k++) {
                            if (outputEdgeSet[nextVertex][k] != null) {
                                nextSymbol = outputEdgeSet[nextVertex][k];
                                nextVertex = k;
                                outputNodeSet[k/(inputString.length()+1)][k%(inputString.length()+1)] = false;
                                output = output.concat(nextSymbol);
                                break;
                            }
                        }
                    } while (nextSymbol != null);

                    do {
                        prevSymbol = null;
                        for (int k = 0; k < outputEdgeSet.length; k++) {
                            if (outputEdgeSet[k][prevVertex] != null) {
                                prevSymbol = outputEdgeSet[k][prevVertex];
                                prevVertex = k;
                                outputNodeSet[k/(inputString.length()+1)][k%(inputString.length()+1)] = false;
                                output = prevSymbol.concat(output);
                                break;
                            }
                        }
                    } while (prevSymbol != null);

                    break loop;
                }
            }
        }
        
        Boolean vaild = true;
        for (int i = 0; i < copySet.size(); i++) {
            for (int j = 0; j < inputString.length()+1; j++) {
                if (outputNodeSet[i][j] == true) {
                    vaild = false;
                }
            }
        }

        if (vaild == true) {
            return output;
        } else {
            System.err.println("Error. Undefined output.");
            return "";
        }
        
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

    /**
     * Evaluate whether Node Formula is true for input vertex
     * @param formula input string
     * @param inputLength vertex number in the same copy set = input string length + 1
     * @param vertexNum vertex number
     * @return true or false
     */
    private Boolean evaluateNodeFormula(Node formula, int inputLength, int vertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            return evaluateNodeFormula(formula.getLeftChild(),inputLength,vertexNum) && evaluateNodeFormula(formula.getRightChild(),inputLength,vertexNum);
        } else if (data.equals("+")) {
            return evaluateNodeFormula(formula.getLeftChild(),inputLength,vertexNum) || evaluateNodeFormula(formula.getRightChild(),inputLength,vertexNum);
        } else if (data.equals("!")) {
            return !evaluateNodeFormula(formula.getLeftChild(),inputLength,vertexNum);
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            if (vertexNum == inputEdgeSet[0].length-1) {
                return false;
            }
            String symbol = data.substring(4, 5);
            if (inputEdgeSet[vertexNum][vertexNum+1].equals(symbol)) {
                return true;
            } else {
                return false;
            }
        } else if (data.matches("#.")) {
            for (int i = (vertexNum/inputLength)*inputLength; i < ((vertexNum/inputLength)+1)*inputLength; i++) {
                if (evaluateNodeFormulaBound(formula.getLeftChild(),vertexNum,data.substring(1, 2),i))    {
                    return true;
                }
            }
            return false;
        } else if (data.matches("$.")) {
            for (int i = (vertexNum/inputLength)*inputLength; i < ((vertexNum/inputLength)+1)*inputLength; i++) {
                if (!evaluateNodeFormulaBound(formula.getLeftChild(),vertexNum,data.substring(1, 2),i))    {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Evaluate whether Node Formula is true for input bound vertex
     * @param formula input string
     * @param freeVertexNum free vertex number
     * @param boundVar bound variable 
     * @param boundVertexNum bound vertex number
     * @return true or false
     */
    private Boolean evaluateNodeFormulaBound(Node formula, int freeVertexNum, String boundVar, int boundVertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            return evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum) && evaluateNodeFormulaBound(formula.getRightChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.equals("+")) {
            return evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum) || evaluateNodeFormulaBound(formula.getRightChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.equals("!")) {
            return !evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            String var = data.substring(4, 5);
            if (var.equals(boundVar)) {
                if (boundVertexNum == inputEdgeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[boundVertexNum][boundVertexNum+1].equals(symbol)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (freeVertexNum == inputEdgeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[freeVertexNum][freeVertexNum+1].equals(symbol)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else if (data.matches(".<.")) {
            if (data.substring(0, 1).equals(boundVar)) {
                if (data.substring(2).equals(boundVar)) {
                    return false;
                } else {
                    if (freeVertexNum > boundVertexNum) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                if (data.substring(2).equals(boundVar)) {
                    if (freeVertexNum < boundVertexNum) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
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
