public class Node {
    private char symbol;
    private int weight;
    private int order;
    private Node parent;
    private Node leftChild;
    private Node rightChild;

    public Node(int order) {
        this.symbol = '\0';
        this.weight = 0;
        this.order = order;
    }

    public Node(char symbol, int weight, int order) {
        this.symbol = symbol;
        this.weight = weight;
        this.order = order;
    }

    public char getSymbol() { return symbol; }
    public int getWeight() { return weight; }
    public void incrementWeight() { this.weight++; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public Node getParent() { return parent; }
    public void setParent(Node parent) { this.parent = parent; }
    public Node getLeftChild() { return leftChild; }
    public Node getRightChild() { return rightChild; }

    public void setLeftChild(Node child) {
        this.leftChild = child;
        if (child != null) child.setParent(this);
    }

    public void setRightChild(Node child) {
        this.rightChild = child;
        if (child != null) child.setParent(this);
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null && symbol != '\0';
    }

    public boolean isNYT() {
        return leftChild == null && rightChild == null && weight == 0;
    }

    @Override
    public String toString() {
        if (isNYT()) return "NYT [#" + order + ", w=" + weight + "]";
        else if (isLeaf()) return "'" + symbol + "' [#" + order + ", w=" + weight + "]";
        else return "Node [#" + order + ", w=" + weight + "]";
    }
}