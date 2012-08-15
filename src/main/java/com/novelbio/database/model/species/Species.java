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
 * 物种信息，包括名字，以及各个文件所在路径
 * @author zong0jie
 */
public class Species {
	/** 全部物种 */
	public static final int ALL_SPECIES = 10;
	/** 有Kegg缩写名的物种 */
	public static final int KEGGNAME_SPECIES = 20;
	/** 有数据库序列等信息的物种 */
	public static final int SEQINFO_SPECIES = 30;
	
	int taxID = 0;
	TaxInfo taxInfo = new TaxInfo();
	String version = "";;
	/** 有哪些版本,0：version 1：year<br>
	 * 按照年代从大到小排序
	 */
	ArrayList<String[]> lsVersion = new ArrayList<String[]>();
	/** key：版本ID,通通小写  value：具体的信息 */
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
	 * 设定taxID，如果设定的是全新的taxID，那么会重新设定version
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
	 * 设定版本号，设定之前务必先设定taxID。如果不存在该版本号，则直接返回
	 * @param version
	 */
	public void setVersion(String version) {
		if (!hashVersion2Species.containsKey(version)) {
			return;
		}
		this.version = version;
	}
	/**
	 * 获得数据库中该物种的所有版本
	 * 倒序排列
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
	 * 获得数据库中该物种的所有版本
	 * 倒序排列，主要用于gui中的选框
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
	 * 获得该物种的信息
	 */
	private void querySpecies() {
		taxInfo = servTaxID.queryTaxInfo(taxID);
		ArrayList<SpeciesFile> lsSpeciesFile = servSpeciesFile.queryLsSpeciesFile(taxID, null);
		for (SpeciesFile speciesFile : lsSpeciesFile) {
			lsVersion.add(new String[]{speciesFile.getVersion(), speciesFile.getPublishYear() + ""});
			hashVersion2Species.put(speciesFile.getVersion().toLowerCase(), speciesFile);
		}
		//年代从大到小排序
		Collections.sort(lsVersion, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer o1int = Integer.parseInt(o1[1]);
				Integer o2int = Integer.parseInt(o2[1]);
				return -o1int.compareTo(o2int);
			}
		});
	}
	/** 常用名 */
	public String getCommonName() {
		return taxInfo.getComName();
	}
	/** 常用名 */
	public String getAbbrName() {
		return taxInfo.getAbbr();
	}
	/**
	 * @return
	 * key: chrID 小写
	 * value： length
	 */
	public HashMap<String, Long> getMapChromInfo() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMapChromInfo();
	}
	/** 染色体全长序列 */
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
	 * 获得chr文件
	 * @return
	 * 0: regex 1: path
	 */
	public String[] getChrRegxAndPath() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getChromFaPath();
	}
	/**
	 * 指定version，和type，返回对应的gff文件，没有则返回null
	 * @param Type
	 */
	public String getGffFile(GFFtype gffType) {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffFile(gffType);
	}
	/**
	 * 指定version，和type，返回对应的gff文件，没有则返回null，
	 * 自动选择最优先的gfftype。
	 * 优先级由GFFtype来决定
	 * @param Type
	 * @return string[2] 0: gffType 1:gffFilePath
	 */
	public String[] getGffFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffFile();
	}
	/**
	 * 返回UCSC的gffRepeat
	 * @param version
	 * @return
	 */
	public String getGffRepeat() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getGffRepeatFile();
	}
	/** 获得本物种指定version的miRNA前体序列 */
	public String getMiRNAhairpinFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMiRNAhairpinFile();
	}
	/** 获得本物种指定version的miRNA序列 */
	public String getMiRNAmatureFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getMiRNAmatureFile();
	}
	/** 获得本物中指定version的rfam序列 */
	public String getRfamFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRfamFile();
	}
	/** 获得本物中指定version的refseq的ncRNA序列 */
	public String getRefseqNCfile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRefseqNCfile();
	}
	/** 获得本物中指定version的refseq的序列 */
	public String getRefseqFile() {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getRefseqFile();
	}
	/** 指定mapping的软件，获得该软件所对应的索引文件 */
	public String getIndexChr(SoftWare softMapping) {
		SpeciesFile speciesFile = hashVersion2Species.get(version.toLowerCase());
		return speciesFile.getIndexChromFa(softMapping);
	}
	////////////////////////    升级   //////////////////////////////////////////////////////////////////////////////////////
	/** 输入taxinfo的文本 */
	public void setUpdateTaxInfo(String taxInfoFile) {
		this.updateTaxInfoFile = taxInfoFile;
	}
	public void setUpdateSpeciesFile(String speciesFile) {
		this.updateSpeciesFile = speciesFile;
	}
	/** 自动化升级 */
	public void update() {
		if (FileOperate.isFileExistAndBigThanSize(updateTaxInfoFile, 0.05))
			updateTaxInfo(updateTaxInfoFile);
		
		if (FileOperate.isFileExistAndBigThanSize(updateSpeciesFile, 0.05))
			updateSpeciesFile(updateSpeciesFile);
	}
	/**
	 * 将配置信息导入数据库
	 * @param txtFile 	 配置信息：第一行，item名称
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
			//升级
			taxInfo.update();
		}
	}
	/**
	 * 将配置信息导入数据库
	 * @param txtFile 	 配置信息：第一行，item名称
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
			//升级
			speciesFile.update();
		}
	}
	
	/**
	 * 返回常用名对taxID
	 * @param allID true返回全部ID， false返回常用ID--也就是有缩写的ID
	 * @return
	 */
	@Deprecated
	public static HashMap<String, Integer> getSpeciesNameTaxID(boolean allID) {
		ServTaxID servTaxID = new ServTaxID();
		return servTaxID.getSpeciesNameTaxID(allID);
	}
	/**
	 * 返回常用名对taxID
	 * @param speciesType 根据不同的
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
	 * 返回物种的常用名，并且按照字母排序（忽略大小写）
	 * 可以配合getSpeciesNameTaxID方法来获得taxID
	 * @param allID true返回全部ID， false返回常用ID--也就是有缩写的ID
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
	 * 返回taxID对常用名
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
