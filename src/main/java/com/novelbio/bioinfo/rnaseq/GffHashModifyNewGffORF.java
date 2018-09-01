package com.novelbio.bioinfo.rnaseq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffCodGene;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.database.domain.modgeneid.GeneType;

/**
 * 给定一个没有ORF的gff
 * 还有一个有ORF的gff
 * 将没有ORF的gff的ATG和UAG注释出来
 * 
 * 务必是同一个物种同一个版本的Gff
 */
public class GffHashModifyNewGffORF {
	public static void main(String[] args) {
//		String parentPath = "/media/nbfs/nbCloud/public/AllProject/project_550a6f82e4b0b3b73a8e211e/";
//		GffHashGene gffBGI = new GffHashGene(GffType.GTF, parentPath + "task_56e8c41a60b2e0371245567a/other_result/2-100-0.gtf");
//		GffHashGene gffNew = new GffHashGene(GffType.GTF, parentPath + "task_572f1cdb60b21a47c508334c/ReconstructTranscriptome_result/CuffMerge.gtf");
		
//		gffNew.writeToGTF(parentPath + "NBCTranscriptom4.gtf");
		
//		GffHashModifyNewGffORF gffHashModifyNewGffORF = new GffHashModifyNewGffORF();
//		gffHashModifyNewGffORF.setGffHashGeneRaw(gffNew);
//		gffHashModifyNewGffORF.setGffHashGeneRef(gffBGI);
//		gffHashModifyNewGffORF.modifyGff();
//		gffNew.writeToGTF(parentPath + "task_572f1cdb60b21a47c508334c/ReconstructTranscriptome_result/NBCTranscriptom4.gtf");
		
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/test/stringtie/result.txt", true);
//		for (String geneName : gffHashModifyNewGffORF.mapRef2ThisGeneName.keySet()) {
//			txtWrite.writefileln(geneName + "\t" + gffHashModifyNewGffORF.mapRef2ThisGeneName.get(geneName) );
//		}
//		txtWrite.close();
		
		
		
		String parentPath = "/home/novelbio/NBCsource/test/gffmerge/";
		GffHashGene gffBGI = new GffHashGene(GffType.GTF, parentPath + "merge_modify.gtf");
		GffHashGene gffNew = new GffHashGene(GffType.GTF, parentPath + "CuffMerge.gtf");
			
		GffHashModifyNewGffORF gffHashModifyNewGffORF = new GffHashModifyNewGffORF();
		gffHashModifyNewGffORF.setGffHashGeneRaw(gffNew);
		gffHashModifyNewGffORF.setGffHashGeneRef(gffBGI);
		gffHashModifyNewGffORF.modifyGff();
		gffNew.writeToGTF(parentPath + "result2.gtf");
		
		
		
	}
	private static final Logger logger = Logger.getLogger(GffHashModifyNewGffORF.class);
	/** 待修该的Gff */
	GffHashGene gffHashGeneRaw;
	/** 参考的Gff，用Ref来矫正Raw的ATG等位点 */
	GffHashGene gffHashGeneRef;
	
	GffHashGene gffResult;
	
	boolean renameGene = true;
	boolean renameIso = true;
	String prefixGeneName = "NBC";
	/** 基因名字对照表 */
	Map<String, String> mapRef2ThisGeneName = new HashMap<>();
	/** 基因名字对照表 */
	Map<String, String> mapRef2ThisIsoName = new HashMap<>();
	/** 用来去重复的 */
	Set<String> setIsoNameRemoveRedundant = new HashSet<>();
	
	/**
	 * 不用设定该方法了<br>
	 * 设定基因的前缀，一般用物种名缩写加上随机数比较合适<br>
	 * 譬如: hsa_123
	 * @param prefixGeneName
	 */
	@Deprecated
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
	
