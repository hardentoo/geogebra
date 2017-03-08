package org.geogebra.web.web.gui.toolbar.mow;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.web.html5.gui.FastClickHandler;
import org.geogebra.web.html5.gui.util.LayoutUtilW;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.util.sliderPanel.SliderPanelW;
import org.geogebra.web.web.gui.util.GeoGebraIconW;
import org.geogebra.web.web.gui.util.ImageOrText;
import org.geogebra.web.web.gui.util.StandardButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PenSubMenu extends SubMenuPanel
		implements ClickHandler, FastClickHandler {
	private static final int MAX_PEN_SIZE = 12;
	private static final int MAX_ERASER_SIZE = 100;
	private static final int PEN_STEP = 1;
	private static final int ERASER_STEP = 20;
	private static final int BLACK = 0;
	private StandardButton pen;
	private StandardButton eraser;
	private FlowPanel penPanel;
	private FlowPanel colorPanel;
	private FlowPanel sizePanel;
	private Label btnColor[];
	private GColor penColor[];
	private SliderPanelW slider;
	private StandardButton btnCustomColor;
	private boolean colorsEnabled;
	private final static String hexColors[] = { "000000", "673AB7", "009688",
			"E67E22" };

	public PenSubMenu(AppW app) {
		super(app, false);
		addStyleName("penSubMenu");
	}

	private void createPenPanel() {
		penPanel = new FlowPanel();
		pen = createButton(EuclidianConstants.MODE_PEN);
		eraser = createButton(EuclidianConstants.MODE_ERASER);
		penPanel.add(LayoutUtilW.panelRow(pen, eraser));
	}

	private Label createColorButton(GColor aColor) {
		ImageOrText color = GeoGebraIconW.createColorSwatchIcon(1, null,
				aColor);
		Label label = new Label();
		color.applyToLabel(label);
		label.addStyleName("MyCanvasButton");
		label.addStyleName("color-button");
		label.addClickHandler(this);
		return label;
	}
	private void createColorPanel() {
		colorPanel = new FlowPanel();
		colorPanel.addStyleName("colorPanel");
		btnColor = new Label[hexColors.length];
		penColor = new GColor[hexColors.length];
		for (int i = 0; i < hexColors.length; i++) {
			penColor[i] = GColor
					.newColorRGB(Integer.parseInt(hexColors[i], 16));
			btnColor[i] = createColorButton(penColor[i]);
		}

		btnCustomColor = new StandardButton("+");
		colorPanel.add(LayoutUtilW.panelRow(btnColor[0], btnColor[1],
				btnColor[2], btnColor[3], btnCustomColor));
	}

	private void createSizePanel() {
		sizePanel = new FlowPanel();
		sizePanel.addStyleName("sizePanel");
		slider = new SliderPanelW(0, 20, app.getKernel(), false);
		slider.addStyleName("optionsSlider");
		sizePanel.add(slider);
		slider.addValueChangeHandler(new ValueChangeHandler<Double>() {

			public void onValueChange(ValueChangeEvent<Double> event) {
				int value = slider.getValue().intValue();
				if (colorsEnabled) {
					getPenGeo().setLineThickness(value);
				} else {
					app.getActiveEuclidianView().getSettings()
							.setDeleteToolSize(value);
				}
			}
		});
	}

	@Override
	protected void createContentPanel() {
		super.createContentPanel();
		createPenPanel();
		createColorPanel();
		createSizePanel();

		contentPanel.add(LayoutUtilW.panelRow(penPanel, colorPanel, sizePanel));
	}

	public void onClick(Widget source) {
		if (source == pen) {
			selectPen();
		} else if (source == eraser) {
			selectEraser();
		}
	}

	public void onClick(ClickEvent event) {
		if (!colorsEnabled) {
			return;
		}

		Object source = event.getSource();
		for (int i = 0; i < btnColor.length; i++) {
			if (source == btnColor[i]) {
				selectColor(i);
			}
		}

	}

	private void selectPen() {
		app.setMode(EuclidianConstants.MODE_PEN);
		pen.addStyleName("penSubMenu-selected");
		eraser.removeStyleName("penSubMenu-selected");
		setColorsEnabled(true);
		selectColor(BLACK);
		slider.setMinimum(1, false);
		slider.setMaximum(MAX_PEN_SIZE, false);
		slider.setStep(PEN_STEP);
		slider.setValue((double) getPenGeo().getLineThickness());
	}

	private void selectEraser() {
		app.setMode(EuclidianConstants.MODE_ERASER);
		pen.removeStyleName("penSubMenu-selected");
		eraser.addStyleName("penSubMenu-selected");
		setColorsEnabled(false);
		slider.setMinimum(1, false);
		slider.setMaximum(MAX_ERASER_SIZE, false);
		slider.setStep(ERASER_STEP);
		int delSize = app.getActiveEuclidianView().getSettings()
				.getDeleteToolSize();
		slider.setValue((double) delSize);
	}

	@Override
	public void onOpen() {
		selectPen();
	}

	private void selectColor(int idx) {
		for (int i = 0; i < btnColor.length; i++) {
			if (idx == i) {
				btnColor[i].addStyleName("penSubMenu-selected");
				getPenGeo().setObjColor(penColor[i]);

			} else {
				btnColor[i].removeStyleName("penSubMenu-selected");
			}
		}

	}

	private void setColorsEnabled(boolean enable) {
		for (int i = 0; i < btnColor.length; i++) {
			btnColor[i].removeStyleName("penSubMenu-selected");
			if (enable) {
				btnColor[i].removeStyleName("disabled");
			} else {
				btnColor[i].addStyleName("disabled");

			}
		}
		colorsEnabled = enable;

	}
	private GeoElement getPenGeo() {
		return app.getActiveEuclidianView().getEuclidianController()
				.getPen().DEFAULT_PEN_LINE;
	}
}
