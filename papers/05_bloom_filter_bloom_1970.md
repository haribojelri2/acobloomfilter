# Bloom Filter (Original)

## Citation
Bloom, B. H. (1970).  
**Space/time trade-offs in hash coding with allowable errors.**  
*Communications of the ACM*, 13(7), 422–426.  
DOI: 10.1145/362686.362692

## Algorithm Summary

### Structure
- Bit array of m bits (all initialized to 0)
- k independent hash functions h₁, h₂, ..., hₖ

### Add(x)
For each hᵢ: set bit[hᵢ(x)] = 1

### Query(x) — "mightContain"
For each hᵢ: if bit[hᵢ(x)] == 0 → DEFINITELY NOT IN SET  
If all bits are 1 → POSSIBLY IN SET (false positive possible)

### False Positive Rate
ε ≈ (1 - e^(-kn/m))^k

### Optimal Parameters
Optimal k = (m/n) · ln(2)  
Optimal m = -n · ln(ε) / (ln(2))²

Where:
- n: expected number of insertions
- ε: desired false positive rate
- m: bit array size (bits)
- k: number of hash functions

## Key Properties
- False negatives: IMPOSSIBLE
- False positives: possible (rate ε)
- No deletion (standard variant)
- Space: ~9.6 bits/element at ε=1%

## Variants Used in This Project

### Counting Bloom Filter / Decay
Modified variant where bits have counts or weights that decay over time.  
Used to implement "soft forgetting" of old path states.

`bloomFilter.decay(BF_DECAY=0.95, rng)` — probabilistically clears bits,  
allowing the filter to forget old bad routes during MMAS stagnation phases.

## Application in This Project (Novel — No Prior Paper)
Context key: `LoadContextKey(prev, cur, load_bucket, capacity)`

When stagnation detected (no improvement for STAGNATION_WINDOW=5 iterations):
1. Activate BloomFilter
2. Register edges from above-average-cost solutions
3. Apply soft penalty (×0.15) to candidates whose context key is in the filter
4. Decay filter each iteration (rate 0.95) to avoid over-penalization

This is a **novel application** — no prior published work combines ACO pheromone  
guidance with Bloom-filter-based soft constraint pruning for VRP.
