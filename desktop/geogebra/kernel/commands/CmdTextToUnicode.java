package geogebra.kernel.commands;

import geogebra.common.main.MyError;
import geogebra.kernel.Kernel;
import geogebra.kernel.arithmetic.Command;
import geogebra.kernel.geos.GeoElement;
import geogebra.kernel.geos.GeoText;


public class CmdTextToUnicode extends CommandProcessor {

	public CmdTextToUnicode(Kernel kernel) {
		super(kernel);
	}

	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;
		arg = resArgs(c);
		
		switch (n) {
		case 1:

			if (arg[0].isGeoText()) {
				GeoElement[] ret = { 
						kernel.TextToUnicode(c.getLabel(),
						(GeoText) arg[0] ) };
				return ret;
			} else
				throw argErr(app, c.getName(), arg[0]);
		
		default:
			throw argNumErr(app, c.getName(), n);
		}
	}

}
