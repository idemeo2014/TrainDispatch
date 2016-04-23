import java.util.*;


public class Scheduler {

    private Router router;
    private Map<Integer, Station> stations;        // Map from name to index
    private SortedMap<String, Train> updateQueue;               // The order for updating train states
    private Collection<String> removeList;          // Trains to be removed after this clock tick
    private List<RoutingRecord> routings;    // Actual routing records
    private double optimalCost;
    private double actualCost;
    private int now;                               // The master clock
    private boolean toAnimate;


    public Scheduler(Router rt, Map<Integer, Station> sts, List<Train> uq) {
        now = 0;
        actualCost = 0.0;
        router = rt;
        stations = sts;
        removeList = new LinkedList<>();
        routings = new LinkedList<>();

        updateQueue = new TreeMap<>();
        for (Train train : uq) {
            updateQueue.put(train.namae, train);
        }
    }

    public void calculateOptimalCost() {
        optimalCost = 0.0;
        for (Train train : updateQueue.values()) {
            double shortestPathLength = router.pathTotalLength(train.path) + (train.path.size()-1) * train.length;
            optimalCost += shortestPathLength * train.CPM;
        }
    }

    public void runSimulation() {
        while (updateQueue.size() > 0) {
            removeTrains();
            updateTrains();
            now++;
        }

         // printOutput();
    }

    private void removeTrains() {
        for (String trainToRemove : removeList) {
            updateQueue.remove(trainToRemove);
        }
        removeList.clear();
    }

    private void updateTrains() {
        for (Train t : updateQueue.values()) {
            if (t.departTime <= now) t.update(now);
        }
    }

    public void runAnimation(String strategyName, int realTimeDuration) {
        (new Thread(new CustomDraw(router.adj, stations, routings, now, actualCost, optimalCost, strategyName, realTimeDuration))).start();
    }

    public void done(Train t) {
        actualCost += t.cost;
        removeList.add(t.namae);
    }

    public void moved(int timeStart, int timeEnd, Train t, int from, int to) {
        routings.add(new RoutingRecord(timeStart, timeEnd, t.namae, from, to));
    }

    public double getOptimalCost() {
        return optimalCost;
    }

    public double getActualCost() {
        return actualCost;
    }

    private void printOutput() {
        routings.sort(RoutingRecord.comparator());
        for (RoutingRecord re : routings) {
            System.out.printf("%3d %-15s %3d -> %3d%n", re.timeStart, re.trainName, stations.get(re.from).index, stations.get(re.to).index);
        }
        System.out.println();
        System.out.printf("    duration: %d%n", now);
        System.out.printf(" actual cost: %16.2f%n", actualCost);
        System.out.printf("minimum cost: %16.2f%n", optimalCost);
    }

}
