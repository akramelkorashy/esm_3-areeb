public class ReservationStation {
	MemoryWord instruction;
	boolean free, flag1, flag2;

	// ROBEntry robEntry;
	public ReservationStation(MemoryWord instruction, boolean free,
			boolean flag1, boolean flag2) {
		this.instruction = instruction;
		this.free = free;
		this.flag1 = flag1;
		this.flag2 = flag2;
	}
	
}
