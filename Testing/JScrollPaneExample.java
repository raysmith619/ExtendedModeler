package Testing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class JScrollPaneExample extends JFrame {

    public static void main(String[] arguments) {
        new JScrollPaneExample();
    }

    /** The gl panel. */
    private final Component glComponent;

    private JScrollPaneExample() {
        String disable = System.getProperty("GLJPanel");
        if (disable==null) {
            glComponent = new GLCanvas();
        } else {
            glComponent = new GLJPanel();
        }


        glComponent.setPreferredSize(new Dimension(800, 600));

        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(glComponent);
        JTextArea textArea = new JTextArea();
        textArea.setText("Test\nTest\nTest\nTest\n");

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(textArea, BorderLayout.NORTH);

        add(panel);
        setLocationRelativeTo(null);
        setTitle("GLCanvas in JScrollPane example");
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
