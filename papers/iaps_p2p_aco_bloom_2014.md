# An Efficient and Distributed File Search in Unstructured Peer-to-Peer Networks (IAPS)

## Citation
(저자 정보: Springer Peer-to-Peer Networking and Applications, 2014)  
DOI: 10.1007/s12083-013-0236-0  
Journal: *Peer-to-Peer Networking and Applications*, Springer  
Springer: https://link.springer.com/article/10.1007/s12083-013-0236-0

## 핵심 내용
- **IAPS** (Improved Adaptive Probabilistic Search) 알고리즘 제안
- 비구조화 P2P 네트워크에서 파일 유형을 고려한 분산 탐색
- **ACO + Attenuated Bloom Filter 결합**:
  - Attenuated Bloom Filter: 각 노드가 d개의 일반 Bloom Filter 배열을 유지
  - i번째 BF는 i-hop 거리에서 발견 가능한 파일 집합을 요약
  - ACO가 페로몬 기반으로 방향 선택 시 ABF를 통해 유망하지 않은 방향 조기 차단
- 통신 대역폭 낭비 대폭 감소, 검색 성공률 향상

## Attenuated Bloom Filter 동작 원리
```
노드 A → [BF_1 | BF_2 | BF_3 | ... | BF_d]
           1hop    2hop   3hop        d-hop
```
- BF_i: i-hop 이내에서 찾을 수 있는 파일 집합
- 탐색 시 해당 방향의 BF를 쿼리 → 없으면 해당 방향 ACO 탐색 생략

## 접근 방법
- DOI: 10.1007/s12083-013-0236-0
- 기관 구독 또는 Springer 접속 필요
- Academia.edu에 저자 공개본 존재 가능: https://www.academia.edu/4623884
