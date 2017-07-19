/**
 * ExtendedModeler - Adapted from SimpleModeller-3D_JavaApplication-JOGL
   Still trying to work with GitHub.
 */
import java.lang.Math;
import java.util.Vector;

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.BoxLayout;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.GLCanvas;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLAutoDrawable;
// import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;		// Execution Trace support








public class ExtendedModeler implements ActionListener {

	static final String applicationName = "Extended Modeler";
	public static SmTrace smTrace;
	JFrame frame;
	Container toolPanel;
	SceneViewer sceneViewer;
	
	public enum AutoAddType {	// Auto add block type
		NONE,					// none added
		POSITION_COMPUTE,		// Block positioning
		POSITION_DRAG,
		POSITION_PUSH,
		POSITION_ROTATE,
		DUPLICATE,
		BOX,
		BALL,
		CONE,
		CYLINDER
	}
	
	JMenuItem deleteAllMenuItem, quitMenuItem, aboutMenuItem;
	
	JMenuItem positionMenuItem;
	JMenuItem positionComputeMenuItem;
	JMenuItem positionDragMenuItem;
	JMenuItem positionPushMenuItem;
	JMenuItem positionRotateMenuItem;

	JMenuItem colorMenuItem;
	JMenuItem colorComputeMenuItem;

