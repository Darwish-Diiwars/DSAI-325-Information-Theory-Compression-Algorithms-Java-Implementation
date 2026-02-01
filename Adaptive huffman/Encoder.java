public class Encoder {
    private final HuffmanTree huffmanTree;

    public Encoder(HuffmanTree tree) {
        this.huffmanTree = tree;
    }

    public String encode(String input) {
        StringBuilder encodedOutput = new StringBuilder();

        for (char symbol : input.toCharArray()) {
            String encodedSymbol = huffmanTree.getEncodingForSymbol(symbol);
            encodedOutput.append(encodedSymbol);
            huffmanTree.update(symbol);
        }

        return encodedOutput.toString();
    }
}