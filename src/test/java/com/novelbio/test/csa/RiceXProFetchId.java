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
import com.novelbio.base.fileOperate.FileOperate;

public class RiceXProFetchId {
	
	List<String> lsRiceLocIds;

	HttpFetchMultiThread httpFetch = HttpFetchMultiThread.getInstance();
	
	String outRiceIdsFile;
	
	public static void main(String[] args) {
		String outFile = "/home/novelbio/tmp/riceXProId.txt";
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/tmp/all_accID2geneID.list");
		List<String> lsRiceLocIds = new ArrayList<>();
		for (String content : txtRead.readlines()) {
			String locId = content.split("\t")[0];
			if (StringOperate.isRealNull(locId)) {
				continue;
			}
			lsRiceLocIds.add(locId);
		}
		txtRead.close();
		
		RiceXProFetchId riceXProFetch = new RiceXProFetchId();
		riceXProFetch.setLsRiceLocIds(lsRiceLocIds);
		riceXProFetch.setOutRiceIdsFile(outFile);
		riceXProFetch.runRiceIds();
		
		//===========================================
		String outFileGGEP = "/home/novelbio/tmp/riceXProValueGGEP.txt";
		String outFileLEAF = "/home/novelbio/tmp/riceXProValueLeaf.txt";
		String outFileAnther = "/home/novelbio/tmp/riceXProValueAnther.txt";

		txtRead = new TxtReadandWrite("/home/novelbio/tmp/riceXProId.txt");
		List<String[]> lsRiceLoc2Ids = new ArrayList<>();
		for (String content : txtRead.readlines()) {
			String[] locId = content.split("\t");
			if (locId.length <2 || StringOperate.isRealNull(locId[1])) {
				continue;
			}
			lsRiceLoc2Ids.add(locId);
		}
		txtRead.close();
		
		RiceXProFetchData riceXProFetchData = new RiceXProFetchData();
		riceXProFetchData.setUrl(RiceXProFetchData.URL_GGEP);
		riceXProFetchData.setLsRiceLocIds(lsRiceLoc2Ids);
		riceXProFetchData.setOutRiceIdsFile(outFileGGEP);
		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				riceXProFetchData.runRiceIds();
			}
		});
		thread1.start();
		
		RiceXProFetchData riceXProFetchData2 = new RiceXProFetchData();
		riceXProFetchData2.setUrl(RiceXProFetchData.URL_LEAF_DIURNAL);
		riceXProFetchData2.setLsRiceLocIds(lsRiceLoc2Ids);
		riceXProFetchData2.setOutRiceIdsFile(outFileLEAF);
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				riceXProFetchData2.runRiceIds();
			}
		});
		thread2.start();
		
		RiceXProFetchData riceXProFetchData3 = new RiceXProFetchData();
		riceXProFetchData3.setUrl(RiceXProFetchData.URL_Reproductive_organs);
		riceXProFetchData3.setLsRiceLocIds(lsRiceLoc2Ids);
		riceXProFetchData3.setOutRiceIdsFile(outFileAnther);
		Thread thread3 = new Thread(new Runnable() {
			@Override
			public void run() {
				riceXProFetchData3.runRiceIds();
			}
		});
		thread3.start();
		
		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void setLsRiceLocIds(List<String> lsRiceLocIds) {
		this.lsRiceLocIds = lsRiceLocIds;
	}
	public void setOutRiceIdsFile(String outRiceIdsFile) {
		this.outRiceIdsFile = outRiceIdsFile;
	}
	public void runRiceIds() {
		Set<String> setRiceIdAlreadyRun = getRiceIdAlreadyRun();
		String outRiceIdsFileTmp = outRiceIdsFile + ".tmp";
		TxtReadandWrite txtWrite = new TxtReadandWrite(outRiceIdsFileTmp, true);
		
		if (FileOperate.isFileExist(outRiceIdsFile)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(outRiceIdsFile);
			for (String content : txtRead.readlines()) {
				txtWrite.writefileln(content);
			}
			txtRead.close();
		}

		boolean isFinish = true;
		int i = 0;
		for (String locId : lsRiceLocIds) {
			if (setRiceIdAlreadyRun.contains(locId)) {
				continue;
			}
			if (++i % 20 == 0) {
				System.out.println(i);
				txtWrite.flush();
			}
			try {
				List<String> lsIds = fetchRiceXProIds(locId);
				for (String id : lsIds) {
					txtWrite.writefileln(locId + "\t" + id);
				}
			} catch (Exception e) {
				isFinish = false;
			}
		}
		System.out.println(isFinish);
		txtWrite.close();
		FileOperate.moveFile(true, outRiceIdsFileTmp, outRiceIdsFile);
	}
	
	public Set<String> getRiceIdAlreadyRun() {
		Set<String> setRiceIdAlreadyRun = new HashSet<>();
		if (!FileOperate.isFileExist(outRiceIdsFile)) {
			return new HashSet<>();
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(outRiceIdsFile);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			setRiceIdAlreadyRun.add(ss[0]);
		}
		txtRead.close();
		return setRiceIdAlreadyRun;
	}
	
	public List<String> fetchRiceXProIds(String riceLocId) throws ClientProtocolException, IOException {
		List<String> lsIds = new ArrayList<>();
		
		String response = httpFetch.queryPostUriStr("http://ricexpro.dna.affrc.go.jp/gene-search.php", 
				Lists.newArrayList(new String[]{"keyword", riceLocId}, new String[]{"category", "field-development"}));
//		System.out.println(response);
		Document doc = Jsoup.parse(response);
		Element ele = doc.getElementsByAttributeValue("id", "result").get(0);
		Elements elesFinal = ele.getElementsByAttributeValue("class", "graph-link");
		for (Element element : elesFinal) {
			lsIds.add(element.childNodes().get(0).toString());
		}
		if (lsIds.isEmpty()) {
			lsIds.add("");
		}
		return lsIds;
	}
	
}
