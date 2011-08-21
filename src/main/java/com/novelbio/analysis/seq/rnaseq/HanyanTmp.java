package com.novelbio.analysis.seq.rnaseq;

import java.io.BufferedReader;
import java.util.HashMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.preprocess.Comb;
import com.novelbio.analysis.seq.genomeNew.GffChrHanYanChrom;
import com.novelbio.analysis.seq.genomeNew.GffChrUnionHanYanRefSeq;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class HanyanTmp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = getProjectPath();
		System.out.println(filePath);
		try {
			combMapPeak(filePath);
			testHanyanRefseq();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//这个不乱码;

	}
	 
	
	static HashMap<String, String> hashConf = null;
	
	public static void combMapPeak(String filePath) throws Exception {
		String thisFilePath=filePath;
		//读取配置文件
		//配置文件格式，第一行soap程序的路径
		//第二行到结束： species \t ChrLenFile \t IndexFile \n
		//物种有 hs，os，mm，dm，ce
		TxtReadandWrite txtConf = new TxtReadandWrite();
		txtConf.setParameter(thisFilePath+"/Conf.txt", false, true);
		BufferedReader reader = txtConf.readfile();
		String content = "";
		hashConf = new HashMap<String, String>();
		while ((content = reader.readLine()) != null) {
			content = content.trim();
			if (content.startsWith("#")||content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			hashConf.put(ss[0], ss[1]);
		}
	}

	public static String getProjectPath() {
		 java.net.URL url = HanyanTmp.class.getProtectionDomain().getCodeSource().getLocation();
		 String filePath = null;
		 try {
		 filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 if (filePath.endsWith(".jar"))
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		 java.io.File file = new java.io.File(filePath);
		 filePath = file.getAbsolutePath();
//		 filePath = FileOperate.getParentPathName(FileOperate.getParentPathName(filePath));
		 return filePath;
	}
	
	 
	public static void testHanyan() throws Exception {
		GffChrHanYanChrom gffChrUnion = new GffChrHanYanChrom();
		gffChrUnion.loadChr(hashConf.get("ChromFa"));
		gffChrUnion.loadGff(NovelBioConst.GENOME_GFF_TYPE_UCSC, hashConf.get("GFF"));
		gffChrUnion.loadMap(hashConf.get("BedFile"),Integer.parseInt(hashConf.get("startRegion")),
				hashConf.get("ChromFa"), Integer.parseInt(hashConf.get("invNum")), 100, hashConf.get("uniqReads").equals("True"),Integer.parseInt(hashConf.get("startCode")));
		
		String gene = hashConf.get("NormalizedType");
		int normalizedType = 0;
		if (gene.trim().equals("PER_GENE")) {
			normalizedType = MapReads.NORMALIZATION_PER_GENE;
		}
		else {
			normalizedType = MapReads.NORMALIZATION_ALL_READS;
		}
		gffChrUnion.drawHeatMap(hashConf.get("Pic"), hashConf.get("Prix"), Integer.parseInt(hashConf.get("AtgUp")), Integer.parseInt(hashConf.get("AtgDown")),normalizedType);
	}
	
	public static void testHanyanRefseq() throws Exception {
		Boolean booFilterCis5to3 = null;
		if (hashConf.get("filterCis5to3") != null) {
			booFilterCis5to3 = hashConf.get("filterCis5to3").equals("+");
		}
		
		GffChrUnionHanYanRefSeq gffChrUnion = new GffChrUnionHanYanRefSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,
				hashConf.get("GFF"), hashConf.get("ChromFa"),9606);
		gffChrUnion.loadMap(hashConf.get("BedFile"),Integer.parseInt(hashConf.get("startRegion")),
				hashConf.get("ChromFa"), Integer.parseInt(hashConf.get("invNum")), 100, hashConf.get("uniqReads").equals("True"),
				Integer.parseInt(hashConf.get("startCode")), Integer.parseInt(hashConf.get("colUnique")),
				booFilterCis5to3,
				hashConf.get("uniqMapping").equals("True"));
		String gene = hashConf.get("NormalizedType");
		int normalizedType = 0;
		if (gene.trim().equals("PER_GENE")) {
			normalizedType = MapReads.NORMALIZATION_PER_GENE;
		}
		else {
			normalizedType = MapReads.NORMALIZATION_ALL_READS;
		}
		gffChrUnion.drawHeatMap(hashConf.get("Pic"), hashConf.get("Prix"), Integer.parseInt(hashConf.get("AtgUp")), Integer.parseInt(hashConf.get("AtgDown")),normalizedType);
	}
}
