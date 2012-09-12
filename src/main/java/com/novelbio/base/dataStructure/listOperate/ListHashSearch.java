package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListGff;
import com.novelbio.database.domain.geneanno.SepSign;


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
	protected LinkedHashMap<String,T> mapName2DetailAbs;
	/**
	 * ��ϣ��LOC--��arraylist�ϵ�Num<br>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ�����Ӧ��chr�ϵ�λ��<br>
	 */
	protected LinkedHashMap<String,Integer> mapName2DetailNum;
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
	protected LinkedHashMap<String, M> mapChrID2ListGff;
	/** ��������gffDetailGene */
	ArrayList<T> lsGffDetailAll = new ArrayList<T>();
	/** ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ���ȡ���еĵ�һ�� */
	protected ArrayList<String> lsNameAll;
	/**
	 * ���List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������ţ�ʵ������������Ŀ��˳����룬���ǲ�����ת¼��(UCSC)�����ظ�(Peak)
	 * ���ID��locHashһһ��Ӧ�����ǲ���������ȷ��ĳ��Ŀ��ǰһ�����һ����Ŀ
	 */
	protected ArrayList<String> lsNameNoRedundent;
	
	String gfffilename = "";
	
	public String getGffFilename() {
		return gfffilename;
	}
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
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,Integer> getMapName2DetailNum() {
		if (mapName2DetailNum != null) {
			return mapName2DetailNum;
		}
		mapName2DetailNum = new LinkedHashMap<String, Integer>();
		for (M listAbs : mapChrID2ListGff.values()) {
			mapName2DetailNum.putAll(listAbs.getMapName2DetailAbsNum());
		}
		return mapName2DetailNum;
	}
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,T> getMapName2Detail() {
		if (mapName2DetailAbs != null) {
			return mapName2DetailAbs;
		}
		mapName2DetailAbs = new LinkedHashMap<String, T>();
		for (M listAbs : mapChrID2ListGff.values()) {
			mapName2DetailAbs.putAll(listAbs.getMapName2DetailAbs());
		}
		return mapName2DetailAbs;
	}
	/**
	 * ����һ��chrID�����ظ�chrID����Ӧ��ListAbs
	 * @param chrID
	 * @return
	 */
	public M getListDetail(String chrID) {
		chrID = chrID.toLowerCase();
		return mapChrID2ListGff.get(chrID);
	}

	/**
	 * ����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������š�
	 * ����ͨ���÷������ĳ��LOC�ڻ����ϵĶ�λ
	 * ÿ��gffDetail����һ��Name
	 */
	public ArrayList<String> getLsNameNoRedundent() {
		if (lsNameNoRedundent == null) {
			lsNameNoRedundent = new ArrayList<String>();
			for (M lsGff : mapChrID2ListGff.values()) {
				for (T gff : lsGff) {
					lsNameNoRedundent.add(gff.getNameSingle());
				}
			}
		}
		return lsNameNoRedundent;
	}
	/** ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ���ȫ���� */
	public ArrayList<String> getLsNameAll() {
		if (lsNameAll != null) {
			return lsNameAll;
		}
		lsNameAll = new ArrayList<String>();
		for (M lsGff : mapChrID2ListGff.values()) {
			lsNameAll.addAll(lsGff.getLsNameAll());
		}
		return lsNameAll;
	}

	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	public HashMap<String, M> getMapChrID2LsGff() {
		if (mapChrID2ListGff == null) {
			mapChrID2ListGff = new LinkedHashMap<String, M>();
		}
		return mapChrID2ListGff;
	}
	/**
	 * ��õ�ÿһ����Ϣ����ʵ�ʵĶ�û��clone
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	public E searchLocation(String chrID, int cod1) {
		chrID = chrID.toLowerCase();
		M Loclist =  getMapChrID2LsGff().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
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
		M Loclist =  getMapChrID2LsGff().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
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
	public void addNumber(String chrID, int location) {
		ListCodAbs<T> gffCodPeak = searchLocation(chrID, location);
		if (!gffCodPeak.isInsideLoc()) {
			return;
		}
		T gffDetailPeak = gffCodPeak.getGffDetailThis();
		gffDetailPeak.addReadsInElementNum();
	}
	/**
	 * ���������Լ�ÿ�������������ǰ�����add��
	 * key��int������
	 * value����������
	 * @return
	 */
	public LinkedHashMap<String,LinkedHashMap<int[], Integer>> getFreq() {
		LinkedHashMap<String, LinkedHashMap<int[], Integer>> hashResult = new LinkedHashMap<String, LinkedHashMap<int[],Integer>>();
		Set<String> setChrID = getMapChrID2LsGff().keySet();
		for (String string : setChrID) {
			LinkedHashMap<int[], Integer> hashTmpResult = new LinkedHashMap<int[], Integer>();
			M lsPeak = getListDetail(string);
			for (T gffDetailPeak : lsPeak) {
				int[] interval = new int[2];
				interval[0] = gffDetailPeak.getStartAbs();
				interval[1]= gffDetailPeak.getEndAbs();
				hashTmpResult.put(interval, gffDetailPeak.getReadsInElementNum());
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
			getMapName2DetailNum();
			getMapName2Detail();
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
		return  getMapName2Detail().get(LOCID.toLowerCase());
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
		return mapChrID2ListGff.get(chrID).get(LOCNum);
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
		T ele = getMapName2Detail().get(LOCID.toLowerCase());
		result[0] = ele.getParentName();
		result[1] = getMapName2DetailNum().get(LOCID.toLowerCase()) + "";
		return result;
	}
	/**
	 * �趨ÿ��GffDetail��tss2UpGene��tes2DownGene
	 */
	private void setItemDistance() {
		for (M lsGffDetail : mapChrID2ListGff.values()) {
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
	/**
	 * �������в��ظ�GffDetailGene
	 * @return
	 */
	public ArrayList<T> getGffDetailAll() {
		if (lsGffDetailAll.size() != 0) {
			return lsGffDetailAll;
		}
		for (M lsGffDetailGenes : mapChrID2ListGff.values()) {
			lsGffDetailAll.addAll(lsGffDetailGenes);
		}
		return lsGffDetailAll;
	}
}
