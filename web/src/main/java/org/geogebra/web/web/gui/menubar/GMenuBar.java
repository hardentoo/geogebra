package org.geogebra.web.web.gui.menubar;

import org.geogebra.common.main.App;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;

public class GMenuBar extends MenuBar{
	private int separators = 0;
	private String menuTitle;
	private App app;

	public GMenuBar(boolean vertical, String menuTitle, App app) {
	    super(vertical);
		this.menuTitle = menuTitle;
		this.app = app;
	}

	public GMenuBar(boolean vertical, String menuTitle,
			MenuResources menuResources, App app) {
	    super(vertical, menuResources);
		this.menuTitle = menuTitle;
		this.app = app;
	}

	public boolean isFirstItemSelected(){
		return this.getItemIndex(this.getSelectedItem()) == 0;
	}

	public boolean isLastItemSelected() {
		return this.getItemIndex(this.getSelectedItem()) == this.getItems().size() + separators - 1;
		
    }
	
	@Override
	public MenuItemSeparator addSeparator(){
		if (app.isUnbundledOrWhiteboard()) {
			return null;
		}
		this.separators  ++;
		return super.addSeparator();
	}

	/**
	 * As far as GWT is buggy in the implementation of this, it is necessary to
	 * have a method to correct that
	 *
	 * @param subleft
	 *            specifies if submenu should open to the left.
	 * @return MenuItem
	 */
	public MenuItem addItem(String itemtext, boolean textishtml,
			final MenuBar submenupopup, final boolean subleft) {

		// this works, but it is still different from addItem in
		// not following mouse movement, only following mouse clicks, etc
		final Object[] ait = new Object[2];
		ait[1] = null;// means the popup assigned to this MenuItem
		ait[0] = addItem(itemtext, textishtml, new ScheduledCommand() {
			@Override
			public void execute() {

				if (ait[0] != null) {
					selectItem((MenuItem) ait[0]);
					// Note that another menu item might have an open popup
					// here, with a different submenupopup, and that disappears
					// because its popuppanel is modal, but its ait[1]
					// variable still remains filled, so it is necessary to
					// check it here, and make it null if necessary
					if ((ait[1] != null) && (ait[1] instanceof PopupPanel)
					        && !((PopupPanel) ait[1]).isShowing()
					        && !((PopupPanel) ait[1]).isAttached()) {
						// but here we should exclude the case where the same
						// menuitem is clicked as the present one!!
						ait[1] = null;
					}

					if (ait[1] == null) {
						// popuppanel still not present
						final PopupPanel pp = new PopupPanel(true, false);
						pp.addAutoHidePartner(((MenuItem)ait[0]).getElement());
						submenupopup.addStyleName(subleft ? "subMenuLeftSide2"
								: "subMenuRightSide2");
						submenupopup.selectItem(null);
						if (subleft) {
							pp.addStyleName("subMenuLeftSidePopup");
						} else {
							pp.addStyleName(
									"GeoGebraMenuBar.subMenuRightSidePopup");

						}

						if (app.isUnbundled()) {
							pp.addStyleName("floatingSubMenu");
						}
						pp.add(submenupopup);
						MenuItem mi0 = ((MenuItem) ait[0]);
						        
						int left = subleft ? mi0.getElement().getAbsoluteLeft()
								: mi0.getElement().getAbsoluteRight() + 8;
						int top = mi0.getElement().getAbsoluteTop();
						pp.setPopupPosition(left, top);

						if (submenupopup instanceof RadioButtonMenuBarW) {
							((RadioButtonMenuBarW) submenupopup)
							        .registerItemSideEffect(new Scheduler.ScheduledCommand() {
								        @Override
										public void execute() {
									        // this should only run if some item
									        // is selected and clicked
									        ait[1] = null;
									        submenupopup.selectItem(null);
									        pp.hide();
								        }
							        });
						}

						// TODO: more difficult to solve autoOpen
						// sadly, the following thing can only work together
						// with submenus created by the other method, in theory
						// parentMenu.setAutoOpen(true);

						ait[1] = pp;
						pp.show();
					} else {
						submenupopup.selectItem(null);

						if (ait[1] instanceof PopupPanel) {
							((PopupPanel) ait[1]).hide();
						}

						// if popuppanel is present, it will be hidden
						// due to autoHide, and no new one is created instead
						// let's forget about it
						ait[1] = null;
					}
				}
			}
		});

		return (MenuItem) ait[0];
	}

	@Override
	public MenuItem addItem(String itemtext, boolean textishtml,
			final MenuBar submenupopup) {
		return addItem(itemtext, textishtml, submenupopup, true);
	}

	public String getMenuTitle() {
		return menuTitle;
	}
}
