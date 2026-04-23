# Graph Report - .  (2026-04-19)

## Corpus Check
- 40 files · ~126,084 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 251 nodes · 319 edges · 38 communities detected
- Extraction: 74% EXTRACTED · 26% INFERRED · 0% AMBIGUOUS · INFERRED: 82 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_ACO Survey & Network BF Papers|ACO Survey & Network BF Papers]]
- [[_COMMUNITY_AcoEngine Core Internals|AcoEngine Core Internals]]
- [[_COMMUNITY_BF Adaptive Activation & Decay|BF Adaptive Activation & Decay]]
- [[_COMMUNITY_AcoWithBloomFilter Internals|AcoWithBloomFilter Internals]]
- [[_COMMUNITY_ACS Algorithm (Dorigo 1997)|ACS Algorithm (Dorigo 1997)]]
- [[_COMMUNITY_Local Search Papers & Classes|Local Search Papers & Classes]]
- [[_COMMUNITY_Algorithm Class Hierarchy|Algorithm Class Hierarchy]]
- [[_COMMUNITY_BenchmarkRunner & Generator|BenchmarkRunner & Generator]]
- [[_COMMUNITY_Node & Distance Model|Node & Distance Model]]
- [[_COMMUNITY_EdgeKey Hashable|EdgeKey Hashable]]
- [[_COMMUNITY_LoadContextKey Hashable|LoadContextKey Hashable]]
- [[_COMMUNITY_PathKey Hashable|PathKey Hashable]]
- [[_COMMUNITY_Project Config & Plugins|Project Config & Plugins]]
- [[_COMMUNITY_CVRP Literature (Bell 2004)|CVRP Literature (Bell 2004)]]
- [[_COMMUNITY_AcoWithLocalSearch|AcoWithLocalSearch]]
- [[_COMMUNITY_Hashable Interface|Hashable Interface]]
- [[_COMMUNITY_VrpSolution|VrpSolution]]
- [[_COMMUNITY_Novel BF+MMAS Contribution|Novel BF+MMAS Contribution]]
- [[_COMMUNITY_Claude Advisor & Implementer|Claude Advisor & Implementer]]
- [[_COMMUNITY_D-Ants Paper (Reimann 2004)|D-Ants Paper (Reimann 2004)]]
- [[_COMMUNITY_2-opt Method|2-opt Method]]
- [[_COMMUNITY_Tabu Search Method|Tabu Search Method]]
- [[_COMMUNITY_BloomFilter mightContain|BloomFilter mightContain]]
- [[_COMMUNITY_BloomFilter decay|BloomFilter decay]]
- [[_COMMUNITY_Source Directory Config|Source Directory Config]]
- [[_COMMUNITY_Output Directory Config|Output Directory Config]]
- [[_COMMUNITY_ACO-VRP Related Works|ACO-VRP Related Works]]
- [[_COMMUNITY_Ant System 1996|Ant System 1996]]
- [[_COMMUNITY_MMAS pBest Parameter|MMAS pBest Parameter]]
- [[_COMMUNITY_MMAS Lambda Parameter|MMAS Lambda Parameter]]
- [[_COMMUNITY_MMAS Branching Threshold|MMAS Branching Threshold]]
- [[_COMMUNITY_MMAS tMax Initialization|MMAS tMax Initialization]]
- [[_COMMUNITY_2-opt O(n2) Complexity|2-opt O(n2) Complexity]]
- [[_COMMUNITY_Tabu maxIter Parameter|Tabu maxIter Parameter]]
- [[_COMMUNITY_BF No-Deletion Property|BF No-Deletion Property]]
- [[_COMMUNITY_BF Space Efficiency|BF Space Efficiency]]
- [[_COMMUNITY_Bianchi 2006 Hybrid Paper|Bianchi 2006 Hybrid Paper]]
- [[_COMMUNITY_BACO Join Evaluation|BACO Join Evaluation]]

