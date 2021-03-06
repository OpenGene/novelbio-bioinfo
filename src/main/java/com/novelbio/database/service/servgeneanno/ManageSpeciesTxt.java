package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.model.species.TaxInfo;
import com.novelbio.generalconf.PathDetailNBC;

public class ManageSpeciesTxt implements IManageSpecies {
	private static final Logger logger = Logger.getLogger(ManageSpeciesTxt.class);
	
	/** version 必须为小写 */
	static LinkedHashMap<Integer, LinkedHashMap<String, SpeciesFile>> mapTaxID_2_version2SpeciesFile;
	
	private ManageSpeciesTxt() {
		if (mapTaxID_2_version2SpeciesFile == null) {
			 mapTaxID_2_version2SpeciesFile = new LinkedHashMap<>();
			 readDBinfo();
		}
	}
	
	private void readDBinfo() {
		String speciesFile = PathDetailNBC.getSpeciesFile();
		if (FileOperate.isFileExistAndBigThanSize(speciesFile, 0)) {
			readSpeciesFile(speciesFile);
		} else {
			logger.error("文本无法读取:" + speciesFile);
		}
	}
		
	public void readSpeciesFile(String speciesFileInput) {
		if (!FileOperate.isFileExistAndBigThanSize(speciesFileInput, 0)) return;
		
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(speciesFileInput, 0);
		String[] title = null;
		for (String[] strings : lsInfo) {
			if (strings[0].trim().startsWith("#title")) {
				strings[0] = strings[0].trim().replace("#title_", "");
				title = strings;
				break;
			}
		}
		if (title == null) return;
		
		HashMap<String, Integer> hashName2ColNum = new HashMap<String, Integer>();
		for (int i = 0; i < title.length; i++) {
			hashName2ColNum.put(title[i].trim().toLowerCase(), i);
		}
		title[0] = "#" + title[0];//下面就可以把title忽略
		 
		for (int i = 0; i < lsInfo.size(); i++) {
			if (lsInfo.get(i)[0].startsWith("#")) continue;
			
			SpeciesFile speciesFile = new SpeciesFile();
			String[] info = lsInfo.get(i);
			info = ArrayOperate.copyArray(info, title.length);

			
			Integer m = hashName2ColNum.get("name");
			if (m != null) speciesFile.setSpeciesName(info[m]);
			
			m = hashName2ColNum.get("taxid");
			if (m != null) {
				try {
					speciesFile.setTaxID((int)Double.parseDouble(info[m]));
				} catch (Exception e) {
					speciesFile.setTaxID(speciesFile.getSpeciesName().hashCode());
				}
			} else {
				speciesFile.setTaxID(speciesFile.getSpeciesName().hashCode());
			}
			
			m = hashName2ColNum.get("version");
			if (m != null) speciesFile.setVersion(info[m]);
			
			m = hashName2ColNum.get("publishyear");
			if (m != null) speciesFile.setPublishYear((int)Double.parseDouble(info[m]));
			
			m = hashName2ColNum.get("chromseq");
			if (m != null) speciesFile.setChromSeq(info[m]);
						
			m = hashName2ColNum.get("gffgenefile");
			if (m != null && !info[m].equals("")) {
				String[] gffUnit = info[m].split(SepSign.SEP_ID);
				for (String gffInfo : gffUnit) {
					String[] gffDB2TypeFile = gffInfo.split(SepSign.SEP_INFO);
					speciesFile.addGffDB2TypeFile(gffDB2TypeFile[0], GffType.getType(gffDB2TypeFile[1]), gffDB2TypeFile[2]);
				}
			}
			
			m = hashName2ColNum.get("gffrepeatfile");
			if (m != null) speciesFile.setGffRepeatFile(info[m]);
			
			m = hashName2ColNum.get("refseq_all_iso");
			if (m != null) speciesFile.setRefSeqFileName(FileOperate.getFileName(info[m]), true, false);

			
			m = hashName2ColNum.get("refseq_one_iso");
			if (m != null) speciesFile.setRefSeqFileName(FileOperate.getFileName(info[m]), false, false);
			
			m = hashName2ColNum.get("refseqncfile");
			if (m != null) speciesFile.setRefseqNCfile(info[m]);
//			try {
//				speciesFile.getMapChromInfo();
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.error("条目出错：" + ArrayOperate.cmbString(info, "\t"));
//			}
			//升级
			saveSpeciesFile(speciesFile);
		}
	
	}
	
