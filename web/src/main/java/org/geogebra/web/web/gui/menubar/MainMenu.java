package org.geogebra.web.web.gui.menubar;

import org.geogebra.common.main.Feature;
import org.geogebra.common.move.events.BaseEvent;
import org.geogebra.common.move.ggtapi.events.LogOutEvent;
import org.geogebra.common.move.ggtapi.events.LoginEvent;
import org.geogebra.common.move.views.BooleanRenderable;
import org.geogebra.common.move.views.EventRenderable;
import org.geogebra.common.plugin.EventType;
import org.geogebra.web.html5.gui.laf.MainMenuI;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.css.MaterialDesignResources;
import org.geogebra.web.web.gui.GuiManagerW;
import org.geogebra.web.web.gui.browser.SignInButton;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.StackPanel;

/**
 * Sidebar menu for SMART
 * 
 * 
 */
public class MainMenu extends FlowPanel implements MainMenuI, EventRenderable, BooleanRenderable {
	
	/**
	 * Appw app
	 */
	/*private MenuItem signIn;
	private SignedInMenuW signedIn;
	private MenuItem signedInMenu;*/
	
	AppW app;
	
	/**
	 * Panel with menus
	 */
	StackPanel menuPanel;
	private ViewMenuW viewMenu;
	private FileMenuW fileMenu;
	private HelpMenuW helpMenu;
	private OptionsMenuW optionsMenu;
	private ToolsMenuW toolsMenu;
	private EditMenuW editMenu;

	private PerspectivesMenuW perspectivesMenu;
	private boolean leftSide = false;
	/**
	 * Menus
	 */
	GMenuBar[] menus;
	private GMenuBar userMenu;
	/** sign in menu */
	final GMenuBar signInMenu;
	GMenuBar logoMenu;

	/**
	 * Constructs the menubar
	 * 
	 * @param app
	 *            application
	 */
	public MainMenu(AppW app) {
		if (!app.has(Feature.NEW_TOOLBAR)) {
			this.addStyleName("menubarSMART");
		}
		signInMenu = new GMenuBar(true, "signin", app);
		leftSide = app.isWhiteboardActive() || app.has(Feature.NEW_TOOLBAR);
		if (leftSide && !app.has(Feature.NEW_TOOLBAR)) {
			addStyleName("mowMenubar");
		}
		this.app = app;
		init();
	}

