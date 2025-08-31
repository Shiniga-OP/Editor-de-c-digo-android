package com.editor;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.text.TextPaint;
import android.view.View;
import android.text.Editable;
import android.os.Handler;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.graphics.Rect;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;

public class MainActivity extends Activity { 
     public EditorCodigo editor;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
		editor = findViewById(R.id.editor);
		new Sintaxe.C().aplicar(editor);
		/*
        new Sintaxe.C().aplicar(editor);
		new AutoCompletar(this, editor, AutoCompletar.sintaxe("C")); */
		AutoCompletar.autocomplete = true;
    }
}
