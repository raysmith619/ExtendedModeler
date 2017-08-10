import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferStrategy;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;


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
		System.out.println("ControlOfColor.setup()");
		if (controlActive)
			return;					// Already present
		
		System.out.println("ControlOfColor.setup()-2c");
		setTitle("Adjust/Report Color");
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
	}

	/**
	 * Digital setting
	 * @param panel
	 */
	public void addDigital(JPanel panel) {
		System.out.println("addDigital");
		JPanel moveto_panel = new JPanel();
		panel.add(moveto_panel);
		pack();
		
		OurBlock cb = scene.getSelected();
		if (cb == null) {
			cb = new OurBlock(new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1)), new Color(1,1,1,1));
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
		JButton moveToButton = new JButton("ColorTo");
		moveToButton.setActionCommand("colorToButton");
		moveToButton.addActionListener(scene);
		/// panel.add(combo);
		moveToButton.setHorizontalAlignment(SwingConstants.CENTER);
		moveto_panel.add(new JLabel("red:"));
		moveto_panel.add(colorRedField);
		moveto_panel.add(new JLabel("green:"));
		moveto_panel.add(colorGreenField);
		moveto_panel.add(new JLabel("blue:"));
		moveto_panel.add(colorBlueField);
		moveto_panel.add(new JLabel("alpha:"));
		moveto_panel.add(colorAlphaField);
		moveto_panel.add(Box.createVerticalStrut(15)); // a spacer
		moveto_panel.add(moveToButton);
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
	private void adjustColor(int direction) {
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
		OurBlock cb = scene.getSelected();
		if (!color_move_duplicate) {
			cb = cb.duplicate();
			scene.addBlock(cb);
		}
		cb.colorAdj(adjcolor_redval, adjcolor_greenval, adjcolor_blueval, adjcolor_alphaval);
		System.out.println(
				String.format("adjust to red=%.2f green=%.2f blue=%.2f  alpha=%.2f",
						cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha()));
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
			colorRedField.setText(String.format("%.2g", cb.getRed()));
			colorGreenField.setText(String.format("%.2g",cb.getGreen()));
			colorBlueField.setText(String.format("%.2g", cb.getBlue()));
			
			adjcolorRedField.setText(String.format("%.2g", cb.getRed()/10));
			adjcolorGreenField.setText(String.format("%.2g",cb.getGreen()/10));
			adjcolorBlueField.setText(String.format("%.2g", cb.getBlue()/10));

			repaint();
			
		}
	}
		
		
		
	private void moveToColor() {
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

		OurBlock cb = scene.getSelected();
		Color new_color = new Color(redval, greenval, blueval);
		if (!color_move_duplicate) {
			cb = cb.duplicate();
			scene.addBlock(cb);
		}
		cb.setColor(new_color);
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f", redval, greenval, blueval));
		scene.repaint();
	}



	/**
	 * Check for and act on action
	 */
	public boolean ckDoAction(String action) {
		switch (action) {
				
			case "moveToColorButton":
				moveToColor();
				break;
					
			case "moveToColorENTER":
				moveToColor();
				break;
			
			case "adjcolorUpButton":
				adjustColor(1);
				break;
				
			case "adjcolorENTER":
				adjustColor(1);
				break;
				
			case "adjcolorDownButton":
				adjustColor(-1);
				break;

			case "deleteBlockButton":
			case "duplicateBlockButton":
			case "addBoxButton":
			case "addBallButton":
			case "addConeButton":
			case "addCylinderButton":
				scene.addBlockButton(action);
				break;

				
			case "colorMapButton":
				colorMapSelect();
				break;
				
				default:
					return false;
		}
		return true;

	}

	
	/**
	 * Select color from color map
	 */
	public void colorMapSelect() {
        Color c = JColorChooser.showDialog(
                this, "Choose a color", Color.CYAN);
        if (c == null) 
        	return;			// None chosen

        System.out.println(String.format("Chose color: %s", c));
		OurBlock cb = scene.getSelected();
		if (cb == null)
			return;			// None selected
		
		cb.setColor(c);
		scene.repaint();
	}

	/**
	 * Update display
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		///mapPaint(colorMapCanvas);
	}


}

