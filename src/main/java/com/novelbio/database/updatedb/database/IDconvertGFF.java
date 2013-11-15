package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collection;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * ID转换，已知两个gff文件，将一个gff与另一个gff进行比对，找到相同的基因然后做ID转换。
 * 目前仅用在大豆上
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
	public void setGffDestination(GffHashGene gffHashGeneDestination) {
		this.gffHashGeneDestination = gffHashGeneDestination;
	}
	public void setDBinfo(String dBinfo) {
		DBinfo = dBinfo;
	}
	public void updateDB() {
		Collection<GffDetailGene> lsGffDetailGenes = gffHashGeneQuery.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String chrID = gffGeneIsoInfo.getRefIDlowcase();
				int startID = gffGeneIsoInfo.getStartAbs();
				int endID = gffGeneIsoInfo.getEndAbs();
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
			lsCoveredCopedID = gffCodGeneDU.getCoveredGene();
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
				copedID.setUpdateDBinfo(DBAccIDSource.NCBI, true);
				copedID.update(true);
			}
		}
		txtOut.close();
	}
	
}
