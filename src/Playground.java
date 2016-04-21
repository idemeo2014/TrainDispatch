import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.Random;

class Playground {
    public static void main(String[] args) {
        stats();
//        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//        int width = gd.getDisplayMode().getWidth();
//        int height = gd.getDisplayMode().getHeight();
//        System.out.println(width + " " + height);
    }


    private static void pq() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.add(1);
        q.add(66);
        q.add(4);
        q.add(23);
        q.add(17);
        q.add(46);

        while (!q.isEmpty()) {
            System.out.println(q.remove());
        }
    }

    private static void stats() {
        int size = 2000;
        double[] a = new double[size];
        for (int i = 0; i < size; i++) {
            a[i] = RandomScheduleGenerator.normal(100.0, 5);
        }

        double minA = StdStats.min(a);
        StdOut.printf("       min %10.3f\n", minA);
        StdOut.printf("      mean %10.3f\n", StdStats.mean(a));
        StdOut.printf("       max %10.3f\n", StdStats.max(a));
        StdOut.printf("       sum %10.3f\n", StdStats.sum(a));
        StdOut.printf("    stddev %10.3f\n", StdStats.stddev(a));
        StdOut.printf("       var %10.3f\n", StdStats.var(a));
        StdOut.printf("   stddevp %10.3f\n", StdStats.stddevp(a));
        StdOut.printf("      varp %10.3f\n", StdStats.varp(a));
    }
}