	private void init() {
		this.app.getLoginOperation().getView().add(this);
		final boolean exam = app.isExam();
		if(app.enableFileFeatures()){
			this.createFileMenu();
		}
		
		boolean enableGraph = !exam || app.enableGraphing();
		if (enableGraph){
			this.createPerspectivesMenu();
			this.createEditMenu();
			this.createViewMenu();
		}
		this.createOptionsMenu();
		if (enableGraph) {
			this.createToolsMenu();
		}
		if (!exam) {
			this.createHelpMenu();
			this.createUserMenu();
			if(!app.enableFileFeatures()){
				if (app.has(Feature.NEW_TOOLBAR)) {
					this.menus = new GMenuBar[] { editMenu, perspectivesMenu,
							optionsMenu, toolsMenu, helpMenu };
				} else {
					this.menus = new GMenuBar[] { editMenu, perspectivesMenu,
							viewMenu, optionsMenu, toolsMenu, helpMenu };
				}
			}else{
				if (app.has(Feature.NEW_TOOLBAR)) {
					this.menus = new GMenuBar[] { fileMenu, editMenu,
							perspectivesMenu, optionsMenu, toolsMenu,
							helpMenu };
				} else {
					this.menus = new GMenuBar[] { fileMenu, editMenu,
							perspectivesMenu, viewMenu, optionsMenu, toolsMenu,
							helpMenu };
				}
			}
		} else {
			this.menus = new GMenuBar[] { fileMenu, optionsMenu };
		}
		
		for(int i=0; i<menus.length; i++){
			final int next = (i+1)%menus.length;
			final int previous = (i-1+menus.length)%menus.length;
			final int index = i;
		this.menus[i].addDomHandler(new KeyDownHandler(){
			
			@Override
            public void onKeyDown(KeyDownEvent event) {
				int keyCode = event.getNativeKeyCode();
				//First / last below are not intuitive -- note that default handler of
				//down skipped already from last to first
				if(keyCode == KeyCodes.KEY_DOWN){
					if(menus[index].isFirstItemSelected()){
						menuPanel.showStack(next);
						menus[next].focus();
					}
					
				}
				if(keyCode == KeyCodes.KEY_UP){
					if(menus[index].isLastItemSelected()){
						menuPanel.showStack(previous);
						menus[previous].focus();
					}
				}
				if(keyCode == KeyCodes.KEY_ESCAPE){
					app.toggleMenu();
					((GuiManagerW)app.getGuiManager()).getToolbarPanel().selectMenuButton(-1);
				}
	            
            }}, KeyDownEvent.getType());
		}
		this.menuPanel = new StackPanel() {
			@Override
			public void showStack(int index) {
				if (index == 0) {
					super.showStack(1);
				} else {
					super.showStack(index);
				}
				dispatchOpenEvent();
				if (index == 0) {
					app.getGuiManager().setDraggingViews(
							isViewDraggingMenu(menus[1]), false);
				} else {
					app.getGuiManager().setDraggingViews(
							isViewDraggingMenu(menus[index]), false);
				}
			}

			@Override
			public void onBrowserEvent(Event event) {
				
				if (!exam && DOM.eventGetType(event) == Event.ONCLICK) {
					Element target = DOM.eventGetTarget(event);
					int index = findDividerIndex(target);
					//check if SignIn was clicked
					//if we are offline, the last item is actually Help
					if (app.getNetworkOperation().isOnline() &&
 !app.getLoginOperation().isLoggedIn()
							&& index >= 0
							&& this.getWidget(index) == signInMenu) {
						((SignInButton)app.getLAF().getSignInButton(app)).login();
						app.toggleMenu();
						return;
					} else if (index >= 0
							&& this.getWidget(index) == logoMenu) {
						app.toggleMenu();
						return;
					}

					if (index != -1) {

						if (index == this.getSelectedIndex()) {
							closeAll(this);
							return;
						}
						showStack(index);
					}
				}
				super.onBrowserEvent(event);
			}

			// violator pattern from
			// https://code.google.com/archive/p/google-web-toolkit/issues/1188
			private native void closeAll(StackPanel stackPanel) /*-{
		          stackPanel.@com.google.gwt.user.client.ui.StackPanel::setStackVisible(IZ)(stackPanel. @com.google.gwt.user.client.ui.StackPanel::visibleStack, false);
		          stackPanel.@com.google.gwt.user.client.ui.StackPanel::visibleStack = -1; 
		     }-*/;

			private int findDividerIndex(Element elemSource) {
				Element elem = elemSource;
				    while (elem != null && elem != getElement()) {
				      String expando = elem.getPropertyString("__index");
				      if (expando != null) {
				        // Make sure it belongs to me!
				        int ownerHash = elem.getPropertyInt("__owner");
				        if (ownerHash == hashCode()) {
				          // Yes, it's mine.
				          return Integer.parseInt(expando);
						}
						// It must belong to some nested StackPanel.
				          return -1;
				      }
				      elem = DOM.getParent(elem);
				    }
				    return -1;
				  }

		};
		if (!app.has(Feature.NEW_TOOLBAR)) {
			this.menuPanel.addStyleName("menuPanel");
		} else {
			logoMenu = new GMenuBar(true,
					getHTML(MaterialDesignResources.INSTANCE.geogebra_logo_transparent(),
							""),
					app);
			logoMenu.setStyleName("logoMenu");
			this.menuPanel.add(logoMenu,
					getHTMLwithLink(MaterialDesignResources.INSTANCE.geogebra_logo_transparent(),
							""),
					true);
		}

		if(app.enableFileFeatures()){	
			this.menuPanel.add(
					fileMenu, getHTML(
							app.has(Feature.NEW_TOOLBAR)
									? MaterialDesignResources.INSTANCE
											.insert_file_black()
									: GuiResources.INSTANCE.menu_icon_file(),
							"File"),
					true);
		}
		if (enableGraph) {
			this.menuPanel.add(editMenu,
					getHTML(app.has(Feature.NEW_TOOLBAR)
							? MaterialDesignResources.INSTANCE.edit_black()
							: GuiResources.INSTANCE.menu_icon_edit(), "Edit"),
					true);

			this.menuPanel
					.add(perspectivesMenu,
							getHTML(app.has(Feature.NEW_TOOLBAR)
									? MaterialDesignResources.INSTANCE
											.geogebra_black()
									: GuiResources.INSTANCE
									.menu_icon_perspectives(), "math_apps"),
							true);
			if (!app.has(Feature.NEW_TOOLBAR)) {
				this.menuPanel.add(viewMenu,
					getHTML(app.has(Feature.NEW_TOOLBAR)
							? MaterialDesignResources.INSTANCE.home_black()
							: GuiResources.INSTANCE.menu_icon_view(), "View"),
					true);
			}

		}
		this.menuPanel.add(optionsMenu,
				getHTML(app.has(Feature.NEW_TOOLBAR)
						? MaterialDesignResources.INSTANCE.settings_black()
						: GuiResources.INSTANCE.menu_icon_options(),
						app.has(Feature.NEW_TOOLBAR)
								? app.getLocalization().getMenu("Settings")
								: "Options"),
				true);
		if (!app.getLAF().isSmart() && enableGraph) {
			this.menuPanel.add(toolsMenu,
					getHTML(app.has(Feature.NEW_TOOLBAR) ? MaterialDesignResources.INSTANCE.tools_black()
							: GuiResources.INSTANCE.menu_icon_tools(), "Tools"),
					true);
		}
		if (!exam) {
			this.menuPanel.add(helpMenu,
					getHTML(app.has(Feature.NEW_TOOLBAR)
							? MaterialDesignResources.INSTANCE.icon_help_black()
							: GuiResources.INSTANCE.menu_icon_help(), "Help"),
					true);
			if(app.getNetworkOperation().isOnline()){
				render(true);
			}
			app.getNetworkOperation().getView().add(this);
		}
	    this.add(menuPanel);	    
	}

