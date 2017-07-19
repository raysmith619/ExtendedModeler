import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

class SceneViewer extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener, ActionListener {
	/**
	 * Position action types
	 *
	 */
	public enum PositionActionType { // positioning
		NONE, // none added
		POSITION_MOVE, POSITION_OK, POSITION_CANCEL,
	}
	JFrame frame;					// Access to main frame
	SmTrace smTrace; 				// Trace support
	GLAutoDrawable drawable; 		// Save base
	GLU glu; // Needed for scene2WorldCoord
	GLUT glut;
	// Updated each display() call
	int viewport[] = new int[4];
	double mvmatrix[] = new double[16];
	double projmatrix[] = new double[16];
	private PositionActionType positionAction = PositionActionType.NONE;
	public Scene scene = new Scene();
	public int indexOfSelectedBlock = -1; // -1 for none
	private Point3D selectedPoint = new Point3D();
	private Vector3D normalAtSelectedPoint = new Vector3D();
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	Camera3D camera = new Camera3D();
	ExtendedModeler.AutoAddType autoAdd = ExtendedModeler.AutoAddType.NONE; // Add
																			// item
																			// source
																			// menu

	RadialMenuWidget radialMenu = new RadialMenuWidget();
	private static final int COMMAND_CREATE_BOX = 10;
	private static final int COMMAND_CREATE_BALL = 11;
	private static final int COMMAND_CREATE_CYLINDAR = 12;
	private static final int COMMAND_CREATE_CONE = 13;
	private static final int COMMAND_DUPLICATE_BLOCK = 14;
	private static final int COMMAND_COLOR_RED = 1;
	private static final int COMMAND_COLOR_YELLOW = 2;
	private static final int COMMAND_COLOR_GREEN = 3;
	private static final int COMMAND_COLOR_BLUE = 4;
	private static final int COMMAND_DELETE = 5;

	public boolean displayColorControl = true;
	public boolean displayPositionControl = true;
	public boolean displayWorldAxes = false;
	public boolean displayCameraTarget = false;
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;

	JDialog posDialog; // Positioning dialog box
						// text fields, if set
	boolean pos_size_position = true; // position / size choice
	boolean pos_move_duplicate = true; // Move / Duplicate choice
	JTextField posXfield;
	JTextField posYfield;
	JTextField posZfield;

	JTextField adjposXfield;
	JTextField adjposYfield;
	JTextField adjposZfield;

	JDialog colorDialog; // Coloring dialog box
	// text fields, if set
	boolean color_move_duplicate = true; // Move/Duplicate choice
	JTextField colorRedField;
	JTextField colorGreenField;
	JTextField colorBlueField;
	JTextField colorAlphaField;

	JTextField adjcolorRedField;
	JTextField adjcolorGreenField;
	JTextField adjcolorBlueField;
	JTextField adjcolorAlphaField;

