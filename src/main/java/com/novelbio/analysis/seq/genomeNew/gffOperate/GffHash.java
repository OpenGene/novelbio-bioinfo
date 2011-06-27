package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * ���Gff����Ŀ��Ϣ<br/>
 * �����GffHash��Ҫʵ��ReadGffarray��ͨ���÷�������������
 * @Chrhash hash��ChrID��--ChrList--GeneInforList(GffDetail��)
 * @locHashtable hash��LOCID��--GeneInforlist
 * @LOCIDList ˳��洢ÿ������Ż���Ŀ��
 */
public abstract class GffHash {

	
	private GffHash() {	}
	
	
	
	public GffHash(String gfffilename) throws Exception {
		ReadGffarray(gfffilename);
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
		LOCNumInfo[0]=gffLOCdetail.ChrID;
		ArrayList<GffDetailAbs> locArrayList=Chrhash.get(LOCNumInfo[0]);
		LOCNumInfo[1]=locArrayList.indexOf(gffLOCdetail)+"";
		return LOCNumInfo;
	}
	
	/**
	 * return searchLocation(chrID, Coordinate);
	 * 
	 * ��������� ����ChrID���������꣬�Լ�GffHash��<br>
	 * ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд, chr1,chr2,chr11<br>
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * û�ҵ��ͷ���null
	 */
	public abstract GffCodAbs searchLoc(String chrID, int Coordinate);
	
	
	/**
	 * ��������� ����ChrID���������꣬�Լ�GffHash��<br>
	 * ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд, chr1,chr2,chr11<br>
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * û�ҵ��ͷ���null
	 */
	protected GffCodAbs searchLocation(String chrID, int Coordinate) {
		String Chrpatten = "Chr\\w+";// Chr1�� chr2��
										// chr11����ʽ,ע�⻹��chrx֮��ģ�chr������Դ�"_"������˵������"_"�ָ�chr���ַ�
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
		Matcher matcher;
		matcher = pattern.matcher(chrID);
		if (!matcher.find()) {
			return null;
		}
		chrID = matcher.group().toLowerCase();
		ArrayList<GffDetailAbs> Loclist =  getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			return null;
		}
		return searchLocation(Loclist, chrID,Coordinate);
	}

	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * û�ҵ��ͷ���null
	 */
	private GffCodAbs searchLocation(ArrayList<GffDetailAbs> Loclist, String chrID, int Coordinate) {
		
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition(Loclist, Coordinate);// ���ַ�����peaknum�Ķ�λ
		if (locInfo[0] == 1) // ��λ�ڻ�����
		{
			GffCodAbs gffCodAbs = SearchLOCinside(Loclist, locInfo[1], locInfo[2],chrID,Coordinate);// ���Ҿ����ĸ��ں��ӻ���������
			gffCodAbs.geneChrHashListNum[0] = locInfo[1];

			if (locInfo[1] == -1)
				gffCodAbs.geneDetail[0] = null;
			else
				gffCodAbs.geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				gffCodAbs.geneDetail[1] = null;
			else
				gffCodAbs.geneDetail[1] = Loclist.get(locInfo[2]);

			gffCodAbs.geneChrHashListNum[1] = locInfo[2];
			return gffCodAbs;
		} else if (locInfo[0] == 2) {
			GffCodAbs gffCodAbs = SearchLOCoutside(Loclist, locInfo[1], locInfo[2],chrID,Coordinate);// ���һ����ⲿ��peak�Ķ�λ���
			if (locInfo[1] == -1)
				gffCodAbs.geneDetail[0] = null;
			else
				gffCodAbs.geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				gffCodAbs.geneDetail[1] = null;
			else
				gffCodAbs.geneDetail[1] = Loclist.get(locInfo[2]);

			gffCodAbs.geneChrHashListNum[0] = locInfo[1];
			gffCodAbs.geneChrHashListNum[1] = locInfo[2];
			return gffCodAbs;
		}
		return null;
	}

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition(ArrayList<GffDetailAbs> Loclist, int Coordinate) {
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < Loclist.get(beginnum).numberstart) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate > Loclist.get(endnum).numberstart) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			if (Coordinate < Loclist.get(endnum).numberend) {
				LocInfo[0] = 1;
				return LocInfo;
			} else {
				LocInfo[0] = 2;
				return LocInfo;
			}
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == Loclist.get(number).numberstart) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < Loclist.get(number).numberstart
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= Loclist.get(beginnum).numberend)// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		return LocInfo;
	}

	/**
	 * ���뱻���ǣ���� result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3 �ȼ���������Ϣ
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param beginnum
	 *            ��������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���
	 * @param endnum
	 *            �¸��������� -1��ʾ����û�л���
	 * @return
	 */
	protected abstract GffCodAbs SearchLOCinside(ArrayList<GffDetailAbs> loclist,
			int beginnum, int endnum, String chrID, int Coordinate);

	/**
	 * ���뱻���ǣ���� result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param beginnum
	 *            ��������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���
	 * @param endnum
	 *            �¸��������� -1��ʾ����û�л���
	 * @return
	 */
	protected abstract GffCodAbs SearchLOCoutside(ArrayList<GffDetailAbs> loclist,
			int beginnum, int endnum, String chrID, int Coordinate);

}
