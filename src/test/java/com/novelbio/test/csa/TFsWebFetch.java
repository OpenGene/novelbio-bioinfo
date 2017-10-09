package com.novelbio.test.csa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.novelbio.base.dataOperate.HttpFetchMultiThread;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 用来抓转录因子数据库的爬虫
 * @author zong0
 *
 */
public class TFsWebFetch {
	List<String> lsTFs = new ArrayList<>();
	HttpFetchMultiThread httpFetch = HttpFetchMultiThread.getInstance();

	public static void main(String[] args) throws ClientProtocolException, IOException {
		TFsWebFetch tFsWebFetch = new TFsWebFetch();

		TxtReadandWrite txtReadTf = new TxtReadandWrite("/media/winE/rice-tfs");
		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/rice-tfs-info.txt", true);
		for (String content : txtReadTf.readlines()) {
			String tf = content.split("\\(")[0].trim();
			List<String[]> lsTmpResult = tFsWebFetch.parseTFs(tf);
			if (lsTmpResult.isEmpty()) {
				txtReadTf.close();
				txtWrite.close();
				throw new RuntimeException("cannot find info on " + tf);
			}
			for (String[] strings : lsTmpResult) {
				txtWrite.writefileln(strings);
			}
		}
		txtReadTf.close();
		txtWrite.close();
	}
	
	public List<String[]> parseTFs(String tfName) throws ClientProtocolException, IOException {
		String html = httpFetch.queryGetUriStr("http://planttfdb.cbi.pku.edu.cn/family.php?sp=Osj&fam=" + tfName);
		List<String> lsTfs = parseTFsHtml(html);
		List<String[]> lsResult = new ArrayList<>();
		for (String tf : lsTfs) {
			lsResult.add(new String[]{tf, tfName});
		}
		return lsResult;
	}
	
	/**
	 * 给定html的结果，将其解析出来
	 * @return
	 */
	public List<String> parseTFsHtml(String html) {
		List<String> lsGeneName = new ArrayList<>();
		Document doc = Jsoup.parse(html);
		Elements eleTable = doc.getElementsByClass("tf_table");
		Elements eleItems = eleTable.select("td");
		for (Element element : eleItems) {
			String locId = element.select("a").html();
			if (!locId.startsWith("LOC_")) {
				continue;
			}
			lsGeneName.add(locId);
		}
		return lsGeneName;
	}
}
