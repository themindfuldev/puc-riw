package br.edu.pucminas.riw;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Implementação do modelo vetorial.
 */
public class VectorialModel implements SearchModel {
	private int[][] termFrequenciesMatrix;
	private int[] documentFrequenciesArray;
	private double[][] vectorModelMatrix;
	private Map<String, Integer> termsIndexMap;

	public VectorialModel(int[][] termDocumentMatrix,
			Map<String, Integer> termsIndexMap) {
		createVectorModelRepresentation(termDocumentMatrix);
		this.termsIndexMap = termsIndexMap;
	}

	/**
	 * Cria a matriz de documentos x termos para a modelagem Vetorial
	 */
	private void createVectorModelRepresentation(int[][] termDocumentMatrix) {
		int documentsTotal = termDocumentMatrix[0].length;
		int termsTotal = termDocumentMatrix.length;

		// Cria a matriz de frequências de termos para calcular TF
		termFrequenciesMatrix = new int[documentsTotal][termsTotal];
		for (int documentIndex = 0; documentIndex < documentsTotal; documentIndex++) {
			for (int termIndex = 0; termIndex < termsTotal; termIndex++) {
				termFrequenciesMatrix[documentIndex][termIndex] = termDocumentMatrix[termIndex][documentIndex];
			}
		}

		// Cria o vetor de frequências de documentos por termo para calcular IDF
		documentFrequenciesArray = new int[termsTotal];
		for (int colIndex = 0; colIndex < termFrequenciesMatrix[0].length; colIndex++) {
			for (int rowIndex = 0; rowIndex < termFrequenciesMatrix.length; rowIndex++) {
				documentFrequenciesArray[colIndex] += (termFrequenciesMatrix[rowIndex][colIndex] > 0 ? 1
						: 0);
			}
		}

		// Cria a matriz do modelo vetorial, calculando TF-IDF
		vectorModelMatrix = new double[documentsTotal][termsTotal];
		for (int rowIndex = 0; rowIndex < documentsTotal; rowIndex++) {
			for (int colIndex = 0; colIndex < termsTotal; colIndex++) {
				if (termFrequenciesMatrix[rowIndex][colIndex] > 0) {
					vectorModelMatrix[rowIndex][colIndex] = (1 + log2(termFrequenciesMatrix[rowIndex][colIndex]))
							* log2(((double) documentsTotal)
									/ documentFrequenciesArray[colIndex]);
				}
			}
		}
	}

	@Override
	public List<Integer> processQuery(List<String> queryTerms) {
		double[] query = prepareQuery(queryTerms);

		List<Integer> resultsList = new ArrayList<Integer>();

		for (int rowIndex = 0; rowIndex < vectorModelMatrix.length; rowIndex++) {
			double similarity = getSimilarity(query, rowIndex);

			if (similarity > 0) {
				resultsList.add(rowIndex);
			}
		}

		return resultsList;
	}

	/**
	 * Obtém a similaridade do documento em questão para a consulta
	 * 
	 * @param query
	 *            a consulta
	 * @param documentIndex
	 *            índice do documento na matriz da modelagem vetorial
	 * @return a similaridade
	 */
	private double getSimilarity(double[] query, int documentIndex) {
		double term1 = 0, term2 = 0, term3 = 0;

		for (int colIndex = 0; colIndex < query.length; colIndex++) {
			term1 += vectorModelMatrix[documentIndex][colIndex]
					* query[colIndex];
			term2 += Math.pow(vectorModelMatrix[documentIndex][colIndex], 2);
			term3 += Math.pow(query[colIndex], 2);
		}
		term2 = Math.sqrt(term2);
		term3 = Math.sqrt(term3);

		double similarity = term1 / (term2 * term3);
		return similarity;
	}

	/**
	 * Prepara a consulta com relação ao modelo atual
	 * 
	 * @param queryTerms
	 *            termos da consulta
	 * @return a consulta com relação ao modelo atual
	 */
	private double[] prepareQuery(List<String> queryTerms) {
		// Cria o vetor de frequências de termos
		int[] termFrequenciesArray = new int[vectorModelMatrix[0].length];
		for (String term : queryTerms) {
			int termIndex = termsIndexMap.get(term);
			termFrequenciesArray[termIndex]++;
		}

		// Cria a consulta no modelo vetorial
		double[] query = new double[vectorModelMatrix[0].length];
		for (int colIndex = 0; colIndex < query.length; colIndex++) {
			if (termFrequenciesArray[colIndex] > 0) {
				query[colIndex] = (1 + log2(termFrequenciesArray[colIndex]))
						* log2(((double) vectorModelMatrix.length)
								/ documentFrequenciesArray[colIndex]);
			}
		}

		return query;
	}

	@Override
	public RelevanceSearchReturn processQueryWithRelevance(
			List<String> queryTerms, int treshold, double minimalRelevance) {
		double[] query = prepareQuery(queryTerms);

		// Calcula a similaridade de cada documento e armazena no mapa de
		// similaridades
		Map<Double, List<Integer>> similarityMap = new TreeMap<Double, List<Integer>>(
				new Comparator<Double>() {
					@Override
					public int compare(Double o1, Double o2) {
						return o2.compareTo(o1);
					}
				});
		for (int rowIndex = 0; rowIndex < vectorModelMatrix.length; rowIndex++) {
			double similarity = getSimilarity(query, rowIndex);

			if (similarity > 0) {
				if (similarityMap.containsKey(similarity) == false) {
					List<Integer> documentsList = new ArrayList<Integer>();
					documentsList.add(rowIndex);
					similarityMap.put(similarity, documentsList);
				} else {
					similarityMap.get(similarity).add(rowIndex);
				}
			}
		}

		// Varre o mapa de similaridades para obter os documentos resultantes
		List<Integer> resultDocumentsList = new ArrayList<Integer>();
		for (Map.Entry<Double, List<Integer>> entry : similarityMap.entrySet()) {
			if (resultDocumentsList.size() >= treshold) {
				break;
			}

			resultDocumentsList.addAll(entry.getValue());
		}

		// Varre o mapa de similaridades para obter os documentos mais
		// relevantes
		Set<Integer> relevantDocumentsSet = new HashSet<Integer>();
		for (Map.Entry<Double, List<Integer>> entry : similarityMap.entrySet()) {
			if (entry.getKey() < minimalRelevance) {
				break;
			}

			relevantDocumentsSet.addAll(entry.getValue());
		}

		return new RelevanceSearchReturn(resultDocumentsList,
				relevantDocumentsSet);
	}

	/**
	 * Calcula o logaritmo na base 2 do número desejado
	 * 
	 * @param number
	 *            número desejado
	 * @return logaritmo na base 2 do número desejado
	 */
	private double log2(double number) {
		return Math.log(number) / Math.log(2);
	}
}
