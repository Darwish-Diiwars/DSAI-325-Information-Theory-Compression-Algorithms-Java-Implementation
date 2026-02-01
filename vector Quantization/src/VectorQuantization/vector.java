package VectorQuantization;

class vector {
    int width;
    int height;
    double data [][];
    vector(int h, int w){
        width = w;
        height = h;
        data = new double [height][width];
    }
}
