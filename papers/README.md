# Papers Reference

알고리즘 구현의 근거가 된 논문 목록.

| 파일 | 논문 | 적용 클래스 |
|------|------|-------------|
| `01_ant_system_dorigo_1996.md` | Dorigo et al. (1996) Ant System | `AcoEngine` |
| `02_mmas_stutzle_hoos_2000.md` | Stützle & Hoos (2000) MAX-MIN Ant System | `MmasEngine`, `MmasWithBloomFilter` |
| `03_two_opt_lin_1965.md` | Lin (1965) / Croes (1958) 2-opt | `LocalSearch.twoOpt()` |
| `04_tabu_search_glover_1989.md` | Glover (1986/1989/1990) Tabu Search | `LocalSearch.tabu()` |
| `05_bloom_filter_bloom_1970.md` | Bloom (1970) Bloom Filter + 프로젝트 적용 | `AcoWithBloomFilter`, `MmasWithBloomFilter` |
| `06_aco_vrp_related.md` | ACO-VRP 관련 논문 목록 | 전체 |

## 주요 발견
- ACO + Bloom Filter soft penalty를 (prev, cur, load_bucket) 키에 적용한 선행 연구는 존재하지 않음
- MMAS 정체 감지 시 BF를 활성화하고 decay로 자동 리셋하는 방식은 이 프로젝트의 독자적 기여
