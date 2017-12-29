package ExtendedModeler;
import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

class SceneDraw {
	private SceneControler sceneControler;
	private Scene scene;
	private GLAutoDrawable drawable;
	
	public SceneDraw(SceneControler sceneControler, GLAutoDrawable drawable) {
		this.sceneControler = sceneControler;
		this.scene = sceneControler.getScene();
		this.drawable = drawable;
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
		block.draw(drawable, expand, drawAsWireframe, cornersOnly);
	}


	public void drawScene(
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		GLAutoDrawable drawable = this.drawable;
		
		GL2 gl = (GL2) drawable.getGL();
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		for (int id : scene.displayedBlocks.getIds()) {
			EMBlock cb = scene.displayedBlocks.getBlock(id);
			if (cb == null) {
				SmTrace.lg(String.format("drawScene: No display block for id:%d",id));
				continue;
			}
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

	public void drawBoundingBoxOfScene(GLAutoDrawable drawable) {
		AlignedBox3D box = scene.getBoundingBoxOfScene();
		if ( ! box.isEmpty() )
			ColoredBox.drawBox(drawable, box, false, true, false );
	}
}
