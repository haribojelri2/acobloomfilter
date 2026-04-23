# 2-opt Local Search

## Citation (Primary)
Lin, S. (1965).  
**Computer solutions of the traveling salesman problem.**  
*Bell System Technical Journal*, 44(10), 2245–2269.  
DOI: 10.1002/j.1538-7305.1965.tb04146.x

## Citation (Independent Discovery)
Croes, G. A. (1958).  
**A method for solving traveling-salesman problems.**  
*Operations Research*, 6(6), 791–812.  
DOI: 10.1287/opre.6.6.791

## Algorithm Summary

### 2-opt Move
Given route [..., a, b, ..., c, d, ...]:
- Remove edges (a,b) and (c,d)
- Add edges (a,c) and (b,d)
- Reverse the segment [b, ..., c]

Improvement condition:
d(a,b) + d(c,d) > d(a,c) + d(b,d)

### Iteration
Repeat until no improving 2-opt move exists (local optimum).

### Complexity
- Single pass: O(n²)
- Until convergence: O(n² · iterations)

### Application to VRP
Applied independently to each vehicle route (intra-route optimization).
Inter-route moves (Or-opt, relocate) are separate operators not implemented here.

## Implementation Notes (vrp/LocalSearch.java)
- Segment reversal via `twoOptSwap(best, i, j)` ✅
- Depot (node 0) handled implicitly at route boundaries ✅
- Convergence: loop until `improved == false` ✅
- Skips routes with < 2 nodes ✅
