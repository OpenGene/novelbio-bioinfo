package com.novelbio.software.coordtransform;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;

/** mummer的一对比较，或者是liftover的一个chain
 * 
 * 其中 ref 是 query的基因组
 * alt 是 subject的基因组
 * 需要将 ref的坐标转化为 alt的坐标
 * @author novelbio
 *
 */
public class CoordPair implements Alignment {
	/** 给数据库用的,一般用不到 */
	String id;
	
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
	/** 给数据库用的,一般用不到 */
	public void setId(String id) {
		this.id = id;
	}
	/** 给数据库用的,一般用不到 */
	public String getId() {
		return id;
	}
	/**
	 * liftover 的 chain 文件需要
	 * 如果是读取的liftover文件，则不需要设置这个
	 * @param refLen ref染色体的长度
	 * @param altLen alt染色体的长度
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
	public int getChainId() {
		return chainId;
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
	public String getChrId() {
		return alignRef.getChrId();
	}
	public void setIdentity(double identity) {
		this.identity = identity;
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
	/** 仅用于数据库查询 */
	public void setAlignRef(Align alignRef) {
		this.alignRef = alignRef;
	}
	/** 仅用于数据库查询 */
	public void setAlignAlt(Align alignAlt) {
		this.alignAlt = alignAlt;
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
		return alignRef.getChrId();
	}
	public String getChrAlt() {
		return alignAlt.getChrId();
	}
	
	public Align getAlignRef() {
		return alignRef;
	}
	public Align getAlignAlt() {
		return alignAlt;
	}
	
	public boolean isSameChr() {
		return alignRef.getChrId().equals(alignAlt.getChrId());
	}
	
	/** 相似度 * 长度  */
	public Double getScore() {
		double coeff = isSameChr()? 1 : 0.8;
		return identity/100*getLength()*coeff;
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
		lsResult.add(" " + alignRef.getChrId() + "\t" + alignAlt.getChrId() + " ");
		return ArrayOperate.cmbString(lsResult, "  |  ");
	}
	
	/**
	 * refId + altId + refStart + refEnd + altStart + altEnd
	 * @return
	 */
	public String getKey() {
		List<String> lsKey = new ArrayList<>();
		lsKey.add(alignRef.getChrId());
		lsKey.add(alignAlt.getChrId());
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
		lsChain.add(alignRef.getChrId());
		lsChain.add(refLen+"");
		String strandRef = alignRef.isCis() ? "+" : "-";
		lsChain.add(strandRef);
		lsChain.add(alignRef.getStartAbs()-1 + "");
		lsChain.add(alignRef.getEndAbs() + "");

		lsChain.add(alignAlt.getChrId());
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
		if (!align1.getChrId().equals(align2.getChrId())
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