## God Nodes (most connected - your core abstractions)
1. `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` - 13 edges
2. `Bloom Filter Paper (Bloom 1970)` - 13 edges
3. `MAX-MIN Ant System Paper (Stützle & Hoos 2000)` - 11 edges
4. `BACO: Bloom-filter Enhanced ACO Algorithm` - 11 edges
5. `AcoEngine` - 9 edges
6. `UTFACO: Unified Threat Identification and Fault Localization ACO` - 9 edges
7. `BloomFilter` - 8 edges
8. `Novel ACO+Bloom Filter VRP Application (No Prior Paper)` - 8 edges
9. `AcoWithBloomFilter` - 7 edges
10. `LocalSearch` - 7 edges

## Surprising Connections (you probably didn't know these)
- `MAX-MIN Ant System Paper (Stützle & Hoos 2000)` --extends--> `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)`  [INFERRED]
  papers/02_mmas_stutzle_hoos_2000.md → papers/01_ant_system_dorigo_1996.md
- `BACO: Bloom-filter Enhanced ACO Algorithm` --extends--> `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)`  [INFERRED]
  papers/baco_encrypted_db_bloom_2018.md → papers/01_ant_system_dorigo_1996.md
- `IAPS: Improved Adaptive Probabilistic Search Algorithm` --uses--> `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)`  [INFERRED]
  papers/iaps_p2p_aco_bloom_2014.md → papers/01_ant_system_dorigo_1996.md
- `UTFACO: Unified Threat Identification and Fault Localization ACO` --uses--> `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)`  [INFERRED]
  papers/utfaco_dos_qos_2016.md → papers/01_ant_system_dorigo_1996.md
- `ACO Theory Survey (Dorigo & Blum 2005)` --surveys--> `MAX-MIN Ant System Paper (Stützle & Hoos 2000)`  [INFERRED]
  papers/06_aco_vrp_related.md → papers/02_mmas_stutzle_hoos_2000.md

## Hyperedges (group relationships)
- **ACO Algorithm Hierarchy** — AcoEngine_class, MmasEngine_class, AcoWithBloomFilter_class, AcoWithLocalSearch_class, MmasWithBloomFilter_class [EXTRACTED 1.00]
- **Bloom Filter + ACO Hybrid Engines** — MmasWithBloomFilter_class, AcoWithBloomFilter_class, BloomFilter_class [EXTRACTED 1.00]
- **Hashable Key Types for BloomFilter** — EdgeKey_class, PathKey_class, VisitKey_class, LoadContextKey_class [EXTRACTED 1.00]
- **Local Search 2-opt and Tabu** — LocalSearch_twoOpt, LocalSearch_tabu, AcoWithLocalSearch_class [EXTRACTED 1.00]
- **ACO + Bloom Filter Combined Applications** — baco2018_algorithm, iaps2014_algorithm, utfaco2016_algorithm, bloom1970_novel_application [EXTRACTED 1.00]
- **ACO Algorithm Family** — as1996_paper, mmas2000_paper, acovrp_acs1997_paper, readme_AcoEngine_class, readme_MmasEngine_class [EXTRACTED 1.00]
- **Local Search / Metaheuristic Techniques** — twoopt1965_paper, tabu1989_paper, readme_LocalSearch_class [EXTRACTED 1.00]
- **MMAS + Bloom Filter Integration (Novel Contribution)** — mmas2000_paper, bloom1970_paper, readme_MmasWithBloomFilter_class, mmas2000_lambda_branching, bloom1970_decay_operation, bloom1970_soft_penalty, bloom1970_project_load_context_key [EXTRACTED 1.00]
- **Pheromone Update Mechanisms Across ACO Variants** — as1996_pheromone_update, mmas2000_single_ant_update, mmas2000_pheromone_clipping, acovrp_acs_local_pheromone_update, acovrp_acs_global_update_best_ant [EXTRACTED 0.95]
- **ACO Applied to VRP Problem Variants** — acovrp_cvrp, acovrp_vrp_time_windows, acovrp_bell_mcmullen_2004_paper, acovrp_macs_vrptw_paper, acovrp_reimann_dants_2004_paper, readme_AcoEngine_class, readme_MmasEngine_class [EXTRACTED 1.00]
- **Bloom Filter Variants and Extensions** — bloom1970_paper, bloom1970_counting_variant, iaps2014_attenuated_bloom_filter, bloom1970_decay_operation [EXTRACTED 0.95]
- **Papers Co-authored by Dorigo** — as1996_paper, acovrp_acs1997_paper, acovrp_dorigo_blum_2005_survey, acovrp_dorigo_birattari_2006 [EXTRACTED 1.00]
- **Stagnation Detection and Handling in ACO** — mmas2000_lambda_branching, mmas2000_stagnation_reinit, bloom1970_novel_application, acovrp_adaptive_bf_activation, bloom1970_stagnation_window [EXTRACTED 1.00]
- **Bloom Filter as Search Space Pruning Tool** — baco2018_search_space_reduction, iaps2014_aco_bf_direction_pruning, utfaco2016_bf_explored_path_pruning, bloom1970_soft_penalty [INFERRED 0.90]
- **Project Java Implementation Classes** — readme_AcoEngine_class, readme_MmasEngine_class, readme_MmasWithBloomFilter_class, readme_AcoWithBloomFilter_class, readme_LocalSearch_class [EXTRACTED 1.00]

