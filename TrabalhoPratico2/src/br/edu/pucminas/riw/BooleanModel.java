package br.edu.pucminas.riw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementação do modelo booleano.
 */
public class BooleanModel implements SearchModel {
	private byte[][] booleanModelMatrix;
	private Map<String, Integer> termsIndexMap;

	public BooleanModel(int[][] termDocumentMatrix,
			Map<String, Integer> termsIndexMap) {
		createBooleanModelRepresentation(termDocumentMatrix);
		this.termsIndexMap = termsIndexMap;
	}

	/**
	 * Cria a matriz de documentos x termos para a modelagem Booleana
	 */
	private void createBooleanModelRepresentation(int[][] termDocumentMatrix) {
		int documentsTotal = termDocumentMatrix[0].length;
		int termsTotal = termDocumentMatrix.length;
		booleanModelMatrix = new byte[documentsTotal][termsTotal];
		for (int documentIndex = 0; documentIndex < documentsTotal; documentIndex++) {
			for (int termIndex = 0; termIndex < termsTotal; termIndex++) {
				booleanModelMatrix[documentIndex][termIndex] = (byte) (termDocumentMatrix[termIndex][documentIndex] > 0 ? 1
						: 0);
			}
		}
	}

	@Override
	public List<Integer> processQuery(List<String> queryTerms) {
		byte[] query = prepareQuery(queryTerms);

		List<Integer> resultsList = new ArrayList<Integer>();

		for (int rowIndex = 0; rowIndex < booleanModelMatrix.length; rowIndex++) {
			byte[] queryComparison = Arrays.copyOf(query, query.length);

			for (int colIndex = 0; colIndex < query.length; colIndex++) {
				queryComparison[colIndex] = (byte) (queryComparison[colIndex] & booleanModelMatrix[rowIndex][colIndex]);
			}

			if (Arrays.equals(queryComparison, query)) {
				resultsList.add(rowIndex);
			}
		}

		return resultsList;
	}

	/**
	 * Prepara a consulta com relação ao modelo atual
	 * 
	 * @param queryTerms
	 *            termos da consulta
	 * @return a consulta com relação ao modelo atual
	 */
	private byte[] prepareQuery(List<String> queryTerms) {
		byte[] query = new byte[booleanModelMatrix[0].length];
		for (String term : queryTerms) {
			int termIndex = termsIndexMap.get(term);
			query[termIndex] = 1;
		}
		return query;
	}

	@Override
	public RelevanceSearchReturn processQueryWithRelevance(
			List<String> queryTerms, int treshold, double minimalRelevance) {
		return null;
	}
}
