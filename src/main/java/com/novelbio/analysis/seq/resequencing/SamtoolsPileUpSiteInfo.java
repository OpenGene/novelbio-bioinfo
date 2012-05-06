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
 * ����samtools��mpileup�ļ�
 * ���ÿһ��λ������������ref��ʲô����reads�����������ȱʧ���ٵȵ���Ϣ
 * @author zong0jie
 * ��MapInfoSnpIdel����
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
	 * GATK��vcf�ļ��������ļ���samtools�õ����ļ��Ķ��ձ�<br>
	 * key: ĳʱ������Ӧ��snp�ļ�<br>
	 * value��ĳʱ������Ӧ��pileup�ļ�
	 */
	HashMap<String, String> hashSnpSamFile = new LinkedHashMap<String, String>();
	String sep = "@//@";
	int taxID = 0;
	/**
	 * ���뾭�������GATK�ļ�,�磺<br>
	 * chr1    14353   A       58      ...........,.............,....,....,.g.,..,..,...,....,..^!.    7=B=IBEBBBE6D@GD89IDHGGHHEG@@DEHHGIBHCI6IIBHHGIII>IHIF=III
	 * @param file
	 */
	public void getGATKFile()
	{
		/** ��������������snpλ����Ϣ 
		 *  key��chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
		 * */
		LinkedHashMap<String, MapInfoSnpIndel> hashChrLocSnp = new LinkedHashMap<String, MapInfoSnpIndel>();
		/** ���ļ�����ÿ���ļ���snpλ����Ϣ <br>
		 * key: ĳʱ������Ӧ��snp�ļ�<br>
		 * value: ��ʱ��ÿ��snpλ������Ӧ��MapInfoSnpIndel
		 *    key��chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
		 * */
		LinkedHashMap<String, LinkedHashMap<String, MapInfoSnpIndel>> hashFileChrLocSnp = new LinkedHashMap<String, LinkedHashMap<String,MapInfoSnpIndel>>();
		for (String string : hashSnpSamFile.keySet()) {
			LinkedHashMap<String, MapInfoSnpIndel> hashFileChrLocSnpTmp = new LinkedHashMap<String, MapInfoSnpIndel>();
			hashFileChrLocSnp.put(string, hashFileChrLocSnpTmp);
			/**
			 * ǰ�洦��Ľ���ļ�����ʽΪ
			 * ChrID	SnpLoc	RefBase	Allelic_depths_Ref	ThisBase	Allelic_depths_Alt 	Quality�ȵ�
			 * chr1	887801	A	0	G	6	205.04
			 */
			ArrayList<String[]> ls = ExcelTxtRead.readLsExcelTxt(string, 2);
			for (String[] strings : ls) {
				MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(taxID, strings[0], Integer.parseInt(strings[1]), strings[2], strings[4]);
				/**����snp��hash*/
				hashChrLocSnp.put(mapInfoSnpIndel.getRefID()+sep+mapInfoSnpIndel.getRefSnpIndelStart(), mapInfoSnpIndel);
				/**ÿ�����������snp��hash*/
				hashFileChrLocSnpTmp.put(mapInfoSnpIndel.getRefID()+sep+mapInfoSnpIndel.getRefSnpIndelStart(), mapInfoSnpIndel);
			}
		}
		setMapInfo(hashFileChrLocSnp, hashChrLocSnp);
	}
	/**
	 * 
	 * ��˳������GATK����õ��ļ���samtools��pileup�ļ�
	 * @param taxID
	 * @param file
	 * @param samPileupFile
	 */
	public void setFile(int taxID, String file, String samPileupFile) {
		hashSnpSamFile.put(file, samPileupFile);
		this.taxID = taxID;
	}
	
	/**
	 * ���ÿ���ı�ȱ�ٵ�snp��Ȼ��д��ÿ�������ļ���
	 * @param hashFileChrLocSnp ���ļ������snp��Ϣ
	 * @param hashAllChrLocSnp ȫ��snp��Ϣ
	 * key��chrID + sep + mapInfoSnpIndel.getRefSnpIndelStart()
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
	 * ����ĳ���ļ����е�mapInfo�����ļ���mapInfo<br>
	 * �ҳ����ļ�ȱ�ٵ�mapInfo
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
