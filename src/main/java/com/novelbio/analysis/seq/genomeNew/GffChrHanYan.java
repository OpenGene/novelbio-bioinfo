package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.tc33.jheatchart.HeatChart;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * 
 * @author zong0jie
 * 
 */
public abstract class GffChrHanYan extends GffChr{
	


private static Logger logger = Logger.getLogger(GffChrHanYan.class);

	public GffChrHanYan(String gffClass, String GffFile,
				String ChrFilePath, int taxID) {
			super(gffClass, GffFile, ChrFilePath, taxID);
			// TODO Auto-generated constructor stub
	}
	/**
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param startRegion bed�ļ��ĵ�һ��ֵ�Ƿ�Ϊ������
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * @param invNum ÿ������λ����
	 * @param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
	 * @param startCod uniqReads ͬһλ����ظ��Ƿ������һ��
	 * @param ����㿪ʼ��ȡ����bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique UniqueMapping�ı������һ��
	 * @param cis5To3 �Ƿ���ѡĳһ�������reads
	 */
	public abstract void loadMap(String mapFile,int startRegion,String chrFilePath,int invNum,int tagLength, boolean uniqReads,
			int startCod, int colUnique,Boolean cis5To3, boolean uniqMapping);
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
		HeatChart map = new HeatChart(GeneEndDensity2,0,200);
		map.setTitle("ATGsit: "+ (AtgUp/3 +1) );
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
			GffGeneIsoSearch gffGeneIsoSearch = gffDetailGene.getCoordSearchLongest();
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
	 * @param AtgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bp������������λ��
	 * @param AtgDown ѡȡATG���ζ���bp,������ATGλ�㡣 -1Ϊȫѡ ѡȡ����λ������ζ���bp������������λ��
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0
	 */
	protected ArrayList<SeqInfo> setMatrix(ArrayList<SeqInfo> lsAtg, int AtgUp, int AtgDown, int filled) {
		int maxGeneBody = 0;
		//������UTR����
		atgAlign = getAtgAlign(lsAtg);//Ҫ��atg��alignment�ģ��ڲ�������������
		//������ATG���γ���,������ATGλ��
		for (SeqInfo ds : lsAtg) {
			if (ds.atg.length-1 - ds.atg[0] > maxGeneBody) {
				maxGeneBody = (int) (ds.atg.length-1 - ds.atg[0]);
			}
		}
		ArrayList<SeqInfo> lsdouble = new ArrayList<SeqInfo>();
		for (SeqInfo ds : lsAtg) {
			//��ʱ��SeqInfo��һλ����ʵ�ʵĵ�һλ������atgsite��
			SeqInfo tmpResult = setDouble(ds, atgAlign, maxGeneBody, AtgUp, AtgDown, filled);
			lsdouble.add(tmpResult);
		}
		//////////////////////
		combineLoc(lsdouble, AtgUp,atgAlign);
		
		//////////////////////
		return lsdouble;
	}
	/**
	 * ����������ϲ�Ϊ1��coding
	 * @param AtgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bo������������λ��
	 * @param AlignATGSite �ATG��λ��ľ���λ�ã���Ҫ����λ��ǰ��ĳ���
	 */
	private  void combineLoc(ArrayList<SeqInfo> lsdouble, int AtgUp, int AlignATGSite)
	{
		//��ʱ��SeqInfo��һλ����ʵ�ʵĵ�һλ������atgsite��
		
		ArrayList<SeqInfo> lsResult = new ArrayList<SeqInfo>();
		
		for (SeqInfo seqInfo : lsdouble) {
			
			if (AtgUp > 0) {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, AtgUp%3 + 1, 3);
			}
			else {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, (AlignATGSite-1)%3, 3);
			}
		}
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
	 * @param alignATGSite �ATG��λ��ľ���λ�ã���Ҫ����λ��ǰ��ĳ���
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
				tmpresult = new double[alignATGSite+alignDown];
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
				for (int i = atgOld - atgUp - 1; i < input.atg.length-1; i++) {
					 if (k >= tmpresult.length) {
							break;
					 }
					 tmpresult[k] = input.atg[i+1];
					 k++;
				}
			}
			else {
				int k = 1;
				for (int i = atgUp - atgOld + 1; i < tmpresult.length; i++) {
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
	 * �����������������
	 * @param input �������飬��һλΪatg����λ��,Ҳ������Ҫ�����λ��
	 * @param alignATGSite �ATG��λ��ľ���λ�ã���Ҫ����λ��ǰ��ĳ���
	 * @param ATGbody Atg�����ܹ��೤��������Atgλ��,��Ҫ����λ��������ж೤
	 * @param atgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bo������������λ��
	 * @param alignDown ѡȡATG���ζ���bp,������ATGλ�㡣 -1Ϊȫѡ ѡȡ����λ������ζ���bp������������λ��
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0 ��λ��ʲô���
	 * @return
	 */
	public static double[] setDouble(double[] atg, int alignATGSite, int ATGbody ,int atgUp, int alignDown,int filled ) {
		int atgOld = (int)atg[0];
		int bias = alignATGSite - atgOld;
		double[] tmpresult = null;
		if (alignDown > 0) {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+alignDown+1];
			else 
				tmpresult = new double[alignATGSite+alignDown];
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
			for (int i = 0; i < atg.length-1; i++) {
				if (i+bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = atg[i+1];
			}
		}
		else {
			if (atgOld > atgUp) {
				int k = 0;
				for (int i = atgOld - atgUp - 1; i < atg.length-1; i++) {
					 if (k >= tmpresult.length) {
							break;
					 }
					 tmpresult[k] = atg[i+1];
					 k++;
				}
			}
			else {
				int k = 1;
				for (int i = atgUp - atgOld + 1; i < tmpresult.length; i++) {
					if (k >= atg.length) {
						break;
					}
					tmpresult[i] = atg[k];
					k++;
				}
			}
		}
		return tmpresult;
	}
	
	
	/**
	 *	����ת¼�������ظ�ת¼����mRNAˮƽ����
	 * @param chrID
	 * @param gffGeneIsoSearch
	 * @return
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
	 */
	protected abstract double[] getReadsInfo(String geneID, GffGeneIsoSearch gffGeneIsoSearch, int normalizeType);
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
}
