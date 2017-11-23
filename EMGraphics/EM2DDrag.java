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

import com.jogamp.opengl.GLCapabilities;

import smTrace.SmTrace;

public class EM2DDrag  extends JPanel implements MouseListener, MouseMotionListener{
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
	int yLabelWidth = 100;		// Left (y) label width
	int xValueSize = 200;		// x plotting surface (min)
	int yValueSize = 200;		// y plotting surface (min)
	static EM2DDrag em_base;	// flag indicating complete
	JPanel xyRegionPanel;		// xy plotting field
	JTextField xCurrentField;	// current xvalue field 
	JTextField yCurrentField;	// current yvalue field
	public EM2DDrag(
		JFrame frame,
		String xLabel, float xMin, float xMax,
		String yLabel, float yMin, float yMax) {
		this.frame = frame;
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
		frame.setLayout(new BorderLayout());
		JPanel xyField = new JPanel(new BorderLayout());
		xyField.setBackground(Color.gray);  
		frame.setSize(400, 400);
		xyField.setSize(300, 400);
		//GridBagConstraints gbc = new GridBagConstraints();
		//gbc.fill = GridBagConstraints.BOTH;
		//gbc.weightx = 1.0;
		//gbc.weighty = 0;

		//frame.getContentPane().add(xyField);
		frame.add(xyField);
		
		JPanel xrow_panel = new JPanel(new BorderLayout());			// blank, x-labels
		xrow_panel.setBackground(Color.yellow);
		JPanel ulcorner = new JPanel(new BorderLayout());
		ulcorner.add(new JTextField(String.format("%26s", "")));	// kluge to make ulcorner take some space
		ulcorner.setBackground(Color.white);
		ulcorner.setMinimumSize(new Dimension(xLabelHeight, yLabelWidth));
		ulcorner.setSize(new Dimension(xLabelHeight, yLabelWidth));
		xrow_panel.add(ulcorner, BorderLayout.WEST);			// Upper left corner - blank
		JPanel xLabelingPanel = new JPanel(new BorderLayout());  // values/label
		///xLabelingPanel.setMinimumSize(new Dimension(xValueSize, xLabelHeight));
		xrow_panel.add(xLabelingPanel, BorderLayout.CENTER);

		
		xyField.add(xrow_panel, BorderLayout.NORTH);
		JPanel yrow_xy_region_panel = new JPanel(new BorderLayout());	// y-labels, x-y region
		///yrow_xy_region_panel.setSize(100, 200);
		yrow_xy_region_panel.setBackground(Color.GREEN);
		xyField.add(yrow_xy_region_panel, BorderLayout.CENTER);
		
		

		JPanel xValuesPanel = new JPanel(new BorderLayout());
		JPanel xLabelPanel = new JPanel(new BorderLayout());
		JTextField xLabelField = new JTextField(xLabel);
		xLabelField.setHorizontalAlignment(JTextField.CENTER);

		JTextField xMinField = new JTextField(String.format(iffmt, xMin));
		JPanel xCurrentPanel = new JPanel(new BorderLayout());
		xCurrentField = new JTextField(String.format(iffmt, xCurrent));
		xCurrentField.setHorizontalAlignment(JTextField.CENTER);
		xCurrentPanel.add(xCurrentField, BorderLayout.CENTER);
		JTextField xMaxField = new JTextField(String.format(iffmt, xMax));
		xValuesPanel.add(xMinField, BorderLayout.WEST);
		///xValuesPanel.add(new JPanel());	// spacer
		xValuesPanel.add(xCurrentPanel, BorderLayout.CENTER);
		///xValuesPanel.add(new JPanel());	// spacer
		xValuesPanel.add(xMaxField, BorderLayout.EAST);
		xLabelingPanel.add(xValuesPanel, BorderLayout.NORTH);
		xLabelingPanel.add(xLabelPanel, BorderLayout.SOUTH);
		xLabelPanel.add(xLabelField, BorderLayout.CENTER);
		xLabelingPanel.add(xLabelPanel, BorderLayout.CENTER);


		JPanel yLabelingPanel = new JPanel(new BorderLayout());
		yLabelingPanel.setBackground(Color.WHITE);
		yrow_xy_region_panel.add(yLabelingPanel, BorderLayout.WEST);
		xyRegionPanel = new JPanel(new BorderLayout());
		xyRegionPanel.setMinimumSize(new Dimension(xValueSize, yValueSize));

		xyRegionPanel.setBorder(new BevelBorder(1));
		yrow_xy_region_panel.add(xyRegionPanel);
		//xyRegionPanel.setSize(200,200);
		
		JLabel yLabelField = new JLabel(yLabel);
		JPanel yValuesPanel = new JPanel(new BorderLayout());
		yLabelingPanel.add(yValuesPanel, BorderLayout.WEST);
		yLabelingPanel.add(yLabelField, BorderLayout.CENTER);
		yLabelField.setBackground(Color.WHITE);
		
		JTextField yMinField = new JTextField(String.format(iffmt, yMin));
		yCurrentField = new JTextField(String.format(iffmt, yCurrent));
		JTextField yMaxField = new JTextField(String.format(iffmt, yMax));

		yValuesPanel.add(yMinField, BorderLayout.NORTH);
		//yValuesPanel.add(new JPanel());	// spacer
		yValuesPanel.add(yCurrentField, BorderLayout.CENTER);
		//yValuesPanel.add(new JPanel());	// spacer
		yValuesPanel.add(yMaxField, BorderLayout.SOUTH);

		setVisible(true);
		addMouseListener(this);
		frame.addMouseListener(this);
		xyRegionPanel.addMouseListener(this);
		xyRegionPanel.addMouseMotionListener(this);
		
		///xyRegionPanel.addMouseMotionListener(new EMMouseMotionListener());
		frame.setVisible(true);
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
	}

