import java.util.List;
import java.util.Map;


public class ImprovedStation extends Station {

    private static final int CACHE_SIZE = 40;  // Max size of paths cache


    public ImprovedStation(String name, int id, Location loc, Router router) {
        super(name, id, loc, router);
    }

    @Override
    public void willMove(int now, Train t) {
        List<Station> path = t.path;

        //////////////////////////////////////
//        System.out.printf("willMove %3d at %3d %-15s path= [ ", now, index, t.NAME);
//        for (Station s : path) {
//            System.out.print(s.index + " ");
//        }
//        System.out.println(" ]");
        //////////////////////////////////////

        if (path.size() == 0) {
            t.BOSS.done(now, t);
            return;
        }

        Rail connection = adj.get(path.get(0).index);
        int arrivalTime = connection.enqueue(index, t, now);
        int tripDuration = connection.timeToTravel(t.LEN, t.SPEED);
        int timeDepart = arrivalTime - tripDuration;

        if (timeDepart == now) {
            t.setState(TrainState.ENROUTE);
            t.setSit(now + tripDuration);
        } else {
            t.setState(TrainState.WAITING);
            t.setSit(timeDepart);
        }
    }

    @Override
    public void didHeadTo(int station, int now, Train t) {
//        System.out.printf("%3d departed %-15s %3d -> %3d%n", now, t.NAME, index, station);
        Rail connection = adj.get(t.path.get(0).index);
        int tripDuration = connection.timeToTravel(t.LEN, t.SPEED);
        t.setState(TrainState.ENROUTE);
        t.setSit(now + tripDuration);
    }

    @Override
    public void haveReached(int station, int now, Train t) {
//        System.out.printf("%3d reached %-15s %3d -> %3d%n", now, t.NAME, index, station);
        t.setState(TrainState.IDLE);
        t.update(now);
    }
}
