import java.awt.Color;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.jogamp.opengl.GLCapabilities;

import smTrace.SmTrace;

public class EMColorChooser extends JColorChooser {
	JColorChooser colorChooser;
	
	public EMColorChooser(Color firstColor,
			Point location,
			ControlOfColor ctlcol) {
        colorChooser = new JColorChooser();
        if (firstColor != null)
        	colorChooser.setColor(firstColor);
        if (location != null)
        	colorChooser.setLocation(location);
        boolean model = false;		// false - modeless
        JDialog dialog = JColorChooser.createDialog(null, "Color Chooser",
                 model, colorChooser, null, null);
        dialog.setVisible(true);
        
        colorChooser.getSelectionModel().addChangeListener(ctlcol);
        colorChooser.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("resized test");
				ctlcol.updateMapLocation(e);		// Kluge to update position
				
			}
        	@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("moved");
				ctlcol.updateMapLocation(e);
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
	 * Self test
	 * @param args
	 */
	public static void main(String[] args) {
		SmTrace smTrace = new SmTrace();
		ExtendedModeler em = new ExtendedModeler();
		SceneViewer sceneViewer = null;
		JFrame frame;
		SmTrace.setProps("EMColorChooser");
		String applicationName = "EMColorChooser Self Test";
		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		
		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		try {
			sceneViewer = new SceneViewer(caps, em, frame, smTrace);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("SceneViewer error %s", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
		}
		ControlOfColor cc = new ControlOfColor(sceneViewer, "testing");
		Color color = Color.BLUE;
		
		Point location = new Point(400,400);
		Point map_location = cc.getMapLocation();
		if (map_location.x >= 0)
			location = map_location;
		EMColorChooser emc = new EMColorChooser(color, location, cc);

	}

}
