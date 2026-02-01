package VectorQuantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class Decompress {
    private ArrayList<vector> codeBook;
    private ArrayList<Integer> codes;
    private int h, w;                      
    private int width, height;              
    private int[][] intImg;
    private String path;

    Decompress(String file) {
        System.out.println("Decompressing...");
        codeBook = new ArrayList<>();
        codes = new ArrayList<>();
        path = file;
    }

    void decode() throws IOException {
        FileReader f = new FileReader(path);
        BufferedReader reader = new BufferedReader(f);
        String line = reader.readLine();
        String[] res = line.split(" ");              
        h = Integer.parseInt(res[0]);
        w = Integer.parseInt(res[1]);
        height = Integer.parseInt(res[2]);
        width = Integer.parseInt(res[3]);
        intImg = new int[height][width];
        line = reader.readLine();
        res = line.split(" ");
        for (String re : res) {
            codes.add(Integer.parseInt(re));
        }

        // read codeBook
        vector v = new vector(h, w);
        int c = 0;
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            res = line.split(" ");
            for (int i = 0; i < v.width; ++i) v.data[c][i] = Double.parseDouble(res[i]);
            if (c == h - 1) {
                codeBook.add(v);
                v = new vector(h, w);
                c = 0;
            } else c++;
        }
    }

    void makeImage() throws IOException {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int idx = 0;
        for (int i = 0; i < height; i += h) {
            for (int j = 0; j < width; j += w) {
                vector v = codeBook.get(codes.get(idx++));
                int r = i, c = j;
                for (int k = 0; k < h; ++k) {
                    for (int l = 0; l < w; ++l) {
                        intImg[r][c++] = (int) (v.data[k][l]);
                    }
                    c = j;
                    r++;
                }
            }
        }

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int p = intImg[j][i];

                out.setRGB(i, j, (p << 16) | (p << 8) | p);
            }
        }
        ImageIO.write(out, "JPG", new File("res.jpg"));
    }
}
