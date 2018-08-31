package com.novelbio.software.snpanno;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.gff.EnumMrnaLocate;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public class SnpIsoHgvsc {
	/** 与剪接位点距离的绝对值，小于该距离才会考虑剪接位点的影响 */
	static int splitRegion = 2;
	
	SnpInfo snpInfo;
	GffIso iso;
	
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta refSeqIntactAA;
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta altSeqIntactAA;
	
	EnumMrnaLocate enumMrnaLocate;
	
	
	public SnpIsoHgvsc(SnpInfo snpRefAltInfo, GffIso iso) {
		this.snpInfo = snpRefAltInfo;
		this.iso = iso;
	}
	
	/**
	 * “g.” for a genomic reference sequence<br>
	 * “c.” for a coding DNA reference sequence<br>
	 * “m.” for a mitochondrial DNA reference sequence<br>
	 * “n.” for a non-coding DNA reference sequence<br>
	 * “r.” for an RNA reference sequence (transcript)<br>
	 * “p.” for a protein reference sequence<br>
	 * <br>
	 * http://varnomen.hgvs.org/recommendations/general/<br>
	 * @return
	 */
	private String getHgvscPrefix() {
		return iso.ismRNAFromCds() ? "c." : "n.";
	}

	/**
	 * 返回HGVSc的值
	 * @return
	 */
	public String getHgvsc() {
		String start = getStartPosCis();
		String end = getEndPosCis();
		String position = start;
		if (!start.equals(end)) {
			position = start + "_" + end;
		}
		return getHgvscPrefix() + position + getHGVStail();
	}
	
	/** 获得起点的 HGVSc */
	@VisibleForTesting
	public String getStartPosCis() {
		if (getCoverGeneType() != EnumCoverGene.Not) {
			return snpInfo.getStartPosition() + "";
		} else {
			//暂时不清楚n怎么弄
			//http://varnomen.hgvs.org/bg-material/numbering/
			return getCodeStartInMRNA();
		}
	}
	/** 获得终点的 HGVSc */
	@VisibleForTesting
	public String getEndPosCis() {
		if (getCoverGeneType() != EnumCoverGene.Not) {
			return snpInfo.getEndPosition() + "";
		} else {
			//暂时不清楚n怎么弄
			//http://varnomen.hgvs.org/bg-material/numbering/
			return getCodeEndInMRNA();
		}
	}
	
	private EnumCoverGene getCoverGeneType() {
		if (snpInfo.getStartPosition() < iso.getStartAbs() && snpInfo.getEndPosition() < iso.getStartAbs()
				|| snpInfo.getStartPosition() > iso.getEndAbs() && snpInfo.getEndPosition() > iso.getEndAbs()
				) {
			return EnumCoverGene.Out_of_Gene;
		} else if (snpInfo.getStartPosition() < iso.getStartAbs() && snpInfo.getEndPosition() > iso.getEndAbs()) {
			return EnumCoverGene.Cover_Gene;
		} else {
			return EnumCoverGene.Not;
		}
	}
	
	/**
	 * 获得hgvs尾部的信息
	 * 譬如 c.1234_1235[delinsTG]
	 * 中括弧中的部分
	 * @return
	 */
	//TODO 待测试
	@VisibleForTesting
	public String getHGVStail() {
		if (snpInfo.getVarType() == EnumHgvsVarType.Substitutions) {
			return getRefSeqIso() + ">" + getAltSeqIso();
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
			return "del";
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Insertions) {
			return "ins" + getAltSeqIso();
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Duplications) {
			return "dup";
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Indels) {
			return "delins" + getAltSeqIso();
		} else {
			throw new ExceptionNBCSnpHgvs("does not support varation type " + snpInfo.getVarType());
		}
	}
	
	/** 根据方向来提取序列 */
	private String getRefSeqIso() {
		SeqFasta seqFasta = new SeqFasta(snpInfo.getSeqRef());
		if (!iso.isCis5to3()) {
			seqFasta = seqFasta.reservecom();
		}
		return seqFasta.toString();
	}
	/** 根据方向来提取序列 */
	private String getAltSeqIso() {
		SeqFasta seqFasta = new SeqFasta(snpInfo.getSeqAlt());
		if (!iso.isCis5to3()) {
			seqFasta = seqFasta.reservecom();
		}
		return seqFasta.toString();
	}
	
	private String getCodeStartInMRNA() {
		int coord = iso.isCis5to3() ? snpInfo.getStartPosition() : snpInfo.getEndPosition();
		return iso.getCodLoc(coord) == GffIso.COD_LOC_OUT ? getCodOutMRNA(coord) : getCodInMRNA(coord);
	}
	
	private String getCodeEndInMRNA() {
		int coord = iso.isCis5to3() ? snpInfo.getEndPosition() : snpInfo.getStartPosition();
		return iso.getCodLoc(coord) == GffIso.COD_LOC_OUT ? getCodOutMRNA(coord) : getCodInMRNA(coord);
	}
	
	private String getCodOutMRNA(int coord) {
		int[] result = getCodOutOfGene2AtgUag(coord);
		String prefix = result[0] <0 ? "" : "*";
		String prefixOut = result[0] <0 ? "u" : "d";
		String append = result[1] > 0 ? "+" : "-";
		return prefix + result[0] + append + prefixOut + Math.abs(result[1]);
	}
	
	private String getCodInMRNA(int coord) {
		int[] result = iso.getCod2UAG(coord) <= 0 ?  getCod2AtgUagMRNA(coord, true) : getCod2AtgUagMRNA(coord, false);
		String prefix = iso.getCod2UAG(coord) <= 0 ? "" : "*";
		String append = "";
		if (result[1] > 0) {
			append = "+" + result[1];
		} else if (result[1] < 0) {
			append = result[1] + "";
		}
		return prefix + result[0] + append;
	}
	
	/** 必须为mRNA才能计算, coord 在基因外部<br>
	 * 参考网址：<br>
	 * http://www.hgvs.org/mutnomen/disc.html#frameshift<br>
	 * <br>
	 * -N-uM = nucleotide M 5' (upstream) of the nucleotide -N of the transcription initiation site -N (e.g. -237-u5A>G)<br>
	 * NOTE: restricted to nucleotides 5' of the transcription initiation site (cap site, i.e. upstream of the gene incl. the promoter)<br>
	 * N+dM = nucleotide M 3' (downstream) of the nucleotide transcription termination site *N (e.g. *237+d5A>G)<br>
	 * NOTE: restricted to locations 3' of the polyA-addition site (downstream of the gene)<br>
	 * @param coord<br>
	 * @return int[2]<br>
	 * 0: 距离ATG/UAG多少位的cds, 负数表示距离ATG，正数表示距离UAG
	 * 1: 当在intron中时，距离头部还是尾部的exon边界
	 * 两者合并即为 123-4，即exon的边界距离ATG123bp，位点在intron中，距离exon的3'边界4bp
	 */
	private int[] getCodOutOfGene2AtgUag(int coord) {
		int num = iso.getNumCodInEle(coord);
		//外显子上时
		if (num != 0) {
			throw new RuntimeException();
		}
		if (!iso.ismRNAFromCds()) {
			//非cds暂时不知道怎么做
			return null;
		}
		int cod2Site = 0, cod2Bound = 0;
		if (iso.isCis5to3() && coord < iso.getStart()
				|| !iso.isCis5to3() && coord > iso.getStart()
				) {
			cod2Site = iso.getCod2ATGmRNA(iso.getStart());
			cod2Bound = -Math.abs(coord - iso.getStart());
		}	else if (iso.isCis5to3() && coord > iso.getEnd()
				|| !iso.isCis5to3() && coord < iso.getEnd()
				) {
			cod2Site = iso.getCod2UAGmRNA(iso.getEnd());
			cod2Bound = Math.abs(coord - iso.getEnd());
		}
		
		return new int[]{cod2Site, cod2Bound};
	}
	
	/** 必须为mRNA才能计算
	 * @param coord
	 * @return int[2]
	 * 0: 距离ATG/UAG多少位的cds
	 * 1: 当在intron中时，距离头部还是尾部的exon边界
	 * 两者合并即为 123-4，即exon的边界距离ATG123bp，位点在intron中，距离exon的3'边界4bp
	 */
	private int[] getCod2AtgUagMRNA(int coord, boolean isAtg) {
		int num = iso.getNumCodInEle(coord);
		//外显子上时
		if (num > 0) {
			return new int[]{getCodeToAtgUag(coord, isAtg), 0};
		}
		//内含子上时，加1是因为这个返回的距离是距离内含子边界，而我现在要的是距离外显子，因此需要加上1
		int cod2BoundStart = iso.getCod2ExInStart(coord)+1;
		int cod2BoundEnd = iso.getCod2ExInEnd(coord)+1;
		int cod2Atg = 0, cod2Bound = 0;
		if (iso.isCis5to3() && cod2BoundStart <= cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord - cod2BoundStart, isAtg);
			cod2Bound = cod2BoundStart;
		} else if (!iso.isCis5to3() && cod2BoundStart <= cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord + cod2BoundStart, isAtg);
			cod2Bound = cod2BoundStart;
		} else if (iso.isCis5to3() && cod2BoundStart > cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord + cod2BoundEnd, isAtg);
			cod2Bound = -cod2BoundEnd;
		} else if (!iso.isCis5to3() && cod2BoundStart > cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord - cod2BoundEnd, isAtg);
			cod2Bound = -cod2BoundEnd;
		}
		return new int[]{cod2Atg, cod2Bound};
	}
	
	private int getCodeToAtgUag(int coord, boolean isAtg) {
		int distance =  isAtg ? iso.getCod2ATGmRNA(coord) : iso.getCod2UAGmRNA(coord);
		if (distance >= 0 && isAtg) distance = distance + 1;
		return distance;
	}
	
	//============= 氨基酸替换 ==============================
	
	
}

/** 内部使用的，主要就是看这个区段覆盖的范围 */
enum EnumCoverGene {
	Cover_Gene,
	Out_of_Gene,
	Not;
}
