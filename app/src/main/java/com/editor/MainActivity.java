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
        new Sintaxe.Java().aplicar(editor);
		new AutoCompletar(this, editor,
							  "System.out.println(", "System.gc()", "Toast.makeText(",
							  "while(", "if(", "for(", "switch(", "try {\n\n\n} catch(Exception e) {\nSystem.out.println(\"erro: \"+e);\n}", 
							  ".replace(", ".split(", ".trim()", ".toString()", "setContentView(", ".setText(", ".getText(",

							  "public", "private", "protected", "static", "class", "void", "final",

							  "int", "String", "double", "float", "byte", "short", "Integer", "long", "char", "boolean",

							  "import", "package", "new", "return", "case", "break;", "continue;", "else", "this",

							  "@Override", "@javascriptInterface",

							  "WebView", "EditText", "TextView", "Button", "GLSurfaceView", "SurfaceView",
							  "Canvas", "Toast", "View", "MediaPlayer"
						  );
    }
}
