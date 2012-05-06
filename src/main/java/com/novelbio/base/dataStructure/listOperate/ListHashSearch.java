package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;


/**
 * ���Gff����Ŀ��Ϣ<br/>
 * �����GffHash��Ҫʵ��ReadGffarray��ͨ���÷�������������
 * @Chrhash hash��ChrID��--ChrList--GeneInforList(GffDetail��)
 * @locHashtable hash��LOCID��--GeneInforlist
 * @LOCIDList ˳��洢ÿ������Ż���Ŀ��
 */
public abstract class ListHashSearch < T extends ListDetailAbs, E extends ListCodAbs<T>, K extends ListCodAbsDu<T, E>, M extends ListAbsSearch<T, E, K>>
{
	Logger logger = Logger.getLogger(ListHashSearch.class);
	/**
	 * ��ϣ��LOC--LOCϸ��<br>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��<br>
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
	  * �����ж��LOCID����һ�����������������ж����ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene<br>
	 */
	protected HashMap<String,T> locHashtable;
	/**
	 * ��ϣ��LOC--��arraylist�ϵ�Num<br>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ�����Ӧ��chr�ϵ�λ��<br>
	 */
	protected HashMap<String,Integer> hashLoc2Num;
	/**  ���Ĭ��Ϊ������  */
	int startRegion = 1;
	/**  �յ�Ĭ��Ϊ������ */
	int endRegion = 0;
	/**
	 * ����������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected LinkedHashMap<String, M> Chrhash;
	
	/**
	 * ����Ƿ�Ϊ�����䣬������Ϊ�����䣬<br>
	 * False: ���������˼�ǣ�24��ʾ��0��ʼ������24λ��Ҳ����ʵ�ʵ�25λ<br>
	 * True: ���������˼�ǣ�24�ʹ����24λ<br>
	 * UCSC��Ĭ���ļ�������ǿ�����
	 */
	public void setStartRegion(boolean region) {
		if (region) 
			this.startRegion = 0;
		else 
			this.startRegion = 1;
	}
	/**
	 * ���Ĭ��Ϊ������
	 */
	public int getStartRegion() {
		return startRegion;
	}
	/**
	 * �յ�Ĭ��Ϊ������
	 */
	public int getEndRegion() {
		return endRegion;
	}

	/**
	 * ����Ƿ�Ϊ�����䣬������Ϊ�����䣬<br>
	 * False: ���������˼�ǣ�24��ʾ��0��ʼ������24λ��Ҳ����ʵ�ʵ�25λ<br>
	 * True: ���������˼�ǣ�24�ʹ����24λ<br>
	 * UCSC��Ĭ���ļ����յ��Ǳ������
	 */
	public void setEndRegion(boolean region) {
		if (region) 
			this.endRegion = 0;
		else 
			this.endRegion = 1;
	}
	
	String gfffilename = "";
	