	JMenuItem duplicateMenuItem;
	JMenuItem boxMenuItem;
	JMenuItem ballMenuItem;
	JMenuItem coneMenuItem;
	JMenuItem cylinderMenuItem;
	JMenuItem cancelAddMenuItem;
	JButton createBoxButton;
	JButton createBallButton;
	JButton createConeButton;
	JButton createCylinderButton;
	JButton deleteSelectionButton;
	JButton lookAtSelectionButton;
	JButton resetCameraButton;
	JCheckBox displayPositionControlCheckBox;
	JCheckBox displayColorControlCheckBox;
	JCheckBox displayWorldAxesCheckBox;
	JCheckBox displayCameraTargetCheckBox;
	JCheckBox displayBoundingBoxCheckBox;
	JCheckBox enableCompositingCheckBox;

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ( source == deleteAllMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really delete all?",
				"Confirm Delete All",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				sceneViewer.deleteAll();
				sceneViewer.repaint();
			}
		}
		else if (source == positionComputeMenuItem) {
			sceneViewer.positionControlSetup();			
		}
		else if (source == colorComputeMenuItem) {
			sceneViewer.colorControlSetup();			
		}
		else if ( source == duplicateMenuItem ) {
			System.out.println(String.format("chose %s", "duplicate"));
			sceneViewer.setAutoAdd(AutoAddType.DUPLICATE);
		}
		else if ( source == cancelAddMenuItem ) {
			sceneViewer.setAutoAdd(AutoAddType.NONE);
		}
		else if ( source == boxMenuItem ) {
			sceneViewer.setAutoAdd(AutoAddType.BOX);
		}
		else if ( source == ballMenuItem ) {
			sceneViewer.setAutoAdd(AutoAddType.BALL);
		}
		else if ( source == coneMenuItem ) {
			sceneViewer.setAutoAdd(AutoAddType.CONE);
		}
		else if ( source == cylinderMenuItem ) {
			sceneViewer.setAutoAdd(AutoAddType.CYLINDER);
		}
		else if ( source == quitMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really quit?",
				"Confirm Quit",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
		else if ( source == aboutMenuItem ) {
			JOptionPane.showMessageDialog(
				frame,
				"'" + applicationName + "' Sample Program\n"
					+ "Original version written April-May 2008",
				"About",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if ( source == createBoxButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Box button"));
			sceneViewer.createNewBlock("box");
			sceneViewer.repaint();
		}
		else if ( source == createBallButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Ball button"));
			sceneViewer.createNewBlock("ball");
			sceneViewer.repaint();
		}
		else if ( source == createConeButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Cone button"));
			sceneViewer.createNewBlock("cone");
			sceneViewer.repaint();
		}
		else if ( source == createCylinderButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Cylinder button"));
			sceneViewer.createNewBlock("cylinder");
			sceneViewer.repaint();
		}
		else if ( source == deleteSelectionButton ) {
			sceneViewer.deleteSelection();
			sceneViewer.repaint();
		}
		else if ( source == lookAtSelectionButton ) {
			sceneViewer.lookAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == resetCameraButton ) {
			sceneViewer.resetCamera();
			sceneViewer.repaint();
		}
		else if ( source == displayPositionControlCheckBox ) {
			sceneViewer.displayPositionControl = ! sceneViewer.displayPositionControl;
			sceneViewer.repaint();
		}
		else if ( source == displayColorControlCheckBox ) {
			sceneViewer.displayColorControl = ! sceneViewer.displayColorControl;
			sceneViewer.repaint();
		}
		else if ( source == displayWorldAxesCheckBox ) {
			sceneViewer.displayWorldAxes = ! sceneViewer.displayWorldAxes;
			sceneViewer.repaint();
		}
		else if ( source == displayCameraTargetCheckBox ) {
			sceneViewer.displayCameraTarget = ! sceneViewer.displayCameraTarget;
			sceneViewer.repaint();
		}
		else if ( source == displayBoundingBoxCheckBox ) {
			sceneViewer.displayBoundingBox = ! sceneViewer.displayBoundingBox;
			sceneViewer.repaint();
		}
		else if ( source == enableCompositingCheckBox ) {
			sceneViewer.enableCompositing = ! sceneViewer.enableCompositing;
			sceneViewer.repaint();
		}
	}


	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI() {
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			System.out.println(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
				deleteAllMenuItem = new JMenuItem("Delete All");
				deleteAllMenuItem.addActionListener(this);
				menu.add(deleteAllMenuItem);

				menu.addSeparator();

				quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(this);
				menu.add(quitMenuItem);
			menuBar.add(menu);
			menu = new JMenu("AddBlock");
				duplicateMenuItem = new JMenuItem("Duplicate");
				duplicateMenuItem.addActionListener(this);
				menu.add(duplicateMenuItem);
				
				menu.addSeparator();
				
				boxMenuItem = new JMenuItem("Box");
				boxMenuItem.addActionListener(this);
				menu.add(boxMenuItem);
				ballMenuItem = new JMenuItem("Ball");
				ballMenuItem.addActionListener(this);
				menu.add(ballMenuItem);
				coneMenuItem = new JMenuItem("Cone");
				coneMenuItem.addActionListener(this);
				menu.add(coneMenuItem);
				cylinderMenuItem = new JMenuItem("Cylinder");
				cylinderMenuItem.addActionListener(this);
				menu.add(cylinderMenuItem);
			menuBar.add(menu);

			menu = new JMenu("colorBlock");
			colorComputeMenuItem = new JMenuItem("Computed");
			colorComputeMenuItem.addActionListener(this);
			menu.add(colorComputeMenuItem);
			menuBar.add(menu);

			menu = new JMenu("positionBlock");
			positionComputeMenuItem = new JMenuItem("Computed");
			positionComputeMenuItem.addActionListener(this);
			menu.add(positionComputeMenuItem);
			positionDragMenuItem = new JMenuItem("Drag");
			positionDragMenuItem.addActionListener(this);
			menu.add(positionDragMenuItem);
			positionPushMenuItem = new JMenuItem("Push");
			positionPushMenuItem.addActionListener(this);
			menu.add(positionPushMenuItem);
			positionRotateMenuItem = new JMenuItem("Rotate");
			positionRotateMenuItem.addActionListener(this);
			menu.add(positionRotateMenuItem);
			menuBar.add(menu);
		
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
		frame.setJMenuBar(menuBar);

		toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		sceneViewer = new SceneViewer(caps, frame, this.smTrace);

		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		pane.add( toolPanel, BorderLayout.LINE_START );
		pane.add( sceneViewer, BorderLayout.CENTER );

		createBoxButton = new JButton("Create Box");
		createBoxButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createBoxButton.addActionListener(this);
		toolPanel.add( createBoxButton );

		createBallButton = new JButton("Create Ball");
		createBallButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createBallButton.addActionListener(this);
		toolPanel.add( createBallButton );

		createConeButton = new JButton("Create Cone");
		createConeButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createConeButton.addActionListener(this);
		toolPanel.add( createConeButton );

		createCylinderButton = new JButton("Create Cylindar");
		createCylinderButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createCylinderButton.addActionListener(this);
		toolPanel.add( createCylinderButton );

		deleteSelectionButton = new JButton("Delete Selection");
		deleteSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		deleteSelectionButton.addActionListener(this);
		toolPanel.add( deleteSelectionButton );

		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		lookAtSelectionButton.addActionListener(this);
		toolPanel.add( lookAtSelectionButton );

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		resetCameraButton.addActionListener(this);
		toolPanel.add( resetCameraButton );

		displayPositionControlCheckBox = new JCheckBox("Display Position Control", sceneViewer.displayPositionControl);
		displayPositionControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayPositionControlCheckBox.addActionListener(this);
		toolPanel.add(displayPositionControlCheckBox);

		displayColorControlCheckBox = new JCheckBox("Display Color Control", sceneViewer.displayColorControl);
		displayColorControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayColorControlCheckBox.addActionListener(this);
		toolPanel.add(displayColorControlCheckBox);

		displayWorldAxesCheckBox = new JCheckBox("Display World Axes", sceneViewer.displayWorldAxes );
		displayWorldAxesCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayWorldAxesCheckBox.addActionListener(this);
		toolPanel.add( displayWorldAxesCheckBox );

		displayCameraTargetCheckBox = new JCheckBox("Display Camera Target", sceneViewer.displayCameraTarget );
		displayCameraTargetCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayCameraTargetCheckBox.addActionListener(this);
		toolPanel.add( displayCameraTargetCheckBox );

		displayBoundingBoxCheckBox = new JCheckBox("Display Bounding Box", sceneViewer.displayBoundingBox );
		displayBoundingBoxCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayBoundingBoxCheckBox.addActionListener(this);
		toolPanel.add( displayBoundingBoxCheckBox );

		enableCompositingCheckBox = new JCheckBox("Enable Compositing", sceneViewer.enableCompositing );
		enableCompositingCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		enableCompositingCheckBox.addActionListener(this);
		toolPanel.add( enableCompositingCheckBox );

		frame.pack();
		frame.setVisible( true );
	}
	
	public static void main( String[] args ) {
		SmTrace.setFlags(args[0]);		// Use first arg
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					ExtendedModeler sp = new ExtendedModeler();
					sp.createUI();
				}
			}
		);
	}
}
