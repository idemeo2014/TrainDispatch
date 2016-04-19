import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Scheduler {

    private InputStream graphIn;
    private InputStream trainIn;

    private RoutingStrategy strategy;
    private Router router;
    private Map<Integer, Station> stations;        // Map from name to index
    private int now;                               // The master clock
    private List<Train> updateQueue;               // The order for updating train states
    private List<Train> trains;
    private Collection<Train> removeList;          // Trains to be removed after this clock tick
    private LinkedList<RoutingRecord> routings;    // Actual routing records
    private double optimalCost;
    private double minimumCost;


    public Scheduler(InputStream graphInput, InputStream trainInput, RoutingStrategy strat) {
        removeList = new LinkedList<>();
        routings = new LinkedList<>();
        graphIn = graphInput;
        trainIn = trainInput;
        strategy = strat;
        now = 0;
        minimumCost = 0.0;
        loadGraph();
        loadTrains();
        calculateOptimalCost();
    }

    private void loadGraph() {
        Scanner in = new Scanner(graphIn);
        int stationCount = in.nextInt();
        
        stations = new HashMap<>(stationCount);
        router = new Router(stationCount, stations);
        
        // Read stations
        for (int i = 0; i < stationCount; i++) {
            int nextInd = in.nextInt();
            String stationName = in.next();
            Location loc = new Location(in.nextDouble(), in.nextDouble());
            stations.put(nextInd, Factory.newStation(strategy, stationName, nextInd, loc, router));
        }
        
        // Read edges
        int edgeCount = in.nextInt();
        for (int i = 0; i < edgeCount; i++) {
            int from = in.nextInt();
            int to   = in.nextInt();
            double weight = in.nextDouble();
        
            if (weight < 0) { // calculate according to coordinates
                Station fromStat = stations.get(from);
                Station toStat   = stations.get(to);
                double fX = fromStat.location.x;
                double tX = toStat.location.x;
                double fY = fromStat.location.y;
                double tY = toStat.location.y;
        
                weight = Math.sqrt(Math.pow(fX-tX,2) + Math.pow(fY-tY,2));
            }
        
            router.addEdge(from, to, weight);
        }
        
        // Set adj list for each station
        for (Station s : stations.values()) {
            s.setAdjMap(router.getAdjMap(s.index));
        }

        in.close();
    }

    
    public void loadTrains() {
        Scanner scin = new Scanner(trainIn);
        int trainCount = scin.nextInt();

        updateQueue = new LinkedList<>();
        trains = new LinkedList<>();

        for (int i = 0; i < trainCount; i++) {
            String name = scin.next();
            int departTime = scin.nextInt();
            int from = scin.nextInt();
            int to = scin.nextInt();
            TrainType type = TrainType.valueOf(scin.next());

            Train newTrain = Factory.newTrain(strategy,
                    name,
                    departTime,
                    from,
                    to,
                    type,
                    scin.nextDouble(),
                    scin.nextDouble(),
                    scin.nextDouble(),
                    scin.nextDouble(),
                    this);

            updateQueue.add(newTrain);
            trains.add(newTrain);
            newTrain.setPath(router.shortest(from, to));
        }

        scin.close();
    }


    private void calculateOptimalCost() {
        for (Train train : updateQueue) {
            double shortestPathLength = router.pathTotalLength(train.path) + train.path.size() * train.length;
            optimalCost += shortestPathLength * train.CPM;
        }
    }


    public void run() {
        updateQueue.sort(Train.comparator());

        while (updateQueue.size() > 0) {
            for (Train trainToRemove : removeList) {
                updateQueue.remove(trainToRemove);
            }
            removeList.clear();
            for (Train t : updateQueue) {
                if (t.departTime <= now) t.update(now);
            }
            now++;
        }


        (new Thread(new CustomDraw(router.adj, stations, trains, routings))).start();
    }


    public void done(int time, Train t) {
        minimumCost += t.cost;
        removeList.add(t);
    }


    public void moved(int timeStart, int timeEnd, Train t, int from, int to) {
        routings.add(new RoutingRecord(timeStart, timeEnd, t.namae, from, to));
    }


    private void printOutput() {
        routings.sort(RoutingRecord.comparator());
        for (RoutingRecord re : routings) {
            System.out.printf("%3d %-15s %3d -> %3d%n", re.timeStart, re.trainName, stations.get(re.from).index, stations.get(re.to).index);
        }
        System.out.println();
        System.out.printf("    duration: %d%n", now);
        System.out.printf(" actual cost: %16.2f%n", minimumCost);
        System.out.printf("minimum cost: %16.2f%n", optimalCost);
    }

}
