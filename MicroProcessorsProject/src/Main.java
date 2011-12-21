import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Main {
	static BufferedReader br;
	static Cache[] DataCaches;
	static Cache[] InstCaches;
	static MemoryWord[] RAM;
	static LinkedList<MemoryWord> instructionBuffer;
	static LinkedList<ROBEntry> rob;
	static int ramSize, ramAccessTime, pipeLineWidth, instructionBufferSize,
			robSize;
	static ReservationStation[] stations;
	static PriorityQueue<MemoryWord>[] functionalUnits;
	static int[] functionalUnitsLatencies;
	static int[] functionalUnitsNums;
	static int cycle = 0;
	static int PC;
	static short[] registers = new short[8];
	static int fetchNow = 0;
	static HashMap<Short, ROBEntry> registerMap = new HashMap<Short, ROBEntry>();

	public static int[] readCacheInputs() throws NumberFormatException,
			IOException {
		int[] res = new int[5];
		System.out.println("Please Enter The Geometry of the cache");
		System.out.print("Cache Size In KB: ");
		res[0] = Integer.parseInt(br.readLine());
		System.out.println();
		System.out.print("Line Size In KB: ");
		res[1] = Integer.parseInt(br.readLine());
		System.out.println();
		System.out.print("Cache Associativity : ");
		res[2] = Integer.parseInt(br.readLine());
		System.out.println();
		System.out.println("\nNow you need to specify the write policy!!");
		System.out
				.println("Type 1 for Write_Back and 2 for Write_Through cache");
		res[3] = Integer.parseInt(br.readLine());
		System.out
				.println("Type 1 for Write_Allocate and 2 for Write_Around cache");
		res[4] = Integer.parseInt(br.readLine());
		System.out
				.println("Pleae Enter the number of cycles needed to access the cache");
		res[5] = Integer.parseInt(br.readLine());
		return res;
	}

	public static void initCaches() throws NumberFormatException, IOException {
		System.out.println("Please enter the number of Data Caches levels");
		int numDataCaches = Integer.parseInt(br.readLine());
		DataCaches = new Cache[numDataCaches];
		System.out
				.println("Please enter the number of Instruction Caches levels");
		int numInstrCaches = Integer.parseInt(br.readLine());
		InstCaches = new Cache[numInstrCaches];
		for (int i = 0; i < numDataCaches; i++) {
			System.out.println("Enter Level " + (i + 1)
					+ " Data Cache specification ");
			DataCaches[i] = new Cache(readCacheInputs());
		}
		for (int i = 0; i < numInstrCaches; i++) {
			System.out.println("Enter Level " + (i + 1)
					+ " Instruction Cache specification ");
			InstCaches[i] = new Cache(readCacheInputs());
		}
	}

	public static void readInput() throws NumberFormatException, IOException {
		System.out.println("Welcome To MIAO Simulator :) ");
		initCaches();
		System.out.println("Enter The Memory Capacity!!");
		ramSize = Integer.parseInt(br.readLine());
		RAM = new MemoryWord[ramSize];
		System.out.println("Please Enter The memory access time");
		ramAccessTime = Integer.parseInt(br.readLine());
		System.out.println("Please enter the pipeline width");
		pipeLineWidth = Integer.parseInt(br.readLine());
		System.out.println("Please enter the size of the instruction buffer");
		instructionBufferSize = Integer.parseInt(br.readLine());
		System.out.println("Please enter the number of reservation stations");
		stations = new ReservationStation[Integer.parseInt(br.readLine())];
		for (int i = 0; i < stations.length; i++) {
			stations[i] = new ReservationStation();
		}
		System.out.println("Please enter the number of ROB entries.");
		robSize = Integer.parseInt(br.readLine());
		functionalUnitsLatencies = new int[3];
		System.out
				.println("Please enter the number of add/subtract Funtional Units");
		functionalUnitsNums[0] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the latency of add/subtract Funtional Unit");
		functionalUnitsLatencies[0] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the number of multiply Funtional Units");
		functionalUnitsNums[1] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the latency of multiply Funtional Unit");
		functionalUnitsLatencies[1] = Integer.parseInt(br.readLine());

		System.out.println("Please enter the number of divide Funtional Units");
		functionalUnitsNums[2] = Integer.parseInt(br.readLine());
		System.out.println("Please enter the latency of divide Funtional Unit");
		functionalUnitsLatencies[2] = Integer.parseInt(br.readLine());
	}

	public static void readInstructions() throws NumberFormatException,
			IOException {
		int startAddress = Integer.parseInt(br.readLine());
		PC = startAddress;
		String inst = br.readLine();
		while (inst != null) {
			RAM[startAddress++] = new MemoryWord(inst);
			inst = br.readLine();
		}
	}

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		br = new BufferedReader(new InputStreamReader(System.in));
		readInput();
		System.setIn(new FileInputStream(new File("Input.in")));
		readInstructions();
		int k = 0;
		while (true) {
			fetch();
			decode();
			dispatch();
			Issue();
			execute();
			Commmit();
			cycle++;
		}
	}

	private static String checkRegisterMap(int src) {
		if (!registerMap.containsKey(src))
			return registers[src] + "";
		ROBEntry entry = registerMap.get(src);
		if (!entry.inProgress)
			return entry.data + "";
		return null;
	}

	private static void Commmit() {
		int committed = 0;
		while (true)
			if (rob.size() > 0 && rob.peek().instruction.cycles[5] < cycle
					&& rob.peek().inProgress == false
					&& committed < pipeLineWidth) {
				committed++;
				registerMap.remove(rob.remove().logicalRegister);
			} else {
				break;
			}
	}

	private static void execute() {
		int doneEx = 0;
		for (int i = 0; i < 2; i++) {
			if (functionalUnits[i].size() == 0) {
				i++;
				continue;
			}
			MemoryWord instruction = functionalUnits[i].peek();
			if (doneEx == pipeLineWidth)
				break;
			if (instruction.cycles[4] > cycle) {
				i++;
				continue;
			}
			functionalUnits[i].remove();
			short result = calculate(instruction.source1Val,
					instruction.source2Val, instruction.type);
			instruction.entry.inProgress = false;
			instruction.entry.data = result;
			instruction.cycles[5] = cycle;
			for (int j = 0; j < stations.length; j++) {
				if (stations[j].free == false) {
					for (int k = 0; k < 2; k++)
						if (!stations[j].ready[k]
								&& stations[j].entry[k]
										.equals(instruction.entry)) {
							stations[j].ready[k] = true;
							stations[j].val[k] = result;
						}
				}
			}
		}
	}

	private static short calculate(short op1, short op2, InsType type) {
		switch (type) {
		case ADD:
		case ADDI:
			return (short) (op1 + op2);
		case NAND:
			return (short) (op1 & (~op2));
		case MUL:
			return (short) (op1 * op2);
		case DIV:
			return (short) (op1 / op2);
		default:
			return 0;
		}
	}

	private static void Issue() {
		int issued = 0;
		for (int i = 0; i < stations.length; i++) {
			if (issued == pipeLineWidth)
				break;
			if (stations[i].free == false
					&& stations[i].instruction.cycles[2] < cycle
					&& stations[i].ready[0] == true
					&& stations[i].ready[1] == true) {
				int type = stations[i].instruction.instType;
				if (functionalUnits[type].size() < functionalUnitsNums[type]) {
					issued++;
					MemoryWord current = stations[i].instruction;
					current.cycles[3] = cycle;
					current.source1Val = stations[i].val[0];
					current.source2Val = stations[i].val[1];
					current.cycles[4] = cycle + functionalUnitsLatencies[type];
					functionalUnits[type].add(current);
					stations[i].free = true;
				}
			}
		}
	}

	private static void dispatch() {
		for (int i = 0; i < stations.length; i++) {
			if (stations[i].free == false
					&& stations[i].instruction.cycles[1] < cycle
					&& stations[i].instruction.cycles[2] == -1) {
				stations[i].instruction.cycles[2] = cycle;
				MemoryWord instruction = stations[i].instruction;
				short src[] = { instruction.source1, instruction.source2 };
				for (int j = 0; j < 2; j++) {
					if (j == 1 && !instruction.flagForSource2) {
						stations[i].ready[j] = true;
						stations[i].val[j] = src[j];
					}
					String val = checkRegisterMap(src[j]);
					if (val != null) {
						stations[i].ready[j] = true;
						stations[i].val[j] = Short.parseShort(val);
					} else {
						stations[i].ready[j] = false;
						stations[i].entry[j] = registerMap.get(src[j]);
					}
				}
				registerMap.put(instruction.destination, stations[i].robEntry);
			}
		}
	}

	private static void decode() {
		if (instructionBuffer.getFirst().cycles[0] >= cycle)
			return;
		int num = pipeLineWidth;
		while (instructionBuffer.size() > 0
				&& instructionBuffer.getFirst().cycles[0] < cycle && num > 0) {
			num--;
			if (robSize == rob.size()) {
				for (int i = 0; i < stations.length; i++) {
					if (stations[i].free) {
						stations[i].free = false;
						MemoryWord instruction = instructionBuffer.remove();
						instruction.cycles[1] = cycle;
						stations[i].instruction = instruction;
						ROBEntry entry = new ROBEntry(instruction.type,
								instruction);
						entry.inProgress = true;
						instruction.entry = entry;
						rob.add(entry);
						stations[i].robEntry = entry;
						break;
					}
				}
			}
		}
	}

	private static void fetch() {
		if (fetchNow == 0 && instructionBuffer.size() < instructionBufferSize) {
			int time = 0;
			int missedCaches = 0;
			MemoryWord[] instructions = null;
			int displacement = 0;
			for (int i = 0; i < InstCaches.length; i++) {
				time += InstCaches[i].latency;
				if ((InstCaches[i].readLine(PC)) != null) {
					displacement = InstCaches[i].accessVariables(PC)[0];
					break;
				} else
					missedCaches++;
			}
			if (missedCaches == 3)
				time += ramAccessTime;
			for (int i = 0; i < missedCaches; i++) {
				InstCaches[i].fetchFromMemory(PC);
			}
			instructions = InstCaches[0].readLine(PC);
			for (int i = displacement; i < instructions.length; i++) {
				if (instructionBuffer.size() == instructionBufferSize)
					break;
				PC++;
				instructions[i].cycles[0] = time + cycle - 1;
				instructionBuffer.add(instructions[i]);
			}
			fetchNow += time - 1;
		} else if (fetchNow > 0)
			fetchNow--;
	}
}
