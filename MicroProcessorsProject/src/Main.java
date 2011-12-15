import java.io.*;
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
	static PriorityQueue<Integer> arithFunctionalUnit;
	static PriorityQueue<Integer> mulFunctionalUnit;
	static PriorityQueue<Integer> divFunctionalUnit;
	static int[] functionalUnitsLatencies;
	static int[] functionalUnitsNums;
	static int cycle = 0;
	static int PC;
	static int startDecodeAfter = 0;
	static short[] regsiters = new short[8];
	static HashMap<Integer, ROBEntry> regsiterMap = new HashMap<Integer, ROBEntry>();

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
        MemoryWord [] go = new MemoryWord[5];
        int k = 0;
        MemoryWord nextCurrent = null;
        MemoryWord nextCurrent2 = null;
		while (true) {
			MemoryWord current = fetch();
			decode(nextCurrent);
			dispatch(nextCurrent2);
			Issue();
			execute();
			Commmit();
			nextCurrent2 = nextCurrent;
			nextCurrent = current;
			cycle++;
		}
	}

	private static void Commmit() {

	}

	private static void Issue() {

	}

	public static void execute() {

	}

	private static void dispatch(MemoryWord instruction) {

	}

	private static void decode(MemoryWord instruction) {
		if (rob.size() == robSize) {
			return;
		}
		for (int i = 0; i < stations.length; i++) {
			if (stations[i].free) {
				stations[i].free = false;
				stations[i].instruction = instruction;
				ROBEntry entry = new ROBEntry(instruction.type,
						instruction.destination);
				rob.add(entry);
				stations[i].robEntry = entry;
				break;
			}
		}
	}

	private static MemoryWord fetch() {
		if (startDecodeAfter > 0) {
			startDecodeAfter--;
			return null;
		}
		if (instructionBuffer.size() < pipeLineWidth) {
		
			// not enough instructions in instruction buffer
			startDecodeAfter = 0;
			boolean getFromMemory = true;
			int displacement = 0;
			MemoryWord[] instructions = null;
			int time = 0;
			for (int i = 0; i < InstCaches.length; i++) {
				time += InstCaches[i].latency;
				if ((instructions = InstCaches[i].readLine(PC)) != null) {
					getFromMemory = false;
					displacement = InstCaches[i].accessVariables(PC)[0];
					break;
				}
			}
			if (getFromMemory) {
				instructions = new MemoryWord[] { RAM[PC] };
				time += ramAccessTime;
				for (int i = 0; i < InstCaches.length; i++)
					InstCaches[i].fetchFromMemory(PC);
			}
			startDecodeAfter = time - 1;
			for (int i = displacement; i < instructions.length; i++) {
				if (instructionBuffer.size() == instructionBufferSize)
					break;
				PC++;
				instructionBuffer.add(instructions[i]);
			}
			return null;
		}
		return instructionBuffer.remove();
	}
}
