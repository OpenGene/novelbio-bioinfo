package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class DownloadSpeciesKGML {
	String keggPathwayUri = "http://www.genome.jp/kegg/pathway.html";
	String keggOrgUri = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_KEGPATH.keg&hier=5";
	String speciesKeggName;
	
	HttpFetch httpFetch = HttpFetch.getInstance();
	
	/** 下载kegg的线程池 */
	ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(5, 8, 5000,
			TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	LinkedList<Future<DownloadKGMLunit>> lsFutureDownload = new LinkedList<Future<DownloadKGMLunit>>();

	String savePath;
	/** 线程池的最大容量 */
	int numMaxPoolNum = 1000;
	
	boolean isStart = false;
	
	/**
	 * 输入hsa等
	 * @param speciesKeggName
	 */
	public void setSpeciesKeggName(String speciesKeggName) {
		this.speciesKeggName = speciesKeggName;
	}
	
	/** 后面不加"/" **/
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	
	/** 这个先开，并且是单开一个线程运行 */
	public void download() throws InterruptedException {
		List<String> lsMapId = getLsPathMapId();
		String path = savePath + speciesKeggName + FileOperate.getSepPath();
		FileOperate.createFolders(path);
		
		for (String string : lsMapId) {
			DownloadKGMLunit downloadKGMLunit = new DownloadKGMLunit();
			downloadKGMLunit.setHttpFetch(httpFetch);
			downloadKGMLunit.setMapId(string);
			downloadKGMLunit.setSpeciesKeggId(speciesKeggName);
			downloadKGMLunit.setSavePath(savePath+speciesKeggName);
			
			while (executorDownload.getQueue().size() > 1000) {
				Thread.sleep(1000);
			}
			lsFutureDownload.add(executorDownload.submit(downloadKGMLunit));
			isStart = true;
		}
	}

	/** 这个后运行 */
	public void executDownLoad() {
		try {
			while (!isStart) {
				Thread.sleep(100);
			}
			
			//将executorGetUrlPrepToDownload中间的内容运行直到完毕
			while (executorDownload.getActiveCount() > 0 || lsFutureDownload.size() > 0) {
				Future<DownloadKGMLunit> futureToDownload = lsFutureDownload.poll();
				if (!futureToDownload.isDone() ) {
					lsFutureDownload.add(futureToDownload);
				}
				Thread.sleep(100);
			}
			executorDownload.shutdown();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	/** 获得pathway的map的Id */
	private List<String> getLsPathMapId() {
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
