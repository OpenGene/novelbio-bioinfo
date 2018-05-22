package com.novelbio.database.service.servgeneanno;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.dao.geneanno.RepoSpeciesFile;
import com.novelbio.database.dao.species.RepoTaxInfo;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.model.species.TaxInfo;
import com.novelbio.generalconf.PathDetailNBC;

public class ManageSpeciesDB implements IManageSpecies {
	private static final Logger logger = Logger.getLogger(ManageSpeciesDB.class);

	@Autowired
	private RepoSpeciesFile repoSpeciesFile;
	@Autowired
	private RepoTaxInfo repoTaxInfo;
	@Autowired
	MongoTemplate mongoTemplate;

	private ManageSpeciesDB() {
		//		repoSpeciesFile = (RepoSpeciesFile)SpringFactoryBioinfo.getFactory().getBean(RepoSpeciesFile.class);
		//		repoTaxInfo = (RepoTaxInfo)SpringFactoryBioinfo.getFactory().getBean(RepoTaxInfo.class);
		//		mongoTemplate = (MongoTemplate)SpringFactoryBioinfo.getFactory().getBean(MongoTemplate.class);
	}

	public void readSpeciesFile(String speciesFileInput) {
		if (!FileOperate.isFileExistAndBigThanSize(speciesFileInput, 0))
			return;

		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(speciesFileInput, 0);
		String[] title = null;
		for (String[] strings : lsInfo) {
			if (strings[0].trim().startsWith("#title")) {
				strings[0] = strings[0].trim().replace("#title_", "");
				title = strings;
				break;
			}
		}
		if (title == null)
			return;

		HashMap<String, Integer> hashName2ColNum = new HashMap<String, Integer>();
		for (int i = 0; i < title.length; i++) {
			hashName2ColNum.put(title[i].trim().toLowerCase(), i);
		}
		title[0] = "#" + title[0];//下面就可以把title忽略

		for (int i = 0; i < lsInfo.size(); i++) {
			if (lsInfo.get(i)[0].startsWith("#"))
				continue;

			SpeciesFile speciesFile = new SpeciesFile();
			String[] info = lsInfo.get(i);
			info = ArrayOperate.copyArray(info, title.length);
			int m = hashName2ColNum.get("taxid");
			if (m < info.length) {
				speciesFile.setTaxID((int) Double.parseDouble(info[m]));
			}

			m = hashName2ColNum.get("version");
			if (m < info.length) {
				speciesFile.setVersion(info[m]);
			}

			m = hashName2ColNum.get("publishyear");
			if (m < info.length) {
				speciesFile.setPublishYear((int) Double.parseDouble(info[m]));
			}

			m = hashName2ColNum.get("chromseq");
			if (m < info.length) {
				speciesFile.setChromSeq(FileOperate.getFileName(info[m]));
			}

			m = hashName2ColNum.get("gffgenefile");
			if (m < info.length && !info[m].equals("")) {
				String[] gffUnit = info[m].split(SepSign.SEP_ID);
				for (String gffInfo : gffUnit) {
					String[] gffDB2TypeFile = gffInfo.split(SepSign.SEP_INFO);
					speciesFile.addGffDB2TypeFile(gffDB2TypeFile[0], GffType.getType(gffDB2TypeFile[1]), FileOperate.getFileName(gffDB2TypeFile[2]));
				}
			}

			m = hashName2ColNum.get("gffrepeatfile");
			if (m < info.length) {
				speciesFile.setGffRepeatFile(FileOperate.getFileName(info[m]));
			}

			m = hashName2ColNum.get("refseq_all_iso");
			if (m < info.length) {
				speciesFile.setRefSeqFileName(FileOperate.getFileName(info[m]), true, false);
			}

			m = hashName2ColNum.get("refseq_one_iso");
			if (m < info.length) {
				speciesFile.setRefSeqFileName(FileOperate.getFileName(info[m]), false, false);
			}

			m = hashName2ColNum.get("refseqncfile");
			if (m < info.length) {
				speciesFile.setRefseqNCfile(FileOperate.getFileName(info[m]));
			}
			//升级
			saveSpeciesFile(speciesFile);
		}

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
		return repoSpeciesFile.findByTaxIDAndVersion(taxID, version);
	}

	/**
	 * @param taxID 必须选项，没这个就不用选了
	 * @param version 可选，主要是hg19等等类似，不过我估计也用不到
	 * @return 没有的话则返回size==0的list
	 */
	public List<SpeciesFile> queryLsSpeciesFile(int taxID) {
		if (taxID <= 0) {
			return new ArrayList<>();
		}
		return repoSpeciesFile.findByTaxID(taxID);
	}

	public List<Integer> getLsNameNotInDB() {
		//		List<Integer> lsTaxId = new ArrayList<>();
		//		for (SpeciesFile speciesFile : repoSpeciesFile.findAll()) {
		//			lsTaxId.add(speciesFile.getTaxID());
		//		}
		//		return lsTaxId;
		return new ArrayList<>();
	}

