package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
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
public abstract class GffHash {	
	
	Logger logger = Logger.getLogger(GffHash.class);
	public GffHash(String gfffilename) {
		try {
			ReadGffarray(gfffilename);
		} catch (Exception e) {
			logger.error("read file error"+gfffilename);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ��ϣ��LOC--LOCϸ��<br>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��<br>
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
	  * �����ж��LOCID����һ�����������������ж����ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene<br>
	 */
	protected HashMap<String,GffDetailAbs> locHashtable;
	
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public HashMap<String,GffDetailAbs> getLocHashtable() {
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
	protected HashMap<String,ArrayList<GffDetailAbs>> Chrhash;
	
	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected HashMap<String,ArrayList<GffDetailAbs>> getChrhash()
	{
		return Chrhash;
	}
	
	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * û�ҵ��ͷ���null
	 */
	public GffCodAbs searchLocation(String chrID, int Coordinate) {
		ArrayList<GffDetailAbs> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition(chrID, Coordinate);// ���ַ�����peaknum�Ķ�λ
		if (locInfo == null) {
			return null;
		}
		GffCodAbs gffCodAbs = setGffCodAbs(chrID, Coordinate);
		if (locInfo[0] == 1) // ��λ�ڻ�����
		{
			gffCodAbs.gffDetailThis = Loclist.get(locInfo[1]); 
			gffCodAbs.gffDetailThis.setCoord(Coordinate);
			gffCodAbs.booFindCod = true;
			gffCodAbs.ChrHashListNumThis = locInfo[1];
			gffCodAbs.insideLOC = true;
			if (locInfo[1] - 1 >= 0) {
				gffCodAbs.gffDetailUp =  Loclist.get(locInfo[1]-1);
				gffCodAbs.gffDetailUp.setCoord(Coordinate);
				gffCodAbs.ChrHashListNumUp = locInfo[1]-1;
				
			}
			if (locInfo[2] != -1) {
				gffCodAbs.gffDetailDown = Loclist.get(locInfo[2]);
				gffCodAbs.gffDetailDown.setCoord(Coordinate);
				gffCodAbs.ChrHashListNumDown = locInfo[2];
			}
		} else if (locInfo[0] == 2) {
			gffCodAbs.insideLOC = false;
			if (locInfo[1] >= 0) {
				gffCodAbs.gffDetailUp =  Loclist.get(locInfo[1]);
				gffCodAbs.gffDetailUp.setCoord(Coordinate);
				gffCodAbs.ChrHashListNumUp = locInfo[1];		
			}
			if (locInfo[2] != -1) {
				gffCodAbs.gffDetailDown = Loclist.get(locInfo[2]);
				gffCodAbs.gffDetailDown.setCoord(Coordinate);
				gffCodAbs.ChrHashListNumDown = locInfo[2];
			}
		}
		return gffCodAbs;
	}

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition(String chrID, int Coordinate) {
		ArrayList<GffDetailAbs> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
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
		if (Coordinate <= Loclist.get(beginnum).getNumStart())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		return LocInfo;
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
	protected abstract void ReadGffarray(String gfffilename) throws Exception;

	/**
	 * ��Ҫ����
	 * ����ĳ���ض�LOC����Ϣ
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID ����ĳLOC�����ƣ�ע��������һ���̵����֣�Ʃ����UCSC�����У�����locstring���ֺü�����������һ������֣����ǵ����Ķ̵�����
	 * @return ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public abstract GffDetailAbs searchLOC(String LOCID);
	
	/**
	 * ��Ҫ����
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * ����chrID�͸�Ⱦɫ���ϵ�λ�ã�����GffDetail��Ϣ
	 * @param chrID Сд
	 * @param LOCNum ��Ⱦɫ���ϴ���ѰLOC��int���
	 * @return  ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public abstract GffDetailAbs searchLOC(String chrID,int LOCNum);
	
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
		GffDetailAbs gffLOCdetail=locHashtable.get(LOCID);
		LOCNumInfo[0]=gffLOCdetail.getChrID();
		ArrayList<GffDetailAbs> locArrayList=Chrhash.get(LOCNumInfo[0]);
		LOCNumInfo[1]=locArrayList.indexOf(gffLOCdetail)+"";
		return LOCNumInfo;
	}
	
	/**
	 * �򵥵�new һ��GffCodAbsȻ������chrID��Coordinate��������<br>
	 * exmple:<br>
	 * return new GffCodCG(chrID, Coordinate);<br>
	 */
	protected abstract GffCodAbs setGffCodAbs(String chrID, int Coordinate);
	
	

}
