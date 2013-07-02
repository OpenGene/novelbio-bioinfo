package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.generalConf.PathNBCDetail;

public class ManageSpeciesFile {
	private static final Logger logger = Logger.getLogger(ManageSpeciesFile.class);
	/**
	 * version 必须为小写
	 */
	static Map<Integer, Map<String, SpeciesFile>> mapTaxID_2_version2SpeciesFile;
	
	public ManageSpeciesFile() {
		if (mapTaxID_2_version2SpeciesFile == null) {
			 mapTaxID_2_version2SpeciesFile = new HashMap<Integer, Map<String,SpeciesFile>>();
			readSpeciesFile(PathNBCDetail.getSpeciesFile());
		}
	}
	
	private void readSpeciesFile(String speciesFileInput) {
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
			String[] chromInfo = info[m].split(SepSign.SEP_ID);
			speciesFile.setChromPath(chromInfo[0], chromInfo[1]);
			
			m = hashName2ColNum.get("chromseq");
			speciesFile.setChromSeq(info[m]);
			//TODO 看下分隔符对不对
			m = hashName2ColNum.get("indexchr");
			if (!info[m].equals("")) {
				String[] indexChrInfo = info[m].split(SepSign.SEP_ID);
				for (String indexChrDetail : indexChrInfo) {
					String[] indexDetail = indexChrDetail.split(SepSign.SEP_INFO);
					speciesFile.addIndexChrom(SoftWare.valueOf(indexDetail[0]), indexDetail[1]);
				}
			}
						
			m = hashName2ColNum.get("gffgenefile");
			if (!info[m].equals("")) {
				String[] gffUnit = info[m].split(SepSign.SEP_ID);
				for (String gffInfo : gffUnit) {
					String[] gffDB2TypeFile = gffInfo.split(SepSign.SEP_INFO);
					speciesFile.addGffDB2TypeFile(gffDB2TypeFile[0], GffType.getType(gffDB2TypeFile[1]), gffDB2TypeFile[2]);
				}
			}
			
			m = hashName2ColNum.get("gffrepeatfile");
			speciesFile.setGffRepeatFile(info[m]);
			
			m = hashName2ColNum.get("refseqfile");
			speciesFile.setRefseqFile(info[m]);
			
			m = hashName2ColNum.get("refseqncfile");
			speciesFile.setRefseqNCfile(info[m]);
			try {
				speciesFile.getMapChromInfo();
			} catch (Exception e) {
				logger.error("条目出错：" + ArrayOperate.cmbString(info, "\t"));
			}
		
			//升级
			speciesFile.update();
		}
	}
	
	/** 返回所有有基因组的物种 */
	public List<Integer> getLsTaxID() {
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
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void update(SpeciesFile speciesFile) {
		if (speciesFile.getTaxID() == 0) {
			return;
		}
		SpeciesFile speciesFileS = querySpeciesFile(speciesFile.getTaxID(), speciesFile.getVersion());
		if (speciesFileS == null) {
			save(speciesFile);
			return;
		}
		
		if (!speciesFile.equalsDeep(speciesFileS)) {
			speciesFile.setId(speciesFileS.getId());
			save(speciesFile);
		}
	}
	
	private void save(SpeciesFile speciesFileS) {
		Map<String, SpeciesFile> mapVersion2Species = mapTaxID_2_version2SpeciesFile.get(speciesFileS.getTaxID());
		if (mapVersion2Species == null) {
			mapVersion2Species = new HashMap<String, SpeciesFile>();
			mapTaxID_2_version2SpeciesFile.put(speciesFileS.getTaxID(), mapVersion2Species);
		}
		mapVersion2Species.put(speciesFileS.getVersion().toLowerCase(), speciesFileS);
	}
	
}
