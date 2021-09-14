package simulator.util;

public class ParseTree {
    private Node root;

    public ParseTree(String input) {
        Node root = new Node(input);
        root.parse();
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }
}

/**
 * Tree Node
 */
class Node {
    private String data;
    private Node leftChild;
    private Node rightChild;

    public Node(String data, Node leftChild, Node rightChild){
        this.data=data;
        this.leftChild=leftChild;
        this.rightChild=rightChild;
    }

    public Node(String data){
        this(data,null,null);
    }

    public Node(String data, Node leftChild){
        this(data,leftChild,null);
    }

    public String getData() {
        return data;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }
    
    public void parse() {
        char[] formula = this.data.toCharArray();
        // number of encountered left parenthesis
        int parentheses = 0;
        for (int i = 0; i < formula.length; i++) {
            if (formula[i] == '(') {
                parentheses += 1;
            } else if (formula[i] == ')') {
                parentheses -= 1;
            } else if (parentheses == 0) {
                if (formula[i] == '*' || formula[i] == '+') {
                    char[] leftFormula = new char[i-2];
                    char[] rightFormula = new char[formula.length-i-3];
                    System.arraycopy(formula,1,leftFormula,0,i-2);
                    System.arraycopy(formula,i+2,rightFormula,0,formula.length-i-3);
                    Node left = new Node(String.valueOf(leftFormula));
                    Node right = new Node(String.valueOf(rightFormula));
                    this.data = String.valueOf(formula[i]);
                    this.leftChild = left;
                    this.rightChild = right;
                    leftChild.parse();
                    rightChild.parse();
                } else if (formula[i] == '!') {
                    Node left = new Node(this.data.substring(1));
                    this.data = "!";
                    this.leftChild = left;
                    leftChild.parse();
                } else if (formula[i] == '#' || formula[i] == '$') {
                    Node left = new Node(this.data.substring(3,this.data.length()-1));
                    this.data = this.data.substring(0,2);
                    this.leftChild = left;
                    leftChild.parse();
                }
            }
        }
    }

    public Boolean evaluate() {
        if (this.data.equals("*")) {
            return leftChild.evaluate() && rightChild.evaluate();
        } else if (this.data.equals("+")) {
            return leftChild.evaluate() || rightChild.evaluate();
        } else if (this.data.matches("out\\{.\\}\\(.\\)")) {

        } else if (this.data.matches("edge\\{.\\}\\(.,.\\)")) {

        } else if (this.data.matches("next\\{.\\}\\(.,.\\)")) {

        } else if (this.data.matches("fps\\{.\\}\\(.,.\\)")) {

        } else if (this.data.matches("ϕ\\{.*\\}\\(.\\)")) {

        } else if (this.data.matches("ϕ\\{.*,.*\\}\\{.\\}\\(.\\)")) {

        }
        return false;
    }
}