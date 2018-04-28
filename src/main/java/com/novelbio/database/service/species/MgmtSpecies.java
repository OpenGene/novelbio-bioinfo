package com.novelbio.database.service.species;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.novelbio.base.StringOperate;
import com.novelbio.database.dao.kegg.RepoKRelation;
import com.novelbio.database.dao.species.RepoSpeciesGff;
import com.novelbio.database.dao.species.RepoSpeciesVersion;
import com.novelbio.database.dao.species.RepoTaxInfo;
import com.novelbio.database.model.species.SpeciesGff;
import com.novelbio.database.model.species.SpeciesVersion;
import com.novelbio.database.model.species.TaxInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtSpecies {
	@Autowired
	private RepoSpeciesVersion repoSpeciesVersion;
	@Autowired
	private RepoSpeciesGff repoSpeciesGff;
	@Autowired
	private RepoTaxInfo repoTaxInfo;
	
	private MgmtSpecies() {
		repoSpeciesVersion = SpringFactoryBioinfo.getFactory().getBean(RepoSpeciesVersion.class);
		repoSpeciesGff = SpringFactoryBioinfo.getFactory().getBean(RepoSpeciesGff.class);
		repoTaxInfo = SpringFactoryBioinfo.getFactory().getBean(RepoTaxInfo.class);
	}
	
	public Page<TaxInfo> queryLsTaxInfo(Pageable pageable) {
		return repoTaxInfo.findAll(pageable);
	}
	
	public TaxInfo queryTaxInfo(int taxId) {
		return repoTaxInfo.findByTaxID(taxId);
	}

	public List<TaxInfo> queryAbbr(String abbr) {
		return repoTaxInfo.findByAbbr(abbr);
	}

	public List<SpeciesVersion> querySpeciesVersionFromTaxId(int taxId) {
		return repoSpeciesVersion.findByTaxId(taxId);
	}
	
	public List<SpeciesGff> querySpecieGff(int taxId, String version) {
		return repoSpeciesGff.findByTaxIdAndVersion(taxId, version);
	}
	public SpeciesGff querySpecieGff(int taxId, String version, String gffDb) {
		List<SpeciesGff> lsSpeciesGffs = querySpecieGff(taxId, version);
		for (SpeciesGff speciesGff : lsSpeciesGffs) {
			if (StringOperate.isEqual(gffDb, speciesGff.getGffdb())) {
				return speciesGff;
			}
		}
		return null;
	}
	
	public void saveTaxInfo(TaxInfo taxInfo) {
		if (taxInfo.getTaxID() == 0) {
			return;
		}
		repoTaxInfo.save(taxInfo);
	}
	
	static class ManageHolder {
		static MgmtSpecies instance = new MgmtSpecies();
	}
	
	public static MgmtSpecies getInstance() {
		return ManageHolder.instance;
	}

}
