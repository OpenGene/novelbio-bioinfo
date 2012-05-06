package com.novelbio.analysis.seq.resequencing;

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
public class SamtoolsPileUpSiteInfo {
	
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		SamtoolsPileUpSiteInfo siteInfo = new SamtoolsPileUpSiteInfo();
		siteInfo.setFile(9606, parentFile + "B_Result_New.xls", parentFile + "BdetailmpileupWithoutBC.txt");
		siteInfo.setFile(9606, parentFile + "A_Result_New.xls", parentFile + "AdetailmpileupWithoutBC.txt");
		siteInfo.setFile(9606, parentFile + "C_Result_New.xls", parentFile + "CdetailmpileupWithoutBC.txt");
		siteInfo.setFile(9606, parentFile + "D_Result_New.xls", parentFile + "DdetailmpileupWithoutBC.txt");
		siteInfo.getGATKFile();
	}
	
	/**
	 * GATK的vcf文件处理后的文件与samtools得到的文件的对照表<br>
	 * key: 某时期所对应的snp文件<br>
	 * value：某时期所对应的pileup文件
	 */
	HashMap<String, String> hashSnpSamFile = new LinkedHashMap<String, String>();
	String sep = "@//@";
	int taxID = 0;
	/**
	 * 输入经过处理的GATK文件,如：<br>
	 * chr1    14353   A       58      ...........,.............,....,....,.g.,..,..,...,....,..^!.    7=B=IBEBBBE6D@GD89IDHGGHHEG@@DEHHGIBHCI6IIBHHGIII>IHIF=III
	 * @param file
	 */
	public void getGATKFile()
	{
		/** 保存总体无冗余snp位点信息 
		 *  key：chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
		 * */
		LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp = new LinkedHashMap<String, MapInfoSnpIndel>();
		/** 分文件保存每个文件的snp位点信息 <br>
		 * key: 某时期所对应的snp文件<br>
		 * value: 该时期每个snp位点所对应的MapInfoSnpIndel
		 *    key：chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
		 * */
		LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp = new LinkedHashMap<String, LinkedHashMap<String,MapInfoSnpIndel>>();
		for (String string : hashSnpSamFile.keySet()) {
			LinkedHashMap<String, MapInfoSnpIndel> hashFileChrLocSnpTmp = new LinkedHashMap<String, MapInfoSnpIndel>();
			hashFileChrLocSnp.put(string, hashFileChrLocSnpTmp);
			/**
			 * 前面处理的结果文件，格式为
			 * ChrID	SnpLoc	RefBase	Allelic_depths_Ref	ThisBase	Allelic_depths_Alt 	Quality等等
			 * chr1	887801	A	0	G	6	205.04
			 */
			ArrayList<String[]> ls = ExcelTxtRead.readLsExcelTxt(string, 2);
			for (String[] strings : ls) {
				MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(taxID, strings[0], Integer.parseInt(strings[1]), strings[2], strings[4]);
				/**总体snp的hash*/
				hashChrLocSnp.put(mapInfoSnpIndel.getRefID()+sep+mapInfoSnpIndel.getRefSnpIndelStart(), mapInfoSnpIndel);
				/**每个单独处理的snp的hash*/
				hashFileChrLocSnpTmp.put(mapInfoSnpIndel.getRefID()+sep+mapInfoSnpIndel.getRefSnpIndelStart(), mapInfoSnpIndel);
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
	 * @param hashAllChrLocSnp 全体snp信息
	 * key：chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
	 */
	private void setMapInfo(LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp, 
			LinkedHashMap<String, MapInfoSnpIndel> hashAllChrLocSnp) {
		for (String fileName : hashFileChrLocSnp.keySet()) {
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_Complement2", "txt"), true);
			ArrayList<MapInfoSnpIndel> lsFileNoMapInfo = getNoSiteMapInfo(hashFileChrLocSnp.get(fileName), hashAllChrLocSnp);
			MapInfoSnpIndel.getSiteInfo(lsFileNoMapInfo, hashSnpSamFile.get(fileName));
			for (MapInfoSnpIndel mapInfoSnpIndel : lsFileNoMapInfo) {
				String key = mapInfoSnpIndel.getRefID() + sep + mapInfoSnpIndel.getRefSnpIndelStart();
				MapInfoSnpIndel otherMap = hashAllChrLocSnp.get(key);
				String tmpResult = mapInfoSnpIndel.getSeqTypeNumStr(otherMap);
				txtOut.writefileln(tmpResult);
			}
			txtOut.close();
		}
	}

	/**
	 * 给定某个文件特有的mapInfo和总文件的mapInfo<br>
	 * 找出该文件缺少的mapInfo
	 * @param hashTmp
	 * @param hashAll
	 * @return
	 */
	private ArrayList<MapInfoSnpIndel> getNoSiteMapInfo(LinkedHashMap<String, MapInfoSnpIndel> hashTmp, LinkedHashMap<String, MapInfoSnpIndel> hashAll) {
		ArrayList<MapInfoSnpIndel> lsResult = new ArrayList<MapInfoSnpIndel>();
		for (String key : hashAll.keySet()) {
			if (!hashTmp.containsKey(key)) {
				MapInfoSnpIndel mapInfoSnpIndelTmp = hashAll.get(key);
				MapInfoSnpIndel mapInfoSnpIndelTmpResult = new MapInfoSnpIndel(taxID, mapInfoSnpIndelTmp.getRefID(), mapInfoSnpIndelTmp.getRefSnpIndelStart(), mapInfoSnpIndelTmp.getRefBase(), mapInfoSnpIndelTmp.getThisBase());
				lsResult.add(mapInfoSnpIndelTmpResult);
			}
		}
		return lsResult;
	}
	
}
