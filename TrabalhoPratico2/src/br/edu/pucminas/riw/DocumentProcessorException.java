package br.edu.pucminas.riw;

/**
 * Exceção de nível de negócio para encapsular quaisquer exceções e retornar
 * mensagens úteis ao usuário
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
