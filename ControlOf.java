import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

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
	SceneViewer scene;			// Our scene
	String name;				// Our unique control name
	boolean setup;				// true - fully setup
	private boolean active;		// true setup and visible
	public boolean inPos;			// true - in position
	public boolean full;		// Reserve full height
	
	ControlOf(SceneViewer scene, String name) {
		super();
		this.scene = scene;
		this.name = name;
		this.setup = false;
		this.active = false;
		this.inPos = false;
		this.full = false;
	}
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 * @throws EMBlockError 
	 */
	public void setup(String name)  {
		if (setup)
			return;			// Already setup
		
		setup();			// Do appropriate setup
		
							// Do common stuff
		addComponentListener(new ComponentListener() {
			public void componentMoved(ComponentEvent e) {
				updateLocation(e);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
	} );
	}

	/**
	 * Set to display full height
	 */
	public void setFull() {
		full = true;
	}
	
	/**
	 * Ck for full display
	 */
	public boolean isFull() {
		return full;
	}
	
	
	/**
	 * Update location
	 * Generally called after move
	 */
	public void updateLocation(ComponentEvent e) {
		SmTrace.lg(String.format("updateLocation(%s)",  name), "location");
		
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
	 * Set as active
	 */
	public void setActive() {
		setActive(true);
	}
	
	
	/**
	 * Set active state
	 * setup, if necessary
	 */
	public void setActive(boolean on ) {
		if (on) {
			if (!setup)
				setup(name);
		}

		setVisible(on);
		active = on;
	}
	
	
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
			setActive(on);
			
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
	 * Set Control window location and remember
	 */
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		this.inPos = true;
		setActive();
	}

	/**
	 * Check if in position
	 */
	public boolean isInPos() {
		return inPos;
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
 		setup = false;
	}

 	public boolean isActive() {
 		if (!setup)
 			return false;
 		return active;
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
	 * Do control displays when appropriate
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
	}

	/**
	 * Adjust block based on control settings and command
	 * @param cb
	 * @param bcmd
	 * @throws EMBlockError 
	 */
	// Overridden by appropriate controls
	public void adjustFromControl(EMBlock cb, EMBCommand bcmd) throws EMBlockError {
	}
}