	/**
	 * @param menu
	 *            menu
	 * @return whether dragging views should be enabled for this menu
	 */
	protected boolean isViewDraggingMenu(GMenuBar menu) {
		return menu == perspectivesMenu || menu == viewMenu;
	}

	@Override
	public void render(boolean online) {
		if(!app.enableFileFeatures()){
			return;
		}
		if (online && app.getLoginOperation().isLoggedIn()) {
			loggedIn = true;
			addUserMenu();
		} else if(online){
			addSignInMenu();
		} else {
			loggedIn = false;
			if(this.signInMenu != null){
				this.menuPanel.remove(this.signInMenu);
			}
			if(this.userMenu != null){
				this.menuPanel.remove(this.userMenu);
			}
		}
    }

	private void createUserMenu() {
		this.userMenu = new GMenuBar(true, "user", app);
		if (app.has(Feature.NEW_TOOLBAR)) {
			this.userMenu.addStyleName("matStackPanel");
		} else {
			this.userMenu.addStyleName("GeoGebraMenuBar");
		}
		this.userMenu.addItem(
				getMenuBarHtml(
						app.has(Feature.NEW_TOOLBAR)
								? MaterialDesignResources.INSTANCE.signout_black().getSafeUri().asString()
								: GuiResources.INSTANCE.menu_icon_sign_out().getSafeUri()
								.asString(),
						app.getLocalization().getMenu("SignOut"), true),
				true, new MenuCommand(app) {

			@Override
            public void doExecute() {
				app.getLoginOperation().performLogOut();
			}
		});
    }

	private String getHTML(ImageResource img, String s){
		//return  "<img src=\""+img.getSafeUri().asString()+"\" /><span style= \"font-size:80% \"  >" + s + "</span>";
		return "<img src=\"" + img.getSafeUri().asString()
				+ "\" draggable=\"false\"><span>"
				+ app.getLocalization().getMenu(s) + "</span>";
	}
	
	private String getHTMLwithLink(ImageResource img, String s){
		//return  "<img src=\""+img.getSafeUri().asString()+"\" /><span style= \"font-size:80% \"  >" + s + "</span>";
		String imgHTML = getHTML(img, s);
		return "<a href=\"https://www.geogebra.org/download\" target=\"_blank\">"+imgHTML+"</a>";
	}
	
	private void createFileMenu() {
		fileMenu = new FileMenuW(app);
	}

	private void createPerspectivesMenu() {
		perspectivesMenu = new PerspectivesMenuW(app);
	}

	private void createEditMenu() {
		editMenu = new EditMenuW(app);
	}
	
	private void createViewMenu() {
		if (!app.has(Feature.NEW_TOOLBAR)) {
			viewMenu = new ViewMenuW(app);
		}
	}
	
	private void createHelpMenu() {
		helpMenu = new HelpMenuW(app);
	}

	private void createOptionsMenu() {
		optionsMenu = new OptionsMenuW(app);
	}

	private void createToolsMenu() {
		toolsMenu = new ToolsMenuW(app);
	}

	private EditMenuW getEditMenu() {
	    return editMenu;
    }


