package ExtendedModeler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GLAutoDrawable;
import smTrace.SmTrace;


public class ControlOfColor extends ControlOf implements ChangeListener {
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
	JColorChooser colorChooser;	// Dynamic color chooser if not null
	
	ControlOfColor(SceneControler SceneControler, String name) {
		super(SceneControler, name);
	}

	/**
	 * reset to default setting
	 */
	public void reset() {
		nextColor = 0;		// Reset generated colors
		setup = false;
		setup();
	}
	
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 * @throws EMBlockError 
	 */
	public void setup() {
		SmTrace.lg("ControlOfColor.setup()", "setup");
		if (setup)
			return;					// Already setup
		
		SmTrace.lg("ControlOfColor.setup()-2c", "setup");
		setTitle("Color - Adjust/Report ");
		sceneControler.selectPrint("ControlOfColor.setup".concat("select"), "select");
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
		md_move_color_button.setActionCommand("emc_md_color_move");
		md_move_color_button.setSelected(true);
		md_move_color_button.addActionListener(sceneControler);

		JRadioButton md_dup_color_button = new JRadioButton("Duplicate");
		md_dup_color_button.setMnemonic(KeyEvent.VK_D);
		md_dup_color_button.setActionCommand("emc_md_color_duplicate");
		md_dup_color_button.addActionListener(sceneControler);

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
		setup = true;
	}

