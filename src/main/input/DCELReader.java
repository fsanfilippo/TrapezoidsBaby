package main.input;

import main.structures.DCEL.DCEL;
import main.structures.DCEL.Face;
import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;

import java.util.*;

public class DCELReader {

    private List<Vertex> vertices = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();
    private List<HalfEdge> halfEdges = new ArrayList<>();

    private Scanner fileScanner;
    private Map<Vertex, String> incidentHalfEdge = new HashMap<>();
    private Map<Face, String> outerComponents = new HashMap<>();
    private Map<Face, String> innerComponents = new HashMap<>();
    private Map<HalfEdge, List<String>> twinNextPrev = new HashMap<>();

    private Map<String, HalfEdge> halfEdgeMap = new HashMap<>();
    private Map<Integer, Vertex> vertexMap = new HashMap<>();
    private Map<Integer, Face> faceMap = new HashMap<>();


    public DCELReader(Scanner fileScanner){
        this.fileScanner = fileScanner;
    }


    public DCEL readDCEL(){

        while(fileScanner.hasNextLine()){
            String line = fileScanner.nextLine();
            if(line.isEmpty()){
                continue;
            }
            byte b = line.getBytes()[0];
            switch(b) {
                case ('v'): {
                    readVertex(line);
                    break;
                }
                case ('f'): {
                    readFace(line);
                    break;
                }
                case ('e'): {
                    readHalfEdge(line);
                    break;
                }
                default: {
                    System.out.println("Malformed Input!!: " + line);
                }
            }
        }
        linkVertices();
        linkFaces();
        linkHalfEdges();

        return new DCEL(vertices, halfEdges, faces);
    }

    private void readVertex(String line){
        Scanner lineScanner = new Scanner(nonDigitsToBlanks(line));
        int no = lineScanner.nextInt();

        float x = lineScanner.nextFloat();
        float y = lineScanner.nextFloat();

        Vertex v = new Vertex(x, y, no);

        int e1 = lineScanner.nextInt();
        int e2 = lineScanner.nextInt();

        //record the incidentEdge like this because we haven't made the halfEdge record yet
        incidentHalfEdge.put(v, e1 + " " + e2);

        //Map vertices to it no. so we can get it later
        vertexMap.put(no, v);

        vertices.add(v);
    }

    private void readFace(String line){
        Scanner lineScanner = new Scanner(line);

        String fName = lineScanner.next();
        int faceNo = Integer.parseInt(fName.substring(1));
        Face f = new Face(fName);

        Scanner componentScanner;

        String outerComponent = lineScanner.next();
        if(!outerComponent.equals("nil")){
             componentScanner = new Scanner(nonDigitsToBlanks(outerComponent));
             int e1 = componentScanner.nextInt();
             int e2 = componentScanner.nextInt();
             outerComponents.put(f, e1 + " " + e2);
        }

        String innerComponent = lineScanner.next();
        if(!innerComponent.equals("nil")){
            componentScanner = new Scanner(nonDigitsToBlanks(innerComponent));
            int e1 = componentScanner.nextInt();
            int e2 = componentScanner.nextInt();
            innerComponents.put(f, e1 + " " + e2);
        }

        faceMap.put(faceNo, f);
        faces.add(f);
    }

    private void readHalfEdge(String line){
        String stripped = nonDigitsToBlanks(line);
        Scanner lineScanner = new Scanner(stripped);

        int e1 = lineScanner.nextInt();
        int e2 = lineScanner.nextInt();

        int originNo = lineScanner.nextInt();

        int twinE1 = lineScanner.nextInt();
        int twinE2 = lineScanner.nextInt();

        int faceNo = lineScanner.nextInt();

        int nextE1 = lineScanner.nextInt();
        int nextE2 = lineScanner.nextInt();

        int prevE1 = lineScanner.nextInt();
        int prevE2 = lineScanner.nextInt();

        Vertex origin = vertexMap.get(originNo);
        Face incident = faceMap.get(faceNo);

        HalfEdge h = new HalfEdge(origin, incident, e1, e2);

        String twin = twinE1 + " " + twinE2;
        String next = nextE1 + " " + nextE2;
        String prev = prevE1 + " " + prevE2;

        List<String> assoc = new ArrayList<>();
        assoc.add(twin);
        assoc.add(next);
        assoc.add(prev);

        twinNextPrev.put(h, assoc);
        halfEdges.add(h);
        halfEdgeMap.put(e1 + " " + e2, h);

    }

    private void linkVertices(){
        for(Vertex v: vertices){
            String incident = incidentHalfEdge.get(v);
            v.incidentEdge  = halfEdgeMap.get(incident);
        }
    }

    private void linkFaces(){
        for(Face f: faces){
            String inner = innerComponents.get(f);
            String outer = outerComponents.get(f);

            if(inner != null){
                f.innerComponent = halfEdgeMap.get(inner);
            }
            if(outer != null){
                f.outerComponent = halfEdgeMap.get(outer);
            }
        }
    }

    private void linkHalfEdges(){
        for(HalfEdge h: halfEdges){
            List<String> tnp = twinNextPrev.get(h);
            h.twin = halfEdgeMap.get(tnp.get(0));
            h.next = halfEdgeMap.get(tnp.get(1));
            h.prev = halfEdgeMap.get(tnp.get(2));
        }
    }
    private String nonDigitsToBlanks(final CharSequence input){
        final StringBuilder sb = new StringBuilder( input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
            else{
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
