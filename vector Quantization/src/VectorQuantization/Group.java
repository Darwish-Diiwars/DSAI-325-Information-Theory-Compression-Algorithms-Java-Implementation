package VectorQuantization;

import java.util.ArrayList;

class Group {
    vector v;
    ArrayList<vector> neighbours= new ArrayList<>();
    Group(vector vv){
        v = vv;
    }
    void setNeighbours(ArrayList<vector> v){
        neighbours = v;
    }
    void add(vector v){
        neighbours.add(v);
    }
}