	/** modify完毕后获得修正后的gff文件 */
	public GffHashGene getGffResult() {
		return gffResult;
	}
	public void modifyGff() {
		setIsoNameRemoveRedundant.clear();
		for (GffGene gffDetailGeneRef : gffHashGeneRef.getLsGffDetailGenes()) {
			//因为gff文件可能有错，gffgene的长度可能会大于mRNA的总长度，这时候就要遍历每个iso
			for (GffIso gffGeneIsoInfo : gffDetailGeneRef.getLsCodSplit()) {
				int median = (gffGeneIsoInfo.getStart() + gffGeneIsoInfo.getEnd())/2;
				GffCodGene gffCodGene = gffHashGeneRaw.searchLocation(gffDetailGeneRef.getRefID(), median);
				if (gffCodGene == null) {
					gffHashGeneRaw.addGffDetailGene(gffDetailGeneRef);
					continue;
				} else if (!gffCodGene.isInsideLoc()) {
					logger.warn("cannot find gene on:" + gffDetailGeneRef.getRefID() + " " + median );
					continue;
				}
				GffGene gffDetailGeneThis = gffCodGene.getAlignThis();
				modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneThis);
				GffGene gffDetailGeneUp = gffCodGene.getAlignUp();
				modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneUp);
				GffGene gffDetailGeneDown = gffCodGene.getAlignDown();
				modifyGffDetailGene(gffDetailGeneRef, gffDetailGeneDown);
			}
		}
		gffResult = filterGene2();
	}
	
	private GffHashGene filterGene() {
		return gffHashGeneRaw;
	}
	
	private GffHashGene filterGene2() {
		Set<String> setGffKnownGeneName = new HashSet<String>();
		for (GffGene gffDetailGene : gffHashGeneRef.getLsGffDetailGenes()) {
			for (GffIso iso : gffDetailGene.getLsCodSplit()) {
				setGffKnownGeneName.add(iso.getParentGeneName());
			}
		}
		
		GffHashGene gffResult = new GffHashGene();
		for (GffGene gffDetailGene : gffHashGeneRaw.getLsGffDetailGenes()) {
			GffGene gffDetailGeneNew = gffDetailGene.clone();
			gffDetailGeneNew.clearIso();
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (setGffKnownGeneName.contains(gffGeneIsoInfo.getParentGeneName())
						|| gffGeneIsoInfo.size() > 1
						) {
					gffDetailGeneNew.addIsoSimple(gffGeneIsoInfo);
				}
				if (!gffGeneIsoInfo.ismRNAFromCds()) {
					gffGeneIsoInfo.setGeneType(GeneType.miscRNA);
				}
			}
			if (gffDetailGeneNew.getLsCodSplit().size() > 0) {
				gffResult.addGffDetailGene(gffDetailGene);
			}
		}
		gffResult.initialGffWhileAddGffDetailGene();
		return gffResult;
	}
	
	private void modifyGffDetailGene(GffGene gffDetailGeneRef, GffGene gffDetailGeneThis) {
		if (gffDetailGeneThis == null) return;
		
		boolean refChangeName = false;
		Set<String> setIsoName = new HashSet<>();//用来去重复的
		for (GffIso gffIso : gffDetailGeneThis.getLsCodSplit()) {
			GffIso gffRefAtg = getSimilarIso(gffIso, gffDetailGeneRef);
			GffIso gffRefChangeName = gffDetailGeneRef.getSimilarIso(gffIso, 0.6);
			gffRefChangeName = getChangeNameIso(gffRefChangeName, gffRefAtg);
			if (gffRefChangeName == null) {
				continue;
			}
			
			if (setIsoNameRemoveRedundant.contains(gffIso.getName())) {
				continue;
			}
			setIsoNameRemoveRedundant.add(gffIso.getName());
			
			refChangeName = true;
			
			if (renameGene) {
				mapRef2ThisGeneName.put(gffRefChangeName.getParentGeneName(), gffIso.getParentGeneName());
				gffIso.setParentGeneName(gffRefChangeName.getParentGeneName());
			}
			if (renameIso) {
				String name = getNoDuplicateName(setIsoName, gffRefChangeName.getName());
				mapRef2ThisIsoName.put(name, gffIso.getName());
				setIsoName.add(name);
				gffIso.setName(name);
			}
			if (gffRefAtg != null && gffRefAtg.ismRNA()) {
				gffIso.setATGUAGauto(gffRefAtg.getATGsite(), gffRefAtg.getUAGsite());
			}
		}
		
		if (renameGene&&refChangeName) {
			gffDetailGeneThis.setName(gffDetailGeneRef.getName());
		}
	}
	
	private GffIso getChangeNameIso(GffIso... refIso) {
		for (GffIso gffGeneIsoInfo : refIso) {
			if (gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		return null;
	}
	
	/**
	 * 将isoname到set中查，查到了就改后缀，直到查不到为止
	 * @param setIsoName
	 * @param isoName
	 * @return
	 */
	@VisibleForTesting
	protected static String getNoDuplicateName(Set<String> setIsoName, String isoName) {
		if (!setIsoName.contains(isoName)) {
			return isoName;
		}
		int i = 1;
		String isoNameTmp = isoName;
		int lastIndex = isoName.lastIndexOf("-");
		if (lastIndex > 0) {
			try {
				int index = Integer.parseInt( isoName.substring(lastIndex+1, isoName.length()));
				if (index < 100) {
					isoNameTmp = isoName.substring(0, lastIndex);
				}
			} catch (Exception e) {}
		}
		String isoNameFinal = isoNameTmp + "-" +i;
		//修改名字
		while (setIsoName.contains(isoNameFinal)) {
			i++;
			isoNameFinal = isoNameTmp + "-" + i;
		}
		return isoNameFinal;
	}
	
	/** 返回相似的ISO，注意这两个ISO的包含atg的exon必须一致或者至少是overlap的 */
	private GffIso getSimilarIso(GffIso gffIso, GffGene gffDetailGeneRef) {
		GffIso gffRef = gffDetailGeneRef.getSimilarIso(gffIso, 0.5);
		if (gffRef != null && gffRef.ismRNA() && isCanbeRef(gffIso, gffRef)) {
			return gffRef;
		} else {
			for (GffIso gffAnoterRef : gffDetailGeneRef.getLsCodSplit()) {
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
	private boolean isCanbeRef(GffIso gffIso, GffIso gffRef) {
		if (gffIso.isCis5to3() != gffRef.isCis5to3()) return false;
		
		if (gffIso.getCodLoc(gffRef.getATGsite()) == GffIso.COD_LOC_EXON 
				&& gffIso.getCodLoc(gffRef.getUAGsite()) ==  GffIso.COD_LOC_EXON
			) {
			return true;
		}
		return false;
	}
	
}
