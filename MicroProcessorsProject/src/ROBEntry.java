public class ROBEntry {

	InsType type;
	// if (inProgress) then the data is still invalid and the ROBEntry
	// dependency will be updated once execution finishes
	boolean inProgress;
	short logicalRegister;
	short data;
	MemoryWord instruction;

	public ROBEntry(InsType type, MemoryWord inst) {
		this.type = type;
		this.logicalRegister = inst.destination;
		inProgress = true;
		instruction = inst;
	}

	public void updateData(short data) {
		this.data = data;
		inProgress = false;
	}
}
