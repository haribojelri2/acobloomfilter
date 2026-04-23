# Multi-join Query Optimization in Bucket-based Encrypted Databases Using BACO

## Citation
Jafarinejad, S., & Amini, M. (2018).  
**Multi-join query optimization in bucket-based encrypted databases using an enhanced ant colony optimization algorithm.**  
*Distributed and Parallel Databases*, Springer.  
DOI: 10.1007/s10619-018-7220-x  
Springer: https://link.springer.com/article/10.1007/s10619-018-7220-x

## 핵심 내용
- **BACO** (Bloom-filter enhanced ACO) 알고리즘 제안
- 버킷 기반 암호화 데이터베이스에서 다중 조인(Multi-join) 쿼리 최적화
- 버킷 기반 암호화의 핵심 문제: False-positive 결과 과다 발생
- **Bloom Filter 결합**:
  - 불필요한 조인 경로를 ACO 탐색 전에 Bloom Filter로 조기 차단
  - False-positive 비율 대폭 감소
  - ACO가 탐색해야 할 조인 순서 공간 축소

## 문제 배경
- 외부 서버에 outsourcing된 DB는 암호화 필수
- 버킷 기반 암호화: 유사 값들을 동일 버킷으로 묶어 보안↑ 하지만 FP↑
- 기존 ACO만으로는 FP로 인한 불필요한 조인 경로 탐색 과다

## BACO 작동 방식
1. Bloom Filter로 각 버킷 간 조인 가능성 사전 평가
2. FP 가능성 높은 조인 경로 → 페로몬 패널티 부여 또는 탐색 제외
3. ACO가 남은 유망한 조인 순서만 탐색 → 처리 비용 절감

## 접근 방법
- DOI: 10.1007/s10619-018-7220-x
- 기관 구독 또는 Springer 접속 필요
