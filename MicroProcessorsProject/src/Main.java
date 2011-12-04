import java.io.*;
import java.util.PriorityQueue;

public class Main {
	static BufferedReader br;
	static int[][] DataCaches;
	static int[][] InstCaches;
	static MemoryWord[] RAM;
	static int ramSize, ramAccessTime, pipeLineWidth;
	static ReservationStation[] stations;
	static PriorityQueue<Integer> arithFunctionalUnit;
	static PriorityQueue<Integer> mulFunctionalUnit;
	static PriorityQueue<Integer> divFunctionalUnit;
	static int[] funtionalUnitsLatencies;
	static int[] funtionalUnitsNums;

	public static int[] readDataCacheInputs() throws NumberFormatException,
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
		DataCaches = new int[numDataCaches][6];
		System.out
				.println("Please enter the number of Instruction Caches levels");
		int numInstrCaches = Integer.parseInt(br.readLine());
		InstCaches = new int[numInstrCaches][6];
		for (int i = 0; i < numDataCaches; i++) {
			System.out.println("Enter Level " + (i + 1)
					+ " Data Cache specification ");
			DataCaches[i] = readDataCacheInputs();
		}
		for (int i = 0; i < numInstrCaches; i++) {
			System.out.println("Enter Level " + (i + 1)
					+ " Instruction Cache specification ");
			InstCaches[i] = readDataCacheInputs();
		}
	}

	public static void readInput() throws NumberFormatException, IOException {
		System.out.println("Welcome To MIAO Simulator :) ");
		initCaches();
		System.out.println("Enter The Memory Capacity!!");
		ramSize = Integer.parseInt(br.readLine());
		RAM=new MemoryWord[ramSize];
		System.out.println("Please Enter The memory access time");
		ramAccessTime = Integer.parseInt(br.readLine());
		System.out.println("Please enter the pipeline width");
		pipeLineWidth = Integer.parseInt(br.readLine());
		System.out.println("Please enter the size of the instruction buffer");
		int instructionBufferSize = Integer.parseInt(br.readLine());
		System.out.println("Please enter the number of reservation stations");
		stations = new ReservationStation[Integer.parseInt(br.readLine())];
		System.out.println("Please enter the number of ROB entries.");
		int robSize = Integer.parseInt(br.readLine());
		funtionalUnitsLatencies = new int[3];
		System.out
				.println("Please enter the number of add/subtract Funtional Units");
		funtionalUnitsNums[0] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the latency of add/subtract Funtional Unit");
		funtionalUnitsLatencies[0] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the number of multiply Funtional Units");
		funtionalUnitsNums[1] = Integer.parseInt(br.readLine());
		System.out
				.println("Please enter the latency of multiply Funtional Unit");
		funtionalUnitsLatencies[1] = Integer.parseInt(br.readLine());
		
		System.out.println("Please enter the number of divide Funtional Units");
		funtionalUnitsNums[2] = Integer.parseInt(br.readLine());
		System.out.println("Please enter the latency of divide Funtional Unit");
		funtionalUnitsLatencies[2] = Integer.parseInt(br.readLine());
	}
	public static void readInstructions() throws NumberFormatException, IOException {
		int startAddress=Integer.parseInt(br.readLine());
		String inst=br.readLine();
		while(inst!=null) {
			RAM[startAddress++]=new MemoryWord(inst);
			inst=br.readLine();
		}
	}
	public static void main(String[] args) throws NumberFormatException,
			IOException {
		br = new BufferedReader(new InputStreamReader(System.in));
		readInput();
		System.setIn(new FileInputStream(new File("Input.in")));
		readInstructions();
	}
}
