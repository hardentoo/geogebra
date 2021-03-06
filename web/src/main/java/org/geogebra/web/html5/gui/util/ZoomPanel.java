package org.geogebra.web.html5.gui.util;

import java.util.Arrays;
import java.util.List;

import org.geogebra.common.euclidian.CoordSystemListener;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.EuclidianViewInterfaceSlim;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.main.Feature;
import org.geogebra.common.util.StringUtil;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.css.ZoomPanelResources;
import org.geogebra.web.html5.gui.FastClickHandler;
import org.geogebra.web.html5.gui.TabHandler;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.main.StringHandler;
import org.geogebra.web.html5.util.ArticleElement;
import org.geogebra.web.web.gui.layout.GUITabs;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Place of the zoom buttons.
 * 
 * @author zbynek, laszlo
 *
 */
public class ZoomPanel extends FlowPanel
		implements CoordSystemListener, TabHandler {

	private StandardButton homeBtn;
	private StandardButton zoomInBtn;
	private StandardButton zoomOutBtn;
	/**
	 * enter/exit fullscreen mode
	 */
	StandardButton fullscreenBtn;
	/**
	 * is in fullscreen mode
	 */
	boolean isFullScreen = false;

	private FlowPanel zoomPanel = this;
	private AppW app;
	private EuclidianView view;
	/** after we leave fullscreen, we must reset container position */
	private String containerPositionBefore;
	private String containerLeft;
	private String containerMarginLeft;
	private String containerMarginTop;
	private double cssScale = 0;
	private List<StandardButton> buttons = null;
	private boolean homeShown;

	/**
	 *
	 * @param view
	 *            The Euclidian View to put zoom buttons onto.
	 */
	public ZoomPanel(EuclidianView view) {
		this.view = view;
		this.app = (AppW) view.getApplication();
		view.getEuclidianController().setZoomerListener(this);
		setStyleName("zoomPanel");
		if (app.isWhiteboardActive()) {
			if (app.has(Feature.MOW_MULTI_PAGE)) {
				addStyleName("zoomPanelWithPageControl");
			} else {
				addStyleName("zommPanelMow");
			}
		} else {
			addStyleName("zoomPanelPosition");
		}
		if (ZoomPanel.needsZoomButtons(app)) {
			addZoomButtons();
		}
		if (ZoomPanel.needsFullscreenButton(app)) {
			addFullscreenButton();
		}
		setLabels();
		if (app.has(Feature.TAB_ON_GUI)) {
			buttons = Arrays.asList(homeBtn, zoomInBtn, zoomOutBtn,
					fullscreenBtn);
			setTabIndexes();
		}
	}

	/**
	 * Updates fullscreen button and article.
	 */
	public void updateFullscreen() {
		ArticleElement ae = app.getArticleElement();
		if (!ae.getDataParamApp() && isFullScreen) {
			scaleApplet(ae.getParentElement(),
					ae.getParentElement().getParentElement());
		}
		if (ae.getDataParamApp() && fullscreenBtn != null) {
			fullscreenBtn.setVisible(
					isFullScreen || !Browser.isCoveringWholeScreen());
		}
	}

	/**
	 * add fullscreen button
	 */
	public void addFullscreenButton() {
		fullscreenBtn = new StandardButton(
				new ImageResourcePrototype(null,
						ZoomPanelResources.INSTANCE.fullscreen_black18()
								.getSafeUri(),
						0, 0, 18, 18, false, false),
				app);
		fullscreenBtn.getDownFace()
				.setImage(new Image(new ImageResourcePrototype(
						null, ZoomPanelResources.INSTANCE
								.fullscreen_exit_black18().getSafeUri(),
						0, 0, 18, 18, false, false)));

		fullscreenBtn.setStyleName("zoomPanelBtn");

		FastClickHandler handlerFullscreen = new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				onFullscreenPressed();
			}
		};

		if (app.has(Feature.TAB_ON_GUI)) {
			fullscreenBtn.addTabHandler(this);
		}

		fullscreenBtn.addFastClickHandler(handlerFullscreen);
		Browser.addFullscreenListener(new StringHandler() {

			@Override
			public void handle(String obj) {
				if ("true".equals(obj)) {
					onFullscreen();
				} else {
					onExitFullscreen();
				}
			}
		});
		zoomPanel.add(fullscreenBtn);
	}

	/**
	 * Handler that runs on switching to fullscreen.
	 */
	void onFullscreen() {
		isFullScreen = true;
		fullscreenBtn
				.setIcon(new ImageResourcePrototype(
						null, ZoomPanelResources.INSTANCE
								.fullscreen_exit_black18().getSafeUri(),
						0, 0, 18, 18, false, false));
	}

	/**
	 * Handler that runs on exiting to fullscreen.
	 */
	void onExitFullscreen() {
		isFullScreen = false;
		fullscreenBtn.setIcon(new ImageResourcePrototype(null,
				ZoomPanelResources.INSTANCE.fullscreen_black18().getSafeUri(),
				0, 0, 18, 18, false, false));
		if (!app.getArticleElement().getDataParamFitToScreen()) {

			final Element scaler = app.getArticleElement().getParentElement();

			scaler.removeClassName("fullscreen");
			scaler.getStyle().setMarginLeft(0, Unit.PX);
			scaler.getStyle().setMarginTop(0, Unit.PX);
			dispatchResize();
			Element container = scaler.getParentElement();
			resetStyleAfterFullscreen(container);
			Browser.scale(scaler,
					cssScale > 0 ? cssScale
							: app.getArticleElement().getDataParamScale(),
					0, 0);
			app.getArticleElement().resetScale();
			app.recalculateEnvironments();
		}

		Browser.scale(zoomPanel.getElement(), 1, 0, 0);
	}

	/**
	 * Resetting position and margins.
	 * 
	 * @param container
	 *            to reset.
	 */
	protected void resetStyleAfterFullscreen(Element container) {
		if (container != null) {
			container.getStyle().setProperty("position",
					containerPositionBefore);
			if (!StringUtil.empty(containerMarginLeft)) {
				container.getStyle().setProperty("marginLeft",
						containerMarginLeft);
			}
			if (!StringUtil.empty(containerMarginTop)) {
				container.getStyle().setProperty("marginTop",
						containerMarginTop);
			}
		}
	}

	/**
	 * Add zoom in/out buttons to GUI
	 */
	public void addZoomButtons() {
		homeBtn = new StandardButton(
				new ImageResourcePrototype(null,
						ZoomPanelResources.INSTANCE.home_zoom_black18()
								.getSafeUri(),
						0, 0, 18, 18, false, false),
				app);
		homeBtn.setStyleName("zoomPanelBtn");
		hideHomeButton();
		FastClickHandler handlerHome = new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				onHomePressed();
			}
		};
		homeBtn.addFastClickHandler(handlerHome);

		if (app.has(Feature.TAB_ON_GUI)) {
			homeBtn.addTabHandler(this);
		}

		zoomPanel.add(homeBtn);
		if (!Browser.isMobile()) {
			addZoomInButton();
			addZoomOutButton();
		}

		if (app.has(Feature.TAB_ON_GUI)) {
			zoomInBtn.addTabHandler(this);
		}
		ClickStartHandler.init(zoomPanel, new ClickStartHandler(true, true) {

			@Override
			public void onClickStart(int x, int y, PointerEventType type) {
				// to stopPropagation and preventDefault.
			}
		});
	}

	private void addZoomOutButton() {
		zoomOutBtn = new StandardButton(
				new ImageResourcePrototype(null,
						ZoomPanelResources.INSTANCE.remove_black18()
								.getSafeUri(),
						0, 0, 18, 18, false, false),
				app);
		zoomOutBtn.setStyleName("zoomPanelBtn");
		FastClickHandler handlerZoomOut = new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				onZoomOutPressed();
			}
		};
		zoomOutBtn.addFastClickHandler(handlerZoomOut);
		zoomPanel.add(zoomOutBtn);
	}

	private void addZoomInButton() {
		zoomInBtn = new StandardButton(new ImageResourcePrototype(null,
				ZoomPanelResources.INSTANCE.add_black18().getSafeUri(), 0, 0,
				18, 18, false, false), app);
		zoomInBtn.setStyleName("zoomPanelBtn");
		FastClickHandler handlerZoomIn = new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				onZoomInPressed();
			}
		};
		zoomInBtn.addFastClickHandler(handlerZoomIn);
		zoomPanel.add(zoomInBtn);
	}

	/**
	 * @return view connected to this panel
	 */
	protected EuclidianViewInterfaceSlim getEuclidianView() {
		return view;
	}

	/**
	 * Shows home button.
	 */
	void showHomeButton() {
		if (homeBtn == null) {
			return;
		}
		homeShown = true;
		homeBtn.addStyleName("zoomPanelHomeIn");
		homeBtn.removeStyleName("zoomPanelHomeOut");
		homeBtn.getElement().setAttribute("arial-hidden", "false");
	}

	/**
	 * Hides home button.
	 */
	void hideHomeButton() {
		if (homeBtn == null) {
			return;
		}
		homeShown = false;
		homeBtn.addStyleName("zoomPanelHomeOut");
		homeBtn.removeStyleName("zoomPanelHomeIn");
		homeBtn.getElement().setAttribute("arial-hidden", "true");
	}

	private void updateHomeButton() {
		if (app.getActiveEuclidianView().isCoordSystemTranslatedByAnimation()) {
			return;
		}
		if (app.getActiveEuclidianView().isStandardView()) {
			hideHomeButton();
		} else {
			showHomeButton();
		}
	}

	@Override
	public void onCoordSystemChanged() {
		updateHomeButton();
	}

	/**
	 * forces a resize event.
	 */
	protected native void dispatchResize() /*-{
		$wnd.dispatchEvent(new Event("resize"));
	}-*/;

	/** Home button handler. */
	protected void onHomePressed() {
		app.getActiveEuclidianView().setStandardView(true);
	}

	/** Zoom In button handler. */
	protected void onZoomInPressed() {
		getEuclidianView().getEuclidianController().zoomInOut(false, false,
				this);
	}

	/** Zoom Out button handler. */
	protected void onZoomOutPressed() {
		getEuclidianView().getEuclidianController().zoomInOut(false, true,
				this);
	}

	/** Full screen button handler. */
	protected void onFullscreenPressed() {
		final Element container;
		final boolean ipad = Browser.isIPad();
		if (app.getArticleElement().getDataParamFitToScreen()) {
			container = null;
		} else {
			ArticleElement ae = app.getArticleElement();
			final Element scaler = ae.getParentElement();
			container = scaler.getParentElement();
			if (!isFullScreen) {
				containerPositionBefore = container.getStyle().getPosition();
				containerMarginLeft = container.getStyle()
						.getProperty("marginLeft");
				containerMarginTop = container.getStyle()
						.getProperty("marginTop");
				container.getStyle().setProperty("marginTop", "0");
				container.getStyle().setProperty("marginLeft", "0");
				if (StringUtil.empty(containerPositionBefore)) {
					containerPositionBefore = "static";
				}
				scaler.addClassName("fullscreen");
				cssScale = ae.getParentScaleX();
				if (ipad) {
					containerLeft = container.getStyle()
							.getProperty("left");
					container.getStyle().setProperty("left", "0px");
					scaler.addClassName("fullscreen-ipad");
				}

				Timer t = new Timer() {

					@Override
					public void run() {
						scaleApplet(scaler, container);
						if (ipad) {
							onFullscreen();
						}
					}
				};
				// delay scaling to make sure scrollbars disappear
				t.schedule(50);
			} else {
				if (ipad) {
					if (containerLeft != null) {
						container.getStyle().setProperty("left", containerLeft);

					}
					scaler.removeClassName("fullscreen-ipad");
					onExitFullscreen();
					if (cssScale != 0) {
						Browser.scale(scaler, cssScale, 0, 0);
					}
				}
			}
		}

		if (!ipad) {
			isFullScreen = !isFullScreen;
			Browser.toggleFullscreen(isFullScreen, container);
		}
	}

	private static double getDeviceScale(double xscale, double yscale) {
		if ((xscale < 1 || yscale < 1) || Browser.isIPad()) {
			return Math.min(1d, Math.min(xscale, yscale));
		}
		return Math.max(1d, Math.min(xscale, yscale));
	}

	/**
	 * Scales the applet to fit the screen.
	 * 
	 * @param scaler
	 *            the applet scaler element.
	 * @param container
	 *            content to scale.
	 */
	protected void scaleApplet(Element scaler, Element container) {
		double xscale = Window.getClientWidth() / app.getWidth();
		double yscale = Window.getClientHeight() / app.getHeight();
		double scale = getDeviceScale(xscale, yscale);
		Browser.scale(scaler, scale, 0, 0);
		Browser.scale(zoomPanel.getElement(), 1 / scale, 120, 100);
		container.getStyle().setWidth(100, Unit.PCT);
		container.getStyle().setHeight(100, Unit.PCT);
		container.getStyle().setPosition(Position.ABSOLUTE);
		double marginLeft = 0;
		double marginTop = 0;
		if (xscale > yscale) {
			marginLeft = (Window.getClientWidth() - app.getWidth() * scale) / 2;
		} else {
			marginTop = (Window.getClientHeight() - app.getHeight() * scale)
					/ 2;
		}
		scaler.getStyle().setMarginLeft(marginLeft, Unit.PX);
		scaler.getStyle().setMarginTop(marginTop, Unit.PX);
		app.getArticleElement().resetScale();
		app.recalculateEnvironments();
	}

	/**
	 * Sets translated titles of the buttons.
	 */
	public void setLabels() {
		setButtonTitleAndAltText(fullscreenBtn, "Fullscreen");
		setButtonTitleAndAltText(homeBtn, "StandardView");
		setButtonTitleAndAltText(zoomOutBtn,"ZoomOut.Tool");
		setButtonTitleAndAltText(zoomInBtn,"ZoomIn.Tool");
	}

	private void setButtonTitleAndAltText(StandardButton btn, String string) {
		if (btn != null) {
			btn.setTitle(app.getLocalization().getMenu(string));
			btn.setAltText(app.getLocalization().getMenu(string));
		}
	}

	private static boolean needsFullscreenButton(AppW app) {
		if (app.has(Feature.MOW_MULTI_PAGE)) {
			return false;
		}
		return app.getArticleElement().getDataParamShowFullscreenButton()
				|| (app.getArticleElement().getDataParamApp()
						&& !Browser.isMobile());
	}

	private static boolean needsZoomButtons(AppW app) {
		return (app.getArticleElement().getDataParamShowZoomButtons()
				|| app.getArticleElement().getDataParamApp())
				&& app.isShiftDragZoomEnabled();
	}

	/**
	 * Checks if the current app needs zoom panel or not.
	 * 
	 * @param app
	 *            the application to check.
	 * @return true if app needs zoom panel.
	 */
	public static boolean neededFor(AppW app) {
		return needsZoomButtons(app) || needsFullscreenButton(app);
	}

	/**
	 * 
	 * @return the minimum height that is needed to display zoomPanel.
	 */
	public int getMinHeight() {
		return needsZoomButtons(app) ? 200 : 100;
	}

	/**
	 * Sets tab order for header buttons.
	 */
	public void setTabIndexes() {
		int tabIndex = GUITabs.ZOOMPANEL;
		for (StandardButton btn : buttons) {
			if (btn != null) {
				btn.setTabIndex(tabIndex);
				tabIndex++;
			}
		}
	}

	@Override
	public boolean onTab(Widget source, boolean shiftDown) {
		if (source == getFirstButton() && shiftDown) {
			app.getAccessibilityManager().focusPrevious(this);
			return true;
		} else if (source == getLastButton() && !shiftDown) {
			app.getAccessibilityManager().focusNext(this);
			return true;
		}

		return false;
	}

	/** Focus the first available button on zoom panel. */
	public void focusFirstButton() {
		Widget btn = getFirstButton();
		if (btn != null) {
			btn.getElement().focus();
		}
	}

	/** Focus the last available button on zoom panel. */
	public void focusLastButton() {
		Widget btn = getLastButton();
		if (btn != null) {
			btn.getElement().focus();
		}
	}

	private Widget getFirstButton() {
		if (homeBtn != null && homeShown) {
			return homeBtn;
		}

		if (zoomInBtn != null) {
			return zoomInBtn;
		}

		return fullscreenBtn;
	}

	private Widget getLastButton() {
		if (fullscreenBtn != null) {
			return fullscreenBtn;
		}

		if (zoomOutBtn != null) {
			return zoomOutBtn;
		}

		if (homeBtn != null && homeShown) {
			return homeBtn;
		}

		return null;
	}
}
