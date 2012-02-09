/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

/*
 * GeoVec2D.java
 *
 * Created on 31. August 2001, 11:34
 */

package geogebra.common.kernel.arithmetic;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoVec2D;
import geogebra.common.main.MyParseError;
//import geogebra.kernel.geos.GeoVec2D;

import java.util.HashSet;

/**
 * 
 * @author Markus
 * @version
 */
public class MyVecNode extends ValidExpression implements VectorValue,
		ReplaceableValue {

	protected ExpressionValue x;
	protected ExpressionValue y;
	private int mode = Kernel.COORD_CARTESIAN;
	protected Kernel kernel;

	/** Creates new MyVec2D */
	public MyVecNode(Kernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * Creates new MyVec2D with coordinates (x,y) as ExpresssionNodes. Both
	 * nodes must evaluate to NumberValues.
	 */
	public MyVecNode(Kernel kernel, ExpressionValue x, ExpressionValue y) {
		this(kernel);
		setCoords(x, y);
	}

	public ExpressionValue deepCopy(Kernel kernel) {
		return new MyVecNode(kernel, x.deepCopy(kernel), y.deepCopy(kernel));
	}

	public void resolveVariables() {
		x.resolveVariables();
		y.resolveVariables();
	}

	public ExpressionValue getX() {
		return x;
	}

	public ExpressionValue getY() {
		return y;
	}

	public ExpressionValue replace(ExpressionValue oldOb, ExpressionValue newOb) {
		if (x == oldOb) {
			x = newOb;
		} else if (x instanceof ReplaceableValue) {
			x = ((ReplaceableValue) x).replace(oldOb, newOb);
		}

		if (y == oldOb) {
			y = newOb;
		} else if (y instanceof ReplaceableValue) {
			y = ((ReplaceableValue) y).replace(oldOb, newOb);
		}

		return this;
	}

	public void setPolarCoords(ExpressionValue r, ExpressionValue phi) {
		setCoords(r, phi);
		mode = Kernel.COORD_POLAR;
	}

	public boolean hasPolarCoords() {
		return mode == Kernel.COORD_POLAR;
	}

	private void setCoords(ExpressionValue x, ExpressionValue y) {
		this.x = x;
		this.y = y;
	}

	final public double[] getCoords() {
		// check if both ExpressionNodes represent NumberValues
		ExpressionValue evx = x.evaluate();
		if (!evx.isNumberValue()) {
			String[] str = { "NumberExpected", evx.toString() };
			throw new MyParseError(kernel.getApplication(), str);
		}
		ExpressionValue evy = y.evaluate();
		if (!evy.isNumberValue()) {
			String[] str = { "NumberExpected", evy.toString() };
			throw new MyParseError(kernel.getApplication(), str);
		}

		if (mode == Kernel.COORD_POLAR) {
			double r = ((NumberValue) evx).getDouble();
			// allow negative radius for US
			double phi = ((NumberValue) evy).getDouble();
			double[] ret = { r * Math.cos(phi), r * Math.sin(phi) };
			return ret;
		}
		// CARTESIAN
		double[] ret = { ((NumberValue) evx).getDouble(),
				((NumberValue) evy).getDouble() };
		return ret;
	}

	@Override
	public String toString(StringTemplate tpl) {
		StringBuilder sb = new StringBuilder();
		double[] coords;

		switch (tpl.getStringType()) {
		case MATH_PIPER:
			coords = getCoords();
			sb.append("{");
			sb.append(coords[0]);
			sb.append(", ");
			sb.append(coords[1]);
			sb.append("}");
			break;

		case MAXIMA:
			coords = getCoords();
			sb.append("[");
			sb.append(coords[0]);
			sb.append(", ");
			sb.append(coords[1]);
			sb.append("]");
			break;

		case MPREDUCE:
			/*
			 * coords = getCoords(); sb.append("list("); sb.append(coords[0]);
			 * sb.append(", "); sb.append(coords[1]); sb.append(")"); break;
			 */
			if (mode == Kernel.COORD_POLAR) {
				sb.append("polartopoint!\u00a7(");
				sb.append(x.toString());
				sb.append(", ");
				sb.append(y.toString());
				sb.append(')');
			} else {
				sb.append("list(");
				sb.append(x.toString());
				sb.append(", ");
				sb.append(y.toString());
				sb.append(')');
			}
			break;

		default: // continue below
			sb.append('(');
			sb.append(x.isGeoElement() ? ((GeoElement) x).getLabel(tpl) : x
					.toString(tpl));
			if (mode == Kernel.COORD_CARTESIAN)
				sb.append(", ");
			else
				sb.append("; ");
			sb.append(y.isGeoElement() ? ((GeoElement) y).getLabel(tpl) : y
					.toString(tpl));
			sb.append(')');
			break;
		}

		return sb.toString();
	}

	public String toValueString(StringTemplate tpl) {
		return toString(tpl);
	}

	final public String toLaTeXString(boolean symbolic,StringTemplate tpl) {
		return toString(tpl);
	}

	/**
	 * interface VectorValue implementation
	 */
	public GeoVec2D getVector() {
		GeoVec2D ret = new GeoVec2D(kernel, getCoords());
		ret.setMode(mode);
		return ret;
	}

	public boolean isConstant() {
		return x.isConstant() && y.isConstant();
	}

	public boolean isLeaf() {
		return true;
	}

	/** POLAR or CARTESIAN */
	public int getMode() {
		return mode;
	}

	public ExpressionValue evaluate() {
		return this;
	}

	/** returns all GeoElement objects in the both coordinate subtrees */
	public HashSet<GeoElement> getVariables() {
		HashSet<GeoElement> temp, varset = x.getVariables();
		if (varset == null) {
			varset = new HashSet<GeoElement>();
		}
		temp = y.getVariables();
		if (temp != null)
			varset.addAll(temp);

		return varset;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	// could be vector or point
	public boolean isVectorValue() {
		return true;
	}

	public boolean isPoint() {
		return true;
	}

	public boolean isNumberValue() {
		return false;
	}

	public boolean isBooleanValue() {
		return false;
	}

	public boolean isPolynomialInstance() {
		return false;
	}

	public boolean isTextValue() {
		return false;
	}

	public boolean isListValue() {
		return false;
	}

	final public boolean isExpressionNode() {
		return false;
	}

	final public boolean contains(ExpressionValue ev) {
		return ev == this;
	}

	public boolean isVector3DValue() {
		// TODO Auto-generated method stub
		return false;
	}

	public String toOutputValueString(StringTemplate tpl) {
		return toValueString(tpl);
	}

	public Kernel getKernel() {
		return kernel;
	}

}
