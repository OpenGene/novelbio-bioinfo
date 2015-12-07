package com.novelbio.database.model.modcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.cosmic.CompleteExport;
import com.novelbio.database.model.modcosmic.MgmtCodingMuts.MgmtCodingMutsHolder;
import com.novelbio.database.mongorepo.cosmic.RepoCodingMuts;
import com.novelbio.database.mongorepo.cosmic.RepoCompleteExport;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCompleteExport {
	private MgmtCompleteExport() {}
	RepoCompleteExport repoCompleteExport = (RepoCompleteExport) SpringFactoryBioinfo.getBean("repoCompleteExport");
	public List<CompleteExport> findCompleteExportsByGeneId(int geneId) {
		return repoCompleteExport.findCompleteExportByGeneId(geneId);
	}
	

	public List<CompleteExport> findAll() {
		return repoCompleteExport.findAll();
	}
	public void save(CompleteExport completeExport) {
		repoCompleteExport.save(completeExport);
	}
	public CompleteExport findCompleteExportByCosmicId(int cosmicId) {
		return repoCompleteExport.findCompleteExportByCosmicId(cosmicId);
	}

	//懒汉模式的单例延迟
	static class MgmtCompleteExportHolder {
		static MgmtCompleteExport mgmtCompleteExport = new MgmtCompleteExport();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtCompleteExport getInstance() {
		return MgmtCompleteExportHolder.mgmtCompleteExport;
	}
	
	
	
	
}
