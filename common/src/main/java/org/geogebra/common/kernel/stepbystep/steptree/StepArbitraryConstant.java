package org.geogebra.common.kernel.stepbystep.steptree;

import org.geogebra.common.main.Localization;
import org.geogebra.common.plugin.Operation;

public class StepArbitraryConstant extends StepNode {

	public enum ConstantType {
		INTEGER, REAL
	}

	private String label;
	private int index;
	private ConstantType type;

	public StepArbitraryConstant(String label, int index, ConstantType type) {
		this.label = label;
		this.index = index;
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public int getIndex() {
		return index;
	}

	public ConstantType getType() {
		return type;
	}

	@Override
	public boolean equals(StepNode sn) {
		if (sn instanceof StepArbitraryConstant) {
			return ((StepArbitraryConstant) sn).label.equals(label) && ((StepArbitraryConstant) sn).index == index;
		}
		return false;
	}

	@Override
	public StepNode deepCopy() {
		StepArbitraryConstant sac = new StepArbitraryConstant(label, index, type);
		sac.setColor(color);
		return sac;
	}

	@Override
	public boolean isOperation() {
		return false;
	}

	@Override
	public boolean isOperation(Operation op) {
		return false;
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public boolean canBeEvaluated() {
		return false;
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public double getValue() {
		return Double.NaN;
	}

	@Override
	public double getValueAt(StepNode variable, double value) {
		if (equals(variable)) {
			return value;
		}
		return Double.NaN;
	}

	@Override
	public StepNode getCoefficient() {
		return this;
	}

	@Override
	public StepNode getVariable() {
		return null;
	}

	@Override
	public StepNode getIntegerCoefficient() {
		return null;
	}

	@Override
	public StepNode getNonInteger() {
		return this;
	}

	@Override
	public String toString() {
		return label + index;
	}

	@Override
	public String toLaTeXString(Localization loc) {
		return toLaTeXString(loc, false);
	}

	@Override
	public String toLaTeXString(Localization loc, boolean colored) {
		if (colored && color != 0) {
			return "\\fgcolor{" + getColorHex() + "}{" + toLaTeXString(loc, false) + "}";
		}
		return label + (index != 0 ? "_{" + index + "}" : "");
	}
}
