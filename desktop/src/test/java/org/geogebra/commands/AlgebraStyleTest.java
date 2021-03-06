package org.geogebra.commands;

import java.util.Locale;

import org.geogebra.common.gui.view.algebra.AlgebraItem;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.EquationValue;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.geos.DescriptionMode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoConicND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.IndexHTMLBuilder;
import org.geogebra.desktop.main.AppDNoGui;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.himamis.retex.editor.share.util.Unicode;

public class AlgebraStyleTest extends Assert {
	static AppDNoGui app;
	static AlgebraProcessor ap;




	private static void checkRows(String def, int rows) {
		GeoElementND[] el = ap.processAlgebraCommandNoExceptionHandling(def,
				false,
				TestErrorHandler.INSTANCE, false, null);
		assertEquals(DescriptionMode.values()[rows],
				el[0].needToShowBothRowsInAV());
	}

	private static void checkEquation(String def, int mode, String check) {
		GeoElementND[] el = ap.processAlgebraCommandNoExceptionHandling(def,
				false, TestErrorHandler.INSTANCE, false, null);
		((GeoConicND) el[0]).setToStringMode(mode);
		assertEquals(check.replace("^2", Unicode.SUPERSCRIPT_2 + ""),
				el[0].toValueString(StringTemplate.defaultTemplate));
	}

	
	@Before
	public void resetSyntaxes(){
		app.getKernel().clearConstruction(true);
		app.getKernel()
				.setAlgebraStyle(Kernel.ALGEBRA_STYLE_DEFINITION_AND_VALUE);
	}
	
	@BeforeClass
	public static void setupApp() {
		app = new AppDNoGui(new LocalizationD(3), false);
		app.setLanguage(Locale.US);
		ap = app.getKernel().getAlgebraProcessor();
		// make sure x=y is a line, not plane
		app.getGgbApi().setPerspective("1");
	    // Setting the general timeout to 11 seconds. Feel free to change this.
		app.getKernel().getApplication().getSettings().getCasSettings().setTimeoutMilliseconds(11000);
	}

	
	@Test
	public void twoRowsAlgebra() {
		checkRows("a=1", 1);
		checkRows("a+a", 2);
		checkRows("sqrt(x+a)", 2);
		checkRows("{a}", 2);
		checkRows("{x}", 1);
		checkRows("{x+a}", 2);
		checkRows("{{1}}", 1);
		checkRows("{{a}}", 2);
		checkRows("{{a}}+{{1}}", 2);
		checkRows("{x=y}", 1);
		checkRows("x=y", 0);
		checkRows("{y=x}", 1);
		checkRows("Sequence[100]", 2);

	}

	@Test
	public void checkEquationExplicit() {
		checkEquation("x^2+4*y^2=1", GeoConicND.EQUATION_EXPLICIT,
				"x^2 + 4y^2 = 1");
		checkEquation("x^2+4*y^2-y+x*y=x +x -1", GeoConicND.EQUATION_EXPLICIT,
				"x^2 + x y + 4y^2 - 2x - y = -1");
		checkEquation("-x^2=x +x -1", GeoConicND.EQUATION_EXPLICIT,
				"-x^2 - 2x = -1");
	}

	@Test
	public void checkEquationVertex() {
		// ellipse: fallback to explicit
		checkNonParabolaFallback(GeoConicND.EQUATION_VERTEX);
		// three actual parabolas
		checkEquation("-x^2=x +x -1+y", GeoConicND.EQUATION_VERTEX,
				"y = -(x + 1)^2 +2");
		checkEquation("x^2=x +x -1+y", GeoConicND.EQUATION_VERTEX,
				"y = (x - 1)^2");
		checkEquation("y^2=y +y -1+x", GeoConicND.EQUATION_VERTEX,
				"(x - 0) = (y - 1)^2");
	}

