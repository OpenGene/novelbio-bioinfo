package com.novelbio.software.idconvert;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;

/**
 * 基于Gff文件进行基因名的对照
 * 
 * 譬如水稻NCBI和IRGSP，目前没找到好的NCBI--IRGSP ID转换
 * 那么我们可以基于Gff文件自己生成这样一个转换关系表
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

		for (GffGene geneRef : gffGeneRef.getLsGffDetailGenes()) {
			GffIso isoRef = geneRef.getLongestSplitMrna();
			
			GffCodGeneDU gffCodGeneDU = gffGeneAlt.searchLocation(geneRef.getRefID(), geneRef.getStartAbs(), geneRef.getEndAbs());
			if (gffCodGeneDU == null) {
				continue;
			}
			Set<GffGene> setGeneAlt = gffCodGeneDU.getCoveredOverlapGffGene();
			
			if (setGeneAlt.size() == 1) {
				GffGene geneAlt = setGeneAlt.iterator().next();
				GffIso isoAlt = geneAlt.getMostSameIso(isoRef);
				if (isoAlt == null) {
					txtOut.writefileln(geneRef.getName() + "\t" + "None");
					continue;
				}
				int[] sameBorderInfo = GffIso.compareIso(isoRef, isoAlt);
				if (isSame(sameBorderInfo)) {
					txtOut.writefileln(geneRef.getName() + "\t" + setGeneAlt.iterator().next().getName());
				} else {
					logger.error("please check single " + geneRef.getName() + " " + geneAlt.getName());
					txtOutWrong.writefileln("single " + geneRef.getName() + "\t" + geneAlt.getName());
					txtOut.writefileln(geneRef.getName() + "\t" + "None");
				}
			} else if (setGeneAlt.isEmpty()) {
				txtOut.writefileln(geneRef.getName() + "\t" + "None");
			} else {
				int[] sameBorderInfo = null;
				GffGene geneAlt = null;
				for (GffGene geneAltTmp : setGeneAlt) {
					GffIso isoAlt = geneAltTmp.getMostSameIso(isoRef);
					if (isoAlt == null) {
						continue;
					}
					int[] sameBorderInfoTmp = GffIso.compareIso(isoRef, isoAlt);
					if (sameBorderInfo == null || sameBorderInfo[0]  < sameBorderInfoTmp[0]) {
						sameBorderInfo = sameBorderInfoTmp;
						geneAlt = geneAltTmp;
					}
				}
				if (sameBorderInfo == null) {
					txtOut.writefileln(geneRef.getName() + "\t" + "None");
				} else if (isSame(sameBorderInfo)) {
					txtOut.writefileln(geneRef.getName() + "\t" + setGeneAlt.iterator().next().getName());
				} else {
					logger.error("please check " + geneRef.getName() + " " + geneAlt.getName());
					txtOutWrong.writefileln("multi " + geneRef.getName() + "\t" + geneAlt.getName());
					txtOut.writefileln(geneRef.getName() + "\t" + "None");
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
