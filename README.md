## ERS (Editor Renderizado com SurfaceView)
é um editor simples de código com sintaxe padrão em Java para Android. Ele não utiliza bibliotecas externas.

## o editor contém:
1. Autocomplete personalizável
2. Destaque de sintaxe divida por sub classes
3. Auto-indentação
4. fechamento automático de chaves

## sintaxes:
Java
Javascript
Fp
Assembly (Aarch64 linux)
C

## uso:

```Java
new Sintaxe.Java().aplicar(editText);
new AutoCompletar(activity, editText, AutoCompletar.sintaxe("java"));

// ou

new AutoCompletar(activity, editText, "palavras", "chave", "para", "sugestoes");

// para desativar o uso do autocomplete use:

AutoCompletar.autocomplete = false;
```

## [NOVO]:


## ferramenta de busca/substuição:
para ativar o ferramenta de busca no codigo, digite:

//busca
sem espaços, e a ferramenta de busca e substuição será ativada.
