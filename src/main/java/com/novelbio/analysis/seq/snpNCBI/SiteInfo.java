package com.novelbio.analysis.seq.snpNCBI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ÿһ��λ������������ref��ʲô����reads�����������ȱʧ���ٵȵ���Ϣ
 * @author zong0jie
 * ��MapInfoSnpIdel����
 */
public class SiteInfo {
	
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.setFile(9606, parentFile + "A_Result2.xls", parentFile + "Adetailmpileup.txt");
		siteInfo.setFile(9606, parentFile + "B_Result2.xls", parentFile + "Bdetailmpileup.txt");
		siteInfo.setFile(9606, parentFile + "C_Result2.xls", parentFile + "Cdetailmpileup.txt");
		siteInfo.setFile(9606, parentFile + "D_Result2.xls", parentFile + "Ddetailmpileup.txt");
		siteInfo.getGATKFile();
	}
	
	/**
	 * GATK��vcf�ļ��������ļ���samtools�õ����ļ��Ķ��ձ�
	 */
	HashMap<String, String> hashSnpSamFile = new HashMap<String, String>();
	String sep = "@//@";
	int taxID = 0;
	/**
	 * ���뾭�������GATK�ļ�
	 * @param file
	 */
	public void getGATKFile()
	{
		/**
		 * ��������������snpλ����Ϣ
		 */
		LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp = new LinkedHashMap<String, MapInfoSnpIndel>();
		/**
		 * ���ļ�����ÿ���ļ���snpλ����Ϣ
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
	 * ��˳������GATK����õ��ļ���samtools��pileup�ļ�
	 * @param samPileupFile
	 */
	public void setFile(int taxID, String file, String samPileupFile) {
		hashSnpSamFile.put(file, samPileupFile);
		this.taxID = taxID;
	}
	
	/**
	 * @param hashFileChrLocSnp ���ļ������snp��Ϣ
	 * @param hashChrLocSnp ȫ��snp��Ϣ
	 */
	private void setMapInfo(LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp, LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp)
	{
		for (String fileName : hashFileChrLocSnp.keySet()) {
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_Complement", "txt"), true);
			ArrayList<MapInfoSnpIndel> lsFileNoMapInfo = getNoSiteMapInfo(hashFileChrLocSnp.get(fileName), hashChrLocSnp);
			MapInfoSnpIndel.getSiteInfo(lsFileNoMapInfo, hashSnpSamFile.get(fileName));
			for (MapInfoSnpIndel mapInfoSnpIndel : lsFileNoMapInfo) {
				String key = mapInfoSnpIndel.getChrID()+sep+mapInfoSnpIndel.getStart();
				MapInfoSnpIndel otherMap = hashChrLocSnp.get(key);//�����ı����������Ϣ
				String tmpResult = mapInfoSnpIndel.getChrID()+"\t"+mapInfoSnpIndel.getStart()+"\t"+mapInfoSnpIndel.getAllelic_depths_Ref();
				tmpResult = tmpResult + "\t" +otherMap.getThisBase() + "\t" + mapInfoSnpIndel.getSeqType(otherMap.getThisBase(), otherMap.getType());
				txtOut.writefileln(tmpResult);
			}
			txtOut.close();
		}
	}

	/**
	 * ����ĳ���ļ����е�mapInfo�����ļ���mapInfo
	 * �ҳ����ļ�ȱ�ٵ�mapInfo
	 * @param hashTmp
	 * @param hashAll
	 * @return
	 */
	private ArrayList<MapInfoSnpIndel> getNoSiteMapInfo(LinkedHashMap<String, MapInfoSnpIndel> hashTmp, LinkedHashMap<String, MapInfoSnpIndel> hashAll)
	{
		ArrayList<MapInfoSnpIndel> lsResult = new ArrayList<MapInfoSnpIndel>();
		for (String key : hashAll.keySet()) {
			if (!hashTmp.containsKey(key)) {
				lsResult.add(hashAll.get(key));
			}
		}
		return lsResult;
	}
	
}