## Communities

### Community 0 - "ACO Survey & Network BF Papers"
Cohesion: 0.06
Nodes (37): Broder, A., Network Applications of Bloom Filters Survey (Broder & Mitzenmacher 2004), Mitzenmacher, M., AIMA 4th Ed. — Cycle Detection and Closed-List Filtering (Russell & Norvig 2020), BACO: Bloom-filter Enhanced ACO Algorithm, Amini, M., Bucket-based Encrypted Database, BF Pre-screening Reduces False Positives in Join Paths (+29 more)

### Community 1 - "AcoEngine Core Internals"
Cohesion: 0.16
Nodes (3): AcoEngine, LocalSearch, MmasEngine

### Community 2 - "BF Adaptive Activation & Decay"
Cohesion: 0.09
Nodes (26): Adaptive BF Activation on MMAS Stagnation, Birattari, M., ACO IEEE Computational Intelligence (Dorigo, Birattari, Stutzle 2006), Probabilistic BF Decay for Self-Reset, Novel: BF as Soft Penalty (not hard pruning) in CVRP, Counting Bloom Filter / Decay Variant, bloomFilter.decay(BF_DECAY=0.95) — Soft Forgetting, Novel ACO+Bloom Filter VRP Application (No Prior Paper) (+18 more)

### Community 3 - "AcoWithBloomFilter Internals"
Cohesion: 0.12
Nodes (4): AcoWithBloomFilter, BloomFilter, MmasWithBloomFilter, VisitKey

### Community 4 - "ACS Algorithm (Dorigo 1997)"
Cohesion: 0.09
Nodes (25): Ant Colony System Paper (Dorigo & Gambardella 1997), ACS Global Update Only by Best Ant, ACS Local Pheromone Update (During Construction), ACS Pseudo-Random Proportional Rule (Exploitation vs Exploration), Blum, C., ACO Theory Survey (Dorigo & Blum 2005), Gambardella, L. M., MACS-VRPTW Paper (Gambardella, Taillard, Agarwal 1999) (+17 more)

### Community 5 - "Local Search Papers & Classes"
Cohesion: 0.11
Nodes (21): LocalSearch Java Class, Tabu Search Paper (Glover 1986/1989/1990), 2-opt Paper (Lin 1965 / Croes 1958), Tabu Search Precursor (Glover 1986), Tabu Search Paper (Glover 1989), Tabu Search Part II (Glover 1990), Aspiration Criterion: Override tabu if better than best-so-far, Tabu Search Core: Neighborhood N(s) + Tabu List (+13 more)

### Community 6 - "Algorithm Class Hierarchy"
Cohesion: 0.15
Nodes (18): AcoEngine Class, AcoWithBloomFilter Class, AcoWithLocalSearch Class, BenchmarkRunner Class, BloomFilter Class, EdgeKey Class, Hashable Interface, LoadContextKey Class (+10 more)

