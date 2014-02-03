package br.edu.pucminas.riw;

/**
 * Exce��o de n�vel de neg�cio para encapsular quaisquer exce��es e retornar
 * mensagens �teis ao usu�rio
 * 
 * @author Tiago Romero Garcia
 */
public class DocumentProcessorException extends Throwable {
	private static final long serialVersionUID = 1L;

	private String message;

	public DocumentProcessorException() {
		super();
	}

	public DocumentProcessorException(String message, Throwable cause) {
		super();
		this.message = message;
		this.initCause(cause);
	}

	public String getMessage() {
		return message;
	}

}
