package com.novelbio.analysis.comparegenomics.coordtransform;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listoperate.BinarySearch;
import com.novelbio.listoperate.BsearchSite;
import com.novelbio.listoperate.BsearchSiteDu;

/** mummer的一对比较，或者是liftover的一个chain */
public class CoordPair implements Alignment {
	long refLen;
	long altLen;
	int chainId;
	
	Align alignRef;
	Align alignAlt;
	/** 0-100 */
	double identity;
		
	List<IndelForRef> lsIndel = new ArrayList<>();
	
	/**
	 * 将mummer的一行转换成对象<br>
	 *     1001 10053455  |        1 10052411  | 10052455 10052411  |    99.99  | 1	1<br>
	 * @param mummerLine
	 */
	public void initialMummer(String mummerLine) {
		String[] ss = mummerLine.split("\\|");
		String[] chrId = ss[4].trim().split("\t");
		String[] refloc = ss[0].trim().split(" +");
		String[] altloc = ss[1].trim().split(" +");
		
		alignRef = new Align(chrId[0], Integer.parseInt(refloc[0]), Integer.parseInt(refloc[1]));
		alignAlt = new Align(chrId[1], Integer.parseInt(altloc[0]), Integer.parseInt(altloc[1]));
		identity = Double.parseDouble(ss[3].trim());
	}
	/**
	 * liftover 的 chain 文件需要
	 * 如果是读取的liftover文件，则不需要设置这个
	 * @param refLen
	 * @param altLen
	 */
	public void setRefAltLen(long refLen, long altLen) {
		this.refLen = refLen;
		this.altLen = altLen;
	}
	/**
	 * liftover 的 chain 文件需要
	 * 如果是读取的liftover文件，则不需要设置这个
	 */
	public void setChainId(int chainId) {
		this.chainId = chainId;
	}
	/**
	 * 将liftoverChain的一行转换成对象<br>
	 * chain 20849626768 chr1 248956422 + 10000 248946422 chr1 249250621 + 10000 249240621 2
	 * @param mummerLine
	 */
	public void initialChainLiftover(String liftoverChain) {
		String[] ss = liftoverChain.split(" +");
		alignRef = new Align(ss[2], Integer.parseInt(ss[5])+1, Integer.parseInt(ss[6]));
		alignRef.setCis5to3(ss[4].equals("+"));
		alignAlt = new Align(ss[7], Integer.parseInt(ss[10])+1, Integer.parseInt(ss[11]));
		alignAlt.setCis5to3(ss[9].equals("+"));
		identity = Double.parseDouble(ss[1]);
		
		refLen = Integer.parseInt(ss[3]);
		altLen = Integer.parseInt(ss[8]);
		chainId = Integer.parseInt(ss[12]);
	}
	public void addChainLiftover(String chainLine) {
		String[] ss = chainLine.split("\t");
		int len = Integer.parseInt(ss[0]);

		if (ss.length == 1) {
			int endSiteRef = len + getLastSiteRef();
			if (endSiteRef != alignRef.getEndAbs()) {
				throw new RuntimeException(alignRef.toString() + "end is not equal to " + endSiteRef);
			}
			int endSiteAlt = alignAlt.isCis() ? getLastSiteAlt()+len : getLastSiteAlt()-len;
			if (endSiteAlt != alignAlt.getEndCis()) {
				throw new RuntimeException(alignAlt.toString() + "end is not equal to " + endSiteAlt);
			}
			return;
		}
		
		int gapRef= Integer.parseInt(ss[1]);
		int gapAlt = Integer.parseInt(ss[2]);
		addChainLiftover(len, gapRef, gapAlt);
	}

	public void addChainLiftover(int len, int gapRef, int gapAlt) {
		IndelForRef indel = new IndelForRef(alignAlt.isCis5to3());
		indel.setRefStart(getLastSiteRef() + len);
		indel.setAltStartCis(getLastSiteAlt());
		indel.setAltStartCis(indel.getStartExtendAlt(-len));
		indel.setRefLen(gapRef);
		indel.setAltLen(gapAlt);
		lsIndel.add(indel);
	}
	
