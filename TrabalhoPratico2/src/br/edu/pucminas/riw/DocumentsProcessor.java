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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
	private static final int METRIC_MAX_RESULTS = 10;
	private static final double METRIC_MINIMAL_RELEVANCE = 0.05;
	private static final String TERM_DOCUMENT_MATRIX_RESULTS_FILE = "step1-term-document-matrix.txt";
	private static final String QUERY_RESULTS_RESULTS_FILE = "step2-query-results.txt";
	private static final String TEN_MOST_RELEVANT_RESULTS_FILE = "step3-10-most-relevant-results.txt";
	private static final String PRECISION_AND_RECALL_RESULTS_FILE = "step4-precision-and-recall.txt";

	private List<String> stopwordsList;
	private List<Document> documentsList;
	private Map<String, Integer> termsIndexMap;
	private int[][] termDocumentMatrix;
	private SearchModel booleanModel;
	private SearchModel vectorialModel;

	public DocumentsProcessor(String fileName, String encoding)
			throws DocumentProcessorException {
		loadStopwords();
		processDocuments(fileName, encoding);
	}

	/**
	 * Lê o arquivo de stopwords e converte numa lista de stopwords
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @throws DocumentProcessorException
	 */
	private void loadStopwords() throws DocumentProcessorException {
		stopwordsList = new ArrayList<String>();

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
	private void processDocuments(String fileName, String encoding)
			throws DocumentProcessorException {
		// Realiza etapas de pré-processamento, remoção de stopwords e stemming
		documentsList = prepareDocumentsList(fileName, encoding);

		createTermDocumentMatrix();
		System.out.println("Criou matriz termos x documentos");

		booleanModel = new BooleanModel(termDocumentMatrix, termsIndexMap);
		System.out.println("Criou matriz do modelo booleano");

		vectorialModel = new VectorialModel(termDocumentMatrix, termsIndexMap);
		System.out.println("Criou matriz do modelo vetorial");
	}

	/**
	 * Processa as queries contidos no arquivo.
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @throws DocumentProcessorException
	 */
	public void processQueries(String fileName, String encoding)
			throws DocumentProcessorException {
		// Realiza etapas de pré-processamento, remoção de stopwords e stemming
		List<Document> queriesList = prepareDocumentsList(fileName, encoding);

		// Aplica as 5 consultas no modelo booleano e vetorial
		StringBuilder contentToWriteStep2 = new StringBuilder();
		contentToWriteStep2
				.append("As 5 consultas foram aplicadas no modelo booleano e os resultados sao:");

		System.out.println("----------------");
		System.out.println("Modelo booleano:");
		applyQueries(booleanModel, queriesList, contentToWriteStep2);
		
		contentToWriteStep2
		.append("As 5 consultas foram aplicadas no modelo vetorial e os resultados sao:");

		System.out.println("----------------");
		System.out.println("Modelo vetorial:");
		applyQueries(vectorialModel, queriesList, contentToWriteStep2);

		writeToFile(QUERY_RESULTS_RESULTS_FILE, contentToWriteStep2);

		// Aplica a métrica de ordenação no modelo vetorial e calcula a precisão
		// e revocação
		StringBuilder contentToWriteStep3 = new StringBuilder();
		StringBuilder contentToWriteStep4 = new StringBuilder();

		System.out.println("----------------");
		System.out.println("Modelo vetorial mostrando os " + METRIC_MAX_RESULTS
				+ " documentos mais relevantes, com relevância mínima de "
				+ METRIC_MINIMAL_RELEVANCE + ":");
		contentToWriteStep3
				.append("Modelo vetorial mostrando os ")
				.append(METRIC_MAX_RESULTS)
				.append(" documentos mais relevantes, com relevância mínima de ")
				.append(METRIC_MINIMAL_RELEVANCE).append(":\n");
		contentToWriteStep4
				.append("Cálculo de precisão e revocação por consulta:\n");

		applyQueriesWithRelevance(vectorialModel, queriesList,
				METRIC_MAX_RESULTS, METRIC_MINIMAL_RELEVANCE,
				contentToWriteStep3, contentToWriteStep4);

		writeToFile(TEN_MOST_RELEVANT_RESULTS_FILE, contentToWriteStep3);
		writeToFile(PRECISION_AND_RECALL_RESULTS_FILE, contentToWriteStep4);
	}

	/**
	 * Aplica um modelo com relação a um conjunto de consultas
	 * 
	 * @param searchModel
	 *            o modelo de consultas
	 * @param queriesList
	 *            a lista de consultas
	 * @param contentToWrite
	 *            conteúdo a ser escrito no arquivo de log
	 */
	private void applyQueries(SearchModel searchModel,
			List<Document> queriesList, StringBuilder contentToWrite) {
		for (Document query : queriesList) {
			List<Integer> documentIndexesList = searchModel.processQuery(query
					.getTerms());
			showResults(query, documentIndexesList, contentToWrite);
		}
	}

	/**
	 * Aplica um modelo com relação a um conjunto de consultas, retornando com
	 * relevância
	 * 
	 * @param searchModel
	 *            o modelo de consultas
	 * @param queriesList
	 *            a lista de consultas
	 * @param treshold
	 *            limite de documentos mais relevantes a serem retornados
	 * @param minimalRelevance
	 *            limiar da métrica de ordenação quanto à relevância mínima
	 * @param contentToWriteStep3
	 *            conteúdo a ser escrito no arquivo de log da etapa 3
	 * @param contentToWriteStep4
	 *            conteúdo a ser escrito no arquivo de log da etapa 4
	 */
	private void applyQueriesWithRelevance(SearchModel searchModel,
			List<Document> queriesList, int threshold, double minimalRelevance,
			StringBuilder contentToWriteStep3, StringBuilder contentToWriteStep4) {
		for (Document query : queriesList) {
			RelevanceSearchReturn relevanceSearchReturn = searchModel
					.processQueryWithRelevance(query.getTerms(), threshold,
							minimalRelevance);
			showResults(query, relevanceSearchReturn.getResultDocumentsList(),
					contentToWriteStep3);
			showMetrics(query, relevanceSearchReturn, contentToWriteStep4);
		}
	}

	/**
	 * Exibe resultados da consulta
	 * 
	 * @param query
	 *            a consulta
	 * @param documentIndexesList
	 *            a lista de índices de documentos
	 * @param contentToWrite
	 *            conteúdo a ser escrito no arquivo de log
	 */
	private void showResults(Document query, List<Integer> documentIndexesList,
			StringBuilder contentToWrite) {
		System.out.println("\nA consulta '" + query.getContent()
				+ "' retornou os seguintes documentos:");
		contentToWrite.append("\n\nA consulta '").append(query.getContent())
				.append("' retornou os seguintes documentos:");
		for (Integer documentIndex : documentIndexesList) {
			System.out.println(">> "+documentsList.get(documentIndex).toString(100));
			contentToWrite.append("\n>> ")
					.append(documentsList.get(documentIndex).toString(300));
		}
	}

	/**
	 * Exibe métricas da consulta
	 * 
	 * @param query
	 *            a consulta
	 * @param relevanceSearchReturn
	 *            objeto contendo o retorno da consulta com relevância
	 * @param contentToWrite
	 *            conteúdo a ser escrito no arquivo de log
	 */
	private void showMetrics(Document query,
			RelevanceSearchReturn relevanceSearchReturn,
			StringBuilder contentToWrite) {
		Set<Integer> rNa = new HashSet<Integer>(
				relevanceSearchReturn.getRelevantDocumentsSet());
		rNa.retainAll(relevanceSearchReturn.getAnsweredDocumentsSet());

		double precision = ((double) rNa.size())
				/ relevanceSearchReturn.getAnsweredDocumentsSet().size();
		double recall = ((double) rNa.size())
				/ relevanceSearchReturn.getRelevantDocumentsSet().size();

		System.out.println("A precisao desta consulta e: " + precision);
		System.out.println("A revocacao desta consulta e: " + recall);

		contentToWrite.append("\nA consulta '").append(query.getContent())
				.append("' possui precisao = ").append(precision)
				.append(" e revocacao = ").append(recall);
	}

	/**
	 * Prepara a lista de documentos
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @param encoding
	 *            codificação
	 * @return lista de documentos
	 * @throws DocumentProcessorException
	 */
	private List<Document> prepareDocumentsList(String fileName, String encoding)
			throws DocumentProcessorException {
		List<Document> documentsList = loadDocuments(fileName, encoding);
		preProcessDocuments(documentsList);
		removeStopwords(documentsList);
		stemDocuments(documentsList);
		return documentsList;
	}

	/**
	 * Lê o arquivo e converte numa lista de documentos
	 * 
	 * @param fileName
	 *            nome do arquivo
	 * @return a lista de documentos
	 * @throws DocumentProcessorException
	 */
	private List<Document> loadDocuments(String fileName, String encoding)
			throws DocumentProcessorException {
		List<Document> documentsList = new ArrayList<Document>();

		try {
			Charset charset = Charset.forName(encoding);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), charset));
			String fileLine = null;
			while ((fileLine = reader.readLine()) != null) {
				documentsList.add(new Document(fileLine.trim(), documentsList
						.size()));
			}
			System.out.println("Leu, na codificacao " + encoding
					+ ", o arquivo: " + fileName);
		} catch (FileNotFoundException e) {
			throw new DocumentProcessorException(MESSAGE_FILE_NOT_FOUND, e);
		} catch (IOException e) {
			throw new DocumentProcessorException(MESSAGE_READING_ERROR, e);
		}

		return documentsList;
	}

	/**
	 * Realiza o pré-processamento em cada documento, removendo caracteres
	 * especiais, caracteres acentuados e substituindo caracteres maiúsculos por
	 * minúsculos.
	 * 
	 * @param documentsList
	 *            lista de documentos
	 */
	private void preProcessDocuments(List<Document> documentsList) {
		for (Document document : documentsList) {
			String content = document.getContent();

			// Remoção de números sozinhos
			String preProcessedContent = content.replaceAll("\\d+", " ");

			// Substituição de caracteres acentuados
			preProcessedContent = Normalizer.normalize(preProcessedContent,
					Normalizer.Form.NFD).replaceAll(
					"\\p{InCombiningDiacriticalMarks}+", "");

			// Remoção de caracteres especiais
			preProcessedContent = preProcessedContent
					.replaceAll(
							"[\\Q.,;:?=/\\!*&%$#(){}[]<>º\"'“”‘’—-_+‰±@|~£¥¨®°µ·½¿×ßø˜…€™¤´¡¦§©ª«»­¯²³¶¸¼¾œƒˆ‡¬„¢†•‹›\\E]",
							" ");

			// Remoção de caracteres não-reconhecidos
			preProcessedContent = preProcessedContent.replaceAll("\\W", " ");

			// Remoção de palavras menores que 2 caracteres
			preProcessedContent = preProcessedContent.replaceAll("\\s\\w?\\s",
					" ");

			// Substituição de caracteres maiúsculos por minúsculos
			preProcessedContent = preProcessedContent.toLowerCase();

			// Substituição de múltiplos espaços por apenas um espaço
			preProcessedContent = preProcessedContent.replaceAll("\\s+", " ");

			document.setTerms(new ArrayList<String>(Arrays
					.asList(preProcessedContent.split(" "))));
		}
	}

	/**
	 * Remove as stopwords de cada documento.
	 * 
	 * @param documentsList
	 *            lista de documentos
	 */
	private void removeStopwords(List<Document> documentsList) {
		for (Document document : documentsList) {
			List<String> termsList = document.getTerms();
			for (int termIndex = 0; termIndex < termsList.size(); termIndex++) {
				String term = termsList.get(termIndex);
				for (String stopword : stopwordsList) {
					if (term.equals(stopword)) {
						termsList.remove(termIndex);
						break;
					}
				}
			}
		}

	}

	/**
	 * Realiza o processo de stemming em cada documento
	 * 
	 * @param documentsList
	 *            lista de documentos
	 */
	private void stemDocuments(List<Document> documentsList) {
		SnowballStemmer stemmer = new englishStemmer();

		for (Document document : documentsList) {
			List<String> termsList = document.getTerms();
			for (int termIndex = 0; termIndex < termsList.size(); termIndex++) {
				stemmer.setCurrent(termsList.get(termIndex));
				stemmer.stem();
				termsList.set(termIndex, stemmer.getCurrent());
			}
		}
	}

	/**
	 * Cria a matriz de termos x documentos
	 * 
	 * @return um array de objetos contendo a matriz de termos x documentos na
	 *         primeira posição e o mapa de índice de termos na segunda posição
	 * @throws DocumentProcessorException
	 */
	private void createTermDocumentMatrix() throws DocumentProcessorException {
		StringBuilder contentToWriteStep1 = new StringBuilder();
		contentToWriteStep1
				.append("A matriz de termos x documentos foi criada:\n");

		List<List<Integer>> termDocumentMatrixBuilderList = new ArrayList<List<Integer>>();
		termsIndexMap = new WeakHashMap<String, Integer>();

		// Cria a lista auxiliadora da construção da matriz de termos x
		// documentos e o mapa de índice de termos
		for (int documentIndex = 0; documentIndex < documentsList.size(); documentIndex++) {
			List<String> termsList = documentsList.get(documentIndex)
					.getTerms();
			for (String term : termsList) {
				if (termsIndexMap.containsKey(term) == false) {
					List<Integer> documentsForTermList = new ArrayList<Integer>();
					documentsForTermList.add(documentIndex);
					termDocumentMatrixBuilderList.add(documentsForTermList);

					termsIndexMap.put(term,
							termDocumentMatrixBuilderList.size() - 1);
				} else {
					termDocumentMatrixBuilderList.get(termsIndexMap.get(term))
							.add(documentIndex);
				}
			}
		}

		// Cria a matriz de termos x documentos
		termDocumentMatrix = new int[termDocumentMatrixBuilderList.size()][documentsList
				.size()];

		for (int rowIndex = 0; rowIndex < termDocumentMatrixBuilderList.size(); rowIndex++) {
			List<Integer> documentsForTermList = termDocumentMatrixBuilderList
					.get(rowIndex);
			for (int colIndex = 0; colIndex < documentsForTermList.size(); colIndex++) {
				int documentIndex = documentsForTermList.get(colIndex);
				termDocumentMatrix[rowIndex][documentIndex]++;
			}
		}

		// Log
		for (int rowIndex = 0; rowIndex < termDocumentMatrix.length; rowIndex++) {
			int[] row = termDocumentMatrix[rowIndex];
			contentToWriteStep1.append("termDocumentMatrix[").append(rowIndex)
					.append("]={");
			for (int colIndex = 0; colIndex < row.length; colIndex++) {
				contentToWriteStep1
						.append(termDocumentMatrix[rowIndex][colIndex]);

				if (colIndex < row.length - 1) {
					contentToWriteStep1.append(",");
				}
			}
			contentToWriteStep1.append("}\n");
		}

		writeToFile(TERM_DOCUMENT_MATRIX_RESULTS_FILE, contentToWriteStep1);
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
		int bufferSize = content.length() - 1 > 1048576? 1048576: content.length() - 1;

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