	public SceneViewer(GLCapabilities caps,
			JFrame frame,
			SmTrace trace) {

		super(caps);
		this.frame = frame;
		this.smTrace = trace; // Passed in
		addGLEventListener(this);

		addMouseListener(this);
		addMouseMotionListener(this);

		radialMenu.setItemLabelAndID(RadialMenuWidget.CENTRAL_ITEM, "", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(1, "Duplicate Block", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(2, "Set Color to Red", COMMAND_COLOR_RED);
		radialMenu.setItemLabelAndID(3, "Set Color to Yellow", COMMAND_COLOR_YELLOW);
		radialMenu.setItemLabelAndID(4, "Set Color to Green", COMMAND_COLOR_GREEN);
		radialMenu.setItemLabelAndID(5, "Set Color to Blue", COMMAND_COLOR_BLUE);
		radialMenu.setItemLabelAndID(7, "Delete Box", COMMAND_DELETE);

		camera.setSceneRadius((float) Math.max(5 * OurBlock.DEFAULT_SIZE,
				scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();

	}

	public Dimension getPreferredSize() {
		return new Dimension(512, 512);
	}

	/**
	 * Get currently selected
	 */
	public OurBlock getSelected() {
		if (indexOfSelectedBlock < 0)
			return null;
		if (scene.ourBlocks.size() == 0)
			return null;
		return scene.ourBlocks.elementAt(indexOfSelectedBlock);
	}
	
	
	/**
	 * enable / disable mouse add block
	 * 
	 * @param source
	 *            - source of add if set
	 */
	public void setAutoAdd(ExtendedModeler.AutoAddType type) {
		autoAdd = type;
	}

	/**
	 * Select given block
	 * @return index of selected block
	 * @param bindex - index of block to select, -1 - most recent block(s)
	 * @param keep - keep previous block(s) seleced 
	 */
	public int select(int bindex, boolean keep) {
		if (bindex < 0)
			bindex = indexOfSelectedBlock;
		if (bindex < 0)
			bindex = scene.ourBlocks.size()-1;	// Last added
		if (!keep)
			unSelect(-1);
		scene.setSelectionStateOfBox(bindex, true);
		indexOfSelectedBlock = bindex;
		indexOfHilitedBox = indexOfSelectedBlock;
		adjustControls();
		return bindex;
	}

	/**
	 * Select new block, unselecting current
	 * @param bindex
	 * @return selected block's index
	 */
	public int select(int bindex) {
		return select(bindex, false);
	}

	/**
	 * select latest block
	 */
	public int select() {
		return select(scene.ourBlocks.size()-1);
	}

	
	/**
	 * Unselect selection
	 * @param bindex - index to unselect, -1 => all
	 */
	public void unSelect(int bindex) {
		if (scene.ourBlocks.size() < 1) {
			return;
		}
		if (bindex < 0) {
			bindex = indexOfSelectedBlock;
		}
		scene.setSelectionStateOfBox(bindex, false);
		indexOfSelectedBlock = -1;
		indexOfHilitedBox = indexOfSelectedBlock;
	}
	
	
	float clamp(float x, float min, float max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}

	/**
	 * Add another block to scene
	 */
	public void addBlock(OurBlock cb) {
		int icb = scene.addBlock(cb);
		select(icb);

	}

	public void addBlock(String blocktype, AlignedBox3D box, Color color) {
		OurBlock cb = OurBlock.getNewBlock(blocktype, box, color);
		addBlock(cb);
	}

	/**
	 * Add a duplicate block at the given point
	 */
	public void addDuplicateBlock(OurBlock cb, Point3D atpoint) {
		OurBlock cbnew = cb.duplicate();
		addBlock(cbnew);
	}

	public void createNewBlock(String blockType) {
		Vector3D halfDiagonalOfNewBox = new Vector3D(OurBlock.DEFAULT_SIZE * 0.5f, OurBlock.DEFAULT_SIZE * 0.5f,
				OurBlock.DEFAULT_SIZE * 0.5f);
		OurBlock cb = getSelected();
		if (cb != null) {
			Point3D centerOfNewBox = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint,
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint)))),
					Vector3D.mult(normalAtSelectedPoint, OurBlock.DEFAULT_SIZE * 0.5f));
			float alpha = cb.getAlpha();
			System.out.println("alpha=" + alpha);
			Color color = new Color(clamp(cb.getRed() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getGreen() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getBlue() + 0.5f * ((float) Math.random() - 0.5f), 0, 1), alpha);
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			addBlock(blockType, box, color);
		} else {
			Point3D centerOfNewBox = camera.target;
			Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(),
					OurBlock.DEFAULT_ALPHA);
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			addBlock(blockType, box, color);
			normalAtSelectedPoint = new Vector3D(1, 0, 0);
		}
		// update selection to be new box
		select();
	}

	public void createNewBlock(String blockType, Point3D at_point, Vector3D size) {
		Vector3D halfDiagonalOfNewBox = new Vector3D(OurBlock.DEFAULT_SIZE * 0.5f, OurBlock.DEFAULT_SIZE * 0.5f,
				OurBlock.DEFAULT_SIZE * 0.5f);
		if (indexOfSelectedBlock >= 0) {
			OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
			Point3D centerOfNewBox = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint,
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint)))),
					Vector3D.mult(normalAtSelectedPoint, OurBlock.DEFAULT_SIZE * 0.5f));
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			Color color = new Color(clamp(cb.getRed() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getGreen() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getBlue() + 0.5f * ((float) Math.random() - 0.5f), 0, 1), cb.getAlpha());

			addBlock(blockType, box, color);
		} else {
			Point3D centerOfNewBox = camera.target;
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(),
					OurBlock.DEFAULT_ALPHA);
			addBlock(blockType, box, color);
			normalAtSelectedPoint = new Vector3D(1, 0, 0);
		}
		// update selection to be new box
		select();
	}

	/**
	 * Duplicate selected block - offset a bit
	 */
	public void duplicateBlock() {
		String blockType = "box"; // Default
		if (indexOfSelectedBlock >= 0) {
			OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
			blockType = cb.blockType();
		}
		createNewBlock(blockType);
	}

	/**
	 * Duplicate block
	 * 
	 * @param cb
	 *            - original bloce
	 * @param atpoint
	 *            - base point of new block
	 * @param size
	 *            - bounding dimensions default: same as original
	 * @param color
	 *            - color of new block default: same as original
	 */
	public void duplicateBlock(OurBlock cb, Point3D atpoint, Vector3D size, Color color) {
		String blockType = cb.blockType();
		createNewBlock(blockType);
	}

	/**
	 * Adjust/Report color
	 */
	
	/**
	 * Dispose of dialog
	 */
	public void colorControlDispose() {
		if (colorDialog != null) {
			colorDialog.dispose();
			colorDialog = null;
		}
	}
	
	
	public void colorControlSetup() {
		if (colorDialog != null)
			return;						// Already setup
		
		// JPanel panel = new JPanel(new GridLayout(2,7));
		colorDialog = new JDialog();
		colorDialog.setTitle("Adjust/Report Color");
		JPanel colorPanel = new JPanel(new GridLayout(0, 1));
		colorDialog.add(colorPanel);
		// Move / Duplicate choice

		JRadioButton md_move_color_button = new JRadioButton("Move");
		md_move_color_button.setMnemonic(KeyEvent.VK_M);
		md_move_color_button.setActionCommand("md_color");
		md_move_color_button.setSelected(true);
		md_move_color_button.addActionListener(this);

		JRadioButton md_dup_color_button = new JRadioButton("Duplicate");
		md_dup_color_button.setMnemonic(KeyEvent.VK_D);
		md_dup_color_button.setActionCommand("md_duplicate");
		md_dup_color_button.addActionListener(this);

		JPanel move_dup_color_panel = new JPanel();
		ButtonGroup move_dup_color_group = new ButtonGroup();
		move_dup_color_group.add(md_move_color_button);
		move_dup_color_group.add(md_dup_color_button);
		move_dup_color_panel.add(md_move_color_button);
		move_dup_color_panel.add(md_dup_color_button);
		colorPanel.add(move_dup_color_panel);

		JPanel moveto_panel = new JPanel();
		colorPanel.add(moveto_panel);

		// JComboBox<String> combo = new JComboBox<>();
		OurBlock cb = getSelected();
		if (cb == null) {
			cb = new OurBlock(new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1)), new Color(1,1,1,1));
		}
		colorRedField = new JTextField(String.format("%.2f", cb.getRed()));
		colorRedField.setActionCommand("ENTER");
		colorRedField.addActionListener(this);
		colorGreenField = new JTextField(String.format("%.2f", cb.getGreen()));
		colorGreenField.setActionCommand("ENTER");
		colorGreenField.addActionListener(this);
		colorBlueField = new JTextField(String.format("%.2f", cb.getBlue()));
		colorBlueField.setActionCommand("ENTER");
		colorBlueField.addActionListener(this);
		colorAlphaField = new JTextField(String.format("%.2f", cb.getAlpha()));
		colorAlphaField.setActionCommand("ENTER");
		colorAlphaField.addActionListener(this);
		JButton moveToButton = new JButton("ColorTo");
		moveToButton.setActionCommand("colorToButton");
		moveToButton.addActionListener(this);
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

		// Adjust by
		JPanel adjcolor_panel = new JPanel();
		colorPanel.add(adjcolor_panel);
		float adjamt = .1f; // Default adjustment
		adjcolor_panel.add(Box.createVerticalStrut(15)); // a spacer
		adjcolorRedField = new JTextField(String.format("%.2f", adjamt));
		adjcolorRedField.setActionCommand("adjcolorENTER");
		adjcolorRedField.addActionListener(this);
		adjcolorGreenField = new JTextField(String.format("%.2f", adjamt));
		adjcolorGreenField.setActionCommand("adjcolorENTER");
		adjcolorGreenField.addActionListener(this);
		adjcolorBlueField = new JTextField(String.format("%.2f", adjamt));
		adjcolorBlueField.setActionCommand("adjcolorENTER");
		adjcolorBlueField.addActionListener(this);
		adjcolorAlphaField = new JTextField(String.format("%.2f", adjamt));
		adjcolorAlphaField.setActionCommand("adjcolorENTER");
		adjcolorAlphaField.addActionListener(this);
		JButton adjcolorUpButton = new JButton("Up By");
		adjcolorUpButton.setActionCommand("adjcolorUpButton");
		adjcolorUpButton.addActionListener(this);
		JButton adjcolorDownButton = new JButton("Down By");
		adjcolorDownButton.setActionCommand("adjcolorDownButton");
		adjcolorDownButton.addActionListener(this);
		adjcolor_panel.add(Box.createVerticalStrut(15)); // a spacer
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
		colorDialog.pack();
		int yloc = frame.getHeight();
		if (posDialog != null)
			yloc += posDialog.getHeight();
		colorDialog.setLocation(new Point(0, yloc));
			
		colorDialog.setVisible(true);
		boolean done = false;
		// int result = JOptionPane.showConfirmDialog(null, colorPanel,
		// "Set/Adjust color", JOptionPane.OK_CANCEL_OPTION);

	}

	
	/**
	 * Remove Control/Display of position
	 */
	public void positionControlDispose() {
		if (posDialog != null) {
			posDialog.dispose();
			posDialog = null;
		}
	}
	
	/**
	 * Setup Control / Display  position of selected block
	 */
	public void positionControlSetup() {
		if (posDialog != null)
			return;					// Already present
		
		// JPanel panel = new JPanel(new GridLayout(2,7));
		posDialog = new JDialog();
		posDialog.setTitle("Adjust/Report Position");
		JPanel posPanel = new JPanel(new GridLayout(0, 1));
		posDialog.add(posPanel);

		JRadioButton ps_pos_button = new JRadioButton("Position");
		ps_pos_button.setActionCommand("ps_position");
		ps_pos_button.setSelected(true);
		ps_pos_button.addActionListener(this);

		JRadioButton ps_size_button = new JRadioButton("Size");
		ps_size_button.setActionCommand("ps_size");
		ps_size_button.setSelected(false);
		ps_size_button.addActionListener(this);


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
		mdmove_button.addActionListener(this);
		
		JRadioButton mddup_button = new JRadioButton("Duplicate");
		mddup_button.setMnemonic(KeyEvent.VK_D);
		mddup_button.setActionCommand("md_duplicate");
		mddup_button.addActionListener(this);

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
		OurBlock cb = getSelected();
		Point3D center = new Point3D(0,0,0);
		if (cb != null)
			center = cb.getCenter();
		posXfield = new JTextField(String.format("%.2f", center.x()));
		posXfield.setActionCommand("ENTER");
		posXfield.addActionListener(this);
		posYfield = new JTextField(String.format("%.2f", center.y()));
		posYfield.setActionCommand("ENTER");
		posYfield.addActionListener(this);
		posZfield = new JTextField(String.format("%.2f", center.z()));
		posZfield.setActionCommand("ENTER");
		posZfield.addActionListener(this);
		JButton moveToButton = new JButton("MoveTo");
		moveToButton.setActionCommand("moveToButton");
		moveToButton.addActionListener(this);
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
		adjposXfield.addActionListener(this);
		adjposYfield = new JTextField(String.format("%.2f", adjamt));
		adjposYfield.setActionCommand("adjposENTER");
		adjposYfield.addActionListener(this);
		adjposZfield = new JTextField(String.format("%.2f", adjamt));
		adjposZfield.setActionCommand("adjposENTER");
		adjposZfield.addActionListener(this);
		JButton adjposUpButton = new JButton("Up By");
		adjposUpButton.setActionCommand("adjposUpButton");
		adjposUpButton.addActionListener(this);
		JButton adjposDownButton = new JButton("Down By");
		adjposDownButton.setActionCommand("adjposDownButton");
		adjposDownButton.addActionListener(this);
		adjpos_panel.add(Box.createVerticalStrut(15)); // a spacer
		adjpos_panel.add(new JLabel("x-adj:"));
		adjpos_panel.add(adjposXfield);
		adjpos_panel.add(new JLabel("y-adj:"));
		adjpos_panel.add(adjposYfield);
		adjpos_panel.add(new JLabel("z-adj:"));
		adjpos_panel.add(adjposZfield);
		adjpos_panel.add(adjposUpButton);
		adjpos_panel.add(adjposDownButton);
		posDialog.pack();
		int yloc = frame.getHeight();
		if (colorDialog != null)
			yloc += colorDialog.getHeight();
		posDialog.setLocation(new Point(0, yloc));
		posDialog.setVisible(true);
		boolean done = false;
		// int result = JOptionPane.showConfirmDialog(null, posPanel,
		// "Set/Adjust Position", JOptionPane.OK_CANCEL_OPTION);

	}

	public void setColorOfSelection(float r, float g, float b) {
		if (indexOfSelectedBlock >= 0) {
			scene.setColorOfBlock(indexOfSelectedBlock, r, g, b);
		}
	}

	public void deleteSelection() {
		if (indexOfSelectedBlock >= 0) {
			int oldindex = indexOfSelectedBlock;
			scene.deleteBlock(indexOfSelectedBlock);
			int newindex = oldindex-1;			// Default - one previous to the departed
			if (newindex < 0 || newindex > scene.ourBlocks.size()-1) {
				newindex = scene.ourBlocks.size()-1;
			}
			select(newindex);
		}
	}

	public void deleteAll() {
		scene.deleteAllBlocks();
		indexOfSelectedBlock = -1;
		indexOfHilitedBox = -1;
	}

	public void lookAtSelection() {
		if (indexOfSelectedBlock >= 0) {
			Point3D p = scene.getBox(indexOfSelectedBlock).getCenter();
			camera.lookAt(p);
		}
	}

	public void resetCamera() {
		camera.setSceneRadius((float) Math.max(5 * OurBlock.DEFAULT_SIZE,
				scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();
	}

	public void init(GLAutoDrawable drawable) {
		this.drawable = drawable; // Save for later
		GL2 gl = (GL2) drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		glu = new GLU();
		glut = new GLUT();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		camera.setViewportDimensions(width, height);

		// set viewport
		gl.glViewport(0, 0, width, height);
	}

	/**
	 * Screen location to world coordinates
	 * 
	 * @param args
	 */
	void screen2WorldCoord(int x, // Screen (mouse) x
			int y, // Screen (mouse) y
			double zval, // desired z value
			double[] wcoord // World coordinates returned x,y,z,
	) {
		int realy = viewport[3] - (int) y - 1;
		if (SmTrace.tr("projection")) {
			System.out.println(String.format("screen2WorldCoord(x=%d,y=%d (realy=%d), z=%.2f", x, y, realy, zval));
		}
		glu.gluUnProject((double) x, (double) realy, zval, //
				mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
		System.out.println(String.format("   wx=%.2f, wy=%.2f, wz=%.2f", wcoord[0], wcoord[1], wcoord[2]));
	}

	/**
	 * Display 4x4 matrix (from double[16]
	 * 
	 * @param heading
	 *            - heading
	 * @param vals
	 *            double[16]
	 */
	public void printMatrix(String heading, double vals[]) {
		System.out.println("\n" + heading);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int idx = i + 4 * j;
				System.out.print(String.format("%7.2g", vals[idx]));
			}
			System.out.print("\n");
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// leave this empty
		System.out.println("displayChanged");
	}

	public void display(GLAutoDrawable drawable) {
		this.drawable = drawable;
		GL2 gl = (GL2) drawable.getGL();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		camera.transform(gl);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glFrontFace(GL.GL_CCW);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glShadeModel(GL2.GL_FLAT);

		scene.drawScene(gl, indexOfHilitedBox, enableCompositing);
		// Update for screen2World coordinate calculations
		int realy = 0; // GL y coord pos
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		if (SmTrace.tr("projection")) {
			printMatrix("display(): mvmatrix", mvmatrix);
			printMatrix("display(): projmatrix", projmatrix);
		}

		if (SmTrace.tr("projection")) {
			gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
			System.out.println(String.format("display():    viewport: %d %d %d %d", viewport[0], viewport[1],
					viewport[2], viewport[3]));
		}
		if (displayPositionControl) {
			positionControlSetup();
		} else {
			positionControlDispose();
		}
		if (displayColorControl) {
			colorControlSetup();
		} else {
			colorControlDispose();
		}
		if (displayWorldAxes) {
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(1, 0, 0);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 1, 0);
			gl.glColor3f(0, 0, 1);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 0, 1);
			gl.glEnd();
		}
		if (displayCameraTarget) {
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(-0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, -0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, -0.5f)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, 0.5f)).get(), 0);
			gl.glEnd();
		}
		if (displayBoundingBox) {
			gl.glColor3f(0.5f, 0.5f, 0.5f);
			scene.drawBoundingBoxOfScene(gl);
		}

		if (radialMenu.isVisible()) {
			radialMenu.draw(gl, glut, getWidth(), getHeight());
		}

		// gl.glFlush(); // I don't think this is necessary
	}

	private void updateHiliting() {
		Ray3D ray = camera.computeRay(mouse_x, mouse_y);
		Point3D newIntersectionPoint = new Point3D();
		Vector3D newNormalAtIntersection = new Vector3D();
		int newIndexOfHilitedBox = scene.getIndexOfIntersectedBox(ray, newIntersectionPoint, newNormalAtIntersection);
		hilitedPoint.copy(newIntersectionPoint);
		normalAtHilitedPoint.copy(newNormalAtIntersection);
		if (newIndexOfHilitedBox != indexOfHilitedBox) {
			indexOfHilitedBox = newIndexOfHilitedBox;
			repaint();
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mousePressed(%d,%d)", mouse_x, mouse_y));
		if (autoAdd != ExtendedModeler.AutoAddType.NONE) {
			// Placement point assuming z == 0
			double world_z = 0;
			double wcoord[] = new double[4];
			screen2WorldCoord(mouse_x, mouse_y, world_z, wcoord);
			Point3D p = new Point3D((float) wcoord[0], (float) wcoord[1], (float) world_z);
			if (indexOfSelectedBlock >= 0) {
				Point3D pc = scene.getBox(indexOfSelectedBlock).getCenter();
				Point3D pnew = new Point3D((float) wcoord[0], (float) wcoord[1], (float) wcoord[2]);
				p = pnew;
			}
			camera.lookAt(p);

			switch (autoAdd) {
			case DUPLICATE:
				duplicateBlock(); // Duplicate selected block
				break;

			case BOX:
				createNewBlock("box");
				break;

			case BALL:
				createNewBlock("ball");
				break;

			case CONE:
				createNewBlock("cone");
				break;

			case CYLINDER:
				createNewBlock("cylinder");
				break;
			default:
				System.out.println("Unrecognized AutoAdd value - ignored");
			}
			repaint();
			return;
		}
		if (radialMenu.isVisible()
				|| (SwingUtilities.isRightMouseButton(e) && !e.isShiftDown() && !e.isControlDown())) {
			int returnValue = radialMenu.pressEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		}

		updateHiliting();

		if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()) {
			select(indexOfHilitedBox);	///TBD - this change has broken stuff.....
			selectedPoint.copy(hilitedPoint);
			normalAtSelectedPoint.copy(normalAtHilitedPoint);
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mouseReleased(%d,%d)", mouse_x, mouse_y));

		if (radialMenu.isVisible()) {
			int returnValue = radialMenu.releaseEvent(mouse_x, mouse_y);

			int itemID = radialMenu.getItemID(radialMenu.getSelection());
			if (SmTrace.tr("select"))
				System.out.println(String.format("itemID=%d returnValue=%d", itemID, returnValue));
			switch (itemID) {
			case COMMAND_DUPLICATE_BLOCK:
				duplicateBlock(); // Duplicate selected block
				break;
			case COMMAND_CREATE_BOX:
				createNewBlock("box");
				break;
			case COMMAND_CREATE_BALL:
				createNewBlock("ball");
				break;
			case COMMAND_CREATE_CONE:
				createNewBlock("cone");
				break;
			case COMMAND_CREATE_CYLINDAR:
				createNewBlock("cylinder");
				break;
			case COMMAND_COLOR_RED:
				setColorOfSelection(1, 0, 0);
				break;
			case COMMAND_COLOR_YELLOW:
				setColorOfSelection(1, 1, 0);
				break;
			case COMMAND_COLOR_GREEN:
				setColorOfSelection(0, 1, 0);
				break;
			case COMMAND_COLOR_BLUE:
				setColorOfSelection(0, 0, 1);
				break;
			case COMMAND_DELETE:
				deleteSelection();
				break;
			}

			repaint();

			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		}
	}

	public void mouseMoved(MouseEvent e) {

		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		if (radialMenu.isVisible()) {
			int returnValue = radialMenu.moveEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		}

		updateHiliting();
	}

	public void mouseDragged(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		int delta_x = mouse_x - old_mouse_x;
		int delta_y = old_mouse_y - mouse_y;

		if (radialMenu.isVisible()) {
			int returnValue = radialMenu.dragEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		} else if (e.isControlDown()) {
			if (SwingUtilities.isLeftMouseButton(e) && SwingUtilities.isRightMouseButton(e)) {
				camera.dollyCameraForward((float) (3 * (delta_x + delta_y)), false);
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				camera.orbit(old_mouse_x, old_mouse_y, mouse_x, mouse_y);
			} else {
				camera.translateSceneRightAndUp((float) (delta_x), (float) (delta_y));
			}
			repaint();
		} else if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown() && indexOfSelectedBlock >= 0) {
			if (!e.isShiftDown()) {
				// translate a box

				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Plane plane = new Plane(normalAtSelectedPoint, selectedPoint);
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					Vector3D translation = Point3D.diff(intersection2, intersection1);
					scene.translateBlock(indexOfSelectedBlock, translation);
					repaint();
				}
			} else {
				// resize a box
				System.out.println("resize");
				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Vector3D v1 = Vector3D.cross(normalAtSelectedPoint, ray1.direction);
				Vector3D v2 = Vector3D.cross(normalAtSelectedPoint, v1);
				Plane plane = new Plane(v2, selectedPoint);
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					Vector3D translation = Point3D.diff(intersection2, intersection1);
					System.out.println(String.format("resize translation: %s", translation));

					// project the translation onto the normal, so that it is
					// only along one axis
					translation = Vector3D.mult(normalAtSelectedPoint,
							Vector3D.dot(normalAtSelectedPoint, translation));
					scene.resizeBlock(indexOfSelectedBlock, scene.ourBlocks.elementAt(indexOfSelectedBlock).getBox()
							.getIndexOfExtremeCorner(normalAtSelectedPoint), translation);
					repaint();
				}
			}
		}
	}

	/**
	 * OpenGl to screen coordinate translation
	 * 
	 * The first thing you should remember once and for all regarding screen
	 * coordinates - the upper left corner of the screen is (0,0) and lower
	 * right is (width, height) - no arguing! Now, lets say we got a 3D point in
	 * world coordinate space at (x, y, z) - this is not relative to the camera,
	 * but absolute (so the camera can have coordinates (cx, cy, cz)). The
	 * camera defines the viewMatrix, and I suppose you also have defined a
	 * projectionMatrix (you better have!). The last thing you need is the width
	 * and height of the are you are rendering on (not the whole screen!). If
	 * you have all these things, then it's pretty easy:
	 * 
	 * 
	 * function point2D get2dPoint(Point3D point3D, Matrix viewMatrix, Matrix
	 * projectionMatrix, int width, int height) {
	 * 
	 * Matrix4 viewProjectionMatrix = projectionMatrix * viewMatrix; //transform
	 * world to clipping coordinates point3D =
	 * viewProjectionMatrix.multiply(point3D); int winX = (int) Math.round(((
	 * point3D.getX() + 1 ) / 2.0) * width ); //we calculate -point3D.getY()
	 * because the screen Y axis is //oriented top->down int winY = (int)
	 * Math.round((( 1 - point3D.getY() ) / 2.0) * height ); return new
	 * Point2D(winX, winY); }
	 * 
	 **/

	/**
	 * Convert screen coordinates to OpenGL coordinates
	 * 
	 * @param point2D
	 * @param width
	 * @param height
	 * @param viewMatrix
	 * @param projectionMatrix
	 * @return
	 */
	/**
	 * TBD public Point3D get3dPoint(Point2D point2D, int width, int height,
	 * Matrix viewMatrix, Matrix projectionMatrix) {
	 * 
	 * double x = 2.0 * winX / clientWidth - 1; double y = - 2.0 * winY /
	 * clientHeight + 1; Matrix4 viewProjectionInverse =
	 * inverse(projectionMatrix * viewMatrix);
	 * 
	 * Point3D point3D = new Point3D(x, y, 0F); return
	 * viewProjectionInverse.multiply(point3D); }
	 */

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();
		System.out.println(String.format("action: %s", action));
		if (action.equals("md_move")) {
			pos_move_duplicate = true;
		} else if (action.equals("md_duplicate")) {
			pos_move_duplicate = false;
		} else if (action.equals("ps_position")) {
			pos_size_position = true;
		} else if (action.equals("ps_size")) {
			pos_size_position = false;
		} else if (action.equals("moveToButton")) {
			moveToPosition();
		} else if (action.equals("ENTER")) {
			moveToPosition();
		} else if (action.equals("adjposUpButton")) {
			adjustPosition(1);
		} else if (action.equals("adjposENTER")) {
			adjustPosition(1);
		} else if (action.equals("adjposDownButton")) {
			adjustPosition(-1);
		} else if (action.equals("moveToColorButton")) {
			moveToColor();
		} else if (action.equals("moveToColorENTER")) {
			moveToColor();
		} else if (action.equals("adjcolorUpButton")) {
			adjustColor(1);
		} else if (action.equals("adjcolorENTER")) {
			adjustColor(1);
		} else if (action.equals("adjcolorDownButton")) {
			adjustColor(-1);
		}

	}

	/**
	 * Color Control / Display
	 * 
	 **/

	/**
	 * Adjust controls based on current selection
	 * @param bindex - indes of currently selected block
	 */
	public void adjustControls() {
		OurBlock cb = getSelected();
		if (cb == null)
			return;
		if (posDialog != null) {
			Point3D min = cb.getMin();
			posXfield.setText(String.format("%.2g", min.x()));
			posYfield.setText(String.format("%.2g", min.y()));
			posZfield.setText(String.format("%.2g", min.z()));

			Vector3D size = cb.getSize();
			adjposXfield.setText(String.format("%.2g",size.x()));
			adjposYfield.setText(String.format("%.2g",size.y()));
			adjposZfield.setText(String.format("%.2g",size.z()));

			posDialog.repaint();
		}
		if (colorDialog != null) {
			Color color = cb.getColor();
			colorRedField.setText(String.format("%.2g", cb.getRed()));
			colorGreenField.setText(String.format("%.2g",cb.getGreen()));
			colorBlueField.setText(String.format("%.2g", cb.getBlue()));
			
			adjcolorRedField.setText(String.format("%.2g", cb.getRed()/10));
			adjcolorGreenField.setText(String.format("%.2g",cb.getGreen()/10));
			adjcolorBlueField.setText(String.format("%.2g", cb.getBlue()/10));

			colorDialog.repaint();
			
		}
		
	}
	/**
	 * Adjust by increments in direction 1- positive, -1 negative
	 * 
	 * @param direction
	 */
	private void adjustColor(int direction) {
		if (indexOfSelectedBlock < 0)
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
		OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
		if (!pos_move_duplicate) {
			cb = cb.duplicate();
			addBlock(cb);
		}
		cb.colorAdj(adjcolor_redval, adjcolor_greenval, adjcolor_blueval, adjcolor_alphaval);
		System.out.println(
				String.format("adjust to red=%.2f green=%.2f blue=%.2f  alpha=%.2f",
						cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha()));
		repaint();
	}

	private void moveToColor() {
		if (indexOfSelectedBlock < 0)
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

		OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
		Color new_color = new Color(redval, greenval, blueval);
		if (!color_move_duplicate) {
			cb = cb.duplicate();
			addBlock(cb);
		}
		cb.setColor(new_color);
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f", redval, greenval, blueval));
		repaint();
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
		if (indexOfSelectedBlock < 0)
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
		OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
		Point3D base_point = cb.getBasePoint();
		Vector3D adjpos_vector = new Vector3D(adjpos_xval, adjpos_yval, adjpos_zval);
		Point3D new_point = Point3D.sum(base_point, adjpos_vector);
		if (!pos_move_duplicate) {
			cb = cb.duplicate();
			addBlock(cb);
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
		repaint();
	}

	/**
	 * Move to position/size accordingly
	 */
	private void moveToPosition() {
		if (indexOfSelectedBlock < 0)
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

		OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
		Point3D new_point = new Point3D(xval, yval, zval);
		if (!pos_move_duplicate) {
			cb = cb.duplicate();
			addBlock(cb);
		}
		if (pos_size_position) {
			cb.moveTo(new_point);
		} else {
			Vector3D size = new Vector3D(new_point);
			cb.resize(0, size);
		}
		System.out.println(String.format("move to x=%.2f y=%.2f z=%.2f", xval, yval, zval));
		repaint();
	}
}
