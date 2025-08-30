package com.editor;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends Activity { 
     
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
		EditText editor = findViewById(R.id.editor);
        new Sintaxe.C().aplicar(editor);
		new AutoCompletar(this, editor, AutoCompletar.sintaxe("C"));
		AutoCompletar.autocomplete = true;
    }
}
