package VectorQuantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter codebook size (K): ");
        int k = scanner.nextInt();

        String img_path = "in.jpg";
        Compress compress = new Compress(img_path, k);  
        compress.encode();

        Decompress d = new Decompress("codeBook.txt");
        d.decode();
        d.makeImage();

        double mse = calculateMSE("in.jpg", "res.jpg");
        System.out.println("MSE: " + mse);
    }

    public static double calculateMSE(String originalPath, String reconstructedPath) throws IOException {
        BufferedImage original = ImageIO.read(new File(originalPath));
        BufferedImage reconstructed = ImageIO.read(new File(reconstructedPath));

        int width = original.getWidth();
        int height = original.getHeight();

        double mse = 0.0;
        int totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = original.getRGB(x, y);
                int rgb2 = reconstructed.getRGB(x, y);

                int gray1 = (rgb1 >> 16) & 0xff;
                int gray2 = (rgb2 >> 16) & 0xff;

                mse += Math.pow(gray1 - gray2, 2);
            }
        }

        return mse / totalPixels;
    }
}
