package VectorQuantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class ConstructVectors {
    ArrayList<vector> redBlocks = new ArrayList<>();
    ArrayList<vector> greenBlocks = new ArrayList<>();
    ArrayList<vector> blueBlocks = new ArrayList<>();
    int hei, wi;
    private int row, col;
    private File file;

    ConstructVectors(int n, int m, String path) {
        row = n;
        col = m;
        file = new File(path);
        LoadImage();
    }

    private void LoadImage() {
        try {
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();
            int[][] redImg = new int[height][width];
            int[][] greenImg = new int[height][width];
            int[][] blueImg = new int[height][width];
            hei = height;
            wi = width;
            System.out.println("Reading complete: " + file.getName());

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int p = img.getRGB(j, i);
                    redImg[i][j] = (p >> 16) & 0xff;   // Red
                    greenImg[i][j] = (p >> 8) & 0xff;  // Green
                    blueImg[i][j] = p & 0xff;          // Blue
                }
            }

            if (width % col == 0 && height % row == 0) {
                for (int i = 0; i < height; i += row) {
                    for (int j = 0; j < width; j += col) {
                        vector redV = new vector(row, col);
                        vector greenV = new vector(row, col);
                        vector blueV = new vector(row, col);
                        int r = i, c = j;
                        for (int k = 0; k < row; k++) {
                            for (int l = 0; l < col; l++) {
                                redV.data[k][l] = redImg[r][c];
                                greenV.data[k][l] = greenImg[r][c];
                                blueV.data[k][l] = blueImg[r][c];
                                c++;
                            }
                            c = j;
                            r++;
                        }
                        redBlocks.add(redV);
                        greenBlocks.add(greenV);
                        blueBlocks.add(blueV);
                    }
                }
            } else {
                System.out.println("Invalid vector size for " + file.getName());
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}