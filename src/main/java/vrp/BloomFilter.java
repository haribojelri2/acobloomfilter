package vrp;

import java.util.BitSet;

public class BloomFilter {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numHashes;

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
        for (long pos : item.getHashes(numHashes, bitSetSize))
            if (!bitSet.get((int) pos)) return false;
        return true;
    }

    public void addLong(long key) {
        for (int i = 0; i < numHashes; i++) {
            long h = (key ^ (i * 2654435761L)) * 6364136223846793005L + 1442695040888963407L;
            bitSet.set((int) ((h & Long.MAX_VALUE) % bitSetSize));
        }
    }

    public boolean mightContainLong(long key) {
        for (int i = 0; i < numHashes; i++) {
            long h = (key ^ (i * 2654435761L)) * 6364136223846793005L + 1442695040888963407L;
            if (!bitSet.get((int) ((h & Long.MAX_VALUE) % bitSetSize))) return false;
        }
        return true;
    }

    public synchronized void clear() {
        bitSet.clear();
    }

    public synchronized double fillRate() { return (double) bitSet.cardinality() / bitSetSize; }

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
