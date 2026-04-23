import os
import csv
from scipy import stats

SIZES = ["50", "100", "200", "500", "1000", "2000", "4000"]
PAIRS = [
    ("ACO_Tabu",       "ACO_TabuLikeBF",       "Tabu vs TabuLikeBF"),
    ("ACO_2opt_Tabu",  "ACO_2opt_TabuLikeBF",  "2opt+Tabu vs 2opt+TabuLikeBF"),
    ("ACO",            "ACO_TabuLikeBF",        "ACO vs TabuLikeBF"),
    ("ACO_2opt",       "ACO_2opt_TabuLikeBF",  "2opt vs 2opt+TabuLikeBF"),
]

def load_costs(size, algo):
    path = f"out/seeds_{size}_{algo}.csv"
    if not os.path.exists(path):
        return None
    with open(path) as f:
        reader = csv.DictReader(f)
        return [float(row["best_cost"]) for row in reader]

print(f"{'Comparison':<35} {'Size':>6} {'A_avg':>12} {'B_avg':>12} {'p-value':>10} {'sig':>5}")
print("-" * 85)

for a_algo, b_algo, label in PAIRS:
    for size in SIZES:
        a = load_costs(size, a_algo)
        b = load_costs(size, b_algo)
        if a is None or b is None:
            continue
        stat, p = stats.wilcoxon(a, b, alternative='two-sided')
        sig = "***" if p < 0.001 else "**" if p < 0.01 else "*" if p < 0.05 else "ns"
        a_avg = sum(a) / len(a)
        b_avg = sum(b) / len(b)
        winner = "<" if a_avg < b_avg else ">"
        print(f"{label:<35} {size:>6} {a_avg:>12.2f} {b_avg:>12.2f} {p:>10.4f} {sig:>5}  {winner}")
    print()
