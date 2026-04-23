package vrp;

public class DestLoadKey implements Hashable {
    private final int cur, loadBucket;

    public DestLoadKey(int cur, int load, int capacity, int buckets) {
        this.cur = cur;
        this.loadBucket = Math.min(buckets - 1, load * buckets / Math.max(1, capacity));
    }

    @Override
    public long[] getHashes(int numHashes, int bitSetSize) {
        long[] hashes = new long[numHashes];
        long k = (long) cur ^ ((long) loadBucket << 12);
        long h1 = 2166136261L;
        long chunk = k;
        while (chunk != 0) { h1 ^= (chunk & 0xFF); h1 = (h1 * 16777619L) & 0xFFFFFFFFL; chunk >>>= 8; }
        long h2 = (0x811C9DC5L ^ (k * 0x9E3779B9L & 0xFFFFFFFFL)) & 0xFFFFFFFFL;
        chunk = k;
        while (chunk != 0) { h2 ^= (chunk & 0xFF); h2 = (h2 * 16777619L) & 0xFFFFFFFFL; chunk >>>= 8; }
        h2 = (h2 | 1) % bitSetSize;
        if (h2 == 0) h2 = 1;
        for (int i = 0; i < numHashes; i++)
            hashes[i] = (h1 + (long) i * h2) % bitSetSize;
        return hashes;
    }
}
