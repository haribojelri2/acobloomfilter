# Tabu Search

## Citations
Glover, F. (1986).  
**Future paths for integer programming and links to artificial intelligence.**  
*Computers & Operations Research*, 13(5), 533–549.  
DOI: 10.1016/0305-0548(86)90048-1

Glover, F. (1989).  
**Tabu search – Part I.**  
*ORSA Journal on Computing*, 1(3), 190–206.  
DOI: 10.1287/ijoc.1.3.190

Glover, F. (1990).  
**Tabu search – Part II.**  
*ORSA Journal on Computing*, 2(1), 4–32.  
DOI: 10.1287/ijoc.2.1.4

## Algorithm Summary

### Core Mechanism
1. Start from current solution s
2. Generate neighborhood N(s) (all swap moves)
3. Select best non-tabu move (or tabu move if aspiration criterion met)
4. Update tabu list (FIFO, fixed tenure)
5. Repeat for maxIter iterations

### Tabu List
- Stores recently applied moves as (i, j) node-swap pairs
- FIFO eviction when list exceeds tenure size
- Prevents cycling by forbidding recent moves

### Aspiration Criterion
Override tabu status if the move leads to a solution better than the best-so-far.

### Dynamic Tenure (recommended)
tenure ∝ √n or n/3 (route-size dependent)

Rationale: larger routes need longer memory to avoid cycling.

### Recommended Parameters
- maxIter: 50–200 (problem-dependent)
- tenure: max(5, route_size / 3)

## Implementation Notes (vrp/LocalSearch.java)
- Move type: pairwise node swap ✅
- Tabu list: `LinkedHashSet<String>` with FIFO eviction ✅
- Aspiration: override if `neighborDist < bestDist` ✅
- Dynamic tenure: `Math.max(5, route.size() / 3)` ✅
- maxIter: 50 ✅
- Applied per route via `applyTabu()` ✅