	public float getyCurrent() {
		return yCurrent;
	}

	public void setyCurrent(float yCurrent) {
		this.yCurrent = yCurrent;
		yCurrentField.setText(String.format("%.2g", this.yCurrent)); 
	}
	@Override
	public Dimension getPreferredSize()
	{
	    return new Dimension(120, 120);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		int x = xyRegionGetX(e);
		int y = xyRegionGetY(e);
		SmTrace.lg(String.format("mouseDragged at(%d, %d)", x,  y));
		setAtMouseXY(x, y);
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
		SmTrace.lg(String.format("mouse at(%d, %d)", x,  y));
		setAtMouseX(x);
		setAtMouseY(y);
		SmTrace.lg(String.format("Loc x: %.2f (%.2f, %.2f) y: %.2f (%.2f, %.2f)",
				getxCurrent(),  xMin, xMax,
				getyCurrent(), yMin, yMax));
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
		SmTrace.lg(String.format("mouseClicked at(%d, %d)", x,  y));
		setAtMouseXY(x,y);
		paintIt();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mousePressed at(%d, %d)", x,  y));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseReleased at(%d, %d)", x,  y));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseEntered at(%d, %d)", x,  y));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		SmTrace.lg(String.format("mouseExited at(%d, %d)", x,  y));
	}

	@Override
	public void paintComponent(Graphics gl) {
		//frame.paintComponent(gl);
		super.paintComponent(gl);
		SmTrace.lg("paintComponent");
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
	 * @return 
	 */
	
	public static void main(String[] args) {
		SmTrace.lg("Begin Test");
		setupSelfTest("EM2DDrag Test");
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
					EM2DDrag em = new EM2DDrag(
							frame,
							"x-values", 0f,10f,
							"y-values", 0f, 10f);
					em.createUI(title);
					em_base = em;		// 
				}
			}
		);
		int inctime = 1000;				// milliseconds
		int maxtime = 10000;			// Max wait in milliseconds
		///maxtime = 1000000;				/// lengthen for debugging
		int dur = 0;					// Current duration
		while (em_base == null) {
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
	private void createUI(String title) {
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			SmTrace.lg(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}
		frame = new JFrame(title);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		
		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

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
}
