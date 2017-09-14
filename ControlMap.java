import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Control Map window
 * Independent window
 */
import smTrace.SmTrace;

class ControlMap extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GLCapabilities caps;			// Main set of capabilities
	ExtendedModeler modeler;		// Main class - currently only for check button access
	JFrame frame;					// Access to our frame
	String frameName;				// Our window name
	SmTrace smTrace; 				// Trace support
	GLAutoDrawable drawable; 		// Save base

	GLU glu; // Needed for scene2WorldCoord
	GLUT glut;
	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;
	
	public ControlMap(String frameName,
			GLCapabilities caps,
			ExtendedModeler modeler,
			SmTrace trace) {

		super(caps);
		this.caps = caps;
		this.frameName = frameName;
		this.frame = new JFrame(frameName);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		this.modeler = modeler;
		this.smTrace = trace; // Passed in
		addGLEventListener(this);

		addMouseListener(this);
		addMouseMotionListener(this);
		
		
		
	}

	public Dimension getPreferredSize() {
		return new Dimension(512, 512);
	}

	public void init(GLAutoDrawable drawable) {
		this.drawable = drawable; // Save for later
		GL2 gl = (GL2) drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		glu = new GLU();
		glut = new GLUT();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();

		// set viewport
		gl.glViewport(0, 0, width, height);
	}

	/**
	 * Display 4x4 matrix (from double[16]
	 * 
	 * @param heading
	 *            - heading
	 * @param vals
	 *            double[16]
	 */
	public void printMatrix(String heading, double vals[]) {
		System.out.println("\n" + heading);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int idx = i + 4 * j;
				System.out.print(String.format("%7.2g", vals[idx]));
			}
			System.out.print("\n");
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// leave this empty
		System.out.println("displayChanged");
	}

	public void display(GLAutoDrawable drawable) {
		this.drawable = drawable;


	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mousePressed(%d,%d)", mouse_x, mouse_y));


	}

	public void mouseReleased(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mouseReleased(%d,%d)", mouse_x, mouse_y));
	}

	public void mouseMoved(MouseEvent e) {

		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

	}

	public void mouseDragged(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

	}


	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();
		System.out.println(String.format("action: %s", action));
	}


	

	public void setCheckBox(String name, boolean checked) {
		if (modeler != null)
			modeler.setCheckBox(name, checked);
	}

	
}