	/**
	 * 正数表示ref相对于alt插入
	 * 负数表示ref相对于alt缺失
	 * @param num
	 */
	public void addIndelMummer(int num) {
		if (num == 0) {
			return;
		}
		IndelForRef indel = null;
		if (!lsIndel.isEmpty()) {
			indel = lsIndel.get(lsIndel.size()-1);
		}
		
		if (isNeedNewIdel(num)) {
			indel = new IndelForRef(alignAlt.isCis5to3());
			int numAbs = Math.abs(num)-1;
			indel.setRefStart(getLastSiteRef()+numAbs);
			indel.setAltStartCis(getLastSiteAlt());
			indel.setAltStartCis(indel.getStartExtendAlt(-numAbs));
			lsIndel.add(indel);
		}
		if (num > 0) {
			indel.addRefLen1();
		} else {
			indel.addAltLen1();
		}
	}
	/** 获得上一个site前一位的坐标 */
	private int getLastSiteRef() {
		if (lsIndel.isEmpty()) {
			return alignRef.getStartAbs()-1;
		}
		IndelForRef indelLast = lsIndel.get(lsIndel.size()-1);
		/**
		 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
		 * 1    ca- - - gcat
		 * 1    ca tgc gcat
		 * 因此deletion的计算位点应该向前移一位
		 */
		return  indelLast.getEndExtend(-1);
	}
	/** 获得上一个site前一位的坐标 */
	private int getLastSiteAlt() {
		if (lsIndel.isEmpty()) {
			return alignAlt.isCis5to3() ? alignAlt.getStartCis()-1 : alignAlt.getStartCis() + 1;
		}
		IndelForRef indelLast = lsIndel.get(lsIndel.size()-1);
		/**
		 * deletion
		 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
		 * 1    ca- - - gcat
		 * 1    ca tgc gcat
		 * 因此deletion的计算位点应该向前移一位
		 */
		return indelLast.getEndExtendAlt(-1);
	}
	
	private boolean isNeedNewIdel(int num) {
		if (Math.abs(num) > 1 || lsIndel.isEmpty()) {
			return true;
		}
		IndelForRef indelLast = lsIndel.get(lsIndel.size()-1);
		return num > 0 && !indelLast.isRefInsertion()
				|| num < 0 && indelLast.isRefInsertion();
	}
	
	public List<IndelForRef> getLsIndel() {
		return lsIndel;
	}
	
	@Override
	public Boolean isCis5to3() {
		return true;
	}

	@Override
	public String getRefID() {
		return alignRef.getRefID();
	}
	public double getIdentity() {
		return identity;
	}
	public void setStart(int start) {
		if (!Alignment.isSiteInAlign(alignRef, start)) {
			throw new RuntimeException();
		}
		if (lsIndel.isEmpty() || lsIndel.get(0).getStartAbs() > start) {
			alignRef.setStartAbs(start);
			alignAlt.startAddLenCis(start - alignRef.getStartAbs());
			return;
		}
		
		List<IndelForRef> lsIndelForRefsNew = new ArrayList<>();
		boolean isSet = false;
		IndelForRef indelForRefLast = null;
		for (IndelForRef indelForRef : lsIndel) {
			if (indelForRef.getEndAbs() < start) {
				indelForRefLast = indelForRef;
				continue;
			}
			if (!isSet && indelForRef.getStartAbs() > start) {
				isSet = true;
				alignRef.setStartAbs(start);
				int length = start - indelForRefLast.getEndAbs();
				alignAlt.setStartCis(indelForRefLast.getEndCisAlt());
				alignAlt.startAddLenCis(length);
			}
			
			if (Alignment.isSiteInAlign(indelForRef, start)) {
				isSet = true;
				if (start == indelForRef.getStartAbs()) {
					alignRef.setStartAbs(indelForRef.getStartAbs());
					alignAlt.setStartCis(indelForRef.getStartCisAlt());
				} else {
					alignRef.setStartAbs(indelForRef.getEndAbs());
					alignAlt.setStartCis(indelForRef.getEndCisAlt());
					continue;
				}
			}
			lsIndelForRefsNew.add(indelForRef);
		}
		lsIndel = lsIndelForRefsNew;
	}
	
