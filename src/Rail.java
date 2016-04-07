import java.util.Deque;
import java.util.LinkedList;


public class Rail {

    public final int kore;        // station with lower index
    public final int sore;        // station with higher index
    public final double railLen;  // edge weight
    public Deque<Integer> queue;  // Trains queued for this rail.

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

        // Positive timeStart means going from lower indexed to higher indexed station
        // Negative means higher to lower
        boolean neg = false;
        if (fromStation == sore) {
            neg = true;
        }
        else if (fromStation != kore) throw new RuntimeException();

        if (queue.isEmpty()) {
            int timeArrival = now + travelTime;
            queue.addLast(neg ? -timeArrival : timeArrival);
            t.addCost(0, railLen + t.LEN);
            t.BOSS.moved(now, Math.abs(queue.getLast()), t, fromStation, other(fromStation));
        } else {
            int lastUnlock = queue.peekLast();
            if ((neg && (lastUnlock < 0)) || (!neg && (lastUnlock > 0))) {  // same direction as last in queue

            } else {
                lastUnlock = Math.abs(lastUnlock);
                queue.addLast(neg ? -(lastUnlock + travelTime) : (lastUnlock + travelTime));
                t.addCost(lastUnlock - now, railLen + t.LEN);
                t.BOSS.moved(lastUnlock, Math.abs(queue.getLast()), t, fromStation, other(fromStation));
            }

        }

        return Math.abs(queue.getLast());
    }


    public int timeToTravel(double trainLen, double trainSpeed) {
        return (int) Math.ceil((railLen + trainLen) / trainSpeed);
    }


    public int blockedUntil() {
        if (queue.isEmpty()) {
            return 0;
        } else {
            return Math.abs(queue.peekLast());
        }
    }


    public void update(int now) {
        if (now == lastUpdated) return;

        while (!queue.isEmpty()) {
            if (Math.abs(queue.peekFirst()) <= now) {
                queue.removeFirst();
            } else {
                break;
            }
        }
        lastUpdated = now;
    }

}
