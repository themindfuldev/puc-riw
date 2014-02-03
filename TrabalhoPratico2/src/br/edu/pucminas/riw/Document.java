package br.edu.pucminas.riw;

import java.util.List;

/**
 * Representa um documento a ser manipulado por este aplicativo.
 */
public class Document {
	private int index;
	private List<String> terms;
	private String content;

	public Document(String content, int index) {
		this.content = content;
		this.index = index;
	}

	public String getContent() {
		return content;
	}

	public int getIndex() {
		return index;
	}

	public List<String> getTerms() {
		return terms;
	}

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}

	@Override
	public String toString() {
		return toString(content.length());
	}

	/**
	 * Escreve a descrição do objeto
	 * 
	 * @param maxSize
	 *            tamanho máximo do conteúdo a ser exibido
	 * @return a descrição do objeto
	 */
	public String toString(int maxSize) {
		String ret = "Documento " + index + ": "
				+ content.substring(0, maxSize).trim() + "...";
		return ret;
	}

}