	public void setEnd(int end) {
		if (!Alignment.isSiteInAlign(alignRef, end)) {
			throw new RuntimeException();
		}
		if (lsIndel.isEmpty() || lsIndel.get(lsIndel.size()-1).getEndAbs() < end) {
			alignRef.setEndAbs(end);
			alignAlt.endAddLenCis(alignRef.getEndAbs()-end);
			return;
		}
		
		boolean isSet = false;
		IndelForRef indelForRefLast = null;
		List<IndelForRef> lsIndelForRefsNew = new ArrayList<>();
		for (IndelForRef indelForRef : lsIndel) {
			
			if (indelForRef.getStartAbs() > end) {
				if (!isSet) {
					alignRef.setEndAbs(end);
					int length = end -indelForRefLast.getEndAbs();
					alignAlt.setEndCis(indelForRefLast.getEndCisAlt());
					alignAlt.endAddLenCis(length);
				}
				break;
			}
			
			if (Alignment.isSiteInAlign(indelForRef, end)) {
				isSet = true;
				if (end == indelForRef.getEndAbs()) {
					alignRef.setEndAbs(indelForRef.getEndAbs());
					alignAlt.setEndCis(indelForRef.getEndCisAlt());
				} else {
					alignRef.setEndAbs(indelForRef.getStartAbs());
					alignAlt.setEndCis(indelForRef.getStartCisAlt());
					break;
				}
			}
			indelForRefLast = indelForRef;
			lsIndelForRefsNew.add(indelForRef);
		}
		lsIndel = lsIndelForRefsNew;
	}

	/** 本比较实际的长度 */
	public int getLength() {
		return Math.max(alignRef.getLength(), alignAlt.getLength());
	}
	
	public String getChrRef() {
		return alignRef.getRefID();
	}
	public String getChrAlt() {
		return alignAlt.getRefID();
	}
	
	public Align getAlignRef() {
		return alignRef;
	}
	public Align getAlignAlt() {
		return alignAlt;
	}
	
	public boolean isSameChr() {
		return alignRef.getRefID().equals(alignAlt.getRefID());
	}
	
	/** 相似度 * 长度  */
	public Double getScore() {
		double coeff = isSameChr()? 1 : 0.8;
		return identity/100*getLength()*coeff;
	}

	
	public VarInfo searchVarInfo(int start, int end) {
		validateSiteInCoord(start);
		validateSiteInCoord(end);
		
		VarInfo varInfo = new VarInfo();
		varInfo.setCis5to3(this.getAlignAlt().isCis5to3());
		varInfo.setChrID(this.getChrAlt());
		
		if (ArrayOperate.isEmpty(lsIndel)) {
			int[] startAlt2Bias = getAltSiteStart(null, start);
			int[] endAlt2Bias = getAltSiteStart(null, end);
			varInfo.setStartCis(startAlt2Bias[0]);
			varInfo.setEndCis(endAlt2Bias[0]);
			varInfo.setStartBias(startAlt2Bias[1]);
			varInfo.setEndBias(endAlt2Bias[1]);
			return varInfo;
		}
		
		
		BinarySearch<IndelForRef> binarySearch = new BinarySearch<>(lsIndel, true);

		//单个位点
		if (start == end) {
			BsearchSite<IndelForRef> bsite = binarySearch.searchLocation(start);
			int[] startAlt2Bias = getAltSiteStart(bsite, start);
			if (startAlt2Bias[1] > 0) {
				return null;
			}
			varInfo.setStartCis(startAlt2Bias[0]);
			varInfo.setEndCis(startAlt2Bias[0]);
			return varInfo;
		}
		
		BsearchSiteDu<IndelForRef> bsiteDu = binarySearch.searchLocationDu(start, end);
		int[] startAlt2Bias = getAltSiteStart(bsiteDu.getSiteLeft(), start);
		int[] endAlt2Bias = getAltSiteEnd(bsiteDu.getSiteRight(), end);
		//区段位于ref相对于alt多的区段
		if (this.getAlignAlt().isCis() && endAlt2Bias[0] < startAlt2Bias[0]
				|| !this.getAlignAlt().isCis() && startAlt2Bias[0] < endAlt2Bias[0]
				) {
			return null;
		}
		varInfo.setStartCis(startAlt2Bias[0]);
		varInfo.setEndCis(endAlt2Bias[0]);
		varInfo.setStartBias(startAlt2Bias[1]);
		varInfo.setEndBias(endAlt2Bias[1]);
		
		List<IndelForRef> lsIndels =bsiteDu.getCoveredElement();
		if (!lsIndels.isEmpty()) {
			varInfo.setLsIndelForRefs(lsIndels);
		}
		return varInfo;
	}
	
