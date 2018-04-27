package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.dao.cosmic.RepoCompleteExport;
import com.novelbio.database.model.cosmic.CompleteExport;
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
	public void save(List<CompleteExport> lsCompleteExport) {
		repoCompleteExport.save(lsCompleteExport);
	}
	public CompleteExport findCompleteExportByCosmicId(int cosmicId) {
		return repoCompleteExport.findCompleteExportByCosmicId(cosmicId);
	}
	public CompleteExport findCompleteExportByPosAndVar(String chr, long pos, String ref, String alt) {
		return repoCompleteExport.findCompleteExportByPosAndVar(chr, pos, ref, alt);
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
