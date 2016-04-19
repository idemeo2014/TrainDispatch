import java.io.InputStream;
import java.util.*;


public class Scheduler {

    private InputStream graphIn;
    private InputStream trainIn;

    private RoutingStrategy strategy;
    private Router router;
    private Map<Integer, Station> stations;        // Map from name to index
    private List<Train> updateQueue;               // The order for updating train states
    private Collection<Train> removeList;          // Trains to be removed after this clock tick
    private LinkedList<RoutingRecord> routings;    // Actual routing records
    private double optimalCost;
    private double minimumCost;
    private int now;                               // The master clock


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
        run();
        printOutput();
    }

    public Scheduler(InputStream graphInput, List<Train> trains) {
        removeList = new LinkedList<>();
        routings = new LinkedList<>();
        graphIn = graphInput;
        now = 0;
        minimumCost = 0.0;
        loadGraph();
        updateQueue = trains;  // these trains are not boostraped yet
        bootstrapTrains();
        calculateOptimalCost();
        run();
        printOutput();
    }

    private void calculateOptimalCost() {
        optimalCost = 0.0;
        for (Train train : updateQueue) {
            double shortestPathLength = router.pathTotalLength(train.path) + train.path.size() * train.length;
            optimalCost += shortestPathLength * train.CPM;
        }
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

    
    private void loadTrains() {
        Scanner scin = new Scanner(trainIn);
        int trainCount = scin.nextInt();

        updateQueue = new ArrayList<>(trainCount);

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
                    scin.nextDouble());

            updateQueue.add(newTrain);
            newTrain.setPath(router.shortest(from, to));
            newTrain.setBoss(this);
        }

        scin.close();
    }


    private void bootstrapTrains() {
        for (Train train : updateQueue) {
            train.setPath(router.shortest(train.fromInd, train.toInd));
            train.setBoss(this);
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

        // Start animating in a new thread
        (new Thread(new CustomDraw(router.adj, stations, routings))).start();
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
