package com.novelbio.software.snpanno;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.CodeInfo;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqHashInt;
import com.novelbio.bioinfo.gff.GffIso;

public abstract class SnpIsoHgvsp {
	private static final Logger logger = LoggerFactory.getLogger(SnpIsoHgvsp.class);
	//TODO 可以对蛋白查找做缓存
//	ConcurrentWeakKeyHashMap<K, V>
	
	Set<EnumVariantClass> setVarType = new LinkedHashSet<>();
	SnpInfo snpInfo;
	RealignUnit realignUnit;
	
	GffIso iso;
	
	boolean isNeedAA3 = true;
	
	/** 需要将alt替换ref的碱基，这里记录替换ref的起点 */
	int snpOnReplaceLocStart;
	/** 需要将alt替换ref的碱基，这里记录替换ref的终点 */
	int snpOnReplaceLocEnd;
	
	/** 如果引起了氨基酸变化，则该start所在读码框三联密码子的起点坐标 */
	int startCds;
	/** 如果引起了氨基酸变化，则该end所在读码框三联密码子的终点坐标 */
	int endCds;
	
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta refSeqNrForAA;
	/** 全长蛋白序列，主要用于查找蛋白层面的duplicate */
	SeqFasta aa;
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta altSeqNrForAA;
	
	public SnpIsoHgvsp(SnpInfo snpRefAltInfo, GffIso iso) {
		this.snpInfo = snpRefAltInfo;
		RealignUnit realignUnit = snpRefAltInfo.getRealignUnit();
		if (realignUnit != null) {
			this.realignUnit = realignUnit.clone();
		}
		this.iso = iso;
	}
	
	/** 根据氨基酸的情况重新align
	 * 主要用于 ref: CAC-ATG-ATA
	 * CAC-AT[AT]G-ATA
	 * CAC[AT]-ATG-ATA
	 * 这种修正
	 */
	protected void realignByAA() {
		int moveNumber = moveBeforeNum();
		if (moveNumber > 0) {
			snpInfo.moveAlign(moveNumber, iso.isCis5to3());
		}
		RealignUnit realignUnit = snpInfo.getRealignUnit();
		if (realignUnit != null) {
			this.realignUnit = realignUnit.clone();
		}
	}
	
	/** 默认返回三字母，可以设定为返回单字母 */
	public void setNeedAA3(boolean isNeedAA3) {
		this.isNeedAA3 = isNeedAA3;
	}
	protected int getStartCis() {
		return iso.isCis5to3() ? snpInfo.getStartReal() : snpInfo.getEndReal();
	}
	protected int getEndCis() {
		return iso.isCis5to3() ? snpInfo.getEndReal() : snpInfo.getStartReal();
	}
	protected int getStartAbs() {
		return snpInfo.getStartReal();
	}
	protected int getEndAbs() {
		return snpInfo.getEndReal();
	}
	
	/** 是否需要氨基酸变化注释，有些在内含子中的就不需要氨基酸变化注释 */
	public boolean isNeedHgvsp() {
		if (!iso.ismRNAFromCds()) {
			return false;
		}
		if (realignUnit != null) {
			snpInfo.setRealignUnit(realignUnit.clone());
		}
		return isNeedHgvspDetail();
	}
	/** 是否需要氨基酸变化注释，有些在内含子中的就不需要氨基酸变化注释 */
	protected abstract boolean isNeedHgvspDetail();
	
	private void initial(SeqHashInt seqHash) {
		setStartEndCis();
		setSiteReplace();
		fillRefAltNrForAA(seqHash);
	}
	
	/** 如果不需要hgvsp，则返回"" */
	public String fillAndGetHgvsp() {
		if (!isNeedHgvsp()) {
			return ""; 
		}
		try {
			return getSnpChange();
		} catch(ExceptionNBCSnpHgvsIsoError e) {
			logger.error("error on " + snpInfo.toString() +"\nerror msg: " + e.getMessage());
			throw e;
		} catch (ExceptionNBCSnpHgvs e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionNBCSnpHgvs("get hgvsp error " + snpInfo.toString(), e);
		}
	}
	protected abstract String getSnpChange();
	/** 返回氨基酸极性变化*/
	public String getAAattrConvert() {
		return "";
	}

	/**
	 * 必须在 {@link #fillAndGetHgvsp()} 调用之后再使用
	 * @return
	 */
	public Set<EnumVariantClass> getSetVarType() {
		return setVarType;
	}
	
	/**
	 * 检测是否需要回移一次
	 * 如果是duplicate的类型，将数据往回移动一次，主要用于剪接位点gt-ag这块<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param isDupMoveLast 默认不移动
	 */	
	protected abstract int moveBeforeNum();