	@Test
	public void checkEquationSpecific() {
		// ellipse
		checkEquation("x^2+4*y^2=1", GeoConicND.EQUATION_SPECIFIC,
				"x^2 / 1 + y^2 / 0.25 = 1");
		// hyperbola
		checkEquation("x^2-4*y^2=2x+2y+1", GeoConicND.EQUATION_SPECIFIC,
				"(x - 1)^2 / 1.75 - (y + 0.25)^2 / 0.44 = 1");
		// double line
		checkEquation("-x^2=x +x -1", GeoConicND.EQUATION_SPECIFIC,
				"(-x - 2.41) (-x + 0.41) = 0");
		// parabolas
		checkEquation("-x^2-x=x -1+y", GeoConicND.EQUATION_SPECIFIC,
				"x^2 = -2x - y + 1");
		checkEquation("y^2=x +x -1+y", GeoConicND.EQUATION_SPECIFIC,
				"y^2 = 2x + y - 1");
		checkEquation("(x+y)^2=x +x -1+y", GeoConicND.EQUATION_SPECIFIC,
				"x^2 + 2x y + y^2 - 2x - y = -1");
	}

	@Test
	public void checkEquationConicform() {
		checkNonParabolaFallback(GeoConicND.EQUATION_CONICFORM);
		// parabolas
		checkEquation("-x^2-x=x -1+y", GeoConicND.EQUATION_CONICFORM,
				"-(y - 2) = (x + 1)^2");
		checkEquation("y^2=x +x -1+y", GeoConicND.EQUATION_CONICFORM,
				"2(x - 0.38) = (y - 0.5)^2");
		checkEquation("(x+y)^2=x +x -1+y", GeoConicND.EQUATION_CONICFORM,
				"x^2 + 2x y + y^2 - 2x - y = -1");
	}

	@Test
	public void checkEquationParametric() {
		// ellipse
		checkEquation("x^2+4*y^2=1", GeoConicND.EQUATION_PARAMETRIC,
				"X = (0, 0) + (cos(t), 0.5 sin(t))");
		// hyperbola
		checkEquation("x^2-4*y^2=2x+2y+1", GeoConicND.EQUATION_PARAMETRIC,
				"X = (1, -0.25) + (" + Unicode.PLUSMINUS
						+ " 1.32 cosh(t), 0.66 sinh(t))");
		// double line TODO wrong
		checkEquation("-x^2=x +x -1", GeoConicND.EQUATION_PARAMETRIC,
				"X = (-1 " + Unicode.PLUSMINUS + " 1.41, 0, 0) + "
						+ Unicode.lambda + " (0, 1, 0)");
		// parabolas
		checkEquation("-x^2-x=x -1+y", GeoConicND.EQUATION_PARAMETRIC,
				"X = (-1, 2) + (-0.5 t, -0.25 t^2)");
		checkEquation("y^2=x +x -1+y", GeoConicND.EQUATION_PARAMETRIC,
				"X = (0.38, 0.5) + (0.5 t^2, t)");
		checkEquation("(x+y)^2=x +x -1+y", GeoConicND.EQUATION_PARAMETRIC,
				"X = (0.81, -0.06) + (0.06 t^2 + 0.13 t, -0.06 t^2 + 0.13 t)");
	}

	@Test
	public void checkEquationImplicit() {
		// ellipse
		checkEquation("x^2+4*y^2=1", GeoConicND.EQUATION_IMPLICIT,
				"x^2 + 4y^2 = 1");
		// hyperbola
		checkEquation("x^2-4*y^2=2x+2y+1", GeoConicND.EQUATION_IMPLICIT,
				"x^2 - 4y^2 - 2x - 2y = 1");
		// double line TODO wrong
		checkEquation("-x^2=x +x -1", GeoConicND.EQUATION_IMPLICIT,
				"-x^2 - 2x = -1");
		// parabolas
		checkEquation("-x^2-x=x -1+y", GeoConicND.EQUATION_IMPLICIT,
				"-x^2 - 2x - y = -1");
		checkEquation("y^2=x +x -1+y", GeoConicND.EQUATION_IMPLICIT,
				"y^2 - 2x - y = -1");
		checkEquation("(x+y)^2=x +x -1+y", GeoConicND.EQUATION_IMPLICIT,
				"x^2 + 2x y + y^2 - 2x - y = -1");
	}

