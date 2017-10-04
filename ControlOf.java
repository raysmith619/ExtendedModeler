import javax.swing.JDialog;
import javax.swing.JFrame;


import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

public class ControlOf extends JDialog implements java.awt.event.WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SceneViewer scene;		// Our scene
	String name;			// Our unique control name
	boolean controlActive;

	
	ControlOf(SceneViewer scene, String name) {
		super();
		this.scene = scene;
		this.name = name;
		this.controlActive = false;
	}
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 * @throws EMBlockError 
	 */
	public void setup()  {
	}

	/**
	 * Required control methods
	 * Overridden only if necessary
	 */

	/**
	 * Required control methods
	 * Overridden, if appropriate
	 * @param windowListener 
	 * @throws EMBlockError 
	 */
	public void setControl(boolean on) {
			int bindex = scene.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlOf.setControl(%b) %s: before - selected(%d)", on, name, bindex),
					"select", "select");
	
		if (on) {
			setup();
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			this.addWindowListener(this);
		} else {
			dispose();
		}
			bindex = scene.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlOf.setControl(%b) %s: after - selected(%d)", on, name, bindex),
					"select", "select");
	}
	
	public void onExit() {
		  System.err.println("Exit");
		  System.exit(0);
		}

	public void windowClosed(WindowEvent evt) {
		SmTrace.lg("control close window");
	}
	/**
	 * Check for and act on action
	 * @throws EMBlockError 
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		return false;		// Default no action
	}
	
	
	/**
	 * Adjust this control if appropriate
	 * Overridden, if appropriate
	 */
	public void adjustControls() {		
	}
	
	
	/**
	 * Update position in scene display
	 */
	void updatePosition() {
		this.setVisible(true);
	}
	
	
	/**
	 * Remove Control
	 */

 	public void dispose() {
  		super.dispose();
		scene.setCheckBox(name, false);			// Clear display checkbox
 		controlActive = false;
	}

	@Override
	public void windowActivated(java.awt.event.WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(java.awt.event.WindowEvent arg0) {
		SmTrace.lg(String.format("ControlOf.windowClosed(%s)", name), "control");		
	}

	@Override
	public void windowClosing(java.awt.event.WindowEvent evt) {
		SmTrace.lg(String.format("ControlOf.windowClosing(%s)", name), "control");
		dispose();
	}

	@Override
	public void windowDeactivated(java.awt.event.WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(java.awt.event.WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(java.awt.event.WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(java.awt.event.WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Derived types override, if needing special display
	 * Do control displays when approriate
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
	}
	
}
