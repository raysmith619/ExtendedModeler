package EMGraphics;

public class EM2DLocationEvent {
	String type;
	float x;
	float y;

	/**
	 * @param type
	 * @param x
	 * @param y
	 */
	public EM2DLocationEvent(String type, float x, float y) {
		super();
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}
