package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.mongorepo.kegg.RepoKIDKeg2Ko;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKIDKeg2Ko {

	RepoKIDKeg2Ko mapKIDKeg2Ko;
	public ServKIDKeg2Ko() {
		mapKIDKeg2Ko = (RepoKIDKeg2Ko)SpringFactoryBioinfo.getFactory().getBean("repoKIDKeg2Ko");
	}

	public List<KGIDkeg2Ko> findLsByKegId(String keggID) {
		return mapKIDKeg2Ko.findyLsByKegId(keggID);
	}
	
	public List<KGIDkeg2Ko> findLsByKegIdAndTaxId(String kegID, int taxID) {
		return mapKIDKeg2Ko.findyLsByKegIdAndTaxId(kegID, taxID);
	}
	
	public KGIDkeg2Ko findByKegIdAndKo(String kegID, String Ko) {
		return mapKIDKeg2Ko.findByKegIdAndKo(kegID, Ko);
	}
	
	public List<KGIDkeg2Ko> findLsByKoAndTaxId(String KoId, int taxID) {
		return mapKIDKeg2Ko.findyLsByKoAndTaxId(KoId, taxID);
	}
	
	public void save(KGIDkeg2Ko kgDkeg2Ko) {
		mapKIDKeg2Ko.save(kgDkeg2Ko);
	}
	
	public void deleteAll() {
		mapKIDKeg2Ko.deleteAll();
	}
	
	static class ManageHolder {
		static ServKIDKeg2Ko instance = new ServKIDKeg2Ko();
	}
	
	public static ServKIDKeg2Ko getInstance() {
		return ManageHolder.instance;
	}




	
}
