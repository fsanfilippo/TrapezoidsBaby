package main.input;

import main.structures.DCEL.Vertex;

import java.util.Comparator;

public class PQComparator implements Comparator<Vertex> {
    public int compare(Vertex a, Vertex b){
        if(a.x < b.x){
            return -1;
        }
        else if(a.x == b.x){
            if(a.y < b.y){
                return -1;
            }
            else if(a.y == b.y){
                return 0;
            }
            else{
                return 1;
            }
        }
        else{
            return 1;
        }
    }
}
