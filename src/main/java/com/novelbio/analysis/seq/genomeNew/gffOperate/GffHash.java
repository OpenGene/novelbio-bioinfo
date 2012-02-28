package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CmpListCluster;
import com.novelbio.database.model.modcopeid.CopedID;


/**
 * ���Gff����Ŀ��Ϣ<br/>
 * �����GffHash��Ҫʵ��ReadGffarray��ͨ���÷�������������
 * @Chrhash hash��ChrID��--ChrList--GeneInforList(GffDetail��)
 * @locHashtable hash��LOCID��--GeneInforlist
 * @LOCIDList ˳��洢ÿ������Ż���Ŀ��
 */
public abstract class GffHash <T extends GffDetailAbs, K extends GffCodAbs<T>, M extends GffCodAbsDu<T, K>> {
	/**
	 * ���Ĭ��Ϊ������
	 */
	int startRegion = 1;
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
	 * �յ�Ĭ��Ϊ������
	 */
	int endRegion = 0;
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
	Logger logger = Logger.getLogger(GffHash.class);
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
		for (ListAbs<T> listAbs : Chrhash.values()) {
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
		for (ListAbs<T> listAbs : Chrhash.values()) {
			listAbs.getLocHashtable(locHashtable);
		}
		return locHashtable;
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
		for (ListAbs<T> lsAbs : Chrhash.values()) {
			LOCChrHashIDList.addAll(lsAbs.getLOCIDList());
		}
		return LOCChrHashIDList;
	}
	
	/**
	 * ����������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected LinkedHashMap<String,ListAbs<T>> Chrhash;
	
	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected HashMap<String,ListAbs<T>> getChrhash()
	{
		return Chrhash;
	}
	
	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	public K searchLocation(String chrID, int Coordinate) {
		chrID = chrID.toLowerCase();
		ListAbs<T> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		int[] locInfo = Loclist.LocPosition(Coordinate);// ���ַ�����peaknum�Ķ�λ
		if (locInfo == null) {
			return null;
		}
		K gffCod = setGffCod(chrID, Coordinate);
		if (locInfo[0] == 1) // ��λ�ڻ�����
		{
			gffCod.gffDetailThis = (T) Loclist.get(locInfo[1]).clone(); 
			gffCod.gffDetailThis.setCoord(Coordinate);
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = locInfo[1];
			gffCod.insideLOC = true;
			if (locInfo[1] - 1 >= 0) {
				gffCod.gffDetailUp =  (T) Loclist.get(locInfo[1]-1).clone();
				gffCod.gffDetailUp.setCoord(Coordinate);
				gffCod.ChrHashListNumUp = locInfo[1]-1;
				
			}
			if (locInfo[2] != -1) {
				gffCod.gffDetailDown = (T) Loclist.get(locInfo[2]).clone();
				gffCod.gffDetailDown.setCoord(Coordinate);
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		} else if (locInfo[0] == 2) {
			gffCod.insideLOC = false;
			if (locInfo[1] >= 0) {
				gffCod.gffDetailUp =  (T) Loclist.get(locInfo[1]).clone();
				gffCod.gffDetailUp.setCoord(Coordinate);
				gffCod.ChrHashListNumUp = locInfo[1];		
			}
			if (locInfo[2] != -1) {
				gffCod.gffDetailDown = (T) Loclist.get(locInfo[2]).clone();
				gffCod.gffDetailDown.setCoord(Coordinate);
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		}
		return gffCod;
	}

	protected abstract K setGffCod(String chrID, int coordinate);
	/**
	 * ����˫�����ѯ�Ľ�����ڲ��Զ��ж� cod1 �� cod2�Ĵ�С
	 * ���cod1 ��cod2 ��һ��С��0����ô���겻���ڣ��򷵻�null
	 * @param chrID
	 * @param cod1 �������0
	 * @param cod2 �������0
	 * @return
	 */
	public M searchLocation(String chrID, int cod1, int cod2) {
		chrID = chrID.toLowerCase();
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		ListAbs<T> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		
		K gffCod1 = searchLocation(chrID, Math.min(cod1, cod2));
		K gffCod2 = searchLocation(chrID, Math.max(cod1, cod2));
		if (gffCod1 == null) {
			System.out.println("error");
		}
		M gffCodDu = setGffCodDu(new ArrayList<T>(),gffCod1, gffCod2 );
		
		if (gffCodDu.gffCod1.getItemNumDown() < 0) {
			gffCodDu.lsgffDetailsMid = null;
		}
		else {
			for (int i = gffCodDu.gffCod1.getItemNumDown(); i <= gffCodDu.gffCod2.getItemNumUp(); i++) {
				gffCodDu.lsgffDetailsMid.add((T)Loclist.get(i).clone());
			}
		}
		return gffCodDu;
	}
	/**
	 * newһ����Ӧ��GffCodDu����
	 * @return
	 */
	protected abstract M setGffCodDu(ArrayList<T> lsgffDetail,
			K gffCod1, K gffCod2);
	
	
 
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
	public T searchLOC(String LOCID){
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
	public T searchLOC(String chrID,int LOCNum)
	{
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
		for (ListAbs<T> lsGffDetail : Chrhash.values()) {
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
					gffDetail.tss2UpGene = distance(gffDetail, gffDetailUp, true);
					gffDetail.tes2DownGene = distance(gffDetail, gffDetailDown, false);
				}
				else {
					gffDetail.tss2UpGene = distance(gffDetail, gffDetailDown, false);
					gffDetail.tes2DownGene = distance(gffDetail, gffDetailUp, true);
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
