package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
/**
 * ר�Ŵ洢UCSC��gene�����ļ�
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * GffDetailList���б���ÿ�����������յ��CDS������յ� 
 * @author zong0jie
 * @GffHashGene��ȡGff�ļ���ÿ��������Ի��������Ϣ
 * ������<br>
 * ��������㣬����UCSC konwn geneĳλ�����л�����ǰ��exon�����<br>
 * �������յ㣬����UCSC konwn geneĳλ�����л��������intron���յ�<br>
 * ����������Ⱦɫ����<br>
 * ������Ĳ�ͬת¼��<br>
 * ������ת¼����<br>
 * �����еļ�����������Gff�����й�<br>
 */
public class GffDetailGene extends GffDetailAbs
{
	
	public final static String INTRON = "intron";
	public final static String EXON_WITHOUT_UTR = "exon_without_utr";
	public final static String EXON = "exon";
	public final static String UTR5 = "5utr";
	public final static String UTR3 = "3utr";
	
	/**
	 * ˳��洢ÿ��ת¼���ĵ��������
	 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//�洢�ɱ���ӵ�mRNA
	/**
	 * ˳��洢���Һ��ÿ��ת¼���ĵ��������
	 */
	private ArrayList<GffGeneIsoSearch> lsGffGeneIsoInfoSearch = null;//�洢�ɱ���ӵ�mRNA
	/**
	 * ˳��洢ÿ��ת¼��������
	 */
	private ArrayList<String> lsIsoName = new ArrayList<String>();
	
	
	/**
	 * 
	 * @param chrID
	 * @param locString
	 * @param cis5to3
	 */
	protected GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	/**
	 * �����һ��ת¼������exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addExonUCSC(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonUCSC(locStart, locEnd);
	}
	/**
	 * ���ˮ�����Ͻ��GFF�ļ�
	 * �����һ��ת¼������exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addExonGFF(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFF(locStart, locEnd);
	}
	/**
	 * ���ˮ�����Ͻ��GFF�ļ�
	 * ��ת¼������exon���꣬GFF3��exon�ĸ�ʽ��
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�
	 * ������exon��ʱ�������CDS��UTR֮�������ŵģ���ô�ͽ���CDS��UTR����һ�𣬷���һ��exon�� ����������Ͱ�ԭ������
	 */
	protected void addExonGFFCDSUTR(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFFCDSUTR(locStart, locEnd);
	}

	
	
	
	/**
	 * �����һ��ת¼������ATG��UAG���꣬<br>
	 * ������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addATGUAG(int atg, int uag)
	{

		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		if (Math.abs(atg - uag)<=1) {
			gffGeneIsoInfo.mRNA = false;
			atg = Math.min(atg, uag);
			uag = Math.min(atg, uag);
		}
		if (cis5to3) {
			gffGeneIsoInfo.ATGsite = Math.min(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.max(atg, uag);
		}
		else {
			gffGeneIsoInfo.ATGsite = Math.max(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.min(atg, uag);
		}
	}
	/**
	 * ֱ������ת¼����֮����addcds()��������ת¼������exon
	 */
	protected void addsplitlist(String splitName) {
		GffGeneIsoInfo gffGeneIsoInfo = new GffGeneIsoInfo(splitName,cis5to3,this);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		lsIsoName.add(splitName);
	}
	/**
	 * @return ����ת¼������Ŀ
	 */
	public int getSplitlistNumber() {
		return lsGffGeneIsoInfos.size();
    }
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼���ľ�����Ϣ
     */
    public GffGeneIsoInfo getIsolist(int splitnum)
    {  
    	return lsGffGeneIsoInfos.get(splitnum);//include one special loc start number to end number	
    }
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     */
    public GffGeneIsoInfo getIsolist(String splitID)
    {  
    	return lsGffGeneIsoInfos.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
    }

