package com.editor;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.text.Layout;

public class AutoCompletar {
    private final Activity activity;
    private final EditText editText;
    private final String[] palavras;
    private final PopupWindow popup;
    private final LinearLayout layout;

    public AutoCompletar(Activity act, EditText et, String... lista) {
        activity = act;
        editText = et;
        palavras = lista;
        layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        popup = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        popup.setOutsideTouchable(true);
        popup.setFocusable(false);

        editText.addTextChangedListener(new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
				@Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
				@Override public void afterTextChanged(Editable s) {
					mostrarSugestoes(obterTokenAtual());
				}
			});
    }

    private void mostrarSugestoes(String token) {
        layout.removeAllViews();
        if(token.length() == 0) {
            popup.dismiss();
            return;
        }

        List<String> sugestoes = new ArrayList<String>();
        for(String p : palavras) {
            if(p.startsWith(token)) sugestoes.add(p);
        }

        if(sugestoes.isEmpty()) {
            popup.dismiss();
            return;
        }

        for(final String s : sugestoes) {
            TextView t = new TextView(activity);
            t.setText(s);
            t.setPadding(20, 10, 20, 10);
            t.setTextSize(16);
            t.setTextColor(Color.BLACK);
            t.setBackgroundColor(0xFFE0E0E0);
            t.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						substituirToken(s);
						popup.dismiss();
						editText.requestFocus();
						InputMethodManager im = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
						im.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
					}
				});
            layout.addView(t);
        }
		
		int offset = editText.getSelectionStart();
		Layout layout = editText.getLayout();

		if (layout != null && offset >= 0) {
			int line = layout.getLineForOffset(offset);
			int baseline = layout.getLineBaseline(line);
			int ascent = layout.getLineAscent(line);
			float x = layout.getPrimaryHorizontal(offset);

			int[] loc = new int[2];
			editText.getLocationOnScreen(loc);

			int popupX = (int)(loc[0] + x);
			int popupY = loc[1] + baseline + ascent;

			if (!popup.isShowing()) {
				popup.setWidth(editText.getWidth() / 2);
				popup.showAtLocation(editText, Gravity.NO_GRAVITY, popupX, popupY);
			} else {
				popup.update(popupX, popupY, popup.getWidth(), popup.getHeight());
			}
		}
    }

    private String obterTokenAtual() {
        int pos = editText.getSelectionStart();
        if(pos == 0) return "";
        CharSequence txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;
        return txt.subSequence(i + 1, pos).toString();
    }

    private void substituirToken(String nova) {
        int pos = editText.getSelectionStart();
        Editable txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;
        txt.replace(i + 1, pos, nova);
        editText.setSelection(i + 1 + nova.length());
    }

    private int cursorX() {
        int[] loc = new int[2];
        editText.getLocationOnScreen(loc);
        return loc[0];
    }

    private int cursorY() {
        int[] loc = new int[2];
        editText.getLocationOnScreen(loc);
        return loc[1] + editText.getHeight();
    }
}