	/**
	 * Update all submenus that depend on file content
	 */
	public void updateMenubar() {
		if(app.hasOptionsMenu()){
			app.getOptionsMenu(null).update();
		}
		if(viewMenu != null){
			viewMenu.update();
		}

		if (this.getEditMenu() != null) {
			getEditMenu().update();
		}
    }
	


	/**
	 * Update on selection change
	 */
	public void updateSelection() {
		if(this.getEditMenu()!=null){
			getEditMenu().invalidate();
		}
	}

	public void focus(){
		int index= Math.max(menuPanel.getSelectedIndex(),0);
		if(this.menus[index]!=null){
			this.menus[index].focus();
		}
	}

	public static void addSubmenuArrow(MenuBar w) {
		addSubmenuArrow(w, false);
	}

	public static void addSubmenuArrow(MenuBar w, boolean left) {
		w.addStyleName(left ? "subMenuRightSide" : "subMenuLeftSide");
		FlowPanel arrowSubmenu = new FlowPanel();
		arrowSubmenu.addStyleName("arrowSubmenu");
		NoDragImage arrow = left
				? new NoDragImage(GuiResources.INSTANCE.arrow_submenu_left()
						.getSafeUri().asString())
				: new NoDragImage(GuiResources.INSTANCE.arrow_submenu_right()
						.getSafeUri().asString());
		arrowSubmenu.add(arrow);
		w.getElement().appendChild(arrowSubmenu.getElement());
    }

	public static String getMenuBarHtml(String url, String str, boolean enabled) {
		String text2 = str.replace("\"", "'");
		String text3 = (enabled) ? text2 :  "<span style=\"color:gray;\">"+text2+"</span>";
		return "<img class=\"GeoGebraMenuImage menuImg\" alt=\"" + text2
				+ "\" src=\""
				+ url + "\" draggable=\"false\">" + text3;
    }

	
	public static String getMenuBarHtml(String url, String str) {
		String text = str.replace("\"", "'");
		return "<img class=\"menuImg\" width=\"16\" height=\"16\" alt=\"" + text
				+ "\" src=\""
				+ url + "\" draggable=\"false\">"
				+ text;
    }

	public static String getMenuBarHtmlImgLast(String str, String url) {
		String text = str.replace("\"", "'");
		return text + "<img class=\"menuImg\" width=\"16\" height=\"16\" alt=\""
				+ text + "\" src=\"" + url + "\" draggable=\"false\">";
	}
	public static void setMenuSelected(MenuItem m, boolean visible) {
		if (visible) {
			m.addStyleName("checked");
		} else {
			m.removeStyleName("checked");
		}
	}
	
	/**
	 * sets the height of the menu
	 * @param height int
	 */
	public void updateHeight(int height) {
		this.setHeight(height + "px");
    }

	private boolean loggedIn = false;
	@Override
	public void renderEvent(final BaseEvent event) {
		if(!app.enableFileFeatures()){
			return;
		}
		if (event instanceof LoginEvent && ((LoginEvent) event).isSuccessful()) {
			if (loggedIn) {
				this.menuPanel.remove(this.userMenu);
			}
			loggedIn = true;
			this.menuPanel.remove(this.signInMenu);
			addUserMenu();
			this.userMenu.setVisible(false);
		} else if (event instanceof LogOutEvent) {
			this.menuPanel.remove(this.userMenu);
			loggedIn = false;
			addSignInMenu();
			this.signInMenu.setVisible(false);
		}
	}

    private void addSignInMenu() {
		this.menuPanel.add(this.signInMenu,
				getHTML(app.has(Feature.NEW_TOOLBAR) ? MaterialDesignResources.INSTANCE.signin_black()
						: GuiResources.INSTANCE.menu_icon_sign_in(),
						app.getLocalization().getMenu("SignIn")),
				true);
    }

    private void addUserMenu() {
		this.menuPanel
				.add(this.userMenu,
						getHTML(app.has(Feature.NEW_TOOLBAR) ? MaterialDesignResources.INSTANCE.person_black()
								: GuiResources.INSTANCE.menu_icon_signed_in_f(), app.getLoginOperation().getUserName()),
						true);
    }

	/**
	 * Inform client listener about opening the menu
	 */
	public void dispatchOpenEvent() {
		if (menuPanel != null) {
			int index = menuPanel.getSelectedIndex();
			if (index == -1) {
				index = 1;
			}
			app.dispatchEvent(new org.geogebra.common.plugin.Event(
					EventType.OPEN_MENU, null, menus[index].getMenuTitle()));
		}
	}

}
