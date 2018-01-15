package ExtendedModeler;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;

import smTrace.SmTrace;

public class ControlOfText extends ControlOfScene {
	/**
	 * Uses ControlOfPlacement for initial placement
	 */
	JTextField textStringTxFld;
	JComboBox fontCBox;
	JCheckBox boldCkBox;
	JCheckBox italicCkBox;
	JCheckBox underlineCkBox;
	JTextField textFontSizeTxFld;
	
	JTextField textXDirTxFld;
	JTextField textYDirTxFld;
	JTextField textZDirTxFld;
	JCheckBox textDirByCharCkBox;
	
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
	JRadioButton positionAtNextRaButton;		// In text direction, spacing
	JRadioButton positionAtCurrRaButton;		// On current block location

	
	
	ControlOfText(SceneControler sceneControler, String name) {
		super(sceneControler, name);
		///setup();
	}	

	/**
	 * reset to default setting
	 */
	public void reset() {
		setup = false;
		setup();
	}
	
	/**
	 * Setup Control / Display  position of selected block
	 */
	public void setup() {
		if (setup)
			return;					// Already present
		
		int bindex = sceneControler.getSelectedBlockIndex();
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
		textStringTxFld = new JTextField("~~~");
		textPanel.add(textStringTxFld);
		textPanel.add(new JLabel("Font"));
		Font font = ColoredText.getFont();
		String font_name = font.getFontName();
		String[] font_names = getFontNames();
		int fnindex = 0;
		for (int i = 0; i < font_names.length; i++) {
			if (font_name.startsWith(font_names[i])) {
				fnindex = i;
				break;
			}
		}
		fontCBox = new JComboBox(font_names);
		fontCBox.setSelectedIndex(fnindex);
		textPanel.add(fontCBox);
		///fontNameTxFld = new JTextField(font.getFontName());
		///textPanel.add(fontNameTxFld);
		textPanel.add(new JLabel("Size"));
		textFontSizeTxFld = new JTextField(String.valueOf(font.getSize()));
		textPanel.add(textFontSizeTxFld);

		boldCkBox = new JCheckBox("Bold", font.isBold());
		textPanel.add(boldCkBox);
		italicCkBox = new JCheckBox("Italic", font.isItalic());
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
		
		textDirByCharCkBox = new JCheckBox("ByChar", true);
		textPropPanel.add(textDirByCharCkBox);
		
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
		
		
		charSizeByBlockCkBox = new JCheckBox("ByBlock", true);
		charPanel.add(charSizeByBlockCkBox);
		pack();
		
		JPanel actionPanel = new JPanel();
		controlPanel.add(actionPanel);

		JButton textNewButton = new JButton("New");
		textNewButton.setActionCommand("emc_text_new");
		textNewButton.addActionListener(sceneControler);
		actionPanel.add(textNewButton);
		
		JButton textDupButton = new JButton("Change");
		textDupButton.setActionCommand("emc_text_change");
		textDupButton.addActionListener(sceneControler);
		actionPanel.add(textDupButton);
		
		JButton textSelectButton = new JButton("Select");
		textSelectButton.setActionCommand("emc_text_select");
		textSelectButton.addActionListener(sceneControler);
		actionPanel.add(textSelectButton);
		

		positionAtNextRaButton = new JRadioButton("At Next");
		positionAtNextRaButton.setActionCommand("emc_pos_at_next");
		positionAtNextRaButton.setSelected(true);
		positionAtNextRaButton.addActionListener(sceneControler);

		positionAtCurrRaButton = new JRadioButton("At Current");
		positionAtCurrRaButton.setActionCommand("emc_pos_at_current");
		positionAtCurrRaButton.addActionListener(sceneControler);

		ButtonGroup positionAtGroup = new ButtonGroup();
		positionAtGroup.add(positionAtNextRaButton);
		positionAtGroup.add(positionAtCurrRaButton);
		
		actionPanel.add(positionAtNextRaButton);
		actionPanel.add(positionAtCurrRaButton);
		
		pack();
		setup = true;
		
	}
	
	


	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		EMBlock cb = sceneControler.getSelectedBlock();
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
			case "emc_text_new":
				sceneControler.addTextButton(bcmd, action);
				break;
				
			case "emc_text_change":
				sceneControler.changeTextButton(bcmd, action);
				break;
				
			case "emc_text_select":
				sceneControler.selectTextButton(bcmd, action);
				break;
			
