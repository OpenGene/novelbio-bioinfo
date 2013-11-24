package com.novelbio.analysis.seq.genome;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 给定一个没有ORF的gff
 * 还有一个有ORF的gff
 * 将没有ORF的gff的ATG和UAG注释出来
 * 
 * 务必是同一个物种同一个版本的Gff
 */
public class GffHashModifyORF {
	private static final Logger logger = Logger.getLogger(GffHashModifyORF.class);
	/** 待修该的Gff */
	GffHashGene gffHashGeneRaw;
	/** 参考的Gff，用Ref来矫正Raw的ATG等位点 */
	GffHashGene gffHashGeneRef;
	
	boolean renameGene = true;
	boolean renameIso = false;
	String prefixGeneName = "NBC";
	
	/** 设定基因的前缀，一般用物种名缩写加上随机数比较合适<br>
	 * 譬如: hsa_123
	 * @param prefixGeneName
	 */
	public void setPrefixGeneName(String prefixGeneName) {
		this.prefixGeneName = prefixGeneName;
	}
	/** 将待注释的iso的Parent名字和gffgenedetail名字改成ref的名字 */
	public void setRenameGene(boolean renameGene) {
		this.renameGene = renameGene;
	}
	/** 将待注释的iso的名字改成ref的名字 */
	public void setRenameIso(boolean renameIso) {
		this.renameIso = renameIso;
	}
	public void setGffHashGeneRaw(GffHashGene gffHashGeneRaw) {
		this.gffHashGeneRaw = gffHashGeneRaw;
	}
	public void setGffHashGeneRef(GffHashGene gffHashGeneRef) {
		this.gffHashGeneRef = gffHashGeneRef;
	}
	
	public void modifyGff() {
		Set<GffDetailGene> setGffGeneName = new HashSet<>();//用来去重复的
		for (GffDetailGene gffDetailGeneRef : gffHashGeneRef.getGffDetailAll()) {
			//因为gff文件可能有错，gffgene的长度可能会大于mRNA的总长度，这时候就要遍历每个iso
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGeneRef.getLsCodSplit()) {
				int median = (gffGeneIsoInfo.getStart() + gffGeneIsoInfo.getEnd())/2;
				GffCodGene gffCodGene = gffHashGeneRaw.searchLocation(gffDetailGeneRef.getRefID(), median);
				if (!gffCodGene.isInsideLoc()) {
					logger.warn("cannot find gene on:" + gffDetailGeneRef.getRefID() + " " + median );
				}
				GffDetailGene gffDetailGeneThis = gffCodGene.getGffDetailThis();
				if (setGffGeneName.contains(gffDetailGeneThis)) {
					continue;
				}
				setGffGeneName.add(gffDetailGeneThis);
				modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneThis);
			}
		}
	}
	
	private void modifyGffDetailGene(GffDetailGene gffDetailGeneRef, GffDetailGene gffDetailGeneThis) {
		if (renameGene) {
			gffDetailGeneThis.addItemName(gffDetailGeneRef.getNameSingle());
		}
		Set<String> setIsoName = new HashSet<>();//用来去重复的
		for (GffGeneIsoInfo gffIso : gffDetailGeneThis.getLsCodSplit()) {
			GffGeneIsoInfo gffRef = getSimilarIso(gffIso, gffDetailGeneRef);
			if (gffRef == null) {
				String geneName = gffIso.getParentGeneName();
				if (geneName.startsWith("XLOC")) {
					gffIso.setParentGeneName(geneName.replace("XLOC_", prefixGeneName));
				}
				continue;
			}
			if (renameGene) {
				gffIso.setParentGeneName(gffRef.getParentGeneName());
			}
			if (renameIso) {
				String name = getNoDuplicateName(setIsoName, gffRef.getName());
				setIsoName.add(name);
				gffIso.setName(name);
			}
			if (gffRef.ismRNA()) {
				gffIso.setATGUAGauto(gffRef.getATGsite(), gffIso.getUAGsite());
			}
		}
	}
	
	/**
	 * 将isoname到set中查，查到了就改后缀，直到查不到为止
	 * @param setIsoName
	 * @param isoName
	 * @return
	 */
	private String getNoDuplicateName(Set<String> setIsoName, String isoName) {
		int i = 1;
		//修改名字
		while (setIsoName.contains(isoName)) {
			isoName = FileOperate.changeFileSuffix(isoName, "", ""+i).replace("/", "");
			i++;
		}
		return isoName;
	}
	
	/** 返回相似的ISO，注意这两个ISO的包含atg的exon必须一致或者至少是overlap的 */
	private GffGeneIsoInfo getSimilarIso(GffGeneIsoInfo gffIso, GffDetailGene gffDetailGeneRef) {
		GffGeneIsoInfo gffRef = gffDetailGeneRef.getSimilarIso(gffIso, 0.5);
		if (gffRef != null && gffRef.ismRNA() && isCanbeRef(gffIso, gffRef)) {
			return gffRef;
		} else {
			for (GffGeneIsoInfo gffAnoterRef : gffDetailGeneRef.getLsCodSplit()) {
				if (gffAnoterRef.ismRNA() && isCanbeRef(gffIso, gffAnoterRef)) {
					return gffAnoterRef;
				}
			}
		}
		return null;
	}
	
	/** 只有当atg和uag都落在exon中，才能被当作是可以作为参考的ref iso
	 * 才能将该ref iso的atg和uag加到该iso上。
	 * @return
	 */
	private boolean isCanbeRef(GffGeneIsoInfo gffIso, GffGeneIsoInfo gffRef) {
		if (gffIso.isCis5to3() != gffRef.isCis5to3()) return false;
		
		ListCodAbs<ExonInfo> lsCodExonAtg = gffIso.searchLocation(gffRef.getATGsite());
		ListCodAbs<ExonInfo> lsCodExonUag = gffIso.searchLocation(gffRef.getUAGsite());
		if (lsCodExonAtg.isInsideLoc() && lsCodExonUag.isInsideLoc()) {
			return true;
		}
		return false;
	}
	
}
