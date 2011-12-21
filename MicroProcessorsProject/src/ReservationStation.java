public class ReservationStation {
	MemoryWord instruction;
	boolean free, ready[];
    short val[];
    ROBEntry entry[];
	ROBEntry robEntry;
	public ReservationStation () {
		ready = new boolean[2];
		val = new short[2];
		entry = new ROBEntry[2];
	}
	/*
	public ReservationStation(MemoryWord instruction, boolean free,
			boolean ready1, boolean ready2, ROBEntry rob) {
		this.instruction = instruction;
		this.free = free;
		this.ready1 = ready1;
		this.ready2 = ready2;
		robEntry = rob;
	}
	*/

}
