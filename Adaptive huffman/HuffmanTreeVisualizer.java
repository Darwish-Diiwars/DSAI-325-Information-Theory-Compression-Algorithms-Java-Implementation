import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class HuffmanTreeVisualizer extends JFrame {
    private HuffmanTree tree;
    private final JPanel treePanel;
    private final StringBuilder encodedBits;
    private final StringBuilder decodedText;
    private final JTextArea encodingOutput;

    public HuffmanTreeVisualizer(HuffmanTree tree) {
        this.tree = tree;
        this.encodedBits = new StringBuilder();
        this.decodedText = new StringBuilder();
        this.encodingOutput = new JTextArea(6, 40);
        encodingOutput.setEditable(false);

        setTitle("Adaptive Huffman Tree Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        treePanel = new TreePanel();
        add(new JScrollPane(treePanel), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        inputPanel.add(createEncodePanel());
        inputPanel.add(createDecodePanel());

        controlPanel.add(inputPanel, BorderLayout.NORTH);
        controlPanel.add(new JScrollPane(encodingOutput), BorderLayout.CENTER);
        controlPanel.add(createResetButton(), BorderLayout.SOUTH);

        return controlPanel;
    }

    private JPanel createEncodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField symbolInput = new JTextField(5);
        JButton updateButton = new JButton("Update Tree");
        JButton saveButton = new JButton("Save Tree Image");

        updateButton.addActionListener(e -> {
            String input = symbolInput.getText();
            if (!input.isEmpty()) {
                char symbol = input.charAt(0);
                String encoding = tree.getEncodingForSymbol(symbol);
                encodedBits.append(encoding);
                decodedText.append(symbol);
                tree.update(symbol);
                encodingOutput.setText("Encoded Bits: " + encodedBits.toString() + "\nDecoded Text: " + decodedText.toString());
                treePanel.repaint();
                symbolInput.setText("");
            }
        });

        saveButton.addActionListener(e -> saveTreeImage());

        panel.add(new JLabel("Enter Symbol:"));
        panel.add(symbolInput);
        panel.add(updateButton);
        panel.add(saveButton);
        return panel;
    }

    private JPanel createDecodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField decodedInput = new JTextField(20);
        JButton decodeButton = new JButton("Decode Binary");

        decodeButton.addActionListener(e -> {
            String input = decodedInput.getText().trim();
            if (!input.isEmpty()) {
                try {
                    HuffmanTree decodeTree = new HuffmanTree(); // Fresh tree for decoding
                    Decoder decoder = new Decoder(decodeTree);
                    String decodedResult = decoder.decode(input);
                    JOptionPane.showMessageDialog(this, "Decoded: " + decodedResult);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error decoding: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(new JLabel("Binary to Decode:"));
        panel.add(decodedInput);
        panel.add(decodeButton);
        return panel;
    }

    private JPanel createResetButton() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("Reset Tree");
        resetButton.addActionListener(e -> {
            tree = new HuffmanTree();
            encodedBits.setLength(0);
            decodedText.setLength(0);
            encodingOutput.setText("");
            treePanel.repaint();
        });
        panel.add(resetButton);
        return panel;
    }

    private void saveTreeImage() {
        try {
            BufferedImage image = new BufferedImage(
                treePanel.getWidth(), treePanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            treePanel.paint(g2d);
            g2d.dispose();
            ImageIO.write(image, "png", new File("huffman_tree.png"));
            JOptionPane.showMessageDialog(this, "Tree image saved to huffman_tree.png");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class TreePanel extends JPanel {
        private Map<Node, Point> nodePositions;
        private int treeWidth;
        private int treeHeight;
        private final int nodeWidth = 60;
        private final int nodeHeight = 40;
        private final int horizontalGap = 20;
        private final int verticalGap = 60;

        public TreePanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tree.getRoot() != null) {
                nodePositions = new HashMap<>();
                treeWidth = 0;
                treeHeight = 0;
                calculateNodePositions(tree.getRoot(), 0, 0);
                setPreferredSize(new Dimension(Math.max(800, treeWidth + 100), Math.max(600, treeHeight + 100)));
                revalidate();
                drawConnections(g2d, tree.getRoot());
                drawNodes(g2d, tree.getRoot());
            }
        }

        private int calculateNodePositions(Node node, int depth, int xOffset) {
            if (node == null) return 0;

            int y = depth * (nodeHeight + verticalGap);
            treeHeight = Math.max(treeHeight, y + nodeHeight);

            if (node.isLeaf() || node.isNYT()) {
                nodePositions.put(node, new Point(xOffset + nodeWidth / 2, y));
                treeWidth = Math.max(treeWidth, xOffset + nodeWidth + horizontalGap);
                return nodeWidth + horizontalGap;
            }

            int leftWidth = calculateNodePositions(node.getLeftChild(), depth + 1, xOffset);
            int rightWidth = calculateNodePositions(node.getRightChild(), depth + 1, xOffset + leftWidth);
            int x = xOffset + (leftWidth + rightWidth) / 2 - nodeWidth / 2;
            nodePositions.put(node, new Point(x, y));
            treeWidth = Math.max(treeWidth, xOffset + leftWidth + rightWidth);
            return leftWidth + rightWidth;
        }

        private void drawNodes(Graphics2D g2d, Node node) {
            if (node == null || !nodePositions.containsKey(node)) return;

            Point pos = nodePositions.get(node);
            g2d.setColor(node.isNYT() ? Color.YELLOW : node.isLeaf() ? Color.LIGHT_GRAY : Color.WHITE);
            g2d.fillRect(pos.x, pos.y, nodeWidth, nodeHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(pos.x, pos.y, nodeWidth, nodeHeight);

            String nodeText = node.isNYT() ? "NYT" : node.isLeaf() ? String.valueOf(node.getSymbol()) : "";
            String weightText = String.valueOf(node.getWeight());
            String orderText = "#" + node.getOrder();

            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(nodeText, pos.x + (nodeWidth - fm.stringWidth(nodeText)) / 2,
                    pos.y + nodeHeight / 2 + fm.getAscent() / 2);
            g2d.drawString(weightText, pos.x + 5, pos.y + 15);
            g2d.drawString(orderText, pos.x + nodeWidth - fm.stringWidth(orderText) - 5, pos.y + 15);

            drawNodes(g2d, node.getLeftChild());
            drawNodes(g2d, node.getRightChild());
        }

        private void drawConnections(Graphics2D g2d, Node node) {
            if (node == null) return;

            if (node.getLeftChild() != null && nodePositions.containsKey(node.getLeftChild())) {
                Point p1 = nodePositions.get(node);
                Point p2 = nodePositions.get(node.getLeftChild());
                g2d.setColor(Color.BLACK);
                g2d.drawLine(p1.x + nodeWidth / 2, p1.y + nodeHeight, p2.x + nodeWidth / 2, p2.y);
                g2d.setColor(Color.BLUE);
                g2d.drawString("0", (p1.x + p2.x + nodeWidth) / 2 - 15, (p1.y + nodeHeight + p2.y) / 2);
            }

            if (node.getRightChild() != null && nodePositions.containsKey(node.getRightChild())) {
                Point p1 = nodePositions.get(node);
                Point p2 = nodePositions.get(node.getRightChild());
                g2d.setColor(Color.BLACK);
                g2d.drawLine(p1.x + nodeWidth / 2, p1.y + nodeHeight, p2.x + nodeWidth / 2, p2.y);
                g2d.setColor(Color.RED);
                g2d.drawString("1", (p1.x + p2.x + nodeWidth) / 2 + 5, (p1.y + nodeHeight + p2.y) / 2);
            }

            drawConnections(g2d, node.getLeftChild());
            drawConnections(g2d, node.getRightChild());
        }
    }
}