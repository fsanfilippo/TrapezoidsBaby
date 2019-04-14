package main.input;
import main.structures.DCEL.DCEL;
import main.structures.DCEL.Face;
import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;
import main.structures.Trapezoid;
import main.structures.TrapezoidMap;
import main.structures.search.SearchStructure;
import main.structures.search.LeafNode;
import main.structures.Segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrapMapBuilder {
    public static TrapezoidMap buildTrapMap(DCEL dcel){
        List<Segment> segments = getSegments(dcel.halfEdges);
        Face unbounded = getUnboundedFace(dcel.faces);
        Map<String, Segment> bb = getBoundingBox(segments, unbounded);
        SearchStructure ss = new SearchStructure(bb.get("L"), bb.get("r"), bb.get("t"), bb.get("b"));
        TrapezoidMap trapMap = new TrapezoidMap(ss);

        for(Segment s: segments){
            //Find the set ∆0,∆1,...,∆k of trapezoids in T properly intersected
            //by si.
            List<Trapezoid> intersecting = ss.followSegment(s);

            //Remove ∆0,∆1,...,∆k from T and replace
            //them by the new trapezoids that appear because of the insertion of si.
            trapMap.traps.removeAll(intersecting);

        }

        return trapMap;
    }

    public static Map<String, Segment> getBoundingBox(List<Segment> segments, Face unbounded){
        float bottom = Float.MAX_VALUE;
        float left = Float.MAX_VALUE;
        float right = Float.MIN_VALUE;
        float top = Float.MIN_VALUE;

        for(Segment s: segments){
            left = (s.p.x < left) ? s.p.x : left;
            left = (s.q.x < left) ? s.q.x : left;

            bottom = (s.p.y < bottom) ? s.p.y : bottom;
            bottom = (s.q.y < bottom) ? s.q.y : bottom;

            right = (s.p.x > right) ? s.p.x : right;
            right = (s.q.x > right) ? s.q.x : right;

            top = (s.p.y > top) ? s.p.y : top;
            top = (s.q.y > top) ? s.q.y : top;
        }

        //The four vertices of R, starting at the upper left corner,
        // are named counterclockwise as c1, c2, c3, and c4.
        Vertex tl = new Vertex(top + 1, left - 1, "c1");
        Vertex bl = new Vertex(bottom - 1, left - 1, "c2");
        Vertex br = new Vertex( bottom - 1, right + 1, "c3");
        Vertex tr = new Vertex(top + 1, right + 1, "c4");

        //The left, right, top, and bottom sides of the bounding box R for
        // all the segments are named L, R, T, and B, respectively.
        Segment l = new Segment(bl, tl, "L");
        Segment r = new Segment(br, tr, "R");
        Segment t = new Segment(tr, tl, "T");
        Segment b = new Segment(br, bl, "B");

        Map<String, Segment> R = new HashMap<>();
        R.put(l.name, l);
        R.put(r.name, r);
        R.put(t.name, t);
        R.put(b.name, b);

        return R;
    }

    //transforms halfEdges into segments
    private static List<Segment> getSegments(List<HalfEdge> halfEdges){
        List<HalfEdge> accountedFor = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        int segmentCount = 0;
        for(HalfEdge h: halfEdges){
            if(!accountedFor.contains(h)){
                Segment s = new Segment(segmentCount++, h, h.twin);
                accountedFor.add(h);
                accountedFor.add(h.twin);
                segments.add(s);
            }
        }
        return segments;
    }

    private static Face getUnboundedFace(List<Face> faces){
        for (Face f: faces){
            if(f.outerComponent == null) return f;
        }
        return null;
    }

}
