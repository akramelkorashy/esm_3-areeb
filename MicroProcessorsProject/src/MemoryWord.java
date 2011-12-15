public class MemoryWord {
	InsType type;
	short destination;
	short source1;
	short source2;
	short value;

	public MemoryWord(String instruction) {
		String[] ar = instruction.split(" ");
		if (ar.length == 4) {
			if (ar[0].equals("LW")) {
				type = InsType.LD;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
			} else if (ar[0].equals("SW")) {
				type = InsType.STR;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
			} else if (ar[0].equals("BEQ")) {
				type = InsType.BEQ;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
			} else if (ar[0].equals("ADD")) {
				type = InsType.ADD;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
			} else if (ar[0].equals("ADDI")) {
				type = InsType.ADDI;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = Short.parseShort(ar[3]);
			} else if (ar[0].equals("NAND")) {
				type = InsType.NAND;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
			} else if (ar[0].equals("MUL")) {
				type = InsType.MUL;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
			} else if (ar[0].equals("DIV")) {
				type = InsType.DIV;
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = getRegNumb(ar[3]);
			}
		} else if (ar.length == 3) {
			if (ar[0].trim().equals("JALR")) {
				destination = getRegNumb(ar[1]);
				source1 = getRegNumb(ar[2]);
				source2 = -1;
			} else if (ar[0].trim().equals("JMP")) {
				destination = getRegNumb(ar[1]);
				source1 = Short.parseShort(ar[2]);
				source2 = -1;
			}
		} else if (ar.length == 2) {
			type = InsType.RET;
			destination = getRegNumb(ar[1]);
			source1 = -1;
			source2 = -1;
		}
	}

	public MemoryWord(short value) {
		type = InsType.DATA;
		this.value = value;
	}

	private short getRegNumb(String regName) {
		return (short) (regName.trim().charAt(1) - '0');
	}
}
