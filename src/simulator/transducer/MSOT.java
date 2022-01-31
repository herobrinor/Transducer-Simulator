package simulator.transducer;
import simulator.util.Node;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Deterministic MSO transducers (MSOT)
 * Formal definition: M = (I, O, C, F{c}{σ}(x), F{c1,c2}{γ}(x,y))
 * where, I is a finite set of input alphabet
 *        O is a finite set of output alphabet
 *        C is a finite copy set
 *        F{c}(x) are node formulas with one free node variable x
 *        F{c1,c2}{γ}(x,y) are edge formulas with two free node variables x,y
 */
public class MSOT{

    private HashMap<String, Integer> inputAlphabet;
    private HashMap<String, Integer> outputAlphabet;
    private HashMap<String, Integer> copySet;
    private Node[] nodeFormula;
    private Node[][][] edgeFormula;
    private String[][] inputEdgeSet;
    private Boolean[][] outputNodeSet;

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
        //initialise graph represetation
        char [] stringArray = inputString.toCharArray();
        inputEdgeSet = new String[copySet.size()*(inputString.length()+1)][copySet.size()*(inputString.length()+1)];
        outputNodeSet = new Boolean[copySet.size()][inputString.length()+1];
        String[][] outputEdgeSet = new String[copySet.size()*(inputString.length()+1)][copySet.size()*(inputString.length()+1)];
        //construct string representation and its copy
        for (int i = 0; i < stringArray.length; i++) {
            String symbol = String.valueOf(stringArray[i]);
            for (int j = 0; j < copySet.size(); j++) {
                int curr = i+j*(inputString.length()+1);
                inputEdgeSet[curr][curr+1] = symbol;
            }
        }

