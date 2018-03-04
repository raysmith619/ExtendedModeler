package Testing;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TestLayoutManagers {

    private JPanel northFlowLayoutPanel;
    private JPanel southBorderLayoutPanel;
    private JPanel centerGridBagLayoutPanel;
    private JPanel westBoxLayoutPanel;
    private JPanel eastGridLayoutPanel;

    private final JButton northButton = new JButton("North Button");
    private final JButton southButton = new JButton("South Button");
    private final JButton centerButton = new JButton("Center Button");
    private final JButton eastButton = new JButton("East Button");
    private final JButton westButton = new JButton("West Button");

    public TestLayoutManagers() {
        northFlowLayoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southBorderLayoutPanel = new JPanel(new BorderLayout());
        centerGridBagLayoutPanel = new JPanel(new GridBagLayout());
        eastGridLayoutPanel = new JPanel(new GridLayout(1, 1));
        Box box = Box.createHorizontalBox();
        westBoxLayoutPanel = new JPanel();

        northFlowLayoutPanel.add(northButton);
        northFlowLayoutPanel.setBorder(BorderFactory.createTitledBorder("Flow Layout"));

        southBorderLayoutPanel.add(southButton);
        southBorderLayoutPanel.setBorder(BorderFactory.createTitledBorder("Border Layout"));

        centerGridBagLayoutPanel.add(centerButton);
        centerGridBagLayoutPanel.setBorder(BorderFactory.createTitledBorder("GridBag Layout"));

        eastGridLayoutPanel.add(eastButton);
        eastGridLayoutPanel.setBorder(BorderFactory.createTitledBorder("Grid Layout"));

        box.add(westButton);
        westBoxLayoutPanel.add(box);
        westBoxLayoutPanel.setBorder(BorderFactory.createTitledBorder("Box Layout"));

        JFrame frame = new JFrame("Test Layout Managers");
        frame.setLayout(new BorderLayout());      // This is the deafault layout
        frame.add(northFlowLayoutPanel, BorderLayout.PAGE_START);
        frame.add(southBorderLayoutPanel, BorderLayout.PAGE_END);
        frame.add(centerGridBagLayoutPanel, BorderLayout.CENTER);
        frame.add(eastGridLayoutPanel, BorderLayout.LINE_END);
        frame.add(westBoxLayoutPanel, BorderLayout.LINE_START);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TestLayoutManagers testLayoutManagers
                        = new TestLayoutManagers();
            }
        });
    }
}