	private void validateSiteInCoord(int site) {
		if (!Alignment.isSiteInAlign(this, site)) {
			throw new ExceptionNBCCoordTransformer("refsite " + site + " is not in " + this.toString());
		}
	}
	/**
	 * 获取refsite起点所对应的位置
	 * @param refSite
	 * @return 0：向右偏移之后的起点，1：偏移的bp 
	 */
	public int[] getAltSiteStart(BsearchSite<IndelForRef> bsite, int refSite) {
		return getAltSite(bsite, refSite, true);
	}
	/**
	 * 获取refsite终点所对应的位置
	 * @param refSite
	 * @return 0：向左偏移之后的起点，1：偏移的bp 
	 */
	public int[] getAltSiteEnd(BsearchSite<IndelForRef> bsite, int refSite) {
		return getAltSite(bsite, refSite, false);
	}
	/**
	 * 获取refsite起点所对应的位置
	 * @param refSite
	 * @param isStart 是起点还是终点
	 * @return 0：偏移之后的起点，1：偏移的bp
	 * 注意如果是 isStart 则向右偏移， isEnd向左偏移 
	 */
	private int[] getAltSite(BsearchSite<IndelForRef> bsite, int refSite, boolean isStart) {
		int altSite = 0, bias = 0;
		if (bsite == null) {
			int length = refSite - alignRef.getStartAbs();
			altSite = alignAlt.isCis() ? alignAlt.getStartAbs()+length : alignAlt.getEndAbs() - length;
			return new int[] {altSite, bias};
		}
		
		if (!bsite.isInsideLoc()) {
			IndelForRef IndelBefore = bsite.getAlignUp();
			if (IndelBefore == null) {
				int length = refSite - alignRef.getStartAbs();
				altSite = alignAlt.isCis() ? alignAlt.getStartAbs()+length : alignAlt.getEndAbs() - length;
			} else {
				int length = refSite - IndelBefore.getEndAbs();
				altSite = IndelBefore.getEndExtendAlt(length);
			}
		} else {
			IndelForRef indelThis = bsite.getAlignThis();
			if (refSite == indelThis.getStartAbs()) {
				altSite = indelThis.getStartCisAlt();
				bias = 0;
			} else if (refSite == indelThis.getEndAbs()) {
				altSite = indelThis.getEndCisAlt();
				bias = 0;
			} else {
				//在indel里面
				if (isStart) {
					altSite = indelThis.getEndCisAlt();
					bias = indelThis.getEndAbs()-refSite;
				} else {
					altSite = indelThis.getStartCisAlt();
					bias = refSite-indelThis.getStartAbs();
				}
			}
		}
		return new int[] {altSite, bias};
	}
	
	@Override
	public int getStartAbs() {
		return alignRef.getStartAbs();
	}
	
	@Override
	public int getEndAbs() {
		return alignRef.getEndAbs();
	}
	
