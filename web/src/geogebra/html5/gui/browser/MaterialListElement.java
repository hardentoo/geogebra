package geogebra.html5.gui.browser;

import geogebra.common.move.ggtapi.models.Material;
import geogebra.common.move.ggtapi.models.Material.MaterialType;
import geogebra.html5.Browser;
import geogebra.html5.gui.FastClickHandler;
import geogebra.html5.gui.ResizeListener;
import geogebra.html5.gui.StandardButton;
import geogebra.html5.main.AppWeb;
import geogebra.html5.move.ggtapi.models.GeoGebraTubeAPIW;
import geogebra.html5.move.ggtapi.models.MaterialCallback;
import geogebra.web.gui.GuiManagerW;
import geogebra.web.gui.dialog.DialogManagerW;
import geogebra.web.gui.images.AppResources;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * GUI Element showing a Material as search Result
 * 
 * @author Matthias Meisinger
 * 
 */
public class MaterialListElement extends FlowPanel implements ResizeListener {
	
	public enum State {
		Default, Selected, Disabled;
	}
	
	private final int MAX_TITLE_HEIGHT = 40;
	private FlowPanel materialElementContent;
	private SimplePanel previewPicturePanel;
	protected FlowPanel infoPanel;

	protected Label title;
	protected Label sharedBy;
	private TextBox renameTextBox;
	protected final Material material;
	protected final AppWeb app;
	
	protected State state = State.Default;
	Runnable editMaterial;

	protected StandardButton viewButton;
	protected StandardButton editButton;

