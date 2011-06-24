package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * peak��λ������Ϣ�Ļ�����,����ֱ�ӿ�������CG��Peak<br>
 * ������GffCodInfoGene
 * 
 * @author zong0jie
 * 
 */
public abstract class GffCodAbs {

	/**
	 * ���캯������ֵ
	 */
	GffCodAbs(String chrID, int Coordinate) {
		distancetoLOCStart[0] = -1000000000;
		distancetoLOCEnd[0] = -1000000000;
		geneChrHashListNum[0] = -1000000000;
		distancetoLOCStart[1] = -1000000000;
		distancetoLOCEnd[1] = -1000000000;
		geneChrHashListNum[1] = -1000000000;
		this.chrID = chrID;
		this.Coordinate = Coordinate;
	}

	String chrID = "";
	int Coordinate = -1;
	/**
	 * ����Ⱦɫ��
	 * @return
	 */
	public String getChrID() {
		return chrID;
	}
	/**
	 * ���ؾ�������
	 * @return
	 */
	public int getCoord() {
		return Coordinate;
	}
	/**
	 * �����Ƿ�鵽 ���ҵ�/û�ҵ�
	 */
	public boolean result = false;

	/**
	 * ��λ��� ��Ŀ��/��Ŀ��
	 */
	public boolean insideLOC = false;

	/**
	 * ����Ŀ/��һ����Ŀ�ķ���
	 */
	public boolean begincis5to3 = false;

	/**
	 * ��һ����Ŀ�ķ��򣬽�������λ����Ŀ��ʱ
	 */
	public boolean endcis5to3 = false;

	/**
	 * ����LOCID��ΪchrHash����ı�ţ�ע�⣺����Ų�һ����LOCIDlist��ı����ͬ��Ŀǰ����UCSCgene�в�ͬ��
	 * UCSCgeneҪ��ͨ��split("/")�и���ܽ���locHashtable���� �� 0������Ŀ��� 1: �ϸ���Ŀ��� 2���¸���Ŀ���
	 * �������ǰ/��û����Ӧ�Ļ���(Ʃ����������ǰ��)����ô��Ӧ��LOCIDΪnull
	 */
	public String[] LOCID = new String[3];

	/**
	 * ���굽��Ŀ����λ��,����������<br/>
	 * Ϊint[2]��<br>
	 * <b>�������Ŀ��</b><br>
	 * 0:����Ϊ�ͱ���Ŀ���ľ��룬��������<br>
	 * 1��-1<br>
	 * <br>
	 * <b>�������Ŀ�䣬����������Ŀ�ľ��룬�������û����/����Ŀ������Ӧ��Ϊ0</b><br>
	 * 0:������ϸ���Ŀ���ľ���<br>
	 * ����ϸ���ĿΪ������Ϊ����+<br>
	 * ����ϸ���ĿΪ������Ϊ����-<br>
	 * <br>
	 * 1:������¸���Ŀ���ľ���<br>
	 * ����¸�����Ϊ������Ϊ����-<br/>
	 * ����¸�����Ϊ������Ϊ����+<br/>
	 */
	public int[] distancetoLOCStart = new int[2];

	/**
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * Ϊint[2]��<br>
	 * <b>�������Ŀ��</b><br>
	 * 0:����Ϊ�ͱ���Ŀ�յ�ľ��룬��������<br>
	 * 1��-1<br>
	 * <br>
	 * <b>�������Ŀ�䣬������/����Ŀ�ľ��룬�������û����/����Ŀ������Ӧ��Ϊ0
	 * ������Ҫ����û����/����Ŀ����geneChrHashListNum���������ӦֵΪ-1����˵��û�и���</b><br>
	 * 0:������ϸ���Ŀ�յ�ľ���<br>
	 * ����ϸ���ĿΪ������Ϊ����-<br>
	 * ����ϸ���ĿΪ������Ϊ����+<br>
	 * <br>
	 * 1:������¸���Ŀ�յ�ľ���<br>
	 * ����¸���ĿΪ������Ϊ����+<br/>
	 * ����¸���ĿΪ������Ϊ����-<br/>
	 */
	public int[] distancetoLOCEnd = new int[2];

