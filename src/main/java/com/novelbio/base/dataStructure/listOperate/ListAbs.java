package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.collections.functors.IfClosure;
import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * ���ǽ����ֳ�Ϊ������ͬ��list��һ��cis��һ��trans��һ��null
 * @author zong0jie
 *
 * @param <E>
 */
public class ListAbs <E extends ListDetailAbs> extends ArrayList<E>  implements Cloneable {
	private static final long serialVersionUID = -3356076601369239937L;
	private static Logger logger = Logger.getLogger(ListAbs.class);
	/**����ĳ�����굽���ڵ��ں���/���������ľ��� */
	HashMap<Integer, Integer> hashLocExInStart;
	/** ����ĳ�����굽���ڵ��ں���/�������յ�ľ��� */
	HashMap<Integer, Integer> hashLocExInEnd;
	/** ����Ŀ������ */
	protected String listName;
	/** ���� */
	Boolean cis5to3 = null;
	
	public void setName(String listName) {
		this.listName = listName;
	}
	public String getName() {
		if (listName == null) {
			if (size() > 0) {
				listName = get(0).getRefID();
			}
			else {
				listName = "";
			}
		}
		return listName;
	}
	/**
	 * û�з����򷵻�null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
 	}
	/**
	 * ����ʵ�ʵ�num��element�����ĳ���
	 * @param num ʵ����Ŀ
	 * @return
	 */
	public int getInterGenic(int num) {
		if (cis5to3 == null) {
			return get(num).getStartAbs() - get(num - 1).getEndAbs();
		}
		else {
			return Math.abs(get(num).getStartCis() - get(num - 1).getEndCis());
		}
	}
	/**
	 * ����ʵ�ʵ�num��element�ĳ���
	 * @param num ʵ����Ŀ
	 * @return
	 */
	public int getEleLen(int num) {
		return get(num-1).Length();
	}
	
	public int getLen() {
		if (cis5to3 != null) {
			return Math.abs(get(0).getStartCis() - get(size()-1).getEndCis()) + 1;
 		}
		else {
			if (size() == 1) {
				return get(0).Length();
			} else {
				if (get(0).getStartAbs() < get(1).getStartAbs()) {
					return get(size()-1).getEndAbs() - get(0).getStartAbs();
				}
				else {
					return get(0).getEndAbs() - get(size()-1).getStartAbs();
				}
			}
		}
	}
	/**
	 * �����η������������η��ظ���
	 * @param loc
	 * @return
	 */
	protected int getLoc2Start(int loc) {
		if (isLocDownStart(loc)) {
			return Math.abs(loc - getStart());
		} else {
			return -Math.abs(loc - getStart());
		}
	}
	/**
	 * �����η������������η��ظ���
	 * @param loc
	 * @return
	 */
	protected int getLoc2End(int loc) {
		if (isLocDownEnd(loc)) {
			return Math.abs(loc - getEnd());
		} else {
			return -Math.abs(loc - getEnd());
		}
	}
	/**
	 * �����loc�Ƿ��ڱ�list�ķ�Χ��
	 * @return
	 */
	protected boolean isLocInside(int loc) {
		if (loc >= Math.max(getStart(), getEnd()) || loc <= Math.min(getStart(), getEnd())) {
			return false;
		}
		return true;
	}
	/**
	 * �����loc�Ƿ���Start������
	 * @return
	 */
	protected boolean isLocDownStart(int loc) {
		if (isCis5to3() && loc >= getStart()
		||
		!isCis5to3() && loc <= getStart()
		) {
			return true;
		}
		return false;
	}
	/**
	 * �����loc�Ƿ���Start������
	 * @return
	 */
	protected boolean isLocDownEnd(int loc) {
		if (isCis5to3() && loc >= getEnd()
		||
		!isCis5to3() && loc <= getEnd()
		) {
			return true;
		}
		return false;
	}
	/** ���ݷ��򷵻� */
	public int getStart() {
		if (cis5to3 != null) {
			return get(0).getStartCis();
		}
		return get(0).getStartAbs();
	}
	/** ���ݷ��򷵻� */
	public int getEnd() {
		if (cis5to3 != null) {
			return get(size() - 1).getEndCis();
		}
		return get(size() - 1).getEndAbs();
	}
	
