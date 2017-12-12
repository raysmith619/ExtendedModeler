package ExtendedModeler;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jogamp.newt.event.KeyEvent;

import EMGraphics.EM3DLocationEvent;
import EMGraphics.EM3DLocationListner;
import EMGraphics.EM3DPosition;

import smTrace.SmTrace;

public class ControlOfLookAt extends ControlOf implements EM3DLocationListner{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel posPanel;
	JTextField posXfield;
	JTextField posYfield;
	JTextField posZfield;

	JTextField adjXfield;
	JTextField adjYfield;
	JTextField adjZfield;
	EM3DPosition m3DPos;		// 3D setting if non-null
	
	ControlOfLookAt(SceneViewer scene, String name) {
		super(scene, name);
		setup();
	}

	/**
	 * reset to default setting
	 */
	public void reset() {
		setup = false;
		setup();
	}

	/**
	 * Setup Control / Display position of selected block
	 */
	public void setup() {
		if (setup)
			return; // Already present
		// JPanel panel = new JPanel(new GridLayout(2,7));
		/// controlDialog = new JDialog();
		setTitle("LookAt - Adjust/Report");
		posPanel = new JPanel(new GridLayout(0, 1));
		///JPanel posPanel = new JPanel();
		add(posPanel);


		JPanel moveto_panel = new JPanel();
		posPanel.add(moveto_panel);

		traceSelected(11);
		Point3D center = new Point3D(scene.camera.target);
		posXfield = new JTextField(String.format("%.2f", center.x()));
		SmTrace.lg(String.format("setup posXfield"));
		posXfield.setActionCommand("emc_ENTER");
		posXfield.addActionListener(scene);
		posYfield = new JTextField(String.format("%.2f", center.y()));
		posYfield.setActionCommand("emc_ENTER");
		posYfield.addActionListener(scene);
		posZfield = new JTextField(String.format("%.2f", center.z()));
		posZfield.setActionCommand("emc_ENTER");
		posZfield.addActionListener(scene);

		moveto_panel.add(new JLabel("Loc:"));
		moveto_panel.add(new JLabel("x:"));
		moveto_panel.add(posXfield);
		moveto_panel.add(new JLabel("y:"));
		moveto_panel.add(posYfield);
		moveto_panel.add(new JLabel("z:"));
		moveto_panel.add(posZfield);
		JButton lookAtButton = new JButton("LookAt");
		lookAtButton.setActionCommand("emc_lookAtButton");
		lookAtButton.addActionListener(scene);
		moveto_panel.add(lookAtButton);

		///JPanel sizeto_panel = new JPanel();
		///posPanel.add(sizeto_panel);
		float defdim = .5f;		// Default dimensions

		// Adjust by
		JPanel adj_panel = new JPanel();
		posPanel.add(adj_panel);
		float adjamt = 1f; // Default adjustment
		adjXfield = new JTextField(String.format("%.2f", adjamt));
		adjXfield.setActionCommand("emc_adjENTER");
		adjXfield.addActionListener(scene);
		adjYfield = new JTextField(String.format("%.2f", adjamt));
		adjYfield.setActionCommand("emc_adjENTER");
		adjYfield.addActionListener(scene);
		adjZfield = new JTextField(String.format("%.2f", adjamt));
		adjZfield.setActionCommand("emc_adjENTER");
		adjZfield.addActionListener(scene);

		JButton adjUpButton = new JButton("Up By");
		adjUpButton.setActionCommand("emc_lookAtAdjUpButton");
		adjUpButton.addActionListener(scene);
		JButton adjDownButton = new JButton("Down By");
		adjDownButton.setActionCommand("emc_lookAtAdjDownButton");
		adjDownButton.addActionListener(scene);
		adj_panel.add(new JLabel("Adj:"));
		adj_panel.add(new JLabel("x:"));
		adj_panel.add(adjXfield);
		adj_panel.add(new JLabel("y:"));
		adj_panel.add(adjYfield);
		adj_panel.add(new JLabel("z:"));
		adj_panel.add(adjZfield);
		adj_panel.add(adjUpButton);
		adj_panel.add(adjDownButton);
		pack();

		JPanel m3D_panel = new JPanel();
		m3D_panel.setBorder(BorderFactory.createLineBorder(Color.green));
		posPanel.add(m3D_panel);
		pack();
		add3DPositioning(m3D_panel);
		/**
		**/
		pack();

		int bindex2 = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfLookAt.setup after - selected(%d)", bindex2), "select");
		setup = true;
	}

	
	
