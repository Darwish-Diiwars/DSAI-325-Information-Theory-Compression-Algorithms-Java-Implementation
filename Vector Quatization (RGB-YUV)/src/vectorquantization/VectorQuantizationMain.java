import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.awt.Graphics2D;

public class VectorQuantizationMain {
    private static final String[] CATEGORIES = {"nature", "faces", "animals"};
    private static final String DATASET_PATH = "dataset";
    private static final String RGB_OUTPUT_PATH = "output_rgb";
    private static final String YUV_OUTPUT_PATH = "output_yuv";
    private static final int MAX_IMAGE_DIMENSION = 1024; 

    public static void main(String[] args) {
        try {
            new File(RGB_OUTPUT_PATH).mkdirs();
            new File(YUV_OUTPUT_PATH).mkdirs();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the block size (e.g., 2): ");
            int BLOCK_SIZE = scanner.nextInt();
            System.out.print("Enter the codebook size (e.g., 256): ");
            int CODEBOOK_SIZE = scanner.nextInt();
            scanner.close();

            System.out.println("=== VECTOR QUANTIZATION COMPRESSION ===");
            System.out.println("Block Size: " + BLOCK_SIZE + "x" + BLOCK_SIZE);
            System.out.println("Codebook Size: " + CODEBOOK_SIZE);
            System.out.println("Max K-means Iterations: 20");
            System.out.println("Training Images: 10 per category (nature, faces, animals)");
            System.out.println("Test Images: 5 per category\n");

            System.out.println("=== TRAINING CODEBOOKS ===");
            List<int[]> redVectors = new ArrayList<>();
            List<int[]> greenVectors = new ArrayList<>();
            List<int[]> blueVectors = new ArrayList<>();
            List<int[]> yVectors = new ArrayList<>();
            List<int[]> uVectors = new ArrayList<>();
            List<int[]> vVectors = new ArrayList<>();

            for (String category : CATEGORIES) {
                System.out.println("\nProcessing training images for category: " + category);
                for (int i = 1; i <= 10; i++) {
                    String baseName = category.endsWith("s") ? category.substring(0, category.length() - 1) : category;
                    String imagePath = String.format("%s/train/%s/%s_%d.jpg", DATASET_PATH, category, baseName, i);
                    System.out.println("  Loading: " + imagePath);

                    BufferedImage img = null;
                    try {
                        img = ImageIO.read(new File(imagePath));
                        if (img == null) {
                            System.err.println("    Error: Unable to read image - " + imagePath);
                            continue;
                        }
                    } catch (IOException e) {
                        System.err.println("    Error processing image: " + e.getMessage());
                        continue;
                    }

                    img = resizeImageIfNeeded(img, MAX_IMAGE_DIMENSION);
                    System.out.println("    Image size: " + img.getWidth() + "x" + img.getHeight());

                    img = VectorQuantizer.adjustImageDimensions(img, BLOCK_SIZE);

                    int[][] red = VectorQuantizer.extractChannel(img, 'R');
                    int[][] green = VectorQuantizer.extractChannel(img, 'G');
                    int[][] blue = VectorQuantizer.extractChannel(img, 'B');

                    redVectors.addAll(VectorQuantizer.extractBlocks(red, BLOCK_SIZE));
                    greenVectors.addAll(VectorQuantizer.extractBlocks(green, BLOCK_SIZE));
                    blueVectors.addAll(VectorQuantizer.extractBlocks(blue, BLOCK_SIZE));

                    int[][][] yuv = VectorQuantizer.convertRGBtoYUV(img);
                    int[][] y = yuv[0];
                    int[][] u = yuv[1];
                    int[][] v = yuv[2];
                    
                    int[][] uSub = VectorQuantizer.subsample(u, 2);
                    int[][] vSub = VectorQuantizer.subsample(v, 2);
                    
                    if (uSub.length < BLOCK_SIZE || uSub[0].length < BLOCK_SIZE) {
                        uSub = VectorQuantizer.adjustSmallChannel(uSub);
                    }
                    if (vSub.length < BLOCK_SIZE || vSub[0].length < BLOCK_SIZE) {
                        vSub = VectorQuantizer.adjustSmallChannel(vSub);
                    }
                    
                    yVectors.addAll(VectorQuantizer.extractBlocks(y, BLOCK_SIZE));
                    uVectors.addAll(VectorQuantizer.extractBlocks(uSub, BLOCK_SIZE));
                    vVectors.addAll(VectorQuantizer.extractBlocks(vSub, BLOCK_SIZE));

                    System.out.println("    Successfully processed");
                }
            }

            System.out.println("\nTraining RGB codebooks...");
            List<int[]> redCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                redVectors, Math.min(CODEBOOK_SIZE, redVectors.size()), 20);
            List<int[]> greenCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                greenVectors, Math.min(CODEBOOK_SIZE, greenVectors.size()), 20);
            List<int[]> blueCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                blueVectors, Math.min(CODEBOOK_SIZE, blueVectors.size()), 20);

            System.out.println("\nTraining YUV codebooks...");
            List<int[]> yCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                yVectors, Math.min(CODEBOOK_SIZE, yVectors.size()), 20);
            List<int[]> uCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                uVectors, Math.min(CODEBOOK_SIZE, uVectors.size()), 20);
            List<int[]> vCodebook = VectorQuantizer.generateCodebookUsingKMeans(
                vVectors, Math.min(CODEBOOK_SIZE, vVectors.size()), 20);

            System.out.println("\nAll codebooks trained successfully!");

            System.out.println("\n=== TESTING IMAGES ===");
            List<String> testResults = new ArrayList<>();

            for (String category : CATEGORIES) {
                System.out.println("\nTesting category: " + category);
                for (int i = 11; i <= 15; i++) {
                    String baseName = category.endsWith("s") ? category.substring(0, category.length() - 1) : category;
                    String imagePath = String.format("%s/test/%s/%s_%d.jpg", DATASET_PATH, category, baseName, i);
                    System.out.println("  Processing: " + imagePath);

                    BufferedImage original = null;
                    try {
                        original = ImageIO.read(new File(imagePath));
                        if (original == null) {
                            System.err.println("    Error: Unable to read image - " + imagePath);
                            continue;
                        }
                    } catch (IOException e) {
                        System.err.println("    Error processing test image: " + e.getMessage());
                        continue;
                    }

                    original = resizeImageIfNeeded(original, MAX_IMAGE_DIMENSION);
                    System.out.println("    Image size: " + original.getWidth() + "x" + original.getHeight());

                    original = VectorQuantizer.adjustImageDimensions(original, BLOCK_SIZE);
                    int width = original.getWidth();
                    int height = original.getHeight();

                    System.out.println("    Processing in RGB mode...");
                    long startTime = System.currentTimeMillis();
                    
                    int[][] red = VectorQuantizer.extractChannel(original, 'R');
                    int[][] green = VectorQuantizer.extractChannel(original, 'G');
                    int[][] blue = VectorQuantizer.extractChannel(original, 'B');

                    int[][] redIndices = VectorQuantizer.quantizeChannel(red, redCodebook);
                    int[][] greenIndices = VectorQuantizer.quantizeChannel(green, greenCodebook);
                    int[][] blueIndices = VectorQuantizer.quantizeChannel(blue, blueCodebook);

                    int[][] reconRed = VectorQuantizer.reconstructChannel(redIndices, redCodebook);
                    int[][] reconGreen = VectorQuantizer.reconstructChannel(greenIndices, greenCodebook);
                    int[][] reconBlue = VectorQuantizer.reconstructChannel(blueIndices, blueCodebook);

                    BufferedImage rgbReconstructed = VectorQuantizer.combineRGB(reconRed, reconGreen, reconBlue);
                    String rgbOutputPath = String.format("%s/%s_compressed_img%d.jpg", RGB_OUTPUT_PATH, category, i);
                    try {
                        ImageIO.write(rgbReconstructed, "jpg", new File(rgbOutputPath));
                    } catch (IOException e) {
                        System.err.println("    Error writing RGB image: " + e.getMessage());
                        continue;
                    }

                    double rgbPsnr = VectorQuantizer.calculatePSNR(original, rgbReconstructed);
                    double rgbCr = VectorQuantizer.calculateCompressionRatio(width, height, CODEBOOK_SIZE);
                    long rgbTime = System.currentTimeMillis() - startTime;

                    System.out.println("    Processing in YUV mode...");
                    startTime = System.currentTimeMillis();
                    
                    int[][][] yuv = VectorQuantizer.convertRGBtoYUV(original);
                    int[][] y = yuv[0];
                    int[][] u = yuv[1];
                    int[][] v = yuv[2];
                    
                    int[][] uSub = VectorQuantizer.subsample(u, 2);
                    int[][] vSub = VectorQuantizer.subsample(v, 2);
                    
                    if (uSub.length < BLOCK_SIZE || uSub[0].length < BLOCK_SIZE) {
                        uSub = VectorQuantizer.adjustSmallChannel(uSub);
                    }
                    if (vSub.length < BLOCK_SIZE || vSub[0].length < BLOCK_SIZE) {
                        vSub = VectorQuantizer.adjustSmallChannel(vSub);
                    }
                    
                    int[][] yIndices = VectorQuantizer.quantizeChannel(y, yCodebook);
                    int[][] uIndices = VectorQuantizer.quantizeChannel(uSub, uCodebook);
                    int[][] vIndices = VectorQuantizer.quantizeChannel(vSub, vCodebook);
                    
                    int[][] reconY = VectorQuantizer.reconstructChannel(yIndices, yCodebook);
                    int[][] reconU = VectorQuantizer.reconstructChannel(uIndices, uCodebook);
                    int[][] reconV = VectorQuantizer.reconstructChannel(vIndices, vCodebook);
                    
                    int[][] reconUUp = VectorQuantizer.upsample(reconU, 2, height, width);
                    int[][] reconVUp = VectorQuantizer.upsample(reconV, 2, height, width);
                    
                    BufferedImage yuvReconstructed = VectorQuantizer.convertYUVtoRGB(reconY, reconUUp, reconVUp);
                    String yuvOutputPath = String.format("%s/%s_compressed_img%d.jpg", YUV_OUTPUT_PATH, category, i);
                    try {
                        ImageIO.write(yuvReconstructed, "jpg", new File(yuvOutputPath));
                    } catch (IOException e) {
                        System.err.println("    Error writing YUV image: " + e.getMessage());
                        continue;
                    }

                    double yuvPsnr = VectorQuantizer.calculatePSNR(original, yuvReconstructed);
                    double yuvCr = VectorQuantizer.calculateCompressionRatioYUV(width, height, CODEBOOK_SIZE);
                    long yuvTime = System.currentTimeMillis() - startTime;

                    String result = String.format(
                        "  %s/%s_%d.jpg - RGB: PSNR=%.2f dB, CR=%.2f:1, Time=%dms | " +
                        "YUV: PSNR=%.2f dB, CR=%.2f:1, Time=%dms",
                        category, category, i, 
                        rgbPsnr, rgbCr, rgbTime,
                        yuvPsnr, yuvCr, yuvTime);
                    testResults.add(result);
                    System.out.println(result);
                }
            }

            // Print summary
            System.out.println("\n=== TEST RESULTS SUMMARY ===");
            System.out.println("Format: [Category/Category_X.jpg] - RGB: PSNR, Compression Ratio, Time | YUV: PSNR, Compression Ratio, Time");
            testResults.forEach(System.out::println);

            System.out.println("\nAll test images processed.");
            System.out.println("RGB compressed images saved in: " + RGB_OUTPUT_PATH);
            System.out.println("YUV compressed images saved in: " + YUV_OUTPUT_PATH);

        } catch (OutOfMemoryError e) {
            System.err.println("\nError: Out of memory. Try running with a larger heap size (e.g., java -Xmx8g -cp ... VectorQuantizationMain)");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\nError in Vector Quantization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeImageIfNeeded(BufferedImage original, int maxDimension) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return original;
        }

        double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        System.out.println("    Resized from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
        return resized;
    }
}