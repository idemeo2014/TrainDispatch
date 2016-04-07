import java.util.Comparator;

/**
 * Record for a single routing
 */
class RoutingRecord {

    final int timeStart;
    final int timeEnd;
    final Train train;
    final int from;
    final int to;


    public RoutingRecord(int timeStart, int timeEnd, Train t, int from, int to) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.train = t;
        this.from = from;
        this.to = to;
    }

    public static Comparator<RoutingRecord> comparator() {
        return new RecordTimeComparator();
    }

    private static class RecordTimeComparator implements Comparator<RoutingRecord> {
        @Override
        public int compare(RoutingRecord o1, RoutingRecord o2) {
            return o1.timeStart - o2.timeStart;
        }
    }

}