	private static void checkNonParabolaFallback(int mode) {
		// ellipse
		checkEquation("x^2+4*y^2=1", mode, "x^2 + 4y^2 = 1");
		// hyperbola
		checkEquation("x^2-4*y^2=2x+2y+1", mode, "x^2 - 4y^2 - 2x - 2y = 1");
		// double line
		checkEquation("-x^2=x +x -1", mode, "-x^2 - 2x = -1");
	}
	
	@Test
	public void undefinedNumbersShouldBeQuestionMark() {
		t("b=1");
		t("SetValue[b,?]");
		assertEquals("b = ?",
				getGeo("b")
				.toString(StringTemplate.editTemplate));
		assertEquals("b = ?", app.getKernel().lookupLabel("b")
				.toString(StringTemplate.editorTemplate));
		assertEquals("b = ?",
				app.getKernel().lookupLabel("b").getDefinitionForEditor());
	}

	private GeoElement getGeo(String string) {
		return app.getKernel().lookupLabel(string);
	}

	@Test
	public void shortLHSshouldBeDisplayedInLaTeX(){
		t("a = 7");
		t("f: y = x^3");
		t("g: y = x^3 + a");
		assertEquals(CommandsTest.unicode("f: \\,y = x^3"),
				getGeo("f").getLaTeXAlgebraDescription(false,
						StringTemplate.defaultTemplate));
		assertEquals(CommandsTest.unicode("f: \\,y = x^3"),
				getGeo("f").getLaTeXAlgebraDescription(true,
				StringTemplate.defaultTemplate));
		assertEquals(CommandsTest.unicode("f: y = x^3"),
				getGeo("f").getDefinitionForEditor());
		assertEquals(CommandsTest.unicode("g: \\,y = x^3 + a"),
				getGeo("g").getLaTeXAlgebraDescription(false,
						StringTemplate.defaultTemplate));
		// TODO missing y =
		assertEquals(CommandsTest.unicode("g: x^3 + a"),
				getGeo("g").getDefinitionForEditor());

		t("in:x>a");
		assertEquals(CommandsTest.unicode("in: x > a"),
				getGeo("in").getDefinitionForEditor());
	}

	@Test
	public void operatorsShouldHaveOneSpace() {
		t("f(x)=If[3 < x <= 5,x^(2)]");
		assertEquals(CommandsTest
				.unicode("f(x) = If(3 < x " + Unicode.LESS_EQUAL + " 5, x^2)"),

				getGeo("f").getDefinitionForEditor());
	}

	@Test
	public void listShouldKeepDefinition() {
		t("list1 = {x+x=y}");
		assertEquals("list1 = {x + x = y}",
				getGeo("list1").getDefinitionForEditor());
		assertEquals("x + x = y",
				((GeoList) getGeo("list1")).get(0)
						.getDefinition(StringTemplate.editTemplate));
		t("list2 = Flatten[{x=y}]");
		assertEquals("list2 = Flatten({x = y})",
				((GeoList) getGeo("list2")).getDefinitionForEditor());
		
	}

	@Test
	public void singleVarEquationShouldHaveSuggestion() {
		t("p: z=0");
		assertEquals("z", String.join(",",
				((EquationValue) getGeo("p")).getEquationVariables()));
		t("p: x^2+z^2=0");
		assertEquals("x,z", String.join(",",
				((EquationValue) getGeo("p")).getEquationVariables()));


	}

