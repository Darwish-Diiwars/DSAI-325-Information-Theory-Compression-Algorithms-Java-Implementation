import java.io.*;
import java.nio.file.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.HashMap;

class AdaptiveHuffman {
    public static void main(String[] args) {
        try {
            // 1. Read input from input.txt
            String input = new String(Files.readAllBytes(Paths.get("input.txt")), "UTF-8").trim();
            System.out.println("Input: " + input);

            // 2. Encoding
            HuffmanTree tree = new HuffmanTree();
            Encoder encoder = new Encoder(tree);
            String encoded = encoder.encode(input);
            Files.write(Paths.get("output.bin"), encoded.getBytes());
            System.out.println("Encoded: " + encoded);

            // 3. Decoding using a new tree
            HuffmanTree decodeTree = new HuffmanTree();
            Decoder decoder = new Decoder(decodeTree);
            String decoded = decoder.decode(encoded);
            Files.write(Paths.get("decoded.txt"), decoded.getBytes("UTF-8"));
            System.out.println("Decoded: " + decoded);

            // 4. Verification
            System.out.println("Success: " + input.equals(decoded));

            // 5. Tree Visualization
            saveCompleteTreeVisualization(decodeTree, "huffman_tree.png");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveCompleteTreeVisualization(HuffmanTree tree, String filename) {
        try {
            JFrame frame = new JFrame();
            CompleteTreePanel panel = new CompleteTreePanel(tree);
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);

            BufferedImage image = new BufferedImage(
                panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            panel.printAll(g2d);
            g2d.dispose();

            ImageIO.write(image, "png", new File(filename));
            frame.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class CompleteTreePanel extends JPanel {
        private final HuffmanTree tree;
        private final Map<Node, Rectangle> nodePositions = new HashMap<>();
        private final int nodeWidth = 80;
        private final int nodeHeight = 50;
        private final int verticalGap = 80;

        public CompleteTreePanel(HuffmanTree tree) {
            this.tree = tree;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(1200, 800));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tree.getRoot() != null) {
                nodePositions.clear();
                calculatePositions(tree.getRoot(), getWidth() / 2, 30, getWidth() / 4);
                drawTree(g2d);
            }
        }

        private void calculatePositions(Node node, int x, int y, int xOffset) {
            if (node == null) return;

            nodePositions.put(node, new Rectangle(x - nodeWidth / 2, y, nodeWidth, nodeHeight));

            if (node.getLeftChild() != null) {
                calculatePositions(node.getLeftChild(), x - xOffset, y + verticalGap, xOffset / 2);
            }
            if (node.getRightChild() != null) {
                calculatePositions(node.getRightChild(), x + xOffset, y + verticalGap, xOffset / 2);
            }
        }

        private void drawTree(Graphics2D g2d) {
            for (Node node : nodePositions.keySet()) {
                if (node.getLeftChild() != null && nodePositions.containsKey(node.getLeftChild())) {
                    drawConnection(g2d, node, node.getLeftChild(), "0", Color.BLUE);
                }
                if (node.getRightChild() != null && nodePositions.containsKey(node.getRightChild())) {
                    drawConnection(g2d, node, node.getRightChild(), "1", Color.RED);
                }
            }

            for (Map.Entry<Node, Rectangle> entry : nodePositions.entrySet()) {
                drawNode(g2d, entry.getKey(), entry.getValue());
            }
        }

        private void drawConnection(Graphics2D g2d, Node parent, Node child, String label, Color color) {
            Rectangle parentRect = nodePositions.get(parent);
            Rectangle childRect = nodePositions.get(child);

            int x1 = parentRect.x + parentRect.width / 2;
            int y1 = parentRect.y + parentRect.height;
            int x2 = childRect.x + childRect.width / 2;
            int y2 = childRect.y;

            g2d.setColor(Color.BLACK);
            g2d.drawLine(x1, y1, x2, y2);

            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;
            g2d.setColor(color);
            g2d.drawString(label, midX, midY);
        }

        private void drawNode(Graphics2D g2d, Node node, Rectangle rect) {
            Color bgColor = node.isNYT() ? Color.YELLOW :
                            node.isLeaf() ? new Color(200, 255, 200) :
                                            new Color(200, 200, 255);
            g2d.setColor(bgColor);
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

            String symbol = node.isNYT() ? "NYT" :
                            node.isLeaf() ? "'" + node.getSymbol() + "'" : "";
            String weight = "W:" + node.getWeight();
            String order = "O:" + node.getOrder();

            FontMetrics fm = g2d.getFontMetrics();
            int centerX = rect.x + rect.width / 2;

            g2d.drawString(symbol, centerX - fm.stringWidth(symbol) / 2, rect.y + 20);

            g2d.setFont(g2d.getFont().deriveFont(10f));
            fm = g2d.getFontMetrics();
            g2d.drawString(weight, rect.x + 5, rect.y + rect.height - 10);
            g2d.drawString(order, rect.x + rect.width - fm.stringWidth(order) - 5, rect.y + rect.height - 10);
        }
    }
}
