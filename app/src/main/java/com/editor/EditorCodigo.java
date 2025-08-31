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

	public EditorCodigo(Context ctx, AttributeSet atributos) {
		super(ctx, atributos);
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

	public void abrirTeclado() {
		InputMethodManager gerenciadorEntrada = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		gerenciadorEntrada.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		renderNumLins(canvas);
		// fundo:
		int posY = linSele * alturaLin - posX;
		canvas.drawRect(0, posY, getWidth(), posY + alturaLin, pincelFundoLin);
		// conteudo:
		String[] lins = conteudo.toString().split("\n");
		for(int i = 0; i < lins.length; i++) {
			int posYTxt = i * alturaLin - posX;
			if(posYTxt >= -alturaLin && posYTxt <= getHeight()) canvas.drawText(lins[i], 60, posYTxt + alturaLin - 10, pincelTxt);
		}
		// cursor:
		int cursorX = colSele * larguraCar + 60;
		int cursorY = linSele * alturaLin - posX;
		canvas.drawLine(cursorX, cursorY, cursorX, cursorY + alturaLin, pincelTxt);
	}

	public void addCaractere(char c) {
		int pos = calcularPosNoTxt();
		conteudo.insert(pos, String.valueOf(c));
		attCursorPos(c);
		invalidate();
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
		}
	}

	public void renderNumLins(Canvas canvas) {
		String[] lins = conteudo.toString().split("\n", -1);
		int quantidadeLinhas = lins.length;

		for(int i = 0; i < quantidadeLinhas; i++) {
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
			String[] linhas = conteudo.toString().split("\n");
			colSele = linhas[linSele].length();
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
		String[] linhas = conteudo.toString().split("\n");
		if(linSele < linhas.length - 1) {
			linSele++;
			colSele = Math.min(colSele, linhas[linSele].length());
			ajustarRolagem();
		}
	}

	public void moverCursorEsquerda() {
		if(colSele > 0) {
			colSele--;
		} else if(linSele > 0) {
			linSele--;
			String[] linhas = conteudo.toString().split("\n");
			colSele = linhas[linSele].length();
		}
		ajustarRolagem();
	}

	public void moverCursorDireita() {
		String[] linhas = conteudo.toString().split("\n");
		if(colSele < linhas[linSele].length()) colSele++;
		else if(linSele < linhas.length - 1) {
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
	}

	public void defTexto(String novoTxt, int linSele, int colSele) {
		conteudo = Editable.Factory.getInstance().newEditable(novoTxt);
		this.linSele = linSele;
		this.colSele = linSele;
		invalidate();
	}

	public String obterTexto() {
		return conteudo.toString();
	}
}
