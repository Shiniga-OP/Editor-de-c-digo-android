package com.editor;

import android.graphics.Paint;
import android.text.Editable;
import android.view.View;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

public class EditorCodigo extends View {
    public final Paint pincelTxt;
    public final Paint pincelNums;
    public final Paint pincelFundoLin;
    public final Rect areaMedida;
    public Editable conteudo;
    public int posX;
    public int linSele;
    public int colSele;
    public int alturaLin = 50;
    public int larguraCar = 25;
    public List<Destaque> destaques = new ArrayList<>();
    public int corTexto = Color.BLACK;
	public Sintaxe sintaxe = null;

    public EditorCodigo(Context ctx, AttributeSet atribs) {
        super(ctx, atribs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);

        pincelTxt = new Paint();
        pincelTxt.setColor(Color.BLACK);
        pincelTxt.setTextSize(40);
        pincelTxt.setAntiAlias(true);

        pincelNums = new Paint();
        pincelNums.setColor(Color.GRAY);
        pincelNums.setTextSize(35);
        pincelNums.setAntiAlias(true);

        pincelFundoLin = new Paint();
        pincelFundoLin.setColor(Color.parseColor("#E3F2FD"));

        areaMedida = new Rect();
        conteudo = Editable.Factory.getInstance().newEditable("");

        setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean foco) {
					if(foco) abrirTeclado();
				}
			});

        setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int tecla, KeyEvent e) {
					if(e.getAction() == KeyEvent.ACTION_DOWN) {
						switch(tecla) {
							case KeyEvent.KEYCODE_DEL:
								rmCaractere();
								return true;
							case KeyEvent.KEYCODE_ENTER:
								addCaractere('\n');
								return true;
							case KeyEvent.KEYCODE_DPAD_UP:
								moverCursorCima();
								return true;
							case KeyEvent.KEYCODE_DPAD_DOWN:
								moverCursorBaixo();
								return true;
							case KeyEvent.KEYCODE_DPAD_LEFT:
								moverCursorEsquerda();
								return true;
							case KeyEvent.KEYCODE_DPAD_RIGHT:
								moverCursorDireita();
								return true;
							default:
								if(e.getUnicodeChar() != 0) {
									addCaractere((char) e.getUnicodeChar());
									return true;
								}
								return false;
						}
					}
					return false;
				}
			});
    }

    public static class Destaque {
        public int inicio;
        public int fim;
        public int cor;

        public Destaque(int inicio, int fim, int cor) {
            this.inicio = inicio;
            this.fim = fim;
            this.cor = cor;
        }
    }

    public void setDestaques(List<Destaque> destaques) {
        this.destaques = destaques;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderNumLins(canvas);

        String textoCompleto = conteudo.toString();
        String[] lins = textoCompleto.split("\n", -1);

        for(int i = 0; i < lins.length; i++) {
            int startLineIndex = obterIndice(i, lins);
            int y = i * alturaLin - posX;
            if(y >= -alturaLin && y <= getHeight()) desenharLinha(canvas, lins[i], startLineIndex, 60, y + alturaLin - 10);
        }
        int cursorX = colSele * larguraCar + 60;
        int cursorY = linSele * alturaLin - posX;
        canvas.drawLine(cursorX, cursorY, cursorX, cursorY + alturaLin, pincelTxt);
    }

    public int obterIndice(int linNum, String[] lines) {
        int idc = 0;
        for(int i = 0; i < linNum; i++) idc += lines[i].length() + 1;
        return idc;
    }

    private void desenharLinha(Canvas canvas, String lin, int inicioGlobal, int x, int y) {
        int xAtual = x;
        int corAtual = corTexto;
        int inicio = 0;

        for(int i = 0; i <= lin.length(); i++) {
            int indiceGlobal = inicioGlobal + i;
            int cor = cor(indiceGlobal);
            if(i == lin.length() || cor != corAtual) {
                if(i > inicio) {
                    String seg = lin.substring(inicio, i);
                    pincelTxt.setColor(corAtual);
                    canvas.drawText(seg, xAtual, y, pincelTxt);
                    xAtual += pincelTxt.measureText(seg);
                }
                inicio = i;
                corAtual = cor;
            }
        }
    }

    public int cor(int i) {
        for(Destaque destaque : destaques) {
            if(i >= destaque.inicio && i < destaque.fim) return destaque.cor;
        }
        return corTexto;
    }

    public void abrirTeclado() {
        InputMethodManager gerenciadorEntrada = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        gerenciadorEntrada.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    public void addCaractere(char c) {
		int pos = calcularPosNoTxt();
		conteudo.insert(pos, String.valueOf(c));
		attCursorPos(c);
		invalidate();
		if(sintaxe != null) sintaxe.att();
	}

    public void rmCaractere() {
        if(calcularPosNoTxt() > 0) {
            int pos = calcularPosNoTxt() - 1;
            if(pos < conteudo.length() && conteudo.charAt(pos) == '\n') {
                String[] lins = conteudo.toString().split("\n", -1);
                if(linSele > 0) {
                    int novaLin = linSele - 1;
                    int novaCol = lins[novaLin].length();
                    conteudo.delete(pos, pos + 1);
                    linSele = novaLin;
                    colSele = novaCol;
                }
            } else {
                conteudo.delete(pos, pos + 1);
                attCursorPosRemocao();
            }
            invalidate();
			if(sintaxe != null) sintaxe.att();
        }
    }

    public void renderNumLins(Canvas canvas) {
        String[] lins = conteudo.toString().split("\n", -1);
        for(int i = 0; i < lins.length; i++) {
            int posicaoY = i * alturaLin - posX;
            if(posicaoY >= -alturaLin && posicaoY <= getHeight()) {
                String numero = String.valueOf(i + 1);
                canvas.drawText(numero, 10, posicaoY + alturaLin - 10, pincelNums);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            abrirTeclado();
            int y = (int) e.getY() + posX;
            linSele = y / alturaLin;
            String[] lins = conteudo.toString().split("\n", -1);
            if(linSele >= lins.length) linSele = lins.length - 1;
            if(linSele < 0) linSele = 0;
            int x = (int) e.getX() - 60;
            colSele = Math.max(0, Math.min(lins[linSele].length(), x / larguraCar));
            invalidate();
            return true;
        }
        return super.onTouchEvent(e);
    }

    public int calcularPosNoTxt() {
        String[] lins = conteudo.toString().split("\n", -1);
        int pos = 0;
        for(int i = 0; i < linSele; i++) pos += lins[i].length() + 1;
			
        pos += colSele;
        return Math.min(pos, conteudo.length());
    }

    public void attCursorPos(char c) {
        if(c == '\n') {
            linSele++;
            colSele = 0;
        } else colSele++;
        ajustarRolagem();
    }

    public void attCursorPosRemocao() {
        if(colSele > 0) colSele--;
        else if(linSele > 0) {
            linSele--;
            String[] lins = conteudo.toString().split("\n");
            colSele = lins[linSele].length();
        }
        ajustarRolagem();
    }

    public void ajustarRolagem() {
        int posicaoYCursor = linSele * alturaLin;
        if(posicaoYCursor < posX) posX = posicaoYCursor;
        else if(posicaoYCursor + alturaLin > posX + getHeight()) posX = posicaoYCursor + alturaLin - getHeight();
        invalidate();
    }

    public void moverCursorCima() {
        if(linSele > 0) {
            linSele--;
            String[] lins = conteudo.toString().split("\n");
            colSele = Math.min(colSele, lins[linSele].length());
            ajustarRolagem();
        }
    }

    public void moverCursorBaixo() {
        String[] lins = conteudo.toString().split("\n");
        if(linSele < lins.length - 1) {
            linSele++;
            colSele = Math.min(colSele, lins[linSele].length());
            ajustarRolagem();
        }
    }

    public void moverCursorEsquerda() {
        if(colSele > 0) colSele--;
        else if(linSele > 0) {
            linSele--;
            String[] lins = conteudo.toString().split("\n");
            colSele = lins[linSele].length();
        }
        ajustarRolagem();
    }

    public void moverCursorDireita() {
        String[] lins = conteudo.toString().split("\n");
        if(colSele < lins[linSele].length()) colSele++;
        else if(linSele < lins.length - 1) {
            linSele++;
            colSele = 0;
        }
        ajustarRolagem();
    }

    public void defTexto(String novoTxt) {
        conteudo = Editable.Factory.getInstance().newEditable(novoTxt);
        linSele = 0;
        colSele = 0;
        invalidate();
		if(sintaxe != null) sintaxe.att();
    }

    public String obterTexto() {
        return conteudo.toString();
    }
}
