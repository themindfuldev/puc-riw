package br.edu.pucminas.riw;


/**
 * Classe de entrada do sistema, que chama os componentes de processamento e
 * lida com a interface de linha de comando e sa�da de dados.
 * 
 * @author Tiago Romero Garcia
 */
public class Processor {
	private static final String PARAM_ENCODING = "-e=";
	private static final String PARAM_FILENAME = "-f=";
	private static final String PARAM_DEBUG = "-d";
	private static final String PARAM_HELP = "-h";
	private static final String MESSAGE_USAGE = "Uso: Processor (-f=nomeDoArquivo) (-e=codificacao) (-d) (-h)";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Argumentos invalidos. " + MESSAGE_USAGE;
	private static final String MESSAGE_REPEATED_ARGUMENTS = "Argumentos n�o podem ser repetidos. " + MESSAGE_USAGE;
	private static final String DEFAULT_FILE = "docs.txt";
	private static final String DEFAULT_ENCODING = "UTF-8";
	
	private String fileName = DEFAULT_FILE;
	private String encoding = DEFAULT_ENCODING;
	private boolean debugMode = false;

	/**
	 * Baseado no n�mero fornecido de argumentos, realiza a prepara��o dos
	 * par�metros a serem passados adiante
	 * 
	 * @param args array de argumentos
	 */
	private Processor(String[] args) {
		for (String argument: args) {
			if (argument.equals(PARAM_HELP)) {
				throw new IllegalStateException(MESSAGE_USAGE);
			}
			else if (argument.equals(PARAM_DEBUG)) {
				if (debugMode == true) {
					throw new IllegalStateException(MESSAGE_REPEATED_ARGUMENTS);
				}
				
				debugMode = true;
			}
			else if (argument.startsWith(PARAM_FILENAME)) {
				if (fileName.equals(DEFAULT_FILE) == false) {
					throw new IllegalStateException(MESSAGE_REPEATED_ARGUMENTS);
				}
				
				fileName = argument.replace(PARAM_FILENAME, "");
				
				if (fileName.length() == 0) {
					throw new IllegalStateException(MESSAGE_INVALID_ARGUMENTS);
				}
			}
			else if (argument.startsWith(PARAM_ENCODING)) {
				if (encoding.equals(DEFAULT_ENCODING) == false) {
					throw new IllegalStateException(MESSAGE_REPEATED_ARGUMENTS);
				}
				
				encoding = argument.replace(PARAM_ENCODING, "");
				
				if (encoding.length() == 0) {
					throw new IllegalStateException(MESSAGE_INVALID_ARGUMENTS);
				}
			}
			else {
				throw new IllegalStateException(MESSAGE_INVALID_ARGUMENTS);
			}
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getEncoding() {
		return encoding;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Este m�todo pode receber 4 par�metros da linha de comando:
	 * -d    			- Se for passado este argumento, imprimir� as stack
	 *            		  traces de eventuais exce��es que ocorrerem. 
	 * -f=nomeDoArquivo - Se for passado este argumento, ser� o caminho do arquivo
	 * 			 		  a ser processado.
	 *           		  Caso n�o seja fornecido, processar� o arquivo que estiver
	 *            		  definido na constante DEFAULT_FILE.
	 * -e=codifica��o 	- Se for passado este argumento, ser� o encoding do arquivo
	 * 			  		  a ser processado.
	 *           		  Caso n�o seja fornecido, utilizar� o encoding que estiver
	 *            		  definido na constante DEFAULT_ENCODING.
	 * -h               - Se for passado este argumento, exibir� a usagem do aplicativo.
	 * @param args array de argumentos
	 */
	public static void main(String[] args) {
		Processor processor = null;
		try {
			processor = new Processor(args);
			
			DocumentsProcessor documentProcessor = new DocumentsProcessor();
			documentProcessor.processDocuments(processor.getFileName(), processor.getEncoding());
		} 
		catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}
		catch (DocumentProcessorException e) {
			System.out.println(e.getMessage());
			if (processor != null && processor.isDebugMode() == true) {
				e.printStackTrace();
			}
		}
	}

}
