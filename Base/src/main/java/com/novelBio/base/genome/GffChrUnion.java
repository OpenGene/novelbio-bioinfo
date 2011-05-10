package com.novelBio.base.genome;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MaskFormatter;

import com.novelBio.base.dataStructure.MathComput;
import com.novelBio.base.genome.getChrSequence.ChrSearch;
import com.novelBio.base.genome.getChrSequence.ChrStringHash;
import com.novelBio.base.genome.gffOperate.GffCodInfo;
import com.novelBio.base.genome.gffOperate.GffCodInfoUCSCgene;
import com.novelBio.base.genome.gffOperate.GffDetail;
import com.novelBio.base.genome.gffOperate.GffDetailUCSCgene;
import com.novelBio.base.genome.gffOperate.GffHash;
import com.novelBio.base.genome.gffOperate.GffHashCG;
import com.novelBio.base.genome.gffOperate.GffHashGene;
import com.novelBio.base.genome.gffOperate.GffHashPeak;
import com.novelBio.base.genome.gffOperate.GffHashPlantGene;
import com.novelBio.base.genome.gffOperate.GffHashRepeat;
import com.novelBio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelBio.base.genome.gffOperate.Gffsearch;
import com.novelBio.base.genome.gffOperate.GffsearchCG;
import com.novelBio.base.genome.gffOperate.GffsearchGene;
import com.novelBio.base.genome.gffOperate.GffsearchPeak;
import com.novelBio.base.genome.gffOperate.GffsearchRepeat;
import com.novelBio.base.genome.gffOperate.GffsearchUCSCgene;
import com.novelBio.base.genome.mappingOperate.MapReads;
import com.novelbio.generalConf.NovelBioConst;






 
/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * @author zong0jie
 *
 */
public class GffChrUnion {
	


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
	public void setDownStreamTssbp(int DownStreamTssbp) {
		this.DownStreamTssbp=DownStreamTssbp;
	}
	
