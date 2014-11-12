package com.novelbio.database.service.servgff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffFile;
import com.novelbio.analysis.seq.genome.gffOperate.GffFileUnit;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoGffFile;
import com.novelbio.database.mongorepo.geneanno.RepoGffFileUnit;
import com.novelbio.database.mongorepo.geneanno.RepoGffGene;
import com.novelbio.database.mongorepo.geneanno.RepoGffIso;
import com.novelbio.database.service.SpringFactory;

public class ManageGffDetailGene {
	private static final Logger logger = Logger.getLogger(ManageGffDetailGene.class);
	static double[] lock = new double[0];
	
	RepoGffFile repoGffFile;
	RepoGffGene repoGffGene;
	RepoGffIso repoGffIso;
	RepoGffFileUnit repoGffFileUnit;
	MongoTemplate mongoTemplate;
	private ManageGffDetailGene() {
		repoGffFile = (RepoGffFile)SpringFactory.getFactory().getBean("repoGffFile");
		repoGffGene = (RepoGffGene)SpringFactory.getFactory().getBean("repoGffGene");
		repoGffIso = (RepoGffIso)SpringFactory.getFactory().getBean("repoGffIso");
		repoGffFileUnit = (RepoGffFileUnit)SpringFactory.getFactory().getBean("repoGffFileUnit");
		mongoTemplate = (MongoTemplate)SpringFactory.getFactory().getBean("mongoTemplate");
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
		repoGffFile.save(gffFile);
		
		Map<String, List<int[]>> mapChrID2LsInterval = gffHashGene.getMapChrID2LsInterval(10);
		for (String chrId : mapChrID2LsInterval.keySet()) {
			List<int[]> lsTrunkNum = mapChrID2LsInterval.get(chrId);
			for (int i = 0; i < lsTrunkNum.size(); i++) {
				GffFileUnit gffFileUnit = new GffFileUnit();
				gffFileUnit.setGffFileId(gffFile.getId());
				gffFileUnit.setTaxVsDb(gffHashGene.getTaxID(), gffHashGene.getVersion(), gffHashGene.getDbinfo());
				gffFileUnit.setChrId(chrId);
				gffFileUnit.setTrunkNum(i);
				gffFileUnit.setTrunkDetail(lsTrunkNum.get(i));
				repoGffFileUnit.save(gffFileUnit);
			}
		}
		
		for (GffDetailGene gffDetailGene : gffHashGene.getLsGffDetailGenes()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffFileId(gffFile.getId());
				repoGffIso.save(gffGeneIsoInfo);
			}
			gffDetailGene.setNameLowcase();
			gffDetailGene.setGffFileId(gffFile.getId());
			repoGffGene.save(gffDetailGene);
		}
	}
	
	public void saveGffFile(GffFile gffFile) {
		repoGffFile.save(gffFile);
	}
	public void delete(GffHashGene gffHashGene) {
		GffFile gffFile = repoGffFile.findByTaxIdAndVersionAndDbinfo(gffHashGene.getTaxID(), gffHashGene.getVersion(), gffHashGene.getDbinfo());
		delete(gffFile);
	}
	public void delete(GffFile gffFile) {
		Query query = new Query( Criteria.where("gffFileId").is(gffFile.getId()));
		mongoTemplate.remove(query, GffGeneIsoInfo.class);
		mongoTemplate.remove(query, GffDetailGene.class);
		mongoTemplate.remove(query, GffFileUnit.class);
		mongoTemplate.remove(query, GffFile.class);
	}
	
	public List<GffFile> getLsGffFileAll() {
		return repoGffFile.findAll();
	}
	
	public GffFile findGffFile(int taxId, String version, String dbinfo) {
		return repoGffFile.findByTaxIdAndVersionAndDbinfo(taxId, version, dbinfo);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 直接查GffFileUnit */
	public GffFileUnit findGffFileUnit(int taxId, String version, String dbinfo, String chrId, int trunkNum) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDbAndChrIdAndTruncNum(taxVsDb, chrId, trunkNum);
	}
	public GffFileUnit findGffFileUnit(String gffFileId, String chrId, int trunkNum) {
		return repoGffFileUnit.findByFileIdAndChrIdAndTruncNum(gffFileId, chrId, trunkNum);
	}
	public List<GffFileUnit> findLsGffFileUnit(String gffFileId) {
		return repoGffFileUnit.findByFileId(gffFileId);
	}
	public List<GffFileUnit> findLsGffFileUnit(int taxId, String version, String dbinfo) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDb(taxVsDb);
	}
	public List<GffFileUnit> findLsGffFileUnit(String gffFileId, String chrId) {
		return repoGffFileUnit.findByFileIdAndChrId(gffFileId, chrId);
	}
	public List<GffFileUnit> findLsGffFileUnit(int taxId, String version, String dbinfo, String chrId) {
		String taxVsDb = GffFileUnit.getTaxVsDb(taxId, version, dbinfo);
		return repoGffFileUnit.findByTaxVsDbAndChrId(taxVsDb, chrId);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public List<GffDetailGene> searchRegionOverlap(String gffFileId, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffDetailGene> lsGffDetailGenes = repoGffGene.findByFileId_ChrId_RegionOverlap
				(gffFileId, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	public List<GffDetailGene> searchRegionIn(String gffFileId, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffDetailGene> lsGffDetailGenes = repoGffGene.findByFileId_ChrId_RegionCover
				(gffFileId, chrID, startAbs, endAbs, new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart")));
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	/** 模糊查找 */
	public List<GffDetailGene> searchGene(GffFile gffFile, String geneNameRegex) {
		List<GffDetailGene> lsGffDetailGenes = searchGeneExact(gffFile.getId(), geneNameRegex);
		if (lsGffDetailGenes.isEmpty()) {
			GeneID geneID = new GeneID(geneNameRegex, gffFile.getTaxID());
			if (geneID.getTaxID() == 0) {
				return new ArrayList<>();
			}
			String geneId = geneID.getGeneUniID();
			lsGffDetailGenes = searchGeneExact(gffFile.getId(), geneId);
		}
		
		if (lsGffDetailGenes.isEmpty()) {
			if (geneNameRegex.length() <= 1) {
				return new ArrayList<>();
			}	
			lsGffDetailGenes = searchGeneRegex(gffFile.getId(), geneNameRegex);
		}

		
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	private List<GffDetailGene> searchGeneRegex(String gffFileId, String geneNameRegex) {
		geneNameRegex = geneNameRegex.toLowerCase();
		return repoGffGene.findByFileId_Name_Regex(gffFileId, geneNameRegex);
	}
	private List<GffDetailGene> searchGeneExact(String gffFileId, String geneNameExact) {
		geneNameExact = geneNameExact.toLowerCase();
		return repoGffGene.findByFileId_Name_Exact(gffFileId, geneNameExact);
	}

	
	static class ManageGffDetailGeneHolder {
		static ManageGffDetailGene manageGffDetailGene = new ManageGffDetailGene();
	}
	
	public static ManageGffDetailGene getInstance() {
		return ManageGffDetailGeneHolder.manageGffDetailGene;
	}
	
}
