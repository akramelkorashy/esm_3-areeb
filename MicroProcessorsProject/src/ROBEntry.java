public class ROBEntry {

	InsType type;
	// if (inProgress) then the data is still invalid and the ROBEntry
	// dependency will be updated once execution finishes
	boolean inProgress;
	int logicalRegister;
	short data;

	public ROBEntry(InsType type, int logicalRegister) {
		this.type = type;
		this.logicalRegister = logicalRegister;
		inProgress = true;
	}

	public void updateData(short data) {
		this.data = data;
		inProgress = false;
	}
}
