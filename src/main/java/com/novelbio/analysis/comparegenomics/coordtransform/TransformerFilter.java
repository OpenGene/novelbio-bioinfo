package com.novelbio.analysis.comparegenomics.coordtransform;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listoperate.BinarySearch;
import com.novelbio.listoperate.BsearchSite;
import com.novelbio.listoperate.BsearchSiteDu;

/**
 * 比较基因组处理MUMMER的结果<br>	
 * <br>
 * 思路，按照score把一对一对的比较装到list中，从大到小排序<br>
 * 然后把一对一对的compare放到list中。<br>
 * <br>
 * 如果后出现的和新出现的overlap了，则截取没有overlap的部分放进去<br>
 * 如果后出现的和老的完全覆盖，无论是后出现覆盖老的还是相反，统统跳过<br>
 * <br>
 * 因为后出现覆盖老的只有一种场景<br>
 * 新元素覆盖老元素的场景，老元素是chr1-vs-chr1，新元素是另一条染色体 chr1-vs-chr2<br>
 * 因为染色体如果不一致，会对元素降权重。这时候如果chr1某个区域同时比对上了 chr1 和chr2，<br>
 * 就可能出现chr2明明比chr1比对的区段长，但是还是优先chr1<br>
 * 这时候新元素就覆盖老元素，但是一样跳过<br>
 * 
 * @author zong0jie
 * @data 2018年8月5日
 */
public class TransformerFilter {
	
	LinkedList<CoordPair> lsPairs;
	List<CoordPair> lsPairsResult = new ArrayList<>();
	
	public static void main(String[] args) {
		CoordPairReader coordPairReader = new CoordPairReader("/media/winE/mywork/coordTransform/GRCh38VShg19/GRCh38VShg19.coords");
		coordPairReader.setIdentityCutoff(99.00);
		String resultFile = "/media/winE/mywork/coordTransform/GRCh38VShg19/GRCh38VShg19.simple.coords";
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		while (coordPairReader.hasNext()) {
			List<CoordPair> lsCoordPairs = coordPairReader.readNext();
			TransformerFilter transformerFilter = new TransformerFilter();
			transformerFilter.setLsPairs(new LinkedList<>(lsCoordPairs));
			transformerFilter.handleLsCoordPairs();
			List<CoordPair> lsCoordResult = transformerFilter.getLsPairsResult();
			for (CoordPair coordPair : lsCoordResult) {
				txtWrite.writefileln(coordPair.toString());
			}
		}
		coordPairReader.close();
		txtWrite.close();
	}
	
	public void setLsPairs(LinkedList<CoordPair> lsPairs) {
		this.lsPairs = lsPairs;
	}

