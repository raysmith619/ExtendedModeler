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
		
		setTitle("Block - Add / Modify");
		JPanel blockPanel = new JPanel(new GridLayout(0, 1, 2, 2));		// any rows, 1 col, sep
		add(blockPanel);

		JPanel modPanel = new JPanel(new GridLayout(1, 0));		// Modifiers in one row
		JButton addDuplicateButton = new JButton("Duplicate");
		addDuplicateButton.setActionCommand("emc_duplicateBlockButton");
		addDuplicateButton.addActionListener(scene);
		modPanel.add(addDuplicateButton);

		JButton addDeleteButton = new JButton("Delete");
		addDeleteButton.setActionCommand("emc_deleteBlockButton");
		addDeleteButton.addActionListener(scene);
		modPanel.add(addDeleteButton);
		blockPanel.add(modPanel);

		JButton addDeleteAllButton = new JButton("Delete ALL");
		addDeleteAllButton.setActionCommand("emc_deleteBlockAllButton");
		addDeleteAllButton.addActionListener(scene);
		modPanel.add(addDeleteAllButton);
		blockPanel.add(modPanel);

		JPanel compPanel = new JPanel(new GridLayout(1,0));		// Components - one row
		JButton addBoxButton = new JButton("Box");
		addBoxButton.setActionCommand("emc_addBoxButton");
		addBoxButton.addActionListener(scene);
		compPanel.add(addBoxButton);

		JButton addBallButton = new JButton("Ball");
		addBallButton.setActionCommand("emc_addBallButton");
		addBallButton.addActionListener(scene);
		compPanel.add(addBallButton);

		JButton addConeButton = new JButton("Cone");
		addConeButton.setActionCommand("emc_addConeButton");
		addConeButton.addActionListener(scene);
		compPanel.add(addConeButton);

		JButton addCylinderButton = new JButton("Cylinder");
		addCylinderButton.setActionCommand("emc_addCylinderButton");
		addCylinderButton.addActionListener(scene);
		compPanel.add(addCylinderButton);
		blockPanel.add(compPanel);

		JButton addTextButton = new JButton("Text");
		addTextButton.setActionCommand("emc_addTextButton");
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
			case "emc_deleteBlockButton":
			case "emc_deleteBlockAllButton":
			case "emc_duplicateBlockButton":
			case "emc_addBoxButton":
			case "emc_addBallButton":
			case "emc_addConeButton":
			case "emc_addCylinderButton":
			case "emc_addTextButton":
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