	/** 把refNr和altNr都准备好 */
	protected void fillRefAltNrForAA(SeqHashInt seqHash) {
		GffIso isoSub = extractIsoSub(iso, startCds, endCds);
		if (isoSub.isEmpty()) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpInfo.toString());
		}
		/** 很可能是蛋白层面的duplicate */
		if (isGetAllLenAA()) {
			GffIso isoAA = iso.getSubGffGeneIso(iso.getATGsite(), iso.getUAGsite());
			aa = seqHash.getSeq(isoAA, false);
			int startAA = iso.getCod2ATGmRNA(startCds);
			int endAA = iso.getCod2ATGmRNA(endCds);
			refSeqNrForAA = aa.getSubSeq(startAA+1, endAA+1, true);
		} else {
			refSeqNrForAA = seqHash.getSeq(isoSub, false);		
		}
		altSeqNrForAA = replaceSnpIndel(getSeqAltNrForAA(), snpOnReplaceLocStart, snpOnReplaceLocEnd);
		
		if (refSeqNrForAA.Length()<3) {
			throw new ExceptionNBCSnpHgvsIsoError("aa anotation error on iso " + iso.getName()+ " due to site error!"
					+ "\nsnpInfo: "+ snpInfo.toString() + " reference is " + refSeqNrForAA.toString() + ", and alt is "+altSeqNrForAA.toString());
		}
	}
	
	private GffIso extractIsoSub(GffIso iso, int startCds, int endCds) {
		GffIso isoSub = iso.getSubGffGeneIso(startCds, endCds);
		if (endCds > 0 &&
				(isoSub.isCis5to3() && isoSub.getEnd() < endCds || !isoSub.isCis5to3() && isoSub.getEnd() > endCds)) {
			int length = Math.abs(endCds-iso.getEnd());
			if (length > 1000) {
				throw new RuntimeException();
			}
			isoSub.extendEnd(length);
		}
		return isoSub;
	}
	
	/** 必须在 {@link #setStartEndCis()} 运行之后调用
	 * 用于蛋白层面的duplication
	 * @return
	 */
	protected abstract boolean isGetAllLenAA();
	
	/**
	 * 根据需求将AA1转换成AA3
	 * @param AA1
	 * @return
	 */
	protected String convertAA(String AA1) {
		if (isNeedAA3) {
			char[] info = AA1.toCharArray();
			StringBuilder sBuilderResult = new StringBuilder();
			for (char c : info) {
				sBuilderResult.append(CodeInfo.convertToAA3(Character.toString(c)));
			}
			return sBuilderResult.toString();
		}
		return AA1;
	}
	/**
	 * 根据需求将AA1转换成AA3
	 * @param AA1
	 * @return
	 */
	protected String convertAA(char AA1) {
		String AA1str = AA1 + "";
		if (isNeedAA3) {
			return CodeInfo.convertToAA3(AA1str);
		}
		return AA1str;
	}
	
	protected abstract void setStartEndCis();
	
	/**
	 * 设置 {@link #snpOnReplaceLocStart}
	 * 和 {@link #snpOnReplaceLocEnd}
	 */
	protected abstract void setSiteReplace();

	/**
	 * 跟方向相关
	 * 给定序列和起始位点，用snp位点去替换序列，同时将本次替换是否造成移码写入orfshift
	 * @param thisSeq 给定序列--该序列必须是正向，然后
	 * @param cis5to3 给定序列的正反向
	 * @param startLoc  实际位点 在序列的哪一个点开始替换，替换包括该位点 0表示插到最前面。1表示从第一个开始替换
	 * 如果ref为""，则将序列插入在startBias那个碱基的后面
	 * @param endLoc 实际位点 在序列的哪一个点结束替换，替换包括该位点
	 * 如果 startLoc > endLoc 说明是<b>插入</b>，并且插入位点紧挨着start位点之后<br>
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = refSeqNrForAA.clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace);
		//修改移码
		return seqFasta;
	}
	
	/** 返回输入的位点在第几个氨基酸上，如果在cds前则返回负数
	 * 从1开始计算
	 * @param coord
	 * @return
	 */
	protected int getAffectAANum(int coord) {
//		if (iso == null || iso.getCodLocUTRCDS(coord) != GffGeneIsoInfo.COD_LOCUTR_CDS) {
//			return -1;
//		}
		int num = iso.getCod2ATGmRNA(coord);
		return num/3 + 1;
	}
	
	/**
	 * 返回alt的序列，用于生成protein的替换ref的序列转AA使用
	 * @return
	 */
	protected String getSeqAltNrForAA() {
		String seq = snpInfo.getSeqAlt();
		if (!iso.isCis5to3()) {
			seq = SeqFasta.reverseComplement(seq);
		}
		return seq;
	}
	
	public static SnpIsoHgvsp generateSnpRefAltHgvsp(SnpInfo snpRefAltInfo, GffIso iso, SeqHashInt seqHash) {
		SnpIsoHgvsp snpIsoHgvsp = generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		snpIsoHgvsp.realignByAA();
		
		if (snpIsoHgvsp.isNeedHgvsp()) {
			snpIsoHgvsp.initial(seqHash);
		}
		return snpIsoHgvsp;
	}
	
	/** 仅用于测试，正式项目不能使用 */
	@VisibleForTesting
	protected static SnpIsoHgvsp generateSnpRefAltHgvsp(SnpInfo snpInfo, GffIso iso) {
		SnpIsoHgvsp snpIsoHgvsp = null;
		int refLen = snpInfo.getSeqRef().length();
		int altLen = snpInfo.getSeqAlt().length();
		if (refLen == 1 && altLen == 1) {
			snpIsoHgvsp = new SnpIsoHgvspSnp(snpInfo, iso);
		} else if (refLen == 0 && altLen >= 1) {
			snpIsoHgvsp = new SnpIsoHgvspIns(snpInfo, iso);
		} else if (refLen >= 1 && altLen == 0) {
			snpIsoHgvsp = new SnpIsoHgvspDel(snpInfo, iso);
		} else if (refLen >= 1 && altLen >= 1) {
			snpIsoHgvsp = new SnpIsoHgvspIndel(snpInfo, iso);
		}
		if (snpIsoHgvsp == null) {
			throw new ExceptionNBCSnpHgvs("cannot find such indel conditon " + snpInfo.toString());
		}
		return snpIsoHgvsp;
	}
	
	/** 读码框外的插入改变
	 * 
	 * @param isExtend 是否为UAG之后延长，譬如 Ter225TyrextTer3，有ext字符
	 * @param isDelete 是否涉及Ref-Change-Alt. True 表示直接删除了氨基酸，就没有change事件了
	 * @return
	 */
	protected String getInDelChangeFrameShift(boolean isExtend, boolean isDelete) {
		char[] refSeq = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altSeqChr = altSeqNrForAA.toStringAA1().toCharArray();
		
		if (refSeq.length >0 && altSeqChr.length > 0 && refSeq[0] == '*' && altSeqChr[0] == '*') {
			setVarType.remove(EnumVariantClass.stop_lost);
			setVarType.add(EnumVariantClass.stop_retained_variant);
			return convertAA(refSeq[0]) + getAffectAANum(startCds) + "=";
		}
		int terNumAlt = getTerNum(altSeqChr);
		int terNumRef = getTerNum(refSeq);

		//如果为 p.Val1106ValfsTer15
		//则需要向后延长一位为 p.Asn1107ProfsTer14
		//就是不能氨基酸不变化
		int num = 0;
		for (; num < Math.min(refSeq.length, altSeqChr.length); num++) {
			if (refSeq[num] != altSeqChr[num]) {
				break;
			}
		}
		/**
		 * 发现了这种场景，是一个移码突变
		 * ref : H *
		 * alt : H * L
		 * 就是说移码前和移码后结果一样
		 * 这种情况单独拿出来处理
		 */
		if (num >= refSeq.length) {
			if (refSeq[refSeq.length-1] == '*') {
				setVarType.clear();
				StringBuilder sBuilder = new StringBuilder();
				sBuilder.append(convertAA(refSeq[0]));
				sBuilder.append(getAffectAANum(startCds));
				sBuilder.append("=");
				return sBuilder.toString();
			} else {
				logger.error("aa anotation may error on iso " + iso.getName()
						+ "\nsnpInfo: "+ snpInfo.toString());
				num = num-1;
			}
		}
		
		int terNumAltReal = terNumAlt - num;
		//如果是 TerfsTer3，则计算位点从Ter开始算1
		//如果是Ter225TyrextTer3，则计算位点从原来Ter的后一位开始算1
		if (isExtend) {
			terNumAltReal = terNumAlt - terNumRef;
		}
		String ter = terNumAlt>0 ? terNumAltReal+"" : "?";
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refSeq[num]));
		sBuilder.append(getAffectAANum(startCds)+num);
		if (!isDelete && altSeqChr.length > 0) {
			sBuilder.append(convertAA(altSeqChr[num]));
		} else {
			//仅用于ter删除
			if (terNumRef > 1) {
				sBuilder.append("_"+convertAA(refSeq[terNumRef-1]));
				sBuilder.append(getAffectAANum(startCds)+terNumRef-1);
			}
			sBuilder.append("del");
		}
		if (isExtend) {
			sBuilder.append("ext");
		} else {
			sBuilder.append("fs");
		}
		sBuilder.append(convertAA("*"));
		sBuilder.append(ter);
		return sBuilder.toString();
	}
	
	private int getTerNum(char[] aaSeqChr) {
		int terNum = 0;
		boolean isHaveTer = false;
		for (char aaChar : aaSeqChr) {
			terNum++;
			if (aaChar == '*') {
				isHaveTer = true;
				break;
			}
		}
		return isHaveTer? terNum : 0;
	}
	
	@VisibleForTesting
	protected static int getStartSame(char[] refAA, char[] altAA) {
		int startNum = 0;
		int len = Math.min(refAA.length, altAA.length);
		for (int i = 0; i < len; i++) {
			if (refAA[i] != altAA[i]) {
				break;
			}
			startNum++;
		}
		return startNum;
	}
	
	@VisibleForTesting
	protected static int getEndSame(char[] refAA, char[] altAA) {
		int endNum = 0;
		int len = Math.min(refAA.length, altAA.length);
		for (int i = 1; i <= len; i++) {
			if (refAA[refAA.length-i] != altAA[altAA.length-i]) {
				break;
			}
			endNum++;
		}
		return endNum;
	}
	
	protected static int[] getValidRange(int[] startEnd, int[] atgUag) {
		int[] startend = new int[] {Math.min(startEnd[0], startEnd[1]), Math.max(startEnd[0], startEnd[1])};
		int[] atguag = new int[] {Math.min(atgUag[0], atgUag[1]), Math.max(atgUag[0], atgUag[1])};
		int[] result = new int[] {Math.max(startend[0], atguag[0]), Math.min(startend[1], atguag[1])};
		if(result[1] < result[0]) {
			return null;
		}
		return result;
	}
	
	//============= 氨基酸层面的duplication =================
	/**
	 * @param indelAA
	 * @param startAA 实际起点，从1开始计算
	 * @param endAA 实际终点
	 * @return
	 */
	protected String getDeletionDuplicate(String indelAA, int startAA, int endAA) {
		String aaSeq = aa.toStringAA1();
		char[] aaChr = aaSeq.toCharArray();
		StringBuilder sBuilder = new StringBuilder(endAA-startAA+10);
		for (int i = startAA-1; i < endAA; i++) {
			sBuilder.append(aaChr[i]);
		}
		if (!indelAA.equals(sBuilder.toString())) {
			throw new RuntimeException();
		}
		
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("", startAA, endAA), indelAA, "", "");
		snpIndelRealignHandle.handleSeqAlign(new SeqHashAAforHgvs(aaSeq));
		snpIndelRealignHandle.moveAlignToAfter();
		Align reAlign = snpIndelRealignHandle.getRealign();
		StringBuilder sBuilderResult = new StringBuilder();
		
		sBuilderResult.append(convertAA(aaChr[reAlign.getStartAbs()-1]));
		sBuilderResult.append(reAlign.getStartAbs());
		if (reAlign.getLength() > 1) {
			sBuilderResult.append("_");
			sBuilderResult.append(convertAA(aaChr[reAlign.getEndAbs()-1]));
			sBuilderResult.append(reAlign.getEndAbs());
		}
		sBuilderResult.append("del");
		return sBuilderResult.toString();
	}
	
	protected String getInsertionDuplicate(String indelAA, int startAA, int endAA) {
		String aaSeq = aa.toStringAA1();
		char[] aaChr = aaSeq.toCharArray();
		
		Align reAlign = new Align("", startAA, endAA);
		SnpIndelRealignHandle snpIndelRealignHandle = null;
		if (!indelAA.contains("*")) {
			snpIndelRealignHandle = new SnpIndelRealignHandle(reAlign, "", indelAA, "");
			snpIndelRealignHandle.handleSeqAlign(new SeqHashAAforHgvs(aaSeq));
			snpIndelRealignHandle.moveAlignToAfter();
			reAlign = snpIndelRealignHandle.getRealign();
		}

		StringBuilder sBuilderResult = new StringBuilder();
		if (snpIndelRealignHandle != null && snpIndelRealignHandle.isDup()) {
			int startDup = reAlign.getStartAbs()-indelAA.length()+1;
			int endDup = reAlign.getStartAbs();
			sBuilderResult.append(convertAA(aaChr[startDup - 1]));
			sBuilderResult.append(startDup);
			if (endDup > startDup) {
				sBuilderResult.append("_");
				sBuilderResult.append(convertAA(aaChr[endDup-1]));
				sBuilderResult.append(endDup);
			}
			sBuilderResult.append("dup");
		} else {
			sBuilderResult.append(convertAA(aaChr[reAlign.getStartAbs()-1]));
			sBuilderResult.append(reAlign.getStartAbs());
			sBuilderResult.append("_");
			sBuilderResult.append(convertAA(aaChr[reAlign.getEndAbs()-1]));
			sBuilderResult.append(reAlign.getEndAbs());
			sBuilderResult.append("ins");
			sBuilderResult.append(convertAA(indelAA));
		}
		return sBuilderResult.toString();
	}
	
}
