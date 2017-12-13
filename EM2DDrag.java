/**
 * EM2Drag.java
 * 2 dimensional graphics input tool, providing x-y mouse position
 * and direction with potential to support coordinated 3-dimensional
 * control.
 */

package EMGraphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.jogamp.opengl.GLCapabilities;

import smTrace.SmTrace;

public class EM2DDrag  extends JPanel
						implements MouseListener, MouseMotionListener {
	String title;		// Display title
	String emType = "xy";	// xy, xz
	JFrame frame;		// Enclosing frame
	String xLabel;		// Descriptive label
	float xMin;
	float xCurrent;
	float xMax;

	String yLabel;		// Descriptive label
	float yMin;
	float yCurrent;
	float yMax;
	final String iffmt = "%.2f";
	int xLabelHeight = 20;		// Top (x) label height
	int yLabelWidth = 400;		// Left (y) label width
	int xValueSize = 200;		// x plotting surface (min)
	int yValueSize = 200;		// y plotting surface (min)
	static boolean setupDone = false;	// Set true when setup complete
	static EM2DDrag em_base;	// flag indicating complete
	JPanel xyRegionPanel;		// xy plotting field
	
	JTextField xMinField;
	JTextField xCurrentField;	// current xvalue field 
	JTextField xMaxField;
	
	JTextField yCurrentField;	// current yvalue field
	
	private static int mainArgIndex;					// current Arg index
	private static String[] mainArgs;						// Args array
	private static int mainArgsLength;					// Number of args
	private static EM3DPosition em3Dbase = null;					// Set to 3D testing
	
	private EM3DPosition emPosition;		// cooperative collection, if any
	
	public EM2DDrag(
		String title,
		String emType,
		String xLabel, float xMin, float xMax,
		String yLabel, float yMin, float yMax) {
		setLayout(new BorderLayout());
		this.title = title;
		this.emType = emType;
		this.xLabel = xLabel;
		this.xMin = xMin;
		this.xMax = xMax;
		this.xCurrent = (xMin+xMax)/2;
		
		this.yLabel = yLabel;
		this.yMin = yMin;
		this.yMax = yMax;
		this.yCurrent = (yMin+yMax)/2;
		setup();
	}

	/**
	 * Setup two dimensional slider with setable/trackable values
	 */
	public void setup() {
		///JPanel xyField = new JPanel(new BorderLayout());
		///add(xyField, BorderLayout.CENTER);
		setBackground(Color.gray);
		setBorder(new BevelBorder(1));
		///xyField.setSize(200, 200);
		///setMinimumSize(new Dimension(500,500));
		
		JPanel xrow_panel = new JPanel(new BorderLayout());			// top row: blank, x-labels
		xrow_panel.setBackground(Color.yellow);
		add(xrow_panel, BorderLayout.NORTH);
		
		JPanel ulcorner = new JPanel(new BorderLayout());
		JTextField title_field = new JTextField(String.format("%s", title));
		ulcorner.add(title_field, BorderLayout.CENTER);	// Now contains title
		ulcorner.setBackground(Color.white);
		ulcorner.setMaximumSize(new Dimension(xLabelHeight, yLabelWidth));
		ulcorner.setSize(new Dimension(xLabelHeight, yLabelWidth));
		xrow_panel.add(ulcorner, BorderLayout.WEST);			// Upper left corner - blank
		
		JPanel xLabelingPanel = new JPanel(new GridLayout(2,1));  // values/label
		
		JPanel xValuesPanel = new JPanel(new GridLayout(1, 3));
		xValuesPanel.setBorder(new EmptyBorder(0,0,0,0));

		JPanel xLabelPanel = new JPanel(new BorderLayout());

		JTextField xLabelField = new JTextField(xLabel);
		xLabelField.setHorizontalAlignment(JTextField.CENTER);
		xLabelPanel.add(xLabelField, BorderLayout.CENTER);
		
		xMinField = new JTextField(String.format(iffmt, xMin));
		xMinField.setHorizontalAlignment(JTextField.LEFT);
		xMinField.setBorder(new EmptyBorder(0,0,0,0));
		///JPanel xCurrentPanel = new JPanel(new BorderLayout());
		xCurrentField = new JTextField(String.format(iffmt, xCurrent));
		xCurrentField.setHorizontalAlignment(JTextField.CENTER);
		xCurrentField.setBorder(new EmptyBorder(0,0,0,0));
		///xCurrentPanel.add(xCurrentField, BorderLayout.CENTER);
		xMaxField = new JTextField(String.format(iffmt, xMax));
		xMaxField.setHorizontalAlignment(JTextField.CENTER);		// Why does RIGHT make the value disappear?
		xMaxField.setBorder(new EmptyBorder(0,0,0,0));
		

		
		
		xValuesPanel.add(xMinField);
		xValuesPanel.add(xCurrentField);
		xValuesPanel.add(xMaxField, BorderLayout.CENTER);
		
		xLabelingPanel.add(xValuesPanel);
		xLabelingPanel.add(xLabelPanel);

		xrow_panel.add(xLabelingPanel, BorderLayout.CENTER);

		
		JPanel yrow_xy_region_panel = new JPanel(new BorderLayout());	// y-labels, x-y region
		///yrow_xy_region_panel.setSize(100, 200);
		yrow_xy_region_panel.setBackground(Color.GREEN);
		add(yrow_xy_region_panel, BorderLayout.CENTER);
		
		


		JPanel yLabelingPanel = new JPanel(new BorderLayout());
		yLabelingPanel.setBackground(Color.WHITE);
		yrow_xy_region_panel.add(yLabelingPanel, BorderLayout.WEST);
		xyRegionPanel = new JPanel(new BorderLayout());
		///xyRegionPanel.setMinimumSize(new Dimension(xValueSize, yValueSize));

		xyRegionPanel.setBorder(new BevelBorder(1));
		yrow_xy_region_panel.add(xyRegionPanel);
		//xyRegionPanel.setSize(200,200);

		JLabel yLabelField = new JLabel(yLabel);
		JPanel yValuesPanel = new JPanel(new GridLayout(3,1));
		
		JTextField yMinField = new JTextField(String.format(iffmt, yMin));
		yMinField.setBorder(new EmptyBorder(0,0,0,0));
		yCurrentField = new JTextField(String.format(iffmt, yCurrent));
		yCurrentField.setBorder(new EmptyBorder(0,0,0,0));
		JTextField yMaxField = new JTextField(String.format(iffmt, yMax));
		yMaxField.setBorder(new EmptyBorder(0,0,0,0));
		
		yLabelingPanel.add(yValuesPanel, BorderLayout.WEST);
		yLabelingPanel.add(yLabelField, BorderLayout.CENTER);
		yLabelField.setBackground(Color.WHITE);
		
		yValuesPanel.add(yMinField, BorderLayout.NORTH);
		//yValuesPanel.add(new JPanel());	// spacer
		yValuesPanel.add(yCurrentField, BorderLayout.CENTER);
		//yValuesPanel.add(new JPanel());	// spacer
		yValuesPanel.add(yMaxField, BorderLayout.SOUTH);

		addMouseListener(this);
		xyRegionPanel.addMouseListener(this);
		xyRegionPanel.addMouseMotionListener(this);
		setVisible(true);
	}
	/*** enabled by xyRegionPanel.addMouseMotionListener(new EMMouseMotionListener());
	class EMMouseMotionListener implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {
	         SmTrace.lg("Mouse Dragged: ("+e.getX()+", "+e.getY() +")");
		}

		public void mouseMoved(MouseEvent e) {
			SmTrace.lg("Mouse Moved: ("+e.getX()+", "+e.getY() +")", "mouse");
		}    
	}    
	***/
	
	
	public int getHeight() {
		return xyRegionPanel.getHeight();
	}

	public int getWidth() {
		return xyRegionPanel.getWidth();
	}

	public float getxMax() {
		return xMax;
	}

	public void setxMax(float xMax) {
		this.xMax = xMax;
	}

	public float getyMax() {
		return yMax;
	}

	public void setyMax(float yMax) {
		this.yMax = yMax;
	}

	public float getxCurrent() {
		return xCurrent;
	}

	public void setxCurrent(float xCurrent) {
		this.xCurrent = xCurrent;
		xCurrentField.setText(String.format("%.2g", this.xCurrent)); 
		SmTrace.lg(String.format("setxCurrent %s Loc x: %.2f (%.2f, %.2f) y: %.2f (%.2f, %.2f)", title,
				getxCurrent(),  xMin, xMax,
				getyCurrent(), yMin, yMax), "graphic");
	}

	
	public float getyCurrent() {
		return yCurrent;
	}

	public void setyCurrent(float yCurrent) {
		this.yCurrent = yCurrent;
		yCurrentField.setText(String.format("%.2g", this.yCurrent)); 
		SmTrace.lg(String.format("setyCurrent %s Loc x: %.2f (%.2f, %.2f) y: %.2f (%.2f, %.2f)", title,
				getxCurrent(),  xMin, xMax,
				getyCurrent(), yMin, yMax), "graphic");
	}
	
	/***
	@Override
	public Dimension getPreferredSize()
	{
	    return new Dimension(120, 120);
	}
	***/
	@Override
	public void mouseDragged(MouseEvent e) {
		int x = xyRegionGetX(e);
		int y = xyRegionGetY(e);
		SmTrace.lg(String.format("mouseDragged at(%d, %d)", x,  y), "mouse");
		setAtMouseXY(x, y);
		em2DLocationNotify();
		paintIt();
	}

	/**
	 * Clip values to region limits
	 * @param e
	 */
	public int xyRegionGetX(MouseEvent e) {
		int x = e.getX();
		if (x < 0)
			return 0;
		int width = xyRegionPanel.getWidth();
		if (x > width)
			return width;
		return x;
	}

	/**
	 * Clip values to region limits
	 * @param e
	 */
	public int xyRegionGetY(MouseEvent e) {
		int y = e.getY();
		if (y < 0)
			return 0;
		int height = xyRegionPanel.getHeight();
		if (y > height)
			return height;
		return y;
	}

	
	
	/**
	 * Kluge to circumvent the problem of not having paintComponent called
	 */
	public void paintIt() {
		Graphics gl = xyRegionPanel.getGraphics();
		paintComponent(gl);
	}
	
	public void setAtMouseX(int ival) {
		float xsize = xyRegionPanel.getWidth();
		float xval = (float)ival * (xMax-xMin)/(float)xsize + xMin;
		setxCurrent(xval);
		
	}

	public void setAtMouseXY(int x, int y) {
		SmTrace.lg(String.format("mouse at(%d, %d)", x,  y), "mouse");
		setAtMouseX(x);
		setAtMouseY(y);
		SmTrace.lg(String.format("mouse %s Loc x: %.2f (%.2f, %.2f) y: %.2f (%.2f, %.2f)", title,
				getxCurrent(),  xMin, xMax,
				getyCurrent(), yMin, yMax), "mouse");
	}

	
	public void setAtMouseY(int ival) {
		float ysize = xyRegionPanel.getHeight();
		float yval = (float)ival * (yMax-yMin)/(float)ysize + yMin;
		setyCurrent(yval);
	}
	
	
	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseMoved at(%d, %d)", x,  y), "mouse");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = xyRegionGetX(e);
		int y = xyRegionGetY(e);
		SmTrace.lg(String.format("mouseClicked at(%d, %d)", x,  y), "mouse");
		setAtMouseXY(x,y);
		em2DLocationNotify();
		paintIt();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mousePressed at(%d, %d)", x,  y), "mouse");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseReleased at(%d, %d)", x,  y), "mouse");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseEntered at(%d, %d)", x,  y), "mouse");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseExited at(%d, %d)", x,  y), "mouse");
	}

	@Override
	public void paintComponent(Graphics gl) {
		//frame.paintComponent(gl);
		super.paintComponent(gl);
		SmTrace.lg("paintComponent", "paint");
	    int width = getWidth();
	    int height = getHeight();
	    gl.setColor(Color.BLACK);
	    int x = xValToInt(getxCurrent());
	    int y = yValToInt(getyCurrent());
	    int cdelta = 5;					// Cross size
	    gl.drawLine(x-cdelta, y, x+cdelta, y);
	    gl.drawLine(x, y-cdelta, x, y+cdelta);
	}
	
	public int xValToInt(float xval) {
		if (xMax == xMin)
			return 0;
		
		float xsize = getWidth();
		int ival = (int)(xsize * (xval-xMin)/(xMax-xMin));
		return ival;
	}
	
	public int yValToInt(float yval) {
		if (yMax == yMin)
			return 0;
		
		float ysize = getHeight();
		int ival = (int)(ysize * (yval-yMin)/(yMax-yMin));
		return ival;
	}
	
	
	/**
	 * Selftest program
	 */
	
	public static void main(String[] args) {
		SmTrace.setLogName("EM2DDrag");
		SmTrace.lg("Begin Test");
		mainArgs = args;
		mainArgsLength = args.length;
		String test_name = "basic";
		for (mainArgIndex = 0; mainArgIndex < args.length; ) {
			String arg = args[mainArgIndex++];
			if (arg.startsWith("--")) {
				String str_val = "";
				boolean boolean_val = false;
				String opt = arg.substring(2).toLowerCase();
				switch (opt) {
					case "test":
					case "t":
						test_name = strArg("1");
						break;
						
					case "trace":
						str_val = strArg("ALL");
						SmTrace.setFlags(str_val);
						break;
						
					default:
						SmTrace.lg(String.format("Unrecognized flag(%s)", opt));
						break;
				}
			} else {
				SmTrace.lg(String.format("Unrecognized arg(%s) - quitting", arg));
				System.exit(1);
			}
					
		}
		SmTrace.lg(String.format("test: %s",  test_name));
		if (test_name.equals("basic") || test_name.equals("1")) {
			setupSelfTest("basic test");
		} else if (test_name.equals("2")) {
			setup2Test("3D testing with 2D controls");
		} else {
			SmTrace.lg(String.format("Unrecognized test name %s - quitting",  test_name));
		}
	}

	
	/**
	 * Get next arg if exists, or default
	 * @param def_str - default value
	 */
	private static String strArg(String def_str) {
		if (mainArgIndex >= mainArgsLength)
			return def_str;		// No more args
		if (mainArgs[mainArgIndex].startsWith("--"))
			return def_str;		// Next is an option name
		String val_str = mainArgs[mainArgIndex++];
		return val_str;
	}

	
	/**
	 * Setup EM2DDrag test for execution
	 * Waits for setup to complete so we can use it for testing
	 */
	public static EM2DDrag setupSelfTest(String title) {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					JFrame frame = new JFrame(title);
					frame.setLayout(new BorderLayout());
					frame.setMinimumSize(new Dimension(500, 500));

					EM2DDrag em = new EM2DDrag(
							"x-y ctl",
							"xy",
							"x-values", 0f,10f,
							"y-values", 0f, 10f);
					frame.add(em, BorderLayout.CENTER);
					setupDone = true;
					frame.setVisible(true);
					em.createUI(title,  frame);
				}
			}
		);
		int inctime = 1000;				// milliseconds
		int maxtime = 10000;			// Max wait in milliseconds
		///maxtime = 1000000;				/// lengthen for debugging
		int dur = 0;					// Current duration
		while (!setupDone) {
			try {
				Thread.sleep(inctime);
			} catch (InterruptedException e) {
				SmTrace.lg("setupModeler interupt exception");
				e.printStackTrace();
			}
			dur += inctime;
			if (dur > maxtime) {
				SmTrace.lg(String.format("Wait time(%d) exceeded, time=%d", maxtime, dur));
				break;
			}
		}
		SmTrace.lg("Completed Setup");

		return em_base;
	}



	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI(String title, JFrame frame) {
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			SmTrace.lg(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			SmTrace.lg(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

	}
	
	/**
	 * Setup EM2DDrag with two coordinated instances x-y, x-z
	 * Waits for setup to complete so we can use it for testing
	 */
	public static EM3DPosition setup2Test(String title) {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					JFrame frame = new JFrame(title);
					frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
					
					EM3DPosition emp = new EM3DPosition(title, frame);
					if (emp != null)
						em3Dbase = emp;
						setupDone = true;
				}
			}
		);
		int inctime = 1000;				// milliseconds
		int maxtime = 10000;			// Max wait in milliseconds
		///maxtime = 1000000;				/// lengthen for debugging
		int dur = 0;					// Current duration
		while (!setupDone) {
			try {
				Thread.sleep(inctime);
			} catch (InterruptedException e) {
				SmTrace.lg("setupModeler interupt exception");
				e.printStackTrace();
			}
			dur += inctime;
			if (dur > maxtime) {
				SmTrace.lg(String.format("Setup wait time(%d) exceeded, time=%d", maxtime, dur));
				System.exit(1);
			}
		}
		SmTrace.lg("Completed Setup");
		JFrame tframe = new JFrame("test pod frame");
		EM3DPosition em3Dtest = new EM3DPosition("test pod", null);
		em3Dbase.addEM3DEventListener(em3Dtest);
		
		return em3Dbase;
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
	}
	
	public static void delay(float time) {
		long ticsize = 100;
		long ntic = (long) (time * 1000);
		long endtic = ntic;
		long tic = 0;
		while (tic < endtic) {
			try {
				Thread.sleep(ticsize);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tic += ticsize;
		}
	}
	  public void display(){
		SmTrace.lg("display");
	    repaint();
	  }
	
	public void addEM2DEventListener(EM3DPosition emp) {
		this.emPosition = emp;
	}
	
	public void em2DLocationNotify() {
		EM2DLocationEvent evt = new EM2DLocationEvent(emType,
			xCurrent, yCurrent);	
		if (emPosition != null)
			emPosition.location2DEvent(evt);
	}

	
	/**
	 * Update graphics baset on external stimuli
	 * @param xNew
	 * @param yNew
	 */
	public void updateLocation(float xNew, float yNew) {
		setxCurrent(xNew);
		setyCurrent(yNew);
		paintIt();
	}
}
