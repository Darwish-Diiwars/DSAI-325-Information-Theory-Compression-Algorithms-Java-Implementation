public class Decoder {
    private final HuffmanTree huffmanTree;

    public Decoder(HuffmanTree tree) {
        this.huffmanTree = tree;
    }

    public String decode(String encodedInput) {
        StringBuilder decodedOutput = new StringBuilder();
        int index = 0;

        while (index < encodedInput.length()) {
            HuffmanTree.DecodingResult result = huffmanTree.decodeNextSymbol(encodedInput, index);
            if (result == null) break;
            decodedOutput.append(result.getSymbol());
            index = result.getNextIndex();
            huffmanTree.update(result.getSymbol());
        }

        return decodedOutput.toString();
    }
}