	/**
	 * ��������֮��ľ��룬��������������mRNA����ľ��룬Ҳ����ֻ����ele�ϵľ��롣
	 * �������ص�ʱ������0
	 * ��loc1��loc2����ʱ��������������loc1��loc2����ʱ�����ظ���
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�GffCodAbs.LOC_ORIGINAL
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
	 */
	public int getLocDistmRNA(int loc1, int loc2) {
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getNumCodInEle(locSmall); 
		int locBigExInNum = getNumCodInEle(locBig);
		
		int distance = ListCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		} else {
			distance = getCod2ExInEnd(locSmall) + getCod2ExInStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + get(i).Length();
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return Math.abs(distance);
		}
		return -Math.abs(distance);
	}
	/**
	 * ���������趨ListAbs�ķ��򣬲��Ҹ÷�������ڲ���element�ķ���Ҫһ��
	 * ���굽element �����룬����ص���Ϊ0
	 * @param location ����
	 */
	public int getCod2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - get(NumExon).getStartCis());//���뱾��������ʼ nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getStartAbs());//���뱾��������ʼ nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - get(NumExon).getEndCis()) -1;// ��ǰһ�������� NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getEndAbs()) -1;// ��ǰһ�������� NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * ���굽element �յ���룬���ص�ʱ��Ϊ0
	 * @param location ����
	 */
	public int getCod2ExInEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null) {
				loc2ExInEnd = Math.abs(get(NumExon).getEndCis() - location);//���뱾��������ֹ  Cnnnnnnn
			} else {
				loc2ExInEnd = Math.abs(get(NumExon).getEndAbs() - location);//���뱾��������ֹ  Cnnnnnnn
			}
		}
		//0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
		else if(exIntronNum < 0) {
			if (cis5to3 != null) {
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartCis() - location) - 1;// ���һ�������� nnCnnnnN
			} else {
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartAbs() - location) - 1;// ���һ�������� nnCnnnnN
			}
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	/**
	 * �������element�ĳ���֮��
	 */
	public int getListLen() {
		int isoLen = 0;
		for (E exons : this) {
			isoLen = isoLen + exons.Length();
		}
		return isoLen;
	}
	/**
	 * ����ÿ��ID��Ӧ�ľ���element�ı��
	 * key����Сд
	 * @return
	 */
	public HashMap<String,Integer> getMapName2DetailAbsNum() {
		HashMap<String, Integer> hashID2Num = new HashMap<String, Integer>();
		for (int i = 0; i < size(); i++) {
			E lsDetail = get(i);
			ArrayList<String> ss = lsDetail.getName();
			for (String string : ss) {
				hashID2Num.put(string.toLowerCase(), i);
			}
		}
		return hashID2Num;
	}
	/**
	 * ����ÿ��ID��Ӧ�ľ���element
	 * ����һ��hashmap�������������Ϣ
	 * key����Сд
	 * @return
	 */
	public HashMap<String, E> getMapName2DetailAbs() {
		HashMap<String, E> mapName2DetailAbs = new HashMap<String, E>();
		for (E ele : this) {
			if (ele.getRefID().equals("chr10") && Math.abs(ele.getStartAbs() - 695888) < 50000) {
				System.out.println("stop");
			}
			ArrayList<String> ss = ele.getName();
			for (String string : ss) {
				mapName2DetailAbs.put(string.toLowerCase(), ele);
				mapName2DetailAbs.put(GeneID.removeDot(string.toLowerCase()), ele);
			}
		}
		return mapName2DetailAbs;
	}
	/**
	 * ���ر�ListAbs�е�����string����
	 * �������Item���ص��ģ�ȡȫ��ID
	 * @return
	 */
	public ArrayList<String> getLsNameAll() {
		ArrayList<String> lsLocID = new ArrayList<String>();
		for (E ele : this) {
			lsLocID.addAll(ele.getName());
		}
		return lsLocID;
	}
	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���<br>
	 * 3���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 */
	protected CoordLocationInfo LocPosition( int Coordinate) {
		if (cis5to3 == null) {
			return BinarySearch.LocPositionAbs(this, Coordinate);
		}
		else if (cis5to3) {
			return BinarySearch.LocPositionCis(this, Coordinate);
		}
		else {
			return BinarySearch.LocPositionTran(this, Coordinate);
		}
	}
	
	/**
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ����1��ʼ����
	 * @return
	 */
	public int getNumCodInEle(int location) {
		return LocPosition(location).getElementNumThisAbs();
	}

	/**
	 * TO BE CHECKED
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 * LnnnnNΪ5λ
	 */
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getNumCodInEle(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getCod2ExInStart(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else
					return  location + Math.abs(mRNAnum);
			}
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = Math.abs(mRNAnum) - getCod2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					E tmpExon = get(i);
					// һ��һ�������ӵ���ǰ����
					if (remain - tmpExon.Length() > 0) {
						remain = remain - tmpExon.Length();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getEndCis() - remain + 1;
						}
						else {
							return tmpExon.getEndCis() + remain - 1;
						}
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getCod2ExInEnd(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else {
					return location - mRNAnum;
				}
			} 
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = mRNAnum - getCod2ExInEnd(location);
				for (int i = exonNum + 1; i < size(); i++) {
					E tmpExon = get(i);
					// һ��һ�������ӵ���ǰ����
					if (remain - tmpExon.Length() > 0) {
						remain = remain - tmpExon.Length();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getStartCis() + remain - 1;
						}
						else {
							return tmpExon.getStartCis() - remain + 1;
						}
					}
				}
				return -1;
			}
		}
	}
	/**
	 * ���αȽ�����list�е�Ԫ���Ƿ�һ�¡��ڲ�����ÿ��Ԫ�ص�equals����
	 * ���Ƚ�name�������Ҫ�Ƚ�name����ô����equal
	 * ��ʱ��û��дequal
	 * �����ӱȽ����һģһ���򷵻�true��
	 * @param lsOtherExon
	 * @return
	 */
	public boolean equalsIso(ListAbs<E> lsOther) {
		if (lsOther.size() != size() ) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			E otherT = lsOther.get(i);
			E thisT = get(i);
			if (!otherT.equals(thisT)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * ��list�е�Ԫ�ؽ����������������ô�ʹӴ�С����
	 * ���������ô�ʹ�С��������
	 * �ڲ���flag�������Ͳ������ŵڶ�����
	 */
	public void sort() {
		if (cis5to3 == null) {
			Collections.sort(this, new CompS2MAbs());
		}
		else if (cis5to3) {
			Collections.sort(this, new CompS2M());
		}
		else {
			Collections.sort(this, new CompM2S());
		}
	}
	/**
	 * �Ѳ��ԣ�����
	 */
	@SuppressWarnings("unchecked")
	public ListAbs<E> clone() {
		ListAbs<E> result = null;
		result = (ListAbs<E>) super.clone();
		result.cis5to3 = cis5to3;
		result.hashLocExInEnd = hashLocExInEnd;
		result.hashLocExInStart = hashLocExInStart;
		result.listName = listName;
		result.clear();
		for (E ele : this) {
			result.add((E) ele.clone());
		}
		return result;
	}
	/**
	 * ����һϵ��ListElement���Լ�һ������
	 * ����ͬ�����ListElement��ȡ������Ȼ��ϲ���Ȼ���ҳ���Щelement�Ĺ�ͬ�߽�
	 * @param cis5to3 null,�����Ƿ���
	 * @param lsIso
	 * @param sepSingle �������������ô�ָ<br>
	 * 	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 *    ---m-m---------------------------------------------n-n----<br>
	 *    true aa �� bb �ֿ�
	 *    false aa �� bb����һ��
	 * @return
	 * ����һ��list������cis5to3�������cis5to3Ϊtrue����С��������
	 * ���cis5to3Ϊfalse���Ӵ�С����
	 * �ڲ���int[] 0: startAbs 1: endAbs
	 */
	public static ArrayList<int[]> getCombSep(Boolean cis5to3, ArrayList<? extends ListAbs<? extends ListDetailAbs>> lsIso, boolean sepSingle) {
		ArrayList<? extends ListDetailAbs> lsAllelement = combListAbs(cis5to3, lsIso);
		ArrayList<int[]> lsSep = null;
		if (sepSingle) {
			lsSep = getLsElementSep(cis5to3, lsAllelement);
		} else {
			lsSep = getLsElementSepComb(cis5to3, lsAllelement);
		}
		return lsSep;
	}
	/**
	 * 
	 * ��һ��List�е�Isoȫ���ϲ�������
	 * @param cis5to3 null,�����Ƿ���
	 * @param lsIso
	 * @return
	 */
	private static ArrayList<? extends ListDetailAbs> combListAbs(Boolean cis5to3, ArrayList<? extends ListAbs<? extends ListDetailAbs>> lsIso) {
		ArrayList<ListDetailAbs> lsAll = new ArrayList<ListDetailAbs>();
		//��ȫ����exon����һ��list���沢������
		for (ListAbs<? extends ListDetailAbs> listAbs : lsIso) {
			if (cis5to3 != null && listAbs.isCis5to3() != cis5to3) {
				continue;
			}
			lsAll.addAll(listAbs);
		}
		Collections.sort(lsAll);
		return lsAll;
	}
	/** �����������exonlist�ϲ�����ü���������exon�����ڷֶ�
	 * ���ص�int[] 0: startAbs    1: endAbs
	 *  
	 *  */
	private static ArrayList<int[]> getLsElementSep(Boolean cis5to3, ArrayList<? extends ListDetailAbs> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);
		for (int i = 1; i < lsAll.size(); i++) {
			int[] exon = new int[]{lsAll.get(i).getStartAbs(), lsAll.get(i).getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
		}
		return lsExonBounder;
	}
	
	/** �����������exonlist�ϲ�����ü���������exon�����ڷֶ�<br>
	 * ���������exon�������ҵ������֣�����<br>
	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 * ---m-m---------------------------------------------n-n----<br>
	 * <br>
	 * ��ôa-a��b-b����һ��<br>
	 *  */
	private static ArrayList<int[]> getLsElementSepComb(Boolean cis5to3, ArrayList<? extends ListDetailAbs> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);
		//һ��flag��ǩ
		
		// ��һ��exon�ĸ��࣬�ж��Ƿ�Ϊͬһ���������
		ListAbs lastExonParent = lsAll.get(0).getParent(); 
		
		//��һ��exon�Ƿ������ڵ�һ���࣬����˵û�и�������һ�������exon��ϣ�����mm��kk�ǻ�ϵģ�aa�ǵ�����
		//* -------m-----------m-------------a--a---------b--b------------n-n----<br>
		 //* ---k---------k--------------------------------------n-n----<br>
		boolean lastParentIsSingle = true; 
		
		for (int i = 1; i < lsAll.size(); i++) {
			int[] exon = new int[]{lsAll.get(i).getStartAbs(), lsAll.get(i).getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					lastParentIsSingle = false;
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					//��������������
					//* ---m-m-------------a--a---------b--b------------n-n----<br>
					//* ---m-m---------------------------------------------n-n----<br>
					if (lastParentIsSingle == true && lastExonParent == lsAll.get(i).getParent() 
							&&
							(i == lsAll.size() - 1 || lsAll.get(i+1).getStartAbs() >= lsAll.get(i).getEndAbs())
					) {
						exonOld[1] = exon[1];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = lsAll.get(i).getParent();
					}
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					lastParentIsSingle = false;
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					if (lastParentIsSingle == true && lastExonParent == lsAll.get(i).getParent() 
							&&
							(i == lsAll.size() - 1 || lsAll.get(i+1).getStartCis() <= lsAll.get(i).getEndCis())
					) {
						exonOld[0] = exon[0];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = lsAll.get(i).getParent();
					}
				}
			}
		}
		return lsExonBounder;
	}
	
}
/**
 * �ڽ��Ķ��ַ������࣬ר������ListAbs����
 * @author zong0jie
 *
 */
