package ExtendedModeler;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JDialog;
import javax.swing.JFrame;


import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.GLAutoDrawable;

import EMGraphics.EM3DLocationEvent;
import smTrace.SmTrace;

public class ControlOfScene extends JDialog implements java.awt.event.WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SceneControler sceneControler;	// Our scene
	String name;					// Our unique control name
	boolean setup;					// true - fully setup
	private boolean active;			// true setup and visible
	public boolean inPos;			// true - in position
	public boolean full;			// Reserve full height
	static int nextColor = 0;		// index for generated colors
	public Point3D setPoint;		// Last check point value
	public float minClose = .5f;	// Close for setPoint comparison 

	
	ControlOfScene(SceneControler sceneControler, String name) {
		super();
		this.sceneControler = sceneControler;
		this.name = name;
		this.setup = false;
		this.active = false;
		this.inPos = false;
		this.full = false;
	}

	/**
	 * Set latest checkpoint "location"
	 */
	public void setPoint(Point3D pt) {
		setPoint = new Point3D(pt);
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

			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("ControlOfView resized");
				// TODO Auto-generated method stub
				
			}
 			@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("ControlOfView moved");
				updateLocation(e);
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
		Point pt = e.getComponent().getLocation();
		recordLocation(pt.x, pt.y);
		SmTrace.lg(String.format(String.format("updateLocation(%s: %d, %d)"
				,  name, pt.x, pt.y)));
		
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

	public void actionPerformed(ActionEvent evt) {
    	System.out.println("ControlOfView Action");
    }

	
	/** our control locations
	 * 
	 */
	private String getPosKeyX() {
		String key = "control." + name + ".pos.x";
		return key;
	}
	private String getPosKeyY() {
		String key = "control." + name + ".pos.y";
		return key;
	}
	
	
	/**
	 * Return position, -1 if none
	 */
	public int getXFromProp() {
		String posstr = SmTrace.getProperty(getPosKeyX());
		if (posstr.equals(""))
			return -1;
		
		return Integer.valueOf(posstr);
	}

	
	public int getYFromProp() {
		String posstr = SmTrace.getProperty(getPosKeyY());
		if (posstr.equals(""))
			return -1;
		
		return Integer.valueOf(posstr);
	}


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
	 * Check active state
	 */
	public boolean isOn() {
		return setup && active;
	}
	
	
	/**
	 * Required control methods
	 * Overridden, if appropriate
	 * @param windowListener 
	 * @throws EMBlockError 
	 */
	public void setControl(boolean on) {
			int bindex = sceneControler.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlOfView.setControl(%b) %s: before - selected(%d)", on, name, bindex),
					"select", "select");
			setActive(on);
			
			bindex = sceneControler.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlOfView.setControl(%b) %s: after - selected(%d)", on, name, bindex),
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


	public void location3DEvent(EM3DLocationEvent e) {
		float x = e.getX();
		float y = e.getY();
		float z = e.getZ();

		SmTrace.lg(String.format("controlOfView.location3DEvent: %s x=%.2g y=%.2g z=%.2g",
									name, x, y, z));
	}

	/**
	 * Set Control window location and remember
	 */
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		recordLocation(x, y);
		this.inPos = true;
		setActive();
	}

	
	/**
	 * Record current location
	 */
	public void recordLocation(int x, int y) {
		String pos_key_x = getPosKeyX();
		SmTrace.setProperty(pos_key_x, String.valueOf(x));
		String pos_key_y = getPosKeyY();
		SmTrace.setProperty(pos_key_y, String.valueOf(y));
	}
	
	
	/**
	 * Check if in position
	 */
	public boolean isInPos() {
		return inPos;
	}

	/**
	 * Generated repeatable color sequence
	 * @throws EMBlockError 
	 */
	public static Color nextColor() {
		final Color colors[] = {
				Color.RED,
				Color.ORANGE,
				Color.YELLOW,
				Color.GREEN,
				Color.BLUE,
				
				Color.CYAN,
				Color.MAGENTA,
				Color.PINK,
				Color.BLACK,
				Color.DARK_GRAY,
				Color.GRAY,
				Color.LIGHT_GRAY,
				Color.WHITE
		};
		Color color = colors[nextColor%colors.length];
		nextColor++;
		return color;
	}
	
	
	/**
	 * Update position in sceneControler display
	 */
	void updatePosition() {
		this.setVisible(true);
	}
	
	
	/**
	 * Remove Control
	 */

 	public void dispose() {
  		super.dispose();
		sceneControler.setCheckBox(name, false);			// Clear display checkbox
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
		SmTrace.lg(String.format("ControlOfView.windowClosed(%s)", name), "control");		
	}

	@Override
	public void windowClosing(java.awt.event.WindowEvent evt) {
		SmTrace.lg(String.format("ControlOfView.windowClosing(%s)", name), "control");
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

	// Overridden by appropriate controls
	public void reset() {
		setPoint = new Point3D();
	}
}
