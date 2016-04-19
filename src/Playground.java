import java.nio.file.Path;
import java.nio.file.Paths;

class Playground {
    public static void main(String[] args) {
        System.out.println(Playground.class.getResource("grid.txt"));
//        Path in = Paths.get("in/grid.txt");
//        Path out = Paths.get("in/out.txt");
//        Scheduler imp = new Scheduler(in, out, RoutingStrategy.IMPROVED);
//        imp.show();
//        System.out.println("---------------------------");
//        Scheduler base = new Scheduler(in, out, RoutingStrategy.BASELINE);
//        base.show();
    }
}
