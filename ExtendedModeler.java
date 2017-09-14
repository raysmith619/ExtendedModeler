/**
 * ExtendedModeler - Adapted from SimpleModeller-3D_JavaApplication-JOGL
   Still trying to work with GitHub.
 */

import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;

import com.jogamp.opengl.GLCapabilities;

import smTrace.SmTrace;		// Execution Trace support








public class ExtendedModeler implements ActionListener {

	static final String applicationName = "Extended Modeler";
	public static SmTrace smTrace;
	JFrame frame;
	Container toolPanel;
	SceneViewer sceneViewer;

	public enum AutoAddType {	// Auto add block type
		NONE,					// none added
		PLACEMENT_COMPUTE,		// Block placement
		PLACEMENT_DRAG,
		PLACEMENT_PUSH,
		PLACEMENT_ROTATE,
		DUPLICATE,
		BOX,
		BALL,
		CONE,
		CYLINDER
	}
	
	JMenuItem deleteAllMenuItem, quitMenuItem, aboutMenuItem;
	
	JMenuItem undoMenuItem;			// Command undo
	JMenuItem redoMenuItem;			// Command redo
	JMenuItem repeatMenuItem;		// Command repeat
	JButton undoButton;				// Command undo
	JButton redoButton;				// Command redo
	JButton repeatButton;			// Command repeat
	
	JMenuItem placementMenuItem;
	JMenuItem placementComputeMenuItem;
	JMenuItem placementDragMenuItem;
	JMenuItem placementPushMenuItem;
	JMenuItem placementRotateMenuItem;

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
	JCheckBox displayAddControlCheckBox;
	JCheckBox displayPlacementControlCheckBox;
	JCheckBox displayColorControlCheckBox;
	JCheckBox displayWorldAxesCheckBox;
	JCheckBox displayCameraTargetCheckBox;
	JCheckBox displayBoundingBoxCheckBox;
	JCheckBox enableCompositingCheckBox;

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == undoButton || source == undoMenuItem) {
			sceneViewer.cmdUndo();
			return;
		}
		else if (source == redoButton || source == redoMenuItem) {
			sceneViewer.cmdRedo();
			return;
		}
		else if (source == repeatButton || source == repeatMenuItem) {
			sceneViewer.cmdRepeat();
			return;
		}
		if ( source == deleteAllMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really delete all?",
				"Confirm Delete All",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				BlockCommand bcmd;
				try {
					bcmd = new BlkCmdAdd("deleteAllMenuItem");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				sceneViewer.deleteAll(bcmd);
				bcmd.doCmd();
			}
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
		else if ( source == deleteSelectionButton ) {
			BlockCommand bcmd;
			try {
				bcmd = new BlkCmdAdd("deleteSelectionButton");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			sceneViewer.deleteSelection(bcmd);
			bcmd.doCmd();
		}
		else if ( source == lookAtSelectionButton ) {
			sceneViewer.lookAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == resetCameraButton ) {
			sceneViewer.resetCamera();
			sceneViewer.repaint();
		}
		else if ( source == displayAddControlCheckBox ) {
			sceneViewer.displayAddControl = ! sceneViewer.displayAddControl;
			sceneViewer.setControl("addControl", sceneViewer.displayAddControl);
			sceneViewer.repaint();
		}
		else if ( source == displayPlacementControlCheckBox ) {
			sceneViewer.displayPlacementControl = ! sceneViewer.displayPlacementControl;
			sceneViewer.setControl("placementControl", sceneViewer.displayPlacementControl);
			sceneViewer.repaint();
		}
		else if ( source == displayColorControlCheckBox ) {
			sceneViewer.displayColorControl = ! sceneViewer.displayColorControl;
			sceneViewer.setControl("colorControl", sceneViewer.displayColorControl);
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
		
			menu = new JMenu("CMD");
				undoMenuItem = new JMenuItem("Undo");
				undoMenuItem.addActionListener(this);
				menu.add(undoMenuItem);
				redoMenuItem = new JMenuItem("Redo");
				redoMenuItem.addActionListener(this);
				menu.add(redoMenuItem);
				repeatMenuItem = new JMenuItem("Repeat");
				repeatMenuItem.addActionListener(this);
				menu.add(repeatMenuItem);
			menuBar.add(menu);
	
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
			
			menuBar.add(new JSeparator());
			undoButton = new JButton("Undo");
			undoButton.addActionListener(this);
			menuBar.add(undoButton);
			
			redoButton = new JButton("Redo");
			redoButton.addActionListener(this);
			menuBar.add(redoButton);
			
			repeatButton = new JButton("Repeat");
			repeatButton.addActionListener(this);
			menuBar.add(repeatButton);
					
					
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
		sceneViewer = new SceneViewer(caps, this, frame, smTrace);

		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		pane.add( toolPanel, BorderLayout.LINE_START );
		pane.add( sceneViewer, BorderLayout.CENTER );


		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		lookAtSelectionButton.addActionListener(this);
		toolPanel.add( lookAtSelectionButton );

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		resetCameraButton.addActionListener(this);
		toolPanel.add( resetCameraButton );

		displayAddControlCheckBox = new JCheckBox("Display Add Control", sceneViewer.displayAddControl);
		displayAddControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayAddControlCheckBox.addActionListener(this);
		toolPanel.add(displayAddControlCheckBox);

		displayPlacementControlCheckBox = new JCheckBox("Display Placement Control", sceneViewer.displayPlacementControl);
		displayPlacementControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayPlacementControlCheckBox.addActionListener(this);
		toolPanel.add(displayPlacementControlCheckBox);

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

	/**
	 * Set/Clear check box
	 * and associated indicator variable
	 * 
	 */
	public void setCheckBox(String name, boolean checked) {
		if (SmTrace.tr("checkbox"))
			System.out.println(String.format("setCheckBox(%s, %b)", name, checked));
		if (SmTrace.tr("select")) {
			sceneViewer.selectPrint(String.format("modeler.setCheckBox(%s): selected;", name));
		}
		switch (name) {
			case "addControl":
				displayAddControlCheckBox.setSelected(checked);
				sceneViewer.displayAddControl = checked;
				break;
				
			case "placementControl":
				displayPlacementControlCheckBox.setSelected(checked);
				sceneViewer.displayPlacementControl = checked;
				break;
				
			case "colorControl":
				displayColorControlCheckBox.setSelected(checked);
				sceneViewer.displayColorControl = checked;
				break;
			
			default:
				System.out.println(String.format("Unrecognized button name: %s - ignored", name));
		}
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