	/**
	 * GGB-2021, TRAC-1642
	 */
	@Test
	public void substitutedFunctionsShouldBeExpanded() {
		t("ff(x)=x");

		t("gg(x)=2*ff(x)");

		t("hh(x)=gg(x-1)");
		assertEquals(
				"2 (x - 1)",
				getGeo("hh").toValueString(StringTemplate.defaultTemplate));

		t("a(x, y) = -y^2 - x y + 2y");

		t("f(x) = x/2");

		t("g(x) = 1 -x/2");

		t("h(x) = a(x, f) - a(x, g)");

		assertEquals(
				CommandsTest.unicode(
						"-(x / 2)^2 - x x / 2 + 2x / 2 - (-(1 - x / 2)^2 - x (1 - x / 2) + 2 (1 - x / 2))"),
				getGeo("h")
				.toValueString(StringTemplate.defaultTemplate));

	}

	@Test
	public void tooltipsShouldHaveDefaultPrecision() {
		t("P=(0,1/3)");
		assertEquals("Point P(0, 0.33)",
				getGeo("P").getTooltipText(false, true));
	}

	@Test
	public void definitionShouldContainCommand() {
		t("text1=TableText[{{1}}]");
		assertEquals("text1 = TableText({{1}})",
				getGeo("text1").getDefinitionForEditor());
		t("text2=FormulaText[sqrt(x)]");
		assertEquals("text2 = FormulaText(sqrt(x))",
				getGeo("text2").getDefinitionForEditor());
	}

	private void t(String def) {
		ap.processAlgebraCommandNoExceptionHandling(def, false,
				TestErrorHandler.INSTANCE, false, null);
	}

	@Test
	public void pointDescriptionShouldNotHaveCoords() {

		app.getKernel().setAlgebraStyle(Kernel.ALGEBRA_STYLE_DESCRIPTION);
		GeoPoint gp = new GeoPoint(app.getKernel().getConstruction());
		gp.setCoords(1, 2, 1);
		gp.setLabel("P");
		IndexHTMLBuilder builder = new IndexHTMLBuilder(false);
		AlgebraItem.buildPlainTextItemSimple(getGeo("P"), builder);
		Assert.assertEquals("Point P", builder.toString());
		t("P=(1,0)");
		AlgebraItem.buildPlainTextItemSimple(getGeo("P"), builder);
		Assert.assertEquals("Point P", builder.toString());
		t("Q=Dilate[P,2]");
		AlgebraItem.buildPlainTextItemSimple(getGeo("Q"), builder);
		Assert.assertEquals("Q = P dilated by factor 2 from (0, 0)",
				builder.toString());
		t("R=2*P");
		AlgebraItem.buildPlainTextItemSimple(getGeo("R"), builder);
		Assert.assertEquals("R = 2P",
				builder.toString());

	}

	@Test
	public void dependentPointsShouldHaveTextDescriptions() {

		app.getKernel().setAlgebraStyle(Kernel.ALGEBRA_STYLE_DESCRIPTION);
		IndexHTMLBuilder builder = new IndexHTMLBuilder(false);
		t("P=(1,0)");
		AlgebraItem.buildPlainTextItemSimple(getGeo("P"), builder);
		Assert.assertEquals("Point P", builder.toString());
		t("Q=Dilate[P,2]");
		AlgebraItem.buildPlainTextItemSimple(getGeo("Q"), builder);
		Assert.assertEquals("Q = P dilated by factor 2 from (0, 0)",
				builder.toString());
		t("R=2*P");
		AlgebraItem.buildPlainTextItemSimple(getGeo("R"), builder);
		Assert.assertEquals("R = 2P", builder.toString());

	}

	@Test
	public void packedGeosShouldHaveJustRHSInEditor() {
		t("c=Cone[(0,0,0),(0,0,1),5]");
		String rhs = getGeo("c").getLaTeXDescriptionRHS(false,
				StringTemplate.editorTemplate);
		assertEquals("Cone((0, 0, 0), (0, 0, 1), 5)", rhs);
	}


}