	/**
	 * �趨�����ת¼���TSS���γ��ȣ�Ĭ��Ϊ3000bp
	 * ���ͳ��ʱ����TSS����֮�ڵ�peak������������
	 */
	public void setUpstreamTSSbp(int upstreamTSSbp) {
		UpStreamTSSbp=upstreamTSSbp;
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
	static int GeneEnd3UTR=100;
	/**
	 * �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 * ����˵������������������100bp����Ϊ��3��UTR,���ͳ��ʱ���ڻ���β��֮�ڵ�peak������������ 
	 * ��ô��ͳ��peak�����ʱ����������������û�б�peak�����ǣ���ͳ�Ƹ�������reads�����
	 */
	public void setGeneEnd3UTR(int geneEnd3UTR) {
		GeneEnd3UTR=geneEnd3UTR;
	}
	
	/**
	 * �趨motif�Ļ�ʽ��Ĭ��ΪCANNTG
	 */
	public String motifregex="CA\\w{2}TG";
	
	
	////////////////////////////////////////////�õ�����/////////////////////////////////////////////////////////
 
	/**
	 * �����õ���һ��gffHash��������ȡgff�ļ�
	 */
	protected GffHash gffHash;
	/**
	 * �����õ���һ��gffsearch����������gffHash��
	 */
	protected Gffsearch gffSearch=null;
	/**
	 * �����õ���һ��mapreads����������map�ļ�
	 */
	protected MapReads mapReads=null;
	
	/**
	 * mapping����reads������
	 */
	protected long readsNum = 0;
	
	
	///////////////////////////////////////////׼������ Loading ��   ��////////////////////////////////////////////////////////////
	/**
	 * ָ����Ӧ�Ĵ�ʵ����Gffhash����
	 * ��ȡ��Ӧ��gff�ļ�
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","TAIR","CG","UCSC","Peak","Repeat"�⼸��<br>
	 * �Ժ��������ӵ�gffhash��������Ҫ�������<br>
	 * @param Gfffilename
	 */
	 public void loadGff(String gffClass,String Gfffilename)
	 {
		 if (gffClass.equals("TIGR")) 
		 {
			 gffHash=new GffHashPlantGene();
			 //���ô��ݣ��������gffHashҲ��ı�
			GffHashPlantGene gffHash2 = (GffHashPlantGene)gffHash;
			gffHash2.GeneName= "LOC_Os\\d{2}g\\d{5}";
			gffHash2.splitmRNA = "(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";
			 gffSearch=new GffsearchUCSCgene();
		 }
		 if (gffClass.equals("TAIR")) 
		 {
			 gffHash=new GffHashPlantGene();
			 gffSearch=new GffsearchUCSCgene();
		 }
		 else if (gffClass.equals("CG")) 
		 {
			 gffHash=new GffHashCG();
			 gffSearch=new GffsearchCG();
		 }
		 else if (gffClass.equals("UCSC")) 
		 {
			 gffSearch=new GffsearchUCSCgene();
			 gffHash=new GffHashUCSCgene();
		 }
		 else if (gffClass.equals("Peak")) 
		 {
			 gffHash=new GffHashPeak();
			 gffSearch=new GffsearchPeak();
		 }
		 else if (gffClass.equals("Repeat")) 
		 {
			 gffHash=new GffHashRepeat();
			 gffSearch=new GffsearchRepeat();
		 }
		 try {
			gffHash.ReadGffarray(Gfffilename);
		} catch (Exception e) {e.printStackTrace();
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
	    try {    ChrStringHash.setChrFilePath(ChrFilePath);  } catch (Exception e) {  e.printStackTrace();   }	
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
	public void loadMap(String mapFile,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength) 
	{
		mapReads=new MapReads();
		try {
			readsNum = mapReads.ReadMapFile(mapFile, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum);
			if (tagLength>20) {
				mapReads.setTagLength(tagLength);
			}
			
		} catch (Exception e) {	e.printStackTrace();	}
	}
	

	////////////////////////////////////��   ȡ   ��   ��//////////////////////////////////////////////////////////////////////////
	/**
	 * ����Item�����γ��ȣ����ػ�õ�����������item���ľ��룬ָ���Ƿ�Ҫ��������������
	 * @param LOCID item��������gffHash�в�ͬ��LOCID��
	 * @param length
	 * @param considerDirection ����������
	 * @param direction �����������������ôtrue����ȫ������,false����ȫ�ַ��򡣷��򷵻ظû�������/����
	 * ���������������ôtrue���ظû�������false���ظû�����
	 * @return
	 */
	public String getUpItemSeq(String Item,int length,boolean considerDirection,boolean direction)
	{
		GffDetail locinfo = Gffsearch.LOCsearch(Item, gffHash);
		if(locinfo==null)
			 return null;
			int StartNum=0;
			if(considerDirection)//���������򣬷��صĶ��Ǳ����������
			{
			  if(locinfo.cis5to3)
			  {
				  StartNum=locinfo.numberstart;
				  return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			  }
			  else 
			  {
				  StartNum=locinfo.numberend;	
				  return	ChrSearch.getSeq(!direction,locinfo.ChrID, StartNum, StartNum+length);
			  }
			}
			else //�����������򣬷��صľ���Ĭ���������
			{
				if(locinfo.cis5to3)
				{
					StartNum=locinfo.numberstart;
					return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
				}
				else 
				{
					StartNum=locinfo.numberend;	
					return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum, StartNum+length);
				}
			}
	}
	/**
	 * ����geneID�����γ��ȣ����ػ�õ�����������gene��Tss���ľ��룬ָ���Ƿ�Ҫ��������������
	 * ע��geneһ����GffDetailUCSCgene��ĿǰTIGR��TAIR��UCSC���������
	 * @param LOCID item����ע���LOCID������gff�ļ��г��֣������������gene symbol�Ļ�����Ҫ�������ݿ��ٽ�һ����ר������symobl��gffID�Ķ�Ӧ
	 * @param length
	 * @param considerDirection ����������
	 * @param direction �����������������ôtrue����ȫ������,false����ȫ�ַ��򡣷��򷵻ظû�������/����
	 * @param GffClass Ŀǰ��TIGR,TAIR,UCSC������
	 * ���������������ôtrue���ظû�������false���ظû�����
	 * @return
	 */
	public String getUpGenSeq(String LOCID,int length,boolean considerDirection,boolean direction,String GffClass)
	{
		if (GffClass.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR)) {
			return getUpItemSeq(LOCID, length, considerDirection, direction);
		}
		GffDetailUCSCgene locinfo = (GffDetailUCSCgene)Gffsearch.LOCsearch(LOCID, gffHash);
		if(locinfo==null)
			 return null;
		int StartNum=0;
		if(considerDirection)//���������򣬷��صĶ��Ǳ����������
		{
			if(locinfo.getCis5to3(LOCID))
			{
				StartNum = locinfo.getExonlist(LOCID).get(2);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			}
			else 
			{
				ArrayList<Integer> lsExon = locinfo.getExonlist(LOCID);
				StartNum = lsExon.get(lsExon.size()-1);
				return	ChrSearch.getSeq(!direction,locinfo.ChrID, StartNum, StartNum+length);
			}
		}
		else //�����������򣬷��صľ���Ĭ���������
		{
			if(locinfo.getCis5to3(LOCID))
			{
				StartNum=locinfo.getExonlist(LOCID).get(2);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			}
			else 
			{
				ArrayList<Integer> lsExon = locinfo.getExonlist(LOCID);
				StartNum = lsExon.get(lsExon.size()-1);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum, StartNum+length);
			}
		}	
	}
	/**
	 * ����Ⱦɫ����ţ����꣬�������߳��ȣ����ظ������������������
	 * �������ڻ����ڲ�ʱ��������Ŀ�ķ���,����ڻ���䣬�򷵻�����<br>
	 * ��ν�����ڻ����ڲ���ָ��������Ŀ����UpstreamTSSbp������GeneEnd3UTR֮�������
	 * @param ChrID ,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * @param codloc peak����
	 * @param lenght peak�������˳���
	 * @param condition Ϊ 0,1,2 �������<br>
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������<br>
	 * 1: ͨͨ��ȡ����<br>
	 * 2: ͨͨ��ȡ����<br>
	 * @return
	 */
	public String getPeakSeq(String ChrID, int codloc ,int lenght,int condition)
	{
		if (condition==0) 
		{
			GffCodInfo peakInfo = gffSearch.searchLocation(ChrID, codloc, gffHash);
			boolean flaginside=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
			boolean cis5to3=true;
			/**
			 * ���ڻ����ʱ
			 */
			if(!peakInfo.insideLOC)
			{	
				/**
				 *  �����ǰһ�����������Χ(����3k���¶�100bp)��
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[0])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
				{
					flaginside=true;
					cis5to3=peakInfo.begincis5to3;
				}
				/**
				 *  �����һ������ķ���Ϊ����----->��ô����peak�ͺ�һ������ת¼���ľ���
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[1])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[1])<GeneEnd3UTR)
				{ 
					flaginside=true;
					cis5to3=peakInfo.endcis5to3;
				}
			}
			/**
			 * ���ڻ����ڲ�ʱ
			 */
			else 
			{
				cis5to3=peakInfo.begincis5to3;
				flaginside=true;
			}
		   
			if(flaginside)//�����ڻ����ڲ����ڻ������λ�����3��UTR�ڲ�
			{
				return ChrSearch.getSeq(ChrID, codloc, lenght, cis5to3);
			}
			return ChrSearch.getSeq(ChrID, codloc, lenght,true);
		}
		else if (condition==1) 
		{
			return ChrSearch.getSeq(ChrID, codloc, lenght, true);
		}
		else if (condition==2) {
			return ChrSearch.getSeq(ChrID, codloc, lenght, false);
		}
		else {
			System.out.println("error");
			return null;
		}
	}

	
	/**
	 * 	�����������ܳ��ȣ��ں����ܳ��ȵ���Ϣ
	 * ������
	 * Ϊһ��ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength <br>
	 * 3: allIntronLength <br>
	 * 4: allup2kLength
	 * 5: allInterGenic <br>
	 * 6: allGeneLength <br>
	 * @return 
	 */
	public ArrayList<Long> getGeneStructureLength()
	{
		return ((GffHashGene)gffHash).getGeneStructureLength();
	}

	
	/**
	 * ���µ¹��Ż�����������ȡ�����������е������ϣ����Ҿ����е������һ��������ĸ��Ȼ���Ը���ĸΪ���ģ����Ҹ���չָ�������ٴ���ȡ����
	 * ����Ⱦɫ����ţ����꣬�������߳��ȣ����ظ������������������
	 * �������ڻ����ڲ�ʱ��������Ŀ�ķ���,����ڻ���䣬�򷵻�����<br>
	 * ��ν�����ڻ����ڲ���ָ��������Ŀ����UpstreamTSSbp������GeneEnd3UTR֮�������
	 * @param ChrID
	 * @param centerChar,�����е������һ��������ĸ,��Ҫ��д��������Զ�ת��ΪСд
	 * @param codloc peak����
	 * @param lenght peak�������˳���
	 * @param condition Ϊ 0,1,2 �������<br>
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������<br>
	 * 1: ͨͨ��ȡ����<br>
	 * 2: ͨͨ��ȡ����<br>
	 * @return
	 */
	public String getCDGPeakSeq(String ChrID,String centerChar, int codloc ,int lenght,int condition)
	{
		String tmpSeq="";
		boolean flaginside=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
		boolean cis5to3=true;
		
		if (condition==0) 
		{
			GffCodInfo peakInfo = gffSearch.searchLocation(ChrID, codloc, gffHash);
		
			/**
			 * ���ڻ����ʱ
			 */
			if(!peakInfo.insideLOC)
			{	
				/**
				 *  �����ǰһ�����������Χ(����3k���¶�100bp)��
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[0])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
				{
					flaginside=true;
					cis5to3=peakInfo.begincis5to3;
				}
				/**
				 *  �����һ������ķ���Ϊ����----->��ô����peak�ͺ�һ������ת¼���ľ���
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[1])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[1])<GeneEnd3UTR)
				{ 
					flaginside=true;
					cis5to3=peakInfo.endcis5to3;
				}
			}
			/**
			 * ���ڻ����ڲ�ʱ
			 */
			else 
			{
				cis5to3=peakInfo.begincis5to3;
				flaginside=true;
			}
		   
			if(flaginside)//�����ڻ����ڲ����ڻ������λ�����3��UTR�ڲ�
			{
				tmpSeq= ChrSearch.getSeq(ChrID, codloc, lenght, cis5to3);
			}
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght,true);
		}
		else if (condition==1) 
		{
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght, true);
		}
		else if (condition==2) {
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght, false);
		}
		else {
			System.out.println("error");
			return null;
		}
		if (!tmpSeq.contains("c")&&!tmpSeq.contains("C")) {
			return "";
		}
		
		
		int centerTmpSeq=tmpSeq.length()/2;
		int bias=0;
		for (int i = 0; i <=centerTmpSeq; i++)
		{		
			String leftindexchar=tmpSeq.charAt(centerTmpSeq-i)+"";
			String rightindexchar=tmpSeq.charAt(centerTmpSeq+i)+"";
			if (centerChar.equals(leftindexchar)) {
				bias=-i;
				break;
			}
			if (centerChar.toLowerCase().equals(leftindexchar)) {
				bias=-i;
				break;
			}
			if (centerChar.equals(rightindexchar)) {
				bias=i;
				break;
			}
			if (centerChar.toLowerCase().equals(rightindexchar)) {
				bias=i;
				break;
			}
		}
		
		String resultSeq="";
		if (condition==0) 
		{
			if(flaginside&&cis5to3)//�����ڻ����ڲ����ڻ������λ�����3��UTR�ڲ�
			{
				resultSeq= ChrSearch.getSeq(ChrID, codloc+bias, lenght, true);
			}
			else if (flaginside&&!cis5to3) {
				resultSeq=  ChrSearch.getSeq(ChrID, codloc-bias, lenght,false);
			} 
			else if (!flaginside) {
				resultSeq=  ChrSearch.getSeq(ChrID, codloc+bias, lenght,true);
			}
		}
		else if (condition==1) 
		{
			resultSeq=ChrSearch.getSeq(ChrID, codloc+bias, lenght, true);
		}
		else if (condition==2) 
		{
			resultSeq=  ChrSearch.getSeq(ChrID, codloc-bias, lenght, false);
		}
		
