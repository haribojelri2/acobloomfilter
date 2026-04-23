package vrp;

public class EdgeKey implements Hashable {
    private final int from;
    private final int to;

    public EdgeKey(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public long[] getHashes(int numHashes, int bitSetSize) {
        long[] hashes = new long[numHashes];
        long base = ((long) from << 20) ^ to;
        for (int i = 0; i < numHashes; i++) {
            long h = (base ^ (i * 2654435761L)) * 6364136223846793005L + 1442695040888963407L;
            hashes[i] = (h & Long.MAX_VALUE) % bitSetSize;
        }
        return hashes;
    }
}