	public List<CoordPair> getLsPairsResult() {
		return lsPairsResult;
	}
	public void handleLsCoordPairs() {
		Collections.sort(lsPairs, (c1, c2)->{return -c1.getScore().compareTo(c2.getScore());});
		LinkedList<CoordPair> lsPairQueue = new LinkedList<>(lsPairs);
		
		while (!lsPairQueue.isEmpty()) {
			CoordPair coordPair = lsPairQueue.poll();
			if (lsPairsResult.isEmpty()) {
				lsPairsResult.add(coordPair);
				continue;
			}
			BinarySearch<CoordPair> binarySearch = new BinarySearch<>(lsPairsResult);
			BsearchSiteDu<CoordPair> bSiteDu = binarySearch.searchLocationDu(coordPair.getStartAbs(), coordPair.getEndAbs());
			List<CoordPair> lsOverlapElement = bSiteDu.getAllElement();
			//没找到，直接插入
			if (lsOverlapElement.isEmpty()) {
				insertElementInResult(bSiteDu, coordPair);
			}
			//场景同下面的新元素覆盖老元素的场景，直接跳过
			else if (lsOverlapElement.size() > 2) {
				continue;
			}
			
			//跟其中一个element有交集
			else if (lsOverlapElement.size() == 1) {
				CoordPair element = lsOverlapElement.get(0);
				/**
				 * 完全覆盖，则跳过。老元素覆盖新元素很好理解
				 * 
				 * 新元素覆盖老元素的场景，老元素是chr1-vs-chr1，新元素是另一条染色体 chr1-vs-chr2
				* 因为染色体如果不一致，会对元素降权重。这时候如果chr1某个区域同时比对上了 chr1 和chr2，
				* 就可能出现chr2明明比chr1比对的区段长，但是还是优先chr1
				* 这时候新元素就覆盖老元素，但是一样跳过
				*/
				if (isCoverOnAnother(element, coordPair)) {
					continue;
				}
				//部分覆盖，修正后放回lsComparesQueen中去
				if (Alignment.isOverlap(element, coordPair)) {
					if (element.getStartAbs() <= coordPair.getStartAbs()) {
						coordPair.setStart(element.getEndAbs()+1);
					} else if (element.getEndAbs() >= coordPair.getEndAbs()) {
						coordPair.setEnd(element.getStartAbs()-1);
					}
				}
			}
			//跟两个element有交集
			else if (lsOverlapElement.size() == 2) {
				if (isCoverOnAnother(lsOverlapElement.get(0), coordPair)
						|| isCoverOnAnother(lsOverlapElement.get(1), coordPair)
						) {
					continue;
				}
				//这两个都要执行，所以不能用 if else。考虑两个if的else都抛出异常
				if (lsOverlapElement.get(0).getStartAbs() <= coordPair.getStartAbs()) {
					coordPair.setStart(lsOverlapElement.get(0).getEndAbs()+1);
				}
				if (lsOverlapElement.get(1).getEndAbs() >= coordPair.getEndAbs()) {
					coordPair.setEnd(lsOverlapElement.get(1).getStartAbs()-1);
				}
			}
			
			insertElementQueue(coordPair);
		}
		

	}
	
	/** 两个元素是否有一个覆盖了另一个 */
	private static boolean isCoverOnAnother(Alignment align1, Alignment align2) {
		return Alignment.isOverlap(align1, align2) || Alignment.isOverlap(align2, align1);
	}
	
	/** 把修改过的element重新插入LinkedList队列 */
	private void insertElementQueue(CoordPair element) {
		if (lsPairs.getLast().getScore() >= element.getScore()) {
			lsPairs.add(element);
			return;
		}
		if (lsPairs.getFirst().getScore() <= element.getScore()) {
			lsPairs.addFirst(element);
			return;
		}
		ListIterator<CoordPair> lsItQueue = lsPairs.listIterator();
		while (lsItQueue.hasNext()) {
			CoordPair elementOld = lsItQueue.next();
			if (elementOld.getScore() <= element.getScore()) {
				lsItQueue.previous();
				lsItQueue.add(element);
				break;
			}
		}
	}
	
	/** 插入元素 */
	private void insertElementInResult(BsearchSiteDu<CoordPair> bSiteDu, CoordPair element) {
		int index = bSiteDu.getSiteRight().getItemNumDown();
		if (index == -1) {
			lsPairsResult.add(element);
		} else {
			lsPairsResult.add(index, element);
		}
	}
	
}

/** 一对比较 */
class CoordPair implements Alignment {
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
	public CoordPair(String mummerLine) {
		String[] ss = mummerLine.split("\\|");
		String[] chrId = ss[4].trim().split("\t");
		String[] refloc = ss[0].trim().split(" +");
		String[] altloc = ss[1].trim().split(" +");
		
		alignRef = new Align(chrId[0], Integer.parseInt(refloc[0]), Integer.parseInt(refloc[1]));
		alignAlt = new Align(chrId[1], Integer.parseInt(altloc[0]), Integer.parseInt(altloc[1]));
		identity = Double.parseDouble(ss[3].trim());
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
		if (isNeedNewIdel(num)) {
			indel = new IndelForRef(alignAlt.isCis5to3());
			int numAbs = Math.abs(num);
			if (num > 0) {
				indel.setRefStart(getLastSiteRef()+numAbs);
				if (alignAlt.isCis5to3()) {
					indel.setAltStartCis(getLastSiteAlt() + numAbs -1);
				} else {
					indel.setAltStartCis(getLastSiteAlt() - numAbs +1);
				}
				indel.addRefLen1();
			} else {
				indel.setRefStart(getLastSiteRef()+numAbs-1);
				if (alignAlt.isCis5to3()) {
					indel.setAltStartCis(getLastSiteAlt() + numAbs);
				} else {
					indel.setAltStartCis(getLastSiteAlt() - numAbs);
				}
				indel.addAltLen1();
			}
			lsIndel.add(indel);
		} else {
			indel = lsIndel.get(lsIndel.size()-1);
			if (num > 0) {
				indel.addRefLen1();
			} else {
				indel.addAltLen1();
			}
		}
	}
	
