package com.novelbio.test.csa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.species.Species;
import com.sun.corba.se.spi.orb.StringPair;

public class Rice3KIndel {
	 /** key index 具体第几个indel，从0开始计算
	 * value LocId@ref@alt
	 */
	Map<Integer, String> mapIndex2LocId2Ref2Alt;
	Set<String> setLocId;

	public static void main(String[] args) {
		addGeneLocInfo();
	}
	public static void runGetIndelInfo() {
		String parentPath = "/home/novelbio/test/3krice/";
		String riceBim = parentPath + "Nipponbare_indel.bim";
		String riceIndel = parentPath + "NipIndel_modify_anno_orf_shift.txt";
		String ricePed = parentPath + "Nipponbare_indel.result.ped";
		String riceIndelResult = parentPath + "Nipponbare_indel.orf.result.larger-than-3bp.txt";

		
		Rice3KIndel rice3kIndel = new Rice3KIndel();
		rice3kIndel.setMapIndex2LocId2Ref2Alt(riceBim, riceIndel);
		Set<String> setLocId = rice3kIndel.getSetLocId();
		Map<String, Map<String, double[]>> mapSample2MapGene2Num = new LinkedHashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(ricePed);
		txtRead.setReadMaxLineNum(100000000);
		for (String content : txtRead.readlines()) {
			String[] sampleIndelInfo = content.split(" ");
			System.out.println(sampleIndelInfo[0]);
			mapSample2MapGene2Num.put(sampleIndelInfo[0], rice3kIndel.getSampleMapGeneId2IndelNum(sampleIndelInfo));
		}
		txtRead.close();
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(riceIndelResult, true);
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add("LocId");
		for (String sampleId : mapSample2MapGene2Num.keySet()) {
			lsTitle.add(sampleId);
		}
		txtWrite.writefileln(lsTitle.toArray(new String[0]));
		
		for (String geneId : setLocId) {
			List<String> lsResult = new ArrayList<>();
			lsResult.add(geneId);
			for (String sample : mapSample2MapGene2Num.keySet()) {
				Map<String, double[]> mapGene2Num = mapSample2MapGene2Num.get(sample);
				double num = mapGene2Num.get(geneId)[0];
				lsResult.add(formatData(num));
			}
			txtWrite.writefileln(lsResult.toArray(new String[0]));
		}
		
		txtWrite.close();
	}
	
