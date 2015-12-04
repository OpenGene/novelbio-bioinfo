package com.novelbio.database.model.modcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.model.modcosmic.MgmtCancerGene.MgmtCancerGeneHolder;
import com.novelbio.database.mongorepo.cosmic.RepoCodingMuts;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmCodingMuts {

	private MgmCodingMuts() {}
	
	RepoCodingMuts repoCodingMuts = (RepoCodingMuts)SpringFactoryBioinfo.getBean("repoCodingMuts");
	public List<CodingMuts> findCodingMutsByGeneId(int geneId) {
		return repoCodingMuts.findCodingMutsByGeneId(geneId);
	}
	public List<CodingMuts> findAll() {
		return repoCodingMuts.findAll();
	}
	public void save(CodingMuts codingMuts) {
		repoCodingMuts.save(codingMuts);
	}
	public CodingMuts findCancerGeneByGeneId(int cosmicId) {
		return repoCodingMuts.findCodingMutsByCosmicId(cosmicId);
	}

	//懒汉模式的单例延迟
	static class MgmtCodingMutsHolder {
		static MgmCodingMuts mgmCodingMuts = new MgmCodingMuts();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmCodingMuts getInstance() {
		return MgmtCodingMutsHolder.mgmCodingMuts;
	}
}
