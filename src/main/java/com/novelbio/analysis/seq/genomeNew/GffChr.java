package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.Species;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGenePlant;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneUCSC;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.java.HeatChart;






 
/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * @author zong0jie
 *
 */
public abstract class GffChr {
	

private static Logger logger = Logger.getLogger(GffChr.class);
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
	 * ��ȡrefseq����
	 */
	protected SeqFastaHash seqFastaHash = null;
	/**
	 * mapping����reads������
	 */
	protected long readsNum = 0;
	/**
	 * Gff�ļ�
	 */
	String GffFile = "";
	
	
	/**
	 * ָ����Ӧ�Ĵ�ʵ����Gffhash����
	 * ��ȡ��Ӧ��gff�ļ�
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","TAIR","CG","UCSC","Repeat"�⼸��<br>
	 * �Ժ��������ӵ�gffhash��������Ҫ�������<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	public GffChr(String gffClass,String GffFile, String ChrFilePath, int taxID)
	{
		if (FileOperate.isFile(GffFile)) {
			 try {
					loadGff(gffClass, GffFile, taxID);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (FileOperate.isFile(ChrFilePath)) {
			loadSeq(ChrFilePath);
		}
		if (FileOperate.isFileDirectory(ChrFilePath)) {
			loadChr(ChrFilePath);
		}
	}

	///////////////////////////////////////////׼������ Loading ��   ��////////////////////////////////////////////////////////////
	/**
	 * ָ����Ӧ�Ĵ�ʵ����Gffhash����
	 * ��ȡ��Ӧ��gff�ļ�
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","TAIR","CG","UCSC","Repeat"�⼸��<br>
	 * �Ժ��������ӵ�gffhash��������Ҫ�������<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	 private void loadGff(String gffClass,String Gfffilename, int taxID) throws Exception
	 {
		 if (gffClass.equals("TIGR")) {
			 gffHash=new GffHashGenePlant(Species.RICE);
		 }
		 if (gffClass.equals("TAIR")) {
			 gffHash=new GffHashGenePlant(Species.ARABIDOPSIS);
		 }
		 else if (gffClass.equals("CG")) {
			 gffHash=new GffHashCG();
		 }
		 else if (gffClass.equals("UCSC")) {
			 gffHash=new GffHashGeneUCSC(taxID);
		 }
		 else if (gffClass.equals("Repeat")) {
			 gffHash=new GffHashRepeat();
		 }
		gffHash.ReadGffarray(Gfffilename);
	 }
	
	/**
	 * �趨����������Ϣ���ļ��У��ļ�����Ӧ��ÿ��Ⱦɫ��һ��fasta�ļ������С��������һ����ϣ��-������Ϣ<br>
	 * <b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * ������Ϣ��ChrStringHash��
	 * @param Chrfilename
	 */
	private void loadChr(String ChrFilePath) 
	{
	   try { chrStringHash = new ChrStringHash(ChrFilePath); } catch (Exception e) {e.printStackTrace();} 
	}
	/**
	 * �趨����������Ϣ���ļ��У��ļ�����Ӧ��ÿ��Ⱦɫ��һ��fasta�ļ������С��������һ����ϣ��-������Ϣ<br>
	 * <b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * ������Ϣ��ChrStringHash��
	 * @param Chrfilename
	 */
	private void loadSeq(String SeqPath) 
	{
		seqFastaHash = new SeqFastaHash(SeqPath);
	}

	
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void drawHeatMap(String resultFilePath, String prefix, int AtgUp, int AtgDown,int normalizedType) throws Exception {
		resultFilePath = FileOperate.addSep(resultFilePath);
		ArrayList<String> lsgenID = gffHash.getLOCChrHashIDList();
		ArrayList<String> lsgeneIDresult = new ArrayList<String>();
		for (String string : lsgenID) {
			lsgeneIDresult.add(string.split("/")[0]);
		}
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgeneIDresult, AtgUp,  AtgDown, -1, normalizedType);
		if (AtgUp <= 0) {
			AtgUp = atgAlign;
		}
		
		
		String[][] GeneEndDensity = new String[lsResult.size()][lsResult.get(0).atg.length+1];
		for (int i = 0; i < GeneEndDensity.length; i++) {
			for (int j = 1; j < GeneEndDensity[0].length; j++) {
				GeneEndDensity[i][j] = lsResult.get(i).atg[j-1] + "";
			}
		}
		for (int i = 0; i < GeneEndDensity.length; i++) {
			GeneEndDensity[i][0] = lsResult.get(i).seqName;
		}
		
