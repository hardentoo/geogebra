/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel.geos;

import geogebra.common.kernel.AbstractConstruction;
import geogebra.common.kernel.MatrixTransformable;
import geogebra.common.kernel.Path;
import geogebra.common.kernel.PathMover;
import geogebra.common.kernel.PathMoverGeneric;
import geogebra.common.kernel.PathParameter;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.arithmetic.ExpressionValue;
import geogebra.common.kernel.arithmetic.MyDouble;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.Dilateable;
import geogebra.common.kernel.geos.GeoClass;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPointInterface;
import geogebra.common.kernel.geos.GeoLineInterface;
import geogebra.common.kernel.geos.GeoVec3D;
import geogebra.common.kernel.geos.LineProperties;
import geogebra.common.kernel.geos.Mirrorable;
import geogebra.common.kernel.geos.PointRotateable;
import geogebra.common.kernel.geos.Traceable;
import geogebra.common.kernel.geos.Transformable;
import geogebra.common.kernel.geos.Translateable;
import geogebra.common.kernel.kernelND.GeoPointND;

import java.util.HashSet;

/**
 * PolyLine (open Polygon) through given points
 * 
 * @author Michael Borcherds, adapted from GeoPolygon
 */
public class GeoPolyLine extends GeoElement implements NumberValue, Path, Traceable, LineProperties,Transformable,
 Mirrorable, MatrixTransformable, PointRotateable, Translateable, Dilateable,GeoPolyLineInterface{
	
	/** maximum number of points when created by tool */
	public static final int POLYLINE_MAX_POINTS = 500;
	/** array of vertices */
	protected GeoPointND [] points;
	/** length of the line */
	protected double length;
	private boolean defined = false;		
	
	/** common constructor for 2D.
	 * @param cons the construction
	 * @param label
	 * @param points vertices 
	 */
	public GeoPolyLine(AbstractConstruction cons, String label, GeoPointND[] points) {
		this(cons, points);
		setLabel(label);
	}

	/**
	 * @param cons
	 * @param points
	 */
	public GeoPolyLine(AbstractConstruction cons, GeoPointND[] points) {
		super(cons);
		this.points = points;
		
		// moved from GeoElement's constructor
		// must be called from the subclass, see
		//http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
		setConstructionDefaults(); // init visual settings

	}

	public String getClassName() {
		return "GeoPolyLine";
	}
	
    protected String getTypeString() {
    		return "PolyLine";	    			
	}
    
    public GeoClass getGeoClassType() {
    	return GeoClass.POLYLINE;
    }
    
	/** return number for points
	 * @return number for points
	 */
	public int getNumPoints(){
		return points.length;
	}
	
	/**
	 * The copy of a polygon is a number (!) with
	 * its value set to the polygons current area
	 */      
	public GeoElement copy() {
		return new GeoNumeric(cons, getLength());        
	}    
	
	public GeoElement copyInternal(AbstractConstruction cons) {						
		GeoPolyLine ret = new GeoPolyLine(cons, null); 
		ret.points = GeoElement.copyPoints(cons, (GeoPoint2[]) points);		
		ret.set(this);
				
		return ret;		
	} 		
	
	public void set(GeoElement geo) {
		GeoPolyLine poly = (GeoPolyLine) geo;		
		length = poly.length;
		defined = poly.defined;	
		
		// make sure both arrays have same size
		if (points.length != poly.points.length) {
			GeoPointND [] tempPoints = new GeoPointND[poly.points.length];
			for (int i=0; i < tempPoints.length; i++) {
				tempPoints[i] = i < points.length ? points[i] : new GeoPoint2(cons);
			}
			points = tempPoints;
		}
		
		for (int i=0; i < points.length; i++) {				
			((GeoPoint2) points[i]).set(poly.points[i]);
		}	
	}
	
	public boolean isFillable() {
		return false;
	}	

	/*
	 * overwrite methods
	 */
	public boolean isDefined() {
		return defined;
   }	
   
   /**
    * Make the polyline defined 
 	*/
	public void setDefined() {
   		defined = true;
   }
   
   public void setUndefined() {
		   defined = false;
	}
        
   public final boolean showInAlgebraView() {	   
	   //return defined;
	   return true;
   }
   
	// Michael Borcherds 2008-04-30
	final public boolean isEqual(GeoElement geo) {
		//TODO
		return false;
	}

	
   final public String toString() {
		sbToString.setLength(0);
		sbToString.append(label);
		sbToString.append(" = ");
		sbToString.append(kernel.format( getLength() ));
	    return sbToString.toString();
   }      
   private StringBuilder sbToString = new StringBuilder(50);
   
   final public String toValueString() {
	   return kernel.format(getLength());
   }

	 /**
     * interface NumberValue
     */    
    public MyDouble getNumber() {    	
        return new MyDouble(kernel,  getLength() );
    }     
    /**
     * @return length of the polyline
     */
    final public double getLength() {
        return length;
    }   
        
    final public double getDouble() {
        return getLength();
    }   
        
    final public boolean isConstant() {
        return false;
    }
    
    final public boolean isLeaf() {
        return true;
    }
    
    final public HashSet<GeoElement> getVariables() {
    	HashSet<GeoElement> varset = new HashSet<GeoElement>();        
        varset.add(this);        
        return varset;          
    }                   
    
    final public ExpressionValue evaluate() { return this; }	

	protected boolean showInEuclidianView() {
		return defined;
	}    
	
	public boolean isNumberValue() {
		return true;
	}

	public boolean isVectorValue() {
		return false;
	}

	public boolean isPolynomialInstance() {
		return false;
	}   
	
	public boolean isTextValue() {
		return false;
	}   
	
	public boolean isGeoPolygon() {
		return false;
	}
	
	public boolean isGeoPolyLine() {
		return true;
	}
	
	/*
	 * Path interface implementation
	 */
	
	public boolean isPath() {
		return true;
	}

	public PathMover createPathMover() {
		return new PathMoverGeneric(this);
	}

	public double getMaxParameter() {
		return points.length - 1;
	}

	public double getMinParameter() {		
		return 0;
	}

	public boolean isClosedPath() {
		if (!isDefined())
			return false;
		else
			return points[0]==points[points.length-1];
		//return false;
	}
	
	// dummy segment to use in calculations
	private GeoSegment seg = new GeoSegment(cons);

	public boolean isOnPath(GeoPointND PI, double eps) {

		GeoPoint2 P = (GeoPoint2) PI;
		
		if (P.getPath() == this)
			return true;
		
		// check if P is on one of the segments
		for (int i=0; i < points.length - 1; i++) {
			setSegmentPoints((GeoPoint2)points[i], (GeoPoint2)points[i + 1]);
			if (seg.isOnPath(P, eps))
				return true;
		}				
		return false;
	}

	public void pathChanged(GeoPointND PI) {		
		
		GeoPoint2 P = (GeoPoint2) PI;
		
		// parameter is between 0 and points.length - 1,
		// i.e. floor(parameter) gives the point index
		int index;
		PathParameter pp = P.getPathParameter();
		if (pp.t == points.length - 1) { // at very end of path
			index = points.length - 2;
		} else {
			pp.t = pp.t % (points.length - 1);
			if (pp.t < 0) 
				pp.t += (points.length - 1);
			index = (int) Math.floor(pp.t) ;	
		}
		setSegmentPoints((GeoPoint2)points[index], (GeoPoint2)points[index + 1]);
		
		double segParameter = pp.t - index;
		
		// calc point for given parameter
		P.x = seg.getPointX(segParameter);
		P.y = seg.getPointY(segParameter);
		P.z = 1.0;	
	}

	public void pointChanged(GeoPointND PI) {
		
		GeoPoint2 P = (GeoPoint2) PI;
		
		double qx = P.x/P.z;
		double qy = P.y/P.z;
		double minDist = Double.POSITIVE_INFINITY;
		double resx=0, resy=0, resz=0, param=0;
		
		// find closest point on each segment
		PathParameter pp = P.getPathParameter();
		for (int i=0; i < points.length - 1; i++) {
			P.x = qx;
			P.y = qy;
			P.z = 1;

			setSegmentPoints((GeoPoint2)points[i], (GeoPoint2)points[i + 1]);
	    	
			seg.pointChanged(P);
		
			double x = P.x/P.z - qx; 
			double y = P.y/P.z - qy;
			double dist = x*x + y*y;			
			if (dist < minDist) {
				minDist = dist;
				// remember closest point
				resx = P.x;
				resy = P.y;
				resz = P.z;
				param = i + pp.t;
			}
		}				
			
		P.x = resx;
		P.y = resy;
		P.z = resz;
		pp.t = param;	
	}	 
	
	private void setSegmentPoints(GeoPoint2 geoPoint, GeoPoint2 geoPoint2) {
		seg.setStartPoint(geoPoint);
		seg.setEndPoint(geoPoint2);
    	GeoVec3D.lineThroughPoints(geoPoint, geoPoint2, seg);      	
    	seg.calcLength();
		
	}


	/**
	 * returns all class-specific xml tags for getXML
	 * GeoGebra File Format
	 */
	protected void getXMLtags(StringBuilder sb) {
		getLineStyleXML(sb);
		getXMLvisualTags(sb);
		getXMLanimationTags(sb);
		getXMLfixedTag(sb);
		getAuxiliaryXML(sb);
		getBreakpointXML(sb);		
		getScriptTags(sb);
	}

	public final GeoPointND[] getPoints() {
		return points;
	}

	public GeoPointND[] getPointsND() {
		return points;
	}
	
	public GeoPointND getPointND(int i) {
		return points[i];
	}
	
	/**
	 * Recompute length of this polyline
	 */
	public void calcLength() {
		
		// last point not checked in loop
		if (!((GeoPoint2)points[points.length - 1]).isDefined()) {
			setUndefined();
			length = Double.NaN;
			return;
		}
		
		length = 0;
		
		for (int i=0; i < points.length - 1; i++) {
			if (!((GeoPoint2)points[i]).isDefined()) {
				setUndefined();
				length = Double.NaN;
				return;
			}
			setSegmentPoints((GeoPoint2)points[i], (GeoPoint2)points[i + 1]);
			length += seg.getLength();
		}
		setDefined();
	}

	/**
	 * Set vertices of the polyline
	 * @param points new vertices
	 */
	public void setPoints(GeoPointND[] points) {
		this.points = points;		
	}

	public void rotate(NumberValue r) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).rotate(r);
		}
	}

	public void rotate(NumberValue r, GeoPointInterface S) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).rotate(r,S);
		}
	}

	public void matrixTransform(double a00, double a01, double a10, double a11) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).matrixTransform(a00, a01, a10, a11);
		}
		calcLength();
		
	}

	public void translate(Coords v) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).translate(v);
		}
	}

	public void dilate(NumberValue r, GeoPointInterface S) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).dilate(r,S);
		}
		calcLength();
	}
	
	public void mirror(GeoPointInterface Q) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).mirror(Q);
		}		
	}

	public void mirror(GeoLineInterface g) {
		for(int i=0;i<points.length;i++) {
			((GeoPoint2)points[i]).mirror(g);
		}
	}


	public boolean isAllVertexLabelsSet() {
		for(int i=0;i<points.length;i++) {
			if(!((GeoPoint2)points[i]).isLabelSet()) return false;
		}
		return true;
	}


	public boolean isVertexCountFixed() {		
		if(getParentAlgorithm().getInput().length<3) return false;
		return true;
	}
	
	   private boolean trace;


		public boolean isTraceable() {
			return true;
		}

		public void setTrace(boolean trace) {
			this.trace = trace;
		}

		public boolean getTrace() {
			return trace;
		}
		public void matrixTransform(double a00, double a01, double a02, double a10,
				double a11, double a12, double a20, double a21, double a22) {
			for(int i=0;i<points.length;i++) {
				((GeoPoint2)points[i]).matrixTransform(a00, a01, a02, a10, a11, a12, a20, a21, a22);
			}
		}

		public GeoPoint2 getPoint(int i) {
			return (GeoPoint2)points[i];
		}
		
		public  void toGeoCurveCartesian(GeoCurveCartesian curve){
			curve.setFromPolyLine(points, false);
	    }


		public Path getBoundary() {
			return this;
		}
		

}
