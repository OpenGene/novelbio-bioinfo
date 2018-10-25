package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.base.binarysearch.BinarySearch;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.fasta.SeqHash;

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
public class CoordReaderMummer {
	
	LinkedList<CoordPair> lsPairs;
	List<CoordPair> lsPairsResult = new ArrayList<>();
	
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
		int index = bSiteDu.getSiteRight().getIndexAlignDown();
		if (index == -1) {
			lsPairsResult.add(element);
		} else {
			lsPairsResult.add(index, element);
		}
	}
	
}
/**
* 专门来读取mummer.coords文件
* @author zong0jie
* @data 2018年8月5日
*/
class CoordPairMummerReader {
	List<CoordPair> lsCoordPairs  = new ArrayList<>();
	/** 相似度的cutoff，如果是不同版本之间的染色体比较，那么cutoff应该会很高 */
	double identityCutoff = 0;
	
	TxtReadandWrite txtRead;
	Iterator<String> itMummer;
	
	CoordPair lastPair;
	
	Map<String, Long> mapChrRef2Len;
	Map<String, Long> mapChrAlt2Len;
	
	public CoordPairMummerReader(String mummerCoord, String refFai, String altFai) {
		txtRead = new TxtReadandWrite(mummerCoord);
		itMummer = txtRead.readlines(5).iterator();
		String content = itMummer.next();
		if (!content.startsWith("==============")) {
			throw new RuntimeException(mummerCoord + " may not the correct mummer coord file.\n"
					+ "coord file line 5 should be: ==============");
		}
		if (!StringOperate.isRealNull(refFai) && !StringOperate.isRealNull(altFai)) {
			mapChrRef2Len = SeqHash.getMapChrId2Len(refFai);
			mapChrAlt2Len = SeqHash.getMapChrId2Len(altFai);
		}
	}
	
	public CoordPairMummerReader(String mummerCoord) {
		this(mummerCoord, null, null);
	}
	
	/** 相似度的cutoff，如果是不同版本之间的染色体比较，那么cutoff应该会很高 */
	public void setIdentityCutoff(double identityCutoff) {
		if (identityCutoff < 1) {
			identityCutoff = identityCutoff*100;
		}
		this.identityCutoff = identityCutoff;
	}
	
	public List<CoordPair> readNext() {
		List<CoordPair> lsCoordPairs = new ArrayList<>();
		if (lastPair != null) {
			lsCoordPairs.add(lastPair);
		}
		while (itMummer.hasNext()) {
			String mummerLine = itMummer.next();
			CoordPair coordPair = new CoordPair();
			coordPair.initialMummer(mummerLine);
			if (mapChrRef2Len != null && mapChrAlt2Len != null) {
				coordPair.setRefAltLen(mapChrRef2Len.get(coordPair.getChrRef()), mapChrAlt2Len.get(coordPair.getChrAlt()));
			}
		
			if (lastPair != null && !coordPair.getChrId().equals(lastPair.getChrId())) {
				lastPair = coordPair;
				break;
			}
			if (coordPair.getIdentity() >= identityCutoff) {
				lsCoordPairs.add(coordPair);
			}
			lastPair = coordPair;
		}
		return lsCoordPairs;
	}
	
	public boolean hasNext() {
		return itMummer.hasNext();
	}
	
	public void close() {
		txtRead.close();
	}
	
	
}

/**
 * 专门来读取mummer.delta文件
 * delta文件中记录了ref-vs-alt的indel信息
 * 把这些信息读取进去
 * @author zong0jie
 * @data 2018年8月5日
 */
class MummerDeltaReader {
	
	Map<String, List<CoordPair>> mapChrId2CoordPair;
	Map<String, CoordPair> mapKey2CoordPair;
	
	public void setMapChrId2CoordPair(Map<String, List<CoordPair>> mapChrId2CoordPair) {
		this.mapChrId2CoordPair = mapChrId2CoordPair;
	}
	
	public void generateMapLoc2Pair() {
		mapKey2CoordPair = new HashMap<>();
		for (List<CoordPair> lsCoordPairs : mapChrId2CoordPair.values()) {
			for (CoordPair coordPair : lsCoordPairs) {
				mapKey2CoordPair.put(coordPair.getKey(), coordPair);
			}
		}
	}
	
	public void readDelta(String mummerDelta) {
		TxtReadandWrite txtRead = new TxtReadandWrite(mummerDelta);
		String[] keyCoordPair = new String[6];
		CoordPair coordPair = null;
		for (String content : txtRead.readlines(3)) {
			if (content.startsWith(">")) {
				String[] ss = content.substring(1).split(" ");
				keyCoordPair[0] = ss[0];
				keyCoordPair[1] = ss[1];
				continue;
			}
			String[] ss = content.split(" ");
			if (ss.length == 7) {
				keyCoordPair[2] = ss[0]; keyCoordPair[3] = ss[1];
				keyCoordPair[4] = ss[2];  keyCoordPair[5] = ss[3];
				coordPair = mapKey2CoordPair.get(ArrayOperate.cmbString(keyCoordPair, " "));
				continue;
			}
			if (coordPair == null) {
				continue;
			}
			int value = Integer.parseInt(content);
			coordPair.addIndelMummer(value);
		}
		txtRead.close();
	}
	
}