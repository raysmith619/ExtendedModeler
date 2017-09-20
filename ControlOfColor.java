import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GLAutoDrawable;
import smTrace.SmTrace;


public class ControlOfColor extends ControlOf {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean color_move_duplicate = true; // Move/Duplicate choice
	JTextField colorRedField;
	JTextField colorGreenField;
	JTextField colorBlueField;
	JTextField colorAlphaField;

	JTextField adjcolorRedField;
	JTextField adjcolorGreenField;
	JTextField adjcolorBlueField;
	JTextField adjcolorAlphaField;
								// Color map
	Container toolPanel;

	int width = 200;  			// width of map
	int height = 0; 			// height of map
	///GLCanvas canvas;			// Map canvas
	JPanel colorMapCanvas;		// Color map canvas
	ColorTriangle  triangle;	// triangle object
	
	ControlOfColor(SceneViewer scene, String name) {
		super(scene, name);
	}
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 */
	public void setup() {
		if(SmTrace.tr("setup"))
			System.out.println("ControlOfColor.setup()");
		if (controlActive)
			return;					// Already present
		
		if(SmTrace.tr("setup"))
			System.out.println("ControlOfColor.setup()-2c");
		setTitle("Adjust/Report Color");
		if (SmTrace.tr("select")) {
			scene.selectPrint("ControlOfColor.setup");
		}
		JPanel colorPanel = new JPanel(new GridLayout(0, 1));
		add(colorPanel);
		pack();
		
		// Move / Duplicate choice
		JPanel mdChoicePanel = new JPanel(new GridLayout(0, 1));
		mdChoicePanel.setBorder(BorderFactory.createLineBorder(Color.green));
		colorPanel.add(mdChoicePanel);
		pack();

		JRadioButton md_move_color_button = new JRadioButton("Move");
		md_move_color_button.setMnemonic(KeyEvent.VK_M);
		md_move_color_button.setActionCommand("md_color_move");
		md_move_color_button.setSelected(true);
		md_move_color_button.addActionListener(scene);

		JRadioButton md_dup_color_button = new JRadioButton("Duplicate");
		md_dup_color_button.setMnemonic(KeyEvent.VK_D);
		md_dup_color_button.setActionCommand("md_color_duplicate");
		md_dup_color_button.addActionListener(scene);

		JPanel move_dup_color_panel = new JPanel();
		ButtonGroup move_dup_color_group = new ButtonGroup();
		move_dup_color_group.add(md_move_color_button);
		move_dup_color_group.add(md_dup_color_button);
		
		move_dup_color_panel.add(md_move_color_button);
		move_dup_color_panel.add(md_dup_color_button);
		mdChoicePanel.add(move_dup_color_panel);
		pack();
		addDigital(mdChoicePanel);
		addMap(mdChoicePanel);
		controlActive = true;
	}

