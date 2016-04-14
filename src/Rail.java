import java.util.Deque;
import java.util.LinkedList;


public class Rail {

    private static final double SAFE_DISTANCE = 0.15;  // typical braking distance
    public final int kore;        // station with lower index
    public final int sore;        // station with higher index
    public final double railLen;  // edge weight
    public Deque<QueueRecord> queue;  // Trains queued for this rail.

    private int lastUpdated = 0;


    public Rail(int kore, int sore, double railLen) {
        // make sure kore has smaller index than sore
        if (kore > sore) {
            int tmp = kore;
            kore = sore;
            sore = tmp;
        }

        this.kore = kore;
        this.sore = sore;
        this.railLen = railLen;
        this.queue = new LinkedList<>();
    }

    public int either() {
        return kore;
    }

    public int other(int one) {
        if      (one == kore) return sore;
        else if (one == sore) return kore;
        else throw new RuntimeException();
    }


    /**
     * Enqueue a train for waiting to use this rail.
     * Add timeStart and travel cost to the train.
     * @param fromStation from which station
     * @param t the sender train
     * @param now timeStart that the train decided to join the queue
     * @return unlock timeStart after this enqueue operation
     */
    public int enqueue(int fromStation, Train t, int now) {
        update(now);

        //////////////////////
        // System.out.printf("%d rail %d - %d %s%n", now, kore, sore, queue.toString());
        //////////////////////

        int travelTime = timeToTravel(t.LEN, t.SPEED);

        if (queue.isEmpty()) {
            int timeArrival = now + travelTime;
            queue.addLast(new QueueRecord(t.SPEED, now, timeArrival, fromStation == kore));
            t.addCost(0, railLen + t.LEN);
            t.BOSS.moved(now, queue.getLast().unlockTime, t, fromStation, other(fromStation));
        } else {
            QueueRecord lastRecord = queue.peekLast();
            int lastUnlockTime = lastRecord.unlockTime;
            boolean lastWasUpward = lastRecord.upward;

            // the same direction as last in queue
            if ((fromStation == kore && lastWasUpward) || (fromStation == sore && !lastWasUpward)) {
                int safeTime = lastRecord.departTime + (int) Math.ceil(SAFE_DISTANCE / lastRecord.speed);
                int wait = safeTime - now;
                if (wait <= 0) {
                    queue.addLast(new QueueRecord(t.SPEED, now, now + travelTime, fromStation == kore));
                    t.addCost(0, railLen + t.LEN);
                    t.BOSS.moved(now, queue.getLast().unlockTime, t, fromStation, other(fromStation));
                } else {
                    queue.addLast(new QueueRecord(t.SPEED, safeTime, safeTime + travelTime, fromStation == kore));
                    t.addCost(wait, railLen + t.LEN);
                    t.BOSS.moved(safeTime, queue.getLast().unlockTime, t, fromStation, other(fromStation));
                }
            } else {  // opposite direction
                lastUnlockTime = Math.abs(lastUnlockTime);
                queue.addLast(new QueueRecord(t.SPEED, now, lastUnlockTime + travelTime, fromStation == kore));
                t.addCost(lastUnlockTime - now, railLen + t.LEN);
                t.BOSS.moved(lastUnlockTime, queue.getLast().unlockTime, t, fromStation, other(fromStation));
            }

        }

        return queue.getLast().unlockTime;
    }


    private void handle(Train t, int now, int unlock, boolean upward, int waitTime, int tripStart, int tripEnd, int fromInd) {
        queue.addLast(new QueueRecord(t.SPEED, now, unlock, upward));
        t.addCost(waitTime, t.LEN + railLen);
        t.BOSS.moved(tripStart, tripEnd, t, fromInd, other(fromInd));
    }


    public int timeToTravel(double trainLen, double trainSpeed) {
        return (int) Math.ceil((railLen + trainLen) / trainSpeed);
    }


    public int blockedUntil() {
        if (queue.isEmpty()) {
            return 0;
        } else {
            return queue.peekLast().unlockTime;
        }
    }


    public void update(int now) {
        if (now == lastUpdated) return;
        while (!queue.isEmpty() && queue.peekFirst().unlockTime <= now) {
            queue.removeFirst();
        }
        lastUpdated = now;
    }


    private class QueueRecord {
        boolean upward;
        int departTime;
        int unlockTime;
        double speed;

        QueueRecord(double spd, int dt, int t, boolean dir) {
            speed = spd;
            departTime = dt;
            unlockTime = t;
            upward = dir;
        }
    }

}
