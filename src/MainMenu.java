import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


class MainMenu {

    private static final String[] AVAILABLE_STAGES = { "US.txt", "China.txt", "Japan.txt" };
    public static final String STR_BASELINE = "Baseline";
    public static final String STR_IMPROVED = "Improved";

    // shared access
    private static JComboBox<String> stagesCombox;
    private static JTextField trainsTextField;
    private static JTextField timeTextField;
    private static JTextField numTrialsTextField;
    private static JTextField seedValTextfield;
    private static JSlider burstSlider;
    private static JSlider crowdSlider;
    private static JSlider timeSlider;
    private static JSlider speedVarSlider;
    private static JSlider compSlider;
    private static JProgressBar progressBar;
    private static JTextField aniDurTextField;
    private static JLabel statusLabel;

    private static Router router;
    private static List<Train> updateQueue;
    private static Map<Integer, Station> stations;

    private MainMenu() {}

    public static void main(String[] args) {
        ikuzo();
    }

    private static void ikuzo() {
        JFrame menuFrame = new JFrame("Menu");
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // dummy row
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        menuPanel.add(new JLabel(), constraints);
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 0;
        menuPanel.add(new JLabel(), constraints);
        constraints.gridwidth = 1;
        constraints.gridx = 2;
        constraints.gridy = 0;
        menuPanel.add(new JLabel(), constraints);

        // Stage choice
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;
        menuPanel.add(new JLabel("Stage"), constraints);

        stagesCombox = new JComboBox<>(AVAILABLE_STAGES);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 1;
        menuPanel.add(stagesCombox, constraints);

        // Number of trains
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 2;
        menuPanel.add(new JLabel("Number of Trains"), constraints);

        trainsTextField = new JTextField("100");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 2;
        menuPanel.add(trainsTextField, constraints);

        // Dispatch time frame
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        menuPanel.add(new JLabel("Departure Time Frame"), constraints);

        timeTextField = new JTextField("20");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 3;
        menuPanel.add(timeTextField, constraints);

        // Burst
        constraints.gridx = 0;
        constraints.gridy = 4;
        menuPanel.add(new JLabel("Burst"), constraints);

        burstSlider = new JSlider(0, 255);
        burstSlider.setValue(0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 4;
        menuPanel.add(burstSlider, constraints);

        // Crowded
        constraints.gridx = 0;
        constraints.gridy = 5;
        menuPanel.add(new JLabel("Crowdedness"), constraints);

        crowdSlider = new JSlider(0, 255);
        crowdSlider.setValue(0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 5;
        menuPanel.add(crowdSlider, constraints);

        // Time Sensitive
        constraints.gridx = 0;
        constraints.gridy = 6;
        menuPanel.add(new JLabel("Time worth"), constraints);

        timeSlider = new JSlider(0, 255);
        timeSlider.setValue(30);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 6;
        menuPanel.add(timeSlider, constraints);

        // Speed Var
        constraints.gridx = 0;
        constraints.gridy = 7;
        menuPanel.add(new JLabel("Speed Variance"), constraints);

        speedVarSlider = new JSlider(0, 255);
        speedVarSlider.setValue(0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 7;
        menuPanel.add(speedVarSlider, constraints);

        // Composition
        constraints.gridx = 0;
        constraints.gridy = 8;
        menuPanel.add(new JLabel("P Train Ratio"), constraints);

        compSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 8;
        menuPanel.add(compSlider, constraints);

        // Animation duration
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 9;
        menuPanel.add(new JLabel("Animation duration"), constraints);

        aniDurTextField = new JTextField("10");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 9;
        menuPanel.add(aniDurTextField, constraints);


        // seed
        constraints.gridx = 0;
        constraints.gridy = 10;
        menuPanel.add(new JLabel("Seed"), constraints);

        seedValTextfield = new JTextField(Long.toString(System.nanoTime()));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 10;
        menuPanel.add(seedValTextfield, constraints);

        // Run baseline button
        JButton runBaseButton = new JButton("Baseline");
        runBaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar.setValue(0);
                (new Thread(new SingleAnimationRunner(RoutingStrategy.BASELINE))).start();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 11;
        menuPanel.add(runBaseButton, constraints);

        // Run improved button
        JButton runImpButton = new JButton("Improved");
        runImpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar.setValue(0);
                (new Thread(new SingleAnimationRunner(RoutingStrategy.IMPROVED))).start();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 11;
        menuPanel.add(runImpButton, constraints);

        // Run comparison button
        JButton runCompButton = new JButton("Comparison");
        runCompButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBar.setValue(0);
                (new Thread(new ComparisonAnimationRunner())).start();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 2;
        constraints.gridy = 11;
        menuPanel.add(runCompButton, constraints);

        // trials
        constraints.gridx = 0;
        constraints.gridy = 12;
        menuPanel.add(new JLabel("Trials"), constraints);

        numTrialsTextField = new JTextField("1000");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 12;
        menuPanel.add(numTrialsTextField, constraints);

        // run stats
        JButton runStatsButton = new JButton("Run Statistics");
        runStatsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 13;
        menuPanel.add(runStatsButton, constraints);

        // Progress bar
        progressBar = new JProgressBar();
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 14;
        menuPanel.add(progressBar, constraints);

        // status label
        statusLabel = new JLabel("Done");
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 15;
        menuPanel.add(statusLabel, constraints);

        // Frame setup
        menuFrame.add(menuPanel);
        menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuFrame.setMaximumSize(new Dimension(380, 520));
        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setResizable(false);
        menuFrame.setVisible(true);
    }

    private static void loadGraph(InputStream inSt, RoutingStrategy strategy) {
        Scanner in = new Scanner(inSt);
        double mapScale = in.nextDouble();
        int stationCount = in.nextInt();

        stations = new HashMap<>(stationCount);
        router = new Router(stationCount, stations);

        // Read stations
        for (int i = 0; i < stationCount; i++) {
            int nextInd = in.nextInt();
            String stationName = in.next();
            Location loc = new Location(in.nextDouble(), in.nextDouble());
            stations.put(nextInd, Factory.newStation(strategy, stationName, nextInd, loc, router));
        }

        // Read edges
        int edgeCount = in.nextInt();
        for (int i = 0; i < edgeCount; i++) {
            int from = in.nextInt();
            int to   = in.nextInt();
            double weight = in.nextDouble();

            if (weight < 0) { // calculate according to coordinates
                Station fromStat = stations.get(from);
                Station toStat   = stations.get(to);
                double fX = fromStat.location.x;
                double tX = toStat.location.x;
                double fY = fromStat.location.y;
                double tY = toStat.location.y;
                weight = Math.sqrt(Math.pow(fX-tX,2) + Math.pow(fY-tY,2)) * mapScale;
            }

            router.addEdge(from, to, weight);
        }

        // Set adj list for each station
        for (Station s : stations.values()) {
            s.setAdjMap(router.getAdjMap(s.index));
        }
    }

    /**
     * Use the random schedule generator with current parameters to produce a new
     * schedule which is saved in updateQueue
     */
    private static void loadTrains(RoutingStrategy st) {
        RandomScheduleGenerator gen = new RandomScheduleGenerator(stations.size(),
                getIntVal(trainsTextField), getIntVal(timeTextField), st, getLongVal(seedValTextfield));
        gen.setBurst(burstSlider.getValue());
        gen.setCompositionRatio(compSlider.getValue());
        gen.setCrowdedness(crowdSlider.getValue());
        gen.setSpeedVar(speedVarSlider.getValue());
        gen.setTimeSensitivity(timeSlider.getValue());
        updateQueue = gen.getSchedule();
    }

    /**
     * Assign to the trains currently in updateQueue their shortest paths,
     * and the scheduler they should report to.
     */
    private static void bootstrapTrains(Scheduler sche) {
        for (Train train : updateQueue) {
            train.setPath(router.shortest(train.fromInd, train.toInd));
            train.setBoss(sche);
        }
    }

    private static int getIntVal(JTextField tf) {
        return Integer.parseInt(tf.getText());
    }

    private static long getLongVal(JTextField tf) {
        return Long.parseLong(tf.getText());
    }

    private static InputStream streamFromBox(JComboBox<String> cb) {
        return MainMenu.class.getResourceAsStream(cb.getItemAt(cb.getSelectedIndex()));
    }

    private static class SingleAnimationRunner implements Runnable {
        private Scheduler runnerSche;
        private int aniDur;

        SingleAnimationRunner(RoutingStrategy strat) {
            statusLabel.setText("Generating");
            aniDur = getIntVal(aniDurTextField);
            loadGraph(streamFromBox(stagesCombox), strat);
            loadTrains(strat);
            runnerSche = new Scheduler(router, stations, updateQueue);
        }

        @Override
        public void run() {
            progressBar.setValue(40);
            statusLabel.setText("Running shortest path");
            bootstrapTrains(runnerSche);
            runnerSche.calculateOptimalCost();
            statusLabel.setText("Running simulation");
            runnerSche.runSimulation();
            progressBar.setValue(90);
            runnerSche.runAnimation(STR_BASELINE, aniDur);
            statusLabel.setText("Done");
            progressBar.setValue(100);
        }
    }

    private static class ComparisonAnimationRunner implements Runnable {
        private Scheduler base_sche;
        private Scheduler impd_sche;
        private int aniDur;

        ComparisonAnimationRunner() {
            aniDur = getIntVal(aniDurTextField);

            statusLabel.setText("Generating");
            loadGraph(streamFromBox(stagesCombox), RoutingStrategy.BASELINE);
            loadTrains(RoutingStrategy.BASELINE);
            base_sche = new Scheduler(router, stations, updateQueue);
            statusLabel.setText("Running shortest path");
            bootstrapTrains(base_sche);

            statusLabel.setText("Generating");
            loadGraph(streamFromBox(stagesCombox), RoutingStrategy.IMPROVED);
            loadTrains(RoutingStrategy.IMPROVED);
            impd_sche = new Scheduler(router, stations, updateQueue);
            statusLabel.setText("Running shortest path");
            bootstrapTrains(impd_sche);
        }

        @Override
        public void run() {
            progressBar.setValue(40);
            base_sche.calculateOptimalCost();
            progressBar.setValue(60);
            impd_sche.calculateOptimalCost();
            statusLabel.setText("Running simulation");
            base_sche.runSimulation();
            impd_sche.runSimulation();
            progressBar.setValue(80);
            base_sche.runAnimation(STR_BASELINE, aniDur);
            impd_sche.runAnimation(STR_IMPROVED, aniDur);
            statusLabel.setText("Done");
            progressBar.setValue(100);
        }
    }

    private static class StatisticsRunner implements Runnable {

        @Override
        public void run() {

        }
    }
}
