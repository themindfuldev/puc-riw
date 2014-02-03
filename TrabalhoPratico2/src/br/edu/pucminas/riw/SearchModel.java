package br.edu.pucminas.riw;

import java.util.List;

/**
 * Interface para abstrair as opera��es de modelos de busca.
 */
public interface SearchModel {

	/**
	 * Processa uma query e retorna todos os documentos resultados
	 * 
	 * @param queryTerms
	 *            lista de termos da query
	 * @return lista de �ndices dos documentos
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
	 *            limiar da m�trica de ordena��o quanto � relev�ncia m�nima
	 * @return objeto contendo listas de �ndices dos documentos retornados e
	 *         relevantes
	 */
	public RelevanceSearchReturn processQueryWithRelevance(
			List<String> queryTerms, int treshold, double minimalRelevance);

}
