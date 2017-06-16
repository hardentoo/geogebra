package org.geogebra.common.gui.view.algebra;

import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgorithmSet;
import org.geogebra.common.kernel.arithmetic.EquationValue;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.debug.Log;

public class SuggestionSolve extends Suggestion {
	
	public SuggestionSolve(String... labels) {
		super(labels);
	}

	@Override
	public String getCommand(Localization loc) {
		return loc.getCommand("Solve");
	}
	
	@Override
	public void execute(GeoElementND geo) {
		Log.printStacktrace(geo.getLabelSimple());
		geo.getKernel().getAlgebraProcessor().processAlgebraCommand(
				"Solve[" + getLabels(geo) + "]", true);
	}

	public static Suggestion get(GeoElement geo) {
		if (geo instanceof EquationValue && !hasDependentAlgo(geo)) {
			String[] vars = ((EquationValue) geo).getEquationVariables();
			if (vars.length == 1) {
				return new SuggestionSolve(geo.getLabelSimple());
			}
			if (vars.length == 2) {
				return getMulti(geo, vars);
			}
		}
		return null;
	}

	private static Suggestion getMulti(GeoElement geo, String[] vars) {
		GeoElementND prev = geo;
		do {
			prev = geo.getConstruction().getPrevious(prev);
			if (prev instanceof EquationValue && subset(
					((EquationValue) prev).getEquationVariables(), vars)) {
				return new SuggestionSolve(prev.getLabelSimple(),
						geo.getLabelSimple());
			}
		} while (prev != null);
		return null;
	}

	private static boolean hasDependentAlgo(GeoElement geo) {
		AlgorithmSet set = geo.getAlgoUpdateSet();
		for (AlgoElement algo : set) {
			if (algo != null && (algo.getClassName() == Commands.Solve
					|| algo.getClassName() == Commands.NSolve)) {
				return true;
			}
		}
		return false;
	}

	public static boolean subset(String[] testSet, String[] superset) {
		if (testSet.length < 1) {
			return false;
		}
		for (String check : testSet) {
			boolean found = false;
			for (String compare : superset) {
				found |= compare.equals(check);
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}