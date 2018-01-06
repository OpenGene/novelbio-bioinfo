package com.novelbio.test.csa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.HttpFetchMultiThread;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class RiceXProFetchData {
	static String URL_GGEP = "http://ricexpro.dna.affrc.go.jp/GGEP/view-plot-data.php?featurenum=";
	static String URL_Reproductive_organs = "http://ricexpro.dna.affrc.go.jp/RXP_0010/view-plot-data.php?featurenum=";
	static String URL_LEAF_DIURNAL = "http://ricexpro.dna.affrc.go.jp/RXP_0002/view-plot-data.php?featurenum=";
	private static String rawdata = "raw-div";
	private static String normdata = "nrm-div";
	List<String[]> lsRiceLoc2Ids;

	HttpFetchMultiThread httpFetch = HttpFetchMultiThread.getInstance();
	
	String outRiceNormFile;
	String outRiceRawFile;

	String url;
	
	public static void main(String[] args) {
		String outFile = "/home/novelbio/tmp/riceXProValueGGEP.txt";
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/tmp/riceXProId2.txt");
		List<String[]> lsRiceLoc2Ids = new ArrayList<>();
		for (String content : txtRead.readlines()) {
			String[] locId = content.split("\t");
			if (locId.length <2 || StringOperate.isRealNull(locId[1])) {
				continue;
			}
			lsRiceLoc2Ids.add(locId);
		}
		txtRead.close();
		
		RiceXProFetchData riceXProFetch = new RiceXProFetchData();
		riceXProFetch.setUrl(URL_GGEP);
		riceXProFetch.setLsRiceLocIds(lsRiceLoc2Ids);
		riceXProFetch.setOutRiceIdsFile(outFile);
		riceXProFetch.runRiceIds();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setLsRiceLocIds(List<String[]> lsRiceLoc2Ids) {
		this.lsRiceLoc2Ids = lsRiceLoc2Ids;
	}
	
	public void setOutRiceIdsFile(String outRiceIdsFile) {
		this.outRiceRawFile = FileOperate.changeFileSuffix(outRiceIdsFile, ".raw", null);
		this.outRiceNormFile = FileOperate.changeFileSuffix(outRiceIdsFile, ".nrm", null);
	}
	
	public void runRiceIds() {
		Set<String> setRiceIdAlreadyRun = getRiceIdAlreadyRun();
		String outRiceNormFileTmp = outRiceNormFile + ".tmp";
		TxtReadandWrite txtWriteNorm = new TxtReadandWrite(outRiceNormFileTmp, true);
		String outRiceRawFileTmp = outRiceRawFile + ".tmp";
		TxtReadandWrite txtWriteRaw = new TxtReadandWrite(outRiceRawFileTmp, true);

		if (FileOperate.isFileExist(outRiceNormFile)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(outRiceNormFile);
			for (String content : txtRead.readlines()) {
				txtWriteNorm.writefileln(content);
			}
			txtRead.close();
		}

		if (FileOperate.isFileExist(outRiceRawFile)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(outRiceRawFile);
			for (String content : txtRead.readlines()) {
				txtWriteRaw.writefileln(content);
			}
			txtRead.close();
		}
		
		boolean isFinish = true;
		int i = 0;
		for (String[] locId : lsRiceLoc2Ids) {
			if (setRiceIdAlreadyRun.contains(locId[1])) {
				continue;
			}
			if (++i % 20 == 0) {
				System.out.println(i);
				txtWriteNorm.flush();
				txtWriteRaw.flush();
			}
			try {
				String response = fetchData(locId[1]);
				List<Double> lsRawData = parseRawData(response);
				List<String> lsRaw = new ArrayList<>();
				lsRaw.add(locId[0]); lsRaw.add(locId[1]);
				for (Double value : lsRawData) {
					lsRaw.add(value+"");
				}
				txtWriteRaw.writefileln(ArrayOperate.cmbString(lsRaw, "\t"));
				
				List<Double> lsNrmData = parseNormData(response);
				List<String> lsNrm = new ArrayList<>();
				lsNrm.add(locId[0]); lsNrm.add(locId[1]);
				for (Double value : lsNrmData) {
					lsNrm.add(value+"");
				}
				txtWriteNorm.writefileln(ArrayOperate.cmbString(lsNrm, "\t"));
				
			} catch (Exception e) {
				isFinish = false;
			}
		}
		System.out.println(isFinish);
		txtWriteNorm.close();
		txtWriteRaw.close();
		FileOperate.moveFile(true, outRiceRawFileTmp, outRiceRawFile);
		FileOperate.moveFile(true, outRiceNormFileTmp, outRiceNormFile);
	}
	
	public Set<String> getRiceIdAlreadyRun() {
		Set<String> setRiceIdAlreadyRun = new HashSet<>();
		if (!FileOperate.isFileExist(outRiceRawFile)) {
			return new HashSet<>();
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(outRiceRawFile);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			setRiceIdAlreadyRun.add(ss[1]);
		}
		txtRead.close();
		return setRiceIdAlreadyRun;
	}
	
	public void readRiceXProId() {
		
	}
	private String fetchData(String riceXProId) throws ClientProtocolException, IOException {
		return httpFetch.queryGetUriStr(url + riceXProId);
	}
	
	private List<Double> parseRawData(String response) throws ClientProtocolException, IOException {
		return parseData(response, "raw-div");
	}
	private List<Double> parseNormData(String response) throws ClientProtocolException, IOException {
		return parseData(response, "nrm-div");
	}
	private List<Double> parseData(String response, String id) throws ClientProtocolException, IOException {
		Document doc = Jsoup.parse(response);
		Elements eles = doc.getElementsByAttributeValue("id", id).get(0).getElementsByTag("tr");
		Element eleMean = null;
		for (Element element : eles) {
			Elements elesub = element.getElementsByTag("th");
			if (elesub.size() == 0) {
				continue;
			}
			if (element.getElementsByTag("th").get(0).toString().contains("mean")) {
				eleMean = element;
			}
		}
		
		List<Double> lsResult = new ArrayList<>();
		Elements eleValues = eleMean.getElementsByTag("td");
		for (Element eleVal : eleValues) {
			lsResult.add(Double.parseDouble(eleVal.childNode(0).toString()));
		}
		return lsResult;
	}
	
}
