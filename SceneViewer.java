package ExtendedModeler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
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

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

class SceneViewer extends JFrame
	implements MouseListener,
			MouseMotionListener,
			GLEventListener,
			ActionListener
			{
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
	
	int width = 400;				// Canvas size
	int height = width;
	String name;					// Viewer name
	GLCanvas canvas;				// Our canvas
	GLCapabilities caps;			// Main set of capabilities
	SceneControler sceneControler;		// Main class - currently only for check button access
	JFrame frame;					// Access to main frame
	ControlMap controlMap;			// Control Map window/frame
	SmTrace smTrace; 				// Trace support
	///ControlsOfView controls;	// Control boxes - use getControls - not ready at initialization
	///GLAutoDrawable drawable; 		// USE canvas - inherits from 
	///GLU glu; // Needed for scene2WorldCoord
	///GLUT glut;
	
	// Updated each display() call
	int viewport[] = new int[4];
	double mvmatrix[] = new double[16];
	double projmatrix[] = new double[16];

	private SceneDraw sceneDraw;	// Access to scene drawing
	///public int indexOfSelectedBlock = -1; // -1 for none
									/**
									 * Only used to go to previously
									 * select block(s) in case of delete
									 */
	private Stack<BlockSelect> selectStack = new Stack<BlockSelect>();			// Stack of recent selected
	
	private Point3D selectedPoint = new Point3D();
	private Vector3D normalAtSelectedPoint = new Vector3D();
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	Camera3D camera = new Camera3D();
	ExtendedModeler.AutoAddType autoAdd = ExtendedModeler.AutoAddType.NONE; // Add
	boolean mousePressed;		// true iff mouse is selected																		// item
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
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;
	
	public SceneViewer(String name,
		SceneControler sceneControler
		)
		throws EMBlockError {


		this.name = name;
		this.sceneControler = sceneControler;
		
		this.setTitle(name);
		caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		
		canvas = new GLCanvas(caps);

		this.getContentPane().add(canvas);
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("SceneViewer resized");
				updateSize(e);
				
			}
 			@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("SceneViewer moved");
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
	} );

		
		setLocationSize();
		this.setVisible(true);
		canvas.getContext().makeCurrent(); 	///Hack to avoid no GLContext
		this.sceneDraw = new SceneDraw(sceneControler, canvas);
		
		canvas.addGLEventListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		
		camera.setSceneRadius((float) Math.max(5 * EMBlockBase.DEFAULT_SIZE,
				sceneControler.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();

	}

	
	private void setLocationSize() {
	int x = getXFromProp();
	int y = getYFromProp();
	if (x < 0 || y < 0 ) {
		x = 100;
		y = 100;
	}
	this.setLocation(x, y);
	
	int w = getWidthFromProp();
	int h = getHeightFromProp();
	if (x < 0 || y < 0 ) {
		w = width;
		h = height;
	}
	this.setSize(w, h);
	}


	/**
	 * Get generated blocks' ids
	 */
	public int[] getDisplayedIds() {
		return sceneControler.getDisplayedIds();
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
	 * Set debug/diagnostic trace settings
	 * Sets absolutely, clearing all flags first
	 */
	public void traceSet(String trace_string) {
		SmTrace.clearFlags();
		SmTrace.setFlags(trace_string);
	}

	
	/**
	 * Set debug/diagnostic trace settings
	 * Sets from traceSelection group
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
	 * Update selection display
	 * For now we just unselect all previous and select all new
	 * Add to selectStack if new_select is populated
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
		ControlsOfView controls = sceneControler.getControls();
		return controls;
	}
	
	/**
	 * Get block, given index
	 */
	public EMBlock getCb(int id) {
		return sceneControler.getCb(id);
	}

	
	/**
	 * Get generated block, given index
	 * null, if none
	 */
	public EMBlock cbGen(int id) {
		return sceneControler.cbGen(id);
	}

	
	/**
	 * Set camera(target) at a point
	 * Updates visible controls
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
	}

	
	/**
	 * Set camera to look at a point
	 * Updates visible controls
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
	 * Insert block to scene display
	 * No other processing is done here
	 * Display update is done elsewhere
	 */
	public void insertBlock(int id) {
		sceneControler.insertBlock(id);
	}
	
	/**
	 * Insert blocks to display
	 * No additional processing is done here.
	 * Display update is done elsewhere.
	 */
	public void insertBlocks(int[] ids) {
		for (int id : ids) {
			insertBlock(id);
		}
	}
	
	/**
	 * Insert blocks to display
	 * No additional processing is done here.
	 * Display update is done elsewhere.
	 */
	public void insertBlocks(EMBlockGroup blocks) {
		sceneControler.insertBlocks(blocks);
	}

	public void init(GLAutoDrawable drawable) {
		///this.drawable = drawable; // Save for later
		GL2 gl = (GL2) canvas.getGL();
		gl.glClearColor(0, 0, 0, 0);
		///glu = new GLU();
		///glut = new GLUT();
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
		GL2 gl = (GL2) canvas.getGL();
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

		sceneDraw.drawScene(indexOfHilitedBox, enableCompositing);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		if (SmTrace.tr("projection")) {
			printMatrix("display(): mvmatrix", mvmatrix);
			printMatrix("display(): projmatrix", projmatrix);
		}

		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		SmTrace.lg(String.format("display():    viewport: %d %d %d %d", viewport[0], viewport[1],
				viewport[2], viewport[3]), "projection");
		
		setControl("eyeat", displayEyeAtControl);
		setControl("lookat", displayLookAtControl);
		setControl("placement", displayPlacementControl);
		setControl("color", displayColorControl);
		setControl("text", displayTextControl);
		setControl("component", displayAddControl);
		getControls().display(canvas);
		displayWorldAxes = true;			/// Force on
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
			sceneDraw.drawBoundingBoxOfScene(canvas);
		}

		if (radialMenu.isVisible()) {
			GLUT glut = new GLUT();
			radialMenu.draw(gl, glut, getWidth(), getHeight());
		}

		// gl.glFlush(); // I don't think this is necessary
	}

	private void updateHiliting() {
		Ray3D ray = camera.computeRay(mouse_x, mouse_y);
		Point3D newIntersectionPoint = new Point3D();
		Vector3D newNormalAtIntersection = new Vector3D();
		int newIndexOfHilitedBox = sceneControler.getIndexOfIntersectedBox(ray, newIntersectionPoint, newNormalAtIntersection);
		hilitedPoint.copy(newIntersectionPoint);
		normalAtHilitedPoint.copy(newNormalAtIntersection);
		if (newIndexOfHilitedBox != indexOfHilitedBox) {
			indexOfHilitedBox = newIndexOfHilitedBox;
			repaint();
		}
	}

	
	/** our control locations
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
	 * Update location
	 * Generally called after move
	 */
	public void updateLocation(ComponentEvent e) {
		Point pt = e.getComponent().getLocation();
		recordLocation(pt.x, pt.y);
		SmTrace.lg(String.format(String.format("updateLocation(%s: %d, %d)"
				,  name, pt.x, pt.y)));
		
	}
	
	
	/**
	 * Update size
	 * Generally called after resize
	 */
	public void updateSize(ComponentEvent e) {
		Dimension dim = e.getComponent().getSize();
		recordSize(dim.width, dim.height);
		SmTrace.lg(String.format(String.format("updateSize(%s: width=%d, height=%d)"
				,  name, dim.width, dim.height)));
		
	}

	
	/** our view size
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

	
	public int getHeightFromProp() {
		String sizestr = SmTrace.getProperty(getSizeKeyHeight());
		if (sizestr.equals(""))
			return -1;
		
		return Integer.valueOf(sizestr);
	}

	public void mouseClicked(MouseEvent e) {
		SmTrace.lg("mouseClick", "mouse");
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
					sceneControler.selectAdd(bcmd, indexOfHilitedBox, true);		// keep with selected
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
		}
		selectedPoint.copy(hilitedPoint);
		normalAtSelectedPoint.copy(normalAtHilitedPoint);
		repaint();
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
		} else if (SwingUtilities.isLeftMouseButton(e)
					&& !e.isControlDown()
					&& sceneControler.anySelected()) {
			if (!e.isShiftDown()) {
				EMBlock[] cbs = sceneControler.getSelectedBlocks();
				if (cbs.length == 0)
					return;
				// translate a box

				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Plane plane = new Plane(normalAtSelectedPoint, selectedPoint);
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					EMBCommand bcmd;
					String action = "mouseDragBlock";
					try {
						bcmd = new BlkCmdAdd(action);
					} catch (Exception e2) {
						e2.printStackTrace();
						return;
					}
					Vector3D translation = Point3D.diff(intersection2, intersection1);
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
				Vector3D v1 = Vector3D.cross(normalAtSelectedPoint, ray1.direction);
				Vector3D v2 = Vector3D.cross(normalAtSelectedPoint, v1);
				Plane plane = new Plane(v2, selectedPoint);
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
					translation = Vector3D.mult(normalAtSelectedPoint,
							Vector3D.dot(normalAtSelectedPoint, translation));
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
		String action = ae.getActionCommand();
		SmTrace.lg(String.format("action: %s", action));
		try {
			if (getControls().ckDoAction(action))
				return;
		} catch (EMBlockError e) {
			SmTrace.lg(String.format(
					"getControls().ckDoaction(%s): %s",
					action, e.getMessage()));
			e.printStackTrace();
			return;
		}
	}

	
	/**
	 * Add component control
	 * @throws EMBlockError 
	 */
	public void addBlockButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("addBlockButton(%s) select", action), "action");
		try {
			if (bcmd == null) {
				SmTrace.lg(String.format(
						"addBlockButton(%s) with no cmd - ignored",
						action));
				return;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		switch(action) {
			case "emc_selectAllButton":
				sceneControler.selectAll(bcmd);
				break;
			
			case "emc_duplicateBlockButton":
				sceneControler.duplicateBlock(bcmd); 			// Duplicate selected block
				break;
				
			case "emc_deleteBlockButton":
				sceneControler.deleteSelection(bcmd);
				break;
				
			case "emc_deleteBlockAllButton":
				sceneControler.deleteAll(bcmd);
				break;
			
			case "emc_addBoxButton":
				sceneControler.createNewBlock(bcmd, "box");
				break;
				
			case "emc_addBallButton":
				sceneControler.createNewBlock(bcmd, "ball");
				break;
				
			case "emc_addConeButton":
				sceneControler.createNewBlock(bcmd, "cone");
				break;
				
			case "emc_addCylinderButton":
				sceneControler.createNewBlock(bcmd, "cylinder");
				break;
				
			case "emc_addTextButton":
				sceneControler.createNewBlock(bcmd, "text");
				break;
				
			default:
				SmTrace.lg(String.format(
						"Unrecognized addBlockButton: %s - ignored", action));
				return;
		}
	}

	
	/**
	 * Add new text
	 * @throws EMBlockError 
	 */
	public void addTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("addTextButton(%s) select", action), "action");
		if (bcmd == null) {
			SmTrace.lg(String.format(
					"addTextButton(%s) with no cmd - ignored",
					action));
			return;
		}
		
		EMBlock cb = new EMBlock(new ColoredText(getControls()));
		if (cb != null) {
			int id = bcmd.addBlock(cb);
			bcmd.selectBlock(id);		// Select new block
		}
	}

	
	/**
	 * Change selected text, based on control settings
	 * @throws EMBlockError 
	 */
	public void changeTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("changeTextButton(%s) select", action), "action");
		BlockSelect bg = sceneControler.getSelected();
		int[] ids = bg.getIds();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			EMBlock cb = getCb(id);
			bcmd.addPrevBlock(cb);
			cb.adjustFromControl((ControlOfText)getControls().getControl("text"), bcmd);
			bcmd.addBlock(cb);
		}
	}

	
	/**
	 * Add new text, duplicate of selected
	 * @throws EMBlockError 
	 */
	public void dupTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("dupTextButton(%s) select", action), "action");
		selectTextButton(bcmd, action);
		addTextButton(bcmd, action);
	}

	
	/**
	 * Populate ControlOfText with currently selected block
	 * @throws EMBlockError 
	 */
	public void selectTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		sceneControler.selectPrint(String.format("selectTextButton(%s) select", action), "action");
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