	private int getLastSiteRef() {
		if (lsIndel.isEmpty()) {
			return alignRef.getStartAbs()-1;
		}
		IndelForRef indelLast = lsIndel.get(lsIndel.size()-1);
		/**
		 * deletion
		 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
		 * 1    ca- - - gcat
		 * 1    ca tgc gcat
		 * 因此deletion的计算位点应该向前移一位
		 */
		return indelLast.isRefInsertion() ? indelLast.getEndAbs() : indelLast.getEndAbs()-1;
	}
	
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
		return indelLast.isAltInsertion() ? indelLast.getEndCisAlt(): indelLast.getEndCisAltBefore1();
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
				if (!indelForRefLast.isRefInsertion()) length++;
				alignAlt.setStartCis(indelForRefLast.getEndCisAlt());
				if (indelForRefLast.isAltInsertion()) {
					// ref atc  ----  atc
					// alt atc ctga atc
					alignAlt.startAddLenCis(length);
				} else {
					// ref atc  ctga  atc
					// alt atc   ----   atc
					alignAlt.startAddLenCis(length-1);
				}
			}
			
			if (Alignment.isSiteInAlign(indelForRef, start)) {
				isSet = true;
				if (indelForRef.isRefInsertion()) {
					// ref atc ctga atc
					// alt atc  ----  atc
					alignRef.setStartAbs(indelForRef.getEndAbs()+1);
					alignAlt.setStartCis(indelForRef.getEndCisAlt());
					if (indelForRef.isAltInsertion()) {
						alignAlt.startAddLenCis(1);
					}
					continue;
				} else {
					alignRef.setStartAbs(start);
					// ref at[c]  ----  atc
					// alt atc ctga atc
					if (start == indelForRef.getStartAbs()) {
						alignAlt.setStartCis(indelForRef.getStartCisAlt());
						alignAlt.startAddLenCis(-1);
					} else {
						// ref atc  ----  [a]tc
						// alt atc ctga atc
						alignAlt.setStartCis(indelForRef.getEndCisAlt());
						alignAlt.startAddLenCis(1);
						continue;
					}
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
					if (!indelForRefLast.isRefInsertion()) length++;
					
					alignAlt.setEndCis(indelForRefLast.getEndCisAlt());
					if (indelForRefLast.isAltInsertion()) {
						// ref atc ctga atc
						// alt atc  ----  atc
						alignAlt.endAddLenCis(length);
					} else {
						// ref atc  ----  atc
						// alt atc ctga atc
						alignAlt.endAddLenCis(length-1);
					}
				}
				break;
			}
			
			if (Alignment.isSiteInAlign(indelForRef, end)) {
				isSet = true;
				if (indelForRef.isRefInsertion()) {
					// ref atc ctga atc
					// alt atc  ----  atc
					alignRef.setEndAbs(indelForRef.getStartAbs()-1);
					alignAlt.setEndCis(indelForRef.getStartCisAlt());
					if (indelForRef.isAltInsertion()) {
						alignAlt.endAddLenCis(-1);
					}
					break;
				} else {
					//ref没有插入，此时alt一定插入
					alignRef.setEndAbs(end);
					// ref at[c]  ----  atc
					// alt atc ctga atc
					if (end == indelForRef.getStartAbs()) {
						alignAlt.setEndCis(indelForRef.getStartCisAlt());
						alignAlt.endAddLenCis(-1);
						break;
					} else {
						// ref atc  ----  [a]tc
						// alt atc ctga atc
						alignAlt.setEndCis(indelForRef.getEndCisAlt());
						alignAlt.endAddLenCis(1);
					}
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
		BinarySearch<IndelForRef> binarySearch = new BinarySearch<>(lsIndel, true);
		VarInfo varInfo = new VarInfo();
		varInfo.setCis5to3(this.getAlignAlt().isCis5to3());
		varInfo.setChrID(this.getChrAlt());
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
		if (lsIndels.isEmpty()) {
			return varInfo;
		}
		IndelForRef indelStart = lsIndels.get(0);
		if (start == indelStart.getStartAbs() && indelStart.isRefInsertion()) {
			lsIndels.remove(0);
		}
		if (lsIndels.isEmpty()) {
			return varInfo;
		}
		IndelForRef indelEnd = lsIndels.get(lsIndels.size()-1);
		if (end == indelEnd.getEndAbs() && indelEnd.isRefInsertion()) {
			lsIndels.remove(lsIndels.size()-1);
		}
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
		if (!bsite.isInsideLoc()) {
			IndelForRef IndelBefore = bsite.getAlignUp();
			if (IndelBefore == null) {
				int length = refSite - alignRef.getStartAbs();
				altSite = alignAlt.isCis() ? alignAlt.getStartAbs()+length : alignAlt.getEndAbs() - length;
			} else {
				int length = IndelBefore.isRefInsertion() ? refSite - IndelBefore.getEndAbs(): refSite - IndelBefore.getEndAbs()+1;
				length = IndelBefore.isAltInsertion() ? length : length-1;
				altSite = alignAlt.isCis() ? IndelBefore.getEndCisAlt() + length : IndelBefore.getEndCisAlt() - length;
			}
		} else {
			IndelForRef indelThis = bsite.getAlignThis();
			if (indelThis.isRefInsertion()) {
				bias = isStart? indelThis.getEndAbs() - refSite + 1 : refSite - indelThis.getStartAbs() + 1;
			}
			if (indelThis.isAltInsertion()) {
				if (indelThis.isRefInsertion()) {
					altSite = isStart? indelThis.getEndCisAltAfter1() : indelThis.getStartCisAltBefore1();
				} else {
					altSite = indelThis.getStartAbs() == refSite ? indelThis.getStartCisAltBefore1() : indelThis.getEndCisAltAfter1();
				}
			} else {
				if (indelThis.isRefInsertion()) {
					altSite = isStart? indelThis.getEndCisAlt() : indelThis.getStartCisAlt();
				} else {
					throw new RuntimeException("cannot happen condition!!");
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
	public void setRefStart(int start) {
		this.refStart = start;
	}
	/** 设置alt的起点 */
	public void setAltStartCis(int altStartCis) {
		this.altStartCis = altStartCis;
	}

	/** 跟 {@link #getStartAbs()} 一样
	 *  * insertion
	 * start是插入的具体起点(t)，end是插入的具体终点(c)，都从1开始计算
	 * 1    ca tgc gcat : alt
	 * 1    ca- - - gcat : ref
	 * 
	 * deletion
	 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
	 * 1    ca- - - gcat : alt
	 * 1    ca tgc gcat : ref
	 * @return
	 */
	public int getStartCisAlt() {
		return altStartCis;
	}
	public int getEndCisAlt() {
		if (isAltCis) {
			return altLen == 0 ? altStartCis+1 : altStartCis + altLen - 1;
		} else {
			return altLen == 0 ? altStartCis-1 : altStartCis - altLen + 1;
		}
	}
	/** end往前退一位 */
	public int getStartCisAltBefore1() {
		int start = getStartCisAlt();
		return isAltCis ? start-1 : start + 1;
	}
	/** end往前退一位 */
	public int getStartCisAltAfter1() {
		int start = getStartCisAlt();
		return isAltCis ? start+1 : start-1;
	}
	/** end往前退一位 */
	public int getEndCisAltBefore1() {
		int end = getEndCisAlt();
		return isAltCis ? end-1 : end + 1;
	}
	/** end往前退一位 */
	public int getEndCisAltAfter1() {
		int end = getEndCisAlt();
		return isAltCis ? end+1 : end-1;
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

	@Override
	public int getStartAbs() {
		return refStart;
	}

	@Override
	public int getEndAbs() {
		return refLen > 0 ? refStart+refLen-1:refStart+1;
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

