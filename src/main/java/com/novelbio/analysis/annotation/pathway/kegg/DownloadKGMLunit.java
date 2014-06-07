package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.concurrent.Callable;

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

import com.novelbio.base.dataOperate.HttpFetch;

/**
 * 下载KGML文件的单元，可以用于多线程
 * @author zong0jie
 *
 */
public class DownloadKGMLunit implements Callable<DownloadKGMLunit> {
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
		this.mapId = mapId;
	}
	/** 保存路径，后面不会自动加上sep */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** 新建一个与输入httpFetch使用同一个httpClient的httpFetch */
	public void setHttpFetch(HttpFetch httpFetch) {
		this.keggFetch = HttpFetch.getInstance( httpFetch);
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
	
	public void runDownload() throws ParserException {
		retryNum++;
		String pathUri = getPathUri();
		if (pathUri != null) {
			download(pathUri);
		}
	}
	
	/** 返回本物中该pathway的uri，就是可以下载图片和kgml的那个页面，没有该pathway则返回null */
	private String getPathUri() throws ParserException {
		String keggOrgUri = DownloadKGMLunit.keggOrgUri.replace("KEGPATH", mapId);
		keggFetch.setUri(keggOrgUri);
		keggFetch.queryExp(3);
		Parser parser = new Parser(keggFetch.getResponse());
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
	private void download(String speciesPathUri) throws ParserException {
		keggFetch.setUri(speciesPathUri);
		keggFetch.queryExp(3);
		String keggPage = keggFetch.getResponse();
		String kgmlUri = getKGMLuri(keggPage);
		if (kgmlUri == null) {
			return;
		}
		keggFetch.setUri(kgmlUri);
		String saveName = kgmlUri.split("entry=")[1].split("&")[0];
		if (keggFetch.query(3)) {
			keggFetch.download(savePath + saveName + ".xml");
		}
		
		String picUri = getPicUri(keggPage);
		if (picUri == null) {
			return;
		}
		keggFetch.setUri(picUri);
		if (keggFetch.query(3)) {
			keggFetch.download(savePath + saveName + ".png");
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
		System.out.println(kgmlUri);
		return keggUri + kgmlUri;
	}

	
}
