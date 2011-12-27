public class ReservationStation {
	MemoryWord instruction;

	// readiness of the two operands
	boolean busy, ready[];

	short val[];

	// The ROB entries that provide the values of the two operands for the
	// instruction.
	ROBEntry entry[];

	// The ROB entry that this instruction will write to.
	ROBEntry robEntry;

	public ReservationStation() {
		ready = new boolean[3];
		val = new short[3];
		entry = new ROBEntry[3];
		// A destination needs no flags, so just set it to ready for
		// instructions that involve no 3rd data source
		ready[2] = instruction.isFirstOperandDestination;
	}
	/*
	 * public ReservationStation(MemoryWord instruction, boolean free, boolean
	 * ready1, boolean ready2, ROBEntry rob) { this.instruction = instruction;
	 * this.free = free; this.ready1 = ready1; this.ready2 = ready2; robEntry =
	 * rob; }
	 */

}
