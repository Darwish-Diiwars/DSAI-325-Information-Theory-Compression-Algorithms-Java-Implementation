import java.io.*;
import java.util.*;

public class Main {

    private static class Tag {
        int position;
        char nextChar;

        Tag(int position, char nextChar) {
            this.position = position;
            this.nextChar = nextChar;
        }
    }

    public static void main(String[] args) throws IOException {
        String inputTextPath = "C:/Users/admin/Desktop/DSAI325_Assignment2_LZ78/lz78/input.txt";
        String outputBinaryPath = "C:/Users/admin/Desktop/DSAI325_Assignment2_LZ78/lz78/output.bin";
        String outputTagPath = "C:/Users/admin/Desktop/DSAI325_Assignment2_LZ78/lz78/output.txt";
        String outputDecompressedPath = "C:/Users/admin/Desktop/DSAI325_Assignment2_LZ78/lz78/decopressed.txt";

        compress(inputTextPath, outputBinaryPath, outputTagPath);

        decompress(outputBinaryPath, outputDecompressedPath);
    }

    public static void compress(String inputPath, String outputPath, String outputTagPath) throws IOException {
        String inputData = readTextFile(inputPath);
        List<Tag> tags = new ArrayList<>();
        Map<String, Integer> dictionary = new HashMap<>();
        dictionary.put("", 0);
        int nextIndex = 1;
        String currentString = "";

        for (char c : inputData.toCharArray()) {
            String candidate = currentString + c;
            if (dictionary.containsKey(candidate)) {
                currentString = candidate;
            } else {
                int pos = dictionary.get(currentString);
                tags.add(new Tag(pos, c));
                dictionary.put(candidate, nextIndex++);
                currentString = "";
            }
        }

        if (!currentString.isEmpty()) {
            int pos = dictionary.get(currentString);
            tags.add(new Tag(pos, '\0')); 
        }

        try (FileWriter tagWriter = new FileWriter(outputTagPath)) {
            StringBuilder tagOutput = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                Tag tag = tags.get(i);
                String charStr = tag.nextChar == '\0' ? "" : String.valueOf(tag.nextChar);
                tagOutput.append("<").append(tag.position).append(",").append(charStr).append(">");
                if (i != tags.size() - 1) tagOutput.append(",");
            }
            tagWriter.write(tagOutput.toString());
        }

        int maxPos = tags.stream().mapToInt(t -> t.position).max().orElse(0);
        int bitsNeeded = bitsNeeded(maxPos);

        StringBuilder binaryString = new StringBuilder();
        binaryString.append(String.format("%8s", Integer.toBinaryString(bitsNeeded)).replace(' ', '0'));

        for (Tag tag : tags) {
            binaryString.append(binarizeTag(tag.position, bitsNeeded, tag.nextChar));
        }

        try (FileWriter binWriter = new FileWriter(outputPath)) {
            binWriter.write(binaryString.toString());
        }

        int compressedSize = (bitsNeeded + 8) * tags.size();
        System.out.println("Original size: " + inputData.length() * 8 + " bits");
        System.out.println("Compressed size: " + compressedSize + " bits");
    }

    public static void decompress(String inputPath, String outputPath) throws IOException {
        String binaryString = readTextFile(inputPath);

        int bitsNeeded = Integer.parseInt(binaryString.substring(0, 8), 2);
        String tagBinary = binaryString.substring(8);
        int tagLength = bitsNeeded + 8;
        List<Tag> tags = new ArrayList<>();

        for (int i = 0; i < tagBinary.length(); i += tagLength) {
            if (i + tagLength > tagBinary.length()) break;
            String segment = tagBinary.substring(i, i + tagLength);
            int pos = Integer.parseInt(segment.substring(0, bitsNeeded), 2);
            char nextChar = (char) Integer.parseInt(segment.substring(bitsNeeded), 2);
            tags.add(new Tag(pos, nextChar));
        }

        List<String> dictionary = new ArrayList<>();
        dictionary.add("");
        StringBuilder output = new StringBuilder();

        for (Tag tag : tags) {
            if (tag.nextChar == '\0') {
                output.append(dictionary.get(tag.position));
                break;
            }
            String phrase = dictionary.get(tag.position) + tag.nextChar;
            output.append(phrase);
            dictionary.add(phrase);
        }

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(output.toString());
        }
    }

    private static String readTextFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int c;
            while ((c = reader.read()) != -1) content.append((char) c);
        }
        return content.toString();
    }

    public static int bitsNeeded(int number) {
        if (number == 0) return 1;
        return (int) (Math.log(number) / Math.log(2)) + 1;
    }

    public static String binarizeTag(int pos, int bits, char nextChar) {
        String binaryPos = String.format("%" + bits + "s", Integer.toBinaryString(pos)).replace(' ', '0');
        String binaryChar = String.format("%8s", Integer.toBinaryString(nextChar)).replace(' ', '0');
        return binaryPos + binaryChar;
    }
}
