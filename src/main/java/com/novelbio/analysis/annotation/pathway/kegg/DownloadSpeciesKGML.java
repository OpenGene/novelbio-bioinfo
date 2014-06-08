package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
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

public class DownloadSpeciesKGML {
	private static final Logger logger = Logger.getLogger(DownloadSpeciesKGML.class);
	
	private static final String keggPathwayUri = "http://www.genome.jp/kegg/pathway.html";
	private static final String keggOrgUri = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_KEGPATH.keg&hier=5";
	private static final String ncbiKeggIDurl = "http://www.genome.jp/dbget-bin/get_linkdb_list";

	String speciesKeggName;
	
	HttpFetch httpFetch = HttpFetch.getInstance();
	
	/** 下载kegg的线程池 */
	ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(5, 8, 5000,
			TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	ArrayBlockingQueue<Future<DownloadKGMLunit>> lsFutureDownload = new ArrayBlockingQueue<Future<DownloadKGMLunit>>(1000);

	String savePath;
	/** 线程池的最大容量 */
	int numMaxPoolNum = 1000;
	
	/** 尝试下载3次 */
	int retryTime = 3;
	
	List<String> lsMapId;
	
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
	
	/** 获得pathway的map的Id */
	public void fetchPathMapId() {
		String path = savePath + speciesKeggName + FileOperate.getSepPath();
		downloadGeneID2KeggID(speciesKeggName, path);
		httpFetch.setUri(keggPathwayUri);
		httpFetch.queryExp(3);
		try {
			lsMapId = getLsPathMapIds(httpFetch.getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** 这个先开，并且是单开一个线程运行 */
	public void download() throws InterruptedException {
		String path = savePath + speciesKeggName + FileOperate.getSepPath();
		FileOperate.createFolders(path);
		for (String string : lsMapId) {
			DownloadKGMLunit downloadKGMLunit = new DownloadKGMLunit();
			downloadKGMLunit.setHttpFetch(httpFetch);
			downloadKGMLunit.setMapId(string);
			downloadKGMLunit.setSpeciesKeggId(speciesKeggName);
			downloadKGMLunit.setSavePath(path);
			//应该是不需要的，因为是blockqueue
//			while (executorDownload.getQueue().size() > 1000) {
//				Thread.sleep(1000);
//			}
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
				} else {
					//失败的重试
					DownloadKGMLunit downloadKGMLunit = futureToDownload.get();
					if (!downloadKGMLunit.isSucess() ) {
						if (downloadKGMLunit.getRetryNum() <= retryTime) {
							lsFutureDownload.add(executorDownload.submit(downloadKGMLunit));
						} else {
							logger.error("cannot download: "+ speciesKeggName + " " + downloadKGMLunit.mapId);
						}
					}
				}
				Thread.sleep(100);
			}
			executorDownload.shutdown();
		} catch (Exception e) {
			// TODO: handle exception
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
            String pathStr = nodePathway.getText();
           if(pathStr.contains("show_pathway")) {
        	   String pathId = pathStr.split("show_pathway\\?")[1].split("&")[0].replace("map=", "").replace("\"", "");
        	   
//        	   if (!pathId.contains("04380")) {
//				continue;
//			}
        	   
        	   lsKegPath.add(pathId);
           }
        }
        return lsKegPath;
	}
	
	/**
	 * 下载所有的有机体
	 * @param savePath 保存的目录
	 */
	private void downloadGeneID2KeggID(String keggName, String savePath) {
		List<String[]> lsParam = generateParam(keggName);
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(ncbiKeggIDurl);
		httpFetch.setPostParam(lsParam);
		String filePath = FileOperate.addSep(savePath) + keggName + "_ncbi-geneid.list";
		if (httpFetch.query()) {
			if(httpFetch.download(filePath)){
				logger.info("下载" + filePath + "成功!");
			}else {
				logger.error("下载" + filePath + "失败!");
			}
		}
	}
	
	/** 用于post提交的信息 */
	private List<String[]> generateParam(String keggName) {
		List<String[]> lsKey2Value = new ArrayList<String[]>(); 
		lsKey2Value.add(new String[] { "page", "download" });
		lsKey2Value.add(new String[] { "u", "uniq" });
		lsKey2Value.add(new String[] { "t", "ncbi-geneid" });
		lsKey2Value.add(new String[] { "targetformat", "" });
		lsKey2Value.add(new String[] { "m", keggName});
		return lsKey2Value;
	}
}
