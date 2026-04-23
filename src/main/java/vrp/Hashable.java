package vrp;

// BloomFilter에 넣을 객체가 구현해야 하는 인터페이스
public interface Hashable {
    long[] getHashes(int numHashes, int bitSetSize);
}
