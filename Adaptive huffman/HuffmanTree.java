import java.util.*;

public class HuffmanTree {
    private Node root;
    private Node nytNode;
    private Map<Character, Node> symbolTable;
    private int nextOrder;

    public HuffmanTree() {
        nextOrder = 512;
        nytNode = new Node(nextOrder--);
        root = nytNode;
        symbolTable = new HashMap<>();
    }

    public void update(char symbol) {
        if (symbolTable.containsKey(symbol)) {
            updateExistingSymbol(symbolTable.get(symbol));
        } else {
            addNewSymbol(symbol);
        }
    }

    private void addNewSymbol(char symbol) {
        Node symbolNode = new Node(symbol, 1, nextOrder--);
        Node internalNode = new Node(nextOrder--);
        Node newNYT = new Node(nextOrder--);

        internalNode.setLeftChild(newNYT);
        internalNode.setRightChild(symbolNode);

        if (root == nytNode) {
            root = internalNode;
        } else {
            Node parent = nytNode.getParent();
            if (parent.getLeftChild() == nytNode) {
                parent.setLeftChild(internalNode);
            } else {
                parent.setRightChild(internalNode);
            }
        }

        nytNode = newNYT;
        symbolTable.put(symbol, symbolNode);

        Node current = internalNode;
        while (current != null) {
            current.incrementWeight();
            current = current.getParent();
        }
    }

    private void updateExistingSymbol(Node leafNode) {
        Node current = leafNode;
        while (current != null) {
            Node blockLeader = findHighestNodeInBlock(current.getWeight());
            if (blockLeader != null && blockLeader != current && 
                blockLeader.getOrder() > current.getOrder() &&
                !isAncestor(blockLeader, current) && !isAncestor(current, blockLeader)) {
                swapNodes(current, blockLeader);
            }
            current.incrementWeight();
            current = current.getParent();
        }
    }

    private Node findHighestNodeInBlock(int weight) {
        Node highest = null;
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.getWeight() == weight && 
                (highest == null || current.getOrder() > highest.getOrder())) {
                highest = current;
            }
            if (current.getLeftChild() != null) queue.add(current.getLeftChild());
            if (current.getRightChild() != null) queue.add(current.getRightChild());
        }
        return highest;
    }

    private boolean isAncestor(Node potential, Node descendant) {
        Node current = descendant;
        while (current != null) {
            if (current == potential) return true;
            current = current.getParent();
        }
        return false;
    }

    private void swapNodes(Node node1, Node node2) {
        Node parent1 = node1.getParent();
        Node parent2 = node2.getParent();
        boolean node1IsLeftChild = parent1 != null && parent1.getLeftChild() == node1;
        boolean node2IsLeftChild = parent2 != null && parent2.getLeftChild() == node2;

        if (parent1 != null) {
            if (node1IsLeftChild) parent1.setLeftChild(node2);
            else parent1.setRightChild(node2);
        }
        if (parent2 != null) {
            if (node2IsLeftChild) parent2.setLeftChild(node1);
            else parent2.setRightChild(node1);
        }

        int tempOrder = node1.getOrder();
        node1.setOrder(node2.getOrder());
        node2.setOrder(tempOrder);

        if (node1 == root) root = node2;
        else if (node2 == root) root = node1;
    }

    public String getEncodingForSymbol(char symbol) {
        Node node = symbolTable.get(symbol);
        if (node == null) {
            return getPathToNode(nytNode) + String.format("%8s", Integer.toBinaryString(symbol)).replace(' ', '0');
        }
        return getPathToNode(node);
    }

    private String getPathToNode(Node node) {
        StringBuilder path = new StringBuilder();
        Node current = node;
        while (current != root) {
            Node parent = current.getParent();
            path.insert(0, parent.getLeftChild() == current ? "0" : "1");
            current = parent;
        }
        return path.toString();
    }

    public DecodingResult decodeNextSymbol(String bits, int startIndex) {
        Node current = root;
        int i = startIndex;

        while (i < bits.length()) {
            if (current.isLeaf()) {
                return new DecodingResult(current.getSymbol(), i);
            }
            if (current.isNYT() && i + 8 <= bits.length()) {
                String asciiStr = bits.substring(i, i + 8);
                char symbol = (char) Integer.parseInt(asciiStr, 2);
                return new DecodingResult(symbol, i + 8);
            }
            char bit = bits.charAt(i++);
            current = bit == '0' ? current.getLeftChild() : current.getRightChild();
            if (current == null) return null;
        }

        if (current.isLeaf()) return new DecodingResult(current.getSymbol(), i);
        else if (current.isNYT() && i + 8 <= bits.length()) {
            String asciiStr = bits.substring(i, i + 8);
            char symbol = (char) Integer.parseInt(asciiStr, 2);
            return new DecodingResult(symbol, i + 8);
        }
        return null;
    }

    public static class DecodingResult {
        private final char symbol;
        private final int nextIndex;

        public DecodingResult(char symbol, int nextIndex) {
            this.symbol = symbol;
            this.nextIndex = nextIndex;
        }

        public char getSymbol() { return symbol; }
        public int getNextIndex() { return nextIndex; }
    }

    public Node getRoot() { return root; }
    public Node getNYTNode() { return nytNode; }
}