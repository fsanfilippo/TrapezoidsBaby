package main.input;
import main.structures.DCEL.DCEL;
import main.structures.DCEL.Face;
import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;
import main.structures.Trapezoid;
import main.structures.TrapezoidMap;
import main.structures.search.*;
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
        List<Segment> boundingList = new ArrayList<>(bb.values());
        Segment l = bb.get("L");
        Segment r =  bb.get("R");
        Segment t = bb.get("T");
        Segment b = bb.get("B");
        Trapezoid trapRoot = new Trapezoid(b.p, b.q, t, b);
        SearchStructure ss = new SearchStructure(l, r, t, b, trapRoot);
        TrapezoidMap trapMap = new TrapezoidMap(ss, boundingList, trapRoot);

        for(Segment s: segments){
            trapMap.segments.add(s);

            //Find the set ∆0,∆1,...,∆k of trapezoids in T properly intersected
            //by si.
            List<Trapezoid> intersecting = ss.followSegment(s);
            intersecting.size();

            //Remove ∆0,∆1,...,∆k from T and replace
            //them by the new trapezoids that appear because of the insertion of si.
            trapMap.traps.removeAll(intersecting);

            if(intersecting.size() == 1){
                handldOneInsersectingTrap(trapMap, s, intersecting, ss);
            }
            else{
                //ToDo: whip this shit out
            }

        }

        return trapMap;
    }

    private static Map<String, Segment> getBoundingBox(List<Segment> segments, Face unbounded){
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

    private static boolean endPointNotContainedInTrap(Vertex trapVertex, Vertex segEndPoint){
        if(trapVertex == segEndPoint) return true;
        if(trapVertex.x == segEndPoint.x) return true;
        return false;
    }

    private static void handldOneInsersectingTrap(TrapezoidMap trapMap, Segment s, List<Trapezoid> intersecting, SearchStructure ss){
        Trapezoid old = intersecting.get(0);
        Node parentOfReplace = ss.getParentNode(s, s.p);
        LeafNode toReplace = ss.segmentQueryNode(s, s.p);
        Node subRoot;

        if(endPointNotContainedInTrap(old.leftp, s.p)){
            if(endPointNotContainedInTrap(old.rightp, s.q)){
                // Both endpoints of s lie on vertical lines or is already in T
                // Therefore only create two new trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s

                Vertex[] left = setLeftRightp(old.leftp, s.p);
                Vertex[] right = setLeftRightp(old.rightp, s.q);

                Vertex leftp_A = left[0];
                Vertex leftp_B = left[1];
                Vertex rightp_A = right[0];
                Vertex rightp_B = right[1];

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);

                trapMap.traps.add(A);
                trapMap.traps.add(B);

                subRoot = new YNode(s);
                subRoot.lChild = new LeafNode(A);
                subRoot.rChild = new LeafNode(B);

            }
            else{
                // Left endpoint of s lies on vertical line or is already in T but not right endpoint
                // Therefore create three trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s
                Trapezoid C; // Right of s

                Vertex[] left = setLeftRightp(old.leftp, s.p);
                Vertex leftp_A = left[0];
                Vertex leftp_B = left[1];
                Vertex leftp_C = s.q;
                Vertex rightp_A = s.q;
                Vertex rightp_B = s.q;
                Vertex rightp_C = old.rightp;

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);
                C = new Trapezoid(leftp_C, rightp_C, old.top, old.bottom);

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);

                subRoot = new XNode(s.q);
                subRoot.rChild = new LeafNode(C);
                Node si = new YNode(s);
                si.lChild = new LeafNode(A);
                si.rChild = new LeafNode(B);
                subRoot.lChild = si;

            }
        }
        else{
            if(endPointNotContainedInTrap(old.rightp, s.q)){
                // right endpoint of s lies on vertical line or is already in T but not left endpoint
                // Therefore create three trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s
                Trapezoid C; // Left of s

                Vertex[] right = setLeftRightp(old.leftp, s.p);

                Vertex leftp_A = s.q;
                Vertex leftp_B = s.q;
                Vertex leftp_C = old.leftp;
                Vertex rightp_A = right[0];
                Vertex rightp_B = right[1];
                Vertex rightp_C = s.p;

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);
                C = new Trapezoid(leftp_C, rightp_C, old.top, old.bottom);

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);

                subRoot = new XNode(s.p);
                subRoot.lChild = new LeafNode(C);
                Node si = new YNode(s);
                si.lChild = new LeafNode(A);
                si.rChild = new LeafNode(B);
                subRoot.rChild = si;
            }
            else{
                // s is completely contained in the trapezoid
                // Therefore create four new trapezoids

                Trapezoid A = new Trapezoid(old.leftp, s.p, old.top, old.bottom); // Left of s
                Trapezoid B = new Trapezoid(s.p, s.q, old.top, s); // Above s
                Trapezoid C = new Trapezoid(s.p, s.q, s, old.bottom); // Below s
                Trapezoid D = new Trapezoid(s.q, old.rightp, old.top, old.bottom); // Right of s

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);
                trapMap.traps.add(D);

                Node si = new YNode(s);
                si.lChild = new LeafNode(B);
                si.rChild = new LeafNode(C);

                Node qi = new XNode(s.q);
                qi.rChild = new LeafNode(D);
                qi.lChild = si;

                subRoot = new XNode(s.p);
                subRoot.lChild = new LeafNode(A);
                subRoot.rChild = si;
            }
        }

        //Replacing root
        if(parentOfReplace == null){
            ss.replaceAtRoot(subRoot);
        }
        else if(toReplace == parentOfReplace.lChild){
            parentOfReplace.lChild = subRoot;
        }
        else{
            parentOfReplace.rChild = subRoot;
        }
    }

    private static Vertex[] setLeftRightp(Vertex old, Vertex sEndpoint){
        Vertex A;
        Vertex B;
        if(old == sEndpoint){
            A = B = sEndpoint;
        }
        else if(old.y > sEndpoint.y){
            A = old;
            B = sEndpoint;
        }
        else{
            A = sEndpoint;
            B = old;
        }
        Vertex AB[] = {A, B};
        return AB;
    }

}
