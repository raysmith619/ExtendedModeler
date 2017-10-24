import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

public class ControlOfText extends ControlOf {
	/**
	 * Uses ControlOfPlacement for initial placement
	 */
	JTextField textStringTxFld;
	JTextField fontNameTxFld;
	JCheckBox boldCkBox;
	JCheckBox italicCkBox;
	JCheckBox underlineCkBox;
	
	JTextField textXDirTxFld;
	JTextField textYDirTxFld;
	JTextField textZDirTxFld;
	JCheckBox textDirTxByCharCkBox;
	
	JTextField charXSizeTxFld;
	JTextField charYSizeTxFld;
	JTextField charZSizeTxFld;
	JCheckBox charSizeByBlockCkBox;

	JCheckBox charDirWithTextCkBox;		// Char orients with text dir
	JTextField charXDirTxFld;
	JTextField charYDirTxFld;
	JTextField charZDirTxFld;
	JCheckBox charDirByBlockCkBox;		// In units of block sizes

	JButton textNewButton;
	JButton textDupButton;
	JButton textSelectButton;	// Populate with selected Block's properties
	JCheckBox positionAsNextCkBlock;		// In text direction, spacing
	JCheckBox positionOnCurrentCkBlock;		// On current block location
	
										// Action
	JCheckBox actionAsNextCkBox;		// At next logical location
	JCheckBox actionAtCurrentCkBox;		// At current position
	
	ControlOfText(SceneViewer scene, String name) {
		super(scene, name);
		setup();
	}	
	
	/**
	 * Setup Control / Display  position of selected block
	 */
	public void setup() {
		if (setup)
			return;					// Already present
		
		int bindex = scene.getSelectedBlockIndex();
		SmTrace.lg(String.format("ControlOfText.setup before - selected(%d)", bindex), "select");
		// JPanel panel = new JPanel(new GridLayout(2,7));
///		controlDialog = new JDialog();
		setTitle("Text - Create / Ajust / Report");
		JPanel controlPanel = new JPanel(new GridLayout(0, 1));
		add(controlPanel);
		//controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
		///controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT );
		
		JPanel textPanel = new JPanel();
		controlPanel.add(textPanel);

		textPanel.add(new JLabel("Text"));
		textStringTxFld = new JTextField("Enter text");
		textPanel.add(textStringTxFld);
		textPanel.add(new JLabel("Font"));
		fontNameTxFld = new JTextField("Font name");
		textPanel.add(fontNameTxFld);

		boldCkBox = new JCheckBox("Bold");
		textPanel.add(boldCkBox);
		italicCkBox = new JCheckBox("Italic");
		textPanel.add(italicCkBox);
		underlineCkBox = new JCheckBox("Underline");
		textPanel.add(underlineCkBox);
		///pack();
		
		JPanel textPropPanel = new JPanel();
		///textPropPanel.setAlignmentX(Component.LEFT_ALIGNMENT );
		controlPanel.add(textPropPanel);
		///textPropPanel.setAlignmentX(Component.LEFT_ALIGNMENT );
		textPropPanel.add(new JLabel("Text:"));
		textPropPanel.add(new JLabel("Dir"));
		
		textPropPanel.add(new JLabel("x"));
		textXDirTxFld = new JTextField("1");
		textPropPanel.add(textXDirTxFld);
		
		textPropPanel.add(new JLabel("y"));
		textYDirTxFld = new JTextField("1");
		textPropPanel.add(textYDirTxFld);
		
		textPropPanel.add(new JLabel("z"));
		textZDirTxFld = new JTextField("1");
		textPropPanel.add(textZDirTxFld);
		
		textDirTxByCharCkBox = new JCheckBox("ByChar", true);
		textPropPanel.add(textDirTxByCharCkBox);
		
		JPanel charPanel = new JPanel();
		controlPanel.add(charPanel);
		charPanel.add(new JLabel("Char:"));

		charPanel.add(new JLabel("Dir"));
		
		charDirWithTextCkBox = new JCheckBox("WithText", true);
		charPanel.add(charDirWithTextCkBox);
		charPanel.add(new JLabel("x"));
		charXDirTxFld = new JTextField("0");
		charPanel.add(charXDirTxFld);
		
		charPanel.add(new JLabel("y"));
		charYDirTxFld = new JTextField("0");
		charPanel.add(charYDirTxFld);
		
		charPanel.add(new JLabel("z"));
		charZDirTxFld = new JTextField("0");
		charPanel.add(charZDirTxFld);

		charPanel.add(new JLabel("Size"));
		
		charPanel.add(new JLabel("x"));
		charXSizeTxFld = new JTextField("1");
		charPanel.add(charXSizeTxFld);
		
		charPanel.add(new JLabel("y"));
		charYSizeTxFld = new JTextField("1");
		charPanel.add(charYSizeTxFld);
		
		charPanel.add(new JLabel("z"));
		charZSizeTxFld = new JTextField("1");
		charPanel.add(charZSizeTxFld);
		
		
		charSizeByBlockCkBox = new JCheckBox("ByBlock");
		charPanel.add(charSizeByBlockCkBox);
		pack();
		
		JPanel actionPanel = new JPanel();
		controlPanel.add(actionPanel);

		JButton textNewButton = new JButton("New");
		textNewButton.setActionCommand("text_new");
		textNewButton.addActionListener(scene);
		actionPanel.add(textNewButton);
		
		JButton textDupButton = new JButton("Dup");
		textNewButton.setActionCommand("text_dup");
		textNewButton.addActionListener(scene);
		actionPanel.add(textDupButton);
		
		JButton textSelectButton = new JButton("Select");
		textNewButton.setActionCommand("text_select");
		textNewButton.addActionListener(scene);
		actionPanel.add(textSelectButton);
		actionAsNextCkBox = new JCheckBox("AsNext", true);
		actionPanel.add(actionAsNextCkBox);
		actionAtCurrentCkBox = new JCheckBox("AtCurrent");
		actionPanel.add(actionAtCurrentCkBox);
		
		pack();
		setup = true;
		
	}
	
	


	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		EMBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;

		if (setup) {

			repaint();
		}
	}


	/**
	 * Check for and act on action
	 * @throws EMBlockError 
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		EMBCommand bcmd;
		if (!isActive())
			return false;	// Not active

		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		switch (action) {
			case"text_new":
				break;
				
			case "text_dup":
				break;
				
			case "text_select":
				break;
			
				default:
					return false;		// No action here
		}
		return bcmd.doCmd();

	}


}
