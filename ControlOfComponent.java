package ExtendedModeler;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ControlOfComponent extends ControlOfScene {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ControlOfComponent(SceneControler sceneControler, String name) {
		super(sceneControler, name);
	}

	/**
	 * reset to default setting
	 */
	public void reset() {
		setup = false;
		setup();
	}
	
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 */
	public void setup() {
		if (setup)
		return;					// Already setup
		
		setTitle("Block - Select / Add / Modify");
		JPanel blockPanel = new JPanel(new GridLayout(0, 1, 2, 2));		// any rows, 1 col, sep
		add(blockPanel);

		JPanel selectPanel = new JPanel(new GridLayout(1, 0));		// Modifiers in one row
		JButton addSelectAllButton = new JButton("Select All");
		addSelectAllButton.setActionCommand("emc_selectAllButton");
		addSelectAllButton.addActionListener(sceneControler);
		selectPanel.add(addSelectAllButton);
		/***
		JButton addToggleSelectButton = new JButton("Toggle Select");
		addToggleSelectButton.setActionCommand("emc_toggleSelectButton");
		addToggleSelectButton.addActionListener(sceneControler);
		selectPanel.add(addToggleSelectButton);
		***/
		blockPanel.add(selectPanel);
		
		JPanel modPanel = new JPanel(new GridLayout(1, 0));		// Modifiers in one row
		JButton addDuplicateButton = new JButton("Duplicate");
		addDuplicateButton.setActionCommand("emc_duplicateBlockButton");
		addDuplicateButton.addActionListener(sceneControler);
		modPanel.add(addDuplicateButton);

		JButton addDeleteButton = new JButton("Delete");
		addDeleteButton.setActionCommand("emc_deleteBlockButton");
		addDeleteButton.addActionListener(sceneControler);
		modPanel.add(addDeleteButton);
		blockPanel.add(modPanel);

		JButton addDeleteAllButton = new JButton("Delete ALL");
		addDeleteAllButton.setActionCommand("emc_deleteBlockAllButton");
		addDeleteAllButton.addActionListener(sceneControler);
		modPanel.add(addDeleteAllButton);
		blockPanel.add(modPanel);

		JPanel compPanel = new JPanel(new GridLayout(1,0));		// Components - one row
		JButton addBoxButton = new JButton("Box");
		addBoxButton.setActionCommand("emc_addBoxButton");
		addBoxButton.addActionListener(sceneControler);
		compPanel.add(addBoxButton);

		JButton addBallButton = new JButton("Ball");
		addBallButton.setActionCommand("emc_addBallButton");
		addBallButton.addActionListener(sceneControler);
		compPanel.add(addBallButton);

		JButton addConeButton = new JButton("Cone");
		addConeButton.setActionCommand("emc_addConeButton");
		addConeButton.addActionListener(sceneControler);
		compPanel.add(addConeButton);

		JButton addCylinderButton = new JButton("Cylinder");
		addCylinderButton.setActionCommand("emc_addCylinderButton");
		addCylinderButton.addActionListener(sceneControler);
		compPanel.add(addCylinderButton);
		blockPanel.add(compPanel);

		JButton addTextButton = new JButton("Text");
		addTextButton.setActionCommand("emc_addTextButton");
		addTextButton.addActionListener(sceneControler);
		compPanel.add(addTextButton);
		blockPanel.add(compPanel);

		JButton addEyeButton = new JButton("Eye");
		addEyeButton.setActionCommand("emc_addEyeButton");
		addEyeButton.addActionListener(sceneControler);
		compPanel.add(addEyeButton);
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
		
		sceneControler.selectPrint(String.format("ckDoAction(%s)", action), "select");
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		switch (action) {
			case "emc_selectAllButton":
			case "emc_toggleSelectButton":
			case "emc_deleteBlockButton":
			case "emc_deleteBlockAllButton":
			case "emc_duplicateBlockButton":
			case "emc_addBoxButton":
			case "emc_addBallButton":
			case "emc_addConeButton":
			case "emc_addCylinderButton":
				sceneControler.addBlockButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
				
			case "emc_addTextButton":		// Emulate Text control
				sceneControler.addTextButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
				
			case "emc_addEyeButton":		// Test eye drawing
				sceneControler.addEyeButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
			
				default:
					break;
		}
		return false;			// Event not processed by us - possibly by someone else
	}


}
