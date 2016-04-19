import java.nio.file.Path;
import java.nio.file.Paths;

class Playground {
    public static void main(String[] args) {
        Path p = Paths.get(Playground.class.getResource("curr_graph").getPath());
//        Path in = Paths.get("in/grid.txt");
//        Path out = Paths.get("in/out.txt");
//        Scheduler imp = new Scheduler(in, out, RoutingStrategy.IMPROVED);
//        imp.ikuzo();
//        System.out.println("---------------------------");
//        Scheduler base = new Scheduler(in, out, RoutingStrategy.BASELINE);
//        base.ikuzo();
    }
}
