package com.novelbio.bioinfo.annotation.pathway.kegg;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.novelbio.base.dataOperate.ExceptionNBCServerConnect;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 下载KGML文件的单元，可以用于多线程
 * @author zong0jie
 *
 */
public class DownloadKGMLunit implements Callable<DownloadKGMLunit> {
	private static final Logger logger = Logger.getLogger(DownloadKGMLunit.class);
	static String keggOrgUri = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_KEGPATH.keg&hier=5";
	static String keggUri = "http://www.genome.jp";
	HttpFetch keggFetch;
	
	/** hsa等 */
	String speciesKeggId;
	String mapId;
	
	/** 保存路径 */
	String savePath;
	
	/** 重试次数，意思在线程池中失败后的重试次数 */
	int retryNum = 0;
	
	boolean isSucess;
	
	/** hsa等 */
	public void setSpeciesKeggId(String speciesKeggId) {
		this.speciesKeggId = speciesKeggId;
	}
	
	public void setMapId(String mapId) {
		String id = PatternOperate.getPatLoc(mapId, "\\d+", false).get(0)[0];
		this.mapId = id;
	}
	
	/** 保存路径，后面不会自动加上sep */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** 新建一个与输入httpFetch使用同一个httpClient的httpFetch */
	public void setHttpFetch(HttpFetch httpFetch) {
		this.keggFetch = httpFetch;
	}
	/** 重试次数，意思在线程池中失败后的重试次数 */
	public int getRetryNum() {
		return retryNum;
	}
	/** 是否成功运行 */
	public boolean isSucess() {
		return isSucess;
	}
	
	@Override
	public DownloadKGMLunit call() throws Exception {
		isSucess = false;
		try {
			runDownload();
			isSucess = true;
		} catch (Exception e) { }
		return this;
	}
	
	public void runDownload() throws ParserException, IOException {
		retryNum++;
		String saveName = savePath + speciesKeggId + mapId;
		if (FileOperate.isFileExistAndNotDir(saveName + ".xml") && FileOperate.isFileExistAndNotDir(saveName + ".png")) {
			return;
		}
		
		String pathUri = getPathUri();
		if (pathUri != null) {
			download(pathUri);
		}
	}
	
	/** 返回本物中该pathway的uri，就是可以下载图片和kgml的那个页面，没有该pathway则返回null */
	private String getPathUri() throws ParserException, IOException {
		String keggOrgUri = DownloadKGMLunit.keggOrgUri.replace("KEGPATH", "map" + mapId);
		keggFetch.setUriGet(keggOrgUri);
		keggFetch.queryExp();
		String response = keggFetch.getResponse();
//		System.out.println(response);
//		if (mapId.equals("01230")) {
//			logger.debug("");
//		}
		Parser parser = new Parser(response);
		NodeFilter filterKGML = new AndFilter(new TagNameFilter("a"), new HasChildFilter(new StringFilter(speciesKeggId)));
		NodeList nodeList = parser.parse(filterKGML);
		if (nodeList.size() == 0) {
			return null;
		}
		String uri = nodeList.elementAt(0).getText();
		uri = keggUri + uri.split("href=")[1].split(" ")[0].replace("\"", "").trim();
		return uri;
	}
	
	/** 给定 可以下载图片和kgml的那个页面，下载kgml文件和相关图片 */
	private void download(String speciesPathUri) throws ParserException, IOException {
		keggFetch.setUriGet(speciesPathUri);
		keggFetch.queryExp();
		String keggPage = keggFetch.getResponse();
		String kgmlUri = getKGMLuri(keggPage);
		if (kgmlUri == null) {
			return;
		}
		String saveName = kgmlUri.split("entry=")[1].split("&")[0];
		String saveToXml = savePath + saveName + ".xml";
		if (!FileOperate.isFileExistAndNotDir(saveToXml)) {//存在文件则不下载
			keggFetch.setUriGet(kgmlUri);
			if (keggFetch.query()) {
				keggFetch.download(saveToXml);
			}
		}
		
		String picUri = getPicUri(keggPage);
		if (picUri == null) {
			return;
		}
		
		String saveToPic = savePath + saveName + ".png";
		if (!FileOperate.isFileExistAndNotDir(saveToPic)) { 
			keggFetch.setUriGet(picUri);
			if (keggFetch.query()) {
				keggFetch.download(saveToPic);
			}
		}
		

		keggFetch.close();
	}
	
	/** kegg kgml的路径，null表示没有kgml文件可供下载
	 * @throws ParserException
	 * */
	private String getKGMLuri(String keggPage) throws ParserException {
		Parser parser = new Parser(keggPage);
		NodeFilter filterKGML = new StringFilter( "Download KGML");
		NodeList nodeList = parser.parse(filterKGML);
		if (nodeList.size() == 0) {
			return null;
		}
		Node nodeDownloadKGML = nodeList.elementAt(0).getParent();
		String kgmlUri = nodeDownloadKGML.getText();
		kgmlUri = kgmlUri.split("href=")[1].split(" ")[0].replace("\"", "");
		return kgmlUri;
	}
	
	/** kegg kgml的路径，null表示没有kgml文件可供下载
	 * @throws ParserException
	 * */
	private String getPicUri(String keggPage) throws ParserException {
		Parser parser = new Parser(keggPage);
		
		NodeFilter filterKGML = new AndFilter(new TagNameFilter("img"), new HasAttributeFilter("name", "pathwayimage"));
		NodeList nodeList = parser.parse(filterKGML);
		if (nodeList.size() == 0) {
			return null;
		}
		Node nodeDownloadKGML = nodeList.elementAt(0);
		String kgmlUri = nodeDownloadKGML.getText().split("src=")[1].split(" ")[0].replace("\"", "").trim();
		logger.info("get " +kgmlUri);
		return keggUri + kgmlUri;
	}

	
}