	/**
	 * Version大小写敏感
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void saveSpeciesFile(SpeciesFile speciesFile) {
		if (speciesFile.getTaxID() == 0) {
			return;
		}
		if (speciesFile.getId() != null) {
			repoSpeciesFile.save(speciesFile);
			return;
		}

		SpeciesFile speciesFileS = querySpeciesFile(speciesFile.getTaxID(), speciesFile.getVersion());
		if (speciesFileS == null) {
			repoSpeciesFile.save(speciesFile);
			return;
		}

		if (!speciesFile.equalsDeep(speciesFileS)) {
			speciesFile.setId(speciesFileS.getId());
			repoSpeciesFile.save(speciesFile);
		}
	}

	/** 删除物种 */
	public boolean deleteByTaxId(int taxid) {
		deleteFile(String.valueOf(taxid));

		// 删除数据库物种和版本记录
		mongoTemplate.remove(new Query(Criteria.where("taxID").is(taxid)), TaxInfo.class);
		mongoTemplate.remove(new Query(Criteria.where("taxID").is(taxid)), SpeciesFile.class);

		logger.info("物种" + taxid + "被删除！");
		return true;
	}

	/**
	 * 根据Id删除物种文本内容
	 * @param speciesFileId
	 */
	public void deleteSpeciesFile(String speciesFileId) {
		SpeciesFile speciesFile = repoSpeciesFile.findOne(speciesFileId);
		deleteFile(String.valueOf(String.valueOf(speciesFile.getTaxID()) + FileOperate.getSepPath() + speciesFile.getVersion()));
		repoSpeciesFile.delete(speciesFileId);
		logger.info("物种" + speciesFile.getTaxID() + "的版本" + speciesFile.getVersion() + "被删除！");
	}

	/**
	 * 删除hdfs上物种或版本相关的全部文件
	 * @param midPath 物种或版本相关的中段路径。<br>
	 * 删除物种文件的格式为：taxID  例如：9606<br>
	 * 删除物种某版本文件的格式为：taxID/version  例如：9606/GRCh38<br>
	 */
	private void deleteFile(String midPath) {
		// 删除物种染色体等等相关文件
		List<String> lsSubPaths = Lists.newArrayList("species", "rrna", "rfam", "miRNA", "chrSep", "Chrom_Sep", "BGgene");
		String pathFull = null;
		for (String subPath : lsSubPaths) {
			pathFull = GenomePath + subPath + FileOperate.getSepPath() + midPath + FileOperate.getSepPath();
			if(FileOperate.isFileExist(pathFull)) {
				FileOperate.deleteFileFolder(pathFull);
				logger.info("物种文件目录被删除:" + pathFull);
			}
		}

		// 删除索引文件
		String indexPath = GenomePath + "index" + FileOperate.getSepPath();
		List<Path> lsPaths = FileOperate.getLsFoldPath(indexPath);
		if (null != lsPaths && lsPaths.size() > 0) {
			for (Path path : lsPaths) {
				pathFull = path.toString() + FileOperate.getSepPath() + midPath + FileOperate.getSepPath();
				if(FileOperate.isFileExist(pathFull)) {
					FileOperate.deleteFileFolder(pathFull);
					logger.info("物种索引文件目录被删除:" + pathFull);
				}
			}
		}
	}

	/**
	 * @param taxID 0 则返回null
	 * @return
	 */
	public TaxInfo queryTaxInfo(int taxID) {
		return repoTaxInfo.findByTaxID(taxID);
	}

	/**
	 * @param taxIDfile 0 则返回null
	 * @return
	 */
	public TaxInfo queryAbbr(String abbr) {
		List<TaxInfo> lsTaxInfos = repoTaxInfo.findByAbbr(abbr);
		if (lsTaxInfos.size() == 0) {
			return null;
		}
		return lsTaxInfos.get(0);
	}

	/**
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void saveTaxInfo(TaxInfo taxInfo) {
		if (taxInfo.getTaxID() == 0) {
			return;
		}
		repoTaxInfo.save(taxInfo);
	}

	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public Map<Integer, String> getMapTaxIDName() {
		Map<Integer, String> mapTaxId2CommName = new HashMap<>();
		for (TaxInfo taxInfo : repoTaxInfo.findAll()) {
			mapTaxId2CommName.put(taxInfo.getTaxID(), taxInfo.getComName());
		}
		return mapTaxId2CommName;
	}

	/**
	 * 返回taxID对常用名
	 * @return
	 */
	public List<TaxInfo> getLsAllTaxID() {
		List<TaxInfo> lsTaxInfos = repoTaxInfo.findAll();
		return lsTaxInfos;
	}

	public Page<TaxInfo> queryLsTaxInfo(Pageable pageable) {
		return repoTaxInfo.findAll(pageable);
	}

	@Override
	public Page<TaxInfo> queryLsTaxInfoByFilter(Pageable pageable, String keyText) {
		return repoTaxInfo.findByFilter(pageable, keyText);
	}

	static class ManageSpeciesDBHold {
		protected static ManageSpeciesDB manageSpecies = new ManageSpeciesDB();
	}

	@Override
	public SpeciesFile findOne(String speciesFileId) {
		return repoSpeciesFile.findOne(speciesFileId);
	}

	public boolean isHaveMiRNArecalculate(TaxInfo taxInfo) {
		if (taxInfo.isHaveMiRNA() == null || !taxInfo.isHaveMiRNA()) {
			taxInfo.setIsHaveMiRNA(ListMiRNAdat.isContainMiRNA(taxInfo.getLatinName_2Word(), PathDetailNBC.getMiRNADat()));
			saveTaxInfo(taxInfo);
		}
		return taxInfo.isHaveMiRNA();
	}

	/** 获取核糖体rna所在的路径，绝对路径 */
	public String getRrnaFileWithPath(TaxInfo taxInfo) {
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(taxInfo.getTaxID());
		String savePath = EnumSpeciesFile.rrnaFile.getSavePath(taxInfo.getTaxID(), null);
		return savePath + taxInfo.getRrnaFile();
	}

}
