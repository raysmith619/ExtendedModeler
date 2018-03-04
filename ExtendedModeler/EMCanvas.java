package ExtendedModeler;

import java.awt.Adjustable;
import java.awt.Canvas;
import java.awt.event.AdjustmentEvent;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

import smTrace.SmTrace;

public class EMCanvas extends GLCanvas {

	public EMCanvas(GLCapabilities caps) {
		super(caps);
	}
	
	
	public void scrollBarAdjustmentEvent(AdjustmentEvent evt) {
		Adjustable source = evt.getAdjustable();
		if (evt.getValueIsAdjusting()) {
			SmTrace.lg("Is Adjusting");
		}
		int orient = source.getOrientation();
		if (orient == Adjustable.HORIZONTAL) {
			SmTrace.lg("from horizontal scrollbar"); 
		} else {
			SmTrace.lg("from vertical scrollbar");
		}
		int type = evt.getAdjustmentType();
		switch (type) {
			case AdjustmentEvent.UNIT_INCREMENT:
				SmTrace.lg("Scrollbar was increased by one unit");
				break;
			case AdjustmentEvent.UNIT_DECREMENT:
				SmTrace.lg("Scrollbar was decreased by one unit");
				break;
			case AdjustmentEvent.BLOCK_INCREMENT:
				SmTrace.lg("Scrollbar was increased by one block");
				break;
			case AdjustmentEvent.BLOCK_DECREMENT:
				SmTrace.lg("Scrollbar was decreased by one block");
				break;
			case AdjustmentEvent.TRACK:
				SmTrace.lg("The knob on the scrollbar was dragged");
				break;
	    }
	    int value = evt.getValue();
	    SmTrace.lg(String.format("Scroll value=%d", value));
	}
			
}
