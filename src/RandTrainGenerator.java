import java.util.List;

class RandTrainGenerator {

    private static RandTrainGenerator gen = new RandTrainGenerator();
    private RandTrainGenerator() {}

    // the graph
    private static Router router;

    // params
    private static int numTrains;
    private static int timeFrame;
    private static double burst;
    private static double timeSense;
    private static double morePTrain;
    private static double speedVariance;


    public List<Train> getSchedule() {
        return null;
    }

    public static void setRequiredParams(Router rt, int nT, int tF) {
        router = rt;
        numTrains = nT;
        timeFrame = tF;
    }

    public static void setBurst(double b) {
        burst = b;
    }

    public static void setTimeSensitivity(double ts) {
        timeSense = ts;
    }

    public static void setCompositionRatio(double cr) {
        morePTrain = cr;
    }

    public static void setSpeedVar(double sv) {
        speedVariance = sv;
    }
}