		return resultSeq;
		
	}

	
	 /**
	   * �εײ�Ĳ���
	   * @cisseq true������������� false����÷�������
	   */
	public String getSeq(boolean cisseq,String chr, int startnum, int endnum) 
	{   
		return ChrSearch.getSeq(cisseq,chr,startnum,endnum);
	}
	  
	  /**
	   * ����Ⱦɫ����λ�úͷ��򷵻�����
	   * @param chrlocationȾɫ���ŷ����磺Chr:1000-2000
	   * @param cisseq����true:���� false:���򻥲�
	   */
	public String GetSequence(String chrlocation, boolean cisseq)
	{
		return ChrSearch.getSeq(chrlocation, cisseq);
	}
	  
	 
	  
	
	/**
	 * ����chrID�;����������䣬�Լ��ֱ��ʣ�����double[]���飬�������Ǹ�������reads�ķֲ����
	 * �����Ⱦɫ����û��mapping������򷵻�null
	 * @param chrID
	 * @param locStart
	 * @param locEnd
	 * @param binBp ÿ��������������bp��
	 * @return
	 */
	public double[] getRangReadsDist(String chrID,int locStart,int locEnd,int binBp) 
	{
		return mapReads.getRengeInfo(binBp,chrID, locStart, locEnd, 0);
	}
	
	/**
	 * ����chrID�;����������䣬�Լ��ֱ��ʣ�����double[]���飬�������Ǹ�������reads�ķֲ����
	 * �����Ⱦɫ����û��mapping������򷵻�null
	 * @param binNum ���ָ�Ŀ���
	 * @param chrID
	 * @param locStart
	 * @param locEnd
	 * @return
	 */
	public double[] getRangReadsDist(int binNum,String chrID,int locStart,int locEnd) 
	{
		return mapReads.getRengeInfo(chrID, locStart, locEnd, binNum, 0);
	}
	
	
	
	
	/**
	 * ����chrID�;����������䣬�Լ��ֱ��ʣ�����double[]����:��Ⱦɫ����tag���ܶȷֲ����������Ǹ�������reads�ķֲ����
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public double[] getChrReadsDist(String chrID,int startLoc,int endLoc,int binNum) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * �� chrID=""ʱ�������chr�ĳ���
	 * @param chrID
	 * @return
	 */
	public long getChrLength(String chrID) 
	{
		if (!chrID.equals("")) {
			return (long) ChrSearch.getChrLength(chrID);
		}
		else {
			ArrayList<String[]> chrInfo=ChrSearch.getChrLengthInfo();
			long chrAllLength=0;
			for (int i = 0; i < chrInfo.size(); i++) {
				chrAllLength=chrAllLength+Long.parseLong(chrInfo.get(i)[1]);
			}
			return chrAllLength;
		}
		 
		
		
	}

	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * @param chrID
	 * @return
	 * 0: chrID 
	 * 1: chr����
	 */
	public ArrayList<String[]> getChrLengthInfo() 
	{
		return ChrSearch.getChrLengthInfo();
	}
	
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��������chr�ĳ���
	 * @param chrID
	 * @return
	 * 0: ���chr����
	 *  1: �chr����
	 */
	public int[] getThreshodChrLength() 
	{ 
		int[] chrLen=new int[2];
		ArrayList<String[]> chrInfo=ChrSearch.getChrLengthInfo();
		chrLen[0]=Integer.parseInt(chrInfo.get(0)[1]);
		chrLen[1]=Integer.parseInt(chrInfo.get(chrInfo.size()-1)[1]);
		// mapReads.getLimChrLength();
		return chrLen;
	}
	
	
	/**
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ��������Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param chrID 
	 * @param startLoc ������꣬Ϊʵ�����
	 * @param endLoc ���յ�Ϊ-1ʱ����ֱ��Ⱦɫ��Ľ�β��
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	/**
	 * ��ñ�Ⱦɫ���ı�ÿһ����������bp��Ŀ
	 * @return
	 */
	public int getChrLineLength() {
		return ChrSearch.getChrLineLength();
	}
	
	/**
	 * ���ĳ��Ⱦɫ���Ӧ��BufferedReader�࣬�����ͷ��ȡ
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID)
	{
		return ChrSearch.getBufChrSeq(chrID);
	}
	
	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * @param outFile
	 */
	 public void saveChrLengthToFile(String outFile) 
	 {
		 ChrSearch.saveChrLengthToFile(outFile);
	 }
	 
		/**
		 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
		 * ��ôresolution���Ƿ��ص�int[]�ĳ���
		 * @param chrID
		 * @param maxresolution
		 */
	 public int[] getChrRes(String chrID,int maxresolution) throws Exception
		{
			return ChrSearch.getChrRes(chrID, maxresolution);
		}
		
	 
		
	 /**
	  * ����ÿ����peak�����ǵĻ���ľ������ <br>
	  * string�� ������<br>
	  * arraylist��������Ϣ�������ĸ�int[]<br>
	  * 0��Tss int[1]����¼peak��Tss����߻����ұߣ����ĸ�ֵ 0��none 1��left 2��right 3��both<br>
	  * 1��Exon int[n]. ��¼��Щ�����ӱ�peak�����ǣ�0��û�б����� 1�������ǡ�nΪÿ�������ת¼������������Ŀ,��0��exonNum-1��<br>
	  * 2��Intron int[n]. ��¼��Щ�ں��ӱ�peak�����ǣ�0��û�б����� 1�������ǡ�nΪÿ�������ת¼�����ں�����Ŀ����0��intronNum-1<br>
	  * 3��GeneEnd int[1]:��¼peak��GeneEnd����߻����ұߣ����ĸ�ֵ 0��none 1��left 2��right 3��both
	  */
	 Hashtable<String, ArrayList<int[]>> hashGenePeakInfo=new Hashtable<String, ArrayList<int[]>>();
		  
	 /**
	  * ����peak��Ϣ����hashGenePeakInfo������
	  * �ñ������ã� ����ÿ����peak�����ǵĻ���ľ������ <br>
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  */
	 private void setHashGenePeakInfo(String[][] LOCInfo)
	 {
		 for (int i = 0; i < LOCInfo.length; i++) {
			 ArrayList<ArrayList<String[]>> lstmpGeneInfo=null;
			 try {
				 lstmpGeneInfo=getGeneInfo(LOCInfo[i][0], Integer.parseInt(LOCInfo[i][1]), Integer.parseInt(LOCInfo[i][2]));
				 setHashGenePeakInfo(lstmpGeneInfo);
			} catch (Exception e) {
				System.out.println(LOCInfo[i][0]+"   "+LOCInfo[i][1]+"   "+LOCInfo[i][2]);
			}
			
		 }
	 }
	 
	 /**
	  * ����Peak��Ϣ����ͳ�Ƶ�Tss�������˵ľ��룬���и�Ŀ�������󷵻����б�peak���ǵ�Tss�Ļ����Tss����readsͳ�ƽ��
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[] getUCSCTssRange(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getTssRange(range, binNum);
	}
	 
	 /**
	  * ����Peak��Ϣ����ͳ�Ƶ�GeneEnd�������˵ľ��룬���и�Ŀ�������󷵻����б�peak���ǵ�GeneEnd�Ļ����GeneEnd����readsͳ�ƽ��
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[] getUCSCGeneEndRange(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getGeneEndRange(range, binNum);
	}
		 
	 /**
	  * ����Peak��Ϣ����ͳ�Ƶ�Tss�������˵ľ��룬���и�Ŀ�������󷵻����б�peak���ǵ�Tss�Ļ����Tss����readsͳ�ƽ��
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[][] getUCSCTssRangeArray(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getTssRangeArray(range, binNum);
	}
	 
	 /**
	  * ����Peak��Ϣ����ͳ�Ƶ�GeneEnd�������˵ľ��룬���и�Ŀ�������󷵻����б�peak���ǵ�GeneEnd�Ļ����GeneEnd����readsͳ�ƽ��
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[][] getUCSCGeneEndRangeArray(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getGeneEndRangeArray(range, binNum);
	}
	 
	 
	 /**
	  * ����peak�����ǵĻ�����������дhashGenePeakInfo��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  *  <b>ls0: ������ڻ������</b>���������ĳһ��û�У���Ϊ"" <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
	  * &nbsp;&nbsp; 4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
	  *<b> ls1: �յ����ڻ������</b>���������ĳһ��û�У�������յ���ͬһ�������ڣ���Ϊ"" <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
	  * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
	  * <b>ls2-lsend: peak�����м��������Ļ���</b> <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
	  * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
	  * @param range ͳ��ʱtss�������ߵľ���   
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ����
	  */
	 @SuppressWarnings({ "unchecked" })
	 private void setHashGenePeakInfo(ArrayList<ArrayList<String[]>> lsReadsInfo) 
	 {
		 GffHash gffHashgene = gffHash;
		 
		 //getGeneInfo(1, "", 12, 21);
		 for(int i=0;i<lsReadsInfo.size();i++)
		 {
			 ArrayList<String[]> lsTmp=lsReadsInfo.get(i);
			 ArrayList<Integer> lsLongestSplit=null;
			 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////v
			 if(lsTmp.get(0)[0]==null)
			 {
				 System.out.println("error");
			 }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			 if (!lsTmp.get(0)[0].equals("")) 
			 {
				 String tmpGeneID=lsTmp.get(0)[0].split("/")[0];
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashgene.LOCsearch(tmpGeneID);
				 lsLongestSplit=(ArrayList<Integer>)(gffDetailUCSCgene.getLongestSplit().get(1));
				 int exonNum= (lsLongestSplit.size()-2)/2;//�ת¼���������ٸ�������
				 if (hashGenePeakInfo.containsKey(lsTmp.get(0)[0]))
				 {
					 ArrayList<int[]> lsTmpPeakInfo=hashGenePeakInfo.get(lsTmp.get(0)[0]);
					 if (!lsTmp.get(2)[0].equals("")) //�����ArrayList<ArrayList<String[]>>��Exon��Ϊ""
					 {
						 int exonStartNum=Integer.parseInt(lsTmp.get(2)[0]);
						 int exonEndNum=Integer.parseInt(lsTmp.get(2)[1]);
						 for (int j = exonStartNum-1; j <exonEndNum; j++) {
							 lsTmpPeakInfo.get(1)[j]=1;
						 }
					 }
					 if (!lsTmp.get(3)[0].equals("")) //Intron��Ϊ""
					 {
						 int intronStartNum=Integer.parseInt(lsTmp.get(3)[0]);
						 int intronEndNum=Integer.parseInt(lsTmp.get(3)[1]);
						 for (int j = intronStartNum-1; j <intronEndNum; j++) {
							 lsTmpPeakInfo.get(2)[j]=1;
						 }
					 }
					 /////////////////TSS///////////////////////////////////////
					 if (!lsTmp.get(1)[0].equals("")) 
					 {
						 int tmpCod=0;
						 if (lsTmp.get(1)[1].equals("right"))  
							 tmpCod=2;
						 else if (lsTmp.get(1)[1].equals("left")) 
							 tmpCod=1;
						 else if (lsTmp.get(1)[1].equals("both"))  
							 tmpCod=3;
						 
						 if (lsTmpPeakInfo.get(0)[0]==0) 
							 lsTmpPeakInfo.get(0)[0]=tmpCod;
						 else if (lsTmpPeakInfo.get(0)[0]!=tmpCod&&tmpCod!=0)//�µķ�����ϵķ���һ�²����·�����ڣ���Ϊboth
							 lsTmpPeakInfo.get(0)[0]=3;
						 
					 }
					 /////////////GeneEnd/////////////////////////////////////////////////
					 if (!lsTmp.get(4)[0].equals("")) 
					 {
						 int tmpCod=0;
						 if (lsTmp.get(4)[1].equals("right"))  
							 tmpCod=2;
						 else if (lsTmp.get(4)[1].equals("left")) 
							 tmpCod=1;
						 else if (lsTmp.get(4)[1].equals("both"))  
							 tmpCod=3;
						 
						 if (lsTmpPeakInfo.get(3)[0]==0) 
							 lsTmpPeakInfo.get(3)[0]=tmpCod;
						 else if (lsTmpPeakInfo.get(3)[0]!=tmpCod&&tmpCod!=0)//�µķ�����ϵķ���һ�²����·�����ڣ���Ϊboth
							 lsTmpPeakInfo.get(3)[0]=3;
					 }
				 }
				 
						/////////////////////////////////////////////////////////////////////////////////////////////////////�����ǰ�Ѿ��й��û�����Ϣ������ȥ//////////////////////////////////////////////////
				 else
				 {
					 String tmpChrID=lsTmp.get(0)[0];
					 
					 ArrayList<int[]> lsTmpPeakInfo=new ArrayList<int[]>();
					 int[] TSS=new int[1];
					 int[] exonInfo=new int[exonNum];
					 int[] intronInfo=new int[exonNum-1];
					 int[] geneEnd=new int[1];
					 if (!lsTmp.get(2)[0].equals("")) //Exon��Ϊ""
					 {
						 int exonStartNum=Integer.parseInt(lsTmp.get(2)[0]);
						 int exonEndNum=Integer.parseInt(lsTmp.get(2)[1]);
						 for (int j = exonStartNum-1; j <exonEndNum; j++) {
							 exonInfo[j]=1;
						 }
					 }
					 if (!lsTmp.get(3)[0].equals("")) //Intron��Ϊ""
					 {
						 int intronStartNum=Integer.parseInt(lsTmp.get(3)[0]);
						 if (lsTmp.get(3)[1].equals("")) {
							System.out.println(lsTmp.get(0)[0]);
						}
						 int intronEndNum=Integer.parseInt(lsTmp.get(3)[1]);
						 for (int j = intronStartNum-1; j <intronEndNum; j++) {
							 intronInfo[j]=1;
						 }
					 }
							 /////////////////TSS///////////////////////////////////////
					 if (lsTmp.get(1)[0]==null) {
						System.out.println("error");
					}
					 if (!lsTmp.get(1)[0].equals("")) 
					 {
						 if (lsTmp.get(1)[1].equals("right"))  
							 TSS[0]=2;
						 else if (lsTmp.get(1)[1].equals("left")) 
							 TSS[0]=1;
						 else if (lsTmp.get(1)[1].equals("both"))  
							 TSS[0]=3;
					 }
					 /////////////GeneEnd/////////////////////////////////////////////////
					 if (!lsTmp.get(4)[0].equals("")) 
					 {
						 
						 if (lsTmp.get(4)[1].equals("right"))  
							 geneEnd[0]=2;
						 else if (lsTmp.get(4)[1].equals("left")) 
							 geneEnd[0]=1;
						 else if (lsTmp.get(4)[1].equals("both"))  
							 geneEnd[0]=3;
					 }
					 lsTmpPeakInfo.add(TSS);lsTmpPeakInfo.add(exonInfo);lsTmpPeakInfo.add(intronInfo);lsTmpPeakInfo.add(geneEnd);
					 hashGenePeakInfo.put(tmpChrID, lsTmpPeakInfo);
				 }
			 }
		 }
	 }
  
	 /**
	  * ��hashGenePeakInfo�Ѿ����ڵ������
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�Ѿ�������У��
	  * У�������ǣ����reads�ܶ�*fold��/��reads��/�漰����gene��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ����
	  */
	 @SuppressWarnings("unchecked")
	private double[] getTssRange(int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult=new double[binNum];//���binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];
			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 
			 double[] tmpTssBin=null;
			 
			 if (lsGeneInfo.get(0)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);

				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberstart+range;
					 startNum=gffDetailUCSCgene.numberstart-range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
				 {
					 startNum=gffDetailUCSCgene.numberend-range;
					 endNum=gffDetailUCSCgene.numberend+range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //����������Щ�����
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpTssBin);
				 }
				 tmpGeneNum++;
				 lsTss.add(tmpTssBin);
			 }
		 }
		 binResult = MathComput.getMeanByColdou(lsTss);
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 /**
	  * ��hashGenePeakInfo�Ѿ����ڵ������
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�Ѿ�������У��
	  * У�������ǣ����reads�ܶ�*fold��/��reads��/�漰����gene��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads�����������
	  */
	 @SuppressWarnings("unchecked")
	private double[][] getTssRangeArray(int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 GffHash gffHashGene = gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult=new double[binNum];//���binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];
			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 
			 double[] tmpTssBin=null;
			 
			 if (lsGeneInfo.get(0)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);

				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberstart+range;
					 startNum=gffDetailUCSCgene.numberstart-range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
				 {
					 startNum=gffDetailUCSCgene.numberend-range;
					 endNum=gffDetailUCSCgene.numberend+range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //����������Щ�����
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpTssBin);
				 }
				 tmpGeneNum++;
				 lsTss.add(tmpTssBin);
			 }
		 }
		 double[][] result = lsTss.toArray(new double[1][1]);
