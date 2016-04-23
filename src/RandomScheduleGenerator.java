
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class RandomScheduleGenerator {

    // Some real-life constants
    static final double AVG_SPEED = 100.0;
    static final double SPEED_STDEV = 5.0;
    static final double AVG_CPM= 5.0;
    static final double CPM_STDEV= 5.0;
    static final double COST_PER_MILE = 5.0;

    // context
    private static Random rgen = new Random();  // @TODO delete 'static'
    private RoutingStrategy strategy;  // used for creating trains
    private int numVertices;
    private int numTrains;
    private long customSeed;
    private int timeFrame;       // time limit for all trains to depart

    // generator params, all are the same as the slider values
    private int burst;           // number of trains to depart at the same time
    private int crowdedness;     // number of trains to depart at the same station
    private int timeSense;       // how valuable is time
    private int morePTrain;      // passenger/freight train ratio
    private int speedVariance;   // how differed are trains' speeds

    // value trackers
    private int sentTrains;         // number of trains already dispatched
    private int lastChosenDepartTime;
    private int lastChosenSourceInd;

    public RandomScheduleGenerator(int nv, int nt, int tf, RoutingStrategy st, long cuseed) {
        // context
        strategy = st;
        numVertices = nv;
        numTrains = nt;
        customSeed = cuseed;
        timeFrame = tf;

        // params
        timeFrame = 20;
        burst = 0;
        crowdedness = 0;
        timeSense = 0;
        morePTrain = 0;
        speedVariance = 0;

        // variable init
        sentTrains = 0;
        lastChosenDepartTime = 0;
        rgen = new Random(cuseed);
    }

    public List<Train> getSchedule() {
        List<Train> trains = new LinkedList<>();

        while (sentTrains < numTrains) {
            int nextSourceVal = nextSource();
            trains.add(Factory.newTrain(strategy, Integer.toHexString(sentTrains++),
                nextDepart(),   // uniform. [timeFrame] controls range. [burst] controls clustering
                nextSourceVal,  // normal, clustered around vertex with max degree. [crowdedness] controls stdev
                nextDest(nextSourceVal),    // uniform, excludes source index
                nextTrainType(),  // uniform. [morePTrain] controls cutoff values
                nextTrainLen(),   // uniform. normal, around an empirical value
                nextSpeed(),      // normal, flat, based on empirical value.
                nextCpm(),  //
                nextCpt()));  //
        }

        return trains;
    }

    private int nextDepart() {
        int candidate = uniform(0, 256) <= burst ? lastChosenDepartTime : uniform(0, timeFrame + 1);
        lastChosenDepartTime = candidate;
        return candidate;
    }

    private int nextSource() {
        int candidate = uniform(0, 256) <= crowdedness ? lastChosenSourceInd : uniform(0, numVertices);
        lastChosenSourceInd = candidate;
        return candidate;
    }

    private int nextDest(int sourceInd) {
        int candidate = uniform(0, numVertices);
        while (candidate == sourceInd) {
            candidate = uniform(0, numVertices);
        }
        return candidate;
    }

    private double nextSpeed() {
        return normal(AVG_SPEED, SPEED_STDEV);
    }

    private double nextCpm() {
        return normal(AVG_CPM, SPEED_STDEV);
    }

    private double nextCpt() {
        return timeSense;
    }

    private double nextTrainLen() {
        return normal(1.0, 0.05);
    }

    private TrainType nextTrainType() {
        double v = rgen.nextDouble();
        if (v > morePTrain) return TrainType.P;
        else                return TrainType.F;
    }

    /**
     * Normal dist with values outside 3 sigma cut off
     */
    public static double normal(double avg, double stdev) {
        double candidate = rgen.nextGaussian() * stdev + avg;
        while  (candidate < avg - 3 * stdev || candidate > avg + 3 * stdev) {
            candidate = rgen.nextGaussian() * stdev + avg;
        }
        return candidate;
    }

    /**
     * Uniform int between min (inclusive) and max (exclusive)
     */
    public static int uniform(int min, int max) {
        return rgen.nextInt(max - min) + min;
    }


    //////// Setters for generator params ////////

    public void setBurst(int b) {
        burst = b;
    }

    public void setCrowdedness(int crd) {
        crowdedness = crd;
    }

    public void setTimeSensitivity(int ts) {
        timeSense = ts;
    }

    public void setCompositionRatio(int cr) {
        morePTrain = cr;
    }

    public void setSpeedVar(int sv) {
        speedVariance = sv;
    }

}