    private int getLongestSplitID() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
			lslength.add(Math.abs(lsExon.get(0)[0] - lsExon.get(lsExon.size()-1)[1]));
		}
		int max = lslength.get(0); int id = 0;
		for (int i = 0; i < lslength.size(); i++) {
			if (lslength.get(i) > max)
			{
				max = lslength.get(i);
				id = i;
			}
		}
		return id;
	}
    
	/**
	 * ��øû��������һ��ת¼������Ϣ
	 * 
	 * @return <br>
	 */
	public GffGeneIsoInfo getLongestSplit() {
		int id = getLongestSplitID();
		return lsGffGeneIsoInfos.get(id);
	}
	/**
	 * ��������Ҿ����ת¼����Ϣ�����������Ϣ��ͬ���򷵻���ǰ����Ϣ
	 * @param coord
	 */
	public ArrayList<GffGeneIsoSearch> getCoordSearchLs() {
		if (lsGffGeneIsoInfoSearch != null && coord == lsGffGeneIsoInfoSearch.get(0).getCoord()) {
			return lsGffGeneIsoInfoSearch;
		}
		lsGffGeneIsoInfoSearch = new ArrayList<GffGeneIsoSearch>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			GffGeneIsoSearch gffGeneIsoSearch = null;
			if (cis5to3) {
				gffGeneIsoSearch = new GffGeneIsoSearchCis(gffGeneIsoInfo, coord);
			}
			else {
				gffGeneIsoSearch = new GffGeneIsoSearchTrans(gffGeneIsoInfo, coord);
			}
			lsGffGeneIsoInfoSearch.add(gffGeneIsoSearch);
		}
		return lsGffGeneIsoInfoSearch;
	}
	/**
	 * ��������Ҿ����ת¼����Ϣ
	 * @param coord
	 */
	public GffGeneIsoSearch getCoordSearchLongest() {
		ArrayList<GffGeneIsoSearch> lsGffGeneIsoSearchs = getCoordSearchLs();
		int id = getLongestSplitID();
		return lsGffGeneIsoSearchs.get(id);
	}
	/**
	 * ��������Ҿ����ת¼����Ϣ
	 * @param coord
	 */
	public GffGeneIsoSearch getCoordSearchName(String splitName) {
		ArrayList<GffGeneIsoSearch> lsGffGeneIsoSearchs = getCoordSearchLs();
		return lsGffGeneIsoSearchs.get(lsIsoName.indexOf(splitName));
	}
    /**
     * ��øû��������һ��ת¼���Ĳ����������Ϣ���Ѿ����ǹ�������������
     * @param type ָ��ΪINTRON,UTR5�ȣ��Ǹ���ĳ����������ֵ
     * @param num ���typeΪ"Intron"��"Exon"��ָ���ڼ���������������򷵻�0
     * num Ϊʵ�ʸ�����
     * ���5UTRֱ�ӷ���ȫ��5UTR
     * 3UTRҲֱ�ӷ���ȫ��3UTR
     * @return 
     */
	public int getTypeLength(String type,int num)  
	{
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
		int exonNum = lsExon.size();
		//TODO ���������Ҫ����0
		if (type.equals(INTRON)) {
			return Math.abs(lsExon.get(num)[0] - lsExon.get(num-1)[1]) - 1;
		}
		if (type.equals(EXON)) {
			return Math.abs(lsExon.get(num)[1] - lsExon.get(num)[0]) + 1;
		}
		if (type.equals(UTR5)) 
		{
			int FUTR=0;
			if (cis5to3) { //0    1     2     3     4     5   ÿ���������� 1 > 0      0    atg   1
				for (int i = 0; i <exonNum; i++) 
				{
					if(lsExon.get(i)[1] < gffGeneIsoInfo.getATGSsite())    // 0       1   atg    
						FUTR = FUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[0] < gffGeneIsoInfo.getATGSsite() && lsExon.get(i)[1] >= gffGeneIsoInfo.getATGSsite())  //     0    atg    1 
						FUTR = FUTR + gffGeneIsoInfo.getATGSsite() - lsExon.get(i)[0];
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getATGSsite())  //     atg   0       1   
						break;
				}
			}
			else { //5  4   3   2   1   0    ÿ���������� 0 > 1     1    gta   0
				for (int i = 0; i < exonNum; i++) 
				{
					if(lsExon.get(i)[1] > gffGeneIsoInfo.getATGSsite())  // gta   1      0
						FUTR = FUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] > gffGeneIsoInfo.getATGSsite()  && lsExon.get(i)[1] <= gffGeneIsoInfo.getATGSsite() ) //   1     gta      0
						FUTR = FUTR + lsExon.get(i)[0] - gffGeneIsoInfo.getATGSsite();
					else if (lsExon.get(i)[0] <= gffGeneIsoInfo.getATGSsite())   //   1        0      gta 
						break;
				}
			}
			return FUTR;
		}
		if (type.equals(UTR3)) 
		{
			int TUTR=0;
			if (cis5to3) { //0    1     2     3     4     5   ÿ���������� 0 < 1      0    uag   1
				for (int i = exonNum - 1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] > gffGeneIsoInfo.getUAGsite())  //      uag     0      1
						TUTR = TUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[1] > gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[0] <= gffGeneIsoInfo.getUAGsite())  //     0     uag    1
						TUTR = TUTR + lsExon.get(i)[1] - gffGeneIsoInfo.getUAGsite();
					else if (lsExon.get(i)[1] <= gffGeneIsoInfo.getUAGsite())   //   0      1     uag   
						break;
				}
			}
			else { //5  4   3   2   1   0    ÿ���������� 0 > 1      1    gau  0
				for (int i = exonNum-1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] < gffGeneIsoInfo.getUAGsite())  //     1      0     gau
						TUTR = TUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[1] < gffGeneIsoInfo.getUAGsite())  //     1    gau    0     
						TUTR = TUTR + gffGeneIsoInfo.getUAGsite() - lsExon.get(i)[1];
					else if (lsExon.get(i)[1] >= gffGeneIsoInfo.getUAGsite())   //   gau   1      0     
						break;
				}
			}
			return TUTR;
		}
		return -1000000;
	}
	
}