	/**
	 * Digital setting
	 * @param panel
	 */
	public void addDigital(JPanel panel) {
		System.out.println("addDigital");
		JPanel colorTo_panel = new JPanel();
		panel.add(colorTo_panel);
		pack();
		
		OurBlock cb = scene.getSelectedBlock();
		if (cb == null) {
			AlignedBox3D box = new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1));
			Color color = new Color(1,1,1,1);
			System.out.println(String.format("addDigital: red=%d", color.getRed()));
			cb = OurBlock.newBlock("box", box, color);
		}
		colorRedField = new JTextField(String.format("%.2f", cb.getRed()));
		colorRedField.setActionCommand("ENTER");
		colorRedField.addActionListener(scene);
		colorGreenField = new JTextField(String.format("%.2f", cb.getGreen()));
		colorGreenField.setActionCommand("ENTER");
		colorGreenField.addActionListener(scene);
		colorBlueField = new JTextField(String.format("%.2f", cb.getBlue()));
		colorBlueField.setActionCommand("ENTER");
		colorBlueField.addActionListener(scene);
		colorAlphaField = new JTextField(String.format("%.2f", cb.getAlpha()));
		colorAlphaField.setActionCommand("ENTER");
		colorAlphaField.addActionListener(scene);
		JButton colorToButton = new JButton("ColorTo");
		colorToButton.setActionCommand("colorToButton");
		colorToButton.addActionListener(scene);
		JButton colorToPreviousButton = new JButton("ColorToPrevious");
		colorToPreviousButton.setActionCommand("colorToPreviousButton");
		colorToPreviousButton.addActionListener(scene);
		/// panel.add(combo);
		colorToButton.setHorizontalAlignment(SwingConstants.CENTER);
		colorTo_panel.add(new JLabel("red:"));
		colorTo_panel.add(colorRedField);
		colorTo_panel.add(new JLabel("green:"));
		colorTo_panel.add(colorGreenField);
		colorTo_panel.add(new JLabel("blue:"));
		colorTo_panel.add(colorBlueField);
		colorTo_panel.add(new JLabel("alpha:"));
		colorTo_panel.add(colorAlphaField);
		colorTo_panel.add(Box.createVerticalStrut(15)); // a spacer
		colorTo_panel.add(colorToButton);
		colorTo_panel.add(colorToPreviousButton);
		pack();
		
		// Adjust by
		JPanel adjcolor_panel = new JPanel();
		panel.add(adjcolor_panel);
		float adjamt = .1f; // Default adjustment
		adjcolor_panel.add(Box.createVerticalStrut(2)); // a spacer
		adjcolorRedField = new JTextField(String.format("%.2f", adjamt));
		adjcolorRedField.setActionCommand("adjcolorENTER");
		adjcolorRedField.addActionListener(scene);
		adjcolorGreenField = new JTextField(String.format("%.2f", adjamt));
		adjcolorGreenField.setActionCommand("adjcolorENTER");
		adjcolorGreenField.addActionListener(scene);
		adjcolorBlueField = new JTextField(String.format("%.2f", adjamt));
		adjcolorBlueField.setActionCommand("adjcolorENTER");
		adjcolorBlueField.addActionListener(scene);
		adjcolorAlphaField = new JTextField(String.format("%.2f", adjamt));
		adjcolorAlphaField.setActionCommand("adjcolorENTER");
		adjcolorAlphaField.addActionListener(scene);
		JButton adjcolorUpButton = new JButton("Up By");
		adjcolorUpButton.setActionCommand("adjcolorUpButton");
		adjcolorUpButton.addActionListener(scene);
		JButton adjcolorDownButton = new JButton("Down By");
		adjcolorDownButton.setActionCommand("adjcolorDownButton");
		adjcolorDownButton.addActionListener(scene);
		adjcolor_panel.add(Box.createVerticalStrut(2)); // a spacer
		adjcolor_panel.add(new JLabel("red-adj:"));
		adjcolor_panel.add(adjcolorRedField);
		adjcolor_panel.add(new JLabel("green-adj:"));
		adjcolor_panel.add(adjcolorGreenField);
		adjcolor_panel.add(new JLabel("blue-adj:"));
		adjcolor_panel.add(adjcolorBlueField);
		adjcolor_panel.add(adjcolorUpButton);
		adjcolor_panel.add(adjcolorDownButton);
		adjcolor_panel.add(new JLabel("alpha-adj:"));
		adjcolor_panel.add(adjcolorAlphaField);
		adjcolor_panel.add(adjcolorUpButton);
		adjcolor_panel.add(adjcolorDownButton);
		pack();
	}
	
	
	/**
	 * Add Color map
	 * @param panel
	 */
	public void addMap(JPanel panel) {
		System.out.println("addMap - adding color map");
		JPanel colorMapPanel = new JPanel();
		colorMapPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(colorMapPanel);
		pack();
		JButton colorMapButton = new JButton("ColorMap");
		colorMapButton.setActionCommand("colorMapButton");
		colorMapPanel.add(colorMapButton);
		colorMapButton.addActionListener(scene);
		pack();
		colorMapPanel.setVisible(true);
	}
	
	/**
	 * Adjust by increments in direction 1- positive, -1 negative
	 * 
	 * @param direction
	 */
	private void adjustColor(BlockCommand bcmd, int direction) {
		if (scene.getSelected() == null)
			return;
		
		float adjcolor_redval = 0;
		float adjcolor_greenval = 0;
		float adjcolor_blueval = 0;
		float adjcolor_alphaval = 0;
		if (adjcolorRedField != null) {
			String text = adjcolorRedField.getText();
			adjcolor_redval = Float.valueOf(text);
		}
		if (adjcolorGreenField != null) {
			String text = adjcolorGreenField.getText();
			adjcolor_greenval = Float.valueOf(text);
		}
		if (adjcolorBlueField != null) {
			String text = adjcolorBlueField.getText();
			adjcolor_blueval = Float.valueOf(text);
		}
		if (adjcolorAlphaField != null) {
			String text = adjcolorAlphaField.getText();
			adjcolor_alphaval = Float.valueOf(text);
		}
		if (direction < 0) {
			adjcolor_redval *= -1;
			adjcolor_greenval *= -1;
			adjcolor_blueval *= -1;
			adjcolor_alphaval *= -1;
		}
		OurBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;
		

		if (!color_move_duplicate) {
			bcmd.addBlock(cb);			// Duplicate --> add original to new blocks ( as well as the newly positioned block)
		}
		bcmd.addPrevBlock(cb);				//Save copy for undo/redo
		OurBlock cb1 = cb.duplicate();		// New or modified

		cb1.colorAdj(adjcolor_redval, adjcolor_greenval, adjcolor_blueval, adjcolor_alphaval);
		System.out.println(
				String.format("adjust to red=%.2f green=%.2f blue=%.2f  alpha=%.2f",
						cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha()));
		bcmd.addBlock(cb1);					// Add New / modified block
		bcmd.setSelect(new BlockSelect(cb1.iD()));
	}

	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		OurBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;

		if (controlActive) {
			colorRedField.setText(String.format("%.2g", cb.getRed()));
			colorGreenField.setText(String.format("%.2g",cb.getGreen()));
			colorBlueField.setText(String.format("%.2g", cb.getBlue()));
			
			adjcolorRedField.setText(String.format("%.2g", cb.getRed()/10));
			adjcolorGreenField.setText(String.format("%.2g",cb.getGreen()/10));
			adjcolorBlueField.setText(String.format("%.2g", cb.getBlue()/10));

			repaint();
			
		}
	}
		
		
		
	private void colorToColor(BlockCommand bcmd) {
		if (scene.getSelected() == null)
			return;
		
		float redval = 0;
		float greenval = 0;
		float blueval = 0;
		if (colorRedField != null) {
			String text = colorRedField.getText();
			redval = Float.valueOf(text);
		}
		if (colorGreenField != null) {
			String text = colorGreenField.getText();
			greenval = Float.valueOf(text);
		}
		if (colorBlueField != null) {
			String text = colorBlueField.getText();
			blueval = Float.valueOf(text);
		}

		OurBlock cb = scene.getSelectedBlock();
		Color new_color = new Color(redval, greenval, blueval);
		if (!color_move_duplicate) {
			cb = cb.duplicate();
			scene.addBlock(bcmd, cb);
		}
		cb.setColor(new_color);
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f", redval, greenval, blueval));
		scene.repaint();
	}

	
	private void colorToPreviousColor(BlockCommand bcmd) {
		OurBlock cb_sel = scene.getSelectedBlock();
		if (cb_sel == null)
			return;

		OurBlock cb_prev = scene.getPrevSelectedBlock();
		
		if (cb_prev == null)
			return;
		
		Color new_color = cb_prev.getColor();
		if (!color_move_duplicate) {
			cb_sel = cb_sel.duplicate();
			scene.addBlock(bcmd, cb_sel);
		}
		cb_sel.setColor(new_color);
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f",
								cb_sel.getRed(), cb_sel.getGreen(), cb_sel.getBlue()));
		scene.repaint();
	}



	/**
	 * Check for and act on action
	 */
	public boolean ckDoAction(String action) {
		BlockCommand bcmd;
		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		switch (action) {
				
			case "colorToButton":
				colorToColor(bcmd);
				break;
				
			case "colorToPreviousButton":
				colorToPreviousColor(bcmd);
				break;
					
			case "colorToColorENTER":
				colorToColor(bcmd);
				break;
			
			case "adjcolorUpButton":
				adjustColor(bcmd, 1);
				break;
				
			case "adjcolorENTER":
				adjustColor(bcmd, 1);
				break;
				
			case "adjcolorDownButton":
				adjustColor(bcmd, -1);
				break;
				
			case "colorMapButton":
				colorMapSelect(bcmd);
				break;
				
				default:
					return false;
		}
		return bcmd.doCmd();

	}

	
	/**
	 * Select color from color map
	 */
	public void colorMapSelect(BlockCommand bcmd) {
		OurBlock cb = scene.getSelectedBlock();
		if (cb == null)
			return;			// None selected
		
        final JColorChooser chooser = new JColorChooser();
        chooser.setColor(cb.getColor());
        chooser.setLocation(400,400);
        JDialog dialog = JColorChooser.createDialog(null, "Color Chooser",
                 true, chooser, null, null);
        dialog.setVisible(true);
        Color c;
        c = chooser.getColor();
        if (c == null)
        	return;
        
       System.out.println(String.format("Chose color: %s", c));
       
		if (!color_move_duplicate) {
			bcmd.addBlock(cb);			// Duplicate --> add original to new blocks ( as well as the newly positioned block)
		}
		bcmd.addPrevBlock(cb);				//Save copy for undo/redo
		OurBlock cb1 = cb.duplicate();		// New or modified
		cb1.setColor(c);
		bcmd.addBlock(cb1);					// Add New / modified block
		bcmd.setSelect(new BlockSelect(cb1.iD()));
	}

	/**
	 * Update display
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		///mapPaint(colorMapCanvas);
	}


}

