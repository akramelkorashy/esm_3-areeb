import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import javax.xml.crypto.Data;

/**
 * Assumptions:
 * 
 * 1- Cache line size is always a multiple of the lower level cache's line size.
 * 
 * 2- Memory has as many ports as needed (infinite) -- This can be limited by
 * keeping counters for each port (when they will be accessible) but the number
 * of ports is not specified as an input parameter
 * 
 * 3- When one level cache has a Write-Allocate policy, then all higher levels
 * of cache should have a Write-Allocate policy as well.
 * */
public class Main {
	static int allCommitted;
	static int lastBranched = -1;
	static BufferedReader br;
	static Cache[] DataCaches;
	static Cache[] InstCaches;
	static MemoryWord[] RAM;
	static LinkedList<MemoryWord> instructionBuffer = new LinkedList<MemoryWord>();
	static LinkedList<ROBEntry> rob = new LinkedList<ROBEntry>();
	static int ramSize, ramAccessTime, pipeLineWidth, instructionBufferSize,
			robSize;
	static ReservationStation[] stations;

	// The 3 PriorityQueues are for the 3 types of functional units; add/sub,
	// mul and div
	static PriorityQueue<MemoryWord>[] functionalUnits = new PriorityQueue[3];

	// The latencies for each of the 3 types of functional units
	static int[] functionalUnitsLatencies;

	static int[] functionalUnitsNums;
	static int cycle = 1;
	static int PC;
	static short[] registers = new short[8];

	// latency before the next fetch can take place
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

	/**
	 * make some initializations, read from stdin the sequence of instructions
	 * to be simulated and make the first step of simulation; storing them in
	 * the RAM
	 * */
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
		System.out
				.println("Done reading specifications. "
						+ "Now, please enter the sequence of instructions to be executed:");
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

	/**
	 * This method also includes the actions of loading and storing into memory
	 * mainly because no functional unit is actually needed at all for accessing
	 * the memory after the address has been calculated.
	 * */
	private static void Commmit() {
		int committed = 0;
		System.out.println("ENTER COMMIT STAGE");
		while (true) {
			ROBEntry peek = rob.peek();
			MemoryWord peekInstruction = rob.peek().instruction;
			if (rob.size() > 0 && peekInstruction.cycles[5] < cycle
					&& !peek.inProgress && committed < pipeLineWidth) {
				// The entry at the peek of the ROB is ready to commit
				System.out.println("COMMITTING");
				committed++;
				allCommitted++;

				short d = peek.logicalRegister;
				short data = rob.remove().data;
				int addr = peekInstruction.source1Val
						+ peekInstruction.source2Val;

				int cacheReached = 0;
				boolean foundInCache = false;

				switch (peekInstruction.type) {
				case LD:
					MemoryWord toLoad = null;
					// getting the word to be loaded
					for (cacheReached = 0; cacheReached < DataCaches.length; cacheReached++) {
						if ((toLoad = (DataCaches[cacheReached].read(addr))) != null) {
							foundInCache = true;
							break;
						}
					}
					if (!foundInCache)
						toLoad = RAM[addr];
					// assigning data to the correct load value in order to be
					// written to the register
					data = toLoad.value;
					// updating the caches back
					while (cacheReached >= 0) {
						DataCaches[cacheReached--].fetchFromMemory(addr);
					}
				case ADD:
				case NAND:
				case MUL:
				case DIV:
				case ADDI:
					registerMap.remove(d);
					registers[d] = data;
					System.out.println(cycle + " " + d + " " + data);
					break;
				case SW:
					// For the purposes of the simulation, any value to be
					// stored will be stored in the RAM array. Only the dirty
					// bits will be set according to the method calls of each
					// cache and the latencies will be adjusted in the current
					// and all subsequent memory accesses based on the dirty
					// bits and the check on the cache policies done here.
					MemoryWord toStore = new MemoryWord(peekInstruction.value);
					RAM[addr] = toStore;
					for (cacheReached = 0; cacheReached < DataCaches.length; cacheReached++) {
						if (DataCaches[cacheReached].write(addr, toStore) == Cache.HIT) {
							foundInCache = true;
							// dirty bit setting already handled by the write
							// method call
							break;
						}
					}

					// According to the assumption that a write-allocate policy
					// will be followed in the higher-level caches by
					// write-allocate policies successively
					int missedCache = 0;
					// finds the first write-allocate cache (goes bottom-up)
					for (missedCache = 0; missedCache < cacheReached; missedCache++) {
						if (DataCaches[missedCache].missPolicy == CachePolicy.WRITE_ALLOCATE)
							break;
					}
					// writes to all caches starting from the first
					// write-allocate (bottom-up)
					while (missedCache < cacheReached) {
						DataCaches[missedCache].fetchFromMemory(addr);
						DataCaches[missedCache--].write(addr, toStore);
					}
				}

			} else {
				break;
			}
		}
	}

