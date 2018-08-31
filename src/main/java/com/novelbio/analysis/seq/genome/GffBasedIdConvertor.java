package com.novelbio.analysis.seq.genome;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.genome.gffoperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 基于gff文件进行id转换
 * @author novelbio
 *
 */
public class GffBasedIdConvertor {
	private static final Logger logger = LoggerFactory.getLogger(GffBasedIdConvertor.class);
	
	String gffRef;
	String geneFlagRef;
	String gffAlt;
	String geneFlagAlt;
	
	String idConvertResult;
	
	public static void main(String[] args) {
		GffBasedIdConvertor gffBasedIdConvertor = new GffBasedIdConvertor();
		gffBasedIdConvertor.setGffRef("/home/novelbio/mywork/nongkeyuan/rice_anno/ref_IRGSP-1.0_top_level.gff3", "Dbxref=GeneID:");
		gffBasedIdConvertor.setGffAlt("/home/novelbio/mywork/nongkeyuan/rice_anno/tigr-msu-all.gff3");
		gffBasedIdConvertor.setIdConvertResult("/home/novelbio/mywork/nongkeyuan/rice_anno/ncbi2tigr.txt");
		gffBasedIdConvertor.convert();
	}
	
	public void setGffRef(String gffRef) {
		this.gffRef = gffRef;
	}
	public void setGffAlt(String gffAlt) {
		this.gffAlt = gffAlt;
	}

	public void setGffRef(String gffRef, String geneFlagRef) {
		this.gffRef = gffRef;
		this.geneFlagRef = geneFlagRef;
	}
	public void setGffAlt(String gffAlt, String geneFlagAlt) {
		this.gffAlt = gffAlt;
		this.geneFlagAlt = geneFlagAlt;
	}
	public void setIdConvertResult(String idConvertResult) {
		this.idConvertResult = idConvertResult;
	}
	
	public void convert() {
		GffHashGene gffGeneRef= new GffHashGene();
		gffGeneRef.addGeneNameFlag(geneFlagRef);
		gffGeneRef.readGffFile(gffRef);
		
		GffHashGene gffGeneAlt = new GffHashGene();
		gffGeneAlt.addGeneNameFlag(geneFlagAlt);
		gffGeneAlt.readGffFile(gffAlt);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(idConvertResult, true);
		TxtReadandWrite txtOutWrong = new TxtReadandWrite(FileOperate.changeFileSuffix(idConvertResult, ".notmatch", null), true);

		for (GffDetailGene geneRef : gffGeneRef.getLsGffDetailGenes()) {
			GffGeneIsoInfo isoRef = geneRef.getLongestSplitMrna();
			
			GffCodGeneDU gffCodGeneDU = gffGeneAlt.searchLocation(geneRef.getRefID(), geneRef.getStartAbs(), geneRef.getEndAbs());
			if (gffCodGeneDU == null) {
				continue;
			}
			Set<GffDetailGene> setGeneAlt = gffCodGeneDU.getCoveredOverlapGffGene();
			
			if (setGeneAlt.size() == 1) {
				GffDetailGene geneAlt = setGeneAlt.iterator().next();
				GffGeneIsoInfo isoAlt = geneAlt.getMostSameIso(isoRef);
				if (isoAlt == null) {
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + "None");
					continue;
				}
				int[] sameBorderInfo = GffGeneIsoInfo.compareIso(isoRef, isoAlt);
				if (isSame(sameBorderInfo)) {
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + setGeneAlt.iterator().next().getNameSingle());
				} else {
					logger.error("please check single " + geneRef.getNameSingle() + " " + geneAlt.getNameSingle());
					txtOutWrong.writefileln("single " + geneRef.getNameSingle() + "\t" + geneAlt.getNameSingle());
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + "None");
				}
			} else if (setGeneAlt.isEmpty()) {
				txtOut.writefileln(geneRef.getNameSingle() + "\t" + "None");
			} else {
				int[] sameBorderInfo = null;
				GffDetailGene geneAlt = null;
				for (GffDetailGene geneAltTmp : setGeneAlt) {
					GffGeneIsoInfo isoAlt = geneAltTmp.getMostSameIso(isoRef);
					if (isoAlt == null) {
						continue;
					}
					int[] sameBorderInfoTmp = GffGeneIsoInfo.compareIso(isoRef, isoAlt);
					if (sameBorderInfo == null || sameBorderInfo[0]  < sameBorderInfoTmp[0]) {
						sameBorderInfo = sameBorderInfoTmp;
						geneAlt = geneAltTmp;
					}
				}
				if (sameBorderInfo == null) {
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + "None");
				} else if (isSame(sameBorderInfo)) {
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + setGeneAlt.iterator().next().getNameSingle());
				} else {
					logger.error("please check " + geneRef.getNameSingle() + " " + geneAlt.getNameSingle());
					txtOutWrong.writefileln("multi " + geneRef.getNameSingle() + "\t" + geneAlt.getNameSingle());
					txtOut.writefileln(geneRef.getNameSingle() + "\t" + "None");
				}
			}
			
		}
		txtOutWrong.close();
		txtOut.close();
	}
	
	private boolean isSame(int[] sameBorderInfo) {
		return (double)sameBorderInfo[0]/sameBorderInfo[2] >= 0.7 || (double)sameBorderInfo[0]/sameBorderInfo[3] >= 0.7
				|| sameBorderInfo[2]-sameBorderInfo[0] <= 4 && ((double)sameBorderInfo[0]/sameBorderInfo[2] >= 0.5 || (double)sameBorderInfo[0]/sameBorderInfo[3] >= 0.5)
				|| Math.abs(sameBorderInfo[1] - sameBorderInfo[2]) <= 4 && Math.max(sameBorderInfo[1], sameBorderInfo[2]) <= 6;
				
	}
}
