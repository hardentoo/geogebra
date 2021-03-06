package org.geogebra.common.kernel.stepbystep.solution;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.gui.view.algebra.StepGuiBuilder;
import org.geogebra.common.kernel.stepbystep.steptree.StepNode;
import org.geogebra.common.main.Localization;

public class SolutionStep {

	private Localization loc;

	/**
	 * The color of the solution step is either contained in the parameters
	 * themselves, or - when there is no parameter, but there is still need for a
	 * color (to signal for example the regrouping of constants), you have to pass a
	 * color. This will be represented as a dot after the text of the step.
	 */

	private SolutionStepType type;
	private StepNode[] parameters;
	private int color;

	private List<SolutionStep> substeps;

	public SolutionStep(Localization loc, SolutionStepType type, StepNode... parameters) {
		this.loc = loc;

		this.type = type;
		this.parameters = new StepNode[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			this.parameters[i] = parameters[i].deepCopy();
		}
	}

	public SolutionStep(Localization loc, SolutionStepType type, int color) {
		this.loc = loc;

		this.type = type;
		this.color = color;
	}

	/**
	 * Get the simple (no colors) text of the step
	 * 
	 * @return default text, formatted using LaTeX
	 */
	public String getDefault() {
		return type.getDefaultText(loc, parameters);
	}

	/**
	 * Get the detailed (colored) text of the step
	 * 
	 * @return colored text, formatted using LaTeX
	 */
	public String getColored() {
		return type.getDetailedText(loc, color, parameters);
	}

	/**
	 * Builds the solution tree using a StepGuiBuilder
	 * 
	 * @param builder
	 *            StepGuiBuilder to use (different for the web and for the tests)
	 */
	public void getListOfSteps(StepGuiBuilder builder) {
		if (substeps != null && type == SolutionStepType.WRAPPER) {
			for (int i = 0; i < substeps.size(); i++) {
				(substeps.get(i)).getListOfSteps(builder);
			}
		} else if (substeps != null && type == SolutionStepType.SUBSTEP_WRAPPER) {
			builder.addLatexRow(getColored());

			for (int i = 0; i < substeps.size(); i++) {
				(substeps.get(i)).getListOfSteps(builder);
			}
		} else {
			builder.addLatexRow(getColored());

			if (substeps != null) {
				builder.startGroup();
				for (int i = 0; i < substeps.size(); i++) {
					(substeps.get(i)).getListOfSteps(builder);
				}
				builder.endGroup();
			}
		}
	}

	/**
	 * Adds a substep
	 * 
	 * @param s
	 *            substep to add
	 */
	public void addSubStep(SolutionStep s) {
		if (s != null) {
			if (substeps == null) {
				substeps = new ArrayList<SolutionStep>();
			}

			substeps.add(s);
		}
	}

	public List<SolutionStep> getSubsteps() {
		return substeps;
	}

	public SolutionStepType getType() {
		return type;
	}
}
