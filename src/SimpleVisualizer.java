import java.util.*;
import java.util.List;

public class SimpleVisualizer {

    private static final int FPS = 30;
    private static final int FPT = 30;


    private Collection<Rail>[] adj;
    private List<RoutingRecord> record;
    private Map<Integer, Station> stations;
    private List<TrainSprite> sprites;

    public SimpleVisualizer(Collection<Rail>[] adj, Map<Integer, Station> stations, List<Train> trains, List<RoutingRecord> record) {
        this.adj = adj;
        this.stations = stations;
        this.record = record;

        sprites = new ArrayList<>(trains.size());

        CustomDraw.setCanvasSize(1200, 720);
        CustomDraw.setXscale(-1, 21);
        CustomDraw.setYscale(-1, 15);
        renderLoop();
    }

    private void drawStations() {
        for (Station station : stations.values()) {
            CustomDraw.text(station.location.x, station.location.y, station.name);
        }
    }

    private void drawEdges() {
        CustomDraw.setPenRadius(0.003);
        for (Collection<Rail> rails : adj) {
            for (Rail rail : rails) {
                Location kore = stations.get(rail.kore).location;
                Location sore = stations.get(rail.sore).location;
                CustomDraw.line(kore.x, kore.y, sore.x, sore.y);
            }
        }
    }

    private void renderLoop() {
        CustomDraw.show(0);

        LinkedList<RoutingRecord> recordRemoveList = new LinkedList<>();
        LinkedList<TrainSprite> spriteRemoveList = new LinkedList<>();

        for (int frameCount = 1, now = 0; !record.isEmpty() || !sprites.isEmpty(); frameCount++) {
            System.out.println(now);
            CustomDraw.clear();
            CustomDraw.setPenColor(CustomDraw.BLACK);
            drawStations();
            drawEdges();

            CustomDraw.setPenColor(CustomDraw.RED);

            // add new sprites
            for (RoutingRecord r : record) {
                if (r.timeStart == now) {
                    sprites.add(new TrainSprite(r.trainName,
                            stations.get(r.from).location,
                            stations.get(r.to).location,
                            r.timeStart,
                            r.timeEnd));
                    recordRemoveList.add(r);
                }
            }

            // process existing sprites
            for (TrainSprite s : sprites) {
                if (s.timeEnd == now) {
                    spriteRemoveList.add(s);
                } else {
                    s.draw(now);
                }
            }

            CustomDraw.show(80);
            now++;

            // clean up
            for (RoutingRecord rec : recordRemoveList) {
                record.remove(rec);
            }

            for (TrainSprite sp : spriteRemoveList) {
                sprites.remove(sp);
            }
        }

    }


    private class TrainSprite {
        String name;
        Location from;
        double currX;
        double currY;
        int timeStart;
        int timeEnd;
        double rangeX;
        double rangeY;

        TrainSprite(String nm, Location f, Location t, int tS, int tE) {
            name = nm;
            from = f;
            timeStart = tS;
            timeEnd = tE;
            currX = f.x;
            currY = f.y;
            rangeX = t.x - f.x;
            rangeY = t.y - f.y;
        }

        void draw(int now) {
            double ratio = ((double) now - timeStart) / (timeEnd - timeStart);
            currX = from.x + ratio * rangeX;
            currY = from.y + ratio * rangeY;
            CustomDraw.text(currX, currY, name);
        }
    }
}
