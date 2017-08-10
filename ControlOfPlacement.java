import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;

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
	
	JTextField adjposXfield;
	JTextField adjposYfield;
	JTextField adjposZfield;

	ControlOfPlacement(SceneViewer scene, String name) {
		super(scene, name);
		setup();
	}	

	/**
	 * Setup Control / Display  position of selected block
	 */
	public void setup() {
		if (controlActive)
			return;					// Already present
		
		// JPanel panel = new JPanel(new GridLayout(2,7));
///		controlDialog = new JDialog();
		setTitle("Adjust/Report Position");
		JPanel posPanel = new JPanel(new GridLayout(0, 1));
		add(posPanel);

		JRadioButton ps_pos_button = new JRadioButton("Position");
		ps_pos_button.setActionCommand("ps_position");
		ps_pos_button.setSelected(true);
		ps_pos_button.addActionListener(scene);

		JRadioButton ps_size_button = new JRadioButton("Size");
		ps_size_button.setActionCommand("ps_size");
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
		mdmove_button.setActionCommand("md_move");
		mdmove_button.setSelected(true);
		mdmove_button.addActionListener(scene);
		
		JRadioButton mddup_button = new JRadioButton("Duplicate");
		mddup_button.setMnemonic(KeyEvent.VK_D);
		mddup_button.setActionCommand("md_duplicate");
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

		// JComboBox<String> combo = new JComboBox<>();
		OurBlock cb = scene.getSelected();
		Point3D center = new Point3D(0,0,0);
		if (cb != null)
			center = cb.getCenter();
		posXfield = new JTextField(String.format("%.2f", center.x()));
		posXfield.setActionCommand("ENTER");
		posXfield.addActionListener(scene);
		posYfield = new JTextField(String.format("%.2f", center.y()));
		posYfield.setActionCommand("ENTER");
		posYfield.addActionListener(scene);
		posZfield = new JTextField(String.format("%.2f", center.z()));
		posZfield.setActionCommand("ENTER");
		posZfield.addActionListener(scene);
		JButton moveToButton = new JButton("MoveTo");
		moveToButton.setActionCommand("moveToButton");
		moveToButton.addActionListener(scene);
		/// panel.add(combo);
		moveToButton.setHorizontalAlignment(SwingConstants.CENTER);
		moveto_panel.add(new JLabel("x-coord:"));
		moveto_panel.add(posXfield);
		moveto_panel.add(new JLabel("y-coord:"));
		moveto_panel.add(posYfield);
		moveto_panel.add(new JLabel("z-coord:"));
		moveto_panel.add(posZfield);
		moveto_panel.add(Box.createVerticalStrut(15)); // a spacer
		moveto_panel.add(moveToButton);

		// Adjust by
		JPanel adjpos_panel = new JPanel();
		posPanel.add(adjpos_panel);
		float adjamt = .1f; // Default adjustment
		adjpos_panel.add(Box.createVerticalStrut(15)); // a spacer
		adjposXfield = new JTextField(String.format("%.2f", adjamt));
		adjposXfield.setActionCommand("adjposENTER");
		adjposXfield.addActionListener(scene);
		adjposYfield = new JTextField(String.format("%.2f", adjamt));
		adjposYfield.setActionCommand("adjposENTER");
		adjposYfield.addActionListener(scene);
		adjposZfield = new JTextField(String.format("%.2f", adjamt));
		adjposZfield.setActionCommand("adjposENTER");
		adjposZfield.addActionListener(scene);
		JButton adjposUpButton = new JButton("Up By");
		adjposUpButton.setActionCommand("adjposUpButton");
		adjposUpButton.addActionListener(scene);
		JButton adjposDownButton = new JButton("Down By");
		adjposDownButton.setActionCommand("adjposDownButton");
		adjposDownButton.addActionListener(scene);
		adjpos_panel.add(Box.createVerticalStrut(15)); // a spacer
		adjpos_panel.add(new JLabel("x-adj:"));
		adjpos_panel.add(adjposXfield);
		adjpos_panel.add(new JLabel("y-adj:"));
		adjpos_panel.add(adjposYfield);
		adjpos_panel.add(new JLabel("z-adj:"));
		adjpos_panel.add(adjposZfield);
		adjpos_panel.add(adjposUpButton);
		adjpos_panel.add(adjposDownButton);
		pack();
	}
	
	
	
	/**
	 * Adjust Position
	 * 
	 **/
	/*
	 * Adjust by increments in direction 1- positive, -1 negative
	 * 
	 * @param direction
	 */
	private void adjustPosition(int direction) {
		if (scene.getSelected() == null)
			return;
		
		float adjpos_xval = 0;
		float adjpos_yval = 0;
		float adjpos_zval = 0;
		if (adjposXfield != null) {
			String text = adjposXfield.getText();
			adjpos_xval = Float.valueOf(text);
		}
		if (adjposYfield != null) {
			String text = adjposYfield.getText();
			adjpos_yval = Float.valueOf(text);
		}
		if (adjposZfield != null) {
			String text = adjposZfield.getText();
			adjpos_zval = Float.valueOf(text);
		}
		if (direction < 0) {
			if (pos_size_position) {
				adjpos_xval *= -1;
				adjpos_yval *= -1;
				adjpos_zval *= -1;
			} else {
				adjpos_xval *= -1;
				adjpos_yval *= -1;
				adjpos_zval *= -1;				
			}
		}
		OurBlock cb = scene.getSelected();
		Point3D base_point = cb.getBasePoint();
		Vector3D adjpos_vector = new Vector3D(adjpos_xval, adjpos_yval, adjpos_zval);
		Point3D new_point = Point3D.sum(base_point, adjpos_vector);
		if (!pos_move_duplicate) {
			cb = cb.duplicate();
			scene.addBlock(cb);
		}
		if (pos_size_position) {
			cb.moveTo(new_point);
			System.out
					.println(String.format("adjust to x=%.2f y=%.2f z=%.2f", new_point.x(), new_point.y(), new_point.z()));
		} else {
			cb.resize(adjpos_vector);
			Vector3D size = cb.getSize();
			System.out
			.println(String.format("adjust size to x=%.2f y=%.2f z=%.2f", size.x(), size.y(), size.z()));
		}
		scene.repaint();
	}

	/**
	 * Move to position/size accordingly
	 */
	private void moveToPosition() {
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

		OurBlock cb = scene.getSelected();
		Point3D new_point = new Point3D(xval, yval, zval);
		if (!pos_move_duplicate) {
			cb = cb.duplicate();
			scene.addBlock(cb);
		}
		if (pos_size_position) {
			cb.moveTo(new_point);
		} else {
			Vector3D size = new Vector3D(new_point);
			cb.resize(0, size);
		}
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f", xval, yval, zval));
		scene.repaint();
	}


	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		OurBlock cb = scene.getSelected();
		if (cb == null)
			return;

		if (controlActive) {
			Point3D min = cb.getMin();
			posXfield.setText(String.format("%.2g", min.x()));
			posYfield.setText(String.format("%.2g", min.y()));
			posZfield.setText(String.format("%.2g", min.z()));

			Vector3D size = cb.getSize();
			adjposXfield.setText(String.format("%.2g",size.x()));
			adjposYfield.setText(String.format("%.2g",size.y()));
			adjposZfield.setText(String.format("%.2g",size.z()));

			repaint();
		}
	}


	/**
	 * Check for and act on action
	 */
	public boolean ckDoAction(String action) {
		switch (action) {
			case"md_move":
				pos_move_duplicate = true;
				break;
				
			case "md_duplicate":
				pos_move_duplicate = false;
				break;
				
			case "ps_position":
				pos_size_position = true;
				break;
				
			case "ps_size":
				pos_size_position = false;
				break;
				
			case "moveToButton":
				moveToPosition();
				break;
				
			case "ENTER":
				moveToPosition();
				break;
				
			case "adjposUpButton":
				adjustPosition(1);
				break;
				
			case "adjposENTER":
				adjustPosition(1);
				break;

			case "adjposDownButton":
				adjustPosition(-1);
				break;

			case "deleteBlockButton":
			case "deleteBlockAllButton":
			case "duplicateBlockButton":
			case "addBoxButton":
			case "addBallButton":
			case "addConeButton":
			case "addCylinderButton":
				scene.addBlockButton(action);
				break;
			
				default:
					return false;		// No action here
		}
		return true;					// Got action

	}


}
