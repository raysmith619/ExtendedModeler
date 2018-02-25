package ExtendedModeler;
import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

class SceneDraw {
	private SceneViewer sceneViewer;
	
	public SceneDraw(SceneViewer sceneViewer) {
		this.sceneViewer = sceneViewer;
	}

	/**
	 * Reset to initial (repeatable) state
	 * @throws EMBlockError 
	 */
	public  void reset() {
	}


	public void drawBlock(
		EMBlock block,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GLAutoDrawable drawable = sceneViewer.getCanvas();
		
		block.draw(drawable, expand, drawAsWireframe, cornersOnly);
	}

	    /*
	     * Initialize z-buffer, projection matrix, light source, and lighting model.
	     * Do not specify a material property here.
	     * From material init
	     */
	public void setLighting(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //
        float ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        float diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float position[] = { 0.0f, 3.0f, 2.0f, 0.0f };
        float lmodel_ambient[] = { 0.4f, 0.4f, 0.4f, 1.0f };
        float local_view[] = { 0.0f };

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local_view, 0);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);

        gl.glClearColor(0.0f, 0.1f, 0.1f, 0.0f);
	}

	public void drawScene(
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		GLAutoDrawable drawable = sceneViewer.getCanvas();
		setLighting(drawable);
		GL2 gl = (GL2) drawable.getGL();
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		Scene scene = sceneViewer.getScene();
		for (int id : scene.displayedBlocks.getIds()) {
			EMBlock cb = scene.displayedBlocks.getBlock(id);
			if (cb == null) {
				SmTrace.lg(String.format("drawScene: No display block for id:%d",id));
				continue;
			}
			if (getViewerLevel() < cb.getViewerLevel())
				continue;				// Skip display for this block
			
			if ( useAlphaBlending )
				gl.glColor4f(cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha());
			else
				gl.glColor3f(cb.getRed(), cb.getGreen(), cb.getBlue());
				///gl.glColor3f(1, 1, 1);
			drawBlock( cb, false, false, false );
		}
		if ( useAlphaBlending ) {
			gl.glDisable( GL.GL_BLEND );
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		for ( int id : scene.displayedBlocks.getIds()) {
			EMBlock cb = scene.displayedBlocks.getBlock(id);
			if (cb == null) {
				SmTrace.lg(String.format("drawScene: No display block for id:%d", id));
				continue;
			}
			if (getViewerLevel() < cb.getViewerLevel())
				continue;				// Skip display for this block
			if (true)
				if (cb.blockType().equals("ball")) {
					SmTrace.lg(String.format("ball at[%d]", id), "tracing ball");
					if (cb.isSelected()) {
						SmTrace.lg("tracing ball selected", "ball");
					} else {
						SmTrace.lg("tracing ball not selected", "ball");
					}
				}
			if ( cb.isSelected() && indexOfHilitedBox == id )
				gl.glColor3f( 1, 1, 0 );
			else if ( cb.isSelected() )
				gl.glColor3f( 1, 0, 0 );
			else if ( indexOfHilitedBox == id )
				gl.glColor3f( 0, 1, 0 );
			else continue;
			drawBlock(cb, true, true, true );
		}
	}

	private int getViewerLevel() {
		return sceneViewer.getViewerLevel();
	}

	public void drawLocalView(GLAutoDrawable drawable) {
		ColoredBox box = sceneViewer.localViewer.getViewBox();
		if ( box != null )
			ColoredBox.drawBox(drawable, box, false, true, false );
	}

	public void drawBoundingBoxOfScene(GLAutoDrawable drawable) {
		ColoredBox box = sceneViewer.getScene().getBoundingBoxOfScene();
		if (box != null)
			ColoredBox.drawBox(drawable, box, false, true, false );
	}
}
