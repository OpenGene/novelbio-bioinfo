package com.novelbio.database.model.species;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.geneanno.SpeciesFile.GFFtype;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.service.servgeneanno.ServSpeciesFile;
import com.novelbio.database.service.servgeneanno.ServTaxID;
/**
 * ������Ϣ���������֣��Լ������ļ�����·��
 * @author zong0jie
 */
public class Species {
	/** ȫ������ */
	public static final int ALL_SPECIES = 10;
	/** ��Kegg��д�������� */
	public static final int KEGGNAME_SPECIES = 20;
	/** �����ݿ����е���Ϣ������ */
	public static final int SEQINFO_SPECIES = 30;
	
	int taxID = 0;
	TaxInfo taxInfo = new TaxInfo();
	String version = "";;
	/** ����Щ�汾,0��version 1��year<br>
	 * ��������Ӵ�С����
	 */
	ArrayList<String[]> lsVersion = new ArrayList<String[]>();
	/** key���汾ID,ͨͨСд  value���������Ϣ */
	HashMap<String, SpeciesFile> hashVersion2Species = new HashMap<String, SpeciesFile>();
	ServSpeciesFile servSpeciesFile = new ServSpeciesFile();
	ServTaxID servTaxID = new ServTaxID();
	
	String updateTaxInfoFile = "";
	String updateSpeciesFile = "";
	public Species() {}
	public Species(int taxID) {
		this.taxID = taxID;
		querySpecies();
		if (lsVersion.size() > 0) {
			this.version = lsVersion.get(0)[0];
		}
	}
	public Species(int taxID, String version) {
		this.taxID = taxID;
		querySpecies();
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * �趨taxID������趨����ȫ�µ�taxID����ô�������趨version
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		if (this.taxID == taxID) {
			return;
		}
		this.taxID = taxID;
		querySpecies();
		this.version = lsVersion.get(0)[0];
	}
	/**
	 * �趨�汾�ţ��趨֮ǰ������趨taxID����������ڸð汾�ţ���ֱ�ӷ���
	 * @param version
	 */
	public void setVersion(String version) {
		if (!hashVersion2Species.containsKey(version)) {
			return;
		}
		this.version = version;
	}
	/**
	 * ������ݿ��и����ֵ����а汾
	 * ��������
	 * @return
	 */
	public ArrayList<String> getVersion() {
		ArrayList<String> lsVersionOut = new ArrayList<String>();
		for (String[] string : lsVersion) {
			lsVersionOut.add(string[0] + "_year_" +string[1]);
		}
		return lsVersionOut;
	}
	/**
	 * ������ݿ��и����ֵ����а汾
	 * �������У���Ҫ����gui�е�ѡ��
	 * @return
	 */
	public HashMap<String, String> getMapVersion() {
		HashMap<String, String> mapVersion = new HashMap<String, String>();
		for (String[] string : lsVersion) {
			mapVersion.put(string[0] + "_year_" +string[1], string[0]);
		}
		return mapVersion;
	}
	/**
	 * ��ø����ֵ���Ϣ
	 */
	private void querySpecies() {
		taxInfo = servTaxID.queryTaxInfo(taxID);
		ArrayList<SpeciesFile> lsSpeciesFile = servSpeciesFile.queryLsSpeciesFile(taxID, null);
		for (SpeciesFile speciesFile : lsSpeciesFile) {
			lsVersion.add(new String[]{speciesFile.getVersion(), speciesFile.getPublishYear() + ""});
			hashVersion2Species.put(speciesFile.getVersion().toLowerCase(), speciesFile);
		}
		//����Ӵ�С����
		Collections.sort(lsVersion, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer o1int = Integer.parseInt(o1[1]);
				Integer o2int = Integer.parseInt(o2[1]);
				return -o1int.compareTo(o2int);
			}
		});
	}
	/** ������ */
	public String getCommonName() {
		return taxInfo.getComName();
	}
	/** ������ */
	public String getAbbrName() {
		return taxInfo.getAbbr();
	}
	/**
	 * @return
	 * key: chrID Сд
	 * value�� length
	 */
	public HashMap<String, Long> getMapChromInfo() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMapChromInfo();
	}
	/** Ⱦɫ��ȫ������ */
	public long getChromLenAll() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		HashMap<String, Long> hashChrID2Len = speciesFile.getMapChromInfo();
		ArrayList<Long> lsChrLen = ArrayOperate.getArrayListValue(hashChrID2Len);
		Long len = 0L;
		for (Long chrLen : lsChrLen) {
			len = len + chrLen;
		}
		return len;
	}
	/**
	 * ���chr�ļ�
	 * @return
	 * 0: regex 1: path
	 */
	public String[] getChrRegxAndPath() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getChromFaPath();
	}
	/**
	 * ָ��version����type�����ض�Ӧ��gff�ļ���û���򷵻�null
	 * @param Type
	 */
	public String getGffFile(GFFtype gffType) {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffFile(gffType);
	}
	/**
	 * ָ��version����type�����ض�Ӧ��gff�ļ���û���򷵻�null��
	 * �Զ�ѡ�������ȵ�gfftype��
	 * ���ȼ���GFFtype������
	 * @param Type
	 * @return string[2] 0: gffType 1:gffFilePath
	 */
	public String[] getGffFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffFile();
	}
	/**
	 * ����UCSC��gffRepeat
	 * @param version
	 * @return
	 */
	public String getGffRepeat() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffRepeatFile();
	}
	/** ��ñ�����ָ��version��miRNAǰ������ */
	public String getMiRNAhairpinFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMiRNAhairpinFile();
	}
	/** ��ñ�����ָ��version��miRNA���� */
	public String getMiRNAmatureFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMiRNAmatureFile();
	}
	/** ��ñ�����ָ��version��rfam���� */
	public String getRfamFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRfamFile();
	}
	/** ��ñ�����ָ��version��refseq��ncRNA���� */
	public String getRefseqNCfile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRefseqNCfile();
	}
	/** ��ñ�����ָ��version��refseq������ */
	public String getRefseqFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRefseqFile();
	}
	/** ָ��mapping���������ø��������Ӧ�������ļ� */
	public String getIndexChr(SoftWare softMapping) {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getIndexChromFa(softMapping);
	}
	////////////////////////    ����   //////////////////////////////////////////////////////////////////////////////////////
	/** ����taxinfo���ı� */
	public void setUpdateTaxInfo(String taxInfoFile) {
		this.updateTaxInfoFile = taxInfoFile;
	}
	public void setUpdateSpeciesFile(String speciesFile) {
		this.updateSpeciesFile = speciesFile;
	}
	/** �Զ������� */
	public void update() {
		if (FileOperate.isFileExistAndBigThanSize(updateTaxInfoFile, 0.05))
			updateTaxInfo(updateTaxInfoFile);
		
		if (FileOperate.isFileExistAndBigThanSize(updateSpeciesFile, 0.05))
			updateSpeciesFile(updateSpeciesFile);
	}
	/**
	 * ��������Ϣ�������ݿ�
	 * @param txtFile 	 ������Ϣ����һ�У�item����
	 */
	private void updateTaxInfo(String txtFile) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 0);
		String[] title = lsInfo.get(0);
		HashMap<String, Integer> hashName2ColNum = new HashMap<String, Integer>();
		for (int i = 0; i < title.length; i++) {
			hashName2ColNum.put(title[i].trim().toLowerCase(), i);
		}
		
		for (int i = 1; i < lsInfo.size()-1; i++) {
			TaxInfo taxInfo = new TaxInfo();
			String[] info = lsInfo.get(i);
			int m = hashName2ColNum.get("taxid");
			taxInfo.setTaxID(Integer.parseInt(info[m]));
			
			m = hashName2ColNum.get("chinesename");
			taxInfo.setChnName(info[m]);
			
			m = hashName2ColNum.get("latinname");
			taxInfo.setLatin(info[m]);
			
			m = hashName2ColNum.get("commonname");
			taxInfo.setComName(info[m]);
			
			m = hashName2ColNum.get("abbreviation");
			taxInfo.setAbbr(info[m]);
			//����
			taxInfo.update();
		}
	}
	/**
	 * ��������Ϣ�������ݿ�
	 * @param txtFile 	 ������Ϣ����һ�У�item����
	 */
	private void updateSpeciesFile(String speciesFileInput) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(speciesFileInput, 0);
		String[] title = lsInfo.get(0);
		HashMap<String, Integer> hashName2ColNum = new HashMap<String, Integer>();
		for (int i = 0; i < title.length; i++) {
			hashName2ColNum.put(title[i].trim().toLowerCase(), i);
		}
		
		for (int i = 1; i < lsInfo.size(); i++) {
			SpeciesFile speciesFile = new SpeciesFile();
			String[] info = lsInfo.get(i);
			info = ArrayOperate.copyArray(info, title.length);
			int m = hashName2ColNum.get("taxid");
			speciesFile.setTaxID((int)Double.parseDouble(info[m]));
			
			m = hashName2ColNum.get("version");
			speciesFile.setVersion(info[m]);
			
			m = hashName2ColNum.get("publishyear");
			speciesFile.setPublishYear((int)Double.parseDouble(info[m]));
			
			m = hashName2ColNum.get("chrompath");
			speciesFile.setChromPath(info[m]);
			
			m = hashName2ColNum.get("chromseq");
			speciesFile.setChromSeq(info[m]);
			
			m = hashName2ColNum.get("indexchr");
			speciesFile.setIndexSeq(info[m]);
			
			m = hashName2ColNum.get("gffgenefile");
			speciesFile.setGffGeneFile(info[m]);
			
			m = hashName2ColNum.get("gffrepeatfile");
			speciesFile.setGffRepeatFile(info[m]);
			
			m = hashName2ColNum.get("rfamfile");
			speciesFile.setRfamFile(info[m]);
			
			m = hashName2ColNum.get("refseqfile");
			speciesFile.setRefseqFile(info[m]);
			
			m = hashName2ColNum.get("refseqncfile");
			speciesFile.setRefseqNCfile(info[m]);
			
			m = hashName2ColNum.get("mirnafile");
			speciesFile.setMiRNAfile(info[m]);
			
			m = hashName2ColNum.get("mirnahairpinfile");
			speciesFile.setMiRNAhairpinFile(info[m]);
			
			speciesFile.getHashChrID2ChrLen();
			//����
			speciesFile.update();
		}
	}
	
	/**
	 * ���س�������taxID
	 * @param allID true����ȫ��ID�� false���س���ID--Ҳ��������д��ID
	 * @return
	 */
	@Deprecated
	public static HashMap<String, Integer> getSpeciesNameTaxID(boolean allID) {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getSpeciesNameTaxID(allID);
	}
	/**
	 * ���س�������taxID
	 * @param speciesType ���ݲ�ͬ��
	 * @return
	 */
	public static HashMap<String, Species> getSpeciesName2Species(int speciesType) {
		HashMap<String, Species> mapSpeciesName2Species = new HashMap<String, Species>();
		ServTaxID servTaxID = new ServTaxID();
		ServSpeciesFile servSpeciesFile = new ServSpeciesFile();
		ArrayList<Integer> lsTaxID = servTaxID.getLsAllTaxID();
		for (Integer taxID : lsTaxID) {
			Species species = new Species(taxID);
			if (speciesType == ALL_SPECIES) {
			}
			else if (speciesType == KEGGNAME_SPECIES) {
				if (species.getAbbrName().equals("")) {
					continue;
				}
			}
			else if (speciesType == SEQINFO_SPECIES) {
				ArrayList<SpeciesFile> lsSpeciesFiles = servSpeciesFile.queryLsSpeciesFile(taxID, null);
				if (lsSpeciesFiles.size() == 0) {
					continue;
				}
			}
			mapSpeciesName2Species.put(species.getCommonName(), species);
		}
		return mapSpeciesName2Species;
	}
	/**
	 * �������ֵĳ����������Ұ�����ĸ���򣨺��Դ�Сд��
	 * �������getSpeciesNameTaxID���������taxID
	 * @param allID true����ȫ��ID�� false���س���ID--Ҳ��������д��ID
	 * @return
	 */
	@Deprecated
	public static ArrayList<String> getSpeciesName(boolean allID) {
		ArrayList<String> lsResult = new ArrayList<String>();
		ServTaxID servTaxID = new ServTaxID();
		HashMap<String, Integer> hashSpecies = servTaxID.getSpeciesNameTaxID(allID);
		for (String name : hashSpecies.keySet()) {
			if (name != null && !name.equals("")) {
				lsResult.add(name);
			}
		}
		Collections.sort(lsResult, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		return lsResult;
	}
	/**
	 * ����taxID�Գ�����
	 * @return
	 */
	@Deprecated
	public static HashMap<Integer,String> getSpeciesTaxIDName() {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getHashTaxIDName();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		Species otherObj = (Species)obj;
		if (
				getTaxID() == otherObj.getTaxID() 
				&& version.equals(otherObj.version)
				)
		{
			return true;
		}
		return false;
	}
}
