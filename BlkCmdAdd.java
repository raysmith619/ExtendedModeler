

public class BlkCmdAdd extends EMBCommand {
	
	public BlkCmdAdd(String action) throws EMBlockError {
		super(action);
	}

	public BlkCmdAdd(EMBCommand base_cmd) throws Exception {
		super(base_cmd);
	}
}