	private static short calculate(short op1, short op2, InsType type) {
		switch (type) {
		case ADD:
		case ADDI:
		case JMP:
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
		// The number of instructions that will broadcast their results to
		// functional units and to the ROB. This shouldn't exceed the
		// pipeLineWidth
		int doneEx = 0;

		for (int i = 0; i < functionalUnits.length;) {
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
			// The inProgress flag of the ROBEntry is set to false and the
			// correct result to be committed is written to the ROBEntry when
			// the instruction execution finish time has been reached
			// (instruction.cycles[4] <= cycle).
			instruction.entry.inProgress = false;
			instruction.entry.data = result;

			// This switch statement handles the branch instructions.
			// NOTE: We need to flush the instruction buffer and keep track
			int temp = lastBranched;
			lastBranched = cycle;

			switch (instruction.type) {
			case JMP:
				PC = PC + 1 + result;
				break;
			case RET:
				PC = instruction.value;
				break;
			case JALR:
				instruction.entry.data = (short) (PC + 1);
				PC = instruction.source2Val;
				break;
			default:
				// NOT a branch instruction.
				lastBranched = temp;
			}
			// The instruction can commit starting from the current cycle only
			// if the instruction is not a Store instruction which takes a
			// longer time to commit, this time is set inside the call of the
			// getExecutionTime
			if (instruction.type != InsType.SW) {
				instruction.cycles[5] = cycle;
			}

			// Broadcasting the result of execution of the instruction to all
			// the reservations stations that have a non-ready operand which is
			// waiting on the value from the same ROB entry that this
			// executed instruction writes to.
			for (int j = 0; j < stations.length; j++) {
				if (stations[j].busy) {
					for (int k = 0; k < stations[j].ready.length; k++)
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
		case SW:
			/*
			 * Note that to get the latency of accessing the memory, we need not
			 * differentiate between the cases of a read and a write (for the
			 * load and store) because the latency is just the same in both
			 * cases. And in case of the different write policies, this doesn't
			 * affect the latency because the total latency accounts for the
			 * cost of accessing all levels lower than the level that will hit.
			 * So, no additional cost will be incurred by writing back to any
			 * level, because its access time will not delay any execution more
			 * than the access time of the highest level reached after which the
			 * needed data will reach its intended destination and execution
			 * continues *
			 */
			int addr = instruction.source1Val + instruction.source2Val;
			int executionTime = functionalUnitsLatencies[0];
			int commitTime = 0;
			boolean foundInCache = false;
			boolean writeBackLatency = false;
			int cacheReached = 0;
			for (cacheReached = 0; cacheReached < DataCaches.length; cacheReached++) {
				commitTime += DataCaches[cacheReached].latency;
				if ((DataCaches[cacheReached].read(addr)) != null) {
					foundInCache = true;
					break;
				}
				writeBackLatency |= (DataCaches[cacheReached].missPolicy == CachePolicy.WRITE_ALLOCATE && DataCaches[cacheReached]
						.willWriteBack(addr));
			}
			// Please note that any write-back that will need to be done is
			// assumed to be done to the main memory because, since a miss
			// happened, the new data will be fetched from memory into all the
			// levels of cache. Otherwise, we would have inconsistent data
			// between the different levels of cache between a block and its
			// subset in the lower level of cache.
			writeBackLatency |= DataCaches[cacheReached].willWriteBack(addr);
			if (!foundInCache || writeBackLatency)
				commitTime += ramAccessTime;
			if (instruction.type == InsType.SW) {
				instruction.cycles[5] = cycle + executionTime + commitTime;
			}
			return executionTime;

		case JMP:
		case JALR:
			return functionalUnitsLatencies[0];
		case RET:
			return 1;
		case BEQ:
			// 2 * functionalUnitsLatencies[0]?? One cycle for comparison and
			// another one for addition??
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
			boolean allReady = true;
			for (boolean ready : stations[i].ready)
				allReady &= ready;
			if (stations[i].busy && stations[i].instruction.cycles[2] < cycle
					&& allReady) {
				System.out.println("ISSUING " + stations[i].instruction);
				int type = stations[i].instruction.functionalUnitType;
				if (functionalUnits[type].size() < functionalUnitsNums[type]) {
					issued++;
					MemoryWord current = stations[i].instruction;
					current.cycles[3] = cycle;
					current.source1Val = stations[i].val[0];
					current.source2Val = stations[i].val[1];
					current.value = stations[i].val[2];
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
				short src[] = { instruction.source1, instruction.source2,
						instruction.destination };

				for (int j = 0; j < stations[i].ready.length; j++) {

					// This check handles write prevention to R0. It handles any
					// invalid value for the register which may evolve from
					// intentionally assigning a dummy value
					if (src[j] < 1 || src[j] > 7) {
						stations[i].ready[j] = true;
						continue;
					}

					if (j == 1 && !instruction.source2IsRegister || j == 2
							&& instruction.isFirstOperandDestination) {
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
