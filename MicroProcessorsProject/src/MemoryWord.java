import java.util.Arrays;

public class MemoryWord implements Comparable<MemoryWord> {
	InsType type;
	short destination;
	short source1;
	short source2;
	short functionalUnitType;
	short source1Val;
	short source2Val;

	// used to hold the value when the MemoryWord is of type DATA or to hold the
	// value of the 1st token of the instruction if it is not a destination
	// (like the Store and BEQ instructions, etc..)
	short value;

	// to differentiate between a register and an immediate value
	boolean source2IsRegister = true;

	// to differentiate between using the register in the first token as source
	// and as a destination
	boolean isFirstOperandDestination = true;

	// The instance of time (in cycles) when the instruction finished each
	// pipeline stage
	int[] cycles;

	ROBEntry entry;

	// The instruction as taken from the input; parsing is done and all
	// attributes are set accordingly.
	String inst;

	public String toString() {
		return inst;
	}

	public MemoryWord(String instruction) {
		inst = instruction;
		cycles = new int[6];
		Arrays.fill(cycles, Integer.MAX_VALUE);
		String[] ar = instruction.split(" ");
		if (ar.length == 4) {
			if (ar[0].equals("LW")) {
				type = InsType.LD;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				source2IsRegister = false;
				functionalUnitType = 0;
			} else if (ar[0].equals("SW")) {
				type = InsType.SW;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				source2IsRegister = false;
				isFirstOperandDestination = false;
				functionalUnitType = 0;
			} else if (ar[0].equals("BEQ")) {
				type = InsType.BEQ;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				source2IsRegister = false;
				isFirstOperandDestination = false;
				functionalUnitType = 0;
			} else if (ar[0].equals("ADD")) {
				type = InsType.ADD;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				functionalUnitType = 0;
			} else if (ar[0].equals("ADDI")) {
				type = InsType.ADDI;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				System.out.println(ar[2]);
				source2IsRegister = false;
				functionalUnitType = 0;
			} else if (ar[0].equals("NAND")) {
				type = InsType.NAND;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				functionalUnitType = 0;
			} else if (ar[0].equals("MUL")) {
				type = InsType.MUL;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				functionalUnitType = 1;
			} else if (ar[0].equals("DIV")) {
				type = InsType.DIV;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				functionalUnitType = 2;
			}
		} else if (ar.length == 3) {
			if (ar[0].trim().equals("JALR")) {
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = -1;
				source2IsRegister = false;
				functionalUnitType = 0;
			} else if (ar[0].trim().equals("JMP")) {
				// "JMP regA, Imm" branches to PC + 1 + regA + Imm
				// For simplicity, use the regA in source1 and Imm in source2
				// instead of putting regA in the destination value and handling
				// that source1 is empty
				source1 = getRegNumb(ar[1]);
				source2 = Short.parseShort(ar[2]);
				destination = -1;
				source2IsRegister = false;
				functionalUnitType = 0;
			}
		} else if (ar.length == 2) {
			type = InsType.RET;
			destination = getRegNumb(ar[1]);
			source1 = -1;
			functionalUnitType = 0;
			source2 = -1;
			source2IsRegister = false;
		}
	}

	public MemoryWord(short value) {
		type = InsType.DATA;
		this.value = value;
	}

	private short getRegNumb(String regName) {
		return (short) (regName.trim().charAt(1) - '0');
	}

	@Override
	public int compareTo(MemoryWord o) {
		return cycles[4] - o.cycles[4];
	}
}
