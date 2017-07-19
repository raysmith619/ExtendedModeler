import java.awt.Color;
import java.util.Vector;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import smTrace.SmTrace;

class Scene {
	public Vector< OurBlock > ourBlocks = new Vector< OurBlock >();

	AlignedBox3D boundingBoxOfScene = new AlignedBox3D();
	boolean isBoundingBoxOfSceneDirty = false;




	public Scene() {
	}

	public AlignedBox3D getBoundingBoxOfScene() {
		if ( isBoundingBoxOfSceneDirty ) {
			boundingBoxOfScene.clear();
			for ( int i = 0; i < ourBlocks.size(); ++i ) {
				OurBlock block = (OurBlock)ourBlocks.elementAt(i).block;
				boundingBoxOfScene.bound(block.boundingBox());
			}
			isBoundingBoxOfSceneDirty = false;
		}
		return boundingBoxOfScene;
	}

	/**
	 * Add already created block to scene
	 * @return index of new element
	 */
	public int  addBlock(
		OurBlock cb) {
		ourBlocks.addElement(cb);
		isBoundingBoxOfSceneDirty = true;
		return ourBlocks.size()-1;
	}
	
	
	public int addBlock(
		String blockType, 		// Block type
		AlignedBox3D box,		// Bounding box
		Color color
	) {
		OurBlock cb = OurBlock.getNewBlock(blockType, box, color);
		
		if (cb == null || !cb.isOk()) {
			System.out.println(String.format("Couldn't create block type '%s'",
					blockType));
			return 0;
		}
		return addBlock(cb);
	}

	public int addColoredBox(
		AlignedBox3D box,
		Color color
	) {
		OurBlock cb = new ColoredBox(box, color);
		return addBlock(cb);
	}

	public int getIndexOfIntersectedBox(
		Ray3D ray, // input
		Point3D intersectionPoint, // output
		Vector3D normalAtIntersection // output
	) {
		boolean intersectionDetected = false;
		int indexOfIntersectedBox = -1;
		float distanceToIntersection = 0;

		// candidate intersection
		Point3D candidatePoint = new Point3D();
		Vector3D candidateNormal = new Vector3D();
		float candidateDistance;

		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock block = ourBlocks.elementAt(i);
			if (block.intersects(ray,candidatePoint,candidateNormal)) {
				candidateDistance = Point3D.diff(
					ray.origin, candidatePoint
				).length();
				if (
					! intersectionDetected
					|| candidateDistance < distanceToIntersection
				) {
					// We've found a new, best candidate
					intersectionDetected = true;
					indexOfIntersectedBox = i;
					distanceToIntersection = candidateDistance;
					intersectionPoint.copy( candidatePoint );
					normalAtIntersection.copy( candidateNormal );
				}
			}
		}
		return indexOfIntersectedBox;
	}

	public AlignedBox3D getBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() )
			return ourBlocks.elementAt(index).getBox();
		return null;
	}

	public boolean getSelectionStateOfBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() )
			return ourBlocks.elementAt(index).isSelected();
		return false;
	}
	public void setSelectionStateOfBox( int index, boolean state ) {
		if ( 0 <= index && index < ourBlocks.size() )
			ourBlocks.elementAt(index).setSelected(state);
	}
	public void toggleSelectionStateOfBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.toggleSelected();
		}
	}

	public void setColorOfBlock( int index, float r, float g, float b ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.setColor(r,g,b);
		}
	}

	public void translateBlock( int index, Vector3D translation ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.translate(translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void resizeBlock(
		int indexOfBlock, int indexOfCornerToResize, Vector3D translation
	) {
		if ( 0 <= indexOfBlock && indexOfBlock < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(indexOfBlock);
			cb.resize(indexOfCornerToResize, translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock( int index ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			ourBlocks.removeElementAt( index );
			isBoundingBoxOfSceneDirty = true;
		}
	}


	public void deleteAllBlocks() {
		ourBlocks.removeAllElements();
		isBoundingBoxOfSceneDirty = true;
	}


	static public void drawBlock(
		GL2 gl,
		OurBlock block,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		block.draw(gl, expand, drawAsWireframe, cornersOnly);
	}


	public void drawScene(
		GL2 gl,
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock cb = ourBlocks.elementAt(i);
			if ( useAlphaBlending )
				gl.glColor4f(cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha());
			else
				gl.glColor3f(cb.getRed(), cb.getGreen(), cb.getBlue());
			drawBlock( gl, cb, false, false, false );
		}
		if ( useAlphaBlending ) {
			gl.glDisable( GL.GL_BLEND );
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock cb = ourBlocks.elementAt(i);
			if (SmTrace.tr("ball"))
				if (cb.blockType().equals("ball")) {
					System.out.println(String.format("ball at[%d]", i));
					if (cb.isSelected()) {
						System.out.println("ball selected");
					} else {
						System.out.println("ball not selected");
					}
				}
			if ( cb.isSelected() && indexOfHilitedBox == i )
				gl.glColor3f( 1, 1, 0 );
			else if ( cb.isSelected() )
				gl.glColor3f( 1, 0, 0 );
			else if ( indexOfHilitedBox == i )
				gl.glColor3f( 0, 1, 0 );
			else continue;
			drawBlock( gl, cb, true, true, true );
		}
	}

	@SuppressWarnings("serial")
	public void drawBoundingBoxOfScene( GL2 gl ) {
		AlignedBox3D box = getBoundingBoxOfScene();
		if ( ! box.isEmpty() )
			ColoredBox.drawBox( gl, box, false, true, false );
	}
}
