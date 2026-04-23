package vrp;

public class SubPathKey implements Hashable {
    private final int[] nodes;
    private final int len;

    public SubPathKey(int[] nodes, int len) {
        this.nodes = nodes;
        this.len = len;
    }

    @Override
    public long[] getHashes(int numHashes, int bitSetSize) {
        long h1 = 2166136261L;
        for (int i = 0; i < len; i++) {
            h1 ^= nodes[i];
            h1 = (h1 * 16777619L) & 0xFFFFFFFFL;
        }
        long h2 = 0x811C9DC5L;
        for (int i = 0; i < len; i++) {
            h2 ^= (nodes[i] * 2654435761L & 0xFFFFFFFFL);
            h2 = (h2 * 16777619L) & 0xFFFFFFFFL;
        }
        h2 = (h2 | 1) % bitSetSize;
        if (h2 == 0) h2 = 1;
        long[] hashes = new long[numHashes];
        for (int i = 0; i < numHashes; i++)
            hashes[i] = (h1 + (long) i * h2) % bitSetSize;
        return hashes;
    }
}
