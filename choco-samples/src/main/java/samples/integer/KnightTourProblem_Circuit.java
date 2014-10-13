/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.integer;

import gnu.trove.list.array.TIntArrayList;
import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import samples.graph.input.HCP_Utils;
import solver.Solver;
import solver.constraints.ICF;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.search.strategy.selectors.IntValueSelector;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 * Solves the Knight's Tour Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class KnightTourProblem_Circuit extends AbstractProblem {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    @Option(name = "-tl", usage = "time limit.", required = false)
    private long limit = 30000;
    private IntVar[] succ;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public static void main(String[] args) {
        new KnightTourProblem_Circuit().execute(args);
    }

    @Override
    public void createSolver() {
        solver = new Solver("solving the Hamiltonian Cycle Problem");
    }

    @Override
    public void buildModel() {
        boolean[][] matrix = HCP_Utils.generateKingTourInstance(40);
        int n = matrix.length;
		succ = new IntVar[n];
		TIntArrayList values = new TIntArrayList();
        for (int i = 0; i < n; i++) {
			values.clear();
            for (int j = 0; j < n; j++) {
                if (matrix[i][j]) {
					values.add(j);
                }
            }
			succ[i] = VF.enumerated("succ_"+i,values.toArray(),solver);
        }
        solver.post(ICF.circuit(succ,0));
    }

    @Override
    public void configureSearch() {
		SMF.limitTime(solver, limit);
		solver.set(ISF.custom(
				ISF.minDomainSize_var_selector(),
				new IntValueSelector() {
					@Override
					public int selectValue(IntVar var) {
						int ub = var.getUB();
						int size = succ.length + 1;
						int val = -1;
						for (int j = var.getLB(); j <= ub; j = var.nextValue(j)) {
							if (succ[j].getDomainSize() < size) {
								val = j;
								size = succ[j].getDomainSize();
							}
						}
						return val;
					}
				},
				succ
		));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {}
}