	public String getGffFilename() {
		return gfffilename;
	}

	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,Integer> getHashLocNum() {
		if (hashLoc2Num != null) {
			return hashLoc2Num;
		}
		hashLoc2Num = new LinkedHashMap<String, Integer>();
		for (M listAbs : Chrhash.values()) {
			listAbs.getHashLocNum(hashLoc2Num);
		}
		return hashLoc2Num;
	}
	
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,T> getLocHashtable() {
		if (locHashtable != null) {
			return locHashtable;
		}
		locHashtable = new LinkedHashMap<String, T>();
		for (M listAbs : Chrhash.values()) {
			listAbs.getLocHashtable(locHashtable);
		}
		return locHashtable;
	}
	/**
	 * ����һ��chrID�����ظ�chrID����Ӧ��ListAbs
	 * @param chrID
	 * @return
	 */
	public M getListDetail(String chrID) {
		chrID = chrID.toLowerCase();
		return Chrhash.get(chrID);
	}
	/**
	 * ���List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������ţ�ʵ������������Ŀ��˳����룬���ǲ�����ת¼��(UCSC)�����ظ�(Peak)
	 * ���ID��locHashһһ��Ӧ�����ǲ���������ȷ��ĳ��Ŀ��ǰһ�����һ����Ŀ
	 */
	protected ArrayList<String> LOCIDList;
	/**
	 * ����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������š�
	 * ����ͨ���÷������ĳ��LOC�ڻ����ϵĶ�λ
	 */
	public ArrayList<String> getLOCIDList() {
		return LOCIDList;
	}
	/**
	 * ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ�����"/"������
	 * ��ô��list�е�Ԫ����split("/")�ָ����locHashtable�Ϳ���ȡ��Ӧ��GffDetail��Ŀǰ��Ҫ��Peak�õ�
	 * ˳���ã����Ի��ĳ��LOC�ڻ����ϵĶ�λ��
	 * ����TigrGene��IDÿ������һ��LOCID��Ҳ����˵TIGR��ID����Ҫ�����и��Ȼ����Ҳû��ϵ
	 */
	protected ArrayList<String> LOCChrHashIDList;
	/**
	 * ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ�����ListAbs.SEP������
	 * ��ô��list�е�Ԫ����split("/")�ָ����locHashtable�Ϳ���ȡ��Ӧ��GffDetail��Ŀǰ��Ҫ��Peak�õ�
	 * ˳���ã����Ի��ĳ��LOC�ڻ����ϵĶ�λ��
	 * ����TigrGene��IDÿ������һ��LOCID��Ҳ����˵TIGR��ID����Ҫ�����и��Ȼ����Ҳû��ϵ
	 */
	public ArrayList<String> getLOCChrHashIDList() {
		if (LOCChrHashIDList != null) {
			return LOCChrHashIDList;
		}
		LOCChrHashIDList = new ArrayList<String>();
		for (M lsAbs : Chrhash.values()) {
			LOCChrHashIDList.addAll(lsAbs.getLOCIDList());
		}
		return LOCChrHashIDList;
	}

	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	public HashMap<String, M> getChrhash()
	{
		if (Chrhash == null) {
			Chrhash = new LinkedHashMap<String, M>();
		}
		return Chrhash;
	}
	/**
	 * ��õ�ÿһ����Ϣ����ʵ�ʵĶ�û��clone
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	public E searchLocation(String chrID, int cod1) {
		chrID = chrID.toLowerCase();
		M Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		E gffCod1 = Loclist.searchLocation(cod1);//(chrID, Math.min(cod1, cod2));
		return gffCod1;
	}
	/**
	 * ����˫�����ѯ�Ľ�����ڲ��Զ��ж� cod1 �� cod2�Ĵ�С
	 * ���cod1 ��cod2 ��һ��С��0����ô���겻���ڣ��򷵻�null
	 * @param chrID �ڲ��Զ�Сд
	 * @param cod1 �������0
	 * @param cod2 �������0
	 * @return
	 */
	public K searchLocation(String chrID, int cod1, int cod2) {
		chrID = chrID.toLowerCase();
		M Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		return Loclist.searchLocationDu(cod1, cod2);
	}
	/**
	 * ����ID����������Ӧ����Ϣ�ϼ�һ
	 * @param name
	 * @param location
	 */
	public void addNumber(String chrID, int location)
	{
		ListCodAbs<T> gffCodPeak = searchLocation(chrID, location);
		if (!gffCodPeak.isInsideLoc()) {
			return;
		}
		T gffDetailPeak = gffCodPeak.getGffDetailThis();
		gffDetailPeak.addNumber();
	}
	/**
	 * ���������Լ�ÿ�������������ǰ�����add��
	 * key��int������
	 * value����������
	 * @return
	 */
	public LinkedHashMap<String,LinkedHashMap<int[], Integer>> getFreq()
	{
		LinkedHashMap<String, LinkedHashMap<int[], Integer>> hashResult = new LinkedHashMap<String, LinkedHashMap<int[],Integer>>();
		Set<String> setChrID = getChrhash().keySet();
		for (String string : setChrID) {
			LinkedHashMap<int[], Integer> hashTmpResult = new LinkedHashMap<int[], Integer>();
			M lsPeak = getListDetail(string);
			for (T gffDetailPeak : lsPeak) {
				int[] interval = new int[2];
				interval[0] = gffDetailPeak.getStartAbs();
				interval[1]= gffDetailPeak.getEndAbs();
				hashTmpResult.put(interval, gffDetailPeak.getNumber());
			}
			hashResult.put(string, hashTmpResult);
		}
		return hashResult;
	}
	/**
	 * �ڶ�ȡ�ļ��������ʲô��Ҫ���õģ�����д��setOther();��������
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		if (this.gfffilename.equals(gfffilename)) {
			return;
		}
		this.gfffilename = gfffilename;
		try {
			ReadGffarrayExcep(gfffilename);
			setItemDistance();
			setOther();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @��������Ҫ������
	 * ��ײ��ȡgff�ķ���<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,
	 * �ṹ���£�<br/>
	 * @1.Chrhash
	 * ��ChrID��--ChrList--GeneInforList(GffDetail��)<br/>
	 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID,
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
	 * 
	 * @2.locHashtable
	 * ��LOCID��--GeneInforlist������LOCID����������Ŀ���,������Ŀ��������Ӧ��GffHash���� <br/>
	 * 
	 * @3.LOCIDList
	 * ��LOCID��--LOCIDList����˳�򱣴�LOCID,ֻ������������һ��򣬲�����ͨ������ĳ��������<br/>
	 * @throws Exception 
	 */
	protected abstract void ReadGffarrayExcep(String gfffilename) throws Exception;
	/**
	 * ��Ҫ����
	 * ����ĳ���ض�LOC����Ϣ
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID ����ĳLOC�����ƣ�ע��������һ���̵����֣�Ʃ����UCSC�����У�����locstring���ֺü�����������һ������֣����ǵ����Ķ̵�����
	 * @return ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public T searchLOC(String LOCID) {
		return  locHashtable.get(LOCID.toLowerCase());
	}
	/**
	 * ��Ҫ����
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * ����chrID�͸�Ⱦɫ���ϵ�λ�ã�����GffDetail��Ϣ
	 * @param chrID Сд
	 * @param LOCNum ��Ⱦɫ���ϴ���ѰLOC��int���
	 * @return  ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public T searchLOC(String chrID,int LOCNum) {
		chrID = chrID.toLowerCase();
		return Chrhash.get(chrID).get(LOCNum);
	}
	
	/**
	 * ����ĳ��LOCID�����ظ�LOC��ĳ��Ⱦɫ���е�λ����źţ��ڼ�λ<br>
	 * Ҳ����Chrhash��ĳ��chr�¸�LOC��λ��<br>
	 * ��λ�ñ�����ڵ���0��������ǳ���<br>
	 * �ñȽ��������õ���LOCID��locHashtable�����GffDetail�࣬Ȼ����ChrID��Chrhash�л��ĳ��Ⱦɫ���gffdetail��List��Ȼ��Ƚ����ǵ�locString�Լ�����������յ�
	 * ������GffDetail��equal������д��
	 * @param LOCID ����ĳ������
	 * @return string[2]<br>
	 * 0: Ⱦɫ���ţ�chr1,chr2�ȣ���ΪСд<br>
	 * 1:��Ⱦɫ���ϸ�LOC����ţ���1467��
	 */
	public String[] getLOCNum(String LOCID) {
		String[] result = new String[2];
		T ele = locHashtable.get(LOCID);
		result[0] = ele.getParentName();
		result[1] = getHashLocNum().get(LOCID) + "";
		return result;
	}
	/**
	 * �趨ÿ��GffDetail��tss2UpGene��tes2DownGene
	 */
	private void setItemDistance() {
		for (M lsGffDetail : Chrhash.values()) {
			for (int i = 0; i < lsGffDetail.size(); i++) {
				T gffDetail = lsGffDetail.get(i);
				T gffDetailUp = null;
				T gffDetailDown = null;
				if (i > 0) {
					gffDetailUp = lsGffDetail.get(i-1);
				}
				if (i < lsGffDetail.size() - 1) {
					gffDetailDown = lsGffDetail.get(i + 1);
				}
				if (gffDetail.isCis5to3()) {
					gffDetail.setTss2UpGene( distance(gffDetail, gffDetailUp, true) );
					gffDetail.setTes2DownGene( distance(gffDetail, gffDetailDown, false) );
				}
				else {
					gffDetail.setTss2UpGene( distance(gffDetail, gffDetailDown, false) );
					gffDetail.setTes2DownGene( distance(gffDetail, gffDetailUp, true) );
				}
			}
		}
	}
	
	private int distance(T gffDetail1, T gffDetail2, boolean Up) {
		if (gffDetail2 == null) {
			return 0;
		}
		else {
			if (Up) {
				return Math.abs(gffDetail1.getStartAbs() - gffDetail2.getEndAbs());
			}
			else {
				return Math.abs(gffDetail1.getEndAbs() - gffDetail2.getStartAbs());
			}
		}
	}

	/**
	 * �ڶ�ȡ�ļ��������ʲô��Ҫ���õģ�����д��setOther();�������棬������Ϊ�գ�ֱ�Ӽ̳м���
	 */
	protected void setOther()
	{
		
	}
}