	/**
	 * 
	 * @param m {@link Material}
	 * @param app {@link AppWeb}
	 */
	public MaterialListElement(final Material m, final AppWeb app) {
		this.app = app;
		this.material = m;
		this.setStyleName("materialListElement");
		this.addStyleName("default");
		this.editMaterial = new Runnable() {
			
			@Override
			public void run() {
				onEdit();
			}
		};
		initMaterialInfos();

		materialElementContent = new FlowPanel();
		this.materialElementContent.addStyleName("materialElementContent");
		this.add(materialElementContent);
		
		addPreviewPicture();
		addInfoPanel();

		showDetails(false);

		this.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				event.preventDefault();
				if (state == State.Disabled) {
					return;
				} else if (state == State.Default) {
					markSelected();
					event.stopPropagation();
				} else {
					event.stopPropagation();
				}
			}
		}, ClickEvent.getType());
		
		setLabels();
	}

	private void addInfoPanel() {
		this.infoPanel = new FlowPanel();
		this.infoPanel.setStyleName("infoPanel");

		addTextInfo();
		addOptions();

		this.materialElementContent.add(this.infoPanel);
	}

	protected void addTextInfo() {
		this.infoPanel.add(this.title);
		this.infoPanel.add(this.sharedBy);
	}

	protected void addOptions() {
		addViewButton();
		addEditButton();
	}

	private void addPreviewPicture() {
		this.previewPicturePanel = new SimplePanel();
		this.previewPicturePanel.addStyleName("fileImage");
		this.previewPicturePanel.addDomHandler(new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				if (state == State.Selected) {
					openDefault();
				} else if (state == State.Disabled) {
					return;
				} else {
					markSelected();
					event.stopPropagation();
				}
			}
		}, ClickEvent.getType());

		final SimplePanel background = new SimplePanel();
		background.setStyleName("background");
		
		final String thumb = this.material.getThumbnail();
		if (thumb != null && thumb.length() > 0) {
			setPicture(background, thumb);
		} else {
			background
			.getElement()
			.getStyle()
			.setBackgroundImage(
					"url("
							+ AppResources.INSTANCE.geogebra64()
							.getSafeUri().asString() + ")");
		}
		
		this.previewPicturePanel.add(background);
		this.materialElementContent.add(this.previewPicturePanel);

		if(this.material.getType() == Material.MaterialType.book){
			final Label deco = new Label();
			deco.setStyleName("bookDecoration");
			background.add(deco);
		}
	}

	protected void setPicture(final SimplePanel background, final String thumb) {
	    background.getElement().getStyle().setBackgroundImage("url(" + Browser.normalizeURL(thumb) + ")");
    }


	/**
	 * 
	 */
	protected void openDefault() {
		onView();
	}

	protected void initMaterialInfos() {
		this.title = new Label(this.material.getTitle());
		this.title.setStyleName("fileTitle");	
		this.sharedBy = new Label(this.material.getAuthor());
		this.sharedBy.setStyleName("sharedPanel");
	}

	protected void addEditButton() {
		this.editButton = new StandardButton(BrowseResources.INSTANCE.document_edit(), "");
		this.infoPanel.add(this.editButton);
		this.editButton.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick() {
				((DialogManagerW) app.getDialogManager()).getSaveUnsavedDialog().setCallback(editMaterial);
				((DialogManagerW) app.getDialogManager()).getSaveUnsavedDialog().showIfNeeded();
			}
		});
	}

	/**
	 * 
	 */
	protected void onEdit() {
		((GuiManagerW) app.getGuiManager()).getBrowseGUI().setMaterialsDefaultStyle();
		if(material.getType() == MaterialType.book){
			((GeoGebraTubeAPIW) app.getLoginOperation().getGeoGebraTubeAPI()).getBookItems(material.getId(), new MaterialCallback(){

				@Override
				public void onLoaded(final List<Material> response) {
					((GuiManagerW) app.getGuiManager()).getBrowseGUI().clearMaterials();
					((GuiManagerW) app.getGuiManager()).getBrowseGUI().onSearchResults(response);
				}
			});
			return;
		}
		((GeoGebraTubeAPIW) app.getLoginOperation().getGeoGebraTubeAPI()).getItem(material.getId(), new MaterialCallback(){

			@Override
			public void onLoaded(final List<Material> parseResponse) {
				app.getGgbApi().setBase64(parseResponse.get(0).getBase64());
			}
		});
		closeBrowseView();
	}

	protected void closeBrowseView() {
		((GuiManagerW) app.getGuiManager()).getBrowseGUI().close();
	}

	protected void addViewButton() {
		this.viewButton = new StandardButton(BrowseResources.INSTANCE.document_view(), "");
		this.viewButton.addStyleName("viewButton");
		this.infoPanel.add(this.viewButton);
		this.viewButton.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick() {
				onView();
			}
		});
	}
	
	/**
	 * marks the material as selected and disables the other materials
	 */
	protected void markSelected() {
		((GuiManagerW) app.getGuiManager()).getBrowseGUI().disableMaterials();
		((GuiManagerW) app.getGuiManager()).getBrowseGUI().rememberSelected(this);
		this.state = State.Selected;
		this.removeStyleName("unselected");
		this.removeStyleName("default");
		this.addStyleName("selected");
		showDetails(true);
	}

	/**
	 * sets the default style 
	 */
	public void setDefaultStyle() {
		this.state = State.Default;
		this.removeStyleName("selected");
		this.removeStyleName("unselected");
		this.addStyleName("default");
		showDetails(false);
	}
	
	/**
	 * Disables the material.
	 */
	public void disableMaterial() {
		this.state = State.Disabled;
	    this.addStyleName("unselected");
	    this.removeStyleName("selected");
	    this.removeStyleName("default");
    }

	/**
	 * 
	 */
	public void setLabels() {
		this.editButton.setText(app.getLocalization().getMenu("Edit"));
		this.viewButton.setText(app.getMenu(getInsertWorksheetTitle(material)));
	}

	/**
	 * 
	 * @return the {@link Material}
	 */
	public Material getMaterial() {
		return this.material;
	}

	protected void showDetails(final boolean show) {
		this.sharedBy.setVisible(true);
		this.viewButton.setVisible(show);
		this.editButton.setVisible(show);
		if (show) {
			this.infoPanel.addStyleName("detailed");
		} else {
			this.infoPanel.removeStyleName("detailed");
		}
	}
	
	/*** LAF dependent methods **/

	public String getInsertWorksheetTitle(final Material m) {
		return "View";
	}

	/**
	 * Opens GeoGebraTube material in a new window (overwritten for tablet app, smart widget)
	 */
	protected void onView() {
		((GuiManagerW) app.getGuiManager()).getBrowseGUI().setMaterialsDefaultStyle();
		openTubeWindow(material.getURL());
	}

	/**
	 * Opens GeoGebraTube material in a new window
	 * @param id material id
	 */
	private native void openTubeWindow(String url)/*-{
		$wnd.open(url);
	}-*/;

	@Override
	public void onResize() {
		// TODO Auto-generated method stub

	}
}