package ExtendedModeler;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;

import EMGraphics.EM3DLocationEvent;
import EMGraphics.EM3DPosition;
import smTrace.SmTrace;

public class ControlOfEye extends ControlOfView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField posXfield;
	JTextField posYfield;
	JTextField posZfield;

	JTextField adjXfield;
	JTextField adjYfield;
	JTextField adjZfield;
	
	EM3DPosition m3DPos;		// 3D setting if non-null

	ControlOfEye(SceneViewer sceneViewer, String name) {
		super(sceneViewer, name);
		///setup();
	}

	/**
	 * Setup Control / Display position of selected block
	 */
	public void setup() {
		if (setup)
			return; // Already present
		int bindex = sceneViewer.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfEyeAt.setup before - selected(%d)", bindex), "select");
		// JPanel panel = new JPanel(new GridLayout(2,7));
		/// controlDialog = new JDialog();
		setTitle(sceneViewer.name + " EyeAt - Adjust/Report");
		JPanel posPanel = new JPanel(new GridLayout(0, 1));
		///JPanel posPanel = new JPanel();
		add(posPanel);


		JPanel moveto_panel = new JPanel();
		posPanel.add(moveto_panel);


		Point3D center = new Point3D(sceneViewer.camera.position);
		posXfield = new JTextField(String.format("%.2f", center.x()));
		SmTrace.lg(String.format("setup posXfield"));
		posXfield.setActionCommand("emc_ENTER");
		posXfield.addActionListener(sceneViewer);
		posYfield = new JTextField(String.format("%.2f", center.y()));
		posYfield.setActionCommand("emc_ENTER");
		posYfield.addActionListener(sceneViewer);
		posZfield = new JTextField(String.format("%.2f", center.z()));
		posZfield.setActionCommand("emc_ENTER");
		posZfield.addActionListener(sceneViewer);

		moveto_panel.add(new JLabel("Loc:"));
		moveto_panel.add(new JLabel("x:"));
		moveto_panel.add(posXfield);
		moveto_panel.add(new JLabel("y:"));
		moveto_panel.add(posYfield);
		moveto_panel.add(new JLabel("z:"));
		moveto_panel.add(posZfield);
		JButton eyeAtButton = new JButton("EyeAt");
		eyeAtButton.setActionCommand("emc_eyeAtButton");
		eyeAtButton.addActionListener(sceneViewer);
		moveto_panel.add(eyeAtButton);

		///JPanel sizeto_panel = new JPanel();
		///posPanel.add(sizeto_panel);
		float defdim = .5f;		// Default dimensions

		// Adjust by
		JPanel adj_panel = new JPanel();
		posPanel.add(adj_panel);
		float adjamt = 1f; // Default adjustment
		adjXfield = new JTextField(String.format("%.2f", adjamt));
		adjXfield.setActionCommand("emc_adjENTER");
		adjXfield.addActionListener(sceneViewer);
		adjYfield = new JTextField(String.format("%.2f", adjamt));
		adjYfield.setActionCommand("emc_adjENTER");
		adjYfield.addActionListener(sceneViewer);
		adjZfield = new JTextField(String.format("%.2f", adjamt));
		adjZfield.setActionCommand("emc_adjENTER");
		adjZfield.addActionListener(sceneViewer);

		JButton adjUpButton = new JButton("Up By");
		adjUpButton.setActionCommand("emc_eyeAtAdjUpButton");
		adjUpButton.addActionListener(sceneViewer);
		JButton adjDownButton = new JButton("Down By");
		adjDownButton.setActionCommand("emc_eyeAtAdjDownButton");
		adjDownButton.addActionListener(sceneViewer);
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
 
		pack();


		int bindex2 = sceneViewer.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfEyeAt.setup after - selected(%d)", bindex2), "select");
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
		m3D_positioning_Button.setActionCommand("emc_m3D_eyeAt_PositionButton");
		m3D_panel.add(m3D_positioning_Button);
		m3D_positioning_Button.addActionListener(sceneViewer);
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
	public boolean EyeAt() throws EMBlockError {
		boolean ret = ckDoAction("eyeAtButton");
		if (!ret)
			SmTrace.lg("ControlOfEyeAt.eyeAt failed");
		return ret;
	}

	/**
	 * Look at location
	 * 
	 * @throws EMBlockError
	 */
	public boolean EyeAt(float x, float y, float z) throws EMBlockError {
		if (!posXfieldSet(x)) {
			SmTrace.lg(String.format("ControlOfEyeAt.Moveto(x=%g) failed", x));
			return false;
		}
		if (!posYfieldSet(y)) {
			SmTrace.lg(String.format("ControlOfEyeAt.Moveto(y=%g) failed", y));
			return false;
		}
		if (!posZfieldSet(z)) {
			SmTrace.lg(String.format("ControlOfEyeAt.Moveto(z=%g) failed", z));
			return false;
		}
		if (!EyeAt()) {
			SmTrace.lg(String.format("ControlOfEyeAt.Moveto move(%g, %g,%g) failed", x, y, z));
			return false;
		}
		return true;
	}

	private void traceSelected(int place) {
		int bindex = sceneViewer.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfEyeAt.setup place(%d) - selected(%d)", place, bindex), "select",
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
	private void eyeAtAdjustPosition(EMBCommand bcmd, int direction) throws EMBlockError {

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
		eyeAtPosition(bcmd);
	}

	/**
	 * Set eye(camera target) at position
	 * creating command, setting values, updating controls
	 * @throws EMBlockError
	 */
	public void setEyeAtPosition(Point3D pt) throws EMBlockError {
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
	 * Place eye at position
	 * creating command, setting values, updating controls
	 * @throws EMBlockError
	 */
	public void eyeAtPosition(float x, float y, float z) throws EMBlockError {
		if (!posXfieldSet(x))
			return;
		
		if (!posYfieldSet(y))
			return;
		
		if (!posZfieldSet(z))
			return;

		
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd("emc_eyeAt");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		SmTrace.lg(String.format("eyeAtPosition cmd x=%.2f y=%.2f z=%.2f", x, y, z));
		Point3D new_pt = new Point3D(x, y, z);
		if (setPoint == null)
			setPoint = new_pt;
		if (Point3D.diff(setPoint, new_pt).length() < minClose) {
			bcmd.checkPoint();
			setPoint(new_pt);
			bcmd.setCanUndo(false);
		}
		bcmd.setView(sceneViewer);
		bcmd.setEyeAt(new Point3D(x, y, z));
		bcmd.doCmd();
	}

	/**
	 * Set eye at position (command)
	 * 
	 * @throws EMBlockError
	 */
	private void eyeAtPosition(EMBCommand bcmd) throws EMBlockError {
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
		SmTrace.lg(String.format("look at x=%.2f y=%.2f z=%.2f", xval, yval, zval));
		bcmd.setEyeAt(new Point3D(xval, yval, zval));
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
		bcmd.setView(sceneViewer);
		switch (action) {

		case "emc_eyeAtButton":
			eyeAtPosition(bcmd);
			break;

		case "emc_ENTER":
			eyeAtPosition(bcmd);
			break;

		case "emc_eyeAtAdjUpButton":
			eyeAtAdjustPosition(bcmd, 1);
			break;

		case "emc_adjENTER":
			eyeAtAdjustPosition(bcmd, 1);
			break;

		case "emc_eyeAtAdjDownButton":
			eyeAtAdjustPosition(bcmd, -1);
			break;
			
		case "emc_m3D_eyeAt_PositionButton":
			m3DPositionSelect(bcmd);
			break;

		default:
			return false; // No action here
		}
		return bcmd.doCmd();

	}

	
	/**
	 * Select eyeAt via 3DPosition widget
	 * @throws EMBlockError 
	 */
	public void m3DPositionSelect(EMBCommand bcmd) throws EMBlockError {
		int width = 350;
		int height = 2*width+20;
		float xMin = -20;
		float xMax = 20;
		float yMin = xMin;
		float yMax = xMax;
		float zMin = xMin;
		float zMax = xMax;
		
		Point lapt = this.getLocation();
		lapt.translate(0, -height);
		JFrame frame = new JFrame();
		frame.setLocation(lapt);
        m3DPos = new EM3DPosition("EyeAt Position", frame,
			width,
			height,
			"xEyeAT", xMin, xMax,
			"yEyeAT", yMin, yMax,
			"zEyeAT", zMin, zMax
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

	/**
	 * reset to default setting
	 */
	public void reset() {
		setup = false;
		setup();
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
			eyeAtPosition(x,y,z);
		} catch (EMBlockError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
}
