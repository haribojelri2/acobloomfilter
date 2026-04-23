package vrp;

import java.util.*;

// ACO + Local Search (Tabu 또는 2-opt) 데코레이터
public class AcoWithLocalSearch extends AcoEngine {
    public enum Mode { TABU, TWO_OPT, TWO_OPT_TABU }
    private final Mode mode;

    public AcoWithLocalSearch(VrpProblem problem, int numAnts, int maxIter,
                               double alpha, double beta, double rho, double q, Mode mode) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        this.mode = mode;
    }

    @Override
    protected List<List<Integer>> constructSolution() {
        List<List<Integer>> routes = super.constructSolution();
        if (mode == Mode.TABU) return LocalSearch.applyTabu(routes, problem);
        if (mode == Mode.TWO_OPT_TABU) return LocalSearch.applyTabu(LocalSearch.applyTwoOpt(routes, problem), problem);
        return LocalSearch.applyTwoOpt(routes, problem);
    }
}
