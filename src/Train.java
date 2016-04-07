import java.util.Comparator;
import java.util.List;

abstract class Train implements Comparable<Train> {

    // PHYSICAL PROPERTIES
    public final String NAME;
    public final int    DEPART_TIME;        // Moment the train sets off
    public final int FROM_IND;
    public final int TO_IND;
    public final double SPEED;         // mph
    public final double LEN;           // Length
    public final double CPM;           // cost per mile
    public final double CPT;           // cost per idle tick
    public final TrainType TYPE;       // train category


    // Cumulative cost of this train
    protected double cost = 0.0;

    // The path for the train to follow
    protected List<Station> path;

    // Previous station traveled to
    protected Station previousStation;

    // The train will not update until this tick
    protected int sitUntil = 0;

    protected TrainState state;

    // The scheduler to report to
    public final Scheduler BOSS;


    public Train(String name, int departTime, int from, int to, TrainType traintype, double trainLen, double speed, double costPerMile, double costPerIdleTick, Scheduler boss) {
        NAME = name;
        DEPART_TIME = departTime;
        FROM_IND = from;
        TO_IND = to;
        TYPE = traintype;
        LEN = trainLen;
        SPEED = speed;
        CPM = costPerMile;
        CPT = costPerIdleTick;
        BOSS = boss;
    }


    /**
     * Core method for Train.
     * Give this train a syncing clock for taking actions according to its state.
     * @param now current master clock timeStart
     */
    public abstract void update(int now);


    public void addCost(int time, double distance) {
        cost += CPT * time;
        cost += CPM * distance;
    }

    public void setSit(int until) {
        sitUntil = until;
    }

    public void setPath(List<Station> newPath) {
        path = newPath;
    }

    public void setState(TrainState st) {
        state = st;
    }

    public List<Station> path() {
        return path;
    }


    // COMPARISON RELATED
    @Override
    public int compareTo(Train b) {
        return (int) (b.CPT + b.TYPE.timeCost() - this.CPT - this.TYPE.timeCost());
    }

    public static Comparator<Train> comparator() {
        return new TrainPriorityComparator();
    }

    protected static class TrainPriorityComparator implements Comparator<Train> {
        /**
         * Trains with higher priorities are "smaller" when being compared.
         */
        @Override
        public int compare(Train a, Train b) {
            return a.compareTo(b);
        }
    }

}
