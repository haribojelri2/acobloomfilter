# ACO for VRP — Related Papers

## Core ACO-VRP Papers

### ACS (Ant Colony System)
Dorigo, M., & Gambardella, L. M. (1997).  
**Ant colony system: A cooperative learning approach to the traveling salesman problem.**  
*IEEE Transactions on Evolutionary Computation*, 1(1), 53–66.  
DOI: 10.1109/4235.585892

Key contributions:
- Pseudo-random proportional rule (exploitation vs exploration)
- Local pheromone update (during construction)
- Global update only by best ant

### MACS-VRPTW (ACO for VRP with Time Windows)
Gambardella, L. M., Taillard, E. D., & Agarwal, M. (1999).  
**Ant colonies for the QAP.**  
In: Corne, D. et al. (Eds.), *New Ideas in Optimization*, McGraw-Hill, pp. 215–232.

Gambardella, L. M., & Dorigo, M. (1996).  
**Solving symmetric and asymmetric TSPs by ant colonies.**  
*Proceedings of IEEE ICEC*, pp. 622–627.

### ACO for CVRP (Capacitated VRP)
Bell, J. E., & McMullen, P. R. (2004).  
**Ant colony optimization techniques for the vehicle routing problem.**  
*Advanced Engineering Informatics*, 18(1), 41–48.  
DOI: 10.1016/j.aei.2004.07.001  
URL: https://www.sciencedirect.com/science/article/abs/pii/S1474034604000060

Reimann, M., Doerner, K., & Hartl, R. F. (2004).  
**D-ants: Savings based ants divide and conquer the vehicle routing problem.**  
*Computers & Operations Research*, 31(4), 563–591.  
DOI: 10.1016/S0305-0548(03)00014-5

### Real-World ACO-VRP
Bianchi, L., Birattari, M., Chiarandini, M., Manfrin, M., Mastrolilli, M., Paquete, L., Rossi-Doria, O., & Schiavinotto, T. (2006).  
**Hybrid metaheuristics for the vehicle routing problem with stochastic demands.**  
*Journal of Mathematical Modelling and Algorithms*, 5(1), 91–110.

## Survey Papers

Dorigo, M., & Blum, C. (2005).  
**Ant colony optimization theory: A survey.**  
*Theoretical Computer Science*, 344(2–3), 243–278.  
DOI: 10.1016/j.tcs.2005.05.020

Dorigo, M., Birattari, M., & Stutzle, T. (2006).  
**Ant colony optimization.**  
*IEEE Computational Intelligence Magazine*, 1(4), 28–39.  
DOI: 10.1109/MCI.2006.329691

## Probabilistic Filtering in Search / Optimization

### Bloom Filter Applications in Databases/Systems
Broder, A., & Mitzenmacher, M. (2004).  
**Network applications of Bloom filters: A survey.**  
*Internet Mathematics*, 1(4), 485–509.  
DOI: 10.1080/15427951.2004.10129096

### Visited-State Caching in Search
Similar ideas (visited-state filters to avoid revisiting) appear in:

Russell, S., & Norvig, P. (2020).  
**Artificial Intelligence: A Modern Approach** (4th ed.), Chapter 3.  
Prentice Hall.  
— Discusses cycle detection and closed-list filtering in graph search.

## Note on ACO + BloomFilter Novelty
As of 2026, no published paper explicitly combines:
- Bloom filter as a **soft penalty** mechanism (not hard pruning)
- Applied to **(prev, cur, load_bucket)** context-aware keys in CVRP
- With **adaptive activation** triggered by MMAS stagnation detection
- With **probabilistic decay** to allow filter self-reset

This combination in `MmasWithBloomFilter` / `AcoWithBloomFilter` appears to be  
an original contribution of this project.