	@Override
	public int getStartCis() {
		return alignRef.getStartAbs();
	}
	
	@Override
	public int getEndCis() {
		return alignRef.getEndAbs();
	}
	
	/**
	 * 将结果整理成这个样子<br>
	 *     1001 10053455  |        1 10052411  | 10052455 10052411  |    99.99  | 1	1<br>
	 */
	public String toString() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(" " + alignRef.getStartCis() + " " + alignRef.getEndCis() + " ");
		lsResult.add(" " + alignAlt.getStartCis() + " " + alignAlt.getEndCis() + " ");
		lsResult.add(" " + alignRef.getLength() + " " + alignRef.getLength() + " ");
		DecimalFormat df = new DecimalFormat("#.00");
		String identityStr = df.format(identity);
		lsResult.add(" " + identityStr + " ");
		lsResult.add(" " + alignRef.getRefID() + "\t" + alignAlt.getRefID() + " ");
		return ArrayOperate.cmbString(lsResult, "  |  ");
	}
	
	/**
	 * refId + altId + refStart + refEnd + altStart + altEnd
	 * @return
	 */
	public String getKey() {
		List<String> lsKey = new ArrayList<>();
		lsKey.add(alignRef.getRefID());
		lsKey.add(alignAlt.getRefID());
		lsKey.add(alignRef.getStartCis() + "");
		lsKey.add(alignRef.getEndCis() + "");
		lsKey.add(alignAlt.getStartCis() + "");
		lsKey.add(alignAlt.getEndCis() + "");
		return ArrayOperate.cmbString(lsKey, " ");
	}
	
	public String toStringHead() {
		List<String> lsChain = new ArrayList<>();
		lsChain.add("chain");
		int length = 0;
		for (IndelForRef indelForRef : lsIndel) {
			length+=indelForRef.getRefLen();
		}
		if (identity == 0) {
			identity = (1-(double)length/alignRef.getLength()) * 10 * alignRef.getLength();
		}
		lsChain.add((long)identity+"");
		lsChain.add(alignRef.getRefID());
		lsChain.add(refLen+"");
		String strandRef = alignRef.isCis() ? "+" : "-";
		lsChain.add(strandRef);
		lsChain.add(alignRef.getStartAbs()-1 + "");
		lsChain.add(alignRef.getEndAbs() + "");

		lsChain.add(alignAlt.getRefID());
		lsChain.add(altLen+"");
		String strandAlt = alignAlt.isCis() ? "+" : "-";
		lsChain.add(strandAlt);
		lsChain.add(alignAlt.getStartAbs()-1 + "");
		lsChain.add(alignAlt.getEndAbs() + "");
		lsChain.add(chainId + "");
		String[] ss = lsChain.toArray(new String[0]);
		return ArrayOperate.cmbString(ss, " ");
	}
	
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public Iterable<String> readPerIndel() {
		Iterator<IndelForRef> itIndels = lsIndel.iterator();
		return new Iterable<String>() {
			IndelForRef indel;
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}
					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					String getLine() {
						if (!itIndels.hasNext()) {
							if (indel == null) {
								return null;
							}
							int length = alignRef.getEndAbs()-indel.getEndExtend(-1);
							indel = null;
							return length+"";
						}
						if (indel == null) {
							indel = itIndels.next();
							return indel.getStartAbs() - alignRef.getStartAbs()+1 + "\t" + indel.getRefLen() + "\t" + indel.getAltLen();
						}
						IndelForRef indelthis = itIndels.next();
						String line = indelthis.getStartAbs() - indel.getEndAbs() +1 + "\t" + indelthis.getRefLen() + "\t" + indelthis.getAltLen();
						indel = indelthis;
						return line;
					}
					String line = getLine();
				};
			}
		};
	}
	
	public boolean isCanAdd(CoordPair coordPair) {
		Align alignRefNext = coordPair.getAlignRef();
		Align alignAltNext = coordPair.getAlignAlt();
		return isAlignSequence(alignRef, alignRefNext) && isAlignSequence(alignAlt, alignAltNext);
	}
	
	/** 在一个list里面，方向都相同的coord可以合并起来 */
	public void addCoordPair(CoordPair coordPair) {
		if (!isCanAdd(coordPair)) {
			throw new ExceptionNBCCoordTransformer("cannot add coord,\n"
					+ "coord1: " + toString() + "\n"
					+ "coord2: " + coordPair.toString() + "\n");
		}

		Align alignRefNext = coordPair.getAlignRef();
		Align alignAltNext = coordPair.getAlignAlt();

		IndelForRef indelForRef = new IndelForRef(alignAlt.isCis());
		indelForRef.setRefStart(alignRef.getEndAbs());
		indelForRef.setAltStartCis(alignAlt.getEndCis());
		indelForRef.setRefLen(alignRefNext.getStartAbs()-alignRef.getEndAbs()-1);
		indelForRef.setAltLen(Math.abs(alignAltNext.getStartCis()-alignAlt.getEndCis())-1);
		lsIndel.add(indelForRef);
		for (IndelForRef indel : coordPair.getLsIndel()) {
			lsIndel.add(indel);
		}
		alignRef.setEndCis(alignRefNext.getEndCis());
		alignAlt.setEndCis(alignAltNext.getEndCis());
	}
	
	private boolean isAlignSequence(Align align1, Align align2) {
		if (!align1.getRefID().equals(align2.getRefID())
				|| align1.isCis5to3() != align2.isCis5to3()
				) {
			return false;
		}
		if (align1.isCis() && align1.getEndAbs() >= align2.getStartAbs()
				|| !align1.isCis() && align1.getStartAbs() <= align2.getEndAbs()
				) {
			return false;
		}
		return true;
	}
	
}

