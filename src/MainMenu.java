import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MainMenu {

    private static final String[] AVAILABLE_STAGES = { "US.txt", "China.txt", "Japan.txt" };

    public static void main(String[] args) {
        ikuzo();
    }

    private static void ikuzo() {
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

        JComboBox<String> stagesCombox = new JComboBox<>(AVAILABLE_STAGES);
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

        JTextField trainsTextField = new JTextField("100");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 2;
        menuPanel.add(trainsTextField, constraints);

        // Dispatch time frame
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        menuPanel.add(new JLabel("Dispatch Time Frame"), constraints);

        JTextField timeTextField = new JTextField("100");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 3;
        menuPanel.add(timeTextField, constraints);

        // Burst
        constraints.gridx = 0;
        constraints.gridy = 4;
        menuPanel.add(new JLabel("Burst"), constraints);

        JSlider burstSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 4;
        menuPanel.add(burstSlider, constraints);

        // Crowded
        constraints.gridx = 0;
        constraints.gridy = 5;
        menuPanel.add(new JLabel("Crowdedness"), constraints);

        JSlider crowdSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 5;
        menuPanel.add(crowdSlider, constraints);

        // Time Sensitive
        constraints.gridx = 0;
        constraints.gridy = 6;
        menuPanel.add(new JLabel("Time Sensitivity"), constraints);

        JSlider timeSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 6;
        menuPanel.add(timeSlider, constraints);

        // Speed Var
        constraints.gridx = 0;
        constraints.gridy = 7;
        menuPanel.add(new JLabel("Speed Variance"), constraints);

        JSlider speedVarSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 7;
        menuPanel.add(speedVarSlider, constraints);

        // Composition
        constraints.gridx = 0;
        constraints.gridy = 8;
        menuPanel.add(new JLabel("Train Composition"), constraints);

        JSlider compSlider = new JSlider(0, 255);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 8;
        menuPanel.add(compSlider, constraints);

        // Run baseline button
        JButton runBaseButton = new JButton("Baseline");
        runBaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Scheduler sche = new Scheduler(this.getClass().getResourceAsStream(stagesCombox.getItemAt(stagesCombox.getSelectedIndex())),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.BASELINE, true);
                sche.runSimulation();
                sche.runAnimation();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 9;
        menuPanel.add(runBaseButton, constraints);

        // Run improved button
        JButton runImpButton = new JButton("Improved");
        runImpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // @TODO
                Scheduler sche = new Scheduler(
                        this.getClass().getResourceAsStream(stagesCombox.getItemAt(stagesCombox.getSelectedIndex())),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.IMPROVED, true);
                sche.runSimulation();
                sche.runAnimation();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 9;
        menuPanel.add(runImpButton, constraints);

        // Run comparison button
        JButton runCompButton = new JButton("Comparison");
        runCompButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // @TODO
                Scheduler sche_one = new Scheduler(
                        this.getClass().getResourceAsStream(stagesCombox.getItemAt(stagesCombox.getSelectedIndex())),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.IMPROVED, true);
                Scheduler sche_two = new Scheduler(
                        this.getClass().getResourceAsStream(stagesCombox.getItemAt(stagesCombox.getSelectedIndex())),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.BASELINE, true);
                sche_one.runSimulation();
                sche_two.runSimulation();
                sche_one.runAnimation();
                sche_two.runAnimation();
            }
        });
        constraints.gridwidth = 1;
        constraints.gridx = 2;
        constraints.gridy = 9;
        menuPanel.add(runCompButton, constraints);

        // Run statistics
        constraints.gridx = 0;
        constraints.gridy = 10;
        menuPanel.add(new JLabel("Trials"), constraints);

        JTextField numTrialsTextField = new JTextField("1000");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        constraints.gridy = 10;
        menuPanel.add(numTrialsTextField, constraints);

        JButton runStatsButton = new JButton("Run Statistics");
        runStatsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // @TODO
            }
        });
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 11;
        menuPanel.add(runStatsButton, constraints);

        // Progress bar for statistics
        JProgressBar statsProgBar = new JProgressBar();
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = 12;
        menuPanel.add(statsProgBar, constraints);

        // Frame setup
        JFrame menuFrame = new JFrame("Menu");
        menuFrame.add(menuPanel);
        menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuFrame.setMaximumSize(new Dimension(380, 500));
        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
         menuFrame.setResizable(false);
        menuFrame.setVisible(true);
    }

}
