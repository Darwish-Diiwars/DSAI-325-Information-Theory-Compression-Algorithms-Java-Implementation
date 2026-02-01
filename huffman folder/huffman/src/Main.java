import java.io.*;
import java.util.*;

public class Main {

    static class HuffmanNode implements Comparable<HuffmanNode> {
        char character;
        int frequency;
        HuffmanNode left, right;

        HuffmanNode(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }

        HuffmanNode(int frequency) {
            this.character = '\0';
            this.frequency = frequency;
        }

        public int compareTo(HuffmanNode node) {
            return Integer.compare(this.frequency, node.frequency);
        }
    }

    public static double calculateEntropy(Map<Character, Integer> frequencyMap, int totalCharacters) {
        double entropy = 0.0;
        for (var entry : frequencyMap.entrySet()) {
            double probability = (double) entry.getValue() / totalCharacters;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }

    public static HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (var entry : frequencyMap.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            pq.add(parent);
        }
        return pq.poll();
    }

    public static void generateHuffmanCodes(HuffmanNode root, String code, Map<Character, String> huffmanCodes) {
        if (root == null)
            return;
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.character, code);
            return;
        }
        generateHuffmanCodes(root.left, code + "0", huffmanCodes);
        generateHuffmanCodes(root.right, code + "1", huffmanCodes);
    }

    public static HuffmanNode rebuildHuffmanTree(Map<Character, String> huffmanCodes) {
        HuffmanNode root = new HuffmanNode('\0', 0);
        for (var entry : huffmanCodes.entrySet()) {
            char ch = entry.getKey();
            String code = entry.getValue();
            HuffmanNode current = root;
            for (char bit : code.toCharArray()) {
                if (bit == '0') {
                    if (current.left == null)
                        current.left = new HuffmanNode('\0', 0);
                    current = current.left;
                } else {
                    if (current.right == null)
                        current.right = new HuffmanNode('\0', 0);
                    current = current.right;
                }
            }
            current.character = ch;
        }
        return root;
    }

    public static void encode(String inputPath, String outputBinaryPath, String outputCodeTablePath)
            throws IOException {
        String inputData = readTextFile(inputPath);
        if (inputData.isEmpty())
            return;
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : inputData.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
        double entropy = calculateEntropy(frequencyMap, inputData.length());
        System.out.println("Entropy: " + entropy);
        HuffmanNode root = buildHuffmanTree(frequencyMap);
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateHuffmanCodes(root, "", huffmanCodes);
        try (FileWriter writer = new FileWriter(outputCodeTablePath)) {
            for (var entry : huffmanCodes.entrySet()) {
                String key = (entry.getKey() == '\n') ? "\\n" : String.valueOf(entry.getKey());
                writer.write(key + ":" + entry.getValue() + "\n");
            }
        }
        StringBuilder encodedData = new StringBuilder();
        for (char c : inputData.toCharArray()) {
            encodedData.append(huffmanCodes.get(c));
        }
        try (FileWriter writer = new FileWriter(outputBinaryPath)) {
            writer.write(encodedData.toString());
        }
        int originalSize = inputData.length() * 8;
        int compressedSize = encodedData.length();
        System.out.println("Original size: " + originalSize + " bits");
        System.out.println("Compressed size: " + compressedSize + " bits");
    }

    public static void decode(String inputBinaryPath, String codeTablePath, String outputPath) throws IOException {
        Map<Character, String> huffmanCodes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(codeTablePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty())
                    continue;
                String[] parts = line.split(":", 2);
                if (parts.length < 2)
                    continue;
                String keyPart = parts[0];
                char ch;
                if (keyPart.equals("\\n"))
                    ch = '\n';
                else if (keyPart.isEmpty())
                    ch = ' ';
                else
                    ch = keyPart.charAt(0);
                huffmanCodes.put(ch, parts[1]);
            }
        }
        HuffmanNode root = rebuildHuffmanTree(huffmanCodes);
        String encodedData = readTextFile(inputBinaryPath).trim();
        StringBuilder decodedText = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encodedData.toCharArray()) {
            current = (bit == '0') ? current.left : current.right;
            if (current.left == null && current.right == null) {
                decodedText.append(current.character);
                current = root;
            }
        }
        writeTextFile(outputPath, decodedText.toString());
    }

    public static String readTextFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    public static void writeTextFile(String path, String content) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Enter choice: encode (e), decode (d), or exit (x)");
            String answer = userInput.readLine().toLowerCase();
            if (answer.equals("x"))
                break;
            else if (answer.equals("e")) {
                System.out.println("Enter input text file path:");
                String inputFile = userInput.readLine();
                System.out.println("Enter output binary file path:");
                String outputBinaryFile = userInput.readLine();
                System.out.println("Enter output code table file path:");
                String outputCodeTableFile = userInput.readLine();
                encode(inputFile, outputBinaryFile, outputCodeTableFile);
            } else if (answer.equals("d")) {
                System.out.println("Enter input binary file path:");
                String inputBinaryFile = userInput.readLine();
                System.out.println("Enter input code table file path:");
                String codeTableFile = userInput.readLine();
                System.out.println("Enter output text file path:");
                String outputFile = userInput.readLine();
                decode(inputBinaryFile, codeTableFile, outputFile);
            } else {
                System.out.println("Invalid choice. Please enter e, d, or x.");
            }
        }
    }
}
