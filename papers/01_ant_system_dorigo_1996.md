# Ant System (AS)

## Citation
Dorigo, M., Maniezzo, V., & Colorni, A. (1996).  
**Ant system: Optimization by a colony of cooperating agents.**  
*IEEE Transactions on Systems, Man, and Cybernetics – Part B*, 26(1), 29–41.  
DOI: 10.1109/3477.484436

## Algorithm Summary

### Pheromone Initialization
τ₀ = m / C_nn

- m: number of ants
- C_nn: nearest-neighbor heuristic tour length

### Transition Rule
p(i→j) = [τ(i,j)^α · η(i,j)^β] / Σ[τ(i,k)^α · η(i,k)^β]

- η(i,j) = 1 / d(i,j) (heuristic visibility)
- α: pheromone weight
- β: heuristic weight

### Pheromone Update (after all ants complete tours)
τ(i,j) ← (1 - ρ) · τ(i,j) + Σ Δτ_k(i,j)

Δτ_k(i,j) = Q / L_k  if ant k used edge (i,j), else 0

- ρ: evaporation rate ∈ (0,1)
- Q: pheromone deposit constant
- L_k: tour length of ant k

### Recommended Parameters
- α = 1, β = 2–5
- ρ = 0.5
- m = n (number of nodes)

## Implementation Notes (vrp/AcoEngine.java)
- τ₀: `numAnts / nearestNeighborDist()` ✅
- Transition: `tau^alpha * (1/dist)^beta` ✅
- Update: evaporate all, then deposit `Q/L` for each ant ✅
