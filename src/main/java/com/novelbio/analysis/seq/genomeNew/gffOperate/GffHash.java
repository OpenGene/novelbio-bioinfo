package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
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
	 * ��ϣ��LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br/>
	 */
	protected Hashtable<String,GffDetailAbs> locHashtable;
	
	/**
	 * ���ع�ϣ�� LOC--LOCϸ��<br/>
	 * ���ڿ��ٽ�LOC��Ŷ�Ӧ��LOC��ϸ��
	 * hash��LOCID��--GeneInforlist������LOCID�������Ļ����� <br/>
	 */
	public Hashtable<String,GffDetailAbs> getLocHashtable() {
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
	protected Hashtable<String,ArrayList<GffDetailAbs>> Chrhash;
	
	/**
	 * ���������Ĳ�����hash��<br>
	 * �����ϣ�����洢
	 * hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br>
	 * ����ChrIDΪСд��
	 * ����Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
	 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 */
	protected Hashtable<String,ArrayList<GffDetailAbs>> getChrhash()
	{
		return Chrhash;
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
	public abstract GffDetailAbs LOCsearch(String LOCID);
	
	/**
	 * ��Ҫ����
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * ����chrID�͸�Ⱦɫ���ϵ�λ�ã�����GffDetail��Ϣ
	 * @param chrID Сд
	 * @param LOCNum ��Ⱦɫ���ϴ���ѰLOC��int���
	 * @return  ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public abstract GffDetailAbs LOCsearch(String chrID,int LOCNum);
	
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
	 * return searchLocation(chrID, Coordinate);
	 * 
	 * ��������� ����ChrID���������꣬�Լ�GffHash��<br>
	 * ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд, chr1,chr2,chr11<br>
	 * ֻҪ��chrhash�����괫����Ӧ��GffCod�༴��
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * û�ҵ��ͷ���null
	 */
	public abstract GffCodAbs searchLoc(String chrID, int Coordinate);
	


}
