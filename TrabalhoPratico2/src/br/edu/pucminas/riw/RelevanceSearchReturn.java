package br.edu.pucminas.riw;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe de retorno de uma busca com relevância
 */
public class RelevanceSearchReturn {
	private List<Integer> resultDocumentsList;
	private Set<Integer> relevantDocumentsSet; // Conjunto R
	private Set<Integer> answeredDocumentsSet; // Conjunto A

	public RelevanceSearchReturn(List<Integer> resultDocumentsList,
			Set<Integer> relevantDocumentsSet) {
		this.resultDocumentsList = resultDocumentsList;
		this.relevantDocumentsSet = relevantDocumentsSet;
		this.answeredDocumentsSet = new HashSet<Integer>(resultDocumentsList);
	}

	public List<Integer> getResultDocumentsList() {
		return resultDocumentsList;
	}

	public Set<Integer> getRelevantDocumentsSet() {
		return relevantDocumentsSet;
	}

	public Set<Integer> getAnsweredDocumentsSet() {
		return answeredDocumentsSet;
	}
	
	

}
