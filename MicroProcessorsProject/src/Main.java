import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import javax.xml.crypto.Data;

public class Main {
	static int allCommitted;
	static BufferedReader br;
	static Cache[] DataCaches;
	static Cache[] InstCaches;
	static MemoryWord[] RAM;
	static LinkedList<MemoryWord> instructionBuffer = new LinkedList<MemoryWord>();
	static LinkedList<ROBEntry> rob = new LinkedList<ROBEntry>();
	static int ramSize, ramAccessTime, pipeLineWidth, instructionBufferSize,
			robSize;
	static ReservationStation[] stations;
	static PriorityQueue<MemoryWord>[] functionalUnits = new PriorityQueue[3];
	static int[] functionalUnitsLatencies;
	static int[] functionalUnitsNums;
	static int cycle = 1;
	static int PC;
	static short[] registers = new short[8];
	static int fetchNow = 0;
	static HashMap<Short, ROBEntry> registerMap = new HashMap<Short, ROBEntry>();

	public static int[] readCacheInputs() throws NumberFormatException,
			IOException {
		int[] res = new int[6];
		System.out.println("Please Enter The Geometry of the cache");
		System.out.print("Cache Size In KB: ");
		res[0] = Integer.parseInt(br.readLine());
		System.out.println();
		System.out.print("Line Size In B: ");
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
		functionalUnitsNums = new int[3];
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
		for (int i = 0; i < functionalUnits.length; i++) {
			functionalUnits[i] = new PriorityQueue<MemoryWord>();
		}
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
		System.setIn(new FileInputStream(new File("Input.txt")));
		br = new BufferedReader(new InputStreamReader(System.in));
		readInput();
		System.out.println("Done reading specifications");
		readInstructions();
		/*
		 * registers[0] = 1; registers[1] = 2; registers[5] = 1; registers[6] =
		 * 7;
		 */

		while (cycle < 30) {
			System.out.println(cycle + "-------------------------");
			fetch();
			decode();
			dispatch();
			Issue();
			execute();
			Commmit();
			cycle++;
		}
	}

	private static void Commmit() {
		int committed = 0;
		System.out.println("ENTER COMMIT STAGE");
		while (true)
			if (rob.size() > 0 && rob.peek().instruction.cycles[5] < cycle
					&& !rob.peek().inProgress && committed < pipeLineWidth) {
				System.out.println("COMMITTING");
				committed++;
				allCommitted++;
				short d = rob.peek().logicalRegister;
				short data = rob.remove().data;
				registerMap.remove(d);
				registers[d] = data;
				System.out.println(cycle + " " + d + " " + data);
			} else {
				break;
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

	private static void execute() {
		System.out.println("ENTER EXECUTE STAGE");
		;
		int doneEx = 0;
		for (int i = 0; i < 2;) {
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
			System.out.println("EXECUTING");
			functionalUnits[i].remove();
			doneEx++;
			short result = calculate(instruction.source1Val,
					instruction.source2Val, instruction.type);
			instruction.entry.inProgress = false;
			instruction.entry.data = result;
			instruction.cycles[5] = cycle;
			for (int j = 0; j < stations.length; j++) {
				if (stations[j].busy) {
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

	private static int getExecuteTime(MemoryWord instruction) {
		switch (instruction.type) {
		case ADD:
		case ADDI:
		case NAND:
		case MUL:
		case DIV:
			return functionalUnitsLatencies[0];
		case LD:
			int adr = instruction.source1Val + instruction.source2Val;
			int time = functionalUnitsLatencies[0];
			boolean flag = false;
			for (int i = 0; i < DataCaches.length; i++) {
				time += DataCaches[i].latency;
				if ((DataCaches[i].read(adr)) != null) {
					flag = true;
					break;
				}
			}
			if (!flag)
				time += ramAccessTime;
			return time;
		case SW:
		case JMP:
		case BEQ:
		case JALR:
		case RET:
		default:
			return 0;
		}
	}

	private static void Issue() {
		System.out.println("ENTER ISSUE STAGE");
		int issued = 0;
		for (int i = 0; i < stations.length; i++) {
			if (issued == pipeLineWidth)
				break;
			if (stations[i].busy && stations[i].instruction.cycles[2] < cycle
					&& stations[i].ready[0] && stations[i].ready[1]) {
				System.out.println("ISSUING " + stations[i].instruction);
				int type = stations[i].instruction.instType;
				if (functionalUnits[type].size() < functionalUnitsNums[type]) {
					issued++;
					MemoryWord current = stations[i].instruction;
					current.cycles[3] = cycle;
					current.source1Val = stations[i].val[0];
					current.source2Val = stations[i].val[1];
					int executing = getExecuteTime(current);
					current.cycles[4] = cycle + executing;
					functionalUnits[type].add(current);
					stations[i].busy = false;
				}
			}
		}
	}

	private static String checkRegisterMap(short src) {
		if (!registerMap.containsKey(src))
			return registers[src] + "";
		ROBEntry entry = registerMap.get(src);
		if (!entry.inProgress)
			return entry.data + "";
		return null;
	}

	private static void dispatch() {
		System.out.println("ENTER DISPATCH STAGE");
		for (int i = 0; i < stations.length; i++) {
			if (stations[i].busy && stations[i].instruction.cycles[1] < cycle
					&& stations[i].instruction.cycles[2] == Integer.MAX_VALUE) {
				stations[i].instruction.cycles[2] = cycle;
				MemoryWord instruction = stations[i].instruction;
				System.out.println("DISPATCHING " + instruction);
				short src[] = { instruction.source1, instruction.source2 };
				for (int j = 0; j < 2; j++) {
					if (j == 1 && !instruction.flagForSource2) {
						stations[i].ready[j] = true;
						stations[i].val[j] = src[j];
						break;
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
		System.out.println("ENTER DECODE STAGE");
		if (instructionBuffer.size() > 0
				&& instructionBuffer.getFirst().cycles[0] >= cycle)
			return;
		int num = pipeLineWidth;
		while (instructionBuffer.size() > 0
				&& instructionBuffer.getFirst().cycles[0] < cycle && num > 0) {
			num--;
			if (robSize > rob.size()) {
				for (int i = 0; i < stations.length; i++) {
					if (!stations[i].busy) {
						stations[i].busy = true;
						MemoryWord instruction = instructionBuffer.remove();
						System.out.println("DECODING " + instruction);
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
		System.out.println("ENTER FETCH STAGE");
		if (fetchNow == 0 && instructionBuffer.size() < instructionBufferSize) {
			System.out.println("FETCHING");
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
			if (missedCaches == InstCaches.length)
				time += ramAccessTime;
			for (int i = 0; i < missedCaches; i++) {
				InstCaches[i].fetchFromMemory(PC);
			}
			instructions = InstCaches[0].readLine(PC);
			for (int i = displacement; i < instructions.length; i++) {
				if (instructionBuffer.size() == instructionBufferSize)
					break;
				if (instructions[i] == null)
					continue;
				PC++;
				instructions[i].cycles[0] = time + cycle - 1;
				instructionBuffer.add(instructions[i]);
			}
			fetchNow += time - 1;
		} else if (fetchNow > 0)
			fetchNow--;
	}
}
