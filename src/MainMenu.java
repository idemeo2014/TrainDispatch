import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MainMenu {

    private static final String[] AVAILABLE_STAGES = { "US.txt", "CN.txt", "JP.txt" };
    private static Scheduler sche;

    public static void main(String[] args) {
        ikuzo();
    }

    private static void ikuzo() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Choose stage
        constraints.gridx = 0;
        constraints.gridy = 0;
        menuPanel.add(new JLabel("Stage"), constraints);

        JComboBox<String> stagesCombox = new JComboBox<>(AVAILABLE_STAGES);
        constraints.gridx = 1;
        constraints.gridy = 0;
        menuPanel.add(stagesCombox, constraints);

        // Number of trains
        constraints.gridx = 0;
        constraints.gridy = 1;
        menuPanel.add(new JLabel("Number of Trains"), constraints);

        JTextField trainsTextField = new JTextField("100");
        constraints.gridx = 1;
        constraints.gridy = 1;
        menuPanel.add(trainsTextField, constraints);

        //
        constraints.gridx = 0;
        constraints.gridy = 1;
        menuPanel.add(new JLabel("Number of Trains"), constraints);

//        JTextField trainsTextField = new JTextField("100");
//        constraints.gridx = 1;
//        constraints.gridy = 1;
//        menuPanel.add(trainsTextField, constraints);

        // One run button
        JButton oneRunButton = new JButton("Animate One Run");
        oneRunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sche = new Scheduler(this.getClass().getResourceAsStream("US.txt"),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.IMPROVED);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 1;
        menuPanel.add(oneRunButton, constraints);

        // Frame setup
        JFrame menuFrame = new JFrame("Menu");
        menuFrame.add(menuPanel);
        menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuFrame.setSize(40,70);
        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

}
