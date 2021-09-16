package simulator.transducer;
import simulator.util.Node;

import java.util.HashMap;

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
            if (nodeFormula[i] == null) {
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
                    if (edgeFormula[i][j][outputNum] == null) {
                        continue;
                    } else {
                        Node root = edgeFormula[i][j][outputNum];
                        for (int k = 0; k < inputString.length()+1; k++) {
                            for (int l = 0; l < inputString.length()+1; l++) {
                                if (evaluateEdgeFormula(root,inputString.length()+1,i*(inputString.length()+1)+k,j*(inputString.length()+1)+l)) {
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
     * @param formula the root of the tree representation of the formula
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
            if (vertexNum%inputLength == inputLength-1) {
                return false;
            }
            String symbol = data.substring(4, 5);
            if (inputEdgeSet[vertexNum][vertexNum+1] != null && inputEdgeSet[vertexNum][vertexNum+1].equals(symbol)) {
                return true;
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
     * @param formula the root of the tree representation of the formula
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
     * @param inputLength vertex number in the same copy set = input string length + 1
     * @param vertex1Num the first vertex number
     * @param vertex1Num the second vertex number
     * @return true or false
     */
    private Boolean evaluateEdgeFormula(Node formula, int inputLength, int vertex1Num, int vertex2Num) {
        String data = formula.getData();
        if (data.equals("*")) {
            return evaluateEdgeFormula(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num) && evaluateEdgeFormula(formula.getRightChild(),inputLength,vertex1Num,vertex2Num);
        } else if (data.equals("+")) {
            return evaluateEdgeFormula(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num) || evaluateEdgeFormula(formula.getRightChild(),inputLength,vertex1Num,vertex2Num);
        } else if (data.equals("!")) {
            return !evaluateEdgeFormula(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num);
        } else if (data.matches("edge\\{.\\}\\([xy],[xy]\\)")) {
            if (data.substring(8, 9).equals("x") && data.substring(10, 11).equals("y")) {
                if (vertex1Num+1 == vertex2Num && vertex1Num%inputLength != inputLength-1) {
                    String symbol = data.substring(5, 6);
                    if (inputEdgeSet[vertex1Num][vertex2Num] != null && inputEdgeSet[vertex1Num][vertex2Num].equals(symbol)) {
                        return true;
                    }
                }
            } else if (data.substring(8, 9).equals("y") && data.substring(10, 11).equals("x")) {
                if (vertex2Num+1 == vertex1Num && vertex2Num%inputLength != inputLength-1) {
                    String symbol = data.substring(5, 6);
                    if (inputEdgeSet[vertex2Num][vertex1Num] != null && inputEdgeSet[vertex2Num][vertex1Num].equals(symbol)) {
                        return true;
                    }
                }
            }
        } else if (data.matches("out\\{.\\}\\([xy]\\)")) {
            if (data.substring(7, 8).equals("x")) {
                if (vertex1Num%inputLength == inputLength-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[vertex1Num][vertex1Num+1] != null && inputEdgeSet[vertex1Num][vertex1Num+1].equals(symbol)) {
                    return true;
                }
            } else if (data.substring(7, 8).equals("y")) {
                if (vertex2Num%inputLength == inputLength-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[vertex2Num][vertex2Num+1] != null && inputEdgeSet[vertex2Num][vertex2Num+1].equals(symbol)) {
                    return true;
                }
            }
        } else if (data.matches("next\\{.\\}\\([xy],[xy]\\)")) {
            String symbol = data.substring(5, 6);
            if (data.substring(8, 9).equals("x") && data.substring(10, 11).equals("y")) {
                int currCopyNum = vertex1Num/inputLength;
                int currVertex = vertex1Num;
                while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol) ) {
                    currVertex += 1;
                }
                if (vertex2Num%inputLength == currVertex%inputLength) {
                    return true;
                }
            } else if (data.substring(8, 9).equals("y") && data.substring(10, 11).equals("x")) {
                int currCopyNum = vertex2Num/inputLength;
                int currVertex = vertex2Num;
                while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol) ) {
                    currVertex += 1;
                }
                if (vertex1Num%inputLength == currVertex%inputLength) {
                    return true;
                }
            }
        } else if (data.matches("fps\\{.\\}\\([xy],[xy]\\)")) {
            String symbol = data.substring(4, 5);
            if (data.substring(7, 8).equals("x") && data.substring(9, 10).equals("y")) {
                int currCopyNum = vertex1Num/inputLength;
                int currVertex = vertex1Num;
                while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                    currVertex -= 1;
                }
                if (vertex2Num%inputLength == currVertex%inputLength) {
                    return true;
                }
            } else if (data.substring(7, 8).equals("y") && data.substring(9, 10).equals("x")) {
                int currCopyNum = vertex2Num/inputLength;
                int currVertex = vertex2Num;
                while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                    currVertex -= 1;
                }
                if (vertex1Num%inputLength == currVertex%inputLength) {
                    return true;
                }
            }
        } else if (data.matches("F\\{.*,.*\\}\\{.\\}\\([xy],[xy]\\)")) {
            String[] formulaString = data.split(",|\\}\\{");
            int copy1Num = copySet.get(formulaString[0].substring(2));
            int copy2Num = copySet.get(formulaString[1]);
            int outputNum = outputAlphabet.get(formulaString[2].substring(0, 1));
            String var1 = formulaString[2].substring(3, 4);
            String var2 = formulaString[3].substring(0, 1);
            if (var1.equals("x") && var2.equals("y")) {
                return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,vertex1Num,vertex2Num);
            } else if (var1.equals("y") && var2.equals("x")) {
                return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,vertex2Num,vertex1Num);
            }
        } else if (data.matches("#.")) {
            for (int i = 0; i < edgeFormula.length; i++) {
                if (evaluateEdgeFormulaBound(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num,data.substring(1, 2),i))    {
                    return true;
                }
            }
            return false;
        } else if (data.matches("$.")) {
            for (int i = 0; i < edgeFormula.length; i++) {
                if (!evaluateEdgeFormulaBound(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num,data.substring(1, 2),i))    {
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
    private Boolean evaluateEdgeFormulaBound(Node formula, int inputLength, int vertex1Num, int vertex2Num, String boundVar, int boundVertexNum) {
        String data = formula.getData();
        if (data.equals("*")) {
            return evaluateEdgeFormulaBound(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num,boundVar,boundVertexNum) && evaluateEdgeFormulaBound(formula.getRightChild(),inputLength,vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.equals("+")) {
            return evaluateEdgeFormulaBound(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num,boundVar,boundVertexNum) || evaluateEdgeFormulaBound(formula.getRightChild(),inputLength,vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.equals("!")) {
            return !evaluateEdgeFormulaBound(formula.getLeftChild(),inputLength,vertex1Num,vertex2Num,boundVar,boundVertexNum);
        } else if (data.matches("edge\\{.\\}\\(.,.\\)")) {
            if (data.substring(8, 9).equals(boundVar)) {
                if (data.substring(10, 11).equals("x")) {
                    if (boundVertexNum+1 == vertex1Num && boundVertexNum%inputLength != inputLength-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[boundVertexNum][vertex1Num] != null && inputEdgeSet[boundVertexNum][vertex1Num].equals(symbol)) {
                            return true;
                        }
                    }                  
                } else if (data.substring(10, 11).equals("y")) {
                    if (boundVertexNum+1 == vertex2Num && boundVertexNum%inputLength != inputLength-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[boundVertexNum][vertex2Num] != null && inputEdgeSet[boundVertexNum][vertex2Num].equals(symbol)) {
                            return true;
                        }
                    }
                }
            } else if (data.substring(10, 11).equals(boundVar)) {
                if (data.substring(8, 9).equals("x")) {
                    if (vertex1Num+1 == boundVertexNum && vertex1Num%inputLength != inputLength-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[vertex1Num][boundVertexNum] != null && inputEdgeSet[vertex1Num][boundVertexNum].equals(symbol)) {
                            return true;
                        }
                    }                  
                } else if (data.substring(8, 9).equals("y")) {
                    if (vertex2Num+1 == boundVertexNum && vertex2Num%inputLength != inputLength-1) {
                        String symbol = data.substring(5, 6);
                        if (inputEdgeSet[vertex2Num][boundVertexNum] != null && inputEdgeSet[vertex2Num][boundVertexNum].equals(symbol)) {
                            return true;
                        }
                    }   
                }
            }
        } else if (data.matches("out\\{.\\}\\(.\\)")) {
            if (data.substring(7, 8).equals(boundVar)) {
                if (boundVertexNum%inputLength == inputLength-1) {
                    return false;
                }
                String symbol = data.substring(4, 5);
                if (inputEdgeSet[boundVertexNum][boundVertexNum+1] != null && inputEdgeSet[boundVertexNum][boundVertexNum+1].equals(symbol)) {
                    return true;
                }
            }
        } else if (data.matches("next\\{.\\}\\(.,.\\)")) {
            String symbol = data.substring(5, 6);
            if (data.substring(8, 9).equals(boundVar)) {
                if (data.substring(10, 11).equals("x")) {
                    int currCopyNum = boundVertexNum/inputLength;
                    int currVertex = boundVertexNum;
                    while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (vertex1Num%inputLength == currVertex%inputLength) {
                        return true;
                    }
                } else if (data.substring(10, 11).equals("y")) {
                    int currCopyNum = boundVertexNum/inputLength;
                    int currVertex = boundVertexNum;
                    while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (vertex2Num%inputLength == currVertex%inputLength) {
                        return true;
                    }
                }
            } else if (data.substring(10, 11).equals(boundVar)) {
                if (data.substring(8, 9).equals("x")) {
                    int currCopyNum = vertex1Num/inputLength;
                    int currVertex = vertex1Num;
                    while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (boundVertexNum%inputLength == currVertex%inputLength) {
                        return true;
                    }
                } else if (data.substring(8, 9).equals("y")) {
                    int currCopyNum = vertex2Num/inputLength;
                    int currVertex = vertex2Num;
                    while (currVertex < (currCopyNum+1)*inputLength-1 && !inputEdgeSet[currVertex][currVertex+1].equals(symbol)) {
                        currVertex += 1;
                    }
                    if (boundVertexNum%inputLength == currVertex%inputLength) {
                        return true;
                    }
                }
            }
        } else if (data.matches("fps\\{.\\}\\(.,.\\)")) {
            String symbol = data.substring(4, 5);
            if (data.substring(7, 8).equals(boundVar)) {
                if (data.substring(9, 10).equals("x")) {
                    int currCopyNum = boundVertexNum/inputLength;
                    int currVertex = boundVertexNum;
                    while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (vertex1Num%inputLength == currVertex%inputLength) {
                        return true;
                    }
                } else if (data.substring(9, 10).equals("y")) {
                    int currCopyNum = boundVertexNum/inputLength;
                    int currVertex = boundVertexNum;
                    while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (vertex2Num%inputLength == currVertex%inputLength) {
                        return true;
                    }
                }
            } else if (data.substring(9, 10).equals(boundVar)) {
                if (data.substring(7, 8).equals("x")) {
                    int currCopyNum = vertex1Num/inputLength;
                    int currVertex = vertex1Num;
                    while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (boundVertexNum%inputLength == currVertex%inputLength) {
                        return true;
                    }
                } else if (data.substring(7, 8).equals("y")) {
                    int currCopyNum = vertex2Num/inputLength;
                    int currVertex = vertex2Num;
                    while (currVertex > currCopyNum*inputLength && inputEdgeSet[currVertex-1][currVertex].equals(symbol) ) {
                        currVertex -= 1;
                    }
                    if (boundVertexNum%inputLength == currVertex%inputLength) {
                        return true;
                    }
                }
            }
        } else if (data.matches("F\\{.*,.*\\}\\{.\\}\\(.,.\\)")) {
            String[] formulaString = data.split(",|\\}\\{");
            int copy1Num = copySet.get(formulaString[0].substring(2));
            int copy2Num = copySet.get(formulaString[1]);
            int outputNum = outputAlphabet.get(formulaString[2].substring(0, 1));
            String var1 = formulaString[2].substring(3, 4);
            String var2 = formulaString[3].substring(0, 1);
            if (var1.equals(boundVar)) {
                if (var2.equals("x")) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,boundVertexNum,vertex1Num);
                } else if (var2.equals("y")) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,boundVertexNum,vertex2Num);
                }
            } else if (var2.equals(boundVar)) {
                if (var1.equals("x")) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,vertex1Num,boundVertexNum);
                } else if (var1.equals("y")) {
                    return evaluateEdgeFormula(edgeFormula[copy1Num][copy2Num][outputNum],inputLength,vertex2Num,boundVertexNum);
                }
            }
        }
        return false;
    }
}