	/**
	 * Add 3D positioning
	 * @param panel
	 */
	public void add3DPositioning(JPanel panel) {
		SmTrace.lg("addMap - adding 3D positioning");
		JPanel m3D_panel = new JPanel();
		m3D_panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(m3D_panel);
		pack();
		JButton m3D_positioning_Button = new JButton("3D Positioning");
		m3D_positioning_Button.setActionCommand("emc_m3D_lookAt_PositionButton");
		m3D_panel.add(m3D_positioning_Button);
		m3D_positioning_Button.addActionListener(scene);
		pack();
		m3D_panel.setVisible(true);
		
		
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
	 * get fields
	 * 
	 */
	/**
	 * get adjustment (composite)
	 * 
	 * @return vector
	 * 
	 */
	public Vector3D getAdj() throws EMBlockError {
		float tx = getAdjX();
		float ty = getAdjY();
		float tz = getAdjZ();
		return new Vector3D(tx, ty, tz);
	}

	public float getPosX() throws EMBlockError {
		if (posXfield == null)
			return 0;
		String text = posXfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getPosY() throws EMBlockError {
		if (posYfield == null)
			return 0;
		String text = posYfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getPosZ() throws EMBlockError {
		if (posZfield == null)
			return 0;
		String text = posZfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getAdjX() throws EMBlockError {
		if (posXfield == null)
			return 0;
		String text = adjXfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getAdjY() throws EMBlockError {
		if (posYfield == null)
			return 0;
		String text = adjYfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getAdjZ() throws EMBlockError {
		if (posZfield == null)
			return 0;
		String text = adjZfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posXfieldSet(String val) {
		if (posXfield == null) {
			SmTrace.lg("posXField - not initialized");
			return false;
		}
		posXfield.setText(val);
		return true;

	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posXfieldSet(float val) {
		String val_str = String.format("%.2g", val);
		return posXfieldSet(val_str);
	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posYfieldSet(String val) {
		if (posYfield == null) {
			SmTrace.lg("posYField - not initialized");
			return false;
		}
		posYfield.setText(val);
		return true;

	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posYfieldSet(float val) {
		String val_str = String.format("%.2g", val);
		return posYfieldSet(val_str);
	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posZfieldSet(String val) {
		if (posZfield == null) {
			SmTrace.lg("posZField - not initialized");
			return false;
		}
		posZfield.setText(val);
		return true;

	}

	/**
	 * Set field
	 * 
	 * @param val
	 * @return true if no error
	 */
	public boolean posZfieldSet(float val) {
		String val_str = String.format("%.2g", val);
		return posZfieldSet(val_str);
	}

	/**
	 * MoveTo button simulation
	 * 
	 * @throws EMBlockError
	 */
	public boolean LookAt() throws EMBlockError {
		boolean ret = ckDoAction("lookAtButton");
		if (!ret)
			SmTrace.lg("ControlOfLookAt.lookAt failed");
		return ret;
	}

	/**
	 * Look at location
	 * 
	 * @throws EMBlockError
	 */
	public boolean LookAt(float x, float y, float z) throws EMBlockError {
		if (!posXfieldSet(x)) {
			SmTrace.lg(String.format("ControlOfLookAt.Moveto(x=%.2g) failed", x));
			return false;
		}
		if (!posYfieldSet(y)) {
			SmTrace.lg(String.format("ControlOfLookAt.Moveto(y=%.2g) failed", y));
			return false;
		}
		if (!posZfieldSet(z)) {
			SmTrace.lg(String.format("ControlOfLookAt.Moveto(z=%.2g) failed", z));
			return false;
		}
		if (!LookAt()) {
			SmTrace.lg(String.format("ControlOfLookAt.Moveto move(%.2g, %.2g,%.2g) failed", x, y, z));
			return false;
		}
		return true;
	}

	private void traceSelected(int place) {
		int bindex = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfLookAt.setup place(%d) - selected(%d)", place, bindex), "select",
				"select");
	}

	/**
	 * Adjust Position
	 * 
	 * @throws EMBlockError
	 * 
	 **/
	/*
	 * Adjust by increments in direction 1- positive, -1 negative
	 * 
	 * @param direction
	 */
	private void lookAtAdjustPosition(EMBCommand bcmd, int direction) throws EMBlockError {

		float adj_xval = 0;
		float adj_yval = 0;
		float adj_zval = 0;
		if (adjXfield != null) {
			String text = adjXfield.getText();
			adj_xval = Float.valueOf(text);
		}
		if (adjYfield != null) {
			String text = adjYfield.getText();
			adj_yval = Float.valueOf(text);
		}
		if (adjZfield != null) {
			String text = adjZfield.getText();
			adj_zval = Float.valueOf(text);
		}
		if (direction < 0) {
			adj_xval *= -1;
			adj_yval *= -1;
			adj_zval *= -1;
		}
		float xval = getPosX();
		float yval = getPosY();
		float zval = getPosZ();
		posXfieldSet(xval + adj_xval);
		posYfieldSet(yval + adj_yval);
		posZfieldSet(zval + adj_zval);
		lookAtPosition(bcmd);
	}

	/**
	 * Look at position
	 * setting values, updating controls
	 * @throws EMBlockError
	 */
	public void setLookAtPosition(Point3D pt) throws EMBlockError {
		if (!posXfieldSet(pt.x()))
			return;
		
		if (!posYfieldSet(pt.y()))
			return;
		
		if (!posZfieldSet(pt.z()))
			return;
		
		if (m3DPos != null)
			m3DPos.updatePoint(pt);
	}

	/**
	 * Look at position
	 * creating command, setting values, updating controls
	 * @throws EMBlockError
	 */
	public void lookAtPosition(float x, float y, float z) throws EMBlockError {
		if (!posXfieldSet(x))
			return;
		
		if (!posYfieldSet(y))
			return;
		
		if (!posZfieldSet(z))
			return;

		
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd("emc lookAt cmd");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		SmTrace.lg(String.format("lookAtPosition cmd x=%.2f y=%.2f z=%.2f", x, y, z));
		bcmd.setLookAt(new Point3D(x, y, z));
		bcmd.doCmd();
	}

	/**
	 * Look at position
	 * 
	 * @throws EMBlockError
	 */
	private void lookAtPosition(EMBCommand bcmd) throws EMBlockError {
		float xval = 0;
		float yval = 0;
		float zval = 0;
		if (posXfield != null) {
			String text = posXfield.getText();
			xval = Float.valueOf(text);
		}
		if (posYfield != null) {
			String text = posYfield.getText();
			yval = Float.valueOf(text);
		}
		if (posZfield != null) {
			String text = posZfield.getText();
			zval = Float.valueOf(text);
		}
		SmTrace.lg(String.format("lookAtPosition cmd x=%.2f y=%.2f z=%.2f", xval, yval, zval));
		bcmd.setLookAt(new Point3D(xval, yval, zval));
		bcmd.doCmd();
	}

	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
	}

	/**
	 * Check for and act on action
	 * 
	 * @throws EMBlockError
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		EMBCommand bcmd;
		if (!isActive())
			return false; // Not active

		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		switch (action) {

		case "emc_lookAtButton":
			lookAtPosition(bcmd);
			break;

		case "emc_ENTER":
			lookAtPosition(bcmd);
			break;

		case "emc_lookAtAdjUpButton":
			lookAtAdjustPosition(bcmd, 1);
			break;

		case "emc_adjENTER":
			lookAtAdjustPosition(bcmd, 1);
			break;

		case "emc_lookAtAdjDownButton":
			lookAtAdjustPosition(bcmd, -1);
			break;
			
		case "emc_m3D_lookAt_PositionButton":
			m3DPositionSelect(bcmd);
			break;

		default:
			return false; // No action here
		}
		return bcmd.doCmd();

	}

	

	
	/**
	 * Select LookAt via 3DPosition widget
	 * @throws EMBlockError 
	 */
	public void m3DPositionSelect(EMBCommand bcmd) throws EMBlockError {
		int width = 350;
		int height = 2*width+20;
		float xMin = -5;
		float xMax = 5;
		float yMin = xMin;
		float yMax = xMax;
		float zMin = xMin;
		float zMax = xMax;
		
		Point lapt = this.getLocation();
		lapt.translate(0, -height);
		JFrame frame = new JFrame();
		frame.setLocation(lapt);
		m3DPos = new EM3DPosition("LookAt Position", frame,
			width,
			height,
			"xLookAT", xMin, xMax,
			"yLookAT", yMin, yMax,
			"zLookAT", zMin, zMax
			);
		m3DPos.addEM3DEventListener(this);

	}
	
	
	/**
	 * Set / Get / Access methods
	 */

	/**
	 * Adjust block based on control settings and command
	 * 
	 * @param cb
	 * @param bcmd
	 * @throws EMBlockError
	 */
	public void adjustFromControl(EMBlock cb, EMBCommand bcmd) throws EMBlockError {
	}

	/**
	 * Set control UI based on current block settings
	 * 
	 * @param cb
	 */
	public void setControl(EMBlock cb) {
	}

	/**
	 * Set control UI based on current block settings
	 * 
	 * @param cb
	 */
	public void setControl(EMBlockBase cb) {
	}

	public void setPos(Point3D cbPos) {
		Float val;
		String text;

		val = cbPos.x();
		text = String.format("%.2g", val);
		posXfield.setText(text);

		val = cbPos.y();
		text = String.format("%.2g", val);
		posYfield.setText(text);

		val = cbPos.z();
		text = String.format("%.2g", val);
		posZfield.setText(text);
	}

	@Override
	public void location3DEvent(EM3DLocationEvent e) {
		float x = e.getX();
		float y = e.getY();
		float z = e.getZ();

		SmTrace.lg(String.format("ControlOfLookAt.location3DEvent: %s x=%.2g y=%.2g z=%.2g",
									"", x, y, z));
		try {
			lookAtPosition(x,y,z);
		} catch (EMBlockError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
