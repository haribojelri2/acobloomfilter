package vrp;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLong;

public class BloomFilter {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numHashes;

    private final AtomicLong queryCount = new AtomicLong(0);
    private final AtomicLong hitCount = new AtomicLong(0);

    public BloomFilter(int bitSetSize, int numHashes) {
        this.bitSetSize = bitSetSize;
        this.numHashes = numHashes;
        this.bitSet = new BitSet(bitSetSize);
    }

    public synchronized void add(Hashable item) {
        for (long pos : item.getHashes(numHashes, bitSetSize))
            bitSet.set((int) pos);
    }

    public boolean mightContain(Hashable item) {
        queryCount.incrementAndGet();
        for (long pos : item.getHashes(numHashes, bitSetSize))
            if (!bitSet.get((int) pos)) return false;
        hitCount.incrementAndGet();
        return true;
    }

    public synchronized void clear() {
        bitSet.clear();
    }

    public synchronized void resetStats() { queryCount.set(0); hitCount.set(0); }
    public synchronized double fillRate() { return (double) bitSet.cardinality() / bitSetSize; }
    public synchronized double getHitRate() { long q = queryCount.get(); return q > 0 ? (double) hitCount.get() / q : 0.0; }
    public synchronized long getQueryCount() { return queryCount.get(); }

    public synchronized void decay(double keepProb, java.util.Random rng) {
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1))
            if (rng.nextDouble() > keepProb) bitSet.clear(i);
    }

    public static int optimalBitSetSize(int expectedElements, double fpr) {
        return (int) Math.ceil(-expectedElements * Math.log(fpr) / (Math.log(2) * Math.log(2)));
    }

    public static int optimalNumHashes(int bitSetSize, int expectedElements) {
        return Math.max(1, (int) Math.round((double) bitSetSize / expectedElements * Math.log(2)));
    }
}
