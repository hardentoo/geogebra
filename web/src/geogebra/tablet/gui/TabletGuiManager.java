package geogebra.tablet.gui;

import geogebra.html5.gui.browser.BrowseGUI;
import geogebra.html5.main.AppWeb;
import geogebra.tablet.gui.browser.TabletBrowseGUI;
import geogebra.web.gui.GuiManagerW;
import geogebra.web.main.AppW;

public class TabletGuiManager extends GuiManagerW {

	public TabletGuiManager(final AppW app) {
	    super(app);
    }

	/**
	 * @return {@link TabletBrowseGUI}
	 */
	@Override
	public BrowseGUI getBrowseGUI() {
		if (this.browseGUI == null) {
			this.browseGUI = new TabletBrowseGUI((AppWeb)this.app);
		}
		return this.browseGUI;
	}
	
}
