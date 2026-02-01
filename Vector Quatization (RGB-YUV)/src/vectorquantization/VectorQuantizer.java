import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VectorQuantizer {

    public static int[][] extractChannel(BufferedImage image, char channel) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                switch (channel) {
                    case 'R':
                        result[y][x] = (rgb >> 16) & 0xFF;
                        break;
                    case 'G':
                        result[y][x] = (rgb >> 8) & 0xFF;
                        break;
                    case 'B':
                        result[y][x] = rgb & 0xFF;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid channel: " + channel);
                }
            }
        }
        return result;
    }

    public static List<int[]> extractBlocks(int[][] channel, int blockSize) {
        List<int[]> blocks = new ArrayList<>();
        int height = channel.length;
        int width = channel[0].length;

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int[] block = new int[blockSize * blockSize];
                int idx = 0;
                for (int dy = 0; dy < blockSize && (y + dy) < height; dy++) {
                    for (int dx = 0; dx < blockSize && (x + dx) < width; dx++) {
                        block[idx++] = channel[y + dy][x + dx];
                    }
                }
                while (idx < blockSize * blockSize) {
                    block[idx++] = 0;
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    public static List<int[]> generateCodebookUsingKMeans(List<int[]> trainingVectors, int codebookSize, int maxIterations) {
        if (trainingVectors.isEmpty()) {
            throw new IllegalArgumentException("Training vectors cannot be empty");
        }

        int vectorLength = trainingVectors.get(0).length;
        List<int[]> codebook = new ArrayList<>();
        Random random = new Random();

        // Initialize codebook with random vectors from training set
        List<int[]> tempVectors = new ArrayList<>(trainingVectors);
        for (int i = 0; i < codebookSize && !tempVectors.isEmpty(); i++) {
            int index = random.nextInt(tempVectors.size());
            codebook.add(tempVectors.get(index));
            tempVectors.remove(index);
        }

        // K-means clustering
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            List<List<int[]>> clusters = new ArrayList<>();
            for (int i = 0; i < codebookSize; i++) {
                clusters.add(new ArrayList<>());
            }

            // Assign each vector to the nearest codeword
            for (int[] vector : trainingVectors) {
                int nearestIndex = findNearestCodeword(vector, codebook);
                clusters.get(nearestIndex).add(vector);
            }

            // Update codewords
            boolean changed = false;
            for (int i = 0; i < codebookSize; i++) {
                List<int[]> cluster = clusters.get(i);
                if (cluster.isEmpty()) continue;

                int[] newCodeword = new int[vectorLength];
                for (int[] vector : cluster) {
                    for (int j = 0; j < vectorLength; j++) {
                        newCodeword[j] += vector[j];
                    }
                }
                for (int j = 0; j < vectorLength; j++) {
                    newCodeword[j] = Math.round((float) newCodeword[j] / cluster.size());
                }

                if (!arrayEquals(newCodeword, codebook.get(i))) {
                    changed = true;
                    codebook.set(i, newCodeword);
                }
            }

            if (!changed) break;
        }

        return codebook;
    }

    private static boolean arrayEquals(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private static int findNearestCodeword(int[] vector, List<int[]> codebook) {
        int nearestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < codebook.size(); i++) {
            double distance = euclideanDistance(vector, codebook.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    private static double euclideanDistance(int[] v1, int[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            int diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public static int[][] quantizeChannel(int[][] channel, List<int[]> codebook) {
        int height = channel.length;
        int width = channel[0].length;
        int blockSize = (int) Math.sqrt(codebook.get(0).length);
        int numBlocksY = (int) Math.ceil((double) height / blockSize);
        int numBlocksX = (int) Math.ceil((double) width / blockSize);
        int[][] indices = new int[numBlocksY][numBlocksX];

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int[] block = new int[blockSize * blockSize];
                int idx = 0;
                for (int dy = 0; dy < blockSize && (y + dy) < height; dy++) {
                    for (int dx = 0; dx < blockSize && (x + dx) < width; dx++) {
                        block[idx++] = channel[y + dy][x + dx];
                    }
                }
                while (idx < blockSize * blockSize) {
                    block[idx++] = 0;
                }
                int blockY = y / blockSize;
                int blockX = x / blockSize;
                if (blockY < numBlocksY && blockX < numBlocksX) {
                    indices[blockY][blockX] = findNearestCodeword(block, codebook);
                }
            }
        }
        return indices;
    }

    public static int[][] reconstructChannel(int[][] indices, List<int[]> codebook) {
        int blockSize = (int) Math.sqrt(codebook.get(0).length);
        int height = indices.length * blockSize;
        int width = indices[0].length * blockSize;
        int[][] channel = new int[height][width];

        for (int y = 0; y < indices.length; y++) {
            for (int x = 0; x < indices[0].length; x++) {
                int[] codeword = codebook.get(indices[y][x]);
                int idx = 0;
                for (int dy = 0; dy < blockSize; dy++) {
                    for (int dx = 0; dx < blockSize; dx++) {
                        if (y * blockSize + dy < height && x * blockSize + dx < width) {
                            channel[y * blockSize + dy][x * blockSize + dx] = codeword[idx++];
                        }
                    }
                }
            }
        }
        return channel;
    }

    public static BufferedImage combineRGB(int[][] red, int[][] green, int[][] blue) {
        int height = red.length;
        int width = red[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = Math.min(Math.max(red[y][x], 0), 255);
                int g = Math.min(Math.max(green[y][x], 0), 255);
                int b = Math.min(Math.max(blue[y][x], 0), 255);
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    public static int[][][] convertRGBtoYUV(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] y = new int[height][width];
        int[][] u = new int[height][width];
        int[][] v = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                y[i][j] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                u[i][j] = (int) (-0.147 * r - 0.289 * g + 0.436 * b + 128);
                v[i][j] = (int) (0.615 * r - 0.515 * g - 0.100 * b + 128);

                y[i][j] = Math.min(Math.max(y[i][j], 0), 255);
                u[i][j] = Math.min(Math.max(u[i][j], 0), 255);
                v[i][j] = Math.min(Math.max(v[i][j], 0), 255);
            }
        }
        return new int[][][]{y, u, v};
    }

    public static BufferedImage convertYUVtoRGB(int[][] y, int[][] u, int[][] v) {
        int height = y.length;
        int width = y[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int Y = y[i][j];
                int U = u[i][j] - 128;
                int V = v[i][j] - 128;

                int r = (int) (Y + 1.13983 * V);
                int g = (int) (Y - 0.39465 * U - 0.58060 * V);
                int b = (int) (Y + 2.03211 * U);

                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);

                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(j, i, rgb);
            }
        }
        return image;
    }

    public static int[][] subsample(int[][] channel, int factor) {
        int height = channel.length;
        int width = channel[0].length;
        int newHeight = height / factor;
        int newWidth = width / factor;
        int[][] subsampled = new int[newHeight][newWidth];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                subsampled[y][x] = channel[y * factor][x * factor];
            }
        }
        return subsampled;
    }

    public static int[][] upsample(int[][] channel, int factor, int targetHeight, int targetWidth) {
        int height = channel.length;
        int width = channel[0].length;
        int[][] upsampled = new int[targetHeight][targetWidth];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int dy = 0; dy < factor && (y * factor + dy) < targetHeight; dy++) {
                    for (int dx = 0; dx < factor && (x * factor + dx) < targetWidth; dx++) {
                        upsampled[y * factor + dy][x * factor + dx] = channel[y][x];
                    }
                }
            }
        }
        return upsampled;
    }

    public static double calculatePSNR(BufferedImage original, BufferedImage reconstructed) {
        int width = original.getWidth();
        int height = original.getHeight();
        double mse = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = original.getRGB(x, y);
                int rgb2 = reconstructed.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;

                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;

                mse += (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
            }
        }

        mse /= (width * height * 3);
        if (mse == 0) return Double.POSITIVE_INFINITY;
        return 20 * Math.log10(255.0) - 10 * Math.log10(mse);
    }

    public static double calculateCompressionRatio(int width, int height, int codebookSize) {
        int blockSize = (int) Math.sqrt(codebookSize);
        int blocks = (width / blockSize) * (height / blockSize);
        double originalBits = width * height * 24.0; 
        double compressedBits = blocks * Math.log(codebookSize) / Math.log(2); 
        return originalBits / compressedBits;
    }

    public static double calculateCompressionRatioYUV(int width, int height, int codebookSize) {
        int blockSize = (int) Math.sqrt(codebookSize);
        int blocksY = (width / blockSize) * (height / blockSize);
        int blocksUV = (width / (blockSize * 2)) * (height / (blockSize * 2));
        double originalBits = width * height * 24.0; 
        double compressedBits = (blocksY + 2 * blocksUV) * Math.log(codebookSize) / Math.log(2);
        return originalBits / compressedBits;
    }

    public static BufferedImage adjustImageDimensions(BufferedImage image, int blockSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = width - (width % blockSize);
        int newHeight = height - (height % blockSize);

        if (newWidth == width && newHeight == height) {
            return image;
        }

        System.out.println("    Cropping from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
        BufferedImage cropped = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                cropped.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return cropped;
    }

    public static int[][] adjustSmallChannel(int[][] channel) {
        int height = channel.length;
        int width = channel[0].length;
        int newHeight = Math.max(height, 2);
        int newWidth = Math.max(width, 2);
        int[][] adjusted = new int[newHeight][newWidth];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                if (y < height && x < width) {
                    adjusted[y][x] = channel[y][x];
                } else {
                    adjusted[y][x] = 128; 
                }
            }
        }
        return adjusted;
    }
}