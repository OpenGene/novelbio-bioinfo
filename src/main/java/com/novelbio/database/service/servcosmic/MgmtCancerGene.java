package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.dao.cosmic.RepoCancerGene;
import com.novelbio.database.model.cosmic.CancerGene;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCancerGene {
	private MgmtCancerGene() {}
	RepoCancerGene repoCancerGene = (RepoCancerGene)SpringFactoryBioinfo.getBean("repoCancerGene");
	public CancerGene findCancerGeneByGeneId(int geneId) {
		return repoCancerGene.findCancerGeneByGeneId(geneId);
	}
	public List<CancerGene> findAll() {
		return repoCancerGene.findAll();
	}
	public void save(CancerGene cancerGene) {
		repoCancerGene.save(cancerGene);
	}
	//懒汉模式的单例延迟
	static class MgmtCancerGeneHolder {
		static MgmtCancerGene mgmtCancerGene = new MgmtCancerGene();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtCancerGene getInstance() {
		return MgmtCancerGeneHolder.mgmtCancerGene;
	}
}
