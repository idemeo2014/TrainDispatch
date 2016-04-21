import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class RandTrainGenerator {

    // context
    private static final Random rgen = new Random();
    private int numVertices;
    private int maxDegVxInd;

    // params
    private int numTrains;          // number of trains
    private int timeFrame;          // time limit for all trains to depart
    private double burst;           // number of trains to depart at the same time
    private double crowdedness;     // number of trains to depart at the same station
    private double timeSense;       // how valuable is time
    private double morePTrain;      // passenger/freight train ratio
    private double speedVariance;   // how differed are trains' speeds
    private RoutingStrategy strategy;  // for creating trains
    private int sentTrains;

    public RandTrainGenerator(int nv, int maxDegInd, RoutingStrategy st) {
        numVertices = nv;
        maxDegVxInd = maxDegInd;
        strategy = st;
        numTrains = 150;
        timeFrame = 20;
        burst = 0;
        timeSense = 0;   // controls avg for CPT
        morePTrain = 0;
        speedVariance = 0;
        sentTrains = 0;
    }

    public List<Train> getSchedule() {
        List<Train> trains = new LinkedList<>();

        for (int i = 0; i < timeFrame; i++) {

            for (int t = 0; t < nextNumTrains(); t++) {
                int nextSourceVal = nextSource();
                trains.add(Factory.newTrain(strategy, Integer.toHexString(sentTrains++),
                    nextDepart(),  // uniform. [timeFrame] controls range
                    nextSourceVal,  // normal, clustered around vertex with max degree. [crowdedness] controls stdev
                    nextDest(nextSourceVal),    // uniform
                    nextTrainType(),  // uniform. [morePTrain] controls cutoff values
                    nextTrainLen(),  // uniform. normal, around an empirical value
                    nextSpeed(),  // normal, flat, based on empirical value.
                    nextCpm(),  //
                    nextCpt()));  //
            }
        }

        return trains;
    }

    private int nextNumTrains() {
        return 0;
    }

    private int nextDepart() {
        return uniform(0, timeFrame);
    }

    private int nextSource() {
        return uniform(0, numVertices);
    }

    private int nextDest(int sourceInd) {
        int candidate = uniform(0, numVertices);
        while (candidate == sourceInd) {
            candidate = uniform(0, numVertices);
        }
        return candidate;
    }

    private double nextSpeed() {
        return 100.0;
    }

    private double nextCpm() {
        return 20.0;
    }

    private double nextCpt() {
        return 20.0;
    }

    private double nextTrainLen() {
        return 1.0;
    }

    private TrainType nextTrainType() {
        double v = rgen.nextDouble();
        if (v > morePTrain) return TrainType.P;
        else                return TrainType.F;
    }


    public static double normal(double avg, double stdev) {
        return rgen.nextGaussian() * stdev + avg;
    }

    public static int uniform(int min, int max) {
        return rgen.nextInt(max - min) + min;
    }


    /*
     * Setters
     */

    public void setNumTrains(int nt) {
        numTrains = nt;
    }

    public void setTimeFrame(int tf) {
        timeFrame = tf;
    }

    public void setBurst(double b) {
        burst = b;
    }

    public void setTimeSensitivity(double ts) {
        timeSense = ts;
    }

    public void setCompositionRatio(double cr) {
        morePTrain = cr;
    }

    public void setSpeedVar(double sv) {
        speedVariance = sv;
    }

    public void setCrowdedness(double crd) {
        crowdedness = crd;
    }
}