	/** 返回所有有基因组的物种 */
	public List<Integer> getLsNameNotInDB() {
//		readDBinfo();
		return new ArrayList<Integer>(mapTaxID_2_version2SpeciesFile.keySet());
	}
	
	/**
	 * @param taxID 必须选项，没这个就不用选了
	 * @param version 必须选，主要是hg19等等类似，不过我估计也用不到 <b> Version大小写敏感</b>
	 * @return 没有的话则返回size==0的list
	 */
	public SpeciesFile querySpeciesFile(int taxID, String version) {
		if (taxID <= 0) {
			return null;
		}
		SpeciesFile speciesFile = null;
		Map<String, SpeciesFile> mapVersion2SpeciesFile = mapTaxID_2_version2SpeciesFile.get(taxID);
		if (mapVersion2SpeciesFile != null) {
			speciesFile = mapVersion2SpeciesFile.get(version.toLowerCase());
		}
		return speciesFile;
	}
	
	/**
	 * @param taxID 必须选项，没这个就不用选了
	 * @param version 可选，主要是hg19等等类似，不过我估计也用不到
	 * @return 没有的话则返回size==0的list
	 */
	public List<SpeciesFile> queryLsSpeciesFile(int taxID) {
		if (taxID <= 0) {
			return new ArrayList<SpeciesFile>();
		}
		Map<String, SpeciesFile> mapVersion2SpeciesFile = mapTaxID_2_version2SpeciesFile.get(taxID);
		if (mapVersion2SpeciesFile != null) {
			return new ArrayList<SpeciesFile>(mapVersion2SpeciesFile.values());
		}
		return new ArrayList<SpeciesFile>();
	}
	
	/**
	 * Version大小写敏感
	 * 没有就插入，有就覆盖
	 * @param taxInfo
	 */
	public void saveSpeciesFile(SpeciesFile speciesFile) {
		if (speciesFile.getTaxID() == 0) {
			return;
		}
		SpeciesFile speciesFileS = querySpeciesFile(speciesFile.getTaxID(), speciesFile.getVersion());
		if (speciesFileS == null) {
			saveToMap(speciesFile);
			return;
		}
		
		if (!speciesFile.equalsDeep(speciesFileS)) {
			speciesFile.setId(speciesFileS.getId());
			saveToMap(speciesFile);
		}
	}
	
	private void saveToMap(SpeciesFile speciesFileS) {
		LinkedHashMap<String, SpeciesFile> mapVersion2Species = mapTaxID_2_version2SpeciesFile.get(speciesFileS.getTaxID());
		if (mapVersion2Species == null) {
			mapVersion2Species = new LinkedHashMap<>();
			mapTaxID_2_version2SpeciesFile.put(speciesFileS.getTaxID(), mapVersion2Species);
		}
		mapVersion2Species.put(speciesFileS.getVersion().toLowerCase(), speciesFileS);
	}
	/**
	 * @param taxID 0 则返回null
	 * @return
	 */
	public TaxInfo queryTaxInfo(int taxID) {
		return null;
	}
	/**
	 * @param taxIDfile 0 则返回null
	 * @return
	 */
	public TaxInfo queryAbbr(String abbr) {
		return null;
	}
	/**
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void saveTaxInfo(TaxInfo taxInfo) {
	}

	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public Map< Integer,String> getMapTaxIDName() {
		return null;
	}
	
	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public List<TaxInfo> getLsAllTaxID() {
		return new ArrayList<>();
	}

	public Page<TaxInfo> queryLsTaxInfo(Pageable pageable) {
		return null;
	}
	
	@Override
	public Page<TaxInfo> queryLsTaxInfoByFilter(Pageable pageable, String keyText) {
		return null;
	}
	
	static class ManageSpeciesTxtHold {
		protected static ManageSpeciesTxt manageSpecies = new ManageSpeciesTxt();
	}
	
	@Override
	public boolean deleteByTaxId(int taxid) {		
		return true;
	}

	@Override
	public void deleteSpeciesFile(String speciesFileId) {		
	}

	@Override
	public SpeciesFile findOne(String speciesFileId) {
		return null;
	}

	@Override
	public boolean isHaveMiRNArecalculate(TaxInfo taxInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRrnaFileWithPath(TaxInfo taxInfo) {
		// TODO Auto-generated method stub
		return null;
	}
}
