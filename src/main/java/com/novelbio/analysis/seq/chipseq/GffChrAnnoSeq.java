package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hg.doc.fa;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

/** 给定peak cover的区域，将覆盖到tss等区域的坐标提取出来
 * 目前仅分析tss上游，tes下游，genebody区域
 * @author novelbio
 *
 */
public class GffChrAnnoSeq {
	/** 左右两边扩展2bp，方便统计CG，CHG，CHH等 */
	int extend = 2;
	
	GffChrAbs gffChrAbs;
	/** 向上扩展的tss上游 */
	int tssUpstream;
	/** 向下延长的tes下游 */
	int tesDownstream;
	public GffChrAnnoSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 正数 */
	public void setTssUpstream(int tssUpstream) {
		this.tssUpstream = tssUpstream;
	}
	/** 正数 */
	public void setTesDownstream(int tesDownstream) {
		this.tesDownstream = tesDownstream;
	}
	/** 设定提取序列的左右扩展，一般分析CG，CHG，CHH需要左右扩展2bp */
	public void setExtend(int extend) {
		this.extend = extend;
	}
	
	/** 查找位点，返回位点所含有的CpG信息 */
	public Map<GeneStructure, CpGanalysis> queryAlignment(Alignment alignment) {
		Map<GeneStructure, CpGanalysis> mapStr2CpgInfo = new HashMap<>();
		
		Map<GeneStructure, List<SeqFasta>> mapStr2LsSeq = querySeq(alignment);
		for (GeneStructure structure : mapStr2LsSeq.keySet()) {
			CpGanalysis cpGanalysis = new CpGanalysis();
			mapStr2CpgInfo.put(structure, cpGanalysis);
			for (SeqFasta seqFasta : mapStr2LsSeq.get(structure)) {
				cpGanalysis.addSequence(seqFasta, false);
			}
		}
		return mapStr2CpgInfo;
	}
	
	
	/** 给定坐标区间，返回落在exon，intron上的序列 */
	public Map<GeneStructure, List<SeqFasta>> querySeq(Alignment alignment) {
		Map<GeneStructure, List<SeqFasta>> mapGeneStructure2LsSeq = new HashMap<>();
		List<Align> lsAlignGeneBody = querySeqGeneBody(alignment);
		mapGeneStructure2LsSeq.put(GeneStructure.ALLLENGTH, getLsSeqFasta(lsAlignGeneBody));
		
		List<Align> lsAlignTss = querySeqTss(alignment);
		mapGeneStructure2LsSeq.put(GeneStructure.TSS, getLsSeqFasta(lsAlignTss));
		
		List<Align> lsAlignTes = querySeqTes(alignment);
		mapGeneStructure2LsSeq.put(GeneStructure.TES, getLsSeqFasta(lsAlignTes));
		
		return mapGeneStructure2LsSeq;
	}
	
	/** 给定坐标区间，返回落在exon，intron上的序列 */
	private List<Align> querySeqGeneBody(Alignment alignment) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
		gffCodGeneDU.setGeneBody(true);
		Set<GffDetailGene> setCoveredGene = gffCodGeneDU.getCoveredOverlapGffGene();
		List<Align> lsGeneBodyCovered = new ArrayList<Align>();
		for (GffDetailGene gffDetailGeneRaw : setCoveredGene) {
			int start = Math.max(alignment.getStartAbs(), gffDetailGeneRaw.getStartAbs());
			int end = Math.min(alignment.getEndAbs(), gffDetailGeneRaw.getEndAbs());
			lsGeneBodyCovered.add(new Align(alignment.getRefID(), start, end));
		}
		lsGeneBodyCovered = Align.mergeLsAlign(lsGeneBodyCovered);
		return lsGeneBodyCovered;
	}
	
	/** 给定坐标区间，返回落在exon，intron上的序列 */
	private List<Align> querySeqTss(Alignment alignment) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
		gffCodGeneDU.setGeneBody(false);
		gffCodGeneDU.setTss(new int[]{-tssUpstream, 0});
		Set<GffDetailGene> setCoveredGene = gffCodGeneDU.getCoveredOverlapGffGene();
		List<Align> lsTssCovered = new ArrayList<Align>();
		for (GffDetailGene gffDetailGeneRaw : setCoveredGene) {
			int start = 0, end = 0;
			if (gffDetailGeneRaw.isCis5to3()) {
				start = Math.max(alignment.getStartAbs(), gffDetailGeneRaw.getStartAbs() - tssUpstream);
				end = Math.min(alignment.getEndAbs(), gffDetailGeneRaw.getStartAbs());
			} else {
				start = Math.max(alignment.getStartAbs(), gffDetailGeneRaw.getEndAbs());
				end = Math.min(alignment.getEndAbs(), gffDetailGeneRaw.getEndAbs() + tssUpstream);
			}
			lsTssCovered.add(new Align(alignment.getRefID(), start, end));			
		}
		return lsTssCovered;
	}
	
	/** 给定坐标区间，返回落在exon，intron上的序列 */
	private List<Align> querySeqTes(Alignment alignment) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
		gffCodGeneDU.setGeneBody(false);
		gffCodGeneDU.setTes(new int[]{0, tesDownstream});
		Set<GffDetailGene> setCoveredGene = gffCodGeneDU.getCoveredOverlapGffGene();
		List<Align> lsTesCovered = new ArrayList<Align>();
		for (GffDetailGene gffDetailGeneRaw : setCoveredGene) {
			int start = 0, end = 0;
			if (gffDetailGeneRaw.isCis5to3()) {
				start = Math.max(alignment.getStartAbs(), gffDetailGeneRaw.getEndAbs());
				end = Math.min(alignment.getEndAbs(), gffDetailGeneRaw.getEndAbs() + tesDownstream);
			} else {
				start = Math.max(alignment.getStartAbs(), gffDetailGeneRaw.getStartAbs() - tesDownstream);
				end = Math.min(alignment.getEndAbs(), gffDetailGeneRaw.getStartAbs());
			}
			lsTesCovered.add(new Align(alignment.getRefID(), start, end));			
		}
		return lsTesCovered;
	}
	
	/** 提取序列，并且左右扩展2bp */
	private List<SeqFasta> getLsSeqFasta(List<Align> lsAlign) {
		List<SeqFasta> lsSeq = new ArrayList<>();
		for (Align align : lsAlign) {
			SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(align.getRefID(), align.getStartAbs() - extend, align.getEndAbs() + extend);
			lsSeq.add(seqFasta);
		}
		return lsSeq;
	}
	
}
