package ExtendedModeler;
import smTrace.SmTrace;

public class EMBlockError extends Exception {

	public EMBlockError(String msg) {
		super(String.format("EMBlockError: %s", msg));
	}
}
