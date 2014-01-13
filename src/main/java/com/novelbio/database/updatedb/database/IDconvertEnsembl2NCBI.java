package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import net.sf.picard.annotation.Gene;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * ID转换，将ensembl的表转化为NCBI的表，以及类似功能
 * @author zong0jie
 *
 */
public class IDconvertEnsembl2NCBI {
	GffHashGene gffHashEnsembl;
	GffHashGene gffHashNCBI;
	/** 存储gffFile和对应的taxID */
	LinkedHashMap<String, Integer> hashEnsemblTaxID = new LinkedHashMap<String, Integer>();
	
	/**
	 * 存储对应的gff文件，现在用NCBI的似乎更合适，如果想用ucsc格式的,就在下面改
	 * 这个的目的是，如果ensemble没找到对应的基因，就到ucsc下面来查找对应的坐标，看该坐标下有没有对应的基因，然后写入数据库
	 */
	ArrayList<String> lsUCSCFile = new ArrayList<String>();
	int taxID;
	GffHashGene gffHashGene = null;

	/**
	 * 必须是txt文件
	 * @param fileName 从ensembl下载的gtf文件
	 * @param ucscFile UCSC的坐标文件，不是gtf格式的
	 * @param taxID
	 */
	public void setEnsemblFile(String ensembleGTF, String ncbiGFF, Integer taxID) {
		gffHashEnsembl = new GffHashGene(GffType.GTF, ensembleGTF);
		gffHashNCBI = new GffHashGene(GffType.NCBI, ncbiGFF);
		this.taxID = taxID;
	}
	public void update() {
		for (GffDetailGene gffDetailGene : gffHashEnsembl.getGffDetailAll()) {
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				if (isExist(iso)) {
					continue;
				}
				int start = iso.getStartAbs();
				int end = iso.getEndAbs();
				GffCodGeneDU gffCodGeneDU = gffHashNCBI.searchLocation(gffDetailGene.getRefID(), start, end);
				Set<GffDetailGene> setGffDetailGenes = gffCodGeneDU.getCoveredGffGene();
				for (GffDetailGene gffDetailGene2 : setGffDetailGenes) {
					GffGeneIsoInfo iso2 = gffDetailGene2.getSimilarIso(iso, 0.8);
					if (iso2 == null) {
						continue;
					}
				}
			}
		}
		
		
		
	}
	
	private boolean isExist(GffGeneIsoInfo iso) {
		GeneID geneIDGene = new GeneID(iso.getParentGeneName(), taxID);
		GeneID geneIDTrans = new GeneID(iso.getName(), taxID);
		if (geneIDGene.getIDtype() != GeneID.IDTYPE_GENEID && geneIDTrans.getIDtype() != GeneID.IDTYPE_GENEID) {
			return false;
		} else if (geneIDGene.getIDtype() == GeneID.IDTYPE_GENEID && geneIDTrans.getIDtype() == GeneID.IDTYPE_GENEID) {
			return true;
		} else if (geneIDGene.getIDtype() == GeneID.IDTYPE_GENEID && geneIDTrans.getIDtype() != GeneID.IDTYPE_GENEID) {
			geneIDGene.setUpdateAccID(geneIDTrans.getAccID());
			geneIDGene.update(false);
		} else if (geneIDGene.getIDtype() != GeneID.IDTYPE_GENEID && geneIDTrans.getIDtype() == GeneID.IDTYPE_GENEID) {
			geneIDTrans.setUpdateAccID(geneIDGene.getAccID());
			geneIDTrans.update(false);
		}
		return true;
	}
}