### Community 7 - "BenchmarkRunner & Generator"
Cohesion: 0.33
Nodes (2): BenchmarkRunner, RandomVrpGenerator

### Community 8 - "Node & Distance Model"
Cohesion: 0.29
Nodes (2): Node, VrpProblem

### Community 9 - "EdgeKey Hashable"
Cohesion: 0.5
Nodes (1): EdgeKey

### Community 10 - "LoadContextKey Hashable"
Cohesion: 0.5
Nodes (1): LoadContextKey

### Community 11 - "PathKey Hashable"
Cohesion: 0.5
Nodes (1): PathKey

### Community 12 - "Project Config & Plugins"
Cohesion: 0.5
Nodes (4): Graphify Knowledge Graph Plugin, paper-verifier Agent, CLAUDE.md Project Configuration, security-guidance Hook

### Community 13 - "CVRP Literature (Bell 2004)"
Cohesion: 0.5
Nodes (4): Bell, J. E., Bell & McMullen 2004 ACO-VRP Paper, Capacitated VRP (CVRP), McMullen, P. R.

### Community 14 - "AcoWithLocalSearch"
Cohesion: 0.67
Nodes (1): AcoWithLocalSearch

### Community 15 - "Hashable Interface"
Cohesion: 0.67
Nodes (1): Hashable

### Community 16 - "VrpSolution"
Cohesion: 0.67
Nodes (1): VrpSolution

### Community 17 - "Novel BF+MMAS Contribution"
Cohesion: 0.67
Nodes (3): LoadContextKey(prev, cur, load_bucket, capacity), MMAS Stagnation-Triggered BF Activation, Novel ACO+Bloom Filter Soft Penalty Contribution

### Community 18 - "Claude Advisor & Implementer"
Cohesion: 1.0
Nodes (2): Advisor Pre/Post Review Hook, Claude Code Implementer Role

### Community 19 - "D-Ants Paper (Reimann 2004)"
Cohesion: 1.0
Nodes (2): Reimann, M., D-Ants Paper (Reimann, Doerner, Hartl 2004)

### Community 20 - "2-opt Method"
Cohesion: 1.0
Nodes (1): twoOpt Method

### Community 21 - "Tabu Search Method"
Cohesion: 1.0
Nodes (1): tabu Method

### Community 22 - "BloomFilter mightContain"
Cohesion: 1.0
Nodes (1): mightContain Method

### Community 23 - "BloomFilter decay"
Cohesion: 1.0
Nodes (1): decay Method

### Community 24 - "Source Directory Config"
Cohesion: 1.0
Nodes (1): src/ Java Source Directory

### Community 25 - "Output Directory Config"
Cohesion: 1.0
Nodes (1): out/ Build Output Directory

### Community 26 - "ACO-VRP Related Works"
Cohesion: 1.0
Nodes (1): ACO-VRP Related Papers

### Community 27 - "Ant System 1996"
Cohesion: 1.0
Nodes (1): Ant Colony (cooperating agents)

### Community 28 - "MMAS pBest Parameter"
Cohesion: 1.0
Nodes (1): p_best: probability best solution constructed (recommended: 0.05)

### Community 29 - "MMAS Lambda Parameter"
Cohesion: 1.0
Nodes (1): λ = 0.05 (branching factor threshold)

### Community 30 - "MMAS Branching Threshold"
Cohesion: 1.0
Nodes (1): BRANCHING_THRESHOLD = 2.0

### Community 31 - "MMAS tMax Initialization"
Cohesion: 1.0
Nodes (1): MMAS Initialization: All τ(i,j) = τ_max

### Community 32 - "2-opt O(n2) Complexity"
Cohesion: 1.0
Nodes (1): 2-opt Complexity: O(n²) per pass

### Community 33 - "Tabu maxIter Parameter"
Cohesion: 1.0
Nodes (1): Tabu maxIter = 50

### Community 34 - "BF No-Deletion Property"
Cohesion: 1.0
Nodes (1): No Deletion in Standard Bloom Filter

