import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ControlOfComponent extends ControlOf {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ControlOfComponent(SceneViewer scene, String name) {
		super(scene, name);
	}
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 */
	public void setup() {
		if (setup)
		return;					// Already setup
		
		setTitle("Add / Modify Block");
		JPanel blockPanel = new JPanel(new GridLayout(0, 1, 2, 2));		// any rows, 1 col, sep
		add(blockPanel);

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

		JButton addDeleteAllButton = new JButton("Delete ALL");
		addDeleteAllButton.setActionCommand("deleteBlockAllButton");
		addDeleteAllButton.addActionListener(scene);
		modPanel.add(addDeleteAllButton);
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

		JButton addTextButton = new JButton("Text");
		addTextButton.setActionCommand("addTextButton");
		addTextButton.addActionListener(scene);
		compPanel.add(addTextButton);
		blockPanel.add(compPanel);
		
		pack();
		setup = true;
	}

	/**
	 * Overridden control methods
	 */



	/**
	 * Check for and act on action
	 * @throws EMBlockError 
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		if (!isActive())
			return false;	// Not active
		
		scene.selectPrint(String.format("ckDoAction(%s)", action), "select");
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		switch (action) {
			case "deleteBlockButton":
			case "deleteBlockAllButton":
			case "duplicateBlockButton":
			case "addBoxButton":
			case "addBallButton":
			case "addConeButton":
			case "addCylinderButton":
			case "addTextButton":
				scene.addBlockButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					scene.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
			
				default:
					break;
		}
		return false;			// Event not processed by us - possibly by someone else
	}


}
