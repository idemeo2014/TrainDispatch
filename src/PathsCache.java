import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Least Recently Used cache for caching shortest paths
 * originating from one station to others
 */
public class PathsCache {

    private final int CAPACITY;  // max cache size
    private int size;            // number of items already stored
    private int origin;          // index of originating station
    private Map<Integer, List<Station>> paths;


    @SuppressWarnings("unchecked")
    public PathsCache(int capacity) {
        this.CAPACITY = capacity;
    }


    public void put(int station, LinkedList<Station> path) {

    }

    public LinkedList<Station> get(int station) {
        return null;
    }

}
