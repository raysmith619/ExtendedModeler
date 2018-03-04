package EMGraphics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLCapabilities;

import ExtendedModeler.ControlOfView;
import ExtendedModeler.Point3D;
import smTrace.SmTrace;

public class EM3DPosition extends JDialog  implements EM3DLocationListner, EM2DLocationListner {
	JFrame frame;
	String title;
	float xCurrent;
	float yCurrent;
	float zCurrent;
	
	EM2DDrag emXY;		// x-y pad
	EM2DDrag emXZ;		// x-z pad
	EM3DPosition em3DPosition;
	ControlOfView controlOfView;			// control if/notification requested
	
	static class dflt {
		static String xLabel = "x-value";
		static int width = 400;				// Widget width in pixels
		static int height = 800;			// Widget height in pixels
		static float xMin = -5;
		static float xMax = 5;
		static String yLabel = "y-value";
		static float yMin = xMin;
		static float yMax = xMax;
		static String zLabel = "z-value";
		static float zMin = xMin;
		static float zMax = xMax;
	}
	
	/**
	 * 
	 * @param title
	 * @param frame - null - no frame, just holder for event processing
	 */
	public EM3DPosition(String title, JFrame frame) {
		this(title, frame,
			dflt.width,
			dflt.height,
			dflt.xLabel, dflt.xMin, dflt.xMax,
			dflt.yLabel, dflt.yMin, dflt.yMax,
			dflt.zLabel, dflt.zMin, dflt.zMax
			);
			
	}
	
	public EM3DPosition(String title,
		JFrame frame,
		int width,
		int height,
		String xLabel, float xMin, float xMax,
		String yLabel, float yMin, float yMax,
		String zLabel, float zMin, float zMax
		) {
		this.title = title;
		if (frame == null)
			return;
		
		frame.setSize(width, height);
		frame.setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(width, height));
		JPanel topFrame = new JPanel(new BorderLayout());
		topFrame.setPreferredSize(new Dimension(width, height/2));
		///topFrame.setSize(500, 500);
		JPanel bottomFrame = new JPanel(new BorderLayout());
		bottomFrame.setPreferredSize(new Dimension(width, height/2));
		///bottomFrame.setSize(500, 500);
		frame.add(topFrame, BorderLayout.NORTH);
		frame.add(bottomFrame, BorderLayout.CENTER);
		
		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				Component emc = e.getComponent();
				int height = emc.getHeight();
				int width = emc.getWidth();
				int cheight = height/2 - 2;
				SmTrace.lg(String.format("resized new: height=%3d width=%3d cheight=%3d",
										height, width, cheight));
				emXY.setSize(width, cheight);
				emXZ.setSize(width,  cheight);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
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
			
		});
		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			SmTrace.lg(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		emXY = new EM2DDrag(
				"x-y ctl",
				"xy",
				xLabel, xMin, xMax,
				yLabel, yMin, yMax);

		emXY.setSize(width, height/2);
		emXY.setPreferredSize(new Dimension(width, height/2));
		emXY.addEM2DEventListener(this);
		topFrame.add(emXY, BorderLayout.CENTER);
		
		emXZ = new EM2DDrag(
				"x-z ctl",
				"xz",
				xLabel, xMin, xMax,
				zLabel, zMin, zMax);
		///em_base = emXZ;
		emXZ.setSize(width, height/2);
		emXZ.addEM2DEventListener(this);
		bottomFrame.add(emXZ, BorderLayout.CENTER);
	}

	@Override
	public void location3DEvent(EM3DLocationEvent e) {
		float x = e.getX();
		float y = e.getY();
		float z = e.getZ();

		SmTrace.lg(String.format("location3DEvent: %s x=%.2g y=%.2g z=%.2g",
									title, x, y, z));
	}
	
	
	public void addEM3DEventListener(EM3DPosition em3Dp) {
		this.em3DPosition = em3Dp;
	}
	
	public void em3DLocationNotify() {
		EM3DLocationEvent evt = new EM3DLocationEvent(
			xCurrent, yCurrent, zCurrent);	
		if (em3DPosition != null)
			em3DPosition.location3DEvent(evt);
		if (controlOfView != null)
			controlOfView.location3DEvent(evt);
	}

	public void location2DEvent(EM2DLocationEvent evt) {
		String evt_type = evt.getType();
		float x = evt.getX();
		float y = evt.getY();
		if (evt_type.equals("xy")) {
			emXZ.setxCurrent(x);
			xCurrent = x;
			yCurrent = y;
			emXZ.paintIt();
			SmTrace.lg(String.format("locationEvent: xy - updating new x in xz"));
		}
		if (evt_type.equals("xz")) {
			emXY.setxCurrent(x);
			xCurrent = x;
			zCurrent = y;
			emXY.paintIt();
			SmTrace.lg(String.format("locationEvent: xz - updating new x in xy"));
		}
		em3DLocationNotify();
	}

	public void addEM3DEventListener(ControlOfView controlOfView) {
		this.controlOfView = controlOfView;		
	}

	public void updatePoint(Point3D pt) {
		xCurrent = pt.x();
		yCurrent = pt.y();
		zCurrent = pt.z();
		emXY.updateLocation(xCurrent, yCurrent);
		emXZ.updateLocation(xCurrent, zCurrent);
	}
}
