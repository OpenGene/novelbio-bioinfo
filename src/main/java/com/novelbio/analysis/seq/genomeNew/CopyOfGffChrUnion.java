package com.novelbio.analysis.seq.genomeNew;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.generalConf.Species;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashPlantGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashUCSCgene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataStructure.MathComput;






 
/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * @author zong0jie
 *
 */
public class CopyOfGffChrUnion {
	


//////////////////////////////////////////////////�����趨/////////////////////////////////////////////////////////

	/**
	 * �趨�����ת¼���TSS���γ��ȣ�Ĭ��Ϊ3000bp
	 */
	static int UpStreamTSSbp=3000;
	
	/**
	 * �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 */
	static int DownStreamTssbp=2000;
	/**
	 * �趨�����ת¼���TSS���γ��ȣ�Ĭ��Ϊ3000bp
	 * ���ͳ��ʱ����TSS����֮�ڵ�peak������������
	 */
	public void setDownStreamTssbp(int downStreamTssbp) {
		DownStreamTssbp = downStreamTssbp;
	}
	
	/**
	 * �趨�����ת¼���TSS���γ��ȣ�Ĭ��Ϊ3000bp
	 * ���ͳ��ʱ����TSS����֮�ڵ�peak������������
	 */
	public void setUpstreamTSSbp(int upstreamTSSbp) {
		UpStreamTSSbp = upstreamTSSbp;
	}
	
	/**
	 * ����У��reads�����Ĳ�������Ϊ���tss����readsֱ�ӳ���ReadsNum���С�������������ͼ
	 * ��ʱ������ܹ�����һ���Ƚϴ���������������������Ӧ�ñȽϽӽ��������������趨Ϊһ����
	 */
	static int fold=1000000;
	
	/**
	 * �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 * ����˵������������������100bp����Ϊ��3��UTR
	 * ��ô��ͳ��peak�����ʱ����������������û�б�peak�����ǣ���ͳ�Ƹ�������reads�����
	 */
	static int GeneEnd3UTR = 100;
	/**
	 * �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 * ����˵������������������100bp����Ϊ��3��UTR,���ͳ��ʱ���ڻ���β��֮�ڵ�peak������������ 
	 * ��ô��ͳ��peak�����ʱ����������������û�б�peak�����ǣ���ͳ�Ƹ�������reads�����
	 */
	public void setGeneEnd3UTR(int geneEnd3UTR) {
		GeneEnd3UTR = geneEnd3UTR;
	}
	
	/**
	 * �趨motif�Ļ�ʽ��Ĭ��ΪCANNTG
	 */
	public String motifregex = "CA\\w{2}TG";
	
	
	////////////////////////////////////////////�õ�����/////////////////////////////////////////////////////////
 
	/**
	 * �����õ���һ��gffHash��������ȡgff�ļ�
	 */
	protected GffHash gffHash;
	/**
	 * �����õ���һ��mapreads����������map�ļ�
	 */
	protected MapReads mapReads=null;
	/**
	 * �����õ���һ��ChrStringHash��������ȡ����
	 */
	protected ChrStringHash chrStringHash=null;
	/**
	 * mapping����reads������
	 */
	protected long readsNum = 0;
	
	
	///////////////////////////////////////////׼������ Loading ��   ��////////////////////////////////////////////////////////////
	/**
	 * ָ����Ӧ�Ĵ�ʵ����Gffhash����
	 * ��ȡ��Ӧ��gff�ļ�
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","TAIR","CG","UCSC","Repeat"�⼸��<br>
	 * �Ժ��������ӵ�gffhash��������Ҫ�������<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	 public void loadGff(String gffClass,String Gfffilename) throws Exception
	 {
		 if (gffClass.equals("TIGR")) {
			 gffHash=new GffHashPlantGene(Gfffilename, Species.RICE);
		 }
		 if (gffClass.equals("TAIR")) {
			 gffHash=new GffHashPlantGene(Gfffilename,  Species.ARABIDOPSIS);
		 }
		 else if (gffClass.equals("CG")) {
			 gffHash=new GffHashCG(Gfffilename);
		 }
		 else if (gffClass.equals("UCSC")) {
			 gffHash=new GffHashUCSCgene(Gfffilename);
		 }
		 else if (gffClass.equals("Repeat")) {
			 gffHash=new GffHashRepeat(Gfffilename);
		 }
		
	 }
	
	/**
	 * �趨����������Ϣ���ļ��У��ļ�����Ӧ��ÿ��Ⱦɫ��һ��fasta�ļ������С��������һ����ϣ��-������Ϣ<br>
	 * <b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * ������Ϣ��ChrStringHash��
	 * @param Chrfilename
	 */
	public void loadChr(String ChrFilePath) 
	{
	   try { chrStringHash = new ChrStringHash(ChrFilePath); } catch (Exception e) {e.printStackTrace();} 
	}
	
	/**
	  * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * @param colChrID ChrID�ڵڼ��У���1��ʼ
	 * @param colStartNum mapping����ڵڼ��У���1��ʼ
	 * @param colEndNum mapping�յ��ڵڼ��У���1��ʼ
	 * @param invNum ÿ������λ����
	 * @param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
	 */
	public void loadMap(String mapFile,String chrFilePath,int invNum,int tagLength, boolean uniqReads) 
	{
		mapReads=new MapReads(invNum, chrFilePath, mapFile);
		try {
			if (tagLength > 20) {
				mapReads.setTagLength(tagLength);
			}
			readsNum = mapReads.ReadMapFile(uniqReads);
		} catch (Exception e) {	e.printStackTrace();	}
	}
	

	
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @param lsGeneID
	 */
	public void getATGDensity(ArrayList<String> lsGeneID) {
		GffHashGene gffHashGene = (GffHashGene)gffHash;
		TreeMap<Integer, double[]> treeATG = new TreeMap<Integer, double[]>();
		for (String string : lsGeneID) {
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(string);
			gffDetailGene.
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getCoordSearchLongest();
			gffGeneIsoInfo.getATGSsite();
			
			
		}
	}
	
}
