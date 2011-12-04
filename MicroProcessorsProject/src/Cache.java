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
		for (int i = 0; i < m; i++)
			Arrays.fill(Tag[i], -1);
	}

	public int[] accessVariables(int addr) {
		int[] res = new int[3];
		int temp = log(wordPerLine);
		res[0] = addr % (1 << temp);
		addr >>= temp;
		temp = log(linesPerBank);
		res[1] = addr % (1 << wordPerLine);
		addr >>= temp;
		res[2] = addr;
		return res;
	}

	public MemoryWord fetchFromMemory(int addr) {
		addr /= wordPerLine;
		addr *= wordPerLine;
		int[] indices = accessVariables(addr);
		int i = indices[1];
		int t = indices[2];
		int m = 0;
		while (m < M && Tag[m][i] != -1)
			m++;
		m %= M;
		for (int j = 0; j < wordPerLine; j++) {
			Data[m][i][j] = Main.RAM[addr + j];
		}
		Tag[m][i] = t;
		return Data[m][i][addr % (1 << wordPerLine)];
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

	public boolean write(int addr, MemoryWord data) {
		int[] indices = accessVariables(addr);
		int d = indices[0];
		int i = indices[1];
		int t = indices[2];
		for (int j = 0; j < M; j++) {
			if (Tag[j][i] == t) {
				hits++;
				Data[j][i][d] = data;
				return HIT;
			}
		}
		misses++;
		return MISS;
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
