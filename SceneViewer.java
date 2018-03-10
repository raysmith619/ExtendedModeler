package ExtendedModeler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.SwingUtilities;

import com.jogamp.graph.font.Font;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
///import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

class SceneViewer extends JFrame implements MouseListener, MouseMotionListener, GLEventListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Placement action types
	 *
	 */
	public enum PlacementActionType { // Placement action
		NONE, // none added
		PLACEMENT_MOVE, PLACEMENT_OK, PLACEMENT_CANCEL,
	}

	static int viewerLevelBase = 0; 	// viewers increase in level
	int viewerLevel; 					// All blocks >= will be invisible
	static int x0 = 0;
	static int y0 = 0;
	static int width = 400; 			// Canvas size
	static int height = width;
	String title; 						// Viewer title
	String name; 						// Viewer name - used in data keys
	JMenuBar menuBar;
	boolean isStub = false;				// true -> no controls, not visible
	EMCanvas canvas; 					// Our canvas
	JScrollPane scrollPane;				// Our Scroll pane
	public static int SCROLL_RANGE = 1000;
	public static int SCROLL_MIN = -SCROLL_RANGE/2;
	public static int SCROLL_MAX = SCROLL_RANGE/2;
	public static int SCROLL_MID = (SCROLL_MIN + SCROLL_MAX)/2;
	JScrollBar verticalScrollBar;
	JScrollBar horizontalScrollBar;
	GLCapabilities caps; 				// Main set of capabilities
	SceneControler sceneControler; 		// Main class - currently only for check button access
	SceneDraw sceneDraw; 				// Direct scene drawing control
	ControlsOfView controls; 			// Control boxes
	ControlMap controlMap; 				// Control Map window/frame

	EMBlock localViewEye; 				// Local view "eye" object
	SceneViewer externalViewer = null; 	// External view if non-null
	SceneViewer localViewer; 			// Local view if non-null
	SmTrace smTrace; // Trace support
	/// ControlsOfView controls; // Control boxes - use getControls - not ready at
	/// initialization
	/// GLAutoDrawable drawable; // USE canvas - inherits from
	/// GLU glu; // Needed for scene2WorldCoord
	/// GLUT glut;

	// Updated each display() call
	int viewport[] = new int[4];
	double mvmatrix[] = new double[16];
	double projmatrix[] = new double[16];
	
										// Grid Display settings
	float gridSpacing = 1.f;
	float labelSpacing = 5*gridSpacing;
	Color gridColor = new Color(1.f, 1.f, 1.f, 0.1f);
	float[] gridColorArray = new float[4];
	float gridBound = 10.f;
	Vector3D gridUp = new Vector3D(0,1,0);	// y
	//float gridXMin = -10.f;
	float gridXMin = -10.f;
	float gridXMax = -gridXMin;
	float gridYMin = gridXMin;
	float gridYMax = gridXMax;
	float gridZMin = gridXMin;
	float gridZMax = gridXMax;
	float gridTickLen = .3f;			// Axes tick length in world coord
	

	ColoredBox gridViewBox = new ColoredBox(new Point3D(-gridBound, -gridBound, -gridBound),
			new Vector3D(gridBound, gridBound, gridBound), null, gridUp);

	/// public int indexOfSelectedBlock = -1; // -1 for none
	/**
	 * Only used to go to previously select block(s) in case of delete
	 */
	private Stack<BlockSelect> selectStack = new Stack<BlockSelect>(); // Stack of recent selected

	private Point3D selectedPoint_ = new Point3D();
	private Vector3D normalAtSelectedPoint_ = new Vector3D();
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	Camera3D camera;
	ExtendedModeler.AutoAddType autoAdd = ExtendedModeler.AutoAddType.NONE; // Add
	boolean mousePressed; // true iff mouse is selected // item
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

	public boolean displayAddControl = true;
	public boolean displayColorControl = true;
	public boolean displayEyeAtControl = true;
	public boolean displayLookAtControl = true;
	public boolean displayPlacementControl = true;
	public boolean displayTextControl = true;
	public boolean displayWorldAxes = false;
	public boolean displayCameraTarget = false;
	public boolean displayLocalView = true;
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;

	// Tools Menu
	JButton eyeAtSelectionButton;
	JToggleButton displayAxesButton;
	JButton lookAtSelectionButton;
	JButton backOffButton;
	JButton resetCameraButton;
	JCheckBox displayEyeAtControlCheckBox;
	JCheckBox displayLookAtControlCheckBox;
	JCheckBox displayPlacementControlCheckBox;
	JCheckBox displayColorControlCheckBox;
	JCheckBox displayTextControlCheckBox;
	JCheckBox displayWorldAxesCheckBox;
	JCheckBox displayCameraTargetCheckBox;
	JCheckBox displayLocalViewCheckBox;
	JCheckBox displayBoundingBoxCheckBox;
	JCheckBox enableCompositingCheckBox;

	// View Menu
	JButton viewAllButton;
	JButton viewCloserButton;
	JButton viewFartherButton;
	JToggleButton viewGridButton;
	
	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;

	/**
	 * Default starting size, location
	 * 
	 * @param title
	 * @param name
	 * @param sceneControler
	 */
	public SceneViewer(String title, String name, SceneControler sceneControler) throws EMBlockError {
		this(title, name, sceneControler, false);
	}

	/**
	 * SceneViewer - with no controls / view
	 * 
	 * @param title
	 * @param name
	 * @param sceneControler
	 * @param isStub - has no controls, is not visible
	 */
	public SceneViewer(String title, String name, SceneControler sceneControler, boolean isStub) throws EMBlockError {
		this(title, name, sceneControler, null, null, null, null, null, isStub);
	}

	/**
	 * @param title
	 * @param name
	 * @param sceneControler
	 * @param viewBox
	 *            - viewing region, null - default
	 * @param eyePosition
	 * @param eyeTarget
	 * @param eyeUp
	 * @param locationSize
	 *            - location and size of window, null - default
	 * @param isStub - true - no controls, not visible
	 * @throws EMBlockError
	 */
	public SceneViewer(String title, String name, SceneControler sceneControler, ColoredBox viewBox,
			Point3D eyePosition, Point3D eyeTarget, Vector3D eyeUp, Rectangle locationSize,
			boolean isStub) throws EMBlockError {

		this.title = title;
		this.setTitle(title);
		this.name = name;
		this.sceneControler = sceneControler;
		viewerLevelBase++;
		this.viewerLevel = viewerLevelBase;
		this.isStub = isStub;
		if (viewBox == null) {
			viewBox = new ColoredBox();
		}
		if (locationSize == null) {
			setLocationSize();
		}

		this.setTitle(title);
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu = toolMenu();
		menuBar.add(menu);
		menuBar.add(new JSeparator());

		menu = viewMenu();
		menuBar.add(menu);
		menuBar.add(new JSeparator());

		displayAxesButton = new JToggleButton("Axes");
		displayAxesButton.addActionListener(this);
		menuBar.add(displayAxesButton);

		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.addActionListener(this);
		menuBar.add(lookAtSelectionButton);

		displayCameraTargetCheckBox = new JCheckBox("Display Camera Target", sceneControler.displayCameraTarget);
		///displayCameraTargetCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayCameraTargetCheckBox.addActionListener(this);
		menuBar.add(displayCameraTargetCheckBox);

		camera = new Camera3D(eyePosition, eyeTarget, eyeUp);

		SmTrace.lg(String.format("sceneView[%d] %s %s", getViewerLevel(), name, title));
		SmTrace.lg(String.format("sceneView: viewBox: %s", getViewBox()));
		SmTrace.lg(String.format("sceneView: eyePosition: %s", getEyePosition(), getEyeTarget()));
		SmTrace.lg(String.format("sceneView: eyeTarget: %s", getEyeTarget()));
		SmTrace.lg(String.format("sceneView: eyeUp: %s", getEyeUp()));
		SmTrace.lg(String.format(" "));

		caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

		canvas = new EMCanvas(caps);
		/*** Pre-scrolled
		 *
		this.getContentPane().add(canvas);
		 *
		 *** Pre-scrolled ***/
		
		/***
		 * Adding ScrollPane
		 */
        JScrollPane scrollPane = new JScrollPane(canvas);
		this.getContentPane().add(scrollPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		verticalScrollBar = scrollPane.getVerticalScrollBar();
		verticalScrollBar.setMinimum(SCROLL_MIN);
		verticalScrollBar.setValue(SCROLL_MID);
		verticalScrollBar.setMaximum(SCROLL_MAX);
		verticalScrollBar.setVisibleAmount(SCROLL_RANGE/500);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		horizontalScrollBar = scrollPane.getHorizontalScrollBar();
		horizontalScrollBar.setMinimum(SCROLL_MIN);
		horizontalScrollBar.setValue(SCROLL_MID);
		horizontalScrollBar.setMaximum(SCROLL_MAX);
		horizontalScrollBar.setVisibleAmount(SCROLL_RANGE/500);
		gridColor.getColorComponents(gridColorArray);		// Can't be done in class def
		
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.scrollBarAdjustmentEvent(e);
				
			}
		});
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.scrollBarAdjustmentEvent(e);
				
			}
		});
		/***
		 * Adding ScrollPane
		 */
		
		this.controls = new ControlsOfView(this);

		
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("SceneViewer resized");
				updateSize(e);

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				SmTrace.lg("SceneViewer moved", "move");
				updateLocation(e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

		setLocationSize();
		this.setVisible(true);
		canvas.getContext().makeCurrent(); /// Hack to avoid no GLContext
		this.sceneDraw = new SceneDraw(this);

		canvas.addGLEventListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

		/// camera.setSceneRadius((float) Math.max(5 * EMBlockBase.DEFAULT_SIZE,
		/// sceneControler.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();

	}

	public int getViewerLevel() {
		return viewerLevel;
	}

	public void setViewerLevel(int viewerLevel) {
		this.viewerLevel = viewerLevel;
	}

	/**
	 * Add viewer's menu
	 */
	private JMenuBar setupMenu(JMenuBar menuBar) {
		JMenu menu = toolMenu();
		menuBar.add(menu);
		return menuBar;
	}

	/**
	 * Create tools menu drop down
	 * 
	 * @retun - display menu item
	 */
	private JMenu toolMenu() {
		JMenu menu = new JMenu("Tools");
		JFrame toolFrame = new JFrame();

		Container toolPane = toolFrame.getContentPane();
		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
		/// menu.add(pane);
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		toolPane.setLayout(new BorderLayout());
		toolPane.add(toolPanel, BorderLayout.LINE_START);
		/// toolPane.add( sceneControler, BorderLayout.CENTER );
		/// toolPane.add(toolPanel);
		menu.add(toolPane);

		eyeAtSelectionButton = new JButton("EyeAt Selection");
		eyeAtSelectionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		eyeAtSelectionButton.addActionListener(this);
		toolPanel.add(eyeAtSelectionButton);

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		resetCameraButton.addActionListener(this);
		toolPanel.add(resetCameraButton);

		displayEyeAtControlCheckBox = new JCheckBox("Display EyeAt Control");
		displayEyeAtControlCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayEyeAtControlCheckBox.addActionListener(this);
		toolPanel.add(displayEyeAtControlCheckBox);

		displayLookAtControlCheckBox = new JCheckBox("Display LookAt Control");
		displayLookAtControlCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayLookAtControlCheckBox.addActionListener(this);
		toolPanel.add(displayLookAtControlCheckBox);

		displayWorldAxesCheckBox = new JCheckBox("Display World Axes", sceneControler.displayWorldAxes);
		displayWorldAxesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayWorldAxesCheckBox.addActionListener(this);
		toolPanel.add(displayWorldAxesCheckBox);

		displayLocalViewCheckBox = new JCheckBox("Display Local View Bounds", sceneControler.displayBoundingBox);
		displayLocalViewCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayLocalViewCheckBox.addActionListener(this);
		toolPanel.add(displayLocalViewCheckBox);

		displayBoundingBoxCheckBox = new JCheckBox("Display Bounding Box", sceneControler.displayBoundingBox);
		displayBoundingBoxCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		displayBoundingBoxCheckBox.addActionListener(this);
		toolPanel.add(displayBoundingBoxCheckBox);

		enableCompositingCheckBox = new JCheckBox("Enable Compositing", sceneControler.enableCompositing);
		enableCompositingCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		enableCompositingCheckBox.addActionListener(this);
		toolPanel.add(enableCompositingCheckBox);


		return menu;
	}

	

	/**
	 * Create view menu drop down
	 * 
	 * @retun - menu item
	 */
	private JMenu viewMenu() {
		JMenu menu = new JMenu("View");
		JFrame viewFrame = new JFrame();

		Container viewPane = viewFrame.getContentPane();
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
		viewPane.setLayout(new BorderLayout());
		viewPane.add(viewPanel, BorderLayout.LINE_START);
		/// viewPane.add( sceneControler, BorderLayout.CENTER );
		/// viewPane.add(viewPanel);
		menu.add(viewPane);

		viewCloserButton = new JButton("Closer");
		viewCloserButton.addActionListener(this);
		viewPanel.add(viewCloserButton);

		viewAllButton = new JButton("All");
		viewAllButton.addActionListener(this);
		viewPanel.add(viewAllButton);


		viewFartherButton = new JButton("Farther");
		viewFartherButton.addActionListener(this);
		viewPanel.add(viewFartherButton);

		viewGridButton = new JToggleButton("Grid");
		viewGridButton.addActionListener(this);
		viewPanel.add(viewGridButton);

		return menu;
	}

	
	/**
	 * Get current location and size
	 */
	public Rectangle getLocationSize() {
		Rectangle locsize = new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		return locsize;
	}

	/**
	 * Get viewer name
	 * Note: getName() is already used by component
	 */
	public String getSceneName() {
		return name;
	}
	
	
	/**
	 * Record external view
	 */
	public void setExternalViewer(SceneViewer externalViewer) {
		this.externalViewer = externalViewer;
	}

	/**
	 * Record local view
	 */
	public void setLocalViewer(SceneViewer localView) {
		this.localViewer = localView;
		setLocalViewerEye();
		localView.setExternalViewer(this);
	}

	/**
	 * Set /reset eye object for local Viewer camera
	 */
	public void setLocalViewerEye() {
		if (localViewEye == null) {
			ColoredEye eye = new ColoredEye(localViewer.camera.position,
					localViewer.camera.target,
					localViewer.camera.up,
					localViewer);
			EMBlock cbEye = new EMBlock(eye);
			cbEye.setViewerLevel(getViewerLevel());
			localViewEye = cbEye;
			sceneControler.insertBlock(cbEye);
		} else {
			localViewEye.moveTo(localViewer.camera.position);
		}
	}

	
	
	/**
	 * Clear local viewer
	 */
	public void clearLocalViewer() {
		if (localViewEye != null) {
			sceneControler.removeBlock(localViewEye.iD());
			externalViewer.dispatchEvent(new WindowEvent((short) 0, this, WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY));
		}
		localViewer = null;
	}

	private void dispatchEvent(WindowEvent windowEvent) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Drop our connection
	 */
	public void dropViewer() {
		///dispatchEvent(new WindowEvent((short) 0, this, WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY));
		///display();
		dispose();
	}

	private void setLocationSize() {
		int x = getXFromProp();
		int y = getYFromProp();
		if (x < 0 || y < 0) {
			x = x0;
			y = y0;
		}
		x0 = x; // Update location
		y0 = y;
		this.setLocation(x, y);

		int w = getWidthFromProp();
		int h = getHeightFromProp();
		if (w < 100 || h < 100) {
			w = width;
			h = height;
		}
		width = w; // Update size
		height = h;
		this.setSize(w, h);
	}

	/**
	 * Get blocks displayed by this viewer
	 */
	public int[] getDisplayedIds() {
		int[] ids = sceneControler.scene.getDisplayedIds();
		EMBlockGroup displayed = new EMBlockGroup();
		for (int id : ids) {
			EMBlock cb = sceneControler.getCb(id);
			if (cb.getViewerLevel() <= getViewerLevel())
				displayed.putBlock(cb); // display all with less level
		}
		return displayed.getIds();
	}

	
	/**
	 * Check if point is in view port
	 * @param gl
	 */
	public boolean isInViewPort(Point3D pt) {
		return camera.isInViewPort(pt);
	}

	/**
	 * Get eye position
	 */
	public Point3D getEyePosition() {
		return camera.position;
	}

	/**
	 * Get eye target
	 */
	public Point3D getEyeTarget() {
		return camera.target;
	}

	/**
	 * Get eye "up"
	 */
	public Vector3D getEyeUp() {
		return camera.up;
	}

	public Dimension getPreferredSize() {
		return new Dimension(512, 512);
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
	 * Set debug/diagnostic trace settings Sets absolutely, clearing all flags first
	 */
	public void traceSet(String trace_string) {
		SmTrace.clearFlags();
		SmTrace.setFlags(trace_string);
	}

	/**
	 * Set debug/diagnostic trace settings Sets from traceSelection group
	 */
	public void traceSelection() {
		SmTrace.clearFlags();
		String trace_string = "test1, test2";
		SmTrace.setFlags(trace_string);
	}

	/**
	 * Check if select stack empty
	 */
	public boolean selectIsEmpty() {
		return selectStack.isEmpty();
	}

	public BlockSelect selectPop() {
		return selectStack.pop();
	}

	public void selectPush(BlockSelect select) {
		selectStack.push(select);
	}

	/**
	 * Text representation
	 */
	public String selectToString(BlockSelect select) {
		String str = "";
		for (int id : select.getIds()) {
			if (str != "")
				str += ", ";
			str += sceneControler.cbGen(id);
		}
		return str;
	}

	/**
	 * Update selection display For now we just unselect all previous and select all
	 * new Add to selectStack if new_select is populated
	 */
	public boolean displayUpdate(BlockSelect new_select, BlockSelect prev_select) {
		for (int id : prev_select.getIds()) {
			sceneControler.setSelectionStateOfBox(id, false);
		}
		for (int id : new_select.getIds()) {
			sceneControler.setSelectionStateOfBox(id, true);
		}
		/**
		 * Update control display
		 */
		getControls().displayUpdate(new_select, prev_select);
		repaint();
		return true;
	}

	/**
	 * Get newest block index
	 */
	public int cbIndex() {
		return sceneControler.cbIndex();
	}

	/**
	 * Get controls
	 */
	public ControlsOfView getControls() {
		return controls;
	}

	public ColoredBox getBoundingBoxOfScene() {
		return sceneControler.getBoundingBoxOfScene();
	}

	/**
	 * Get block, given index
	 */
	public EMBlock getCb(int id) {
		return sceneControler.getCb(id);
	}

	/**
	 * Get generated block, given index null, if none
	 */
	public EMBlock cbGen(int id) {
		return sceneControler.cbGen(id);
	}

	/**
	 * Set camera(eye) at a point Updates visible controls
	 * NO Update external to avoid recursion
	 * @param pt
	 */
	public void eyeAt(Point3D pt) {
		camera.eyeAt(pt);
		ControlOfEye eac = (ControlOfEye) getControls().getControl("eyeat");
		if (eac != null)
			try {
				eac.setEyeAtPosition(pt);
			} catch (EMBlockError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		///if (localViewEye != null) {
		///	localViewEye.setPosition(pt);
		///}
	}

	
	public void setUp(Vector3D up) {
		camera.setUp(up);
	}
	
	
	/**
	 * Set camera to look at a point Updates visible controls
	 * 
	 * @param pt
	 */
	public void lookAt(Point3D pt) {
		camera.lookAt(pt);
		ControlOfLookAt lac = (ControlOfLookAt) getControls().getControl("lookat");
		if (lac != null)
			try {
				lac.setLookAtPosition(pt);
			} catch (EMBlockError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Repaint graphics canvas
	 * 
	 */
	public void repaint() {
		canvas.repaint();
	}

	/**
	 * Reset graphics
	 */
	public void reset() {
		repaint();
	}

	/**
	 * Check if we are in a mouse pressed state
	 */
	public boolean isMousePressed() {
		return mousePressed;
	}

	/**
	 * Set our mouse pressed state
	 */
	public void setMousePressed(boolean setting) {
		mousePressed = setting;
	}

	public void resetCamera() {
		camera.setSceneRadius((float) Math.max(5 * EMBlockBase.DEFAULT_SIZE,
				sceneControler.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();
		ColoredText.reset();
	}

	/**
	 * Insert block to scene display No other processing is done here Display update
	 * is done elsewhere
	 */
	public void insertBlock(int id) {
		sceneControler.insertBlock(id);
	}

	/**
	 * Insert blocks to display No additional processing is done here. Display
	 * update is done elsewhere.
	 */
	public void insertBlocks(int[] ids) {
		for (int id : ids) {
			insertBlock(id);
		}
	}

	/**
	 * Insert blocks to display No additional processing is done here. Display
	 * update is done elsewhere.
	 */
	public void insertBlocks(EMBlockGroup blocks) {
		sceneControler.insertBlocks(blocks);
	}

	public void init(GLAutoDrawable drawable) {
		/// this.drawable = drawable; // Save for later
		GL2 gl = (GL2) canvas.getGL();
		gl.glClearColor(0, 0, 0, 0);
		/// glu = new GLU();
		/// glut = new GLUT();
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
		SmTrace.lg(String.format("screen2WorldCoord(x=%d,y=%d (realy=%d), z=%.2f", x, y, realy, zval), "projection");
		((GLU) canvas.getGL()).gluUnProject((double) x, (double) realy, zval, //
				mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
		SmTrace.lg(String.format("   wx=%.2f, wy=%.2f, wz=%.2f", wcoord[0], wcoord[1], wcoord[2]), "projection");
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
		SmTrace.lg("\n" + heading);
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
		SmTrace.lg("displayChanged");
	}

	public void display() {
		sceneControler.setDisplayedViewer(this);
		if (SmTrace.trace("displayviewer")) {
			SmTrace.lg(String.format("display: viewer=%s", name));
			if (EMBlockBase.isExternalViewer())
				SmTrace.lg("display: tests external");
			if (EMBlockBase.isLocalViewer())
				SmTrace.lg("display: tests local");
		}
		if (externalViewer != null) {
			externalViewer.display(); 		// Update external display
		}

		if (SmTrace.trace("skipimage")) {
			if (EMBlockBase.isExternalViewer()) {
				SmTrace.lg(String.format("viewer %s display skipping in External Viewer", getSceneName()));
				return;
			}
		}
		
		GL2 gl = (GL2) canvas.getGL();
		if (gl == null)
			return;
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		camera.transform(gl);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		setLighting(gl);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glFrontFace(GL.GL_CCW);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glShadeModel(GL2.GL_FLAT);
		sceneDraw.drawScene(indexOfHilitedBox, enableCompositing);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		if (SmTrace.tr("projection")) {
			printMatrix("display(): mvmatrix", mvmatrix);
			printMatrix("display(): projmatrix", projmatrix);
		}

		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		SmTrace.lg(String.format("display():    viewport: %d %d %d %d", viewport[0], viewport[1], viewport[2],
				viewport[3]), "projection");

		setControl("eyeat", displayEyeAtControl);
		setControl("lookat", displayLookAtControl);
		setControl("placement", displayPlacementControl);
		setControl("color", displayColorControl);
		setControl("text", displayTextControl);
		setControl("component", displayAddControl);
		///getControls().display(canvas);

		if (viewGridButton.isSelected()) {
			displayGrid(gl);
		}

		if (displayAxesButton.isSelected()) {
			displayGridAxes(gl);
		}
		
		if (displayWorldAxes) {
			EMViewedText tr3 = new EMViewedText(gl);
			float tx = 1;
			float ty = 0;
			float tz = 0;
			
			setEmphasis(gl, new Color(1,0,0));
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(1, 0, 0);
			gl.glEnd();
			tx = 1;
			ty = 0;
			tz = 0;
			tr3.draw("X", tx, ty, tz);
			clearEmphasis();
			
			setEmphasis(gl, new Color(0,1,0));
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(0, 1, 0);			// Need this to see lines, letters
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 1, 0);
			gl.glEnd();
			tx = 0;
			ty = 1;
			tz = 0;
			tr3.draw("Y", tx, ty, tz);
			clearEmphasis();

			setEmphasis(gl, new Color(0,0,1));
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(0, 0, 1);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 0, 1);
			gl.glEnd();
			tx = 0;
			ty = 0;
			tz = 1;
			tr3.draw("Z", tx, ty, tz);
			clearEmphasis();
			
		}
		if (displayCameraTarget) {
			int nLongitudes = 10;
			int nLatitudes = nLongitudes;
			GLUT glut = new GLUT();
			float r = .1f;
			setEmphasis(gl, new Color(1,0,1));
			gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glPushMatrix();
			SmTrace.lg(String.format("displayCameraTarget sphere=%s", camera.target));
			gl.glColor3f(1, 1, 1);
			gl.glPushMatrix();
			gl.glTranslatef(camera.target.x(), camera.target.y(), camera.target.z());
			glut.glutWireSphere(r, nLongitudes, nLatitudes);
			gl.glPopMatrix();
			gl.glColor3f(1, 1, 1);
			SmTrace.lg(String.format("displayCameraTarget axes=%s", camera.target));
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(-0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, -0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, -0.5f)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, 0.5f)).get(), 0);
			gl.glEnd();
			gl.glPopMatrix();
			gl.glPopAttrib();
			clearEmphasis();
}
		if (displayLocalView && localViewer != null) {
			gl.glColor3f(0f, 0f, 1f);
			sceneDraw.drawLocalView(canvas);
		}
		if (displayBoundingBox) {
			gl.glColor3f(0.5f, 0.5f, 0.5f);
			sceneDraw.drawBoundingBoxOfScene(canvas);
		}

		if (radialMenu.isVisible()) {
			GLUT glut = new GLUT();
			radialMenu.draw(gl, glut, getWidth(), getHeight());
		}

		repaint();
	}

	/**
	 * Display a grid to show 3D space coordinates
	 * Use World axes
	 * @param gl
	 */
	public void displayGrid(GL2 gl) {
		SmTrace.lg("displayGrid", "drawGrid");
		setGridLighting(gl);
		float gridSpacing = 1.f;
		float label_spacing = 5*gridSpacing;
		gl_set(gl);			// Setup for trace
		/**
		 * Create grid with tree sets planes
		 * one set orthogonal to x axis, x = 0, 1*gridSpacing,... n*gridSpacing
		 * 										-1*gridSpacing,...
		 * one set orthogonal to y axis, y = 0, 1*gridSpacing,... n*gridSpacing
		 * 										-1*gridSpacing,... n*gridSpacing
		 * 
		 * The planes are translucent
		 */
		int niter = 0;
		int nmin = (int)(gridXMin/gridSpacing);
		int nmax = (int)(gridXMax/gridSpacing);
		for (int i = nmin; i <= nmax; i++) {
			float x = i*gridSpacing;
			if (x < gridXMin || x > gridXMax)
				continue;		// Out of view
			niter++;
			displayGridXPlane(gl, x);
		}
		if (niter == 0) {
			SmTrace.lg(String.format("displayGrid x niter=%d", niter));
		}
		
		niter = 0;
		nmin = (int)(gridYMin/gridSpacing);
		nmax = (int)(gridYMax/gridSpacing);
		for (int i = nmin; i <= nmax; i++) {
			float y = i*gridSpacing;
			if (y < gridYMin || y > gridYMax)
				continue;		// Out of view
			niter++;
			displayGridYPlane(gl, y);
		}
		if (niter == 0) {
			SmTrace.lg(String.format("displayGrid niter=%d", niter));
		}
		displayGridAxes(gl);
	}

	
	public void displayGridAxes(GL2 gl) {
		gl_set(gl);
		Color xcolor = new Color(1f,0f,0f,.1f);	// color in that direction
		Color ycolor = new Color(0f,1f,0f,.1f);
		Color zcolor = new Color(0.2f,0.2f,1f, .1f);
		displayGridLine(gl, new Point3D(gridXMin, 0, 0),
		new Point3D(gridXMax, 0,0), xcolor);
										// X-axis tics
										// Tics in direction of other axes
		for (float x = gridSpacing; x <= gridXMax; x += gridSpacing) {
			Point3D p1 = new Point3D(x, 0, 0);
			Point3D py = new Point3D(x, gridTickLen, 0);		// In y direction
			displayGridLine(gl, p1, py, ycolor);
			Point3D pz = new Point3D(x, 0, gridTickLen);
			displayGridLine(gl, p1, pz, zcolor);
		}
		for (float x = -gridSpacing; x >= gridXMin; x -= gridSpacing) {
			Point3D p1 = new Point3D(x, 0, 0);
			Point3D py = new Point3D(x, gridTickLen, 0);		// In y direction
			displayGridLine(gl, p1, py, ycolor);
			Point3D pz = new Point3D(x, 0, gridTickLen);
			displayGridLine(gl, p1, pz, zcolor);
		}

		// Y-axis tics
		
		displayGridLine(gl, new Point3D(0, gridYMin, 0),
						new Point3D(0, gridYMax,0), ycolor);
		// Tics in direction of other axes
		for (float y = gridSpacing; y <= gridYMax; y += gridSpacing) {
			Point3D p1 = new Point3D(0, y, 0);
			Point3D px = new Point3D(gridTickLen, y, 0);		// In x direction
			displayGridLine(gl, p1, px, xcolor);
			Point3D pz = new Point3D(0, y, gridTickLen);
			displayGridLine(gl, p1, pz, zcolor);
		}
		for (float y = -gridSpacing; y >= gridYMin; y -= gridSpacing) {
			Point3D p1 = new Point3D(0, y, 0);
			Point3D px = new Point3D(gridTickLen, y, 0);		// In x direction
			displayGridLine(gl, p1, px, xcolor);
			Point3D pz = new Point3D(0, y, gridTickLen);
			displayGridLine(gl, p1, pz, zcolor);
		}
		
		displayGridLine(gl, new Point3D(0, gridYMin, 0),
		new Point3D(0, gridYMax,0), ycolor);
		displayGridLine(gl, new Point3D(0, 0, gridZMin),
		new Point3D(0, 0, gridZMax), zcolor);
		setLighting(gl);

		// Z-axis tics
		displayGridLine(gl, new Point3D(0, 0, gridZMin),
						new Point3D(0, 0, gridZMax), zcolor);
		// Tics in direction of other axes
		for (float z = gridSpacing; z <= gridYMax; z += gridSpacing) {
			Point3D p1 = new Point3D(0, 0, z);
			Point3D px = new Point3D(gridTickLen, 0, z);		// In x direction
			displayGridLine(gl, p1, px, xcolor);
			Point3D py = new Point3D(0, gridTickLen, z);
			displayGridLine(gl, p1, py, ycolor);
		}
		for (float z = -gridSpacing; z >= gridYMin; z -= gridSpacing) {
			Point3D p1 = new Point3D(0, 0, z);
			Point3D px = new Point3D(gridTickLen, 0, z);		// In x direction
			displayGridLine(gl, p1, px, xcolor);
			Point3D py = new Point3D(0, gridTickLen, z);
			displayGridLine(gl, p1, py, ycolor);
		}
		
		setLighting(gl);
	}

	
	private boolean doDisplayGridByComp = true;
	public void displayGridXPlane(GL2 gl, float x) {
		SmTrace.lg("displayGridXPlane", "drawGridPlane");
		if (doDisplayGridByComp) {
			displayGridXPlaneByComp(gl, x);
			return;
		}
		gl.glPushAttrib(PROPERTIES);
		///gl.glEnable(GL.GL_BLEND);
		///gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		setEmphasis(gl, gridColor);
		///EMBlockBase.setMaterial(gl, gridColor);
		gl.glColor4fv(gridColorArray, 0);
		gl_glBegin(GL2.GL_QUADS, String.format("displayGridXPlane x=%.2f", x));
		gl_glVertex3f(x, gridYMin, gridZMin);
		gl_glVertex3f(x, gridYMax, gridZMin);
		gl_glVertex3f(x, gridYMax, gridZMax);
		gl_glVertex3f(x, gridYMin, gridZMax);
		gl_glEnd();
		///EMBlockBase.clearMaterial(gl);
		clearEmphasis();
		gl.glPopAttrib();

	}

	
	public void displayGridYPlane(GL2 gl, float y) {
		if (doDisplayGridByComp) {
			displayGridYPlaneByComp(gl, y);
			return;
		}
		Color gridColor = new Color(0.f, 0.f, 1.f, 0);
		float[] colorarray = new float[4];
		gl.glColor4fv(gridColor.getColorComponents(colorarray), 0);
		gl.glBegin(GL2.GL_QUADS);
		gl_glVertex3f(gridXMin, y, 0);
		gl_glVertex3f(0, y, gridZMin);
		gl_glVertex3f(gridXMax, y,  0);
		gl_glVertex3f(0, y, gridZMax);
		gl_glEnd();

	}
	
	public void setGridLighting(GL2 gl) {
		float light_ambient[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	
	/**
	 * Create grid plane using balls+lines
	 * because I can't get planes(QUADS) to work (transparency, view from all sides
	 */
	private void displayGridXPlaneByComp(GL2 gl, float x) {
		int niter = 0;
		int nmin = (int)(gridYMin/gridSpacing);
		int nmax = (int)(gridYMax/gridSpacing);
		for (int i = nmin; i <= nmax; i++) {
			float y = i*gridSpacing;
			if (y < gridYMin || y > gridYMax)
				continue;		// Out of view
			niter++;
			displayGridZLine(gl, x, y);
		}
		if (niter == 0) {
			SmTrace.lg(String.format("displayGridXPlaneByComp niter=%d", niter));
		}
	}

	
	/**
	 * Create grid plane using balls+lines
	 * because I can't get planes(QUADS) to work (transparency, view from all sides
	 */
	private void displayGridYPlaneByComp(GL2 gl, float y) {
		int niter = 0;
		int nmin = (int)(gridXMin/gridSpacing);
		int nmax = (int)(gridXMax/gridSpacing);
		for (int i = nmin; i <= nmax; i++) {
			float x = i*gridSpacing;
			if (x < gridXMin || x > gridXMax)
				continue;		// Out of view
			niter++;
			displayGridZLine(gl, x, y);
		}
		if (niter == 0) {
			SmTrace.lg(String.format("displayGridYPlaneByComp niter=%d", niter));
		}
	}
	
	/**
	 * Display Z line at (xn,yn) for grid display
	 * 
	 */
	private void displayGridZLine(GL2 gl, float x, float y) {
		displayGridLine(gl, new Point3D(x,y,gridZMin), new Point3D(x,y,gridZMax));
		Color gridColor = new Color(0.f, 0.f, 1.f, 0.1f);
		float[] colorarray = new float[4];
		int niter = 0;
		int nmin = (int)(gridZMin/gridSpacing);
		int nmax = (int)(gridZMax/gridSpacing);
		for (int i = nmin; i <= nmax; i++) {
			float z = i*gridSpacing;
			if (z < gridZMin || z > gridZMax)
				continue;		// Out of view
			niter++;
			displayGridPoint(gl, x, y, z);
		}
		if (niter == 0) {
			SmTrace.lg(String.format("displayGridXPlaneByComp niter=%d", niter));
		}
	}

	/**
	 * Display grid line
	 */
	private void displayGridLine(GL2 gl, Point3D p1, Point3D p2, Color color) {
		if (color == null)
			color = gridColor;
		gl.glPushAttrib(PROPERTIES);
		///gl.glEnable(GL.GL_BLEND);
		///gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		setEmphasis(gl, color);
		///EMBlockBase.setMaterial(gl, gridColor);
		float color_array[] = {0,0,0,0};
		gl.glColor4fv(color.getColorComponents(color_array), 0);
		gl_glBegin(GL2.GL_LINES, String.format("displayGridLine p1=%s p2=%s", p1, p2));
		gl_glVertex3f(p1.x(), p1.y(), p1.z());
		gl_glVertex3f(p2.x(), p2.y(), p2.z());
		gl_glEnd();
		///EMBlockBase.clearMaterial(gl);
		clearEmphasis();
		gl.glPopAttrib();
	}
	
	private void displayGridLine(GL2 gl, Point3D p1, Point3D p2) {
		displayGridLine(gl, p1, p2, null);
	}
	/**
	 * Display grid point (intersection)
	 */
	private void displayGridPoint(GL2 gl, float x, float y, float z, Color color) {
		if (color == null) {
			color = gridColor;
		}
		GLUT glut = new GLUT();
		float gridPointRadius = .02f;	// May be based on location
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		int ng = 10;
		
		ColoredBall.setMaterial(gl, color);
		glut.glutWireSphere(gridPointRadius, ng, ng);
		ColoredBall.clearMaterial(gl);
		gl.glPopMatrix();
	}

	private void displayGridPoint(GL2 gl, float x, float y, float z) {
		displayGridPoint(gl, x, y, z, null);
	}

	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */
	private static GL2 glp;				// for tracking routine
	private static String block = "grid";
	private static void gl_glVertex3f(float x, float y, float z) {
		String desc = traceDesc;
		glp.glVertex3f(x, y, z);
		SmTrace.lg(String.format("%s glVertex3f %.2f %.2f %.2f", block, x, y, z, desc), "draw");
	}
	private static String traceDesc = "trace description";
	private static void gl_set(GL2 gl) {
		glp = gl;
	}
	private static void gl_glBegin(int mode, String desc) {
		traceDesc = desc;
		if (glp == null) {
			SmTrace.lg(String.format("gl_set - not called %s", desc));
			System.exit(1);
		}
		glp.glBegin(mode);
		SmTrace.lg(String.format("%s gl_glBegin(%s)", block, desc), "draw");
	}
	private static void gl_glBegin(int mode) {
		String desc = String.format("mode(%d)", mode);
		gl_glBegin(mode, desc);
	}
	
	private static void gl_glEnd(String desc) {
		glp.glEnd();
		SmTrace.lg(String.format("%s gl_glEnd %s", block, desc), "draw");
	}
	private static void gl_glEnd() {
		String desc = traceDesc;
		gl_glEnd(desc);
	}
	
	public void setLighting(GL2 gl) {
		float light_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		float light_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	/**
	 * Set for lighting emphasis
	 * disable lighting set color
	 * Needs matching clearEmphasis
	 * @param gl
	 * @param color
	 */
	private Color emphasisColor = Color.WHITE;
	private GL2 glEmphasis = null;
	public void setEmphasis(GL2 gl, Color color) {
		glEmphasis = gl;
		if (color == null) {
			color = emphasisColor;
		}
		gl.glDisable(GL2.GL_LIGHTING);
		float cv[] = new float[4];
		float colors[] = color.getColorComponents(cv);
		gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
		
	}
	
	public void setEmphasis(GL2 gl) {
		setEmphasis(gl, null);
	}
	
	
	public void clearEmphasis() {
		if (glEmphasis == null) {
			SmTrace.lg("clearEmphasis - lacks matching setEmphasis");
		}
		glEmphasis.glEnable(GL2.GL_LIGHTING);
		glEmphasis = null;
	}
	
	
	public EMCanvas getCanvas() {
		return canvas;
	}

	public void setCanvas(EMCanvas canvas) {
		this.canvas = canvas;
	}

	public Scene getScene() {
		return sceneControler.scene;
	}

	private void updateHiliting() {
		Ray3D ray = camera.computeRay(mouse_x, mouse_y);
		Point3D newIntersectionPoint = new Point3D();
		Vector3D newNormalAtIntersection = new Vector3D();
		int newIndexOfHilitedBox = sceneControler.getIndexOfIntersectedBox(ray, newIntersectionPoint,
				newNormalAtIntersection);
		hilitedPoint.copy(newIntersectionPoint);
		normalAtHilitedPoint.copy(newNormalAtIntersection);
		if (newIndexOfHilitedBox != indexOfHilitedBox) {
			SmTrace.lg(String.format("SceneViewer.updateHiliting: indexOfHiliteBox(%d - was %d)", newIndexOfHilitedBox,
					indexOfHilitedBox), "hilite");
			indexOfHilitedBox = newIndexOfHilitedBox;

			repaint();
		}
	}

	/**
	 * our control locations
	 * 
	 */
	private String getPosKeyX() {
		String key = "control." + name + ".pos.x";
		return key;
	}

	private String getPosKeyY() {
		String key = "control." + name + ".pos.y";
		return key;
	}

	/**
	 * Return position, -1 if none
	 */
	public int getXFromProp() {
		String posstr = SmTrace.getProperty(getPosKeyX());
		if (posstr.equals(""))
			return -1;

		return Integer.valueOf(posstr);
	}

	public int getYFromProp() {
		String posstr = SmTrace.getProperty(getPosKeyY());
		if (posstr.equals(""))
			return -1;

		return Integer.valueOf(posstr);
	}

	/**
	 * Record current location
	 */
	public void recordLocation(int x, int y) {
		String pos_key_x = getPosKeyX();
		SmTrace.setProperty(pos_key_x, String.valueOf(x));
		String pos_key_y = getPosKeyY();
		SmTrace.setProperty(pos_key_y, String.valueOf(y));
	}

	/**
	 * Record current size
	 */
	public void recordSize(int width, int height) {
		String size_key_width = getSizeKeyWidth();
		SmTrace.setProperty(size_key_width, String.valueOf(width));
		String size_key_height = getSizeKeyHeight();
		SmTrace.setProperty(size_key_height, String.valueOf(height));
	}

	
	/**
	 * Set/reset location/size
	 * Update ScrollPane
	 */
	public void setLocationSize(Rectangle rec) {
		setLocation(rec.x, rec.y);
		setSize(rec.width, rec.height);
	}

	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		recordLocation(x, y);
	}
	

	public void setSize(int width, int height) {
		super.setSize(width, height);
		recordSize(width, height);
	}

	/**
	 * Update location Generally called after move
	 */
	public void updateLocation(ComponentEvent e) {
		Point pt = e.getComponent().getLocation();
		recordLocation(pt.x, pt.y);
		SmTrace.lg(String.format("updateLocation(%s: %d, %d)", name, pt.x, pt.y),
				"location");

	}

	/**
	 * Update size Generally called after resize
	 */
	public void updateSize(ComponentEvent e) {
		Dimension dim = e.getComponent().getSize();
		recordSize(dim.width, dim.height);
		SmTrace.lg(String.format(String.format("updateSize(%s: width=%d, height=%d)", name, dim.width, dim.height)));

	}

	/**
	 * Create external view Which includes and displays the target and the
	 * eye(camera) with default viewing box and screen location and size
	 * 
	 * @throws EMBlockError
	 */
	public SceneViewer externalView(String title, String name) throws EMBlockError {
		return externalView(title, name, null, null, null, null, null);
	}

	/**
	 * Create external view Which includes and displays the target and the
	 * eye(camera)
	 * 
	 * @throws EMBlockError
	 */
	public SceneViewer externalView(String title, String name, ColoredBox viewBox, Point3D eyePosition,
			Point3D eyeTarget, Vector3D eyeUp, Rectangle locationSize) throws EMBlockError {
		if (name == null)
			name = "External_View";
		if (title == null)
			title = "External " + name;

		if (viewBox == null) {
			viewBox = eViewBox();
		}
		if (eyePosition == null) {
			eyePosition = viewBox.getMax(); // Place at viewing box upper left corner
		}
		if (eyeTarget == null) {
			eyeTarget = this.camera.target; // Use same target
			eyeTarget = Point3D.average(this.camera.position, this.camera.target);
		}
		if (eyeUp == null) {
			eyeUp = this.camera.up; // Use same orientation
		}
		if (locationSize == null) {
			/**
			 * Place the new view to the right of the current view
			 */
			Rectangle locsize = getLocationSize();
			Rectangle new_locsize = new Rectangle(locsize.x + locsize.width + 2, locsize.y, locsize.width,
					locsize.height);
			locationSize = new_locsize;
		}

		externalViewer = new SceneViewer(title, name, sceneControler, viewBox, eyePosition, eyeTarget, eyeUp,
				locationSize, isStub);
		externalViewer.setLocalViewer(this); // Record local viewer for display/manipulation
		return externalViewer;
	}

	
	/**
	 * Calculate extended view box
	 */
	public ColoredBox eViewBox(float factor) {
		/**
		 * Expand viewing region to show based viewing region plus viewing target
		 * (lookAt) and camera position (origin)
		 */
		if (factor == 0)
			factor  = .5f;
		ColoredBox lvbox = getViewBox();
		ColoredBox evbox = new ColoredBox(lvbox);
		evbox.bound(camera.target);
		evbox.bound(camera.position);
		Vector3D adj = Vector3D.mult(evbox.getDiagonal(), factor);
		evbox.bound(Point3D.sum(evbox.getMax(), adj));
		return evbox;
	}
	public ColoredBox eViewBox() {
		return eViewBox(0);
	}

	
	/**
	 * Update external view based on localViewer changes
	 */
	public void updateFromLocalViewer() {
		if (localViewer == null)
			return;					// No local viewer - ignore
		
		ColoredBox viewBox = localViewer.eViewBox();
		Point3D eyePosition = viewBox.getMax(); // Place at viewing box upper left corner
		Point3D eyeTarget = localViewer.camera.target; // Use same target
		///eyeTarget = Point3D.average(localViewer.camera.position, localViewer.camera.target);
		Vector3D eyeUp = localViewer.camera.up; // Use same orientation
		setUp(eyeUp);
		eyeAt(eyePosition);
		lookAt(eyeTarget);
		setLocalViewerEye();
	}
	
	
	/**
	 * Get currently selected block
	 */
	public int getSelectedBlockIndex() {
		return sceneControler.getSelectedBlockIndex();
	}

	/**
	 * our view size
	 * 
	 */
	private String getSizeKeyWidth() {
		String key = "viewer." + name + ".size.width";
		return key;
	}

	private String getSizeKeyHeight() {
		String key = "viewer." + name + ".size.height";
		return key;
	}

	/**
	 * Return position, -1 if none
	 */
	public int getWidthFromProp() {
		String sizestr = SmTrace.getProperty(getSizeKeyWidth());
		if (sizestr.equals(""))
			return -1;

		return Integer.valueOf(sizestr);
	}

	
	/**
	 * Get viewer's up
	 */
	public Vector3D getUp() {
		return camera.up;
	}
	
	
	/**
	 * Get Viewing box, viewed by camera
	 * 
	 * @return
	 */
	public ColoredBox getViewBox() {
		return camera.getViewBox();
	}

	public int getHeightFromProp() {
		String sizestr = SmTrace.getProperty(getSizeKeyHeight());
		if (sizestr.equals(""))
			return -1;

		return Integer.valueOf(sizestr);
	}

	public void mouseClicked(MouseEvent e) {
		SmTrace.lg("mouseClick", "mouse");
		if (!e.isControlDown() && isAddAtMouse() ) {
			addAtMouse(e);
			return;
		}
		if (e.isControlDown())
			SmTrace.lg("isControlDown", "mouse");
		if (indexOfHilitedBox >= 0) {
			if (e.isControlDown()) {
				EMBCommand bcmd;
				String action = "emc_mouseAddSelect";
				try {
					bcmd = new BlkCmdAdd(action);
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}
				if (sceneControler.isSelected(indexOfHilitedBox)) {
					bcmd.removeSelect(indexOfHilitedBox);
				} else {
					sceneControler.selectAdd(bcmd, indexOfHilitedBox, true); // keep with selected
				}
				bcmd.doCmd();
			} else {
				EMBCommand bcmd;
				String action = "emc_mouseNewSelect";
				try {
					bcmd = new BlkCmdAdd(action);
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}
				sceneControler.selectAdd(bcmd, indexOfHilitedBox, false);
				bcmd.doCmd();
			}
		} else {
			EMBCommand bcmd;
			String action = "emc_mouseUnSelect";
			try {
				bcmd = new BlkCmdAdd(action);
			} catch (Exception e2) {
				e2.printStackTrace();
				return;
			}
							// Unselect all currently selected
			EMBlock[] cbs = sceneControler.getSelectedBlocks();
			if (cbs.length == 0)
				return;
			
			bcmd.prevSelect = bcmd.newSelect;
			bcmd.newSelect = new BlockSelect();
			bcmd.doCmd();
		}
		selectedPoint(hilitedPoint());
		normalAtSelectedPoint(normalAtHilitedPoint());
		repaint();
	}

	
	private void addAtMouse(MouseEvent e) {
		ControlOfComponent coco = (ControlOfComponent)sceneControler.controls.getControl("component");
		if (coco == null) {
			return;
		}
		coco.addAtMouse(e);
	}

	public boolean isAddAtMouse() {
		ControlOfComponent coco = (ControlOfComponent)sceneControler.controls.getControl("component");
		if (coco == null) {
			return false;
		}
		return coco.isAddAtMouse();
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
		SmTrace.lg(String.format("mousePressed(%d,%d)", mouse_x, mouse_y), "mouse");
		try {
			if (autoAdd != ExtendedModeler.AutoAddType.NONE) {
				// Placement point assuming z == 0
				double world_z = 0;
				double wcoord[] = new double[4];
				screen2WorldCoord(mouse_x, mouse_y, world_z, wcoord);
				Point3D p = new Point3D((float) wcoord[0], (float) wcoord[1], (float) world_z);
				if (sceneControler.anySelected()) {
					Point3D pnew = new Point3D((float) wcoord[0], (float) wcoord[1], (float) wcoord[2]);
					p = pnew;
				}
				camera.lookAt(p);
				EMBCommand bcmd = null;
				switch (autoAdd) {
				case DUPLICATE:
					try {
						bcmd = new BlkCmdAdd("emc_duplicate");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					try {
						sceneControler.duplicateBlock(bcmd);
					} catch (EMBlockError e2) {
						e2.printStackTrace();
						SmTrace.lg("duplicateBlock error");
						return;
					}
					break;

				case BOX:
					try {
						bcmd = new BlkCmdAdd("emc_duplicate");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "box");
					break;

				case BALL:
					try {
						bcmd = new BlkCmdAdd("ball");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "ball");
					break;

				case CONE:
					try {
						bcmd = new BlkCmdAdd("emc_cone");
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "cone");
					break;

				case CYLINDER:
					try {
						bcmd = new BlkCmdAdd("emc_cylinder");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "cylinder");
					break;

				case TEXT:
					try {
						bcmd = new BlkCmdAdd("emc_text");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "text");
					break;

				default:
					SmTrace.lg("Unrecognized AutoAdd value - ignored");
				}
				if (bcmd != null)
					bcmd.doCmd();
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
		} catch (EMBlockError eb) {
			SmTrace.lg(String.format("mousePressed error %s", eb.getMessage()));
			return;
		}
	}

	public void mouseReleased(MouseEvent e) {
		setMousePressed(false);
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		try {
			SmTrace.lg(String.format("mouseReleased(%d,%d)", mouse_x, mouse_y), "mouse");

			if (radialMenu.isVisible()) {
				int returnValue = radialMenu.releaseEvent(mouse_x, mouse_y);

				int itemID = radialMenu.getItemID(radialMenu.getSelection());
				SmTrace.lg(String.format("itemID=%d returnValue=%d", itemID, returnValue), "select");
				EMBCommand bcmd = null;
				switch (itemID) {
				case COMMAND_DUPLICATE_BLOCK:
					try {
						bcmd = new BlkCmdAdd("duplicate_block");
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					try {
						sceneControler.duplicateBlock(bcmd);
					} catch (EMBlockError e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						SmTrace.lg("duplicateBlock error");
						return;
					} // Duplicate selected block
					break;
				case COMMAND_CREATE_BOX:
					try {
						bcmd = new BlkCmdAdd("emc_create_box");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "box");
					break;
				case COMMAND_CREATE_BALL:
					try {
						bcmd = new BlkCmdAdd("emc_create_ball");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "ball");
					break;
				case COMMAND_CREATE_CONE:
					try {
						bcmd = new BlkCmdAdd("emc_create_cone");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "cone");
					break;
				case COMMAND_CREATE_CYLINDAR:
					try {
						bcmd = new BlkCmdAdd("emc_create_cylinder");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.createNewBlock(bcmd, "cylinder");
					break;
				case COMMAND_COLOR_RED:
					try {
						bcmd = new BlkCmdAdd("emc_color_red");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.setColorOfSelection(bcmd, 1, 0, 0);
					break;
				case COMMAND_COLOR_YELLOW:
					try {
						bcmd = new BlkCmdAdd("emc_color_yello");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.setColorOfSelection(bcmd, 1, 1, 0);
					break;
				case COMMAND_COLOR_GREEN:
					try {
						bcmd = new BlkCmdAdd("emc_color_green");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.setColorOfSelection(bcmd, 0, 1, 0);
					break;
				case COMMAND_COLOR_BLUE:
					try {
						bcmd = new BlkCmdAdd("emc_color_blue");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.setColorOfSelection(bcmd, 0, 0, 1);
					break;
				case COMMAND_DELETE:
					try {
						bcmd = new BlkCmdAdd("emc_delete");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					sceneControler.deleteSelection(bcmd);
					break;
				}

				repaint();

				if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
					return;
				if (bcmd != null) {
					bcmd.saveCmd();
				}
			}
		} catch (EMBlockError eb) {
			SmTrace.lg(String.format("mouseReleased error %s", eb.getMessage()));
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
		} else if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown() && sceneControler.anySelected()) {
			if (!e.isShiftDown()) {
				EMBlock[] cbs = sceneControler.getSelectedBlocks();
				if (cbs.length == 0)
					return;
				// translate a box

				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				if (SmTrace.trace("drag")) {
					SmTrace.lg(String.format("drag: %d %s normalAtSelectedPoint: %s selectedPoint: %s",
							cbs[0].iD(), cbs[0].blockType(),
							normalAtSelectedPoint(), selectedPoint()));
				}
		
				Plane plane = new Plane(normalAtSelectedPoint(), selectedPoint());
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					EMBCommand bcmd;
					String action = "mouseDragBlock";
					try {
						bcmd = new BlkCmdAdd(action);
					} catch (Exception e2) {
						e2.printStackTrace();
						return;
					}
					if (SmTrace.trace("dragInterSect")) {
						SmTrace.lg(String.format("drag:  %d %s  intersection1 %s intersection2: %s",
								cbs[0].iD(), cbs[0].blockType(),
								intersection1, intersection2));
					}
					Vector3D translation = Point3D.diff(intersection2, intersection1);
					if (SmTrace.trace("dragInterSect")) {
						float ts = 10;
						if (cbs[0].blockType().equals("xxxScale")) {
							SmTrace.lg(String.format("Scaling translation by %f", ts));
							translation = translation.mult(translation, ts);
						}
						SmTrace.lg(String.format("drag: translation: %s",
								translation));
					}
					if (SmTrace.trace("dragILarge")) {
						if (translation.length() > .1) {
							SmTrace.lg(String.format("dragILarge:  %d %s translation: %s",
									cbs[0].iD(), cbs[0].blockType(),
									translation));
							SmTrace.lg(String.format("dragILarge:  %d %s  intersection1 %s intersection2: %s",
									cbs[0].iD(), cbs[0].blockType(),
									intersection1, intersection2));
						}
					}
					if (SmTrace.trace("settranslation")) {
						translation = new Vector3D(1,1,1);
					}
					for (int i = 0; i < cbs.length; i++) {
						EMBlock cb = cbs[i];
						bcmd.addPrevBlock(cb);
						cb.translate(translation);
						bcmd.addBlock(cb);
					}
					bcmd.doCmd();
				}
			} else {
				// resize a box
				EMBlock[] cbs = sceneControler.getSelectedBlocks();
				if (cbs.length == 0)
					return;
				SmTrace.lg("resize");
				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Vector3D v1 = Vector3D.cross(normalAtSelectedPoint(), ray1.direction);
				Vector3D v2 = Vector3D.cross(normalAtSelectedPoint(), v1);
				Plane plane = new Plane(v2, selectedPoint());
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					EMBCommand bcmd;
					String action = "mouseSizeBlock";
					try {
						bcmd = new BlkCmdAdd(action);
					} catch (Exception e2) {
						e2.printStackTrace();
						return;
					}

					Vector3D translation = Point3D.diff(intersection2, intersection1);
					SmTrace.lg(String.format("resize translation: %s", translation));

					// project the translation onto the normal, so that it is
					// only along one axis
					translation = Vector3D.mult(normalAtSelectedPoint(),
							Vector3D.dot(normalAtSelectedPoint(), translation));
					for (int i = 0; i < cbs.length; i++) {
						EMBlock cb = cbs[i];
						bcmd.addPrevBlock(cb);
						cb.resize(0, translation);
					}
					bcmd.doCmd();
				}
			}
		}
	}

	/**
	 * OpenGl to screen coordinate translation
	 * 
	 * The first thing you should remember once and for all regarding screen
	 * coordinates - the upper left corner of the screen is (0,0) and lower right is
	 * (width, height) - no arguing! Now, lets say we got a 3D point in world
	 * coordinate space at (x, y, z) - this is not relative to the camera, but
	 * absolute (so the camera can have coordinates (cx, cy, cz)). The camera
	 * defines the viewMatrix, and I suppose you also have defined a
	 * projectionMatrix (you better have!). The last thing you need is the width and
	 * height of the are you are rendering on (not the whole screen!). If you have
	 * all these things, then it's pretty easy:
	 * 
	 * 
	 * function point2D get2dPoint(Point3D point3D, Matrix viewMatrix, Matrix
	 * projectionMatrix, int width, int height) {
	 * 
	 * Matrix4 viewProjectionMatrix = projectionMatrix * viewMatrix; //transform
	 * world to clipping coordinates point3D =
	 * viewProjectionMatrix.multiply(point3D); int winX = (int) Math.round(((
	 * point3D.getX() + 1 ) / 2.0) * width ); //we calculate -point3D.getY() because
	 * the screen Y axis is //oriented top->down int winY = (int) Math.round((( 1 -
	 * point3D.getY() ) / 2.0) * height ); return new Point2D(winX, winY); }
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
	 * TBD public Point3D get3dPoint(Point2D point2D, int width, int height, Matrix
	 * viewMatrix, Matrix projectionMatrix) {
	 * 
	 * double x = 2.0 * winX / clientWidth - 1; double y = - 2.0 * winY /
	 * clientHeight + 1; Matrix4 viewProjectionInverse = inverse(projectionMatrix *
	 * viewMatrix);
	 * 
	 * Point3D point3D = new Point3D(x, y, 0F); return
	 * viewProjectionInverse.multiply(point3D); }
	 */

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	/**
	 * Print displayed blocks
	 */
	public void displayPrint(String tag, String trace) {
		if (tag == null) {
			tag = "displayList";
		}
		int[] block_ids = sceneControler.getDisplayedIds();
		if (block_ids.length == 0) {
			SmTrace.lg(String.format("%s displayed: None", tag), trace);
			return;
		}

		String str = "";

		for (int id : block_ids) {
			if (str != "") {
				str += ", ";
			}
			str += sceneControler.getDisplayedBlocks().getBlock(id);
		}
		SmTrace.lg(String.format("%s displayed:%s", tag, str), trace);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// Check on menu commands
		Object source = ae.getSource();
		if (source == eyeAtSelectionButton) {
			this.eyeAtSelection();
			this.repaint();
		} else if (source == viewAllButton) {
			this.viewAll();
			this.repaint();
		} else if (source == viewCloserButton) {
			this.viewCloser();
			this.repaint();
		} else if (source == viewFartherButton) {
			this.viewFarther();
			this.repaint();
		} else if (source == viewGridButton) {
			this.modViewGrid();
			this.repaint();
		} else if (source == displayAxesButton) {
			this.modDisplayAxes();
			this.repaint();
		} else if (source == lookAtSelectionButton) {
			this.lookAtSelection();
			this.repaint();
		} else if (source == resetCameraButton) {
			this.resetCamera();
			this.repaint();
		} else if (source == displayEyeAtControlCheckBox) {
			this.displayEyeAtControl = !this.displayEyeAtControl;
			this.setControl("eyeAt", this.displayEyeAtControl);
			this.repaint();
		} else if (source == displayLookAtControlCheckBox) {
			this.displayLookAtControl = !this.displayLookAtControl;
			this.setControl("lookat", this.displayLookAtControl);
			this.repaint();
		} else if (source == displayPlacementControlCheckBox) {
			this.displayPlacementControl = !this.displayPlacementControl;
			this.setControl("placement", this.displayPlacementControl);
			this.repaint();
		} else if (source == displayColorControlCheckBox) {
			this.displayColorControl = !this.displayColorControl;
			this.setControl("color", this.displayColorControl);
			this.repaint();
		} else if (source == displayTextControlCheckBox) {
			this.displayTextControl = !this.displayTextControl;
			this.setControl("text", this.displayTextControl);
			this.repaint();
		} else if (source == displayWorldAxesCheckBox) {
			this.displayWorldAxes = !this.displayWorldAxes;
			this.repaint();
		} else if (source == displayCameraTargetCheckBox) {
			this.displayCameraTarget = !this.displayCameraTarget;
			this.repaint();
		} else if (source == displayBoundingBoxCheckBox) {
			this.displayBoundingBox = !this.displayBoundingBox;
			this.repaint();
		} else if (source == enableCompositingCheckBox) {
			this.enableCompositing = !this.enableCompositing;
			this.repaint();
		}

		// Check on commands
		String action = ae.getActionCommand();
		SmTrace.lg(String.format("action: %s", action));
		try {
			if (getControls().ckDoAction(action))
				return;
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("getControls().ckDoaction(%s): %s", action, e.getMessage()));
			e.printStackTrace();
			return;
		}
	}

	public void eyeAtSelection() {
		sceneControler.eyeAtSelection();
	}

	/**
	 * Set camera to look at a point Updates visible controls
	 * 
	 * @param pt
	 */
	public void lookAt_OBSOLETE(Point3D pt) {
		/// currentViewerCamera().lookAt(pt);
		ControlOfLookAt lac = (ControlOfLookAt) controls.getControl("lookat");
		if (lac != null)
			try {
				lac.setLookAtPosition(pt);
			} catch (EMBlockError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public void lookAtSelection() {
		sceneControler.lookAtSelection();
	}
	
	/**
	 * Position so as  to see all objects
	 * target in middle of objects
	 * eye at distance to see all objects
	 * Attempt to keep as close as possible to the previous eye
	 * Algorithm
	 * 1. Calculate the new maximum(max) and minimum(min) of displayed objects.
	 * 2. new.target = average(max,min)
	 * 3. min2max = vector min to max
	 * 4. target_plane = plane with: normal = min2max, point = new.target
	 * 5. old_eye_plane = plane with: normal = min2max, point = old.eye
	 * 6. old_eye_plane_p1 = point on old_eye_plane intersecting with min2max
	 * 7. vec_oep1_to_new_target = vector(new.target, old_eye_plane_p1)
	 * 8. new.eye = point(old.eye + vec_oep1_to_new_target)
	 * 5. Move new.eye on new.line, perpendicular to min2max, till angle(min, new.eye, max) is
	 *    the desired angle for viewing. 
	 */
	public void viewAll() {
		Point3D max = getMax();
		Point3D min = getMin();
		Point3D old_eye = getEyePosition();
		Point3D new_target = Point3D.average(max, min);
				/** TBD - rough approximation
				 * twice as far from max as max is from center
				 */
		Vector3D min2max = Point3D.diff(max, min);			// Vector along maximum dimension
		Plane target_plane = new Plane(min2max, new_target);
		Plane old_eye_plane = new Plane(min2max, old_eye);
		Ray3D min2max_ray = new Ray3D(new_target, min2max);
		Point3D old_eye_plane_p1 = new Point3D();
		old_eye_plane.intersects(min2max_ray, old_eye_plane_p1, true);
		Vector3D p1_to_new_target = Point3D.diff(new_target, old_eye_plane_p1);
		Point3D new_eye_1 = Point3D.sum(old_eye, p1_to_new_target);
		Vector3D new_target_to_eye_n = Point3D.diff(new_target, new_eye_1).normalized();
		Vector3D new_target_to_eye = Vector3D.mult(new_target_to_eye_n, min2max.length()*2.0f);
		Point3D new_eye = Point3D.sum(new_target, new_target_to_eye);
		eyeAt(new_eye);
		if (externalViewer != null)
			externalViewer.updateFromLocalViewer();
	}
	
	/**
	 * Get closer to target
	 */
	public void viewCloser() {
		Point3D target = camera.target;
		Point3D eye = camera.position;
		Point3D new_eye = Point3D.average(target, eye);
		eyeAt(new_eye);
		if (externalViewer != null)
			externalViewer.updateFromLocalViewer();
	}
	
	/**
	 * 	Move eye further away
	 *  Double distance between eye and target
	 */
	public void viewFarther() {
		Point3D target = camera.target;
		Point3D eye = camera.position;
		Vector3D eye_change = Point3D.diff(eye, target);
		Point3D new_eye = Point3D.sum(eye, eye_change);
		eyeAt(new_eye);
		if (externalViewer != null)
			externalViewer.updateFromLocalViewer();
	}
	
	/**
	 * 	Toggle grid display
	 */
	public void modViewGrid() {
		if (viewGridButton.isSelected()) {
			viewGridButton.setText("No Grid");
		} else {
			viewGridButton.setText("Grid");
		}
	}
	
	/**
	 * 	Toggle axes display
	 */
	public void modDisplayAxes() {
		if (displayAxesButton.isSelected()) {
			displayAxesButton.setText("No Axes");
		} else {
			displayAxesButton.setText("Axes");
		}
	}

	


	public Point3D getMin() {
		float xmin = getMinX();
		float ymin = getMinY();
		float zmin = getMinZ();
		return new Point3D(xmin, ymin, zmin);
	}

	
	public float getMinX() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinX();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinY() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinY();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinZ() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}
	
	
	public Point3D getMax() {
		float xmax = getMaxX();
		float ymax = getMaxY();
		float zmax = getMaxZ();
		return new Point3D(xmax, ymax, zmax);
	}

	
	public float getMaxX() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxX();
			if (first) {
				limit = value;
				first = false;
			} else if (value > limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxY() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxY();
			if (first) {
				limit = value;
				first = false;
			} else if (value > limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxZ() {
		EMBlockGroup group = getDisplayedBlocks();
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value > limit){
				limit = value; 
			}
		}
		return limit;		
	}
	
	
	/**
	 * Add component control
	 * 
	 * @throws EMBlockError
	 */
	public void addBlockButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("addBlockButton(%s) select", action), "action");
		try {
			if (bcmd == null) {
				SmTrace.lg(String.format("addBlockButton(%s) with no cmd - ignored", action));
				return;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		switch (action) {

		default:
			SmTrace.lg(String.format("Unrecognized addBlockButton: %s - ignored", action));
			return;
		}
	}

	/**
	 * Control / Access to highlighted
	 */
	public Point3D hilitedPoint(Point3D point) {
		if (point != null) {
			this.hilitedPoint = new Point3D(point);
		}
		return this.hilitedPoint;
	}
	public Point3D hilitedPoint() {
		return this.hilitedPoint;
	}

	
	public Vector3D normalAtHilitedPoint(Vector3D normal) {
		if (normal != null) {
			this.normalAtHilitedPoint = new Vector3D(normal);
		}
		return this.normalAtHilitedPoint;
	}
	public Vector3D normalAtHilitedPoint() {
		return this.normalAtHilitedPoint;
	}

	/**
	 * Control / Access to selected
	 */
	public Point3D selectedPoint(Point3D point) {
		if (point != null) {
			this.selectedPoint_ = new Point3D(point);
		}
		return this.selectedPoint_;
	}
	public Point3D selectedPoint() {
		return this.selectedPoint_;
	}

	
	public Vector3D normalAtSelectedPoint(Vector3D normal) {
		if (normal != null) {
			this.normalAtSelectedPoint_ = new Vector3D(normal);
		}
		return normalAtSelectedPoint();
	}
	public Vector3D normalAtSelectedPoint() {
		if (normalAtSelectedPoint_.length() == 0) {
			SmTrace.lg("normalAtSelectedPoint==0");
			normalAtSelectedPoint_ = getUp();
		}
		return normalAtSelectedPoint_;
	}

	
	/**
	 * Add/Remove Control/Display
	 */
	public void setControl(String controlName, boolean on) {
		getControls().setControl(controlName, on);
	}

	public void setCheckBox(String name, boolean checked) {
		sceneControler.setCheckBox(name, checked);
	}

	public EMDisplay getDisplay() {
		EMDisplay disp = new EMDisplay("SceneViewer");
		disp.putBlocks(getDisplayedBlocks());
		return disp;
	}

	private EMBlockGroup getDisplayedBlocks() {
		return sceneControler.getDisplayedBlocks();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		display();

	}

}
