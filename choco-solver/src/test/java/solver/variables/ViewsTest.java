/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.variables;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.constraints.set.SCF;
import solver.constraints.ternary.Max;
import solver.exception.ContradictionException;
import solver.search.strategy.ISF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.SetStrategyFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/08/11
 */
public class ViewsTest {

    public static void check(Solver ref, Solver solver, long seed, boolean strict, boolean solveAll) {
//        SearchMonitorFactory.log(ref, true, true);
//        SearchMonitorFactory.log(solver, true, true);
        if (solveAll) {
            ref.findAllSolutions();
            solver.findAllSolutions();
        } else {
//            System.out.printf("%s\n", ref.toString());
            ref.findSolution();
//            System.out.printf("%s\n", solver.toString());
            solver.findSolution();
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                ref.getMeasures().getSolutionCount(), "solutions (" + seed + ")");
//        System.out.printf("%d : %d vs. %d  -- ", seed, ref.getMeasures().getNodeCount(),
//                solver.getMeasures().getNodeCount());
        if (strict) {
            Assert.assertEquals(solver.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount(), "nodes (" + seed + ")");
        } else {
            Assert.assertTrue(ref.getMeasures().getNodeCount() >=
                    solver.getMeasures().getNodeCount(), seed + "");
        }
//        System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
//                solver.getMeasures().getTimeCount(),
//                ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
    }