class BinarySearch {
	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 */
	protected static CoordLocationInfo LocPositionCis(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < lsElement.get(beginnum).getStartCis()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate >= lsElement.get(endnum).getStartCis()) {
			if (Coordinate > lsElement.get(endnum).getEndCis()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndCis())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate >= lsElement.get(endnum).getStartCis())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location�ڻ����ⲿ
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 */
	protected static CoordLocationInfo LocPositionTran(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate > lsElement.get(beginnum).getStartCis()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) {
			if (Coordinate < lsElement.get(endnum).getEndCis()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate > lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate >= lsElement.get(beginnum).getEndCis()) { // location�ڻ����ڲ�
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) 
		{// location�ڻ����ڲ�
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location�ڻ����ⲿ
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}
	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 */
	protected static CoordLocationInfo LocPositionAbs(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate >= lsElement.get(endnum).getStartAbs()) {
			if (Coordinate > lsElement.get(endnum).getEndAbs()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartAbs()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndAbs()) {
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		coordLocationInfo.setElementInsideOutSideNum(-beginnum-1);
		return coordLocationInfo;
	}
	
}

/**
 * ��С��������
 * @author zong0jie
 */
class CompS2M implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * ��С���������þ�������ֵ����
 * @author zong0jie
 */
class CompS2MAbs implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartAbs();
		Integer o2start = o2.getStartAbs();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndAbs();
			Integer o2end = o2.getEndAbs();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * �Ӵ�С����
 * @author zong0jie
 */
class CompM2S implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return -o1end.compareTo(o2end);
		}
		return -comp;
	}
}
/**
 * ǰ�ᣬ��һ��element��������list����㣬���һ��element���յ����list���յ�
 * �����Ҫ<b>��дgetElementNumThisAbs() ����</b>
 * 
 * ���ַ�����location���ڵ�λ�����������Ϣ
 * ����һ��int[3]���飬<br>
 * 0: 1-������ 2-������<br>
 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
 * 2���¸��������� -1��ʾ����û�л���
 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
 * ����Ϊ0
 * ��0��ʼ����Ŀ������ֱ����get(i)��ȡ
 */

