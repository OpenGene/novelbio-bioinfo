package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.generalConf.Species;

public class GffHashGene implements	GffHashGeneInf, GffHashInf<GffDetailGene, GffCodGene,GffCodGeneDU>{
	
	GffHashGeneAbs gffHashGene = null;
	public GffHashGene(String GffType, String gffFile)
	{
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(Species.RICE);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TAIR)) {
			gffHashGene = new GffHashGenePlant(Species.ARABIDOPSIS);
		}
		gffHashGene.ReadGffarray(gffFile);
	}
	@Override
	public GffDetailGene getGeneDetail(String accID) {
		return gffHashGene.getGeneDetail(accID);
	}

	@Override
	public ArrayList<Long> getGeneStructureLength(int upBp) {
		return gffHashGene.getGeneStructureLength(upBp);
	}

	@Override
	public void setEndRegion(boolean region) {
		gffHashGene.setEndRegion(region);
		
	}

	@Override
	public ArrayList<String> getLOCIDList() {
		return gffHashGene.getLOCIDList();
	}

	@Override
	public ArrayList<String> getLOCChrHashIDList() {
		return gffHashGene.getLOCChrHashIDList();
	}

	@Override
	public String[] getLOCNum(String LOCID) {
		return gffHashGene.getLOCNum(LOCID);
	}

	@Override
	public void setStartRegion(boolean region) {
		gffHashGene.setStartRegion(region);
	}

	@Override
	public HashMap<String, GffDetailGene> getLocHashtable() {
		return gffHashGene.getLocHashtable();
	}

	@Override
	public GffCodGene searchLocation(String chrID, int Coordinate) {
		return gffHashGene.searchLocation(chrID, Coordinate);
	}

	@Override
	public GffDetailGene searchLOC(String LOCID) {
		return gffHashGene.searchLOC(LOCID);
	}

	@Override
	public GffDetailGene searchLOC(String chrID, int LOCNum) {
		return gffHashGene.searchLOC(chrID, LOCNum);
	}
	@Override
	public GffCodGeneDU searchLocation(String chrID, int cod1, int cod2) {
		return gffHashGene.searchLocation(chrID, cod1, cod2);
	}

}