	/**
	 * 0: �������Ŀ�ڣ�Ϊ����Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ������)
	 */
	public GffDetail[] geneDetail = new GffDetail[2];

	/**
	 * ���ȿ��ϸ��������¸����� 0: �������Ŀ�ڣ�Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	public int[] geneChrHashListNum = new int[2];

	/**
	 * ��������� ����ChrID���������꣬�Լ�GffHash��<br>
	 * ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд, chr1,chr2,chr11<br>
	 * ��û�����Ϣ-�洢��GffCoordiatesInfo����<br>
	 * ������Ҫ�Ľ�����ò�ͬ��GffCodInfo�������<br>
	 * ����GffsearchGene��������GffCodinfoGene����
	 */
	public void searchLocation(GffHash gffHash) {
		String Chrpatten = "Chr\\w+";// Chr1�� chr2��
										// chr11����ʽ,ע�⻹��chrx֮��ģ�chr������Դ�"_"������˵������"_"�ָ�chr���ַ�
		/**
		 * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
		Matcher matcher;
		matcher = pattern.matcher(chrID);
		if (!matcher.find()) {
			result = false;
			return;
		}
		chrID = matcher.group().toLowerCase();

		Hashtable<String, ArrayList<GffDetail>> LocHash = gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			result = false;
			return;
		}
		searchLocation(Loclist);
	}

	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 */
	private void searchLocation(ArrayList<GffDetail> Loclist) {
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition(Loclist);// ���ַ�����peaknum�Ķ�λ
		if (locInfo[0] == 1) // ��λ�ڻ�����
		{
			SearchLOCinside(Loclist, locInfo[1], locInfo[2]);// ���Ҿ����ĸ��ں��ӻ���������
			geneChrHashListNum[0] = locInfo[1];

			if (locInfo[1] == -1)
				geneDetail[0] = null;
			else
				geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				geneDetail[1] = null;
			else
				geneDetail[1] = Loclist.get(locInfo[2]);

			geneChrHashListNum[1] = locInfo[2];
			return;
		} else if (locInfo[0] == 2) {
			SearchLOCoutside(Loclist, locInfo[1], locInfo[2]);// ���һ����ⲿ��peak�Ķ�λ���
			if (locInfo[1] == -1)
				geneDetail[0] = null;
			else
				geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				geneDetail[1] = null;
			else
				geneDetail[1] = Loclist.get(locInfo[2]);

			geneChrHashListNum[0] = locInfo[1];
			geneChrHashListNum[1] = locInfo[2];
			return;
		}
		return;
	}

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition(ArrayList<GffDetail> Loclist) {
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
	 * @param i
	 *            ��������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���
	 * @param j
	 *            �¸��������� -1��ʾ����û�л���
	 * @return
	 */
	protected abstract void SearchLOCinside(ArrayList<GffDetail> loclist,
			int i, int j);

	/**
	 * ���뱻���ǣ���� result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param i
	 *            ��������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���
	 * @param j
	 *            �¸��������� -1��ʾ����û�л���
	 * @return
	 */
	protected abstract void SearchLOCoutside(ArrayList<GffDetail> loclist,
			int i, int j);

	/**
	 * ��Ҫ���� ���������������ظû�����Ϣ,һ��GffDetailList�� ������Ҫ�Ľ�����ò�ͬ��GffDetail�������<br>
	 * ����GffsearchGene��������GffDetailGene����
	 * 
	 * @param LocID
	 */
	public static GffDetail LOCsearch(String LocID, GffHash gffHash) {
		return gffHash.LOCsearch(LocID);
	}

}
