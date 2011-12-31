package com.novelbio.analysis.seq.snpNCBI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.velocity.runtime.directive.Stop;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 查找samtools的mpileup文件
 * 获得每一个位点的序列情况，ref是什么，总reads数，插入多少缺失多少等等信息
 * @author zong0jie
 * 用MapInfoSnpIdel代替
 */
public class SiteInfo {
	
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.setFile(9606, parentFile + "B_Result2.xls", parentFile + "Bdetailmpileup.txt");
		siteInfo.setFile(9606, parentFile + "A_Result2.xls", parentFile + "Adetailmpileup.txt");
	
		siteInfo.setFile(9606, parentFile + "C_Result2.xls", parentFile + "Cdetailmpileup.txt");
		siteInfo.setFile(9606, parentFile + "D_Result2.xls", parentFile + "Ddetailmpileup.txt");
		siteInfo.getGATKFile();
	}
	
	/**
	 * GATK的vcf文件处理后的文件与samtools得到的文件的对照表
	 */
	HashMap<String, String> hashSnpSamFile = new LinkedHashMap<String, String>();
	String sep = "@//@";
	int taxID = 0;
	/**
	 * 输入经过处理的GATK文件
	 * @param file
	 */
	public void getGATKFile()
	{
		/**
		 * 保存总体无冗余snp位点信息
		 */
		LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp = new LinkedHashMap<String, MapInfoSnpIndel>();
		/**
		 * 分文件保存每个文件的snp位点信息
		 */
		LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp = new LinkedHashMap<String, LinkedHashMap<String,MapInfoSnpIndel>>();
		for (String string : hashSnpSamFile.keySet()) {
			LinkedHashMap<String, MapInfoSnpIndel> hashFileChrLocSnpTmp = new LinkedHashMap<String, MapInfoSnpIndel>();
			hashFileChrLocSnp.put(string, hashFileChrLocSnpTmp);
			ArrayList<String[]> ls = ExcelTxtRead.readLsExcelTxt(string, 2);
			for (String[] strings : ls) {
				MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(taxID, strings[0], Integer.parseInt(strings[1]), strings[2], strings[4]);
				hashChrLocSnp.put(mapInfoSnpIndel.getChrID()+sep+mapInfoSnpIndel.getStart(), mapInfoSnpIndel);
				hashFileChrLocSnpTmp.put(mapInfoSnpIndel.getChrID()+sep+mapInfoSnpIndel.getStart(), mapInfoSnpIndel);
			}
		}
		setMapInfo(hashFileChrLocSnp, hashChrLocSnp);
	}
	/**
	 * 
	 * 安顺序输入GATK处理好的文件和samtools的pileup文件
	 * @param taxID
	 * @param file
	 * @param samPileupFile
	 */
	public void setFile(int taxID, String file, String samPileupFile) {
		hashSnpSamFile.put(file, samPileupFile);
		this.taxID = taxID;
	}
	
	/**
	 * 获得每个文本缺少的snp，然后写入每个补充文件中
	 * @param hashFileChrLocSnp 分文件保存的snp信息
	 * @param hashChrLocSnp 全体snp信息
	 */
	private void setMapInfo(LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp, LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp)
	{
		for (String fileName : hashFileChrLocSnp.keySet()) {
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_Complement2", "txt"), true);
			ArrayList<MapInfoSnpIndel> lsFileNoMapInfo = getNoSiteMapInfo(hashFileChrLocSnp.get(fileName), hashChrLocSnp);
			MapInfoSnpIndel.getSiteInfo(lsFileNoMapInfo, hashSnpSamFile.get(fileName));
			for (MapInfoSnpIndel mapInfoSnpIndel : lsFileNoMapInfo) {
				String key = mapInfoSnpIndel.getChrID() + sep + mapInfoSnpIndel.getStart();
				MapInfoSnpIndel otherMap = hashChrLocSnp.get(key);//正常的别的样本的信息
				String tmpResult = mapInfoSnpIndel.getChrID()+"\t"+mapInfoSnpIndel.getStart()+"\t"+otherMap.getRefBase()+"\t" +mapInfoSnpIndel.getAllelic_depths_Ref();
				tmpResult = tmpResult + "\t" +otherMap.getThisBase() + "\t" + mapInfoSnpIndel.getSeqType(otherMap);
				txtOut.writefileln(tmpResult);
			}
			txtOut.close();
		}
	}

	/**
	 * 给定某个文件特有的mapInfo和总文件的mapInfo
	 * 找出该文件缺少的mapInfo
	 * @param hashTmp
	 * @param hashAll
	 * @return
	 */
	private ArrayList<MapInfoSnpIndel> getNoSiteMapInfo(LinkedHashMap<String, MapInfoSnpIndel> hashTmp, LinkedHashMap<String, MapInfoSnpIndel> hashAll)
	{
		ArrayList<MapInfoSnpIndel> lsResult = new ArrayList<MapInfoSnpIndel>();
		for (String key : hashAll.keySet()) {
			if (key.contains("662622")) {
				System.out.println("stop");
			}
			if (!hashTmp.containsKey(key)) {
		
				MapInfoSnpIndel mapInfoSnpIndelTmp = hashAll.get(key);
				MapInfoSnpIndel mapInfoSnpIndelTmpResult = new MapInfoSnpIndel(taxID, mapInfoSnpIndelTmp.getChrID(), mapInfoSnpIndelTmp.getStart(), mapInfoSnpIndelTmp.getRefBase(), mapInfoSnpIndelTmp.getThisBase());
				lsResult.add(mapInfoSnpIndelTmpResult);
			}
		}
		return lsResult;
	}
	
}
