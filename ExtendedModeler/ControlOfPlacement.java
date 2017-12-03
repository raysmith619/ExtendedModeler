package ExtendedModeler;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;

import smTrace.SmTrace;

public class ControlOfPlacement extends ControlOf {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean pos_size_position = true; // position / size choice
	boolean pos_move_duplicate = true; // Move / Duplicate choice
	JTextField posXfield;
	JTextField posYfield;
	JTextField posZfield;
	JCheckBox posByBlockCkBox; // Treat position by block size

	JTextField sizeXfield;
	JTextField sizeYfield;
	JTextField sizeZfield;

	JTextField adjXfield;
	JTextField adjYfield;
	JTextField adjZfield;
	JCheckBox adjByBlockCkBox; // Adjust by units of block size

	ControlOfPlacement(SceneViewer scene, String name) {
		super(scene, name);
		///setup();
	}

	/**
	 * Setup Control / Display position of selected block
	 */
	public void setup() {
		if (setup)
			return; // Already present
		int bindex = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfPlacement.setup before - selected(%d)", bindex), "select");
		// JPanel panel = new JPanel(new GridLayout(2,7));
		/// controlDialog = new JDialog();
		setTitle("Placement - Adjust/Report");
		JPanel posPanel = new JPanel(new GridLayout(0, 1));
		add(posPanel);

		JRadioButton ps_pos_button = new JRadioButton("Position");
		ps_pos_button.setActionCommand("emc_ps_position");
		ps_pos_button.setSelected(true);
		ps_pos_button.addActionListener(scene);

		JRadioButton ps_size_button = new JRadioButton("Size");
		ps_size_button.setActionCommand("emc_ps_size");
		ps_size_button.setSelected(false);
		ps_size_button.addActionListener(scene);

		JPanel pos_size_panel = new JPanel();
		ButtonGroup pos_size_group = new ButtonGroup();
		pos_size_group.add(ps_pos_button);
		pos_size_group.add(ps_size_button);
		pos_size_panel.add(ps_pos_button);
		pos_size_panel.add(ps_size_button);
		posPanel.add(pos_size_panel);

		// Move / Duplicate choice
		JRadioButton mdmove_button = new JRadioButton("Move");
		mdmove_button.setMnemonic(KeyEvent.VK_M);
		mdmove_button.setActionCommand("emc_md_move");
		mdmove_button.setSelected(true);
		mdmove_button.addActionListener(scene);

		JRadioButton mddup_button = new JRadioButton("Duplicate");
		mddup_button.setMnemonic(KeyEvent.VK_D);
		mddup_button.setActionCommand("emc_md_duplicate");
		mddup_button.addActionListener(scene);

		JPanel movedup_panel = new JPanel();
		ButtonGroup movedup_group = new ButtonGroup();
		movedup_group.add(mdmove_button);
		movedup_group.add(mddup_button);
		movedup_panel.add(mdmove_button);
		movedup_panel.add(mddup_button);
		posPanel.add(movedup_panel);

		JPanel moveto_panel = new JPanel();
		posPanel.add(moveto_panel);

		traceSelected(11);
		Point3D center = new Point3D(-2, -2, -2);
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
		posByBlockCkBox = new JCheckBox("ByBlock", true);

		moveto_panel.add(new JLabel("Loc:"));
		moveto_panel.add(new JLabel("x:"));
		moveto_panel.add(posXfield);
		moveto_panel.add(new JLabel("y:"));
		moveto_panel.add(posYfield);
		moveto_panel.add(new JLabel("z:"));
		moveto_panel.add(posZfield);
		moveto_panel.add(posByBlockCkBox);
		JButton moveToButton = new JButton("MoveTo");
		moveToButton.setActionCommand("emc_moveToButton");
		moveToButton.addActionListener(scene);
		moveto_panel.add(moveToButton);

		JPanel sizeto_panel = new JPanel();
		posPanel.add(sizeto_panel);
		float defdim = .5f;		// Default dimensions
		sizeXfield = new JTextField(String.format("%.2f", defdim));
		SmTrace.lg(String.format("setup sizeXfield"));
		sizeXfield.setActionCommand("emc_ENTER");
		sizeXfield.addActionListener(scene);
		sizeYfield = new JTextField(String.format("%.2f", defdim));
		sizeYfield.setActionCommand("emc_ENTER");
		sizeYfield.addActionListener(scene);
		sizeZfield = new JTextField(String.format("%.2f", defdim));
		sizeZfield.setActionCommand("emc_ENTER");
		sizeZfield.addActionListener(scene);
		sizeto_panel.add(Box.createVerticalStrut(15)); // a spacer
		sizeto_panel.add(new JLabel("Size:"));
		sizeto_panel.add(new JLabel("x:"));
		sizeto_panel.add(sizeXfield);
		sizeto_panel.add(new JLabel("y:"));
		sizeto_panel.add(sizeYfield);
		sizeto_panel.add(new JLabel("z:"));
		sizeto_panel.add(sizeZfield);

		JButton sizeToButton = new JButton("SizeTo");
		sizeToButton.setActionCommand("emc_sizeToButton");
		sizeToButton.addActionListener(scene);
		sizeto_panel.add(sizeToButton);

		// Adjust by
		JPanel adj_panel = new JPanel();
		posPanel.add(adj_panel);
		float adjamt = 1f; // Default adjustment
		adj_panel.add(Box.createVerticalStrut(15)); // a spacer
		adjXfield = new JTextField(String.format("%.2f", adjamt));
		adjXfield.setActionCommand("emc_adjENTER");
		adjXfield.addActionListener(scene);
		adjYfield = new JTextField(String.format("%.2f", adjamt));
		adjYfield.setActionCommand("emc_adjENTER");
		adjYfield.addActionListener(scene);
		adjZfield = new JTextField(String.format("%.2f", adjamt));
		adjZfield.setActionCommand("emc_adjENTER");
		adjZfield.addActionListener(scene);
		adjByBlockCkBox = new JCheckBox("ByBlock", true);

		JButton adjUpButton = new JButton("Up By");
		adjUpButton.setActionCommand("emc_adjUpButton");
		adjUpButton.addActionListener(scene);
		JButton adjDownButton = new JButton("Down By");
		adjDownButton.setActionCommand("emc_adjDownButton");
		adjDownButton.addActionListener(scene);
		adj_panel.add(Box.createVerticalStrut(15)); // a spacer
		adj_panel.add(new JLabel("Adj:"));
		adj_panel.add(new JLabel("x:"));
		adj_panel.add(adjXfield);
		adj_panel.add(new JLabel("y:"));
		adj_panel.add(adjYfield);
		adj_panel.add(new JLabel("z:"));
		adj_panel.add(adjZfield);
		adj_panel.add(adjByBlockCkBox);
		adj_panel.add(adjUpButton);
		adj_panel.add(adjDownButton);
		pack();

		int bindex2 = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfPlacement.setup after - selected(%d)", bindex2), "select");
		setup = true;
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
		if (getAdjByBlock())
			tx *= getSizeX();
		float ty = getAdjY();
		if (getAdjByBlock())
			ty *= getSizeY();
		float tz = getAdjZ();
		if (getAdjByBlock())
			tz *= getSizeZ();
		return new Vector3D(tx, ty, tz);
	}

