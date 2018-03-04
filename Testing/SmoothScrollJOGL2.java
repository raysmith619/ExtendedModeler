package Testing;

	import java.awt.BorderLayout;
	import java.awt.GraphicsDevice;
	import java.awt.GraphicsEnvironment;
	import java.awt.event.KeyEvent;
	import java.awt.event.KeyListener;

	import javax.swing.JFrame;
	import javax.swing.JLabel;
	import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
/***
 * 	import net.java.games.jogl.GL;
	import net.java.games.jogl.GLCanvas;
	import net.java.games.jogl.GLCapabilities;
	import net.java.games.jogl.GLDrawable;
	import net.java.games.jogl.GLDrawableFactory;
	import net.java.games.jogl.GLEventListener;
	import net.java.games.jogl.GLU;
***/
public class SmoothScrollJOGL2 
     extends JFrame 
     implements KeyListener, GLEventListener
	{
     GraphicsDevice device;
     
     GLCanvas canvas;
     Animator animator;
     
     JLabel label = new JLabel("hahaha");
     
     long   elapsedStart;
     double elapsedSecond;
     
     /**
      * Constructor.
      *
      */
     public SmoothScrollJOGL2()
     {
           device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

         ///canvas = ((Object) GLDrawableFactory.getEGLFactory()).createGLCanvas(new GLCapabilities(null));
         canvas = new GLCanvas();                                                         //Jogl JSR-231
           canvas.addGLEventListener(this);
           
           setUndecorated(true);

           getContentPane().setLayout(new BorderLayout());
           getContentPane().add(canvas, BorderLayout.CENTER);
           getContentPane().add(label, BorderLayout.SOUTH);
           
         animator = new Animator(canvas);

           device.setFullScreenWindow(this);

           elapsedStart = System.nanoTime();
           
         animator.start();

           addKeyListener(this);
     }
     
     public void keyTyped(KeyEvent e) {}
     public void keyPressed(KeyEvent e) {}

     public void keyReleased(KeyEvent e)
     {
       new Thread(new Runnable() 
                  {
                             public void run() 
                                   {
                               animator.stop();
                               System.exit(0);
                                   }
                          }).start();
     }


     public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
     
     public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
     {
 		GL2 gl = (GL2) drawable.getGL();
        ///GLU glu = drawable.getGLU();
        GLU glu = new GLU();

           // turn on vsync, just to be sure
           /// We don't HAVE gl.wglSwapIntervalEXT(1);
           
       gl.glMatrixMode(GL2.GL_PROJECTION);
       gl.glLoadIdentity();

           // choose major depending on which axis is bigger
           if(width > height)
           {
                 float AR = (float)height / width; // width-major

                 gl.glOrtho(-1f, +1f,  // left, right
                                -AR, +AR,  // bottom, top
                                -1f, +1f); // near, far
           }
           else
           {
                 float AR = (float)width  / height; // height-major
           
                 gl.glOrtho(-AR, +AR,  // left, right
                                -1f, +1f,  // bottom, top
                                -1f, +1f); // near, far
           }

           System.out.println("reshape(): " + width + "," + height);
     }

     public void display(GLAutoDrawable drawable)
     {
           elapsedSecond = (System.nanoTime() - elapsedStart) / 1000000000.0;
           
           GL2  gl  = (GL2) drawable.getGL();
           //GLU glu = drawable.getGLU();
           GLU glu = new GLU();

           gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

           
           gl.glColor3f(1,1,1);
           
       gl.glMatrixMode(GL2.GL_PROJECTION);
       gl.glLoadIdentity();
           gl.glTranslatef((float)Math.sin(elapsedSecond), 0, 0);
           
           
       float x = 0.1f / 2;
       float y = 2 / 2;

       gl.glBegin(GL2.GL_QUADS);
           
         gl.glVertex3f(-x, -y, 0); // BL
         gl.glVertex3f(+x, -y, 0); // BR
         gl.glVertex3f(+x, +y, 0); // TR
         gl.glVertex3f(-x, +y, 0); // TL
                 
           gl.glEnd();

           SwingUtilities.invokeLater(new Runnable() { public void run() { label.setText(""+elapsedSecond); } });
     }


     public static void main(String[] args)
     {
           new SmoothScrollJOGL2();
     }

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}
}