import java.util.*;

/**
 * Router provides graph related operations
 */
public class Router {

    Collection<Rail>[] adj;      // adjacency list
    private boolean[] marked;    // aux memory for traversing the graph
    private int[] edgeTo;
    private Map<Integer, Station> stations;


    @SuppressWarnings("unchecked")
    public Router(int num_v, Map<Integer, Station> stationMap) {
        stations = stationMap;
        marked = new boolean[num_v];
        edgeTo = new int[num_v];
        adj = (Collection<Rail>[]) new Collection<?>[num_v];
        for (int i = 0; i < num_v; i++) {
            adj[i] = new LinkedList<>();
        }
    }


    public void addEdge(int a, int b, double weight) {
        int kore =     a < b ? a : b;  // assign kore the lower index
        int sore = kore == a ? b : a;  // and sore the higher one

        if (!neighbors(a, b)) {
            Rail newRail = new Rail(kore, sore, weight);
            adj[a].add(newRail);
            adj[b].add(newRail);
        }
    }


    public Map<Integer, Rail> getAdjMap(int stationInd) {
        Map<Integer, Rail> adjMap = new HashMap<>(adj[stationInd].size());
        for (Rail r : adj[stationInd]) {
            adjMap.put(r.other(stationInd), r);
        }
        return adjMap;
    }


    public boolean neighbors(int a, int b) {
        for (Rail r : adj[a]) {
            if (r.other(a) == b) return true;
        }
        return false;
    }


    /**
     * Distance between two adjacent stations
     */
    public double distanceAdj(int a, int b) {
        for (Rail e : adj[a]) {
            if (e.other(a) == b) return e.railLen;
        }
        throw new NoSuchElementException(String.format("No edge between %d and %d", a, b));
    }


    /**
     * Length of a path.
     * @param p the path
     * @return railLen of the path
     */
    public double pathTotalLength(List<Station> p) {
        double totalDistance = 0.0;
        for (int i = 1; i < p.size(); i++) {
            totalDistance += distanceAdj(p.get(i-1).index, p.get(i).index);
        }
        return totalDistance;
    }


    public List<Station> shortest(int from, int to) {
        LinkedList<Station> shortestPath = new LinkedList<>();
        Arrays.fill(marked, false);
        bfs(from);
        for (int i = to; i != from; i = edgeTo[i]) {
            shortestPath.addFirst(stations.get(i));
        }
        shortestPath.addFirst(stations.get(from));
        return shortestPath;
    }


    private void bfs(int from) {
        LinkedList<Integer> queue = new LinkedList<>();
        marked[from] = true;
        queue.addLast(from);
        while (!queue.isEmpty()) {
            int v = queue.removeFirst();
            for (Rail rail : adj[v]) {
                int w = rail.other(v);
                if (!marked[w]) {
                    marked[w] = true;
                    edgeTo[w] = v;
                    queue.addLast(w);
                }
            }
        }
    }


    public List<Station> reroute(int from, int to, int ... exclude) {
        return null;
    }

}
