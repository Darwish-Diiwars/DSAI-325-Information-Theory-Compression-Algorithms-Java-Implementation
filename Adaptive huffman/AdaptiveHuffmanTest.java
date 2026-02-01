import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AdaptiveHuffmanTest {

    public static void main(String[] args) {
        try {
            testBasicEncodingDecoding();
            testRepeatedSymbols();
            testLongerText();
            System.out.println("All tests passed successfully!");
        } catch (AssertionError e) {
            String errorMsg = "Test failed: " + e.getMessage();
            System.err.println(errorMsg);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void testBasicEncodingDecoding() throws IOException {
        StringBuilder results = new StringBuilder();
        results.append("Test 1: Basic Encoding and Decoding\n");
        String[] testStrings = {"a", "ab", "abc", "hello"};

        for (String test : testStrings) {
            HuffmanTree encodeTree = new HuffmanTree();
            Encoder encoder = new Encoder(encodeTree);
            String encoded = encoder.encode(test);

            HuffmanTree decodeTree = new HuffmanTree();
            Decoder decoder = new Decoder(decodeTree);
            String decoded = decoder.decode(encoded);

            assertEquals(test, decoded, "Basic test failed for '" + test + "'\nEncoded: " + encoded + "\nDecoded: '" + decoded + "'");

            double originalBits = test.length() * 8;
            double compressedBits = encoded.length();
            double ratio = compressedBits > 0 ? originalBits / compressedBits : 0;

            results.append("Passed: '").append(test).append("'\n");
            results.append("    Encoded: ").append(encoded).append("\n");
            results.append("    Decoded: ").append(decoded).append("\n");
            results.append(String.format("    Compression ratio: %.2f:1 (Original: %.0f bits, Compressed: %.0f bits)\n", ratio, originalBits, compressedBits));
        }

        writeToFile("Test1_BasicEncodingDecoding.txt", results.toString());
        System.out.println("Test 1 completed. Results in Test1_BasicEncodingDecoding.txt");
    }

    private static void testRepeatedSymbols() throws IOException {
        StringBuilder results = new StringBuilder();
        results.append("Test 2: Repeated Symbols\n");
        String test = "aaaabbb";

        HuffmanTree encodeTree = new HuffmanTree();
        Encoder encoder = new Encoder(encodeTree);
        String encoded = encoder.encode(test);

        HuffmanTree decodeTree = new HuffmanTree();
        Decoder decoder = new Decoder(decodeTree);
        String decoded = decoder.decode(encoded);

        assertEquals(test, decoded, "Repeated symbols test failed\nEncoded: " + encoded + "\nDecoded: '" + decoded + "'");

        Node aNode = encodeTree.getRoot().getRightChild(); 
        assertEquals(4, aNode.getWeight(), "Weight of 'a' should be 4");

        double originalBits = test.length() * 8;
        double compressedBits = encoded.length();
        double ratio = compressedBits > 0 ? originalBits / compressedBits : 0;

        results.append("Passed: Repeated symbols '").append(test).append("'\n");
        results.append("    Encoded: ").append(encoded).append("\n");
        results.append("    Decoded: ").append(decoded).append("\n");
        results.append("    Weight of 'a': ").append(aNode.getWeight()).append(" (Expected: 4)\n");
        results.append(String.format("    Compression ratio: %.2f:1 (Original: %.0f bits, Compressed: %.0f bits)\n", ratio, originalBits, compressedBits));

        writeToFile("Test2_RepeatedSymbols.txt", results.toString());
        System.out.println("Test 2 completed. Results in Test2_RepeatedSymbols.txt");
    }

    private static void testLongerText() throws IOException {
        StringBuilder results = new StringBuilder();
        results.append("Test 3: Longer Text\n");
        String test = "The quick brown fox jumps over the lazy dog";

        HuffmanTree encodeTree = new HuffmanTree();
        Encoder encoder = new Encoder(encodeTree);
        String encoded = encoder.encode(test);

        HuffmanTree decodeTree = new HuffmanTree();
        Decoder decoder = new Decoder(decodeTree);
        String decoded = decoder.decode(encoded);

        assertEquals(test, decoded, "Longer text test failed\nEncoded: " + encoded + "\nDecoded: '" + decoded + "'");

        double originalBits = test.length() * 8;
        double compressedBits = encoded.length();
        double ratio = compressedBits > 0 ? originalBits / compressedBits : 0;

        results.append("Passed: Longer text\n");
        results.append("    Encoded: ").append(encoded).append("\n");
        results.append("    Decoded: ").append(decoded).append("\n");
        results.append(String.format("    Compression ratio: %.2f:1 (Original: %.0f bits, Compressed: %.0f bits)\n", ratio, originalBits, compressedBits));

        writeToFile("Test3_LongerText.txt", results.toString());
        System.out.println("Test 3 completed. Results in Test3_LongerText.txt");
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + "\nExpected: " + expected + ", Actual: " + actual);
        }
    }

    private static void writeToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Error writing to " + fileName + ": " + e.getMessage());
        }
    }
}