class CoordLocationInfo {
	/**�����ҵ�list��Ԫ�ظ��� */
	int listSize;
	/** ��ʾ�õ��ڵڼ���Ԫ����<br>
	 * ������ʾ�ڵڼ���Ԫ���У�Ʃ���ڵڼ���exon�л�ڼ��������У�ʵ����Ŀ<br>
	 * ������ʾ�ڵڼ���intron�л�ڼ�������У�ʵ����Ŀ��
	 * �����list��ǰ�棬��Ϊ0�������list����棬��Ϊ������list.size()
	 */
	int elementInsideOutSideNumAbs = 0;
	
	public CoordLocationInfo(int listSize) {
		this.listSize = listSize;
	}
	/** ��ʾ�õ��ڵڼ���Ԫ���У�<b>ʵ����Ŀ</b><br>
	 * ������ʾ�ڵڼ���Ԫ���У�Ʃ���ڵڼ���exon�л�ڼ��������У�ʵ����Ŀ<br>
	 * ������ʾ�ڵڼ���intron�л�ڼ�������С�
	 * �����list��ǰ�棬��Ϊ0�������list����棬��Ϊ������list.size()
	 */
	public void setElementInsideOutSideNum(int elementInsideOutSideNumAbs) {
		this.elementInsideOutSideNumAbs = elementInsideOutSideNumAbs;
	}
	
