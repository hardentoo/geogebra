/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * Area of polygon P[0], ..., P[n]
 *
 */

package geogebra.kernel.algos;

import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.kernel.Construction;
import geogebra.kernel.geos.GeoElement;
import geogebra.kernel.geos.GeoNumeric;

/**
 * Parent algorithm for commands that are functions of R^2 -> R
 * @author  Markus Hohenwarter
 * @version 
 */
public abstract class AlgoTwoNumFunction extends AlgoElement {

	protected NumberValue a, b;  // input
    protected GeoNumeric num;     // output           
        
    protected AlgoTwoNumFunction(Construction cons, String label, NumberValue a, NumberValue b) {       
  	  	this(cons, a, b);
        num.setLabel(label);
      }   
    
    protected AlgoTwoNumFunction(Construction cons, NumberValue a, NumberValue b) {       
  	  super(cons); 
        this.a = a;
        this.b = b;
        num = new GeoNumeric(cons); 
        setInputOutput(); // for AlgoElement
        
        // compute angle
        compute();     
      }   
    
    @Override
	public abstract String getClassName();
    
    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input =  new GeoElement[2];
        input[0] = (GeoElement)a.toGeoElement();
        input[1] = (GeoElement)b.toGeoElement();
        
        super.setOutputLength(1);
        super.setOutput(0, num);
        setDependencies(); // done by AlgoElement
    }    
    
    public GeoNumeric getResult() { return num; }        

    @Override
	public
	abstract void compute();     
}
