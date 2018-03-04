package Testing;

// Small example with the problem described above. 
import com.jogamp.opengl.util.FPSAnimator; 
import java.awt.*; 
import java.awt.event.*; 
import com.jogamp.opengl.*; 
import com.jogamp.opengl.awt.GLCanvas; 
import javax.swing.*; 

public class Test extends JFrame implements GLEventListener { 
   private CardLayout cards; 
   private static final String LABEL = "label"; 
   private static final String CANVAS = "canvas"; 
   private String selected = LABEL; 

   public Test() { 
      GLProfile glp = GLProfile.get(GLProfile.GL2); 
      GLCapabilities caps = new GLCapabilities(glp); 
      GLCanvas canvas = new GLCanvas(caps); 
      canvas.setPreferredSize(new Dimension(640, 480)); 
      canvas.addGLEventListener(this); 
      final FPSAnimator animator = new FPSAnimator(canvas, 60); 
      addWindowListener(new WindowAdapter() { 
         public void windowClosing(WindowEvent e) { 
            new Thread() { 
               public void run() { 
                  animator.stop(); 
                  System.exit(0); 
               } 
            }.start(); 
         } 
      }); 
      JButton button = new JButton("Switch Cards"); 
      add(button, BorderLayout.NORTH); 
      final JPanel cardHolder = new JPanel(); 
      cards = new CardLayout(); 
      cardHolder.setLayout(cards); 
      cardHolder.add(new JLabel("A label to cover the canvas"), LABEL); 
      cardHolder.add(canvas, CANVAS); 
      add(cardHolder, BorderLayout.CENTER); 
      button.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                if (selected.equals(LABEL)) { 
                    animator.start(); 
                    cards.show(cardHolder, CANVAS); 
                    selected = CANVAS; 
                } 
                else { 
                    animator.stop(); 
                    cards.show(cardHolder, LABEL); 
                    selected = LABEL; 
                } 
            } 
        }); 
      pack(); 
      setTitle("OpenGL 2 Test"); 
      setVisible(true); 
   } 

   public static void main(String[] args) { 
      new Test(); 
   } 

   public void init(GLAutoDrawable drawable) { 
     GL2 gl = drawable.getGL().getGL2(); 
     gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 
   } 

   float spin = 0; 

   public void display(GLAutoDrawable drawable) { 
     GL2 gl = drawable.getGL().getGL2(); 
     gl.glClear(GL2.GL_COLOR_BUFFER_BIT); 
     gl.glPushMatrix(); 
     gl.glRotatef(spin, 0.0f, 0.0f, 1.0f); 
     gl.glColor3f(1.0f, 1.0f, 1.0f); 
     gl.glRectf(-25.0f, -25.0f, 25.0f, 25.0f); 
     gl.glPopMatrix(); 
     gl.glFlush(); 
     spin += 1; 
     while (spin > 360) spin -= 360; 
   } 

   public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { 
     GL2 gl = drawable.getGL().getGL2(); 
     gl.glViewport(0, 0, w, h); 
     gl.glMatrixMode(GL2.GL_PROJECTION); 
     gl.glLoadIdentity(); 
     if (w <= h) gl.glOrtho(-50.0, 50.0, 
         -50.0 * (float) h / (float) w, 
         50.0 * (float) h / (float) w, 
         -1.0, 1.0); 
     else gl.glOrtho(-50.0 * (float) w / (float) h, 
         50.0 * (float) w / (float) h, -50.0, 50.0, 
         -1.0, 1.0); 
     gl.glMatrixMode(GL2.GL_MODELVIEW); 
     gl.glLoadIdentity(); 
   } 

   public void dispose(GLAutoDrawable drawable) { } 
} 