//		 for (int i = 0; i < result.length; i++) {
//			for (int j = 0; j < result[0].length; j++) {
//				result[i][j] = result[i][j] *fold/readsNum;
//			}
//		 }
		 return result;
	 }
	 
	 int tmpGeneNum = 0;
	 /**
	  * ������getTssRange��getGeneEndRange�����󣬸÷����᷵�ظ������ڽ��м�����gene�ĸ���
	  * @return
	  */
	 public int getRegGenNum() {
		return tmpGeneNum;
	}
	 
	 /**
	  * ָ��geneID��ע����RefSeq�ڵ�geneID��Ҳ����˵����ID������gff�ļ��г��ֹ������û����������ô��������Ȼ����Ϊ�ڴ����ʱ���Ѿ���ͬһ����Ķ��ת¼�������˺ϲ�����������õ����ת¼������Ϣ
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�Ѿ�������У��
	  * У�������ǣ����reads�ܶ�*fold��/��reads��/�漰����gene��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ����
	  */
	 @SuppressWarnings("unchecked")
	public double[] getTssRange(String[] geneID, int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 
		 GffHash gffHashGene=gffHash;
		 //�������geneID���ظ���geneID���кϲ����õ�Ψһ��GeneID
		 double[] binResult=new double[binNum];//���binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 HashSet<String> hashUniGeneID = new HashSet<String>();//����ȥ�ظ���
		 for (int i = 0; i < geneID.length; i++)
		 {
			 //������Կ��ǽ������geneID�������ݿ�ת����refseqID
			 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID[i]);
			 if (gffDetailUCSCgene == null) {
				continue;
			}
			 if (hashUniGeneID.contains(gffDetailUCSCgene.locString)) {
				continue;
			 }
			 hashUniGeneID.add(gffDetailUCSCgene.locString);
			 double[] tmpTssBin=null;
			 int startNum=0; int endNum=0;
			 if (gffDetailUCSCgene.cis5to3) 
			 {
				 startNum=gffDetailUCSCgene.numberstart-range;
				 endNum=gffDetailUCSCgene.numberstart+range;
				 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 if (tmpTssBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
			 }
			 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
			 {
				 startNum=gffDetailUCSCgene.numberend-range;
				 endNum=gffDetailUCSCgene.numberend+range;
				 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 //����������Щ�����
				 if (tmpTssBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
				 MathComput.convertArray(tmpTssBin);
			 }
			 tmpGeneNum++;
			 lsTss.add(tmpTssBin);	
		 }
		 binResult = MathComput.getMeanByColdou(lsTss);
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 /**
	  * ָ��geneID��ע����RefSeq�ڵ�geneID��Ҳ����˵����ID������gff�ļ��г��ֹ������û����������ô��������Ȼ����Ϊ�ڴ����ʱ���Ѿ���ͬһ����Ķ��ת¼�������˺ϲ�����������õ����ת¼������Ϣ
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�Ѿ�������У��
	  * У�������ǣ����reads�ܶ�*fold��/��reads��/�漰����gene��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱgeneEnd�������ߵľ��룬����˵����geneEnd�������߶��ٵľ���
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����geneEnd��reads���ۼ����
	  */
	 public double[] getGeneEndRange(String[] geneID,int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 //�������geneID���ظ���geneID���кϲ����õ�Ψһ��GeneID
		 tmpGeneNum = 0;
		 double[] binResult=null;//���binResult
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 HashSet<String> hashUniGeneID = new HashSet<String>();//����ȥ�ظ���
		 for (int i = 0; i < geneID.length; i++)
		 {
			 //������Կ��ǽ������geneID�������ݿ�ת����refseqID
			 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID[i]);
			 if (gffDetailUCSCgene == null) {
				continue;
			}
			 if (hashUniGeneID.contains(gffDetailUCSCgene.locString)) {
				continue;
			 }
			 double[] tmpGeneEndBin=null;
			 int startNum=0; int endNum=0;
			 if (gffDetailUCSCgene.cis5to3) 
			 {
				 startNum=gffDetailUCSCgene.numberend-range;
				 endNum=gffDetailUCSCgene.numberend+range;
				 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 //����������Щ�����
				 if (tmpGeneEndBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
			 }
			 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
			 {
				 startNum=gffDetailUCSCgene.numberstart-range;
				 endNum=gffDetailUCSCgene.numberstart+range;
				 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 if (tmpGeneEndBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
				 MathComput.convertArray(tmpGeneEndBin);
			 }
			 tmpGeneNum++;
			 lsGeneEnd.add(tmpGeneEndBin);
		 }
		 binResult = MathComput.getMeanByColdou(lsGeneEnd);//��λ��
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 
	 /**
	  * ָ������geneID��ע����RefSeq�ڵ�geneID��Ҳ����˵����ID������gff�ļ��г��ֹ������û����������ô��������Ȼ����Ϊ�ڴ����ʱ���Ѿ���ͬһ����Ķ��ת¼�������˺ϲ�����������õ����ת¼������Ϣ
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�����н���
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ���������û�鵽���ͷ���null
	  */
	 @SuppressWarnings("unchecked")
	public double[] getTssRange(String geneID, int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		//������Կ��ǽ������geneID�������ݿ�ת����refseqID
		 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID);
		 if (gffDetailUCSCgene == null) {
			return null;
		}
		 double[] tmpTssBin=null;
		 int startNum=0; int endNum=0;
		 if (gffDetailUCSCgene.cis5to3) 
		 {
			 startNum=gffDetailUCSCgene.numberstart-range;
			 endNum=gffDetailUCSCgene.numberstart+range;
			 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 
			 if (tmpTssBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				return null;
			}
		 }
		 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
		 {
			 startNum=gffDetailUCSCgene.numberend-range;
			 endNum=gffDetailUCSCgene.numberend+range;
			 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 //����������Щ�����
			 if (tmpTssBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				 return null;
			}
			 MathComput.convertArray(tmpTssBin);
		 }
		 return tmpTssBin;
	 }
	 
	 
	 /**
	  *ָ������geneID��ע����RefSeq�ڵ�geneID��Ҳ����˵����ID������gff�ļ��г��ֹ������û����������ô��������Ȼ����Ϊ�ڴ����ʱ���Ѿ���ͬһ����Ķ��ת¼�������˺ϲ�����������õ����ת¼������Ϣ
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ,�Ѿ�������У��
	  * У�������ǣ����reads�ܶ�*fold��/��reads��/�漰����gene��
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱgeneEnd�������ߵľ��룬����˵����geneEnd�������߶��ٵľ���
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����geneEnd��reads���ۼ���������û�鵽���ͷ���null
	  */
	 public double[] getGeneEndRange(String geneID,int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 //������Կ��ǽ������geneID�������ݿ�ת����refseqID
		 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID);
		 if (gffDetailUCSCgene == null) {
			return null;
		}
		 double[] tmpGeneEndBin=null;
		 int startNum=0; int endNum=0;
		 if (gffDetailUCSCgene.cis5to3) 
		 {
			 startNum=gffDetailUCSCgene.numberend-range;
			 endNum=gffDetailUCSCgene.numberend+range;
			 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 //����������Щ�����
			 if (tmpGeneEndBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				 return null;
			}
		 }
		 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
		 {
			 startNum=gffDetailUCSCgene.numberstart-range;
			 endNum=gffDetailUCSCgene.numberstart+range;
			 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 if (tmpGeneEndBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				return null;
			}
			 MathComput.convertArray(tmpGeneEndBin);
		 }	
		 return tmpGeneEndBin;
	 }
	 
	 
	 
	 /**
	  * ��hashGenePeakInfo�Ѿ����ڵ������
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ����
	  */
	 private double[] getGeneEndRange(int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult = null;//���binResult
		 tmpGeneNum = 0;
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];

			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 double[] tmpGeneEndBin=null;
			 if (lsGeneInfo.get(3)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);
				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberend+range;
					 startNum=gffDetailUCSCgene.numberend-range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //����������Щ�����
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
				 {
					 startNum=gffDetailUCSCgene.numberstart-range;
					 endNum=gffDetailUCSCgene.numberstart+range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpGeneEndBin);
				 }
				 tmpGeneNum++;
				lsGeneEnd.add(tmpGeneEndBin);
				// MathComput.addArray(binResult,tmpGeneEndBin);
				 //tmpGeneNum++;
			 }
		 }
		 binResult = MathComput.getMeanByColdou(lsGeneEnd);//��λ��
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 /**
	  * ��hashGenePeakInfo�Ѿ����ڵ������
	  * ����peak�����ǵĻ���������ͬʱָ���������и���������ظ��������и�����µ�reads�ܶ�ֵ
	  * @param lsReadsInfo getGeneInfo�������صĽ��
	  * @param range ͳ��ʱtss�������ߵľ��룬����˵����tss�������߶��ٵľ��룬��������UpstreamTSSbp��DownstreamTss��һ�£�UpstreamTSSbp��DownstreamTss�������������������������������peak�򽫸�tss����ͳ��
	  * @param gffHashUCSCgene 
	  * @return ���ظ���������peak���ǵ�����tss��reads���ۼ����
	  */
	 private double[][] getGeneEndRangeArray(int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult = null;//���binResult
		 tmpGeneNum = 0;
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];

			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 double[] tmpGeneEndBin=null;
			 if (lsGeneInfo.get(3)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);
				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberend+range;
					 startNum=gffDetailUCSCgene.numberend-range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //����������Щ�����
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//��Ϊ���з���������Ҫ�����Ҳ��һ�� 
				 {
					 startNum=gffDetailUCSCgene.numberstart-range;
					 endNum=gffDetailUCSCgene.numberstart+range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpGeneEndBin);
				 }
				 tmpGeneNum++;
				lsGeneEnd.add(tmpGeneEndBin);
				// MathComput.addArray(binResult,tmpGeneEndBin);
				 //tmpGeneNum++;
			 }
		 }
		 double[][] result = lsGeneEnd.toArray(new double[1][1]);
