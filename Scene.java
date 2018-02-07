package ExtendedModeler;
import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

class Scene {
	EMBlockGroup genBlocks;						// Generated
	EMBlockGroup displayedBlocks;					// Displayed
	ColoredBox boundingBoxOfScene = new ColoredBox();
	boolean isBoundingBoxOfSceneDirty = false;




	public Scene() {
		reset();
	}

	/**
	 * Reset to initial (repeatable) state
	 * @throws EMBlockError 
	 */
	public  void reset() {
		genBlocks = new EMBlockGroup();			// Generated
		EMBlock.setGenerated(genBlocks);
		try {
			EMBlock.setDefaults("box",
					new EMBox3D(new Point3D(0,0,0), EMBox3D.RADIUS),
					new Color(255, 0, 255));
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		displayedBlocks = new EMBlockGroup();		// Displayed
		
	}
	
	
	public ColoredBox getBoundingBoxOfScene() {
		if ( isBoundingBoxOfSceneDirty ) {
			for ( int id : displayedBlocks.getIds()) {
				EMBlock block = displayedBlocks.getBlock(id);
				boundingBoxOfScene.bound(block.boundingBox());
			}
			isBoundingBoxOfSceneDirty = false;
		}
		return boundingBoxOfScene;
	}


	/**
	 * Add block from generated to displayed scene
	 */
	public void insertBlock(int id) {
		EMBlock cb = genBlocks.getBlock(id);
		if (cb != null) {
			displayedBlocks.putBlock(cb);
			isBoundingBoxOfSceneDirty = true;
		}
	}


	/**
	 * Add block to displayed scene
	 * May be modified versions of blocks in genBlocks
	 * May replace element of the same id 
	 */
	public void insertBlock(EMBlock cb) {
		if (cb != null) {
			if (genBlocks.getBlock(cb.iD()) == null) {
				// Shouldn't really happen
				SmTrace.lg(String.format("Adding new block %s id=%d", cb, cb.iD()));
				genBlocks.putBlock(cb);
			}
			displayedBlocks.putBlock(cb);
			isBoundingBoxOfSceneDirty = true;
		}
	}


	/**
	 * Add blocks to displayed list
	 * May be modified versions of blocks in genBlocks
	 * May replace element of the same id 
	 */
	public void insertBlocks(EMBlockGroup blocks) {
		for (int  id : blocks.getIds()) {
			insertBlock(blocks.getBlock(id));
		}
	}

	/**
	 * Add already created block to scene
	 * @return index of new element
	 */
	/***
	public int  addBlock(
		EMBCommand bcmd,
		EMBlock cb) {
		addBlock(cb.iD);
		bcmd.addBlock(cb);
		return cb.iD;
	}
***/	
	
	public int addBlock(
		EMBCommand bcmd,		// Current command
		String blockType, 		// Block type
		EMBox3D box,		// Bounding box
		Color color
	) {
		EMBlock cb = EMBlock.newBlock(blockType, box, color);		
		if (cb == null || !cb.isOk()) {
			SmTrace.lg(String.format("Couldn't create block type '%s'",
					blockType));
			return 0;
		}
		genBlocks.putBlock(cb);		// Add to generated blocks
		addBlock(bcmd, cb.iD());			// Add to displayed blocks
		return cb.iD();
	}

	public int addColoredBox(
		EMBCommand bcmd,
		EMBox3D box,
		Color color
	) {
		EMBlock cb = EMBlock.newBlock("box", box, color);
		return addBlock(bcmd, cb.iD());
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

		for (int id : displayedBlocks.getIds())  {
			EMBlock block = displayedBlocks.getBlock(id);
			if (block == null) {
				SmTrace.lg(String.format("getIndexOfIntersectedBox: No display block id:%d", id));
				continue;
			}

			if (block.intersects(ray,candidatePoint,candidateNormal)) {
				///TFD - second try to facilitate tracking
				if (candidateNormal.lengthSquared() == 0) {
					SmTrace.lg(String.format("Scene.getIndexOfIntersectedBox: Zero length normal(%s) at intersection:%s ray:%s",
							candidateNormal, candidatePoint, ray), "intersection");
					Point3D candidatePoint2 = new Point3D();
					Vector3D candidateNormal2 = new Vector3D();
					block.intersects(ray,candidatePoint2,candidateNormal2);
					candidateNormal = new Vector3D(0,0,1);
					SmTrace.lg(String.format("Scene.getIndexOfIntersectedBox: FORCED candidateNormal(%s)", candidateNormal), "intersection");
				}
				candidateDistance = Point3D.diff(
					ray.origin, candidatePoint
				).length();
				if (
					! intersectionDetected
					|| candidateDistance < distanceToIntersection
				) {
					// We've found a new, best candidate
					intersectionDetected = true;
					indexOfIntersectedBox = id;
					distanceToIntersection = candidateDistance;
					intersectionPoint.copy( candidatePoint );
					normalAtIntersection.copy( candidateNormal );
					
				}
			}
		}
		if (indexOfIntersectedBox > 0)
			SmTrace.lg(String.format("Scene.getIndexOfIntersectedBox: %d", indexOfIntersectedBox));
		return indexOfIntersectedBox;
	}


	public EMBlock getBlock( int id ) {
		return genBlocks.getBlock(id);
	}

	public EMBox3D getBox( int index ) {
		EMBlock cb = getBlock(index);
		if (cb == null)
			return null;
		return cb.getBox();
	}

	
	/**
	 * Get generated blocks' ids
	 */
	public int[] getDisplayedIds() {
		return displayedBlocks.getIds();
	}

	public boolean getSelectionStateOfBox( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return false;
		
		return cb.isSelected();
	}
	
	public void setSelectionStateOfBox( int id, boolean state ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.setSelected(state);
	}
	
	
	public void toggleSelectionStateOfBox( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.toggleSelected();
	}

	public void setColorOfBlock( int id, float r, float g, float b ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null)
			cb.setColor(r,g,b);
	}

	public void translateBlock( int id, Vector3D translation ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.translate(translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void resizeBlock(
		int id, int indexOfCornerToResize, Vector3D translation
	) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.resize(indexOfCornerToResize, translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			displayedBlocks.removeBlock(id);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock(BlockSelect selected) {
		ArrayList<Integer> index_list = selected.getList();
		for (Integer si : index_list) {
			deleteBlock(si.intValue());
		}
	}
	
	/**
	 * Get newest block index
	 * -1 if none
	 */
	public int cbIndex() {
		return displayedBlocks.getNewestId();
	}

	
	/**
	 * Get block, given index
	 */
	public EMBlock cb(int id) {
		return genBlocks.getBlock(id);
	}


	public void deleteAllBlocks() {
		displayedBlocks.removeAllBlocks();
		isBoundingBoxOfSceneDirty = true;
	}

	public int addBlock(EMBCommand bcmd, int id) {
		return bcmd.addBlock(id);
		
	}
}
