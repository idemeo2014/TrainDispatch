import org.junit.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

public class Playground {
    public static void main(String[] args) {

        runTest();

    }

    public static void runTest() {
        Path in = Paths.get("in/grid.txt");
        Path out = Paths.get("in/out.txt");
//        Scheduler s = new Scheduler(in, out, RoutingStrategy.BASELINE);
//        s.run();
//        System.out.println("\n/////////////////////////////////////////////////\n");
        Scheduler s = new Scheduler(in, out, RoutingStrategy.IMPROVED);
        s.run();
    }

    public void parseTest() {
        Path inputFile = Paths.get("in/simplest_priority.txt");
        try (Scanner in = new Scanner(inputFile)) {
            int stationCount = in.nextInt();

            for (int i = 0; i < stationCount; i++) {
                System.out.printf("%d ", in.nextInt());
                System.out.print(in.next() + " ");
                System.out.print(in.nextDouble() + " ");
                System.out.print(in.nextDouble() + " \n");
            }

            // Read edges
            int edgeCount = in.nextInt();
            for (int i = 0; i < edgeCount; i++) {
                int from = in.nextInt();
                int to   = in.nextInt();
                double weight = in.nextDouble();
                System.out.printf("%d %d %f\n", from, to, weight);
            }

            // Read trains
            int trainCount = in.nextInt();
            for (int i = 0; i < trainCount; i++) {
                System.out.printf("%s %d %d %d %s %f %f %f %f\n", in.next(), in.nextInt(),in.nextInt(), in.nextInt(), in.next(),in.nextDouble(),in.nextDouble(),in.nextDouble(),in.nextDouble());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