	/**
	 * Digital setting
	 * @param panel
	 * @throws EMBlockError 
	 */
	public void addDigital(JPanel panel) {
		SmTrace.lg("addDigital");
		JPanel colorTo_panel = new JPanel();
		panel.add(colorTo_panel);
		pack();
		
		EMBlock cb = sceneControler.getSelectedBlock();
		if (cb == null) {
			AlignedBox3D box = new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1));
			Color color = new Color(1f, 0f, 1f);
			SmTrace.lg(String.format("addDigital: red=%d", color.getRed()));
			cb = EMBlock.newBlock("box", box, color);
		}
		colorRedField = new JTextField(String.format("%.2f", cb.getRed()));
		colorRedField.setActionCommand("emc_ENTER");
		colorRedField.addActionListener(sceneControler);
		colorGreenField = new JTextField(String.format("%.2f", cb.getGreen()));
		colorGreenField.setActionCommand("emc_ENTER");
		colorGreenField.addActionListener(sceneControler);
		colorBlueField = new JTextField(String.format("%.2f", cb.getBlue()));
		colorBlueField.setActionCommand("emc_ENTER");
		colorBlueField.addActionListener(sceneControler);
		colorAlphaField = new JTextField(String.format("%.2f", cb.getAlpha()));
		colorAlphaField.setActionCommand("emc_ENTER");
		colorAlphaField.addActionListener(sceneControler);
		JButton colorToButton = new JButton("ColorTo");
		colorToButton.setActionCommand("emc_colorToButton");
		colorToButton.addActionListener(sceneControler);
		JButton colorToPreviousButton = new JButton("ColorToPrevious");
		colorToPreviousButton.setActionCommand("emc_colorToPreviousButton");
		colorToPreviousButton.addActionListener(sceneControler);
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
		adjcolorRedField.setActionCommand("emc_adjcolorENTER");
		adjcolorRedField.addActionListener(sceneControler);
		adjcolorGreenField = new JTextField(String.format("%.2f", adjamt));
		adjcolorGreenField.setActionCommand("emc_adjcolorENTER");
		adjcolorGreenField.addActionListener(sceneControler);
		adjcolorBlueField = new JTextField(String.format("%.2f", adjamt));
		adjcolorBlueField.setActionCommand("emc_adjcolorENTER");
		adjcolorBlueField.addActionListener(sceneControler);
		adjcolorAlphaField = new JTextField(String.format("%.2f", adjamt));
		adjcolorAlphaField.setActionCommand("emc_adjcolorENTER");
		adjcolorAlphaField.addActionListener(sceneControler);
		JButton adjcolorUpButton = new JButton("Up By");
		adjcolorUpButton.setActionCommand("emc_adjcolorUpButton");
		adjcolorUpButton.addActionListener(sceneControler);
		JButton adjcolorDownButton = new JButton("Down By");
		adjcolorDownButton.setActionCommand("emc_adjcolorDownButton");
		adjcolorDownButton.addActionListener(sceneControler);
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
		SmTrace.lg("addMap - adding color map");
		JPanel colorMapPanel = new JPanel();
		colorMapPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(colorMapPanel);
		pack();
		JButton colorMapButton = new JButton("ColorMap");
		colorMapButton.setActionCommand("emc_colorMapButton");
		colorMapPanel.add(colorMapButton);
		colorMapButton.addActionListener(sceneControler);
		pack();
		colorMapPanel.setVisible(true);
		
		
		// Do common stuff
		addComponentListener(new ComponentListener() {
		public void componentMoved(ComponentEvent e) {
		updateLocation(e);
		}
		
		@Override
		public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
		}
		
		@Override
		public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
		}
		
		@Override
		public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
		}
		} );
	}

	
	/**
	 * Update location
	 * Generally called after move
	 */
	public void updateMapLocation(ComponentEvent e) {
		Point pt = e.getComponent().getLocation();
		recordMapLocation(pt.x, pt.y);
		SmTrace.lg(String.format(String.format("updateMapLocation(%s: %d, %d)"
				,  name, pt.x, pt.y)));
		
	}

	
	/**
	 * Record current location
	 */
	public void recordMapLocation(int x, int y) {
		String pos_key_x = getMapPosKeyX();
		SmTrace.setProperty(pos_key_x, String.valueOf(x));
		String pos_key_y = getMapPosKeyY();
		SmTrace.setProperty(pos_key_y, String.valueOf(y));
	}

	
	/** our control locations
	 * 
	 */
	private String getMapPosKeyX() {
		String key = "control." + name + "map.pos.x";
		return key;
	}
	private String getMapPosKeyY() {
		String key = "control." + name + "map.pos.y";
		return key;
	}
	
	
	/**
	 * Adjust by increments in direction 1- positive, -1 negative
	 * 
	 * @param direction
	 * @throws EMBlockError 
	 */
	private void adjustColor(EMBCommand bcmd, int direction) throws EMBlockError {
		if (sceneControler.getSelected() == null)
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
		EMBlock cb = sceneControler.getSelectedBlock();
		if (cb == null)
			return;
		

		if (!color_move_duplicate) {
			bcmd.addBlock(cb);			// Duplicate --> add original to new blocks ( as well as the newly positioned block)
		}
		bcmd.addPrevBlock(cb);				//Save copy for undo/redo
		EMBlock cb1 = cb.duplicate();		// New or modified

		cb1.colorAdj(adjcolor_redval, adjcolor_greenval, adjcolor_blueval, adjcolor_alphaval);
		SmTrace.lg(
				String.format("adjust to red=%.2f green=%.2f blue=%.2f  alpha=%.2f",
						cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha()), "color");
		bcmd.addBlock(cb1);					// Add New / modified block
		bcmd.setSelect(new BlockSelect(cb1.iD()));
	}

	/**
	 * Adjust control based on selection
	 */
	public void adjustControls() {
		EMBlock cb = sceneControler.getSelectedBlock();
		if (cb == null)
			return;

		if (setup) {
			colorRedField.setText(String.format("%.2g", cb.getRed()));
			colorGreenField.setText(String.format("%.2g",cb.getGreen()));
			colorBlueField.setText(String.format("%.2g", cb.getBlue()));
			
			adjcolorRedField.setText(String.format("%.2g", cb.getRed()/3));
			adjcolorGreenField.setText(String.format("%.2g",cb.getGreen()/3));
			adjcolorBlueField.setText(String.format("%.2g", cb.getBlue()/3));

			repaint();
			
		}
	}
		
		
		
	private void colorToColor(EMBCommand bcmd) throws EMBlockError {
		if (sceneControler.getSelected() == null)
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

		EMBlock cb = sceneControler.getSelectedBlock();
		if (cb == null)
			return;				// None selected
		
		Color new_color = new Color(redval, greenval, blueval);
		if (!color_move_duplicate) {
			cb = cb.duplicate();
			sceneControler.addBlock(bcmd, cb);
		}
		cb.setColor(new_color);
		SmTrace.lg(String.format("move to x=%.2f y=%.2f z=%.2f", redval, greenval, blueval));
		sceneControler.repaint();
	}

	
	private void colorToPreviousColor(EMBCommand bcmd) throws EMBlockError {
		EMBlock cb_sel = sceneControler.getSelectedBlock();
		if (cb_sel == null)
			return;

		EMBlock cb_prev = sceneControler.getPrevSelectedBlock();
		
		if (cb_prev == null)
			return;
		
		Color new_color = cb_prev.getColor();
		if (!color_move_duplicate) {
			cb_sel = cb_sel.duplicate();
			sceneControler.addBlock(bcmd, cb_sel);
		}
		cb_sel.setColor(new_color);
		SmTrace.lg(String.format("move to x=%.2f y=%.2f z=%.2f",
								cb_sel.getRed(), cb_sel.getGreen(), cb_sel.getBlue()));
		sceneControler.repaint();
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
				
			case "emc_colorToButton":
				colorToColor(bcmd);
				break;
				
			case "emc_colorToPreviousButton":
				colorToPreviousColor(bcmd);
				break;
					
			case "emc_colorToColorENTER":
				colorToColor(bcmd);
				break;
			
			case "emc_adjcolorUpButton":
				adjustColor(bcmd, 1);
				break;
				
			case "emc_adjcolorENTER":
				adjustColor(bcmd, 1);
				break;
				
			case "emc_adjcolorDownButton":
				adjustColor(bcmd, -1);
				break;
				
			case "emc_colorMapButton":
				colorMapSelect(bcmd);
				break;
				
				default:
					return false;
		}
		return bcmd.doCmd();

	}

	
	/**
	 * Select color from color map
	 * @throws EMBlockError 
	 */
	public void colorMapSelect(EMBCommand bcmd) throws EMBlockError {
		EMBlock[] cbs = sceneControler.getSelectedBlocks();
		
        colorChooser = new JColorChooser();
        if (cbs.length > 0)
        	colorChooser.setColor(cbs[0].getColor());
        setMapLocation(400, 400);
        boolean model = false;		// false - modeless
        JDialog dialog = JColorChooser.createDialog(null, "Color Chooser",
                 model, colorChooser, null, null);
        dialog.setVisible(true);
        
        colorChooser.getSelectionModel().addChangeListener(this);
        colorChooser.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("resized");
				// TODO Auto-generated method stub
				
			}
        	@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("moved");
				updateMapLocation(e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				System.out.println("shown");
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				System.out.println("hidden");
				// TODO Auto-generated method stub
				
			}}
        );

	}

	/**
	 * Get map location
	 */
	public Point getMapLocation() {
		
		int x = -1;			// Not set
		int y = -1;
		String mapx = SmTrace.getProperty(getMapPosKeyX());
		String mapy = SmTrace.getProperty(getMapPosKeyY());
		if (!mapx.equals("")) {
			x = Integer.valueOf(mapx);
			y = Integer.valueOf(mapy);
		}
		return new Point(x, y);
	}

	/**
	 * set map location
	 */
	public void setMapLocation(int x0, int y0) {
		
		int x = x0;
		int y = y0;
		String mapx = SmTrace.getProperty(getMapPosKeyX());
		String mapy = SmTrace.getProperty(getMapPosKeyY());
		if (!mapx.equals("")) {
			x = Integer.valueOf(mapx);
			y = Integer.valueOf(mapy);
		}
        colorChooser.setLocation(x, y);
	}
	
	/**
	 * Update display
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		///mapPaint(colorMapCanvas);
	}

	/**
	 * Access methods
	 */
	
	public float getRed() throws EMBlockError {
		float val = 0;
		if (colorRedField != null) {
			String text = colorRedField.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getGreen() throws EMBlockError {
		float val = 0;
		if (colorGreenField != null) {
			String text = colorGreenField.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getBlue() throws EMBlockError {
		float val = 0;
		if (colorBlueField != null) {
			String text = colorBlueField.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public float getAlpha() throws EMBlockError {
		float val = 0;
		if (colorAlphaField != null) {
			String text = colorAlphaField.getText();
			val = Float.valueOf(text);
		}
		return val;
	}
	
	public Color getColor(String string) throws EMBlockError {
		Color val = null;
		
		val = new Color(getRed(), getGreen(), getBlue(), getAlpha());
		return val;
	}

	public void stateChanged(ChangeEvent e) {
		System.out.println(String.format("ControlOfColor stateChange %s",
				e));
        
		if (colorChooser == null)
			return;
		
        Color c;
        c = colorChooser.getColor();
        if (c == null)
        	return;
       
       EMBlock[] cbs = sceneControler.getSelectedBlocks();
       SmTrace.lg(String.format("Chose color: %s", c));
        if (cbs.length == 0) {
            SmTrace.lg(String.format("Nothing selected to color"));
        	return;
        }
        int ccx = colorChooser.getX();
        int ccy = colorChooser.getY();
        SmTrace.lg(String.format("Setting ColorChooser to x=%d,  y=%d",  ccx, ccy));
        recordMapLocation(ccx, ccy);	// Because componentMoved doesn't work for me
        BlockSelect new_select = new BlockSelect();
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd("ecm_color_choice");
		} catch (Exception e2) {
			e2.printStackTrace();
			return;
		}
        for (int i = 0; i < cbs.length; i++) {
			if (!color_move_duplicate) {
				bcmd.addBlock(cbs[i]);			// Duplicate --> add original to new blocks ( as well as the newly positioned block)
			}
			bcmd.addPrevBlock(cbs[i]);			//Save copy for undo/redo
			EMBlock cb1 = null;
			try {
				cb1 = cbs[i].duplicate();
			} catch (EMBlockError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	// New or modified
			cb1.setColor(c);
			bcmd.addBlock(cb1);					// Add New / modified block
			new_select.addIndex(cb1.iD());
        }
		bcmd.setSelect(new_select);
		bcmd.doCmd();
	}

	
}