	/** 根据基因添加其所在坐标信息 */
	public static void addGeneLocInfo() {
		String gff = "/media/nbfs/nbCloud/public/nbcplatform/genome/species/39947/tigr7/gff/all.gff3";
		String inFile = "/home/novelbio/test/3krice/Nipponbare_indel.orf.result.larger-than-3bp-800rice.txt";
		String outFile = "/home/novelbio/test/3krice/Nipponbare_indel.orf.result.larger-than-3bp-800rice-loc.txt";
		GffHashGene gffHashGene = new GffHashGene(gff);
		TxtReadandWrite txtRead = new TxtReadandWrite(inFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		String title = txtRead.readFirstLine();
		String[] sstitle = title.split("\t");
		List<String> lsTitle = ArrayOperate.converArray2List(sstitle);
		lsTitle.add(1, "chrId"); lsTitle.add(2, "startPos"); lsTitle.add(3, "endPos");
		txtWrite.writefileln(lsTitle.toArray(new String[0]));
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			List<String> lsInfo = ArrayOperate.converArray2List(ss);
			GffDetailGene detailGene = gffHashGene.searchLOC(ss[0]);
			if (detailGene == null) {
				txtRead.close();
				txtWrite.close();
				throw new RuntimeException("cannot find gene " + ss[0]);
			}
			lsInfo.add(1, detailGene.getRefID()); lsInfo.add(2, detailGene.getStartAbs() + ""); lsInfo.add(3, detailGene.getEndAbs() + "");
			txtWrite.writefileln(lsInfo.toArray(new String[0]));
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/** 从结果中将洪骏的800个水稻样本提取出来 */
	public static void getRice800() {
		String rice800 = "/home/novelbio/test/3krice/hongjun-800-811-650.fam";
		String indelFile = "/home/novelbio/test/3krice/Nipponbare_indel.orf.result.larger-than-3bp.txt";
		String indelFile800 = "/home/novelbio/test/3krice/Nipponbare_indel.orf.result.larger-than-3bp-800rice.txt";
		List<Integer> ls800Index = new ArrayList<>();
		Set<String> setRice800 = new HashSet<>();
		TxtReadandWrite txtReadRice800 = new TxtReadandWrite(rice800);
		TxtReadandWrite txtReadIndelFile = new TxtReadandWrite(indelFile);
		for (String content : txtReadRice800.readlines()) {
			String[] ss = content.split(" ");
			setRice800.add(ss[0]);
		}
		String title = txtReadIndelFile.readFirstLine();
		String[] ss = title.split("\t");
		for (int i = 0; i < ss.length; i++) {
			if (setRice800.contains(ss[i])) {
				ls800Index.add(i);
			}
		}
		txtReadRice800.close();
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(indelFile800, true);
		for (String content : txtReadIndelFile.readlines()) {
			List<String> lsResult = new ArrayList<>();
			String[] ss2 = content.split("\t");
			lsResult.add(ss2[0]);
			for (Integer index : ls800Index) {
				lsResult.add(ss2[index]);
			}
			txtWrite.writefileln(lsResult.toArray(new String[0]));
		}
		txtReadIndelFile.close();
		txtWrite.close();
		
	
	}
	
	private static String formatData(double value) {
		String result = "";
		if (value%1 == 0) {
			result = (int)value + "";
		} else {
			result = value + "";
		}
		return result;
	}
	
	public Map<String, double[]> getSampleMapGeneId2IndelNum(String[] sampleIndelInfo) {
		Map<String, double[]> mapLocId2Value = new HashMap<>();
		for (String locId : setLocId) {
			mapLocId2Value.put(locId, new double[] {0});
		}
		
		int index = -1;
		for (int i = 6; i < sampleIndelInfo.length-1; i=i+2) {
			index++;

			String allel1 = sampleIndelInfo[i];
			String allel2 = sampleIndelInfo[i+1];
			if (!mapIndex2LocId2Ref2Alt.containsKey(index)) continue;
			
			if (allel1.equals("0") && allel2.equals("0")) {
				continue;
			}
			String locId2Ref2Alt = mapIndex2LocId2Ref2Alt.get(index);
			String[] ss2 = locId2Ref2Alt.split("@");
			String locId = ss2[0];
			String ref = ss2[1];
			String alt = ss2[2];
			double num = 0;
			if (allel1.equals(alt)) {
				num+=0.5;
			}
			if (allel2.equals(alt)) {
				num+=0.5;
			}
			double[] tmpValue = mapLocId2Value.get(locId);
			tmpValue[0] = tmpValue[0] + num;
		}
		return mapLocId2Value;
	}
	
	public void setMapIndex2LocId2Ref2Alt(String bimFile, String annoFile) {
		mapIndex2LocId2Ref2Alt = getMapIndels(bimFile, annoFile);
		setLocId = new LinkedHashSet<>();
		for (String locId2Ref2Alt : mapIndex2LocId2Ref2Alt.values()) {
			String[] ss = locId2Ref2Alt.split("@");
			setLocId.add(ss[0]);
		}
	}
	public Set<String> getSetLocId() {
		return setLocId;
	}
	/**
	 * @param bimFile
	 * @param annoFile
	 * @return
	 * key index 具体第几个indel
	 * value LocId@ref@alt
	 */
	private Map<Integer, String> getMapIndels(String bimFile, String annoFile) {
		TxtReadandWrite txtReadAnnoFile = new TxtReadandWrite(annoFile);
		Map<String, String> mapSnpId2LocId = new LinkedHashMap<>();
		for (String content : txtReadAnnoFile.readlines()) {
			if (content.startsWith("#")) continue;
			
			String[] ss = content.split("\t");
			//仅考虑indel长度大于3的突变
			if (ss[4].length() < 4) continue;
			
			mapSnpId2LocId.put(ss[1] , ss[6] + "@" + ss[3] + "@" + ss[4]);
		}
		txtReadAnnoFile.close();
		
		TxtReadandWrite txtReadBim = new TxtReadandWrite(bimFile);
		Map<Integer, String> mapIndex2LocId = new LinkedHashMap<>();
		int i = 0;
		for (String content : txtReadBim.readlines()) {
			String[] ss = content.split("\t");
			if (mapSnpId2LocId.containsKey(ss[1])) {
				mapIndex2LocId.put(i, mapSnpId2LocId.get(ss[1]));
			}
			i++;
		}
		txtReadBim.close();
		return mapIndex2LocId;
	}
}
