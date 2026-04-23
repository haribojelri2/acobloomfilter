package vrp;

public class LoadContextKey implements Hashable {
    private final int prev;
    private final int cur;
    private final int loadBucket;

    public LoadContextKey(int prev, int cur, int load, int capacity, int buckets) {
        this.prev = prev;
        this.cur = cur;
        this.loadBucket = Math.min(buckets - 1, load * buckets / Math.max(1, capacity));
    }

    @Override
    public long[] getHashes(int numHashes, int bitSetSize) {
        long[] hashes = new long[numHashes];
        long base = ((long) prev * 1000003L) ^ ((long) cur * 998244353L) ^ ((long) loadBucket * 2654435761L);
        long h1 = 2166136261L;
        long chunk = base;
        while (chunk != 0) { h1 ^= (chunk & 0xFF); h1 = (h1 * 16777619L) & 0xFFFFFFFFL; chunk >>>= 8; }
        long h2 = (0x811C9DC5L ^ (base * 0x9E3779B9L & 0xFFFFFFFFL)) & 0xFFFFFFFFL;
        chunk = base;
        while (chunk != 0) { h2 ^= (chunk & 0xFF); h2 = (h2 * 16777619L) & 0xFFFFFFFFL; chunk >>>= 8; }
        h2 = (h2 | 1) % bitSetSize;
        if (h2 == 0) h2 = 1;
        for (int i = 0; i < numHashes; i++)
            hashes[i] = (h1 + (long) i * h2) % bitSetSize;
        return hashes;
    }
}
