import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;

public class SceneControlDialog {

	SceneViewer scene;		// Our scene
	JDialog controlDialog; 	// control dialog box

	
	SceneControlDialog(SceneViewer scene) {
		this.scene = scene;
		setup();
	}
	/**
	 * Remove Control
	 */
	public void controlDispose() {
		if (controlDialog != null) {
			controlDialog.dispose();
			controlDialog = null;
		}
	}
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 */
	public void setup() {
		if (controlDialog != null)
			return;					// Already present
		
		// JPanel panel = new JPanel(new GridLayout(2,7));
		controlDialog = new JDialog();
		controlDialog.setTitle("Add / Modify Block");
		JPanel blockPanel = new JPanel(new GridLayout(0, 1, 2, 2));		// any rows, 1 col, sep
		controlDialog.add(blockPanel);

		JPanel modPanel = new JPanel(new GridLayout(1, 0));		// Modifiers in one row
		JButton addDuplicateButton = new JButton("Duplicate");
		addDuplicateButton.setActionCommand("duplicateBlockButton");
		addDuplicateButton.addActionListener(scene);
		modPanel.add(addDuplicateButton);

		JButton addDeleteButton = new JButton("Delete");
		addDeleteButton.setActionCommand("deleteBlockButton");
		addDeleteButton.addActionListener(scene);
		modPanel.add(addDeleteButton);
		blockPanel.add(modPanel);

		JPanel compPanel = new JPanel(new GridLayout(1,0));		// Components - one row
		JButton addBoxButton = new JButton("Box");
		addBoxButton.setActionCommand("addBoxButton");
		addBoxButton.addActionListener(scene);
		compPanel.add(addBoxButton);

		JButton addBallButton = new JButton("Ball");
		addBallButton.setActionCommand("addBallButton");
		addBallButton.addActionListener(scene);
		compPanel.add(addBallButton);

		JButton addConeButton = new JButton("Cone");
		addConeButton.setActionCommand("addConeButton");
		addConeButton.addActionListener(scene);
		compPanel.add(addConeButton);

		JButton addCylinderButton = new JButton("Cylinder");
		addCylinderButton.setActionCommand("addCylinderButton");
		addCylinderButton.addActionListener(scene);
		compPanel.add(addCylinderButton);
		blockPanel.add(compPanel);
		
		controlDialog.pack();
		updatePosition();
	}

	/**
	 * Required control methods
	 */
	public void setVisible(boolean visible) {
		controlDialog.setVisible(visible);
	}

	public void setLocation(Point point) {
		controlDialog.setLocation(point);
	}

	public int getHeight() {
		return controlDialog.getHeight();
	}
	
	public void dispose() {
		controlDialog.dispose();
	}
	
	
	/**
	 * Update position in scene display
	 */
	void updatePosition() {
		scene.setControlPosition(this, "addControl");
		controlDialog.setVisible(true);
	}
}
