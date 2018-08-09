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
		CoordPairReader coordPairReader = new CoordPairReader("/home/novelbio/下载/IRGSP-1VSIRGSP-4.filter.coords");
//		coordPairReader.setIdentityCutoff(99.00);
		String resultFile = "/home/novelbio/下载/IRGSP-1VSIRGSP-4.filter.filter.coords";
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
	/** 从哪里开始，正数，相对位置，表示从alignRef.getStartAbs() 向后偏移的位置 */
	int startbias;
	/** 到哪里结束，正数，相对位置，表示从alignRef.getEndAbs() 向前偏移的位置 */
	int endbias;
	
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
			indel = new IndelForRef();
			indel.setInsertion(num>0);
			int numAbs = Math.abs(num);
			if (num > 0) {
				indel.setStartEndLoc(getLastSiteNum()+numAbs, getLastSiteNum()+numAbs);
			} else {
				indel.setStartEndLoc(getLastSiteNum()+numAbs-1, getLastSiteNum()+numAbs);
				indel.addDelLen1();
			}
			lsIndel.add(indel);
		} else {
			indel = lsIndel.get(lsIndel.size()-1);
			if (num > 0) {
				indel.setEndAbs(indel.getEndAbs()+1);
			} else {
				indel.addDelLen1();
			}
		}
	}
	
	private int getLastSiteNum() {
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
		if (Alignment.isSiteInAlign(alignRef, start)) {
			throw new RuntimeException();
		}
		startbias = start - alignRef.getStartAbs();
	}
	public void setEnd(int end) {
		if (Alignment.isSiteInAlign(alignRef, end)) {
			throw new RuntimeException();
		}
		endbias = alignRef.getEndAbs() - end;
	}
	/** 本比较实际的长度，考虑了startbias和endbias */
	public int getLength() {
		return Math.max(alignRef.getLength(), alignAlt.getLength()) - startbias - endbias + 1 ;
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
	
	@Override
	public int getStartAbs() {
		return alignRef.getStartAbs()+startbias;
	}
	
	@Override
	public int getEndAbs() {
		return alignRef.getEndAbs()-endbias;
	}
	
	@Override
	public int getStartCis() {
		return alignRef.getStartAbs()+startbias;
	}
	@Override
	public int getEndCis() {
		return alignRef.getEndAbs()-endbias;
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
class IndelForRef extends Align {
	
	boolean isRefInsertion;
	/** 仅当缺失时有效，表示缺失的长度 */
	int deletionLen = 0;
	/** 是否ref相对于alt为插入 */
	public void setInsertion(boolean isRefInsertion) {
		this.isRefInsertion = isRefInsertion;
	}
	public void addDelLen1() {
		deletionLen++;
	}
	public int getDelLen() {
		return deletionLen;
	}
	public int getLength() {
		return isRefInsertion ? super.getLength() : deletionLen;
	}
	/**
	 * true: ref相对于alt为插入<br>
	 * false: ref相对于alt为缺失<br>
	 * @return
	 */
	public boolean isRefInsertion() {
		return isRefInsertion;
	}
	
}