### Community 35 - "BF Space Efficiency"
Cohesion: 1.0
Nodes (1): Space Efficiency: ~9.6 bits/element at ε=1%

### Community 36 - "Bianchi 2006 Hybrid Paper"
Cohesion: 1.0
Nodes (1): Hybrid Metaheuristics VRP Stochastic Demands (Bianchi et al. 2006)

### Community 37 - "BACO Join Evaluation"
Cohesion: 1.0
Nodes (1): Bloom Filter Pre-evaluation of Bucket Join Feasibility

## Knowledge Gaps
- **100 isolated node(s):** `LocalSearch Utility Class`, `twoOpt Method`, `tabu Method`, `mightContain Method`, `decay Method` (+95 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Claude Advisor & Implementer`** (2 nodes): `Advisor Pre/Post Review Hook`, `Claude Code Implementer Role`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `D-Ants Paper (Reimann 2004)`** (2 nodes): `Reimann, M.`, `D-Ants Paper (Reimann, Doerner, Hartl 2004)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `2-opt Method`** (1 nodes): `twoOpt Method`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Tabu Search Method`** (1 nodes): `tabu Method`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BloomFilter mightContain`** (1 nodes): `mightContain Method`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BloomFilter decay`** (1 nodes): `decay Method`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Source Directory Config`** (1 nodes): `src/ Java Source Directory`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Output Directory Config`** (1 nodes): `out/ Build Output Directory`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ACO-VRP Related Works`** (1 nodes): `ACO-VRP Related Papers`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Ant System 1996`** (1 nodes): `Ant Colony (cooperating agents)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MMAS pBest Parameter`** (1 nodes): `p_best: probability best solution constructed (recommended: 0.05)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MMAS Lambda Parameter`** (1 nodes): `λ = 0.05 (branching factor threshold)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MMAS Branching Threshold`** (1 nodes): `BRANCHING_THRESHOLD = 2.0`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `MMAS tMax Initialization`** (1 nodes): `MMAS Initialization: All τ(i,j) = τ_max`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `2-opt O(n2) Complexity`** (1 nodes): `2-opt Complexity: O(n²) per pass`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Tabu maxIter Parameter`** (1 nodes): `Tabu maxIter = 50`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BF No-Deletion Property`** (1 nodes): `No Deletion in Standard Bloom Filter`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BF Space Efficiency`** (1 nodes): `Space Efficiency: ~9.6 bits/element at ε=1%`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Bianchi 2006 Hybrid Paper`** (1 nodes): `Hybrid Metaheuristics VRP Stochastic Demands (Bianchi et al. 2006)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BACO Join Evaluation`** (1 nodes): `Bloom Filter Pre-evaluation of Bucket Join Feasibility`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` connect `ACS Algorithm (Dorigo 1997)` to `ACO Survey & Network BF Papers`, `BF Adaptive Activation & Decay`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Why does `BACO: Bloom-filter Enhanced ACO Algorithm` connect `ACO Survey & Network BF Papers` to `BF Adaptive Activation & Decay`, `ACS Algorithm (Dorigo 1997)`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` (e.g. with `MAX-MIN Ant System Paper (Stützle & Hoos 2000)` and `Ant Colony System Paper (Dorigo & Gambardella 1997)`) actually correct?**
  _`Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `MAX-MIN Ant System Paper (Stützle & Hoos 2000)` (e.g. with `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` and `ACO Theory Survey (Dorigo & Blum 2005)`) actually correct?**
  _`MAX-MIN Ant System Paper (Stützle & Hoos 2000)` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `BACO: Bloom-filter Enhanced ACO Algorithm` (e.g. with `Ant System Paper (Dorigo, Maniezzo, Colorni 1996)` and `Novel ACO+Bloom Filter VRP Application (No Prior Paper)`) actually correct?**
  _`BACO: Bloom-filter Enhanced ACO Algorithm` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `LocalSearch Utility Class`, `twoOpt Method`, `tabu Method` to the rest of the system?**
  _100 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ACO Survey & Network BF Papers` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._