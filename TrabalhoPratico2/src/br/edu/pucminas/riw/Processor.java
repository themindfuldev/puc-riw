package br.edu.pucminas.riw;


/**
 * Classe de entrada do sistema, que chama os componentes de processamento e
 * lida com a interface de linha de comando e saída de dados.
 * 
 * @author Tiago Romero Garcia
 */
public class Processor {
	private static final String PARAM_ENCODING = "-e=";
	private static final String PARAM_FILENAME = "-f=";
	private static final String PARAM_QUERIESFILE = "-q=";
	private static final String PARAM_DEBUG = "-d";
	private static final String PARAM_HELP = "-h";
	private static final String MESSAGE_USAGE = "Uso: Processor (-f=nomeDoArquivo) (-q=nomeDoArquivo) (-e=codificacao) (-d) (-h)";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Argumentos invalidos. " + MESSAGE_USAGE;
	private static final String MESSAGE_REPEATED_ARGUMENTS = "Argumentos não podem ser repetidos. " + MESSAGE_USAGE;
	private static final String DEFAULT_DOCS_FILE = "docs.txt";
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_QUERIES_FILE = "queries.txt";
	
	private String docsFileName = DEFAULT_DOCS_FILE;
	private String queriesFileName = DEFAULT_QUERIES_FILE;
	private String encoding = DEFAULT_ENCODING;
	private boolean debugMode = false;

	/**
	 * Baseado no número fornecido de argumentos, realiza a preparação dos
	 * parâmetros a serem passados adiante
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
				if (docsFileName.equals(DEFAULT_DOCS_FILE) == false) {
					throw new IllegalStateException(MESSAGE_REPEATED_ARGUMENTS);
				}
				
				docsFileName = argument.replace(PARAM_FILENAME, "");
				
				if (docsFileName.length() == 0) {
					throw new IllegalStateException(MESSAGE_INVALID_ARGUMENTS);
				}
			}
			else if (argument.startsWith(PARAM_QUERIESFILE)) {
				if (queriesFileName.equals(DEFAULT_QUERIES_FILE) == false) {
					throw new IllegalStateException(MESSAGE_REPEATED_ARGUMENTS);
				}
				
				queriesFileName = argument.replace(PARAM_QUERIESFILE, "");
				
				if (queriesFileName.length() == 0) {
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
	
	public String getDocsFileName() {
		return docsFileName;
	}
	
	public String getQueriesFileName() {
		return queriesFileName;
	}

	public String getEncoding() {
		return encoding;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Este método pode receber 4 parâmetros da linha de comando:
	 * -d    			- Se for passado este argumento, imprimirá as stack
	 *            		  traces de eventuais exceções que ocorrerem. 
	 * -f=nomeDoArquivo - Se for passado este argumento, será o caminho do arquivo
	 * 			 		  de documentos a ser processado.
	 *           		  Caso não seja fornecido, processará o arquivo que estiver
	 *            		  definido na constante DEFAULT_DOCS_FILE.
	 * -q=nomeDoArquivo - Se for passado este argumento, será o caminho do arquivo
	 * 			 		  de consultas a ser processado.
	 *           		  Caso não seja fornecido, processará o arquivo que estiver
	 *            		  definido na constante DEFAULT_QUERIES_FILE.
	 * -e=codificação 	- Se for passado este argumento, será o encoding do arquivo
	 * 			  		  a ser processado.
	 *           		  Caso não seja fornecido, utilizará o encoding que estiver
	 *            		  definido na constante DEFAULT_ENCODING.
	 * -h               - Se for passado este argumento, exibirá a usagem do aplicativo.
	 * @param args array de argumentos
	 */
	public static void main(String[] args) {
		Processor processor = null;
		try {
			processor = new Processor(args);
			
			DocumentsProcessor documentProcessor = new DocumentsProcessor(processor.getDocsFileName(), processor.getEncoding());
			
			documentProcessor.processQueries(processor.getQueriesFileName(), processor.getEncoding());
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
