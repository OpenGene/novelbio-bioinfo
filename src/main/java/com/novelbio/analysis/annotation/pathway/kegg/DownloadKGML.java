package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.database.model.species.Species;

public class DownloadKGML {
	String keggPathwayUri = "http://www.genome.jp/kegg/pathway.html";
	String keggOrgUri = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_KEGPATH.keg&hier=5";
	String speciesKeggName;
	
	HttpFetch httpFetch = HttpFetch.getInstance();
	
	/**
	 * 输入hsa等
	 * @param speciesKeggName
	 */
	public void setSpeciesKeggName(String speciesKeggName) {
		this.speciesKeggName = speciesKeggName;
	}
	/** 输入物种，和{@link #setSpeciesKeggName(String)} 二选一 */
	public void setSpecies(Species species) {
		this.speciesKeggName = species.getAbbrName();
	}
	
	/** 获得pathway的map的Id */
	private List<String> getPathMapId() {
		httpFetch.setUri(keggPathwayUri);
		httpFetch.queryExp(3);
		try {
			return getLsPathMapIds(httpFetch.getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** 获得全体kegg的pathwayId */
	private List<String> getLsPathMapIds(String keggPage) throws ParserException {
		List<String> lsKegPath = new ArrayList<>();
		Parser parser = new Parser(keggPage);
		NodeFilter filterKGML = new AndFilter(new TagNameFilter("table"), new HasAttributeFilter("width", "660"));
		NodeList nodeListPicture = parser.parse(filterKGML);
		Node node = nodeListPicture.elementAt(0);
		parser = new Parser(node.toHtml());
		NodeFilter filterPathNode = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("href"));
		NodeList nodeListPath = parser.parse(filterPathNode);
		
		SimpleNodeIterator iterator = nodeListPath.elements();
        while (iterator.hasMoreNodes()) {
        	//每个pathway的node
            Node nodePathway = iterator.nextNode();
           if(nodePathway.getText().contains("show_pathway")) {
        	   String pathId = nodePathway.getText().split("map=")[1].split("&")[0];
        	   lsKegPath.add(pathId);
           }
        }
        return lsKegPath;
	}
	
}
