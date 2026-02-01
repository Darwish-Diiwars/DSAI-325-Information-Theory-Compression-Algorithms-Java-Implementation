import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Compress or decompress? (c/d)");
        String answer = userInput.readLine().toLowerCase();

        if (answer.equals("c")) {
            doCompress("C:/Users/admin/Desktop/DSAI 325 – Assignment 1 -Mohamed darwish-202201273/lz77/plain.txt",
                    "C:/Users/admin/Desktop/DSAI 325 – Assignment 1 -Mohamed darwish-202201273/lz77/src/output.txt");
        } else if (answer.equals("d")) {
            doDecompress("C:/Users/admin/Desktop/DSAI 325 – Assignment 1 -Mohamed darwish-202201273/lz77/src/output.txt",
                    "C:/Users/admin/Desktop/DSAI 325 – Assignment 1 -Mohamed darwish-202201273/lz77/plain.txt");
        } else {
            System.out.println("Just type c or d!");
        }
    }

    public static void doCompress(String readFile, String writeFile) throws IOException {
        FileReader fileIn = new FileReader(readFile);
        FileWriter fileOut = new FileWriter(writeFile);

        StringBuilder inputData = new StringBuilder();
        int c;
        while ((c = fileIn.read()) != -1) {
            inputData.append((char) c);
        }

        int originalBits = inputData.length() * 8;
        System.out.println("Original size: " + originalBits + " bits");
        System.out.println("Original length: " + inputData.length());

        int window = 30;
        int pos = 0;
        int biggestDistance = 0;
        int biggestLength = 0;
        int tagCounter = 0;

        while (pos < inputData.length()) {
            int bestDist = 0;
            int bestLen = 0;

            // Find best match
            for (int back = Math.max(0, pos - window); back < pos; back++) {
                int currentLen = 0;
                while (currentLen < window &&
                        pos + currentLen < inputData.length() &&
                        inputData.charAt(back + currentLen) == inputData.charAt(pos + currentLen)) {
                    currentLen++;
                }
                if (currentLen > bestLen) {
                    bestLen = currentLen;
                    bestDist = pos - back;
                }
            }

            // Update biggest values
            if (bestDist > biggestDistance)
                biggestDistance = bestDist;
            if (bestLen > biggestLength)
                biggestLength = bestLen;

            // Write tag
            if (bestLen > 0) {
                char nextChar = pos + bestLen < inputData.length() ? inputData.charAt(pos + bestLen) : ' ';
                fileOut.write("(" + bestDist + "," + bestLen + "," + nextChar + ")\n");
                pos += bestLen + 1;
            } else {
                fileOut.write("(0,0," + inputData.charAt(pos) + ")\n");
                pos++;
            }
            tagCounter++;
        }

        // Compressed size calculation
        int bitsForDistance = Integer.toBinaryString(biggestDistance).length();
        int bitsForLength = Integer.toBinaryString(biggestLength).length();
        int compressedBits = (bitsForDistance + bitsForLength + 8) * tagCounter;
        System.out.println("Compressed size: " + compressedBits + " bits");

        fileIn.close();
        fileOut.close();
    }

    public static void doDecompress(String readFile, String writeFile) throws IOException {
        BufferedReader fileIn = new BufferedReader(new FileReader(readFile));
        FileWriter fileOut = new FileWriter(writeFile);
        StringBuilder result = new StringBuilder();

        String line;
        while ((line = fileIn.readLine()) != null) {
            line = line.replace("(", "").replace(")", "");
            String[] parts = line.split(",");
            int d = Integer.parseInt(parts[0]);
            int l = Integer.parseInt(parts[1]);
            char ch = parts[2].charAt(0);

            if (d > 0) {
                int start = result.length() - d;
                for (int i = 0; i < l; i++) {
                    result.append(result.charAt(start + i));
                }
            }
            result.append(ch);
        }

        fileOut.write(result.toString());
        fileIn.close();
        fileOut.close();
    }
}