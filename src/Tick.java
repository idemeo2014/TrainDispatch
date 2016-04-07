/**
 * 00:00 of the current day is the 1st tick.
 * Then each minute counts as a tick.
 * So 23:59 of the same day is the 1440th tick,
 *    00:00 of the next day is the 1441th tick.
 *
 * int provides 2^31-1 ticks or more than 4080 years of tick range,
 * so that is way more than enough.
 */

public class Tick {
    private final int t;

    public Tick(int tick) {
        this.t = tick;
    }

    public int getT() {
        return t;
    }
}
