package com.novelbio.database.service.servgeneanno;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.mongorepo.geneanno.RepoSpeciesFile;
import com.novelbio.database.service.SpringFactory;

public class ManageSpeciesFile {
	private static final Logger logger = Logger.getLogger(ManageSpeciesFile.class);
	
	@Autowired
	private RepoSpeciesFile repoSpeciesFile;
	public ManageSpeciesFile() {
		repoSpeciesFile = (RepoSpeciesFile)SpringFactory.getFactory().getBean("repoSpeciesFile");
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
			return null;
		}
		return repoSpeciesFile.findByTaxID(taxID);
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
			repoSpeciesFile.save(speciesFile);
			return;
		}
		
		if (!speciesFile.equalsDeep(speciesFileS)) {
			speciesFile.setId(speciesFileS.getId());
			repoSpeciesFile.save(speciesFile);
		}
	}
	
}
