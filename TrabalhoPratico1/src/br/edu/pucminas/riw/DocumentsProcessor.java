package br.edu.pucminas.riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * Classe processadora de documentos.
 * 
 * @author Tiago Romero Garcia
 */
public class DocumentsProcessor {
	private static final String MESSAGE_READING_ERROR = "Erro na leitura do arquivo.";
	private static final String MESSAGE_WRITING_ERROR = "Erro na gravação do arquivo.";
	private static final String MESSAGE_FILE_NOT_FOUND = "Arquivo nao encontrado.";
	private static final String STOPWORDS_FILE = "stopwords.txt";
	private static final String PREPROCESSED_RESULTS_FILE = "step1-preprocessed.txt";
	private static final String STOPWORDS_REMOVED_RESULTS_FILE = "step2-stopwords-removed.txt";
	private static final String STEMMED_RESULTS_FILE = "step3-stemmed.txt";
	private static final String TERM_DOCUMENT_MATRIX_RESULTS_FILE = "step4-term-document-matrix.txt";
	private static final String BOOLEAN_MODEL_RESULTS_FILE = "step5-boolean-model.txt";
	private static final String VECTOR_MODEL_RESULTS_FILE = "step6-vector-model.txt";

	private List<String> stopwordsList;
	private List<String> originalDocumentsList;
	private List<List<String>> termsDocumentsList;
	private Map<String, List<Integer>> termsIndexMap;
	private int[][] termDocumentMatrix;
	private byte[][] booleanModelMatrix;
	private int[][] vectorModelMatrix;

	public DocumentsProcessor() throws DocumentProcessorException {
		stopwordsList = new ArrayList<String>();
		originalDocumentsList = new ArrayList<String>();
		termsDocumentsList = new ArrayList<List<String>>();
		termsIndexMap = new TreeMap<String, List<Integer>>();
		loadStopwords();
	}

