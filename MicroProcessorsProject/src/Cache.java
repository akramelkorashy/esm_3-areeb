import java.util.Arrays;

public class Cache {
	static final int memorySize = 16;
	static final boolean HIT = false;
	static final boolean MISS = true;
	int L, M, linesPerBank, wordPerLine;
	int latency;
	int hits, misses;
	CachePolicy hitPolicy, missPolicy;
	MemoryWord[][][] Data;
	int[][] Tag;
	boolean[][] dirty;

	public Cache(int[] params) {
		this(params[0], params[1], params[2], params[params.length - 1],
				((params[3] == 1) ? CachePolicy.WRITE_BACK
						: CachePolicy.WRITE_THROUGH),
				((params[4] == 1) ? CachePolicy.WRITE_ALLOCATE
						: CachePolicy.WRITE_AROUND));
	}

	public Cache(int s, int l, int m, int la, CachePolicy hp, CachePolicy mp) {
		s <<= 10;
		L = l;
		M = m;
		int numLines = s / L;
		linesPerBank = numLines / M;
		wordPerLine = L / 2;
		latency = la;
		hits = misses = 0;
		Data = new MemoryWord[m][linesPerBank][wordPerLine];
		Tag = new int[m][linesPerBank];
		dirty = new boolean[m][linesPerBank];
		for (int i = 0; i < m; i++)
			Arrays.fill(Tag[i], -1);
		// System.out.println(L+" "+M+" "+s+" "+numLines+" "+linesPerBank+" "+wordPerLine);
	}

	public int[] accessVariables(int addr) {
		int[] res = new int[3];
		int temp = log(wordPerLine);
		res[0] = addr % (1 << temp);
		addr >>= temp;
		temp = log(linesPerBank);
		// res[1] = addr % (1 << wordPerLine);
		res[1] = addr % (1 << temp);
		addr >>= temp;
		res[2] = addr;
		return res;
	}

	/**
	 * Given the displacement, tag and the 3rd parameter :D, return the memory
	 * address
	 */
	public int getAddress(int[] vars) {
		return vars[0] + vars[1] << log(wordPerLine) + vars[2] << (log(wordPerLine) + log(linesPerBank));
	}

	/**
	 * To be called when a cache block is to be brought from the main memory due
	 * to a miss. The effect of this method is that the cache block (line) which
	 * contains the MemoryWord addressed by the addr parameter is stored in the
	 * cache. The assumption that makes accessing the Main Memory directly and
	 * not the upper level of cache (when a miss that requires heating the cache
	 * happens) is that the line size of a higher level of cache is a multiple
	 * of the level of cache just below it.
	 * */
	public MemoryWord fetchFromMemory(int addr) {
		int[] indices = accessVariables(addr);
		addr /= wordPerLine;
		addr *= wordPerLine;
		int i = indices[1];
		int t = indices[2];
		// ////////The way m is chosen here is the same as in the method,
		// willWriteBack
		int m = 0;
		while (m < M && Tag[m][i] != -1)
			m++;
		m %= M;
		// ////////

		// If the line -at the location to which the new data will be fetched-
		// is dirty, then we need to reset the dirty bit and write the line back
		// to the main memory
		if (dirty[m][i]) {
			dirty[m][i] = false;
			// Need to get the address given the 3 parameters
			// 0 in the first parameter is just a dummy value. Tag[m][i] (the
			// value passed to the 3rd argument) is the
			// old tag for which we need to construct the address
			int oldTag = Tag[m][i];
			int oldAddress = getAddress(new int[] { 0, i, oldTag });
			for (int j = 0; j < wordPerLine; j++) {
				Main.RAM[oldAddress + j] = Data[m][i][j];
			}
			
		}
		for (int j = 0; j < wordPerLine; j++) {
			Data[m][i][j] = Main.RAM[addr + j];
		}
		Tag[m][i] = t;
		return Data[m][i][indices[0]];
	}

	public MemoryWord read(int addr) {
		int[] indices = accessVariables(addr);
		int d = indices[0];
		int i = indices[1];
		int t = indices[2];
		for (int j = 0; j < M; j++) {
			if (Tag[j][i] == t) {
				hits++;
				return Data[j][i][d];
			}
		}
		misses++;
		return null;
	}

	public MemoryWord[] readLine(int addr) {
		int[] indices = accessVariables(addr);
		int i = indices[1];
		int t = indices[2];
		for (int j = 0; j < M; j++) {
			if (Tag[j][i] == t) {
				hits++;
				return Data[j][i];
			}
		}
		misses++;
		return null;
	}

	public boolean write(int addr, MemoryWord data) {
		int[] indices = accessVariables(addr);
		int d = indices[0];
		int i = indices[1];
		int t = indices[2];
		for (int j = 0; j < M; j++) {
			if (Tag[j][i] == t) {
				hits++;
				Data[j][i][d] = data;
				if (hitPolicy == CachePolicy.WRITE_BACK) {
					dirty[j][i] = true;
				}
				return HIT;
			}
		}
		misses++;
		return MISS;
	}

	public boolean isLineDirty(int addr) {
		int[] indices = accessVariables(addr);
		int i = indices[1];
		int t = indices[2];
		for (int j = 0; j < M; j++) {
			if (Tag[j][i] == t) {
				return dirty[j][i];
			}
		}
		return false;
	}

	public boolean willWriteBack(int addr) {
		// searches for the value of m where the memory word with the given
		// address will be cached, then checks if this line is dirty
		int[] indices = accessVariables(addr);
		int i = indices[1];
		int m = 0;
		while (m < M && Tag[m][i] != -1)
			m++;
		m %= M;
		return dirty[m][i];
	}

	public static int log(int x) {
		int ans = 0;
		while (x > 1) {
			x >>= 1;
			ans++;
		}
		return ans;
	}
}