				default:
					return false;		// No action here
		}
		return bcmd.doCmd();

	}

	/**
	 * Access methods
	 */
	
	/**
	 * collect font info, creating a Font object
	 */
	public Font getFont() {
		Font font = ColoredText.getFont();
		String fontName = (String) fontCBox.getSelectedItem();
		boolean bold = font.isBold();
		try {
			bold = getBold();
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean italic = font.isItalic();
		try {
			italic = getItalic();
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int size = font.getSize();
		try {
			size = getFontSize();
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int style = Font.PLAIN;
		if (bold)
			style += Font.BOLD;
		if (italic)
			style += Font.ITALIC;
		
		font = new Font(fontName, style, size);

		return font;
	}
	
	public String getText() throws EMBlockError {
		String val = "";
		if (textStringTxFld != null) {
			String text = textStringTxFld.getText();
			val = text;
		}
		return val;
	}
	
	public String getFontName() throws EMBlockError {
		String val = "";
		if (fontCBox != null) {
			String text = (String) fontCBox.getSelectedItem();
			val = text;
		}
		return val;
	}
	
	public int getFontSize() throws EMBlockError {
		int val = ColoredText.getFont().getSize();
		if (textFontSizeTxFld != null) {
			String text = textFontSizeTxFld.getText();
			if (!text.equals(""))
				val = Integer.valueOf(text);
		}
		return val;
	}
	
	public boolean getBold() throws EMBlockError {
		boolean val = false;
		if (boldCkBox != null) {
			val = boldCkBox.isSelected();
		}
		return val;
	}
	
	public boolean getItalic() throws EMBlockError {
		boolean val = false;
		if (italicCkBox != null) {
			val = italicCkBox.isSelected();
		}
		return val;
	}
	
	public boolean getUnderline() throws EMBlockError {
		boolean val = false;
		if (underlineCkBox != null) {
			val = underlineCkBox.isSelected();
		}
		return val;
	}
	
	public float getTextXDir() throws EMBlockError {
		float val = 0;
		if (textXDirTxFld != null) {
			String text = textXDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getTextYDir() throws EMBlockError {
		float val = 0;
		if (textYDirTxFld != null) {
			String text = textYDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getTextZDir() throws EMBlockError {
		float val = 0;
		if (textZDirTxFld != null) {
			String text = textZDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public boolean getTextDirByChar() throws EMBlockError {
		boolean val = false;
		if (textDirByCharCkBox != null) {
			val = textDirByCharCkBox.isSelected();
		}
		return val;
	}
	
	public float getCharXSize() throws EMBlockError {
		float val = 0;
		if (charXSizeTxFld != null) {
			String text = charXSizeTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getCharYSize() throws EMBlockError {
		float val = 0;
		if (charYSizeTxFld != null) {
			String text = charYSizeTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getCharZSize() throws EMBlockError {
		float val = 0;
		if (charZSizeTxFld != null) {
			String text = charZSizeTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public boolean getCharSizeByBlock() throws EMBlockError {
		boolean val = false;
		if (charSizeByBlockCkBox != null) {
			val = charSizeByBlockCkBox.isSelected();
		}
		return val;
	}
	
	public boolean getCharDirWithText() throws EMBlockError {
		boolean val = false;
		if (charDirWithTextCkBox != null) {
			val = charDirWithTextCkBox.isSelected();
		}
		return val;
	}
	
	public float getCharXDir() throws EMBlockError {
		float val = 0;
		if (charXDirTxFld != null) {
			String text = charXDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getCharYDir() throws EMBlockError {
		float val = 0;
		if (charYDirTxFld != null) {
			String text = charYDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getCharZDir() throws EMBlockError {
		float val = 0;
		if (charZDirTxFld != null) {
			String text = charZDirTxFld.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public boolean getCharDirByBlockCkBox() throws EMBlockError {
		boolean val = false;
		if (charDirByBlockCkBox != null) {
			val = charDirByBlockCkBox.isSelected();
		}
		return val;
	}

	public boolean getPosAtNext() throws EMBlockError {
		if (positionAtNextRaButton == null)
			return false;
		if (positionAtNextRaButton.isSelected())
			return true;
		return false;
	}
	
	/**
	 * Get font names
	 * 
	 */
	public String[] getFontNames() {
		String fonts[] = 
		      GraphicsEnvironment.getLocalGraphicsEnvironment()
		      .getAvailableFontFamilyNames();
		return fonts;
	}
}
