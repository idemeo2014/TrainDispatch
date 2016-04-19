import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrainMenu {

    private static final String[] AVAILABLE_STAGES = { "US.txt", "CN.txt", "JP.txt" };
    private static final ThreadLocal<Scheduler> runningSche = new ThreadLocal<>();

    public static void main(String[] args) {
        show();
    }

    public static void show() {
        // menuFrame
        JComboBox<String> stagesCombox = new JComboBox<>(AVAILABLE_STAGES);
        JButton generateButton = new JButton("Generate");

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runningSche.set(new Scheduler(this.getClass().getResourceAsStream("US.txt"),
                        this.getClass().getResourceAsStream("grid_trains.txt"),
                        RoutingStrategy.IMPROVED));
                runningSche.get().run();
            }
        });

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        menuPanel.add(new JLabel("Select Stage"));
        menuPanel.add(stagesCombox);
        menuPanel.add(generateButton);

        JFrame menuFrame = new JFrame("Menu");
        menuFrame.add(menuPanel);
        menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuFrame.setSize(40,70);
        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

}