	public boolean getAdjByBlock() throws EMBlockError {
		boolean val = false;
		if (adjByBlockCkBox != null) {
			val = adjByBlockCkBox.isSelected();
		}
		return val;
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

	public float getSizeX() throws EMBlockError {
		if (sizeXfield == null)
			return 0;
		String text = sizeXfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getSizeY() throws EMBlockError {
		if (posYfield == null)
			return 0;
		String text = sizeYfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public float getSizeZ() throws EMBlockError {
		if (sizeZfield == null)
			return 0;
		String text = sizeZfield.getText();
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			throw new EMBlockError(String.format("%s not valid: %s", text, e.getMessage()));
		}
	}

	public AlignedBox3D getBox() throws EMBlockError {
		Point3D p0 = new Point3D(getPosX(), getPosY(), getPosZ());
		Point3D p1 = new Point3D(getPosX() + getSizeX(), getPosY() + getSizeY(), getPosZ() + getSizeZ());
		AlignedBox3D box = new AlignedBox3D(p0, p1);
		return box;
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
		String val_str = Float.toString(val);
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
		String val_str = Float.toString(val);
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
		String val_str = Float.toString(val);
		return posZfieldSet(val_str);
	}

	/**
	 * MoveTo button simulation
	 * 
	 * @throws EMBlockError
	 */
	public boolean MoveTo() throws EMBlockError {
		boolean ret = ckDoAction("moveToButton");
		if (!ret)
			SmTrace.lg("ControlOfPlacement.MoveTo failed");
		return ret;
	}

	/**
	 * MoveTo location
	 * 
	 * @throws EMBlockError
	 */
	public boolean MoveTo(float x, float y, float z) throws EMBlockError {
		if (!posXfieldSet(x)) {
			SmTrace.lg(String.format("ControlOfPlacement.Moveto(x=%g) failed", x));
			return false;
		}
		if (!posYfieldSet(y)) {
			SmTrace.lg(String.format("ControlOfPlacement.Moveto(y=%g) failed", y));
			return false;
		}
		if (!posZfieldSet(z)) {
			SmTrace.lg(String.format("ControlOfPlacement.Moveto(z=%g) failed", z));
			return false;
		}
		if (!MoveTo()) {
			SmTrace.lg(String.format("ControlOfPlacement.Moveto move(%g, %g,%g) failed", x, y, z));
			return false;
		}
		return true;
	}

	private void traceSelected(int place) {
		int bindex = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfPlacement.setup place(%d) - selected(%d)", place, bindex), "select",
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
	private void adjustPosition(EMBCommand bcmd, int direction) throws EMBlockError {
		EMBlock[] cbs = scene.getSelectedBlocks();
		if (cbs.length == 0)
			return;

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
			if (pos_size_position) {
				adj_xval *= -1;
				adj_yval *= -1;
				adj_zval *= -1;
			} else {
				adj_xval *= -1;
				adj_yval *= -1;
				adj_zval *= -1;
			}
		}

		BlockSelect new_selected = new BlockSelect();	// Record newly selected
		for (int i = 0; i < cbs.length; i++) {
			EMBlock cb = cbs[i];
			if (!pos_move_duplicate) {
				bcmd.addBlock(cb); // Duplicate --> add original to new blocks ( as well as the newly positioned
									// block)
			}
			
			bcmd.addPrevBlock(cb); // Save copy for undo/redo
			EMBlock cb1 = cb.duplicate(); // New or modified
	
			Point3D base_point = cb.getBasePoint();
			Vector3D adj_vector = new Vector3D(adj_xval, adj_yval, adj_zval);
			Point3D adj_point = Point3D.sum(base_point, adj_vector);
			if (pos_size_position) {
				cb1.moveTo(adj_point);
				System.out.println(
						String.format("adjust to x=%.2f y=%.2f z=%.2f", adj_point.x(), adj_point.y(), adj_point.z()));
			} else {
				cb1.resize(adj_vector);
				Vector3D size = cb1.getSize();
				System.out.println(String.format("adjust size to x=%.2f y=%.2f z=%.2f", size.x(), size.y(), size.z()));
			}
			bcmd.addBlock(cb1); // Add New / modified block
			new_selected.addIndex(cb1.iD());
		}
		bcmd.setSelect(new_selected);
	}

	/**
	 * Move to position/size accordingly
	 * 
	 * @throws EMBlockError
	 */
	private void moveToPosition(EMBCommand bcmd) throws EMBlockError {
		if (scene.getSelected() == null)
			return;
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

		EMBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;

		if (!pos_move_duplicate) {
			bcmd.addBlock(cb); // Keep original also
		}
		bcmd.addPrevBlock(cb); // Save original, for undo
		EMBlock cb1 = cb.duplicate(); // New or modified

		Point3D new_point = new Point3D(xval, yval, zval);
		if (pos_size_position) {
			cb1.moveTo(new_point);
		} else {
			Vector3D size = new Vector3D(new_point);
			cb1.resize(0, size);
		}
		bcmd.addBlock(cb1); // Save new or modified
		SmTrace.lg(String.format("move to x=%.2f y=%.2f z=%.2f", xval, yval, zval));
		scene.repaint();
	}

	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		EMBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;

		if (setup) {
			Point3D min = cb.getMin();
			posXfield.setText(String.format("%.2g", min.x()));
			posYfield.setText(String.format("%.2g", min.y()));
			posZfield.setText(String.format("%.2g", min.z()));

			Vector3D size = cb.getSize();
			adjXfield.setText(String.format("%.2g", size.x()));
			adjYfield.setText(String.format("%.2g", size.y()));
			adjZfield.setText(String.format("%.2g", size.z()));

			repaint();
		}
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
		case "emc_md_move":
			pos_move_duplicate = true;
			break;

		case "emc_md_duplicate":
			pos_move_duplicate = false;
			break;

		case "emc_ps_position":
			pos_size_position = true;
			break;

		case "emc_ps_size":
			pos_size_position = false;
			break;

		case "emc_moveToButton":
			moveToPosition(bcmd);
			break;

		case "emc_ENTER":
			moveToPosition(bcmd);
			break;

		case "emc_adjUpButton":
			adjustPosition(bcmd, 1);
			break;

		case "emc_adjENTER":
			adjustPosition(bcmd, 1);
			break;

		case "emc_adjDownButton":
			adjustPosition(bcmd, -1);
			break;

		default:
			return false; // No action here
		}
		return bcmd.doCmd();

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
		Vector3D tran = getAdj();
		cb.translate(tran);
	}

	/**
	 * Set control UI based on current block settings
	 * 
	 * @param cb
	 */
	public void setControl(EMBlock cb) {
		setControl(cb.getBase());
	}

	/**
	 * Set control UI based on current block settings
	 * 
	 * @param cb
	 */
	public void setControl(EMBlockBase cb) {
		setPos(cb.getPos());
		setSize(cb.getSize());

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

	public void setSize(Vector3D cbSize) {
		Float val;
		String text;

		val = cbSize.x();
		text = String.format("%.2g", val);
		sizeXfield.setText(text);

		val = cbSize.y();
		text = String.format("%.2g", val);
		sizeYfield.setText(text);

		val = cbSize.z();
		text = String.format("%.2g", val);
		sizeZfield.setText(text);
	}
}