        //use node formulas to construct new gragh
        for (int i = 0; i < nodeFormula.length; i++) {
            if (nodeFormula[i] == null) {
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
        for (int i = 0; i < edgeFormula.length; i++) {
            for (int j = 0; j < edgeFormula.length; j++) {
                for (String outputSymbol : outputAlphabet.keySet()) {
                    int outputNum = outputAlphabet.get(outputSymbol);
                    if (edgeFormula[i][j][outputNum] == null) {
                        continue;
                    } else {
                        Node root = edgeFormula[i][j][outputNum];
                        for (int k = 0; k < inputString.length()+1; k++) {
                            if (outputNodeSet[i][k] == true) {
                                for (int l = 0; l < inputString.length()+1; l++) {
                                    if (outputNodeSet[j][l] == true) {
                                        if (evaluateEdgeFormula(root,i*(inputString.length()+1)+k,j*(inputString.length()+1)+l)) {
                                            outputEdgeSet[i*(inputString.length()+1)+k][j*(inputString.length()+1)+l] = outputSymbol;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // loops to print outputEdgeSet (to debug)
        // System.out.println("Output edge matrix:");
        // for (int i = 0; i < outputEdgeSet.length; i++) {
        //     System.out.println(Arrays.toString(outputEdgeSet[i]));
        // }

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

        //check whether string representation is vaild
        Boolean vaild = true;
        for (int i = 0; i < copySet.size(); i++) {
            for (int j = 0; j < inputString.length()+1; j++) {
                if (outputNodeSet[i][j] == true) {
                    vaild = false;
                }
            }
        }

        //return output if vaild, inform error otherwise 
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
     * @param formula the root of the tree representation of the formula
     * @param vertexNum vertex number
     * @return true or false
     */
    private Boolean evaluateNodeFormula(Node formula, int vertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            //logic for and
            return evaluateNodeFormula(formula.getLeftChild(),vertexNum) && evaluateNodeFormula(formula.getRightChild(),vertexNum);
        } else if (data.equals("+")) {
            //logic for or
            return evaluateNodeFormula(formula.getLeftChild(),vertexNum) || evaluateNodeFormula(formula.getRightChild(),vertexNum);
        } else if (data.equals("!")) {
            //logic for not
            return !evaluateNodeFormula(formula.getLeftChild(),vertexNum);
        } else if (data.equals("true")) {
            //logic for true
            return true;
        } else if (data.equals("false")) {
            //logic for false
            return false;
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            //logic for formula out
            //return true if the vertex has an out-edge with related symbol
            if (vertexNum%outputNodeSet[0].length == outputNodeSet[0].length-1) {
                return false;
            }
            String symbol = data.substring(4, 5);
            if (inputEdgeSet[vertexNum][vertexNum+1] != null && inputEdgeSet[vertexNum][vertexNum+1].equals(symbol)) {
                return true;
            }
        } else if (data.matches("#.")) {
            //logic for exist
            for (int i = (vertexNum/outputNodeSet[0].length)*outputNodeSet[0].length; i < ((vertexNum/outputNodeSet[0].length)+1)*outputNodeSet[0].length; i++) {
                if (evaluateNodeFormulaBound(formula.getLeftChild(),vertexNum,data.substring(1, 2),i)) {
                    return true;
                }
            }
            return false;
        } else if (data.matches("$.")) {
            //logic for forall
            for (int i = (vertexNum/outputNodeSet[0].length)*outputNodeSet[0].length; i < ((vertexNum/outputNodeSet[0].length)+1)*outputNodeSet[0].length; i++) {
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
     * @param formula the root of the tree representation of the formula
     * @param freeVertexNum free vertex number
     * @param boundVar bound variable 
     * @param boundVertexNum bound vertex number
     * @return true or false
     */
    private Boolean evaluateNodeFormulaBound(Node formula, int freeVertexNum, String boundVar, int boundVertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            //logic for and
            return evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum) && evaluateNodeFormulaBound(formula.getRightChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.equals("+")) {
            //logic for or
            return evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum) || evaluateNodeFormulaBound(formula.getRightChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.equals("!")) {
            //logic for not
            return !evaluateNodeFormulaBound(formula.getLeftChild(),freeVertexNum,boundVar,boundVertexNum);
        } else if (data.equals("true")) {
            //logic for true
            return true;
        } else if (data.equals("false")) {
            //logic for false
            return false;
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            //logic for formula out
            //return true if the vertex has an out-edge with related symbol
            String var = data.substring(7, 8);
            if (var.equals(boundVar)) {
                if (boundVertexNum == inputEdgeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[boundVertexNum][boundVertexNum+1] != null && inputEdgeSet[boundVertexNum][boundVertexNum+1].equals(symbol)) {
                    return true;
                }
            } else {
                if (freeVertexNum == inputEdgeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[freeVertexNum][freeVertexNum+1] != null && inputEdgeSet[freeVertexNum][freeVertexNum+1].equals(symbol)) {
                    return true;
                }
            }
        } else if (data.matches(".<.")) {
            //logic for formal <
            //return true if there is a path from the first vertex to the second vertex
            if (data.substring(0, 1).equals(boundVar)) {
                if (data.substring(2).equals(boundVar)) {
                    return false;
                } else {
                    if (freeVertexNum > boundVertexNum) {
                        return true;
                    }
                }
            } else {
                if (data.substring(2).equals(boundVar)) {
                    if (freeVertexNum < boundVertexNum) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Evaluate whether Node Formula is true for input vertex
     * @param formula the root of the tree representation of the formula
     * @param vertex1Num the first vertex number
     * @param vertex1Num the second vertex number
     * @return true or false
     */
    private Boolean evaluateEdgeFormula(Node formula, int vertex1Num, int vertex2Num) {
        String data = formula.getData();
        if (data.equals("*")) {
            //logic for and
            return evaluateEdgeFormula(formula.getLeftChild(),vertex1Num,vertex2Num) && evaluateEdgeFormula(formula.getRightChild(),vertex1Num,vertex2Num);
        } else if (data.equals("+")) {
            //logic for or
            return evaluateEdgeFormula(formula.getLeftChild(),vertex1Num,vertex2Num) || evaluateEdgeFormula(formula.getRightChild(),vertex1Num,vertex2Num);
        } else if (data.equals("!")) {
            //logic for not
            return !evaluateEdgeFormula(formula.getLeftChild(),vertex1Num,vertex2Num);
        } else if (data.equals("true")) {
            //logic for true
            return true;
        } else if (data.equals("false")) {
            //logic for false
            return false;
        } else if (data.matches("edge\\{.\\}\\(.,.\\)")) {
            //logic for formula edge
            //return true if there is an edge with related symbol from the first vertex to the second vertex
            if (data.substring(8, 9).equals("x") && data.substring(10, 11).equals("y")) {
                if (vertex1Num+1 == vertex2Num && vertex1Num%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                    String symbol = data.substring(5, 6);
                    if (inputEdgeSet[vertex1Num][vertex2Num] != null && inputEdgeSet[vertex1Num][vertex2Num].equals(symbol)) {
                        return true;
                    }
                }
            } else if (data.substring(8, 9).equals("y") && data.substring(10, 11).equals("x")) {
                if (vertex2Num+1 == vertex1Num && vertex2Num%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                    String symbol = data.substring(5, 6);
                    if (inputEdgeSet[vertex2Num][vertex1Num] != null && inputEdgeSet[vertex2Num][vertex1Num].equals(symbol)) {
                        return true;
                    }
                }
            }
        } else if (data.matches("out\\{.\\}\\([xy]\\)")) {
            //logic for formula out
            //return true if the vertex has an out-edge with related symbol
            if (data.substring(7, 8).equals("x")) {
                if (vertex1Num%outputNodeSet[0].length == outputNodeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[vertex1Num][vertex1Num+1] != null && inputEdgeSet[vertex1Num][vertex1Num+1].equals(symbol)) {
                    return true;
                }
            } else if (data.substring(7, 8).equals("y")) {
                if (vertex2Num%outputNodeSet[0].length == outputNodeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[vertex2Num][vertex2Num+1] != null && inputEdgeSet[vertex2Num][vertex2Num+1].equals(symbol)) {
                    return true;
                }
            }
        } else if (data.matches("next\\{.\\}\\([xy],[xy]\\)")) {
            //logic for formula next
            //return true if the second vertex is the first vertx after the first vertx that has an out-edge with related symbol 
            String symbol = data.substring(5, 6);
            if (data.substring(8, 9).equals("x") && data.substring(10, 11).equals("y")) {
                int currCopyNum = vertex1Num/outputNodeSet[0].length;
                int currVertex = vertex1Num + 1;
                while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol) ) {
                    currVertex += 1;
                }
                if (vertex2Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                    return true;
                }
            } else if (data.substring(8, 9).equals("y") && data.substring(10, 11).equals("x")) {
                int currCopyNum = vertex2Num/outputNodeSet[0].length;
                int currVertex = vertex2Num + 1;
                while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol) ) {
                    currVertex += 1;
                }
                if (vertex1Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                    return true;
                }
            }
        } else if (data.matches("fps\\{.\\}\\([xy],[xy]\\)")) {
            //logic for formula fps
            //return true if the second vertex is the first vertx in the front of the first vertx that has an out-edge with related symbol 
            String symbol = data.substring(4, 5);
            if (data.substring(7, 8).equals("x") && data.substring(9, 10).equals("y")) {
                int currCopyNum = vertex1Num/outputNodeSet[0].length;
                int currVertex = vertex1Num;
                while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                    currVertex -= 1;
                }
                if (vertex2Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                    return true;
                }
            } else if (data.substring(7, 8).equals("y") && data.substring(9, 10).equals("x")) {
                int currCopyNum = vertex2Num/outputNodeSet[0].length;
                int currVertex = vertex2Num;
                while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                    currVertex -= 1;
                }
                if (vertex1Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                    return true;
                }
            }
        } else if (data.matches("F\\{.*,.*\\}\\{.\\}\\([xy],[xy]\\)")) {
            //logic for edge formulas
            //return true if the edge formula is true
            String[] formulaString = data.split(",|\\}\\{");
            int copy1Num = copySet.get(formulaString[0].substring(2));
            int copy2Num = copySet.get(formulaString[1]);
            int outputNum = outputAlphabet.get(formulaString[2].substring(0, 1));
            String var1 = formulaString[2].substring(3, 4);
            String var2 = formulaString[3].substring(0, 1);
            if (var1.equals("x") && var2.equals("y")) {
                if (vertex1Num/outputNodeSet[0].length == copy1Num && vertex2Num/outputNodeSet[0].length == copy2Num) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],vertex1Num,vertex2Num);
                }
            } else if (var1.equals("y") && var2.equals("x")) {
                if (vertex2Num/outputNodeSet[0].length == copy1Num && vertex1Num/outputNodeSet[0].length == copy2Num) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],vertex2Num,vertex1Num);
                }
            }
        } else if (data.matches("#.")) {
            //logic for exist
            for (int i = 0; i < inputEdgeSet.length; i++) {
                if (evaluateEdgeFormulaBound(formula.getLeftChild(),vertex1Num,vertex2Num,data.substring(1, 2),i)) {
                    return true;
                }
            }
            return false;
        } else if (data.matches("$.")) {
            //logic for forall
            for (int i = 0; i < inputEdgeSet.length; i++) {
                if (!evaluateEdgeFormulaBound(formula.getLeftChild(),vertex1Num,vertex2Num,data.substring(1, 2),i))    {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Evaluate whether Edge Formula is true for input bound vertex
     * @param formula the root of the tree representation of the formula
     * @param vertex1Num the first free vertex number
     * @param vertex2Num the second free vertex number
     * @param boundVar bound variable 
     * @param boundVertexNum bound vertex number
     * @return true or false
     */
    private Boolean evaluateEdgeFormulaBound(Node formula, int vertex1Num, int vertex2Num, String boundVar, int boundVertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            //logic for and
            return evaluateEdgeFormulaBound(formula.getLeftChild(),vertex1Num,vertex2Num,boundVar,boundVertexNum) && evaluateEdgeFormulaBound(formula.getRightChild(),vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.equals("+")) {
            //logic for or
            return evaluateEdgeFormulaBound(formula.getLeftChild(),vertex1Num,vertex2Num,boundVar,boundVertexNum) || evaluateEdgeFormulaBound(formula.getRightChild(),vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.equals("!")) {
            //logic for not
            return !evaluateEdgeFormulaBound(formula.getLeftChild(),vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.equals("true")) {
            //logic for true
            return true;
        } else if (data.equals("false")) {
            //logic for false
            return false;
        } else if (data.matches("edge\\{.\\}\\(.,.\\)")) {
            //logic for formula edge
            //return true if there is an edge with related symbol from the first vertex to the second vertex
            if (data.substring(8, 9).equals(boundVar)) {
                if (data.substring(10, 11).equals("x")) {
                    if (boundVertexNum+1 == vertex1Num && boundVertexNum%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[boundVertexNum][vertex1Num] != null && inputEdgeSet[boundVertexNum][vertex1Num].equals(symbol)) {
                            return true;
                        }
                    }                  
                } else if (data.substring(10, 11).equals("y")) {
                    if (boundVertexNum+1 == vertex2Num && boundVertexNum%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[boundVertexNum][vertex2Num] != null && inputEdgeSet[boundVertexNum][vertex2Num].equals(symbol)) {
                            return true;
                        }
                    }
                }
            } else if (data.substring(10, 11).equals(boundVar)) {
                if (data.substring(8, 9).equals("x")) {
                    if (vertex1Num+1 == boundVertexNum && vertex1Num%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[vertex1Num][boundVertexNum] != null && inputEdgeSet[vertex1Num][boundVertexNum].equals(symbol)) {
                            return true;
                        }
                    }                  
                } else if (data.substring(8, 9).equals("y")) {
                    if (vertex2Num+1 == boundVertexNum && vertex2Num%outputNodeSet[0].length != outputNodeSet[0].length-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[vertex2Num][boundVertexNum] != null && inputEdgeSet[vertex2Num][boundVertexNum].equals(symbol)) {
                            return true;
                        }
                    }   
                }
            }
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            //logic for formula out
            //return true if the vertex has an out-edge with related symbol
            if (data.substring(7, 8).equals(boundVar)) {
                if (boundVertexNum%outputNodeSet[0].length == outputNodeSet[0].length-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[boundVertexNum][boundVertexNum+1] != null && inputEdgeSet[boundVertexNum][boundVertexNum+1].equals(symbol)) {
                    return true;
                }
            }
        } else if (data.matches("next\\{.\\}\\(.,.\\)")) {
            //logic for formula next
            //return true if the second vertex is the first vertx after the first vertx that has an out-edge with related symbol
            String symbol = data.substring(5, 6);
            if (data.substring(8, 9).equals(boundVar)) {
                if (data.substring(10, 11).equals("x")) {
                    int currCopyNum = boundVertexNum/outputNodeSet[0].length;
                    int currVertex = boundVertexNum + 1;
                    while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (vertex1Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                } else if (data.substring(10, 11).equals("y")) {
                    int currCopyNum = boundVertexNum/outputNodeSet[0].length;
                    int currVertex = boundVertexNum + 1;
                    while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (vertex2Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                }
            } else if (data.substring(10, 11).equals(boundVar)) {
                if (data.substring(8, 9).equals("x")) {
                    int currCopyNum = vertex1Num/outputNodeSet[0].length;
                    int currVertex = vertex1Num + 1;
                    while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (boundVertexNum%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                } else if (data.substring(8, 9).equals("y")) {
                    int currCopyNum = vertex2Num/outputNodeSet[0].length;
                    int currVertex = vertex2Num + 1;
                    while (currVertex < (currCopyNum+1)*outputNodeSet[0].length-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (boundVertexNum%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                }
            }
        } else if (data.matches("fps\\{.\\}\\(.,.\\)")) {
            //logic for formula fps
            //return true if the second vertex is the first vertx in the front of the first vertx that has an out-edge with related symbol 
            String symbol = data.substring(4, 5);
            if (data.substring(7, 8).equals(boundVar)) {
                if (data.substring(9, 10).equals("x")) {
                    int currCopyNum = boundVertexNum/outputNodeSet[0].length;
                    int currVertex = boundVertexNum;
                    while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (vertex1Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                } else if (data.substring(9, 10).equals("y")) {
                    int currCopyNum = boundVertexNum/outputNodeSet[0].length;
                    int currVertex = boundVertexNum;
                    while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (vertex2Num%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                }
            } else if (data.substring(9, 10).equals(boundVar)) {
                if (data.substring(7, 8).equals("x")) {
                    int currCopyNum = vertex1Num/outputNodeSet[0].length;
                    int currVertex = vertex1Num;
                    while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (boundVertexNum%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                } else if (data.substring(7, 8).equals("y")) {
                    int currCopyNum = vertex2Num/outputNodeSet[0].length;
                    int currVertex = vertex2Num;
                    while (currVertex > currCopyNum*outputNodeSet[0].length && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (boundVertexNum%outputNodeSet[0].length == currVertex%outputNodeSet[0].length) {
                        return true;
                    }
                }
            }
        } else if (data.matches("F\\{.*,.*\\}\\{.\\}\\(.,.\\)")) {
            //logic for edge formulas
            //return true if the edge formula is true and the bound vertex is vaild in the output node set
            if (outputNodeSet[boundVertexNum/outputNodeSet[0].length][boundVertexNum%outputNodeSet[0].length] == false) {
                return false;
            }
            String[] formulaString = data.split(",|\\}\\{");
            int copy1Num = copySet.get(formulaString[0].substring(2));
            int copy2Num = copySet.get(formulaString[1]);
            int outputNum = outputAlphabet.get(formulaString[2].substring(0, 1));
            String var1 = formulaString[2].substring(3, 4);
            String var2 = formulaString[3].substring(0, 1);
            if (var1.equals(boundVar)) {
                if (var2.equals("x")) {
                    if (boundVertexNum/outputNodeSet[0].length == copy1Num && vertex1Num/outputNodeSet[0].length == copy2Num) {
                        return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],boundVertexNum,vertex1Num);
                    }
                } else if (var2.equals("y")) {
                    if (boundVertexNum/outputNodeSet[0].length == copy1Num && vertex2Num/outputNodeSet[0].length == copy2Num) {
                        return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],boundVertexNum,vertex2Num);
                    }
                }
            } else if (var2.equals(boundVar)) {
                if (var1.equals("x")) {
                    if (vertex1Num/outputNodeSet[0].length == copy1Num && boundVertexNum/outputNodeSet[0].length == copy2Num) {
                        return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],vertex1Num,boundVertexNum);
                    }
                } else if (var1.equals("y")) {
                    if (vertex2Num/outputNodeSet[0].length == copy1Num && boundVertexNum/outputNodeSet[0].length == copy2Num) {
                        return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],vertex2Num,boundVertexNum);
                    }
                }
            }
        }
        return false;
    }
}
