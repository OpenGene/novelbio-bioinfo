package com.novelbio.analysis.tools.kegarray;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.multithread.RunProcess;
/**
 * 当用<b>KegArray</b>获得结果url之后，用该类来将所有标注好的pathway图片下载下来。
 * @author ywd
 *
 */
public class DownKeggPng extends RunProcess<Integer> {
	private static Logger logger = Logger.getLogger(DownKeggPng.class);
	public static void main(String[] args) {
		DownKeggPng  downKeggPng = new DownKeggPng();
		String URL = "http://www.genome.jp//kegg-bin/color_pathway_object?org_name=hsa&file=30600&reference=gray";
//		downKeggPng.findWebHref(URL);
//		downKeggPng.findPngHref(URL);
		downKeggPng.setDownLoadPng(URL, "/home/novelbio/PNG/");
		Thread thread = new Thread(downKeggPng);
		thread.start();
//		downKeggPng.downPng("http://www.genome.jp/tmp//mark_pathway135339130411618/map01100_0.3511618.png", "/home/ywd/PNG/");
	}
	String url;
	String outPath;
	ArrayList<String> lsPngWebUrl = new ArrayList<String>();
	
	/**
	 * 设定的什么
	 * @param urlFromKegArray 通过什么产生的url
	 * @param outPath
	 */
	public void setDownLoadPng(String urlFromKegArray,String outPath) {
		this.url = urlFromKegArray;
		this.outPath = outPath;

	}
	
	/**
	 * 必须在{@link #querKegArrayUrl}方法之后调用
	 * @return
	 */
	public int getDownloadPicNum() {
		return lsPngWebUrl.size();
	}
	/**
	 * 访问kegarray的url，获得所有待下载的链接
	 */
	public void querKegArrayUrl() {
		try {
			lsPngWebUrl = findWebHref(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void running() {
		ArrayList<String> lsPngWebUrl = new ArrayList<>();
		try {
			lsPngWebUrl = findWebHref(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int downloadNum = 0;
		for (String pngWebUrl : lsPngWebUrl) {
			suspendCheck();
			if (flagStop) {
				break;
			}
			String pngHref = findPngHref(pngWebUrl);
			downPng(pngHref);
			setRunInfo(downloadNum);
			downloadNum++;
		}
		logger.info("总共需要下载"+lsPngWebUrl.size()+"个文件!");
	}
	/**
	 * 根据给定网页，找到存放图片的网页的链接；
	 * @param URL 给定网页的链接
	 * @return 存放PNG网页的链接的list
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException 
	 */
	private ArrayList<String> findWebHref(String URL) throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		ArrayList<String> lsUrl = new ArrayList<String>();
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUriGet(URL);
		httpFetch.queryExp();
		for (String	oneLines : httpFetch.readResponse()) {
			if (oneLines.contains("target=\"_map\"")) {
				String strUrl = oneLines.split("href=\"")[1].split("\" target=\"_map\">")[0];
				strUrl = "http://www.genome.jp" +strUrl;
				lsUrl.add(strUrl);
				System.out.println(strUrl);
			}
			
		}
		return lsUrl;
	}
	/**
	 * 根据给定的png网页，查找图片的链接
	 * @param pngURL png网页的链接
	 * @return 返回图片的链接
	 */
	private String findPngHref(String pngURL) {
		HttpFetch httpFetch = HttpFetch.getInstance();
		String pngHref = null;
			httpFetch.setUriGet(pngURL);
			if (httpFetch.query(10)) {
				for (String onePngLines : httpFetch.readResponse()) {
					if (onePngLines.contains("usemap=\"#mapdata\"")) {
						String pattern = "(?<=<img src=\").+?(?=\" name=\")|(?<=<img src=\").+?(?=\" usemap=)";
						Pattern sp = Pattern.compile(pattern);
						Matcher matcher = sp.matcher(onePngLines);
						while(matcher.find()){
							pngHref =  matcher.group();
						}        
						pngHref = "http://www.genome.jp"+ pngHref;
					}
				}
			}else {
				logger.error("PNGweb Not Find");
			}
		System.out.println(pngHref);	
		return pngHref;
	}
	
	/**
	 *  根据图片的链接，下载该图片
	 * @param pngHrefString 图片的链接
	 * @param outFile 输出图片的文件
	 */
	private void downPng(String pngHrefString) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpFetch httpFetch = HttpFetch.getInstance();
				httpFetch.setUriGet(pngHrefString);
				try {
					httpFetch.queryExp();
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] urlTmps =  pngHrefString.split("/");
				int length = urlTmps.length;
				String pngName = urlTmps[length-1];
				try {
					httpFetch.download(outPath + pngName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("下载"+pngName);
			}
		}).start();
	}

}
