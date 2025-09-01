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
    public List<Destaque> destaques = new ArrayList<>();
    public int corTexto = Color.BLACK;
	public Sintaxe sintaxe;
	public AutoCompletar auto;

    public EditorCodigo(Context ctx, AttributeSet atribs) {
        super(ctx, atribs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);

        pincelTxt = new Paint();
		pincelTxt.setColor(Color.BLACK);
		pincelTxt.setTextSize(30);
		pincelTxt.setAntiAlias(true);
		pincelTxt.setTypeface(Typeface.MONOSPACE);

        pincelNums = new Paint();
        pincelNums.setColor(Color.GRAY);
        pincelNums.setTextSize(20);
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
		public int prioridade;
		public boolean negrito;
		public boolean italico;

		public Destaque(int inicio, int fim, int cor, int prioridade, boolean negrito, boolean italico) {
			this.inicio = inicio;
			this.fim = fim;
			this.cor = cor;
			this.prioridade = prioridade;
			this.negrito = negrito;
			this.italico = italico;
		}
	}

	public void renderLinha(Canvas canvas, String lin, int inicioGlobal, int x, int y) {
		int xAtual = x;
		int corAtual = corTexto;
		boolean negritoAtual = false;
		boolean italicoAtual = false;
		int inicio = 0;
		// pincelTxt.setShadowLayer(2f, 2f, 2f, 0xFF000000);

		for(int i = 0; i <= lin.length(); i++) {
			int indiceGlobal = inicioGlobal + i;
			Destaque destaqueAtual = obterDestaque(indiceGlobal);
			int cor = destaqueAtual != null ? destaqueAtual.cor : corTexto;
			boolean negrito = destaqueAtual != null ? destaqueAtual.negrito : false;
			boolean italico = destaqueAtual != null ? destaqueAtual.italico : false;

			if(i == lin.length() || cor != corAtual || negrito != negritoAtual || italico != italicoAtual) {
				if(i > inicio) {
					String seg = lin.substring(inicio, i);
					pincelTxt.setColor(0xFF000000); // cor da borda
					pincelTxt.setStyle(Paint.Style.STROKE);
					pincelTxt.setStrokeWidth(1.5f); // espessura da borda
					canvas.drawText(seg, xAtual, y, pincelTxt);

					pincelTxt.setColor(corAtual);
					pincelTxt.setStyle(Paint.Style.FILL);
					canvas.drawText(seg, xAtual, y, pincelTxt);

					Typeface tipoAtual = Typeface.MONOSPACE;
					int estilo = Typeface.NORMAL;
					if(negritoAtual && italicoAtual) estilo = Typeface.BOLD_ITALIC;
					else if(negritoAtual) estilo = Typeface.BOLD;
					else if(italicoAtual) estilo = Typeface.ITALIC;
					
					pincelTxt.setTypeface(Typeface.create(tipoAtual, estilo));

					xAtual += pincelTxt.measureText(seg);
				}
				inicio = i;
				corAtual = cor;
				negritoAtual = negrito;
				italicoAtual = italico;
			}
		}
	}

	public Destaque obterDestaque(int i) {
		Destaque melhorDestaque = null;
		for(Destaque destaque : destaques) {
			if(i >= destaque.inicio && i < destaque.fim) {
				if(melhorDestaque == null || destaque.prioridade > melhorDestaque.prioridade) melhorDestaque = destaque;
			}
		}
		return melhorDestaque;
	}
	
	public float medirAteColuna(String linha, int col) {
		return pincelTxt.measureText(linha.substring(0, Math.min(col, linha.length())));
	}

    public void defDestaques(List<Destaque> destaques) {
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
			int inicioLin = obterIndice(i, lins);
			int y = i * alturaLin - posX;
			if(y >= -alturaLin && y <= getHeight()) renderLinha(canvas, lins[i], inicioLin, 60, y + alturaLin - 10);
		}
		String linhaAtual = lins[linSele];
		int cursorX = (int) medirAteColuna(linhaAtual, colSele) + 60;
		float baselin = linSele * alturaLin - posX + (alturaLin - 3);
		float cursorTopo = baselin - pincelTxt.getTextSize();
		canvas.drawLine(cursorX, cursorTopo, cursorX, baselin, pincelTxt);
	}

    public int obterIndice(int linNum, String[] lins) {
        int idc = 0;
        for(int i = 0; i < linNum; i++) idc += lins[i].length() + 1;
        return idc;
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
		if(auto != null) auto.att(conteudo, pos, 0, 1);
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
			if(auto != null) auto.att(conteudo, pos, 1, 0);
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
			String linha = lins[linSele];
			colSele = 0;
			float acumulado = 0;
			for(int i = 0; i < linha.length(); i++) {
				float larguraChar = pincelTxt.measureText(String.valueOf(linha.charAt(i)));
				if(acumulado + larguraChar / 2 >= x) {
					colSele = i;
					break;
				}
				acumulado += larguraChar;
				colSele = i + 1;
			}
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
	
	public void ajustarRolagem(int x, int y) {
        if(x < posX) posX = y;
        else if(x + alturaLin > posX + getHeight()) posX = y + alturaLin - getHeight();
        invalidate();
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
	
	public void defSelecao(int pos) {
		String texto = conteudo.toString();
		int lin = 0;
		int col = 0;
		int atual = 0;
		String[] lins = texto.split("\n", -1);
		for(lin = 0; lin < lins.length; lin++) {
			int tam = lins[lin].length();
			if(atual + tam >= pos) {
				col = pos - atual;
				break;
			}
			atual += tam + 1; // +1 para o \n
		}
		linSele = lin;
		colSele = col;
		invalidate();
	}

    public void defTexto(String novoTxt) {
        conteudo = Editable.Factory.getInstance().newEditable(novoTxt);
        linSele = 0;
        colSele = 0;
        invalidate();
		if(sintaxe != null) sintaxe.att();
    }
	
	public int obterCursorY() {
		return linSele * alturaLin - posX;
	}
	
	public int obterCursorX() {
		return (int) medirAteColuna(conteudo.toString().split("\n", - 1)[linSele], colSele) + 60;
	}

    public String obterTexto() {
        return conteudo.toString();
    }
}
