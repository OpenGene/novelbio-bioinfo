package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.mongorepo.cosmic.RepoCodingMuts;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCodingMuts {

	private MgmtCodingMuts() {}
	
	RepoCodingMuts repoCodingMuts = (RepoCodingMuts)SpringFactoryBioinfo.getBean("repoCodingMuts");
	public List<CodingMuts> findCodingMutsByGeneId(int geneId) {
		return repoCodingMuts.findCodingMutsByGeneId(geneId);
	}
	public CodingMuts findCancerGeneByCosmicId(int cosmicId) {
		return repoCodingMuts.findCodingMutsByCosmicId(cosmicId);
	}
	public List<CodingMuts> findCodingMutsByPosAndVar(String chr, Long pos, String alt) {
		return repoCodingMuts.findCodingMutsByPosAndVar(chr, pos, alt);
	}
	public List<CodingMuts> findAll() {
		return repoCodingMuts.findAll();
	}
	public void save(CodingMuts codingMuts) {
		repoCodingMuts.save(codingMuts);
	}


	//懒汉模式的单例延迟
	static class MgmtCodingMutsHolder {
		static MgmtCodingMuts mgmtCodingMuts = new MgmtCodingMuts();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtCodingMuts getInstance() {
		return MgmtCodingMutsHolder.mgmtCodingMuts;
	}
}