	/**
	 * Lê o arquivo de stopwords e converte numa lista de stopwords
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @throws DocumentProcessorException
	 */
	private void loadStopwords() throws DocumentProcessorException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					STOPWORDS_FILE));
			String fileLine = null;
			while ((fileLine = reader.readLine()) != null) {
				stopwordsList.addAll(Arrays.asList(fileLine.split(",")));
			}
			System.out.println("Leu o arquivo de stopwords: " + STOPWORDS_FILE);
		} catch (FileNotFoundException e) {
			throw new DocumentProcessorException(MESSAGE_FILE_NOT_FOUND, e);
		} catch (IOException e) {
			throw new DocumentProcessorException(MESSAGE_READING_ERROR, e);
		}
	}

	/**
	 * Processa os documentos contidos no arquivo.
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @throws DocumentProcessorException
	 */
	public void processDocuments(String fileName, String encoding)
			throws DocumentProcessorException {
		loadDocuments(fileName, encoding);
		preProcessDocuments();
		removeStopwords();
		stemDocuments();
		createTermsIndexMap();
		createTermDocumentMatrix();
		createBooleanModelRepresentation();
		createVectorModelRepresentation();
	}

	/**
	 * Lê o arquivo e converte numa lista de documentos
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @throws DocumentProcessorException
	 */
	private void loadDocuments(String fileName, String encoding)
			throws DocumentProcessorException {
		try {
			Charset charset = Charset.forName(encoding);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), charset));
			String fileLine = null;
			while ((fileLine = reader.readLine()) != null) {
				originalDocumentsList.add(fileLine);
			}
			System.out.println("Leu, na codificacao " + encoding
					+ ", o arquivo de documentos: " + fileName);
		} catch (FileNotFoundException e) {
			throw new DocumentProcessorException(MESSAGE_FILE_NOT_FOUND, e);
		} catch (IOException e) {
			throw new DocumentProcessorException(MESSAGE_READING_ERROR, e);
		}
	}

	/**
	 * Realiza o pré-processamento em cada documento, removendo caracteres
	 * especiais, caracteres acentuados e substituindo caracteres maiúsculos por
	 * minúsculos.
	 * 
	 * @throws DocumentProcessorException
	 */
	private void preProcessDocuments() throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite
				.append("O pré-processamento foi realizado e os resultados são:\n");
		int documentCount = 0;

		for (String document : originalDocumentsList) {
			// Remoção de números sozinhos
			String preProcessedDocument = document.replaceAll("\\d+", " ");

			// Substituição de caracteres acentuados
			preProcessedDocument = Normalizer.normalize(preProcessedDocument,
					Normalizer.Form.NFD).replaceAll(
					"\\p{InCombiningDiacriticalMarks}+", "");

			// Remoção de caracteres especiais
			preProcessedDocument = preProcessedDocument
					.replaceAll(
							"[\\Q.,;:?=/\\!*&%$#(){}[]<>º\"'“”‘’—-_+‰±@|~£¥¨®°µ·½¿×ßø˜…€™¤´¡¦§©ª«»­¯²³¶¸¼¾œƒˆ‡¬„¢†•‹›\\E]",
							" ");

			// Remoção de caracteres não-reconhecidos
			preProcessedDocument = preProcessedDocument.replaceAll("\\W", " ");

			// Remoção de palavras menores que 2 caracteres
			preProcessedDocument = preProcessedDocument.replaceAll(
					"\\s\\w?\\s", " ");

			// Substituição de caracteres maiúsculos por minúsculos
			preProcessedDocument = preProcessedDocument.toLowerCase();

			// Substituição de múltiplos espaços por apenas um espaço
			preProcessedDocument = preProcessedDocument.replaceAll("\\s+", " ");

			List<String> termsDocument = new ArrayList<String>(
					Arrays.asList(preProcessedDocument.split(" ")));
			termsDocumentsList.add(termsDocument);

			// Log
			contentToWrite.append("Documento ").append(documentCount++)
					.append(": ").append(termsDocument).append("\n");
		}

		writeToFile(PREPROCESSED_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Remove as stopwords de cada documento.
	 * 
	 * @throws DocumentProcessorException
	 */
	private void removeStopwords() throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite
				.append("A remoção de stopwords foi realizada e os resultados são:\n");
		int documentCount = 0;

		for (List<String> document : termsDocumentsList) {
			for (int termIndex = 0; termIndex < document.size(); termIndex++) {
				String term = document.get(termIndex);
				for (String stopword : stopwordsList) {
					if (term.equals(stopword)) {
						document.remove(termIndex);
						break;
					}
				}
			}

			// Log
			contentToWrite.append("Documento ").append(documentCount++)
					.append(": ").append(document).append("\n");
		}

		writeToFile(STOPWORDS_REMOVED_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Realiza o processo de stemming em cada documento
	 * 
	 * @throws DocumentProcessorException
	 */
	private void stemDocuments() throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite
				.append("O processo de stemming foi realizado e os resultados são:\n");
		int documentCount = 0;

		SnowballStemmer stemmer = new englishStemmer();

		for (List<String> document : termsDocumentsList) {
			for (int termIndex = 0; termIndex < document.size(); termIndex++) {
				stemmer.setCurrent(document.get(termIndex));
				stemmer.stem();
				document.set(termIndex, stemmer.getCurrent());
			}

			// Log
			contentToWrite.append("Documento ").append(documentCount++)
					.append(": ").append(document).append("\n");
		}

		writeToFile(STEMMED_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Cria um mapa de termos x documentos
	 */
	private void createTermsIndexMap() {
		for (int documentIndex = 0; documentIndex < termsDocumentsList.size(); documentIndex++) {
			List<String> document = termsDocumentsList.get(documentIndex);
			for (String term : document) {
				if (termsIndexMap.containsKey(term) == false) {
					List<Integer> documentsIndexesList = new ArrayList<Integer>();
					documentsIndexesList.add(documentIndex);
					termsIndexMap.put(term, documentsIndexesList);
				} else {
					termsIndexMap.get(term).add(documentIndex);
				}
			}
		}
	}

	/**
	 * Cria a matriz de termos x documentos
	 * 
	 * @throws DocumentProcessorException
	 */
	private void createTermDocumentMatrix() throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite.append("A matriz de termos x documentos foi criada:\n");

		termDocumentMatrix = new int[termsIndexMap.size()][termsDocumentsList
				.size()];
		int termIndex = 0;
		for (Map.Entry<String, List<Integer>> entry : termsIndexMap.entrySet()) {
			for (Integer documentIndex : entry.getValue()) {
				termDocumentMatrix[termIndex][documentIndex]++;
			}

			termIndex++;
		}

		// Log
		for (int rowIndex = 0; rowIndex < termDocumentMatrix.length; rowIndex++) {
			int[] row = termDocumentMatrix[rowIndex];
			contentToWrite.append("termDocumentMatrix[").append(rowIndex)
					.append("]={");
			for (int colIndex = 0; colIndex < row.length; colIndex++) {
				contentToWrite.append(termDocumentMatrix[rowIndex][colIndex]);

				if (colIndex < row.length - 1) {
					contentToWrite.append(",");
				}
			}
			contentToWrite.append("}\n");
		}

		writeToFile(TERM_DOCUMENT_MATRIX_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Cria a matriz de documentos x termos para a modelagem Booleana
	 * 
	 * @throws DocumentProcessorException
	 */
	private void createBooleanModelRepresentation()
			throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite
				.append("A matriz de documentos x termos para a modelagem Booleana foi criada:\n");

		int documentsTotal = termDocumentMatrix[0].length;
		int termsTotal = termDocumentMatrix.length;
		booleanModelMatrix = new byte[documentsTotal][termsTotal];
		for (int documentIndex = 0; documentIndex < termsDocumentsList.size(); documentIndex++) {
			contentToWrite.append("booleanModelMatrix[").append(documentIndex)
					.append("]={");
			for (int termIndex = 0; termIndex < termsTotal; termIndex++) {
				booleanModelMatrix[documentIndex][termIndex] = (byte) (termDocumentMatrix[termIndex][documentIndex] > 0 ? 1
						: 0);

				// Log
				contentToWrite
						.append(booleanModelMatrix[documentIndex][termIndex]);
				if (termIndex < termsTotal - 1) {
					contentToWrite.append(",");
				}
			}
			contentToWrite.append("}\n");
		}

		writeToFile(BOOLEAN_MODEL_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Cria a matriz de documentos x termos para a modelagem Vetorial
	 * 
	 * @throws DocumentProcessorException
	 */
	private void createVectorModelRepresentation()
			throws DocumentProcessorException {
		StringBuilder contentToWrite = new StringBuilder();
		contentToWrite
				.append("A matriz de documentos x termos para a modelagem Vetorial foi criada:\n");

		int documentsTotal = termDocumentMatrix[0].length;
		int termsTotal = termDocumentMatrix.length;
		vectorModelMatrix = new int[documentsTotal][termsTotal];
		for (int documentIndex = 0; documentIndex < termsDocumentsList.size(); documentIndex++) {
			contentToWrite.append("vectorModelMatrix[").append(documentIndex)
					.append("]={");
			for (int termIndex = 0; termIndex < termsTotal; termIndex++) {
				vectorModelMatrix[documentIndex][termIndex] = termDocumentMatrix[termIndex][documentIndex];

				// Log
				contentToWrite
						.append(vectorModelMatrix[documentIndex][termIndex]);
				if (termIndex < termsTotal - 1) {
					contentToWrite.append(",");
				}
			}
			contentToWrite.append("}\n");
		}

		writeToFile(VECTOR_MODEL_RESULTS_FILE, contentToWrite);
	}

	/**
	 * Escreve no arquivo desejado
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @param content
	 *            conteúdo do arquivo
	 * @throws DocumentProcessorException
	 */
	private void writeToFile(String fileName, StringBuilder content)
			throws DocumentProcessorException {
		int bufferSize = 1048576;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

			int initPosition = 0;
			int currentPosition = initPosition + bufferSize;
			int contentSize = content.length();

			while (currentPosition < contentSize) {
				char[] buffer = new char[bufferSize];
				content.getChars(initPosition, currentPosition, buffer, 0);
				writer.write(buffer);

				initPosition = currentPosition + 1;

				if (initPosition < contentSize
						&& initPosition + bufferSize >= contentSize) {
					bufferSize = contentSize - initPosition - 1;
				}
				currentPosition = initPosition + bufferSize;
			}

			writer.close();
			System.out.println("Escreveu no arquivo " + fileName);
		} catch (FileNotFoundException e) {
			throw new DocumentProcessorException(MESSAGE_FILE_NOT_FOUND, e);
		} catch (IOException e) {
			throw new DocumentProcessorException(MESSAGE_WRITING_ERROR, e);
		}
	}
}
