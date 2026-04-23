import vrp.*;
import java.util.*;
public class MmasOnly {
    static final double ALPHA=1,BETA=2,RHO_MMAS=0.02,Q=100,T_MAX=10,T_MIN=0.01,P_BEST=0.05;
    static final int RUNS=30;
    public static void main(String[] a) throws Exception {
        VrpParser.ParsedInstance inst = VrpParser.parse("benchmark_data/Golden/Golden/Golden_11.vrp");
        VrpProblem p = inst.problem; int opt = inst.optimalValue;
        System.out.println("N="+p.size()+" opt="+opt);
        run("MMAS",    () -> new MmasEngine(p,30,100,ALPHA,BETA,RHO_MMAS,Q,T_MAX,T_MIN,P_BEST), p, opt);
        run("MMAS+BF", () -> new MmasWithBloomFilter(p,30,100,ALPHA,BETA,RHO_MMAS,Q,T_MAX,T_MIN,P_BEST), p, opt);
    }
    interface F { AcoEngine get(); }
    static void run(String lbl, F f, VrpProblem p, int opt) throws Exception {
        double sum=0, best=Double.MAX_VALUE;
        var field = AcoEngine.class.getDeclaredField("rng"); field.setAccessible(true);
        for(int r=0;r<RUNS;r++){
            AcoEngine e=f.get(); field.set(e,new Random(r));
            double g=(e.solve().totalDistance-opt)/opt*100;
            sum+=g; if(g<best)best=g;
        }
        System.out.printf("%-12s avg=%6.2f%%  best=%6.2f%%%n",lbl,sum/RUNS,best);
    }
}
