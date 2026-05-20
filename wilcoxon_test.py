import os
import csv
from scipy import stats

SIZES = ["50", "100", "200", "500", "1000", "2000"]

PAIRS = [
    ("ACO_Tabu", "TabuLikeBF_fpr=0.001", "Tabu vs BF(fpr=0.001)"),
    ("ACO_Tabu", "TabuLikeBF_fpr=0.01",  "Tabu vs BF(fpr=0.01) "),
    ("ACO_Tabu", "TabuLikeBF_fpr=0.05",  "Tabu vs BF(fpr=0.05) "),
    ("ACO_Tabu", "TabuLikeBF_fpr=0.20",  "Tabu vs BF(fpr=0.20) "),
]
METRICS = ["best_cost", "time_sec"]

def load_data(size, algo, metric):
    path = f"out/seeds_{size}_{algo}.csv"
    if not os.path.exists(path):
        return None
    with open(path) as f:
        reader = csv.DictReader(f)
        rows = list(reader)
        if metric not in rows[0]:
            return None
        return [float(row[metric]) for row in rows]

for metric in METRICS:
    print(f"\n{'='*95}")
    print(f"  Metric: {metric}")
    print(f"{'='*95}")
    print(f"{'Comparison':<30} {'Size':>6} {'Tabu_avg':>12} {'BF_avg':>12} {'p-value':>10} {'sig':>5}  winner")
    print("-" * 95)

    for a_algo, b_algo, label in PAIRS:
        for size in SIZES:
            a = load_data(size, a_algo, metric)
            b = load_data(size, b_algo, metric)
            if a is None or b is None:
                continue
            stat, p = stats.wilcoxon(a, b, alternative='two-sided')
            sig = "***" if p < 0.001 else "**" if p < 0.01 else "*" if p < 0.05 else "ns"
            a_avg = sum(a) / len(a)
            b_avg = sum(b) / len(b)
            winner = "Tabu" if a_avg < b_avg else "BF"
            print(f"{label:<30} {size:>6} {a_avg:>12.4f} {b_avg:>12.4f} {p:>10.4f} {sig:>5}  {winner}")
        print()
