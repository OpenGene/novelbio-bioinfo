package com.novelbio.database.service.servgff;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffFile;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.SpringFactory;

public class ManageGffDetailGene {
	private static final Logger logger = Logger.getLogger(ManageGffDetailGene.class);
	static double[] lock = new double[0];

	MongoTemplate mongoTemplate;

	private ManageGffDetailGene() {
		mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
	}
	
	public void saveGffHashGene(GffHashGene gffHashGene) {
		GffFile gffFile = new GffFile();
		gffFile.setFileName(gffHashGene.getGffFilename());
		gffFile.setTaxID(gffHashGene.getTaxID());
		gffFile.setVersion(gffHashGene.getVersion());
		gffFile.setDbinfo(gffHashGene.getDbinfo());
		gffFile.setMapChrID2LsInterval(gffHashGene.getMapChrID2LsInterval(10));
		List<GffFile> lsGffFiles = mongoTemplate.find(new Query(Criteria.where("fileName").is(gffFile.getFileName())), GffFile.class);
		if (lsGffFiles.isEmpty()) {
			mongoTemplate.save(gffFile);
		} else {
			gffFile.setId(lsGffFiles.get(0).getId());
			mongoTemplate.save(gffFile);
		}
		
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			saveGffDetailGene(gffDetailGene);
		}
	}
	
	/** 仅update 每条染色体上的interval */
	public void saveGffChrInterval(GffHashGene gffHashGene) {
		List<GffFile> lsGffFiles = mongoTemplate.find(new Query(Criteria.where("fileName").is(gffHashGene.getGffFilename())), GffFile.class);
		if (lsGffFiles.isEmpty()) {
			return;
		}
		GffFile gffFile = lsGffFiles.get(0);
		gffFile.setMapChrID2LsInterval(gffHashGene.getMapChrID2LsInterval(10));
		mongoTemplate.save(gffFile);
	}
	
	public void saveGffFile(GffFile gffFile) {
		mongoTemplate.save(gffFile);
	}
	
	public void delete(GffFile gffFile) {
		mongoTemplate.remove(gffFile);
		mongoTemplate.remove(new Query( Criteria.where("taxID").is(gffFile.getTaxID())
				.andOperator( Criteria.where("version").is(gffFile.getVersion())
						.andOperator(Criteria.where("dbinfo").is(gffFile.getDbinfo()))
						)
				),
				GffDetailGene.class);
	}
	
	public List<GffFile> getLsGffFileAll() {
		return mongoTemplate.findAll(GffFile.class);
	}
	
	public GffFile findGffFile(int taxId, String version, String dbinfo) {
		List<GffFile> lsGffFiles = mongoTemplate
				.find(new Query( Criteria.where("taxID").is(taxId)
						.andOperator(
								Criteria.where("version").is(version)
								.andOperator(Criteria.where("dbinfo").is(dbinfo)
										)
								)
						),
						GffFile.class);
		if (lsGffFiles.isEmpty()) {
			return null;
		}
		return lsGffFiles.get(0);
	}
	
	public List<GffFile> findAllGffFile() {
		List<GffFile> lsGffFiles = mongoTemplate.findAll(GffFile.class);
		return lsGffFiles;
	}
	
	public void saveGffDetailGene(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null) {
			return;
		}
		List<GffDetailGene> lsGffDetailGenes = mongoTemplate
				.find(new Query( Criteria.where("taxID").is(gffDetailGene.getTaxID())
						.andOperator(
								Criteria.where("version").is(gffDetailGene.getVersion())
								.andOperator(Criteria.where("dbinfo").is(gffDetailGene.getDbinfo())
										.andOperator(
												Criteria.where("parentName").is(gffDetailGene.getRefID())
												.andOperator(
														Criteria.where("setItemName").is(gffDetailGene.getNameSingle()))
												)
										)
								)
						),
						GffDetailGene.class);
		for (GffDetailGene gffDetailGeneSubject : lsGffDetailGenes) {
			if (gffDetailGeneSubject.getStartAbs() == gffDetailGene.getStartAbs() && gffDetailGeneSubject.getEndAbs() == gffDetailGene.getEndAbs()) {
				if (gffDetailGene.getNameSingle().equals(gffDetailGeneSubject.getNameSingle())) {
					gffDetailGene.setId(gffDetailGeneSubject.getId());
				} else {
					logger.warn("find same start and end gene, insert:" + gffDetailGene.getNameSingle() + " exist:" + gffDetailGeneSubject.getNameSingle() +" so just return");
					return;
				}
			}
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			saveGffIso(gffGeneIsoInfo);
		}
		gffDetailGene.setNameLowcase();
		mongoTemplate.save(gffDetailGene);
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			saveGffIso(gffGeneIsoInfo);
		}
	}
	
	private void saveGffIso(GffGeneIsoInfo gffGeneIsoInfo) {
		if (gffGeneIsoInfo == null) {
			return;
		}
		else if (gffGeneIsoInfo.getId() == null && gffGeneIsoInfo.getGffGeneId() != null) {
			List<GffGeneIsoInfo> lsIsos = mongoTemplate
					.find(new Query( Criteria.where("gffGeneId").is(gffGeneIsoInfo.getGffGeneId())
							.andOperator(
									Criteria.where("listName").is(gffGeneIsoInfo.getName())
									)
							),
							GffGeneIsoInfo.class);
			if (lsIsos.size() > 0) {
				return;
			}
		}
		
		mongoTemplate.save(gffGeneIsoInfo);
	}
	
	public List<GffDetailGene> searchRegionOverlap(int taxID, String version, String dbinfo, String chrID, int start, int end) {
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		List<GffDetailGene> lsGffDetailGenes = mongoTemplate
				.find(new Query( Criteria.where("taxID").is(taxID)
						.andOperator(
								Criteria.where("version").is(version)
								.andOperator(
										Criteria.where("dbinfo").is(dbinfo)
										.andOperator(
												Criteria.where("parentName").is(chrID)
												.andOperator(
														Criteria.where("numberstart").lte(endAbs)
														.andOperator(Criteria.where("numberend").gte(startAbs))
														)
												)
										)
								)
						).with(new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart"))),
						GffDetailGene.class);
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	public List<GffDetailGene> searchRegionOverlap(int taxID, String version, String dbinfo, String geneName) {
		geneName = geneName.toLowerCase();
		List<GffDetailGene> lsGffDetailGenes = mongoTemplate
				.find(new Query( Criteria.where("taxID").is(taxID)
						.andOperator(
								Criteria.where("version").is(version)
								.andOperator(Criteria.where("dbinfo").is(dbinfo)
										.andOperator(
												Criteria.where("setNameLowcase").is(geneName)
												)
										)
								)
						).with(new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart"))),
						GffDetailGene.class);
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
		List<GffDetailGene> lsGffDetailGenes = mongoTemplate
				.find(new Query( Criteria.where("taxID").is(taxID)
						.andOperator(
								Criteria.where("version").is(version)
								.andOperator(Criteria.where("dbinfo").is(dbinfo)
										.andOperator(
												Criteria.where("parentName").is(chrID)
												.andOperator(
														Criteria.where("numberstart").gte(startAbs)
														.andOperator(Criteria.where("numberend").lte(endAbs))
														)
												)
										)
								)
						).with(new Sort(new Sort.Order(Sort.Direction.ASC, "numberstart"))),
						GffDetailGene.class);
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				gffGeneIsoInfo.setGffDetailGeneParent(gffDetailGene);
			}
		}
		return lsGffDetailGenes;
	}
	
	static class ManageGffDetailGeneHolder {
		static ManageGffDetailGene manageGffDetailGene = new ManageGffDetailGene();
	}
	
	public static ManageGffDetailGene getInstance() {
		return ManageGffDetailGeneHolder.manageGffDetailGene;
	}
	
}
