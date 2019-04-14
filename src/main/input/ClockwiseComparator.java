package main.input;

import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;

import java.util.Comparator;

public class ClockwiseComparator implements Comparator<HalfEdge> {

    public int compare(HalfEdge a, HalfEdge b){

//        Vertex nextA = a.twin.origin;
//        Vertex nextB = b.twin.origin;
//        Vertex center = a.origin;
//
//        if (nextA.x - center.x >= 0 && nextB.x - center.x < 0)
//            return -1;
//        if (nextA.x - center.x < 0 && nextB.x - center.x >= 0)
//            return 1;
//        if (nextA.x - center.x == 0 && nextB.x - center.x == 0) {
//            if (nextA.y - center.y >= 0 || nextB.y - center.y >= 0)
//                return nextA.y > nextB.y ? -1 : 1;
//            return nextB.y > nextA.y ? -1 : 1;
//        }
//
//        // compute the cross product of vectors (center -> a) x (center -> b)
//        int det = (nextA.x - center.x) * (nextB.y - center.y) - (nextB.x - center.x) * (nextA.y - center.y);
//        if (det < 0)
//            return -1;
//        if (det > 0)
//            return 1;
//
//        //A and B are on the same line. This shouldn't happen
        return 0;
    }
}
