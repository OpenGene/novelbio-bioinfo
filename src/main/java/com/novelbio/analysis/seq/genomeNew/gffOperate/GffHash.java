package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * ���Gff����Ŀ��Ϣ<br/>
 * �����GffHash��Ҫʵ��ReadGffarray��ͨ���÷�������������
 * @Chrhash hash��ChrID��--ChrList--GeneInforList(GffDetail��)
 * @locHashtable hash��LOCID��--GeneInforlist
 * @LOCIDList ˳��洢ÿ������Ż���Ŀ��
 */
public abstract class GffHash <T extends GffDetailAbs, K extends GffCodAbs<T>, M extends GffCodAbsDu<T, K>>{
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
	
	
	
	Logger logger = Logger.getLogger(GffHash.class);
	/**
	 * ��ϣ��LOC--LOCϸ��<br>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��<br>
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
	  * �����ж��LOCID����һ�����������������ж����ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene<br>
	 */
	protected HashMap<String,T> locHashtable;
	
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,T> getLocHashtable() {
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
	 * ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ�����"/"������
	 * ��ô��list�е�Ԫ����split("/")�ָ����locHashtable�Ϳ���ȡ��Ӧ��GffDetail��Ŀǰ��Ҫ��Peak�õ�
	 * ˳���ã����Ի��ĳ��LOC�ڻ����ϵĶ�λ��
	 * ����TigrGene��IDÿ������һ��LOCID��Ҳ����˵TIGR��ID����Ҫ�����и��Ȼ����Ҳû��ϵ
	 */
	public ArrayList<String> getLOCChrHashIDList() {
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
	protected HashMap<String,ArrayList<T>> Chrhash;
	
	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected HashMap<String,ArrayList<T>> getChrhash()
	{
		return Chrhash;
	}
	
	
	

	
	
	
	
	
	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	public K searchLocation(String chrID, int Coordinate) {
		ArrayList<T> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		int[] locInfo = LocPosition(chrID, Coordinate);// ���ַ�����peaknum�Ķ�λ
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
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		ArrayList<T> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		
		
		K gffCod1 = searchLocation(chrID, Math.min(cod1, cod2));
		K gffCod2 = searchLocation(chrID, Math.max(cod1, cod2));
		M gffCodDu = setGffCodDu(new ArrayList<T>(),gffCod1, gffCod2 );
		
		if (gffCodDu.gffCod1.getItemNumDown() < 0) {
			gffCodDu.lsgffDetailsMid = null;
		}
		else {
			for (int i = gffCodDu.gffCod1.getItemNumDown(); i <= gffCodDu.gffCod2.getItemNumUp(); i++) {
				gffCodDu.lsgffDetailsMid.add(Loclist.get(i));
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
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition(String chrID, int Coordinate) {
		ArrayList<T> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < Loclist.get(beginnum).getNumStart()) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate > Loclist.get(endnum).getNumStart()) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			if (Coordinate < Loclist.get(endnum).getNumStart()) {
				LocInfo[0] = 1;
				return LocInfo;
			} else {
				LocInfo[0] = 2;
				return LocInfo;
			}
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == Loclist.get(number).getNumStart()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < Loclist.get(number).getNumStart()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= Loclist.get(beginnum).getNumEnd())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		return LocInfo;
	}
	/**
	 * �ڶ�ȡ�ļ��������ʲô��Ҫ���õģ�����д��setOther();��������
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
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
		return  locHashtable.get(LOCID);
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
		String[] LOCNumInfo=new String[2];
		T gffLOCdetail=locHashtable.get(LOCID);
		LOCNumInfo[0]=gffLOCdetail.getChrID();
		ArrayList<T> locArrayList=Chrhash.get(LOCNumInfo[0]);
		LOCNumInfo[1]=locArrayList.indexOf(gffLOCdetail)+"";
		return LOCNumInfo;
	}
	/**
	 * �趨ÿ��GffDetail��tss2UpGene��tes2DownGene
	 */
	private void setItemDistance() {
		for (ArrayList<T> lsGffDetail : Chrhash.values()) {
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
				if (gffDetail.cis5to3) {
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
				return Math.abs(gffDetail1.getNumberstart() - gffDetail2.getNumberend());
			}
			else {
				return Math.abs(gffDetail1.getNumberend() - gffDetail2.getNumberstart());
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