	public boolean isInsideElement() {
		if (elementInsideOutSideNumAbs > 0) {
			return true;
		}
		return false;
	}
	/**
	 * ���ظõ����ڵ�Ԫ�أ�һֱ���������������list�⣬����-1<br>
	 * ������0��ʼ<br>
	 * <b>-1��ʾǰ��û�л���</b>
	 * @return
	 */
	public int getElementNumLastElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 2;
		}
		else if (elementInsideOutSideNumAbs < 0) {
			return Math.abs(elementInsideOutSideNumAbs) - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * ���ظõ����ڵ�Element��һֱ���������������element֮�⣬����-1
	 * ������0��ʼ
	 * @return
	 */
	public int getElementNumThisElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * ���ظõ����ڵ�Ԫ�أ�һֱ���������������list�⣬����-1<br>
	 * ������0��ʼ<br>
	 * <b>-1��ʾ����û�л���</b>
	 * @return
	 */
	public int getElementNumNextElementFrom0() {
		if (elementInsideOutSideNumAbs >= 0 && elementInsideOutSideNumAbs < listSize) {
			return elementInsideOutSideNumAbs;
		}
		else if (elementInsideOutSideNumAbs < 0 && Math.abs(elementInsideOutSideNumAbs) < listSize) {
			return Math.abs(elementInsideOutSideNumAbs);
		}
		else {
			return -1;
		}
	}
	/**
	 * ǰ�ᣬ��һ��element��������list����㣬���һ��element���յ����list���յ�<br>
	 * ���ظõ����ڵ�Ԫ�أ���1��ʼ��<br>
	 * ������ʾ�ڵڼ���Ԫ���У�Ʃ���ڵڼ���exon�л�ڼ��������У�ʵ����Ŀ<br>
	 * ������ʾ�ڵڼ���intron�л�ڼ�������С�<br>
	 * ���<b>��list��ǰ�������棬��Ϊ0</b>��
	 */
	public int getElementNumThisAbs() {
		if (elementInsideOutSideNumAbs == -listSize) {
			return 0;
		}
		return elementInsideOutSideNumAbs;
	}
}

