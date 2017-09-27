
public class OurBlockError extends Exception {

	public OurBlockError(String msg) {
		System.out.println(String.format("OurBlockError: %s", msg));
	}
}
