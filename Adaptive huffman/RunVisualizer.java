import javax.swing.*;

 class RunVisualizer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HuffmanTree tree = new HuffmanTree();
            new HuffmanTreeVisualizer(tree);
        });
    }
}