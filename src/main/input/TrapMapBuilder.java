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

    private static float epsilon = 0.0001f;

    public static TrapezoidMap buildTrapMap(DCEL dcel) {
        List<Segment> segments = getSegments(dcel.halfEdges);
        Face unbounded = getUnboundedFace(dcel.faces);
        Map<String, Segment> bb = getBoundingBox(segments, unbounded);
        List<Segment> boundingList = new ArrayList<>(bb.values());
        Segment l = bb.get("L");
        Segment r = bb.get("R");
        Segment t = bb.get("T");
        Segment b = bb.get("B");
        Trapezoid trapRoot = new Trapezoid(b.p, b.q, t, b);
        SearchStructure ss = new SearchStructure(l, r, t, b, trapRoot);
        TrapezoidMap trapMap = new TrapezoidMap(ss, boundingList, trapRoot);

        for (Segment s : segments) {
            trapMap.segments.add(s);

            //Find the set ∆0,∆1,...,∆k of trapezoids in T properly intersected
            //by si.
            List<Trapezoid> intersecting = ss.followSegment(s);

            //Remove ∆0,∆1,...,∆k from T and replace
            //them by the new trapezoids that appear because of the insertion of si.
            trapMap.traps.removeAll(intersecting);

            if (intersecting.size() == 1) {
                handleOneIntersectingTrap(trapMap, s, intersecting, ss);
            } else {
                //TODO: Implement this
            }

        }

        return trapMap;
    }

    private static Map<String, Segment> getBoundingBox(List<Segment> segments, Face unbounded) {
        float bottom = Float.MAX_VALUE;
        float left = Float.MAX_VALUE;
        float right = Float.MIN_VALUE;
        float top = Float.MIN_VALUE;

        for (Segment s : segments) {
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
        Vertex tl = new Vertex(left - 1.0f, top + 1.0f, "c1");
        Vertex bl = new Vertex(left - 1.0f, bottom - 1.0f, "c2");
        Vertex br = new Vertex(right + 1.0f, bottom - 1.0f, "c3");
        Vertex tr = new Vertex(right + 1.0f, top + 1.0f, "c4");

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
    private static List<Segment> getSegments(List<HalfEdge> halfEdges) {
        List<HalfEdge> accountedFor = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        int segmentCount = 0;
        for (HalfEdge h : halfEdges) {
            if (!accountedFor.contains(h)) {
                Segment s = new Segment(segmentCount++, h, h.twin);
                accountedFor.add(h);
                accountedFor.add(h.twin);
                segments.add(s);
                h.segment = s;
                h.twin.segment = s;
            }
        }
        return segments;
    }

    private static Face getUnboundedFace(List<Face> faces) {
        for (Face f : faces) {
            if (f.outerComponent == null) return f;
        }
        return null;
    }

//    private static boolean endPointNotContainedInTrap(Vertex trapVertex, Vertex segEndPoint) {
//        if (trapVertex == segEndPoint) return true;
//        if (Math.abs(trapVertex.x - segEndPoint.x) < epsilon) return true;
//        return false;
//    }

    private static void handleOneIntersectingTrap(TrapezoidMap trapMap, Segment s, List<Trapezoid> intersecting, SearchStructure ss) {
        Trapezoid old = intersecting.get(0);
        Node parentOfReplace = ss.getParentNode(s, s.p, true);
        LeafNode toReplace = ss.segmentQueryNode(s, s.p, true);
        Node subRoot;

        if (old.leftp == s.p) {
            if (old.rightp == s.q) {
                // Both endpoints of s are already in T
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

                // Set left neighbors
                setLeftNeighbors_spOnVert(A, B, old, s);
                // Set right neighbors
                setRightNeighbors_sqOnVert(A, B, old, s);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqOnVert(A, B, old);

                //Set right neighbors of old traps
                setNeighborsForOldTraps_spOnVert(A, B, old);

                trapMap.traps.add(A);
                trapMap.traps.add(B);

                subRoot = new YNode(s);
                subRoot.lChild = new LeafNode(A);
                subRoot.rChild = new LeafNode(B);

            } else {
                // Left endpoint of s lies is already in T but not right endpoint
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

                // Add left neighbors
                setLeftNeighbors_spOnVert(A, B, old, s);
                C.leftNeighbors.add(B);
                C.leftNeighbors.add(A);

                // Add right neighbors
                A.rightNeighbors.add(C);
                B.rightNeighbors.add(C);
                C.rightNeighbors.addAll(old.rightNeighbors);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqNOTonVert(C, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spOnVert(A, B, old);

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
        } else {
            if (old.rightp == s.q) {
                // right endpoint of s is already in T but not left endpoint
                // Therefore create three trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s
                Trapezoid C; // Left of s

                Vertex[] right = setLeftRightp(old.rightp, s.p);

                Vertex leftp_A = s.p;
                Vertex leftp_B = s.p;
                Vertex leftp_C = old.leftp;
                Vertex rightp_A = right[0];
                Vertex rightp_B = right[1];
                Vertex rightp_C = s.q;

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);
                C = new Trapezoid(leftp_C, rightp_C, old.top, old.bottom);

                // Add left neighbors
                C.leftNeighbors.addAll(old.leftNeighbors);
                A.leftNeighbors.add(C);
                B.leftNeighbors.add(C);

                // Add right neighbors
                setRightNeighbors_sqOnVert(A, B, old, s);
                C.rightNeighbors.add(B);
                C.rightNeighbors.add(A);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqOnVert(A, B, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spNOTonVert(C, old);

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);

                subRoot = new XNode(s.p);
                subRoot.lChild = new LeafNode(C);
                Node si = new YNode(s);
                si.lChild = new LeafNode(A);
                si.rChild = new LeafNode(B);
                subRoot.rChild = si;
            } else {
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

                // Add left neighbors
                A.leftNeighbors.addAll(old.leftNeighbors);
                B.leftNeighbors.add(A);
                C.leftNeighbors.add(A);
                D.leftNeighbors.add(C);
                D.leftNeighbors.add(B);

                // Add right neighbors
                A.rightNeighbors.add(C);
                A.rightNeighbors.add(B);
                B.rightNeighbors.add(D);
                C.rightNeighbors.add(D);
                D.rightNeighbors.addAll(old.rightNeighbors);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqNOTonVert(D, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spNOTonVert(A, old);

                Node si = new YNode(s);
                si.lChild = new LeafNode(B);
                si.rChild = new LeafNode(C);

                Node qi = new XNode(s.q);
                qi.rChild = new LeafNode(D);
                qi.lChild = si;

                subRoot = new XNode(s.p);
                subRoot.lChild = new LeafNode(A);
                subRoot.rChild = qi;
            }
        }

        //Replacing root
        if (parentOfReplace == null) {
            ss.replaceAtRoot(subRoot);
        } else if (toReplace == parentOfReplace.lChild) {
            parentOfReplace.lChild = subRoot;
        } else {
            parentOfReplace.rChild = subRoot;
        }
    }

    private static Vertex[] setLeftRightp(Vertex old, Vertex sEndpoint) {
        Vertex A;
        Vertex B;
        if (old == sEndpoint) {
            A = B = sEndpoint;
        } else if (old.y > sEndpoint.y) {
            A = old;
            B = sEndpoint;
        } else {
            A = sEndpoint;
            B = old;
        }
        Vertex AB[] = {A, B};
        return AB;
    }

    private static boolean incident(Vertex v, Segment s) {
        HalfEdge start = v.incidentEdge;
        HalfEdge next = start;
        do {
            next = next.next;
            if (next.segment == s) {
                return true;
            }
        } while (start != next);
        return false;
    }

    private static void setLeftNeighbors_spOnVert(Trapezoid A, Trapezoid B, Trapezoid old, Segment s) {
        if (old.leftp == s.p) {
            if (old.leftNeighbors.size() == 1) {
                Trapezoid oln = old.leftNeighbors.get(0);
                if (!incident(s.p, old.top)) {
                    A.leftNeighbors.add(oln);
                }
                if (!incident(s.p, old.bottom)) {
                    B.leftNeighbors.add(oln);
                }
            } else if (old.leftNeighbors.size() == 2) {
                Trapezoid olnb = old.leftNeighbors.get(0);
                Trapezoid olnt = old.leftNeighbors.get(1);
                A.leftNeighbors.add(olnt);
                B.leftNeighbors.add(olnb);
            }
            return;
        }
        throw new Error("FUCK");
//        else if(old.leftp.y > s.p.y){
//            if(old.leftNeighbors.size() == 1){
//                Trapezoid oln = old.leftNeighbors.get(0);
//                A.leftNeighbors.add(oln);
//                B.leftNeighbors.add(oln);
//            }
//            else if(old.leftNeighbors.size() == 2){
//                Trapezoid olnb = old.leftNeighbors.get(0);
//                Trapezoid olnt = old.leftNeighbors.get(1);
//
//                A.leftNeighbors.add(olnb);
//                A.leftNeighbors.add(olnt);
//                B.leftNeighbors.add(olnb);
//            }
//        }
//        else{
//            if(old.leftNeighbors.size() == 1){
//                Trapezoid oln = old.leftNeighbors.get(0);
//                A.leftNeighbors.add(oln);
//                B.leftNeighbors.add(oln);
//            }
//            else if(old.leftNeighbors.size() == 2){
//                Trapezoid olnb = old.leftNeighbors.get(0);
//                Trapezoid olnt = old.leftNeighbors.get(1);
//
//               A.leftNeighbors.add(olnt);
//               B.leftNeighbors.add(olnb);
//               B.leftNeighbors.add(olnt);
//            }
//        }
    }

    private static void setRightNeighbors_sqOnVert(Trapezoid A, Trapezoid B, Trapezoid old, Segment s) {
        //Should always occur
        if (old.rightp == s.q) {
            if (old.rightNeighbors.size() == 1) {
                Trapezoid orn = old.rightNeighbors.get(0);
                if (!incident(s.q, old.top)) {
                    A.rightNeighbors.add(orn);
                }
                if (!incident(s.p, old.bottom)) {
                    B.rightNeighbors.add(orn);
                }
            } else if (old.rightNeighbors.size() == 2) {
                Trapezoid olnb = old.rightNeighbors.get(0);
                Trapezoid olnt = old.rightNeighbors.get(1);
                A.rightNeighbors.add(olnt);
                B.rightNeighbors.add(olnb);
            }
            return;
        }
        throw new Error("FUCK");
//        else if(old.rightp.y > s.q.y){
//            if(old.rightNeighbors.size() == 1){
//                Trapezoid orn = old.rightNeighbors.get(0);
//                A.rightNeighbors.add(orn);
//                B.rightNeighbors.add(orn);
//            }
//            else if(old.rightNeighbors.size() == 2){
//                Trapezoid ornb = old.rightNeighbors.get(0);
//                Trapezoid ornt = old.rightNeighbors.get(1);
//
//                A.rightNeighbors.add(ornb);
//                A.rightNeighbors.add(ornt);
//                B.rightNeighbors.add(ornb);
//            }
//        }
//        else{
//            if(old.rightNeighbors.size() == 1){
//                Trapezoid orn = old.rightNeighbors.get(0);
//                A.rightNeighbors.add(orn);
//                B.rightNeighbors.add(orn);
//            }
//            else if(old.rightNeighbors.size() == 2){
//                Trapezoid ornb = old.rightNeighbors.get(0);
//                Trapezoid ornt = old.rightNeighbors.get(1);
//
//                A.rightNeighbors.add(ornt);
//                B.rightNeighbors.add(ornb);
//                B.rightNeighbors.add(ornt);
//            }
//        }
//
    }

    public static void setNeighborsForOldTraps_spOnVert(Trapezoid A, Trapezoid B, Trapezoid old) {
        if (old.leftNeighbors.size() != 0) {
            if (old.leftNeighbors.size() == 1) {
                Trapezoid oln = old.leftNeighbors.get(0);
                if (oln.rightNeighbors.size() == 2) {
                    if (oln.rightNeighbors.get(0) == old) {
                        oln.rightNeighbors.set(0, B);
                    } else {
                        oln.rightNeighbors.set(1, A);
                    }
                } else {
                    throw new Error("I thought this was impossible");
                }
            } else {
                Trapezoid olnt = old.leftNeighbors.get(1);
                Trapezoid olnb = old.leftNeighbors.get(0);
                if (olnt.rightNeighbors.size() == 1) {
                    olnt.rightNeighbors.set(0, A);
                } else {
                    throw new Error("I thought this was impossible");
                }
                if (olnb.rightNeighbors.size() == 1) {
                    olnb.rightNeighbors.set(0, B);
                } else {
                    throw new Error("I Thought this was impossible");
                }
            }
        }
    }

    public static void setNeighborsForOldTraps_sqOnVert(Trapezoid A, Trapezoid B, Trapezoid old) {
        if (old.rightNeighbors.size() != 0) {
            if (old.rightNeighbors.size() == 1) {
                Trapezoid oln = old.rightNeighbors.get(0);
                if (oln.leftNeighbors.size() == 2) {
                    if (oln.leftNeighbors.get(0) == old) {
                        oln.leftNeighbors.set(0, B);
                    } else {
                        oln.leftNeighbors.set(1, A);
                    }
                } else {
                    throw new Error("I thought this was impossible");
                }
            } else {
                Trapezoid olnt = old.rightNeighbors.get(1);
                Trapezoid olnb = old.rightNeighbors.get(0);
                if (olnt.leftNeighbors.size() == 1) {
                    olnt.leftNeighbors.set(0, A);
                } else {
                    throw new Error("I thought this was impossible");
                }
                if (olnb.leftNeighbors.size() == 1) {
                    olnb.leftNeighbors.set(0, B);
                } else {
                    throw new Error("I Thought this was impossible");
                }
            }
        }
    }

    public static void setNeighborsForOldTraps_spNOTonVert(Trapezoid A, Trapezoid old) {

        if(old.leftNeighbors.size() == 2){
            Trapezoid olnt = old.leftNeighbors.get(1);
            Trapezoid olnb = old.leftNeighbors.get(0);

            if(olnt.rightNeighbors.size() == 2){
                if(olnt.rightNeighbors.get(0) == old){
                    olnt.rightNeighbors.set(0, A);
                }
                else{
                    olnt.rightNeighbors.set(1, A);
                }
            }
            else if(olnt.rightNeighbors.size() == 1){
                olnt.rightNeighbors.set(0, A);
            }

            if(olnb.rightNeighbors.size() == 2){
                if(olnb.rightNeighbors.get(0) == old){
                    olnb.rightNeighbors.set(0, A);
                }
                else{
                    olnb.rightNeighbors.set(1, A);
                }
            }
            else if(olnb.rightNeighbors.size() == 1){
                olnb.rightNeighbors.set(0, A);
            }
        }
        else if(old.leftNeighbors.size() == 1){
            Trapezoid oln = old.leftNeighbors.get(0);
            if(oln.rightNeighbors.size() == 2){
                if(oln.rightNeighbors.get(0) == old){
                    oln.rightNeighbors.set(0, A);
                }
                else{
                    oln.rightNeighbors.set(1, A);
                }
            }
            else if(oln.rightNeighbors.size() == 1){
                oln.rightNeighbors.set(0, A);
            }
        }
        else{
            // do nothing. Don't need to update anything
        }

    }

    public static void setNeighborsForOldTraps_sqNOTonVert(Trapezoid A, Trapezoid old) {

        if(old.rightNeighbors.size() == 2){
            Trapezoid olnt = old.rightNeighbors.get(1);
            Trapezoid olnb = old.rightNeighbors.get(0);

            if(olnt.leftNeighbors.size() == 2){
                if(olnt.leftNeighbors.get(0) == old){
                    olnt.leftNeighbors.set(0, A);
                }
                else{
                    olnt.leftNeighbors.set(1, A);
                }
            }
            else if(olnt.leftNeighbors.size() == 1){
                olnt.leftNeighbors.set(0, A);
            }

            if(olnb.leftNeighbors.size() == 2){
                if(olnb.leftNeighbors.get(0) == old){
                    olnb.leftNeighbors.set(0, A);
                }
                else{
                    olnb.leftNeighbors.set(1, A);
                }
            }
            else if(olnb.leftNeighbors.size() == 1){
                olnb.leftNeighbors.set(0, A);
            }
        }
        else if(old.rightNeighbors.size() == 1){
            Trapezoid oln = old.rightNeighbors.get(0);
            if(oln.leftNeighbors.size() == 2){
                if(oln.leftNeighbors.get(0) == old){
                    oln.leftNeighbors.set(0, A);
                }
                else{
                    oln.leftNeighbors.set(1, A);
                }
            }
            else if(oln.leftNeighbors.size() == 1){
                oln.leftNeighbors.set(0, A);
            }
        }
        else{
            // do nothing. Don't need to update anything
        }

    }
}
