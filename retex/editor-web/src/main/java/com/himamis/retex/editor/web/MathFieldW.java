/**
 * This file is part of the ReTeX library - https://github.com/himamis/ReTeX
 *
 * Copyright (C) 2015 Balazs Bencze
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.himamis.retex.editor.web;


import java.util.ArrayList;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.himamis.retex.editor.share.editor.MathField;
import com.himamis.retex.editor.share.editor.MathFieldInternal;
import com.himamis.retex.editor.share.event.ClickListener;
import com.himamis.retex.editor.share.event.FocusListener;
import com.himamis.retex.editor.share.event.KeyEvent;
import com.himamis.retex.editor.share.event.KeyListener;
import com.himamis.retex.editor.share.event.MathFieldListener;
import com.himamis.retex.editor.share.input.KeyboardInputAdapter;
import com.himamis.retex.editor.share.meta.MetaModel;
import com.himamis.retex.editor.share.meta.MetaModelParser;
import com.himamis.retex.editor.share.model.MathFormula;
import com.himamis.retex.renderer.share.CursorBox;
import com.himamis.retex.renderer.share.SelectionBox;
import com.himamis.retex.renderer.share.TeXFormula;
import com.himamis.retex.renderer.share.TeXIcon;
import com.himamis.retex.renderer.share.platform.Resource;
import com.himamis.retex.renderer.web.JlmLib;
import com.himamis.retex.renderer.web.graphics.JLMContext2d;

public class MathFieldW implements MathField, IsWidget {

	private static final MetaModel metaModel;

	static {
		metaModel = new MetaModelParser().parse(new Resource().loadResource(
				"/com/himamis/retex/editor/desktop/meta/Octave.xml"));
	}


	private MathFieldInternal mathFieldInternal;
	private Widget html;
	private Context2d ctx;
	private boolean focused = false;
	private TeXIcon lastIcon;
	private double ratio = 1;
	private KeyListener keyListener;
	private boolean rightAltDown = false;
	private boolean leftAltDown = false;
	private static Timer tick;
	static ArrayList<MathFieldW> instances = new ArrayList<MathFieldW>();

	/**
	 * 
	 * @param el
	 *            parent element
	 * @param context
	 *            drawing context
	 * @param listener
	 *            listener for special events
	 */
	public MathFieldW(Widget el, Context2d context,
			MathFieldListener listener) {
		html = el;
		mathFieldInternal = new MathFieldInternal(this);
		getHiddenTextArea();

		// el.getElement().setTabIndex(1);
		this.ctx = context;
		SelectionBox.touchSelection = false;

		mathFieldInternal.setSelectionMode(true);
		mathFieldInternal.setFieldListener(listener);
		mathFieldInternal.setType(TeXFormula.SANSSERIF);
		mathFieldInternal.setFormula(MathFormula.newFormula(metaModel));
		if (tick == null) {
			tick = new Timer() {

				@Override
				public void run() {
					CursorBox.blink = !CursorBox.blink;
					for (MathFieldW field : instances) {
						field.repaintWeb();
					}
				}
			};
			tick.scheduleRepeating(500);
		}
		instances.add(this);
		el.addDomHandler(new MouseDownHandler() {

			public void onMouseDown(MouseDownEvent event) {
				event.stopPropagation();
				setFocus(true);
				rightAltDown = false;
				leftAltDown = false;

			}
		}, MouseDownEvent.getType());

		setKeyListener(wrap, keyListener);
	}

	@Override
	public void setTeXIcon(TeXIcon icon) {
		this.lastIcon = icon;


		ctx.getCanvas().getStyle().setHeight(icon.getIconHeight() + 15,
				Unit.PX);

		ctx.getCanvas().getStyle().setWidth(icon.getIconWidth() + 30, Unit.PX);
		repaintWeb();
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// addFocusListener(new FocusListenerAdapterW(focusListener));
	}

	@Override
	public void setClickListener(ClickListener clickListener) {
		ClickAdapterW adapter = new ClickAdapterW(clickListener, this);
		adapter.listenTo(html);
	}

	public void setPixelRatio(double ratio) {
		this.ratio = ratio;
	}
	@Override
	public void setKeyListener(final KeyListener keyListener) {
		this.keyListener = keyListener;


	}

	private void setKeyListener(Widget html2, final KeyListener keyListener) {
		html2.addDomHandler(new KeyPressHandler() {

			public void onKeyPress(KeyPressEvent event) {
				// don't kill Ctrl+V or write V
				if (event.isControlKeyDown() && (event.getCharCode() == 'v'
						|| event.getCharCode() == 'V') || leftAltDown) {

					event.stopPropagation();
				} else {
					keyListener.onKeyTyped(
							new KeyEvent(event.getNativeEvent().getKeyCode(), 0,
									event.getCharCode()));
					event.stopPropagation();
					event.preventDefault();
				}

			}
		}, KeyPressEvent.getType());
		html2.addDomHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				int code = event.getNativeEvent().getKeyCode();
				code = fixCode(code);
				keyListener.onKeyReleased(
						new KeyEvent(code, getModifiers(event),
								getChar(event.getNativeEvent())));
				if (isRightAlt(event.getNativeEvent())) {
					rightAltDown = false;
				}
				if (isLeftAlt(event.getNativeEvent())) {
					leftAltDown = false;
				}
				if (code == 8 || code == 27) {
					event.preventDefault();
				}
				event.stopPropagation();

			}
		}, KeyUpEvent.getType());
		html2.addDomHandler(new KeyDownHandler() {

			public void onKeyDown(KeyDownEvent event) {
				if (isRightAlt(event.getNativeEvent())) {
					rightAltDown = true;
				}
				if (isLeftAlt(event.getNativeEvent())) {
					leftAltDown = true;
				}
				int code = event.getNativeEvent().getKeyCode();

				code = fixCode(code);
				boolean handled = keyListener.onKeyPressed(
						new KeyEvent(code, getModifiers(event),
								getChar(event.getNativeEvent())));
				// need to prevent sdefault for arrows to kill keypress
				// (otherwise strange chars appear in Firefox). Backspace/delete
				// also need killing.
				if (code == 8 || code == 27 || handled) {
					event.preventDefault();
				}
				event.stopPropagation();

			}
		}, KeyDownEvent.getType());

	}

	/**
	 * @param nativeEvent
	 *            native event
	 * @return whether this is right alt up/down event
	 */
	public static native boolean isRightAlt(NativeEvent nativeEvent) /*-{
		return nativeEvent.code == "AltRight";
	}-*/;

	/**
	 * @param nativeEvent
	 *            native event
	 * @return whether this is left alt up/down event
	 */
	public static native boolean isLeftAlt(NativeEvent nativeEvent) /*-{
		return nativeEvent.code == "AltLeft";
	}-*/;

	protected int fixCode(int code) {
		switch (code) {
		case 46:
			return KeyEvent.VK_DELETE;
		}
		return code;
	}

	public native void debug(String string) /*-{
		$wnd.console.log(string);

	}-*/;

	protected int getModifiers(com.google.gwt.event.dom.client.KeyEvent event) {
		return (event.isShiftKeyDown() ? KeyEvent.SHIFT_MASK : 0)
				+ (event.isControlKeyDown() || rightAltDown ? KeyEvent.CTRL_MASK
						: 0)
				+ (event.isAltKeyDown() ? KeyEvent.ALT_MASK : 0);
	}

	protected char getChar(NativeEvent nativeEvent) {
		return 0;
	}

	@Override
	public boolean hasParent() {
		return false;
	}

	@Override
	public void requestViewFocus() {
		setFocus(true);
	}

	@Override
	public void requestLayout() {

	}

	public KeyListener getKeyListener() {
		return mathFieldInternal;

	}

	public MetaModel getMetaModel() {
		return metaModel;
	}
	public void repaint() {

	}

	public void repaintWeb() {
		if (lastIcon == null) {
			return;
		}
		ctx.getCanvas()
				.setHeight((int) ((lastIcon.getIconHeight() + 15) * ratio));
		ctx.getCanvas()
				.setWidth((int) ((lastIcon.getIconWidth() + 30) * ratio));
		ctx.setFillStyle("rgb(255,255,255)");
		((JLMContext2d) ctx).scale2(ratio, ratio);
		ctx.fillRect(0, 0, ctx.getCanvas().getWidth(),
				lastIcon.getIconHeight() + 15);
		JlmLib.draw(lastIcon, ctx, 0, 0, "#000000", "#FFFFFF", null);

	}

	private native void debug(boolean blink) /*-{
		$wnd.console.log(blink);
	}-*/;

	// private native void trace(String txt) /*-{
	// $wnd.console.trace(txt);
	// }-*/;

	public boolean hasFocus() {
		return focused;
	}

	public void hideCopyPasteButtons() {
		// TODO Auto-generated method stub

	}

	public boolean showKeyboard() {
		// TODO Auto-generated method stub
		return false;
	}

	public void showCopyPasteButtons() {
		// TODO Auto-generated method stub

	}

	public void scroll(int dx, int dy) {
		// TODO Auto-generated method stub

	}

	public void fireInputChangedEvent() {
		// TODO Auto-generated method stub

	}

	public Widget asWidget() {
		return html;
	}

	public void setFormula(MathFormula formula) {
		this.mathFieldInternal.setFormula(formula);
	}

	public MathFormula getFormula() {
		return this.mathFieldInternal.getFormula();
	}

	private Timer focuser;
	private boolean pasteInstalled = false;
	public void setFocus(boolean focus) {
		if (focus) {
			startBlink();

			focuser = new Timer() {

				@Override
				public void run() {
					onFocusTimer();

				}
			};
			focuser.schedule(200);
			startEditing();
			wrap.getElement().focus();
			if (!pasteInstalled) {
				pasteInstalled = true;
				installPaste(this.getHiddenTextArea());
			}

		} else {
			if (focuser != null) {
				focuser.cancel();
			}
			this.lastIcon = null;
			instances.remove(this);
		}
		this.focused = focus;
	}

	/**
	 * Make sure the HTML element has focus and update to render cursor
	 */
	protected void onFocusTimer() {
		mathFieldInternal.update();
		wrap.getElement().focus();

	}

	private native void installPaste(Element target) /*-{
		var that = this;
		target.addEventListener('paste',
			function(a){
				if(a.clipboardData){
					that.@com.himamis.retex.editor.web.MathFieldW::insertString(Ljava/lang/String;)(a.clipboardData.getData("text/plain"));
				}else if($wnd.clipboardData){
					that.@com.himamis.retex.editor.web.MathFieldW::insertString(Ljava/lang/String;)($wnd.clipboardData.getData("Text"));
				}
			}
			);
		
	}-*/;

	public void startEditing() {
		if (mathFieldInternal.getEditorState().getCurrentField() == null) {
			mathFieldInternal.getCursorController()
					.lastField(mathFieldInternal.getEditorState());
		}
		// update even when cursor didn't change here
		mathFieldInternal.update();

	}

	public String deleteCurrentWord() {
		return this.mathFieldInternal.deleteCurrentWord();
	}

	public String getCurrentWord() {
		return this.mathFieldInternal.getCurrentWord();
	}

	public void selectNextArgument() {
		this.mathFieldInternal.selectNextArgument();

	}

	public void startBlink() {
		if (!instances.contains(this)) {
			instances.add(this);
		}
	}

	public void paste() {
		// insertString(getSystemClipboardChromeWebapp(html.getElement()));

	}

	private void insertString(String text) {
		KeyboardInputAdapter.insertString(mathFieldInternal, text);

		mathFieldInternal.selectNextArgument();

		mathFieldInternal.update();

	}

	Element el = null;
	private TextArea wrap;
	private Element getHiddenTextArea() {
		if (el == null) {
			el = getHiddenTextAreaNative(instances.size());
			mathFieldInternal.debug("GWT connect");
			wrap = TextArea.wrap(el);

			wrap.addFocusHandler(new FocusHandler() {

				public void onFocus(FocusEvent event) {
					event.stopPropagation();

				}
			});
			wrap.addBlurHandler(new BlurHandler() {

				public void onBlur(BlurEvent event) {
					event.stopPropagation();

				}
			});
		}
		Timer tim = new Timer() {

			@Override
			public void run() {
				((Panel) html.getParent()).add(wrap);

			}
		};
		tim.schedule(5000);

		return el;
	}

	private static native Element getHiddenTextAreaNative(int counter) /*-{
		var hiddenTextArea = $doc.getElementById('hiddenCopyPasteLatexArea'
				+ counter);
		if (!hiddenTextArea) {
			hiddenTextArea = $doc.createElement("textarea");
			hiddenTextArea.id = 'hiddenCopyPasteLatexArea' + counter;
			hiddenTextArea.style.position = 'absolute';
			hiddenTextArea.style.zIndex = '100';
			hiddenTextArea.style.left = '-1000px';
			hiddenTextArea.style.top = '0px'; //prevent messed up scrolling in FF/IE
			$doc.body.appendChild(hiddenTextArea);
			if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i
					.test(window.navigator.userAgent)) {
				hiddenTextArea.setAttribute("disabled", "true");
			}
		}
		//hiddenTextArea.value = '';
		return hiddenTextArea;
	}-*/;



	public void copy() {
		nativeCopy(mathFieldInternal.copy());

	}

	private native void nativeCopy(String value) /*-{
		var copyFrom = this.@com.himamis.retex.editor.web.MathFieldW::getHiddenTextArea()();
		copyFrom.value = value;
		copyFrom.select();
		$doc.execCommand('copy');

	}-*/;

	protected void listenToTextArea() {
		if (keyListener != null) {
			getHiddenTextArea();
			this.setKeyListener(wrap, keyListener);
		}
	}

	public native boolean useCustomPaste() /*-{
		return false;
	}-*/;

	public void moveCaretLeftOrRight(boolean right) {
		if (right) {
			mathFieldInternal.getCursorController()
					.lastField(mathFieldInternal.getEditorState());
		} else {
			mathFieldInternal.getCursorController()
					.firstField(mathFieldInternal.getEditorState());
		}
		// update even when cursor didn't change here
		mathFieldInternal.update();

	}

}
