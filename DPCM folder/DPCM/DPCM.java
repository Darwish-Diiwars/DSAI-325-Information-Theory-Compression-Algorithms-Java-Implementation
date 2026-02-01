import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DPCM {

    private static int quantize(int value, int levels) {
        int stepSize = 256 / levels;
        return Math.round((float) value / stepSize) * stepSize;
    }

    private static int predict(int a, int b, int c, String predictor) {
        switch (predictor) {
            case "order1": return a;
            case "order2": return a + b - c;
            case "adaptive":
                if (c >= Math.max(a, b)) return Math.min(a, b);
                else if (c <= Math.min(a, b)) return Math.max(a, b);
                else return a + b - c;
            default: return 0;
        }
    }

    private static int[][] readGrayscaleImage(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int[][] gray = new int[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                gray[y][x] = image.getRGB(x, y) & 0xFF;
        return gray;
    }

    private static BufferedImage reconstructImage(int[][] reconstructed) {
        int height = reconstructed.length;
        int width = reconstructed[0].length;
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int gray = reconstructed[y][x];
                int rgb = (gray << 16) | (gray << 8) | gray;
                output.setRGB(x, y, rgb);
            }
        return output;
    }

    private static void compressAndReconstruct(int[][] original, int[][] reconstructed, int[][] residual, int levels, String predictor) {
        int height = original.length, width = original[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = (x > 0) ? reconstructed[y][x - 1] : 0;
                int b = (y > 0) ? reconstructed[y - 1][x] : 0;
                int c = (x > 0 && y > 0) ? reconstructed[y - 1][x - 1] : 0;
                int pred = predict(a, b, c, predictor);
                int error = original[y][x] - pred;
                int quantizedError = quantize(error, levels);

                residual[y][x] = quantizedError;
                reconstructed[y][x] = Math.min(255, Math.max(0, pred + quantizedError));
            }
        }
    }

    private static double calculateMSE(int[][] original, int[][] reconstructed) {
        double mse = 0;
        int height = original.length, width = original[0].length;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                mse += Math.pow(original[y][x] - reconstructed[y][x], 2);
        return mse / (width * height);
    }

    public static void main(String[] args) {
        String[] imageFiles = {
            "Barbara_gray.png",
            "Lena_gray.png",
            "Cameraman_gray.png",
            "Peppers_gray.png",
            "Goldhill_gray.png"
        };

        String[] predictors = {"order1", "order2", "adaptive"};
        int[] quantizationLevels = {8, 16, 32};

        for (String fileName : imageFiles) {
            for (String predictor : predictors) {
                for (int levels : quantizationLevels) {
                    try {
                        File file = new File(fileName);
                        if (!file.exists()) {
                            System.err.println("File not found: " + fileName);
                            continue;
                        }

                        BufferedImage image = ImageIO.read(file);
                        int[][] original = readGrayscaleImage(image);
                        int height = original.length, width = original[0].length;
                        int[][] reconstructed = new int[height][width];
                        int[][] residual = new int[height][width];

                        compressAndReconstruct(original, reconstructed, residual, levels, predictor);
                        BufferedImage outputImage = reconstructImage(reconstructed);

                        String baseName = file.getName().replaceFirst("[.][^.]+$", "");
                        String outputPath = "reconstructed_" + baseName + "_" + predictor + "_" + levels + ".png";
                        ImageIO.write(outputImage, "png", new File(outputPath));

                        double mse = calculateMSE(original, reconstructed);
                        int originalBits = width * height * 8;
                        int compressedBits = width * height * (int) Math.ceil(Math.log(levels) / Math.log(2));
                        double compressionRatio = (double) originalBits / compressedBits;

                        System.out.println("\n===== " + baseName + " | Predictor: " + predictor + " | Levels: " + levels + " =====");
                        System.out.printf("MSE: %.2f\n", mse);
                        System.out.printf("Original Size: %d bits (%.2f KB)\n", originalBits, originalBits / 8192.0);
                        System.out.printf("Compressed Size: %d bits (%.2f KB)\n", compressedBits, compressedBits / 8192.0);
                        System.out.printf("Compression Ratio: %.2f\n", compressionRatio);
                        System.out.println("Reconstructed image saved to: " + outputPath);

                    } catch (IOException e) {
                        System.err.println("Error processing " + fileName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