//		 for (int i = 0; i < result.length; i++) {
//			for (int j = 0; j < result[0].length; j++) {
//				result[i][j] = result[i][j] *fold/readsNum;
//			}
//		 }
		 return result;
	 }
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		  /**
		   * ����peak�������˵㣬�����������˵��ڲ����漰���Ļ��򼰾��������Ŀǰ��֧��UCSCgene��������չΪCG��<br>
		   * ����Ҫ�趨UpStreamTSSbp��DownstreamTss��UpstreamTSSbp��DownstreamTss������������������peak�ڴ˷�Χ���򽫸û������ͳ��<br>
		   * Ȼ��Ҫ�趨GeneEnd3UTR��peak����GeneEnd���ҵ�GeneEnd3UTR��Χ���򽫸û������ͳ��<br>
		   * ע����ν�漰��tss���򣬱�ʾpeak�ܹ����ǵ�TSS�����UpstreamTSSbp����
		   * ���������HashGenepeakInfo��ʱ���ϲ�ͬһ���������Ϣ
		   * @param chrID �ڼ���Ⱦɫ��
		   * @param startLoc peak���
		   * @param endLoc peak�յ�
		   * @return 
		   * ���� ArrayList--ArrayList--String[] <br>
		   *  <b>ls0: ������ڻ������</b>���������ĳһ��û�У���Ϊ"" <br>
	  	   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  	   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
	  	   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
	  	   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
	  	   * &nbsp;&nbsp; 4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
		   *<b> ls1: �յ����ڻ������</b>���������ĳһ��û�У�������յ���ͬһ�������ڣ���Ϊ"" <br>
		   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
		   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
		   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
		   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
		   * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
		   * <b>ls2-lsend: peak�����м��������Ļ���</b> <br>
		   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
		   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":��ʾpeak��ռtss����һ�ߣ���0Ϊ""ʱ��˵���û���tss�����û��ϵ <br>
		   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID ������0�������������ӵ�1���ڽ����������ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��exonID*2��������Ҫ�������ӵ���㡣Ҳ���Ǵ� exonID[0]*2(���)-exonID[1]*2(���)�������� <br>
		   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID ������0���������ں��ӵ�1���ڽ������ں��ӣ�����������UCSCgene���������ֻҪ���UCSCgeneDetail�󣬲���������ֱ��intronID*2+1��������Ҫ���ں��ӵ���㡣Ҳ���Ǵ� intronID[0]*2+1(���)-intronID[1]*2+1(���)���ں��� <br>
		   * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":��ʾpeak��ռGeneEnd����һ�ߣ���0Ϊ""ʱ��˵���û���GeneEnd�����û��ϵ <br>
		   */
		  @SuppressWarnings({ "unchecked" })
		private  ArrayList<ArrayList<String[]>> getGeneInfo(String chrID,int startLoc,int endLoc)
		{
			  ArrayList<Object> gffPairEndDetail = gffSearch.searchLocation(chrID, startLoc, endLoc, gffHash);
			  GffCodInfoUCSCgene startCodInfo = (GffCodInfoUCSCgene) ((Object[])gffPairEndDetail.get(0))[0];
			  GffCodInfoUCSCgene endCodInfo = (GffCodInfoUCSCgene) ((Object[])gffPairEndDetail.get(1))[0];
			  ArrayList<ArrayList<String[]>> lsReadsInfo=new ArrayList<ArrayList<String[]>>();
			  ////////////////////////////// �� ʼ λ �� //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  if (startCodInfo.insideLOC) //�ڻ�����
			  {
				  GffDetail startDetail=startCodInfo.geneDetail[0];//��øû���ľ�����Ϣ
				  ArrayList<String[]> lsStartReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1]; GeneID[0]=startCodInfo.LOCID[0]; 
				  String[] tss=new String[2];//0:TSS 1:������һ�ߺ��У�none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:����һ�ߺ��У�none��left��both
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) ((GffDetailUCSCgene)startDetail).getLongestSplit().get(1);
				  int ExonNum=(lsLongestSplit.size()-2)/2;
				  ////////////// �� �� �� �� //////////////////////////////////////////////////
				  if (lsLongestSplit.size()%2==1) 
					  System.out.println("lsLongestSplit��Ŀ��Ϊż��");
				  /////////////////////////////////////////////////////////////////////////////////
				  String[] exonID=new String[2];  //������split�ڲ���˳������Ҳ����˵�������������ô�ʹӺ���ǰ���ڼ��������ӻ��ں��ӣ������Ӻ��ڻ�ȽϺô���
				  String[] intronID=new String[2];//0:Exon/Intron����� 1:Exon/Intron���յ�
				  if (startCodInfo.begincis5to3) //��������
				  {
					  //////TSS�趨//////////
					  if(startCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
					  {
						  tss[0]="TSS";
						  tss[1]="right";
					  }
					  else {
						  tss[0]="";
						  tss[1]="";
					  }
					  //////////// Exon Intron �� �� ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak �� һ �� �� �� �� ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) 
					  {
						  ///////GeneEnd�趨///////////////////
						  if (Math.abs(endCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
						  {
							  geneEnd[0]="GeneEnd";
							  geneEnd[1]="left";
						  }
						  else {
							  geneEnd[0]="";
							  geneEnd[1]="";
						  }
						  ///////////////////////////////////////////////
						  int startExInType=startCodInfo.GeneInfo.get(0)[0];//��������������� exon/intron
						  int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  int endExInType=endCodInfo.GeneInfo.get(0)[0];//�յ������������� exon/intron
						  int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  ///////////peak ��һ��Exon/Intron��/////////////////////////////
						  if (startExInType==endExInType && startExInNum==endExInNum) 
						  {
							  if (startExInType==1) {
								  exonID[0]=startExInNum+"";exonID[1]=startExInNum+"";
								  intronID[0]="";intronID[1]="";
							  }
							  else {
								  intronID[0]=startExInNum+"";intronID[1]=startExInNum+"";
								  exonID[0]="";exonID[1]="";
							  }
						  }
						  ///////////peak ����һ��Exon/Intron��////////////////////////////
						  else {
							  ///////�������������
							  if (startExInType==1) 
							  {
								  exonID[0]=startExInNum+"";
								  if (startExInNum!=ExonNum) 
									  intronID[0]=startExInNum+"";
								  else 
									  intronID[0]="";
							  }
							  else {//��������ں���
								  intronID[0]=startExInNum+"";
								  exonID[0]=startExInNum+1+"";
							  }
							  ///////�յ�����������
							  if (endExInType==1) 
							  {
								  exonID[1]=endExInNum+"";
								  intronID[1]=endExInNum-1+"";
							  }
							  else 
							  {//�յ������ں���
								  intronID[1]=endExInNum+"";
								  exonID[1]=endExInNum+"";
							  }
						  }
						  ///////////////���Ƿ������/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					  }
					  /////////// peak �� �� һ �� �� �� �� ///////////////////////////////////////////////////////////////
					 else 
					 {
						 geneEnd[0]="GeneEnd";
						 geneEnd[1]="both";					 
						 int startExInType=startCodInfo.GeneInfo.get(0)[0];//��������������� exon/intron
						 int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  ///////�������������
						  if (startExInType==1) {
							  exonID[0]=startExInNum+"";
							  if (startExInNum!=ExonNum) 
								  intronID[0]=startExInNum+"";
							  else 
								  intronID[0]="";
						  }
						  else {//��������ں���
							  intronID[0]=startExInNum+"";
							  exonID[0]=startExInNum+1+"";
						  }
						  exonID[1]=ExonNum+"";
						  if(intronID[0].equals(""))
							  intronID[1]=""; 
						  else
							  intronID[1]=ExonNum-1+"";
						  ///////////////���Ƿ������/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					 }
				  }
				  else //������ 
				  {
					  ///////GeneEnd�趨///////////////////
					  if (Math.abs(startCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
					  {
						  geneEnd[0]="GeneEnd";
						  geneEnd[1]="left";
					  }
					  else {
						  geneEnd[0]="";
						  geneEnd[1]="";
					  }
					  //////////////////////////////////////////// 
					  //////////// Exon Intron �� �� ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak �� һ �� �� �� �� ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) 
					  {
						  if (endCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
						  {
							  tss[0]="TSS";
							  tss[1]="right";
						  }
						  else {
							  tss[0]="";
							  tss[1]="";
						  }
						  int startExInType=startCodInfo.GeneInfo.get(0)[0];//��������������� exon/intron
						  int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  int endExInType=endCodInfo.GeneInfo.get(0)[0];//�յ������������� exon/intron
						  int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  ///////////peak ��һ��Exon/Intron��/////////////////////////////
						  if (startExInType==endExInType&&startExInNum==endExInNum) 
						  {
							  if (startExInType==1) {
								  exonID[0]=ExonNum+1-startExInNum+"";exonID[1]=ExonNum+1-startExInNum+"";
								  intronID[0]="";intronID[1]="";
							  }
							  else {
								  intronID[0]=ExonNum-startExInNum+"";intronID[1]=ExonNum-startExInNum+"";
								  exonID[0]="";exonID[1]="";
							  }
						  }
						  ///////////peak ����һ��Exon/Intron��////////////////////////////
						  else {
							  ///////�������������
							  if (startExInType==1)
							  {
								  exonID[0]=ExonNum+1-startExInNum+"";
								  if (startExInNum!=1) 
									  intronID[0]=ExonNum+1-startExInNum+"";
								  else 
									  intronID[0]="";
							  }
							  else {//��������ں���
								  intronID[0]=ExonNum-startExInNum+"";
								  exonID[0]=ExonNum-startExInNum+1+"";
							  }
							  ///////�յ�����������
							  if (endExInType==1) {
								  exonID[1]=ExonNum+1-endExInNum+"";
								  intronID[1]=ExonNum-endExInNum+"";
							  }
							  else {//�յ������ں���
								  intronID[1]=ExonNum-endExInNum+"";
								  exonID[1]=ExonNum-endExInNum+"";
							  }
						  }
						  ///////////////���Ƿ������/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					  }
					  /////////// peak �� �� һ �� �� �� �� ///////////////////////////////////////////////////////////////
					 else 
					 {
						 tss[0]="TSS";
						 tss[1]="both";
						 int startExInType=startCodInfo.GeneInfo.get(0)[0];//��������������� exon/intron
						 int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  ///////�������������
						 if (startExInType==1) {
							  exonID[0]=ExonNum+1-startExInNum+"";
							  if (startExInNum!=1) 
								  intronID[0]=ExonNum+1-startExInNum+"";
							  else 
								  intronID[0]="";
						 }
						 ////////////////////////////////////////////////////
						 else {//��������ں���
							 intronID[0]=ExonNum-startExInNum+"";
							 exonID[0]=ExonNum-startExInNum+1+"";
						 }
						 exonID[1]=ExonNum+"";
						 if (intronID[0].equals("")) 
							 intronID[1]="";
						 else
							 intronID[1]=ExonNum-1+"";
						 ///////////////���Ƿ������/////////////////////////////////  
						 lsStartReadsInfo.add(GeneID);
						 lsStartReadsInfo.add(tss);
						 lsStartReadsInfo.add(exonID);
						 lsStartReadsInfo.add(intronID);
						 lsStartReadsInfo.add(geneEnd);
						 /////////////////////////////////////////////////////////////////////
					 }
				  }
				  lsReadsInfo.add(lsStartReadsInfo);
			  }
			  //��㲻�ڻ����ڣ���ôֻ���Ǳ��������һ������Ĺ�ϵ
			  else {
				  ArrayList<String[]> lsStartReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:������һ�ߺ��У�none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:����һ�ߺ��У�none��left��both
				  String[] exonID=new String[2];  //������split�ڲ���˳������Ҳ����˵�������������ô�ʹӺ���ǰ���ڼ��������ӻ��ں��ӣ������Ӻ��ڻ�ȽϺô���
				  String[] intronID=new String[2];//0:Exon/Intron����� 1:Exon/Intron���յ�
				  GeneID[0]="";
				  tss[0]="";tss[1]="";
				  geneEnd[0]="";geneEnd[1]="";
				  exonID[0]="";exonID[1]="";
				  intronID[0]="";intronID[1]="";			  
				  /////////�ϸ����������Ҿ����ϸ������end�ܽ�
				  if (startCodInfo.LOCID[1]!=null&&startCodInfo.begincis5to3 && Math.abs(startCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR) {
					  GeneID[0]=startCodInfo.LOCID[1];
					  geneEnd[0]="GeneEnd";geneEnd[1]="right";
				  }
				  else if (startCodInfo.LOCID[1]!=null&&!startCodInfo.begincis5to3 && Math.abs(startCodInfo.distancetoLOCStart[0])<UpStreamTSSbp) {
					  GeneID[0]=startCodInfo.LOCID[1];
					  tss[0]="TSS";tss[1]="left";	
				  }
				  ///////////////���Ƿ������/////////////////////////////////  
				  lsStartReadsInfo.add(GeneID);
				  lsStartReadsInfo.add(tss);
				  lsStartReadsInfo.add(exonID);
				  lsStartReadsInfo.add(intronID);
				  lsStartReadsInfo.add(geneEnd);
				  /////////////////////////////////////////////////////////////////////
				  lsReadsInfo.add(lsStartReadsInfo);
			  }
			  ////////////////////////////// �� �� λ �� //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  if (endCodInfo.insideLOC) //�ڻ�����
			  {
				  GffDetail endDetail=endCodInfo.geneDetail[0];//��øû���ľ�����Ϣ
				  ArrayList<String[]> lsEndReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1]; GeneID[0]=endCodInfo.LOCID[0]; 
			
				  String[] tss=new String[2];//0:TSS 1:������һ�ߺ��У�none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:����һ�ߺ��У�none��left��both
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) ((GffDetailUCSCgene)endDetail).getLongestSplit().get(1);
				  int ExonNum=(lsLongestSplit.size()-2)/2;
				  ////////////// �� �� �� �� //////////////////////////////////////////////////
				  if (lsLongestSplit.size()%2==1) 
					  System.out.println("lsLongestSplit��Ŀ��Ϊż��");
				  /////////////////////////////////////////////////////////////////////////////////
				  String[] exonID=new String[2];  //������split�ڲ���˳������Ҳ����˵�������������ô�ʹӺ���ǰ���ڼ��������ӻ��ں��ӣ������Ӻ��ڻ�ȽϺô���
				  String[] intronID=new String[2];//0:Exon/Intron����� 1:Exon/Intron���յ�
				  if (endCodInfo.begincis5to3) //��������
				  {
					  //////////// Exon Intron �� �� ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak �� �� һ �� �� �� �ڡ�peak��һ�������ڵ�����Ѿ��������//////////////////////////////////////////////////////////////
					  if (startCodInfo.insideLOC && startCodInfo.LOCID[0].equals(endCodInfo.LOCID[0])) 
					  {
						  GeneID[0]="";
						  tss[0]="";tss[1]="";
						  geneEnd[0]="";geneEnd[1]="";
						  exonID[0]="";exonID[1]="";
						  intronID[0]="";intronID[1]="";	
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  lsReadsInfo.add(lsEndReadsInfo);
					  }
					  else
					  {
						  tss[0]="TSS";
						  tss[1]="both";
						  //////���������յ���趨//////////
						  if(Math.abs(endCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
						  {
							  geneEnd[0]="GeneEnd";
							  geneEnd[1]="left";
						  }
						  else {
							  geneEnd[0]="";
							  geneEnd[1]="";
						  }
						 int endExInType=endCodInfo.GeneInfo.get(0)[0];//�յ������������� exon/intron
						 int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  /////// �յ�����������
						  if (endExInType==1) {
							  exonID[1]=endExInNum+"";
							  if (endExInNum==1) 
								  intronID[1]="";
							  else
								  intronID[1]=endExInNum-1+"";
						  }
						  else {// �յ������ں���
							  intronID[1]=endExInNum+"";
							  exonID[1]=endExInNum+"";
						  }
						  exonID[0]=1+"";
						  if (intronID[1].equals("")) 
							  intronID[0]="";
						  else
							  intronID[0]=1+"";
						  ///////////////���Ƿ������/////////////////////////////////  
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
						  lsReadsInfo.add(lsEndReadsInfo);
					 }
				  }
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				  else //������ 
				  {
					  geneEnd[0]="GeneEnd";
					  geneEnd[1]="both";
				  
					  //////////////////////////////////////////// 
					  //////////// Exon Intron �� �� ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak �� �� һ �� �� �� �� ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) {
						  GeneID[0]="";
						  tss[0]="";tss[1]="";
						  geneEnd[0]="";geneEnd[1]="";
						  exonID[0]="";exonID[1]="";
						  intronID[0]="";intronID[1]="";	
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  lsReadsInfo.add(lsEndReadsInfo);
					  }
					  else
					  {
						  //////���������յ���趨//////////
						  if(endCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
						  {
							  tss[0]="TSS";
							  tss[1]="right";
						  }
						  else {
							  tss[0]="";
							  tss[1]="";
						  }
						 int endExInType=endCodInfo.GeneInfo.get(0)[0];//�յ������������� exon/intron
						 int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron��λ��
						  ///////�յ�����������
						 if (endExInType==1) {
							  exonID[1]=ExonNum+1-endExInNum+"";
							  if (endExInNum==ExonNum) 
								  intronID[1]="";
							  else
								  intronID[1]=ExonNum-endExInNum+"";
						 }
						 ////////////////////////////////////////////////////
						 else {//�յ������ں���
							 intronID[1]=ExonNum-endExInNum+"";
							 exonID[1]=ExonNum-endExInNum+"";
						 }
						 exonID[0]=1+"";
						 if (intronID[1].equals("")) 
							 intronID[0]="";
						 else
							 intronID[0]=1+"";
						 ///////////////���Ƿ������/////////////////////////////////  
						 lsEndReadsInfo.add(GeneID);
						 lsEndReadsInfo.add(tss);
						 lsEndReadsInfo.add(exonID);
						 lsEndReadsInfo.add(intronID);
						 lsEndReadsInfo.add(geneEnd);
						 /////////////////////////////////////////////////////////////////////
						 lsReadsInfo.add(lsEndReadsInfo);
					 }
				  }
			  }
			  //�յ㲻�ڻ����ڣ���ôֻ���Ǳ��յ�����һ������Ĺ�ϵ
			  else {
				  ArrayList<String[]> lsEndReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:������һ�ߺ��У�none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:����һ�ߺ��У�none��left��both
				  String[] exonID=new String[2];  //������split�ڲ���˳������Ҳ����˵�������������ô�ʹӺ���ǰ���ڼ��������ӻ��ں��ӣ������Ӻ��ڻ�ȽϺô���
				  String[] intronID=new String[2];//0:Exon/Intron����� 1:Exon/Intron���յ�
				  GeneID[0]="";
				  tss[0]="";tss[1]="";
				  geneEnd[0]="";geneEnd[1]="";
				  exonID[0]="";exonID[1]="";
				  intronID[0]="";intronID[1]="";			  
				  ////////////////////////////////////////////////////////�¸����������Ҿ����¸������TSS�ܽ�/////////////////////////////////////////////////////////////////////////
				  if (endCodInfo.LOCID[1]!=null&&endCodInfo.begincis5to3 && Math.abs(endCodInfo.distancetoLOCStart[0])<UpStreamTSSbp) {
					  GeneID[0]=endCodInfo.LOCID[1];
					  tss[0]="TSS";tss[1]="left";	
				  }
				  else if (endCodInfo.LOCID[1]!=null&&!endCodInfo.begincis5to3 && Math.abs(endCodInfo.distancetoLOCStart[0])<GeneEnd3UTR) {
					  GeneID[0]=endCodInfo.LOCID[1];
					  geneEnd[0]="GeneEnd";geneEnd[1]="right";
				  }
				  ////////////////////////////////////////////////////////////���Ƿ������///////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
				  lsEndReadsInfo.add(GeneID);
				  lsEndReadsInfo.add(tss);
				  lsEndReadsInfo.add(exonID);
				  lsEndReadsInfo.add(intronID);
				  lsEndReadsInfo.add(geneEnd);
				  /////////////////////////////////////////////////////////////////////	 
				  lsReadsInfo.add(lsEndReadsInfo);
			  }
			  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  int medGene=gffPairEndDetail.size();
			  for (int i = 2; i < medGene; i++)
			  {
				  GffDetailUCSCgene medDetailUCSCgene=(GffDetailUCSCgene) gffPairEndDetail.get(i);
				  ArrayList<String[]> lsMedReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:������һ�ߺ��У�none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:����һ�ߺ��У�none��left��both
				  String[] exonID=new String[2];  //������split�ڲ���˳������Ҳ����˵�������������ô�ʹӺ���ǰ���ڼ��������ӻ��ں��ӣ������Ӻ��ڻ�ȽϺô���
				  String[] intronID=new String[2];//0:Exon/Intron����� 1:Exon/Intron���յ�
				  GeneID[0]=medDetailUCSCgene.locString;
				  tss[0]="TSS";tss[1]="both";
				  geneEnd[0]="GeneEnd";geneEnd[1]="both";
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) medDetailUCSCgene.getLongestSplit().get(1);
				  exonID[0]="1";exonID[1]=(lsLongestSplit.size()-2)/2+"";
				  intronID[0]="1";intronID[1]=(lsLongestSplit.size()-2)/2-1+"";	
				  lsMedReadsInfo.add(GeneID);
				  lsMedReadsInfo.add(tss);
				  lsMedReadsInfo.add(exonID);
				  lsMedReadsInfo.add(intronID);
				  lsMedReadsInfo.add(geneEnd);
				  lsReadsInfo.add(lsMedReadsInfo);
			  }
			  return lsReadsInfo;
		}
		
		

	
}
