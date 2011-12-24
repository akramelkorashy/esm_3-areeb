import java.util.Arrays;

public class MemoryWord implements Comparable<MemoryWord> {
	InsType type;
	short destination;
	short source1;
	short source2;
	short instType;
	short source1Val;
	short source2Val;
	short value;
	boolean flagForSource2 = true;
	int[] cycles;
	ROBEntry entry;
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
				flagForSource2 = false;
				instType = 0;
			} else if (ar[0].equals("SW")) {
				type = InsType.SW;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				flagForSource2 = false;
				instType = 0;
			} else if (ar[0].equals("BEQ")) {
				type = InsType.BEQ;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				flagForSource2 = false;
				instType = 0;
			} else if (ar[0].equals("ADD")) {
				type = InsType.ADD;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				instType = 0;
			} else if (ar[0].equals("ADDI")) {
				type = InsType.ADDI;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
				System.out.println(ar[2]);
				flagForSource2 = false;
				instType = 0;
			} else if (ar[0].equals("NAND")) {
				type = InsType.NAND;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				instType = 0;
			} else if (ar[0].equals("MUL")) {
				type = InsType.MUL;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				instType = 1;
			} else if (ar[0].equals("DIV")) {
				type = InsType.DIV;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
				instType = 2;
			}
		} else if (ar.length == 3) {
			if (ar[0].trim().equals("JALR")) {
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = -1;
				flagForSource2 = false;
				instType = 0;
			} else if (ar[0].trim().equals("JMP")) {
				destination = getRegNumb(ar[1]);
				source1 = Short.parseShort(ar[2]);
				source2 = -1;
				flagForSource2 = false;
				instType = 0;
			}
		} else if (ar.length == 2) {
			type = InsType.RET;
			destination = getRegNumb(ar[1]);
			source1 = -1;
			instType = 0;
			source2 = -1;
			flagForSource2 = false;
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