    @Test(groups = "1m")
    public void test1() {
        // Z = X + Y
//        int seed = 5;
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 4, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y}, new int[]{1, 1}, z));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = VariableFactory.enumerated("Z", 0, 200, solver);
                solver.post(IntConstraintFactory.sum(new IntVar[]{x, y}, z));
                solver.set(IntStrategyFactory.random_value(new IntVar[]{x, y, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1a() {
        // Z = X + Y (bounded)
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.bounded("x", 0, 2, ref);
                IntVar y = VariableFactory.bounded("y", 0, 2, ref);
                IntVar z = VariableFactory.bounded("z", 0, 4, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y}, new int[]{1, 1}, z));
                ref.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = VariableFactory.bounded("x", 0, 2, solver);
                IntVar y = VariableFactory.bounded("y", 0, 2, solver);
                IntVar z = VariableFactory.enumerated("Z", 0, 200, solver);
                solver.post(IntConstraintFactory.sum(new IntVar[]{x, y}, z));
                solver.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void testa() {
        // Z = max(X + Y)
        for (int seed = 0; seed < 99999; seed += 1) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.bounded("z", 0, 2, ref);
                ref.post(IntConstraintFactory.maximum(z, x, y));
                ref.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Max.var(x, y);
                solver.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));

            }
            check(ref, solver, seed, false, true);
        }
    }

    @Test(groups = "1m")
    public void test1b() {
        // Z = |X|
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 2, ref);

                ref.post(IntConstraintFactory.absolute(z, x));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = VariableFactory.abs(x);
                solver.set(IntStrategyFactory.random_bound(new IntVar[]{x, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1bb() {
        // Z = X + c
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -1, 3, ref);

                ref.post(IntConstraintFactory.arithm(z, "=", x, "+", 1));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = VariableFactory.offset(x, 1);
                solver.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1bbb() {
        // Z = X * c
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -4, 4, ref);

                ref.post(IntConstraintFactory.times(x, VariableFactory.fixed(2, ref), z));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = VariableFactory.scale(x, 2);
                solver.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, solver, seed, false, true);
        }
    }

    @Test(groups = "1m")
    public void test1c() {
        // Z = -X
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 0, ref);

                ref.post(IntConstraintFactory.arithm(z, "+", x, "=", 0));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar z = VariableFactory.minus(x);
                solver.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1d() {
        // Z = X + Y + ...
        for (int seed = 2; seed < 9; seed += 1) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            int n = seed * 2;
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, ref);
                ref.post(IntConstraintFactory.sum(x, VariableFactory.fixed(n, solver)));
                ref.set(IntStrategyFactory.minDom_LB(x));
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, solver);
                IntVar[] y = new IntVar[seed];
                for (int i = 0; i < seed; i++) {
                    y[i] = VariableFactory.enumerated("Z", 0, 200, solver);
                    solver.post(IntConstraintFactory.sum(new IntVar[]{x[i], x[i + seed]}, y[i]));
                }
                solver.post(IntConstraintFactory.sum(y, VariableFactory.fixed(n, solver)));

                solver.set(IntStrategyFactory.minDom_LB(x));

            }
            check(ref, solver, seed, true, true);
        }
    }


    @Test(groups = "30s")
    public void test1f() {
        // Z = MAX(X,Y)
        Solver ref = new Solver();
        Solver solver = new Solver();
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, ref);
            IntVar y = VariableFactory.enumerated("y", -999, 999, ref);
            IntVar z = VariableFactory.enumerated("z", -9999, 9999, ref);
            ref.post(IntConstraintFactory.arithm(z, "+", x, "=", 180));
            ref.post(IntConstraintFactory.maximum(y, VariableFactory.fixed(0, ref), z));
        }
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, solver);
            IntVar y = VariableFactory.enumerated("y", -999, 999, solver);
            IntVar z = VariableFactory.offset(VariableFactory.minus(x), 180);
            solver.post(IntConstraintFactory.maximum(y, VariableFactory.fixed(0, solver), z));

            check(ref, solver, 0, false, true);
        }
    }


    @Test(groups = "1m")
    public void test2() {
        // Z = X - Y
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y}, new int[]{1, -1}, z));
                ref.set(IntStrategyFactory.random_value(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = VariableFactory.enumerated("Z", -200, 200, solver);
                solver.post(IntConstraintFactory.sum(new IntVar[]{z, y}, x));
                solver.set(IntStrategyFactory.random_value(new IntVar[]{x, y, z}, seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test3() {
        // Z = |X - Y|
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                IntVar az = VariableFactory.enumerated("az", 0, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y}, new int[]{1, -1}, z));
                ref.post(IntConstraintFactory.absolute(az, z));
                ref.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, az}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = VariableFactory.enumerated("Z", -2, 2, solver);
                IntVar az = VariableFactory.abs(z);
                solver.post(IntConstraintFactory.sum(new IntVar[]{z, y}, x));
                solver.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, az}, seed));
            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test4() {
        // Z = |X - Y| + AllDiff
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                IntVar az = VariableFactory.enumerated("az", 0, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y}, new int[]{1, -1}, z));
                ref.post(IntConstraintFactory.absolute(az, z));
                ref.post(IntConstraintFactory.alldifferent(new IntVar[]{x, y, az}, "BC"));
                ref.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, az}, seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar diff = VariableFactory.enumerated("diff", -2, 2, solver);
                solver.post(IntConstraintFactory.sum(new IntVar[]{diff, y}, x));
                IntVar z = VariableFactory.abs(diff);
                solver.post(IntConstraintFactory.alldifferent(new IntVar[]{x, y, z}, "BC"));
                solver.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));
            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1s")
    public void test5() {
        // ~all-interval series
        int k = 5;
        for (int seed = 0; seed < 99; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", k, 0, k - 1, ref);
                IntVar[] y = VariableFactory.enumeratedArray("y", k - 1, -(k - 1), k - 1, ref);
                IntVar[] t = VariableFactory.enumeratedArray("t", k - 1, 0, k - 1, ref);
                for (int i = 0; i < k - 1; i++) {
                    ref.post(IntConstraintFactory.scalar(new IntVar[]{x[i + 1], x[i]}, new int[]{1, -1}, y[i]));
                    ref.post(IntConstraintFactory.absolute(t[i], y[i]));
                }
                ref.post(IntConstraintFactory.alldifferent(x, "BC"));
                ref.post(IntConstraintFactory.alldifferent(t, "BC"));
                ref.post(IntConstraintFactory.arithm(x[1], ">", x[0]));
                ref.post(IntConstraintFactory.arithm(t[0], ">", t[k - 2]));
                ref.set(IntStrategyFactory.random_value(x, seed));
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", k, 0, k - 1, solver);
                IntVar[] t = new IntVar[k - 1];
                for (int i = 0; i < k - 1; i++) {
                    IntVar z = VariableFactory.enumerated("Z", -200, 200, solver);
                    solver.post(IntConstraintFactory.sum(new IntVar[]{z, x[i]}, x[i + 1]));
                    t[i] = VariableFactory.abs(z);
                }
                solver.post(IntConstraintFactory.alldifferent(x, "BC"));
                solver.post(IntConstraintFactory.alldifferent(t, "BC"));
                solver.post(IntConstraintFactory.arithm(x[1], ">", x[0]));
                solver.post(IntConstraintFactory.arithm(t[0], ">", t[k - 2]));
                solver.set(IntStrategyFactory.random_value(x, seed));
            }
            check(ref, solver, k, true, true);
        }
    }


    @Test(groups = "1s")
    public void test6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = VariableFactory.enumerated("x", 0, 10, solver);
        IntVar y = VariableFactory.abs(x);
        IntVar z = VariableFactory.abs(VariableFactory.abs(x));

        for (int j = 0; j < 200; j++) {
            long t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (y.getLB() == x.getUB()) {
                    y.updateLowerBound(0, Cause.Null);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms vs. ", t / 1000 / 1000f);
            t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (z.getLB() == x.getUB()) {
                    z.updateLowerBound(0, Cause.Null);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms\n", t / 1000 / 1000f);
        }
    }

    @Test(groups = "1s")
    public void testJL1() throws ContradictionException {
        Solver s = new Solver();
        IntVar v1 = VF.enumerated("v1", -2, 2, s);
        IntVar v2 = VF.minus(VF.minus(VF.enumerated("v2", -2, 2, s)));
        s.post(ICF.arithm(v1, "=", v2));
        s.post(ICF.arithm(v2, "!=", 1));

        s.propagate();

        Assert.assertFalse(v1.contains(1));
    }

    @Test(groups = "1s")
    public void testJL2() {
        Solver solver = new Solver();
        SetVar v1 = VF.fixed("{0,1}", new int[]{0, 1}, solver);
        SetVar v2 = VF.set("v2", 0, 3, solver);
        solver.post(SCF.subsetEq(new SetVar[]{v1, v2}));
        solver.set(SetStrategyFactory.force_first(new SetVar[]{v1, v2}));
        if (solver.findSolution()) {
            do {
                System.out.println(v1 + " subseteq " + v2);
            } while (solver.nextSolution());
        }
    }

	@Test(groups = "1s")
	public void testJL3(){
		Solver solver = new Solver();
		solver.post(ICF.arithm(
				VF.enumerated("int", -3,3, solver),
				"=",
				VF.minus(VF.bool("bool", solver))));
		if(solver.findSolution()) {
			do{
				System.out.println(solver);
			}while(solver.nextSolution());
		}
	}

	@Test(groups = "1s")
	public void testJL4() throws ContradictionException {
		//this test is unfixed yet

		// it illustrates that views can break idempotency and correctness of a propagator !
		
		// Here the set use the value 1, so the channeling propagator sets "bool" to true.
		// However, as views squize propagation, it sets to true "view" as well without
		// informing the channeling propagator.
	    Solver s = new Solver();

	    BoolVar bool = VF.bool("bool", s);
		BoolVar view = VF.eq(bool);

	    SetVar set = VF.set("set", 0, 1, s);

	    // Keep 1 bool not in EqView
		s.post(SCF.bool_channel(new BoolVar[]{view, bool}, set, 0));

	    s.post(SCF.member(VF.one(s), set));
	    s.set(ISF.minDom_UB(bool));

	    if (s.findSolution()) {
	        do {
	            System.out.println(bool + " : " + set + " : " + s.isSatisfied());
	        } while (s.nextSolution());
	    }
	}

	@Test(groups = "1s")
	public void testJG() throws ContradictionException {
		//this test is unfixed yet
		// it illustrates that views can break idempotency and correctness of a propagator !
	    Solver s = new Solver();
	    BoolVar bool = VF.bool("bool", s);
		BoolVar view = VF.eq(bool);
		IntVar sum = VF.bounded("sum",0,6,s);

		s.post(ICF.scalar(new IntVar[]{view, bool}, new int[]{1,5}, sum));
	    s.post(ICF.arithm(sum, ">", 2));

		s.propagate();

		Assert.assertEquals(sum.isInstantiated(), true);

		/** What currently happens :
		 * sum > 2 implies sum = [3,7]
		 * so the scalar sets to 1 "bool" (coef=5)
		 * As "view" is an eqView of "bool", it is set to 1 as well
		 * However, it informs its propagators that it changes because of the scalar
		 * This is false. The real cause is the equality constraint hidden inside the view.
		 * Therefore the scalar does not know that "view" has changes,
		 * so it does not fix the sum to 6
		 * ...
		 * I believe we the cause should be the view...
		 */
	}
}
