# MAX-MIN Ant System (MMAS)

## Citation
Stützle, T., & Hoos, H. H. (2000).  
**MAX-MIN ant system.**  
*Future Generation Computer Systems*, 16(8), 889–914.  
DOI: 10.1016/S0167-739X(00)00043-1

## Algorithm Summary

### τ_max Computation
τ_max = 1 / (ρ · C_best)

- ρ: evaporation rate
- C_best: best-so-far solution cost (updated each iteration)

### τ_min Computation (Eq. 9 in paper)
τ_min = τ_max · (1 - p_best^(1/n)) / ((n/2 - 1) · p_best^(1/n))

- p_best: probability that best solution is constructed (recommended: 0.05)
- n: number of nodes

### Pheromone Update
Only ONE ant deposits pheromone per iteration (iteration-best or global-best):

τ(i,j) ← (1 - ρ) · τ(i,j) + Δτ_best(i,j)

After update, clip: τ_min ≤ τ(i,j) ≤ τ_max

### f_gb Schedule (global-best vs iteration-best)
Gradual transition from iteration-best to global-best:
- Early iterations (< 25%): always use iteration-best
- Mid-early (25–50%): use global-best every 5 iterations
- Mid-late (50–75%): use global-best every 3 iterations
- Late (> 75%): always use global-best

### Stagnation Detection (λ-branching factor)
bf = (1/n) · Σ_i |{j : τ(i,j) ≥ λ · τ_max}|

- λ = 0.05 (recommended)
- If bf < threshold (e.g., 2.0): reinitialize all pheromone to τ_max

### Initialization
All τ(i,j) = τ_max at start

## Implementation Notes (vrp/MmasEngine.java)
- τ_max: `1.0 / (rho * bestDist)` ✅
- τ_min: `computeTMin(tMax, n)` following Eq. 9 ✅
- f_gb schedule: `useGlobalBest(iter)` with 25/50/75% thresholds ✅
- λ-branching: `branchingFactor()` with LAMBDA=0.05, BRANCHING_THRESHOLD=2.0 ✅
- Clipping: `Math.min(tMax, Math.max(tMin, ...))` ✅
- Init: all pheromone set to tMax ✅