/** 对于ref来说的indel
 * insertion
 * start是插入的具体起点(t)，end是插入的具体终点(c)，都从1开始计算
 * 1    ca tgc gcat
 * 1    ca- - - gcat
 * 
 * deletion
 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
 * 1    ca- - - gcat
 * 1    ca tgc gcat
 * @author zong0jie
 * @data 2018年8月6日
 */
class IndelForRef implements Alignment {
	
	/** refStart一定是从小到大 */
	int refStart;
	
	boolean isAltCis;
	int altStartCis;
	
	/** ref是否为缺失 */
	boolean isRefDel;
	/** 表示alt的长度 */
	int altLen = 0;
	/** 表示ref的长度 */
	int refLen = 0;
	
	public IndelForRef(boolean isAltCis) {
		this.isAltCis = isAltCis;
	}
	/**
	 * 如果ref为插入，则为插入前的位点<br>
	 * ATA[C] ACATCGGCA T<br>
	 * 如果ref为缺失，则为缺失前的位点<br>
	 * ATA[C] -------CGGCA T
	 * @param start
	 */
	public void setRefStart(int start) {
		this.refStart = start;
	}
	/**
	 * 如果alt为插入，则为插入前的位点<br>
	 * ATA[C] ACATCGGCA T<br>
	 * 如果alt为缺失，则为缺失前的位点<br>
	 * ATA[C] -------CGGCA T
	 * @param start
	 */
	public void setAltStartCis(int altStartCis) {
		this.altStartCis = altStartCis;
	}

