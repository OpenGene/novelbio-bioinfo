package com.novelbio.database.service.servgff;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Sort;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffFile;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoGffFile;
import com.novelbio.database.mongorepo.geneanno.RepoGffGene;
import com.novelbio.database.mongorepo.geneanno.RepoGffIso;
import com.novelbio.database.service.SpringFactory;

public class ManageGffDetailGene {
	private static final Logger logger = Logger.getLogger(ManageGffDetailGene.class);
	static double[] lock = new double[0];
	
	RepoGffFile repoGffFile;
	RepoGffGene repoGffGene;
	RepoGffIso repoGffIso;
	private ManageGffDetailGene() {
		repoGffFile = (RepoGffFile)SpringFactory.getFactory().getBean("repoGffFile");
		repoGffGene = (RepoGffGene)SpringFactory.getFactory().getBean("repoGffGene");
		repoGffIso = (RepoGffIso)SpringFactory.getFactory().getBean("repoGffIso");
	}
	
	public void saveGffHashGene(GffHashGene gffHashGene) {
		GffFile gffFile = repoGffFile.findByTaxIdAndVersionAndDbinfo(gffHashGene.getTaxID(), gffHashGene.getVersion(), gffHashGene.getDbinfo());
		if (gffFile != null) {
			return;
		}
		gffFile = new GffFile();
		gffFile.setFileName(gffHashGene.getGffFilename());
		gffFile.setTaxID(gffHashGene.getTaxID());
		gffFile.setVersion(gffHashGene.getVersion());
		gffFile.setDbinfo(gffHashGene.getDbinfo());
		gffFile.setMapChrID2LsInterval(gffHashGene.getMapChrID2LsInterval(10));
		repoGffFile.save(gffFile);
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffFileId(gffFile.getId());
				repoGffIso.save(gffGeneIsoInfo);
			}
			gffDetailGene.setGffFileId(gffFile.getId());
			repoGffGene.save(gffDetailGene);
		}
	}
	
	public void saveGffFile(GffFile gffFile) {
		repoGffFile.save(gffFile);
	}
	
	public void delete(GffFile gffFile) {
		repoGffGene.deleteByFileId(gffFile.getId());
		repoGffIso.deleteByFileId(gffFile.getId());
		repoGffFile.delete(gffFile.getId());
	}
	
	public List<GffFile> getLsGffFileAll() {
		return repoGffFile.findAll();
	}
	
	public GffFile findGffFile(int taxId, String version, String dbinfo) {
		return repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbinfo);
	}	
	
	public List<GffDetailGene> searchRegionOverlap(int taxID, String version, String dbinfo, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffDetailGene> lsGffDetailGenes = repoGffGene.findByTaxId_Version_Dbinfo_ChrId_RegionOverlap
				(taxID, version, dbinfo, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	public List<GffDetailGene> searchRegionIn(int taxID, String version, String dbinfo, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffDetailGene> lsGffDetailGenes = repoGffGene.findByTaxId_Version_Dbinfo_ChrId_RegionCover
				(taxID, version, dbinfo, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	/** 模糊查找 */
	public List<GffDetailGene> searchGene(int taxID, String version, String dbinfo, String geneNameRegex) {
		List<GffDetailGene> lsGffDetailGenes = searchGeneExact(taxID, version, dbinfo, geneNameRegex);
		if (lsGffDetailGenes.isEmpty()) {
			GeneID geneID = new GeneID(geneNameRegex, taxID);
			if (geneID.getTaxID() == 0) {
				return new ArrayList<>();
			}
			String geneId = geneID.getGeneUniID();
			lsGffDetailGenes = searchGeneExact(taxID, version, dbinfo, geneId);
		}
		
		if (lsGffDetailGenes.isEmpty()) {
			if (geneNameRegex.length() <= 1) {
				return new ArrayList<>();
			}	
			lsGffDetailGenes = searchGeneRegex(taxID, version, dbinfo, geneNameRegex);
		}

		
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	private List<GffDetailGene> searchGeneRegex(int taxID, String version, String dbinfo, String geneNameRegex) {
		geneNameRegex = geneNameRegex.toLowerCase();
		return repoGffGene.findByTaxId_Version_Dbinfo_Name_Regex(taxID, version, dbinfo, geneNameRegex);
	}
	private List<GffDetailGene> searchGeneExact(int taxID, String version, String dbinfo, String geneNameExact) {
		geneNameExact = geneNameExact.toLowerCase();
		return repoGffGene.findByTaxId_Version_Dbinfo_Name_Exact(taxID, version, dbinfo, geneNameExact);
	}

	
	static class ManageGffDetailGeneHolder {
		static ManageGffDetailGene manageGffDetailGene = new ManageGffDetailGene();
	}
	
	public static ManageGffDetailGene getInstance() {
		return ManageGffDetailGeneHolder.manageGffDetailGene;
	}
	
}