		double[][] GeneEndDensity2 = new double[lsResult.size()][lsResult.get(0).atg.length];
		for (int i = 0; i < GeneEndDensity2.length; i++) {
			for (int j = 0; j < GeneEndDensity2[0].length; j++) {
				GeneEndDensity2[i][j] = lsResult.get(i).atg[j];
			}
		}
		System.out.println("���з����Ļ�����Ŀ��" + GeneEndDensity.length);
		HeatChart map = new HeatChart(GeneEndDensity2,0,40);
		map.setTitle("ATGsit: "+ AtgUp+1 );
		map.setXAxisLabel("X Axis");
		map.setYAxisLabel("Y Axis");
//		int[] aa = new String[]{"a","b","c","d","e","f"};
		map.setXValues(-20, 1);
		String[] yvalue = new String[GeneEndDensity2.length];
		for (int i = 0; i < yvalue.length; i++) {
			yvalue[i] = "";
		}
		map.setYValues(yvalue);
		Dimension bb = new Dimension();
		bb.setSize(12, 0.05);
		map.setCellSize(bb );
		//Output the chart to a file.
		Color colorHigh = Color.BLUE;
		Color colorDown = Color.WHITE;
		//map.setBackgroundColour(color);
		map.setHighValueColour(colorHigh);
		map.setLowValueColour(colorDown);
		try {
			map.saveToFile(new File(resultFilePath+prefix+"Atg.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ͼƬ�ĸ߶�����Ϊ�� "+map.getChartSize().getHeight());
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(resultFilePath+prefix+"Atgmatrix.txt", true, false);
		txtReadandWrite.ExcelWrite(GeneEndDensity, "\t");
	}
	
	int atgAlign = 0;
	int maxUTR5 = 0;
	/**
	 * @param lsGeneID
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0
	 */
	private ArrayList<SeqInfo> getATGDensity(ArrayList<String> lsGeneID,int AtgUp, int AtgDown, int filled, int normlizType) {
		GffHashGeneAbs gffHashGene = (GffHashGeneAbs)gffHash;
		ArrayList<SeqInfo> lsAtg = new ArrayList<SeqInfo>();
		for (String string : lsGeneID) {
			SeqInfo seqInfo = new SeqInfo();
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(string);
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplit();
			if (gffGeneIsoSearch.ismRNA()) {
				seqInfo.atg = getReadsInfo(string,gffGeneIsoSearch,normlizType);
				if (seqInfo.atg == null) {
					logger.error("������û����Ӧ����Ϣ��"+gffGeneIsoSearch.getThisGffDetailGene().getChrID()+" "+ 
							gffGeneIsoSearch.getTSSsite() +"  " +gffGeneIsoSearch.getTESsite() +"  "+gffGeneIsoSearch.getIsoName());
					continue;
				}
				seqInfo.seqName = string;
				lsAtg.add(seqInfo);
			}
		}
		return setMatrix(lsAtg, AtgUp, AtgDown, filled);
	}
	/**
	 * ������Ժ������ķ���������5UTR�ĳ��Ƚ������򣬴�С�������У�Ȼ��
	 * @param lsAtg key 5UTR�ĳ��ȣ�value���ܹ����еĳ��ȣ���һλΪatg����λ��
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0
	 */
	private ArrayList<SeqInfo> setMatrix(ArrayList<SeqInfo> lsAtg, int AtgUp, int AtgDown, int filled) {
		int maxGeneBody = 0;
		atgAlign = getAtgAlign(lsAtg);//Ҫ��atg��alignment�ģ��ڲ�������������
		for (SeqInfo ds : lsAtg) {
			if (ds.atg.length-1 - ds.atg[0] > maxGeneBody) {
				maxGeneBody = (int) (ds.atg.length-1 - ds.atg[0]);
			}
		}
		ArrayList<SeqInfo> lsdouble = new ArrayList<SeqInfo>();
		for (SeqInfo ds : lsAtg) {
			SeqInfo tmpResult = setDouble(ds, atgAlign, maxGeneBody, AtgUp, AtgDown, filled);
			lsdouble.add(tmpResult);
		}
		return lsdouble;
	}
	/**
	 * ������atgλ���ֵ
	 * @param lsAtg value���ܹ����еĳ��ȣ���һλΪatg����λ��
	 * @return
	 */
	public int getAtgAlign( ArrayList<SeqInfo> lsAtg) {
		//�Ӵ�С����
		Collections.sort(lsAtg, new Comparator<SeqInfo>() {
			@Override
			public int compare(SeqInfo o1, SeqInfo o2) {
				if (o1.atg[0] < o2.atg[0]) {
					return 1;
				}
				else if (o1.atg[0] == o2.atg[0]) {
					return 0;
				}
				else {
					return -1;
				}
			}
		});
		return (int) lsAtg.get(0).atg[0];
	}
	
	
	/**
	 * �����������������
	 * @param input �������飬��һλΪatg����λ��,Ҳ������Ҫ�����λ��
	 * @param alignATGSite �ATG��λ���ǰһλ����Ҫ����λ��ǰ��ĳ���--����Ǹ��ж೤
	 * @param ATGbody Atg�����ܹ��೤��������Atgλ��,��Ҫ����λ��������ж೤
	 * @param atgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bo������������λ��
	 * @param alignDown ѡȡATG���ζ���bp,������ATGλ�㡣 -1Ϊȫѡ ѡȡ����λ������ζ���bp������������λ��
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0 ��λ��ʲô���
	 * @return
	 */
	public static SeqInfo setDouble(SeqInfo input, int alignATGSite, int ATGbody ,int atgUp, int alignDown,int filled ) {
		int atgOld = (int)input.atg[0];
		int bias = alignATGSite - atgOld;
		double[] tmpresult = null;
		if (alignDown > 0) {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+alignDown+1];
			else 
				tmpresult = new double[alignATGSite+alignDown+1];
		}
		else {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+ATGbody+1];
			else 
				tmpresult = new double[alignATGSite+ATGbody];			
		}
		//��-1��������
		for (int i = 0; i < tmpresult.length; i++) {
			tmpresult[i] = filled;
		}
		//��ʽ����	
		if (atgUp < 0) {
			for (int i = 0; i < input.atg.length-1; i++) {
				if (i+bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = input.atg[i+1];
			}
		}
		else {
			if (atgOld > atgUp) {
				int k = 0;
				for (int i = atgOld - atgUp; i < input.atg.length-1; i++) {
					 if (k >= tmpresult.length) {
							break;
					 }
					 tmpresult[k] = input.atg[i+1];
					 k++;
				}
			}
			else {
				int k = 1;
				for (int i = atgUp - atgOld; i < tmpresult.length; i++) {
					if (k >= input.atg.length) {
						break;
					}
					tmpresult[i] = input.atg[k];
					k++;
				}
			}
		}
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.atg = tmpresult;
		seqInfo.seqName = input.seqName;
		return seqInfo;
	}
	/**
	 *	����ת¼�������ظ�ת¼����mRNAˮƽ����
	 * @param chrID
	 * @param gffGeneIsoSearch
	 * @return
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
	 */
	private double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoSearch, int normalizeType) {
		int geneLength = 0;
		try {
			geneLength = seqFastaHash.getHashChrLength().get(geneID.toLowerCase()).intValue();
		} catch (Exception e) {
			return null;
		}
		
		double[] iso = mapReads.getRengeInfo(1, geneID.toLowerCase(), 1, geneLength, 0);
		if (iso == null) {
			return null;
		}
		mapReads.normDouble(iso, normalizeType);
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoSearch.getLocDistance(gffGeneIsoSearch.getATGSsite(), gffGeneIsoSearch.getTSSsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}

 
	
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
class SeqInfo
{
	/**
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
	 */
	public double[] atg;
	public String seqName = "";
}
