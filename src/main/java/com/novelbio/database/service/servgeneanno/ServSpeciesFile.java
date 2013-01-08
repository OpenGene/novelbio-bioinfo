package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mapper.geneanno.MapSpeciesFile;
import com.novelbio.database.service.SpringFactory;

public class ServSpeciesFile implements MapSpeciesFile{
	private static Logger logger = Logger.getLogger(ServSpeciesFile.class);

	@Inject
	private MapSpeciesFile mapSpeciesFile;
	public ServSpeciesFile() {
		mapSpeciesFile = (MapSpeciesFile)SpringFactory.getFactory().getBean("mapSpeciesFile");
	}
	@Override
	public SpeciesFile querySpeciesFile(SpeciesFile speciesFile) {
		return mapSpeciesFile.querySpeciesFile(speciesFile);
	}
	@Override
	public ArrayList<SpeciesFile> queryLsSpeciesFile(SpeciesFile speciesFile) {
		ArrayList<SpeciesFile> lsResult = mapSpeciesFile.queryLsSpeciesFile(speciesFile);
		if (lsResult == null) {
			lsResult = new ArrayList<SpeciesFile>();
		}
		return lsResult;
	}

	@Override
	public void insertSpeciesFile(SpeciesFile speciesFile) {
		mapSpeciesFile.insertSpeciesFile(speciesFile);
	}

	@Override
	public void updateSpeciesFile(SpeciesFile speciesFile) {
		mapSpeciesFile.updateSpeciesFile(speciesFile);
	}
	/**
	 * @param taxID 必须选项，没这个就不用选了
	 * @param version 可选，主要是hg19等等类似，不过我估计也用不到
	 * @return 没有的话则返回size==0的list
	 */
	public ArrayList<SpeciesFile> queryLsSpeciesFile(int taxID, String version) {
		if (taxID <= 0) {
			return new ArrayList<SpeciesFile>();
		}
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(taxID);
		if (version != null && !version.trim().equals("")) {
			speciesFile.setVersion(version.trim());
		}
		return queryLsSpeciesFile(speciesFile);
	}
	/**
	 * 没有就插入，有就升级
	 * @param taxInfo
	 */
	public void update(SpeciesFile speciesFile) {
		if (speciesFile.getTaxID() == 0) {
			return;
		}
		ArrayList<SpeciesFile> lsSpeciesFileQ = queryLsSpeciesFile(speciesFile);
		if (lsSpeciesFileQ == null || lsSpeciesFileQ.size() == 0) {
			insertSpeciesFile(speciesFile);
		}
		boolean updateFlag = true;
		for (SpeciesFile speciesFile2 : lsSpeciesFileQ) {
			if (speciesFile2.equalsDeep(speciesFile)) {
				updateFlag = false;
			}
		}
		if (updateFlag) {
			updateSpeciesFile(speciesFile);
		}
	}
}