	/** 跟 {@link #getStartAbs()} 一样<br>
	 *  * insertion<br>
	 * start是插入前的位点，end是插入的后一个位点，都从1开始计算<br>
	 * 1    c[a] tgc [g]cat : alt<br>
	 * 1      ca  - - - gcat : ref<br>
	 * <br>
	 * deletion<br>
	 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 1    ca   tgc  gcat : ref
	 * @return
	 */
	public int getStartCisAlt() {
		return altStartCis;
	}
	/** 跟 {@link #getEndAbs()} 一样<br>
	 *  * insertion<br>
	 * start是插入前的位点，end是插入的后一个位点，都从1开始计算<br>
	 * 1    c[a] tgc [g]cat : alt<br>
	 * 1      ca  - - - gcat : ref<br>
	 * <br>
	 * deletion<br>
	 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 1    ca   tgc  gcat : ref
	 * @return
	 */
	public int getEndCisAlt() {
		return isAltCis ? altStartCis + altLen+1 : altStartCis - altLen-1;
	}
	public boolean isAltCis() {
		return isAltCis;
	}
	/** end向后延长n为多少，注意不修改值<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 延长3bp变成<br>
	 * 1    c[a]- - - gca[t] : alt
	 * @param num
	 * @return
	 */
	public int getEndExtendAlt(int num) {
		return isAltCis() ? getEndCisAlt() + num : getEndCisAlt()-num;
	}
	/** start向前延长n为多少，注意不修改值
	 * 1    atgc[a]- - - [g]cat : alt
	 * 延长3bp变成
	 * 1    a[t]gca- - - gca[t] : alt
	 * @param num
	 * @return
	 */
	public int getStartExtendAlt(int num) {
		return isAltCis() ? getStartCisAlt() - num : getStartCisAlt()+num;
	}
	/** end向后延长n为多少，注意不修改值<br>
	 * 1    c[a]- - - [g]cat : ref<br>
	 * 延长3bp变成<br>
	 * 1    c[a]- - - gca[t] : ref
	 * @param num
	 * @return
	 */
	public int getEndExtend(int num) {
		return getEndAbs() + num;
	}
	/** start向前延长n为多少，注意不修改值<br>
	 * 1    atgc[a]- - - [g]cat : ref<br>
	 * 延长3bp变成<br>
	 * 1    a[t]gca- - - gca[t] : ref
	 * @param num
	 * @return
	 */
	public int getStartExtend(int num) {
		return getStartAbs() - num;
	}
	public void setRefLen(int refLen) {
		this.refLen = refLen;
	}
	public void setAltLen(int altLen) {
		this.altLen = altLen;
	}
	public void addAltLen1() {
		altLen++;
	}
	public int getAltLen() {
		return altLen;
	}
	
	public void addRefLen1() {
		refLen++;
	}
	public int getRefLen() {
		return refLen;
	}
	
	@Deprecated
	public int getLength() {
		return refLen;
	}
	/**
	 * 注意 {@link #isRefInsertion()} 和 {@link #isAltInsertion()}
	 * 可以同时为true，表示这个就是一个替换
	 * true: ref相对于alt为插入<br>
	 * false: ref相对于alt没插入<br>
	 * @return
	 */
	public boolean isRefInsertion() {
		return refLen > 0;
	}
	
	/**
	 * true: alt相对于ref为插入<br>
	 * false: alt相对于ref为缺失<br>
	 * @return
	 */
	public boolean isAltInsertion() {
		return altLen > 0;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		if (!super.equals(obj)) {
			return false;
		}
		IndelForRef otherAlign = (IndelForRef)obj;
		return altStartCis == otherAlign.altStartCis && altLen == otherAlign.altLen
				&& isAltCis == otherAlign.isAltCis && refLen == otherAlign.refLen;
	}

	/**
	 * 如果ref为插入，则为插入前的位点
	 * ATA[C] ACATCGGCA T
	 * 如果ref为缺失，则为缺失前的位点
	 * ATA[C] -------CGGCA T
	 */
	@Override
	public int getStartAbs() {
		return refStart;
	}
	/**
	 * 如果ref为插入，则为插入后的位点
	 * ATAC ACAT  [C]GGCA
	 * 如果ref为缺失，则为缺失后的位点
	 * ATAC -------[C]GGCA
	 */
	@Override
	public int getEndAbs() {
		return refStart+refLen+1;
	}

	@Override
	public int getStartCis() {
		return getStartAbs();
	}

	@Override
	public int getEndCis() {
		return getEndAbs();
	}

	@Override
	public Boolean isCis5to3() {
		return true;
	}

	@Override
	public String getRefID() {
		return null;
	}
	
}

