package br.edu.pucminas.riw;

import java.util.List;

/**
 * Interface para abstrair as operações de modelos de busca.
 */
public interface SearchModel {

	/**
	 * Processa uma query e retorna todos os documentos resultados
	 * 
	 * @param queryTerms
	 *            lista de termos da query
	 * @return lista de índices dos documentos
	 */
	public List<Integer> processQuery(List<String> queryTerms);

	/**
	 * Processa uma query e retorna os documentos resultados mais relevantes
	 * 
	 * @param queryTerms
	 *            lista de termos da query
	 * @param treshold
	 *            limite de documentos mais relevantes a serem retornados
	 * @param minimalRelevance
	 *            limiar da métrica de ordenação quanto à relevância mínima
	 * @return objeto contendo listas de índices dos documentos retornados e
	 *         relevantes
	 */
	public RelevanceSearchReturn processQueryWithRelevance(
			List<String> queryTerms, int treshold, double minimalRelevance);

}
