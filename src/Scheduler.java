import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class Scheduler {

    public final Path outputFile;
    public final Path inputFile;

    private RoutingStrategy strategy;
    private Router router;
    private Map<Integer, Station> stations;     // Map from name to index
    private int now;   // The master clock

    private List<Train> updateQueue;   // The order for updating train states
    private List<Train> trains;
    private Collection<Train> removeList;       // Trains to be removed after this clock tick
    private List<RoutingRecord> routings;      // Actual routing records
    private double optimalCost;
    private double minimumCost;


    public Scheduler(Path input, Path output, RoutingStrategy strat) {
        removeList = new LinkedList<>();
        routings = new LinkedList<>();
        inputFile = input;
        outputFile = output;
        strategy = strat;
        init();
        System.out.printf("%s loaded successfully%n", input.toString());
        now = 0;
        minimumCost = 0.0;
    }


    private void initJson() {

    }

    private void init() {
        try (Scanner in = new Scanner(inputFile)) {
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

            System.out.println("stations loaded");

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

            System.out.println("graph loaded");

            // Read trains
            int trainCount = in.nextInt();

            updateQueue = new LinkedList<>();
            trains = new LinkedList<>();

            for (int i = 0; i < trainCount; i++) {
                String name = in.next();
                int departTime = in.nextInt();
                int from = in.nextInt();
                int to = in.nextInt();
                TrainType type = TrainType.valueOf(in.next());

                Train newTrain = Factory.newTrain(strategy,
                        name,
                        departTime,
                        from,
                        to,
                        type,
                        in.nextDouble(),
                        in.nextDouble(),
                        in.nextDouble(),
                        in.nextDouble(),
                        this);

                updateQueue.add(newTrain);
                trains.add(newTrain);
                newTrain.setPath(router.shortest(from, to));
            }
            System.out.println("trains loaded");
            calculateOptimalCost();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void calculateOptimalCost() {
        for (Train train : updateQueue) {
            double shortestPathLength = router.pathTotalLength(train.path) + train.path.size() * train.LEN;
            optimalCost += shortestPathLength * train.CPM;
        }
    }


    public void run() {
        updateQueue.sort(Train.comparator());
        ////////////////////
//        System.out.println("------------------------------");
        ////////////////////
        while (updateQueue.size() > 0) {

            for (Train trainToRemove : removeList) {
                updateQueue.remove(trainToRemove);
            }
            removeList.clear();

            //////////////////////
//            System.out.printf("t= %d queue= [%n", now);
//            for (int i = 0; i < updateQueue.size(); i++) {
//                System.out.printf("\t%-15s %8s%n", updateQueue.get(i).NAME, updateQueue.get(i).state);
//            }
//            System.out.println("]");
            /////////////////////

            for (Train t : updateQueue) {
                if (t.DEPART_TIME <= now) {
                    t.update(now);
                }
            }
            now++;

            ///////////////////
//            System.out.println("------------------------------");
            ///////////////////
        }

//        printOutput();
        drawOutput();
    }


    public void done(int time, Train t) {
        minimumCost += t.cost;
        removeList.add(t);
    }


    public void moved(int timeStart, int timeEnd, Train t, int from, int to) {
        routings.add(new RoutingRecord(timeStart, timeEnd, t, from, to));
    }


    private void printOutput() {
        routings.sort(RoutingRecord.comparator());
        for (RoutingRecord re : routings) {
            System.out.printf("%3d %-15s %3d -> %3d%n", re.timeStart, re.train.NAME, stations.get(re.from).index, stations.get(re.to).index);
        }
        System.out.println();
        System.out.printf("    duration: %d%n", now);
        System.out.printf(" actual cost: %16.2f%n", minimumCost);
        System.out.printf("minimum cost: %16.2f%n", optimalCost);
    }

    private void drawOutput() {
        SimpleVisualizer visual = new SimpleVisualizer(router.adj, stations, trains, routings);
    }

}
