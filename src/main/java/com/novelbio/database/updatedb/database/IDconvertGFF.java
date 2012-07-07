package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ID转换，已知两个gff文件，将一个gff与另一个gff进行比对，找到相同的基因然后做ID转换。
 * @author zong0jie
 *
 */
public class IDconvertGFF {
	int taxID = 0;
	/** 待转化的gff */
	GffHashGene gffHashGeneQuery = null;
	/** 目的gff */
	GffHashGene gffHashGeneDestination= null;
	String DBinfo = "";
	TxtReadandWrite txtOut = null;
	public void setTxtOut(String txtOutFile) {
		this.txtOut = new TxtReadandWrite(txtOutFile, true);
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public void setGffQuery(String gffFile, String gffType) {
		gffHashGeneQuery = new GffHashGene(gffType, gffFile);
	}
	public void setGffDestination(String gffFile, String gffType) {
		gffHashGeneDestination = new GffHashGene(gffType, gffFile);
	}
	public void setDBinfo(String dBinfo) {
		DBinfo = dBinfo;
	}
	public void updateDB() {
		Collection<GffDetailGene> lsGffDetailGenes = gffHashGeneQuery.getLocHashtable().values();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String chrID = gffGeneIsoInfo.getChrID();
				int startID = gffGeneIsoInfo.getStartAbs();
				int endID = gffGeneIsoInfo.getEndAbs();
				if (gffGeneIsoInfo.getName().contains("Glyma01g00600.1")) {
					System.out.println("stop");
					 endID = gffGeneIsoInfo.getEndAbs();
				}
				GffCodGeneDU gffCodGeneDU = gffHashGeneDestination.searchLocation(chrID, startID, endID);
				if (!updateInfo(gffGeneIsoInfo, gffCodGeneDU)) {
					txtOut.writefileln(gffGeneIsoInfo.getName());
				}
			}
		}
		txtOut.close();
	}
	
	/**
	 * 给定query的geneIso的信息，和查找Destination的结果，将结果进行升级
	 * @param gffGeneIsoInfo
	 * @param gffCodGeneDU
	 */
	private boolean updateInfo(GffGeneIsoInfo gffGeneIsoInfo, GffCodGeneDU gffCodGeneDU) {
		GeneID copedIDUp = new GeneID(gffGeneIsoInfo.getName(), taxID);
		ArrayList<GeneID> lsCoveredCopedID = new ArrayList<GeneID>();
		if (gffCodGeneDU != null) {
			lsCoveredCopedID = gffCodGeneDU.getAllCoveredGenes();
			if (lsCoveredCopedID != null && lsCoveredCopedID.size() > 0) {
				GeneID copedIDmid = lsCoveredCopedID.get(lsCoveredCopedID.size()/2);
				copedIDUp.setUpdateRefAccID(copedIDmid.getAccID());
			}
		}
		copedIDUp.setUpdateDBinfo(DBinfo, true);
		return copedIDUp.update(true);
	}
	
	public void updateDBNCBI() {
		Collection<GffDetailGene> lsGffDetailGenes = gffHashGeneDestination.getLocHashtable().values();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				GeneID copedID = new GeneID(gffGeneIsoInfo.getName(), taxID);
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBIID, true);
				copedID.update(true);
			}
		}
		txtOut.close();
	}
	
}
