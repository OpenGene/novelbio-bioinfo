package com.novelbio.analysis.seq.genome.gffOperate;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 比较UCSC的gff和NCBI的gff是否一致，
 * 一致的话表示基因组也是一致的，那么就
 * 可以用UCSC的repeat去代替NCBI */
public class GffCompareUCSCandNCBI {
	Logger logger = Logger.getLogger(GffCompareUCSCandNCBI.class);
	String gffUCSC = "";
	String gffNCBI = "";
	String outFile = "";            
	GffHashGeneNCBI gffHashGeneNCBI = new GffHashGeneNCBI();
	GffHashGeneUCSC gffHashGeneUCSC = new GffHashGeneUCSC();
	
	public static void main(String[] args) {
		String gffNCBI = "/media/winE/Bioinformatics/genome/mouse/mm10_GRCm38/gff/ref_GRCm38_top_level_modify.gff3";
		String gffUCSC = "/media/winE/Bioinformatics/genome/mouse/mm10_GRCm38/gff/mm10_refseq_UCSC.txt";
		String out = "/media/winE/Bioinformatics/genome/mouse/mm10_GRCm38/gff/out";
		GffCompareUCSCandNCBI gffCompareUCSCandNCBI = new GffCompareUCSCandNCBI();
		gffCompareUCSCandNCBI.setGff(gffNCBI, gffUCSC, out);
		gffCompareUCSCandNCBI.compare();
	}
	/**
	 * @param gffNCBI
	 * @param gffUCSC
	 * @param out 统计结果
	 */
	public void setGff(String gffNCBI, String gffUCSC, String out) {
		this.gffNCBI = gffNCBI;
		this.gffUCSC = gffUCSC;
		this.outFile = out;
	}
	public void compare() {
		initial();
		compare(outFile);
	}
	private void initial() {
		gffHashGeneNCBI.ReadGffarray(gffNCBI);
		gffHashGeneUCSC.ReadGffarray(gffUCSC);
	}
	
	private void compare(String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (GffDetailGene gffDetailGeneUCSC : gffHashGeneUCSC.getGffDetailAll()) {
			GffGeneIsoInfo gffGeneIsoInfoUCSC = gffDetailGeneUCSC.getLongestSplitMrna();
			GffGeneIsoInfo gffGeneIsoInfoNCBI = gffHashGeneNCBI.searchISO(gffGeneIsoInfoUCSC.getName());
			if (gffGeneIsoInfoNCBI == null) {
				continue;
			}
			if (gffGeneIsoInfoUCSC.getName().startsWith("NR_") || gffGeneIsoInfoUCSC.getATGsite() == gffGeneIsoInfoNCBI.getATGsite() || gffGeneIsoInfoUCSC.getUAGsite() == gffGeneIsoInfoNCBI.getUAGsite()) {
				continue;
			}
			else {
				txtOut.writefileln("出现不一致的Iso\t" +gffGeneIsoInfoNCBI.getParentGffDetailGene().getRefID() + "\t" + gffGeneIsoInfoNCBI.getName());
				logger.error("出现不一致的Iso " + gffGeneIsoInfoNCBI.getName());
			}
		}
	}
}
