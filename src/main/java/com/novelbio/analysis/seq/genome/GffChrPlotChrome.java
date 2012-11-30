package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;

import com.novelbio.database.model.species.Species;

import de.erichseifert.gral.util.GraphicsUtils;
/**
 * ����Ⱦɫ��ֲ���ͼ
 * @author zong0jie
 *
 */
public class GffChrPlotChrome {
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	private static final Logger logger = Logger.getLogger(GffChrMap.class);
	String fileName = "";
	int maxresolution = 10000;
	MapReads mapReads;
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	
	int[] tssRegion;
	
	public GffChrPlotChrome() {
	}
	
	public GffChrPlotChrome(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��GffChrAbs����
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	public void setTssRegion(int[] tssRegion) {
		this.tssRegion = tssRegion;
	}
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	/**
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public int getThisInv() {
		return mapReads.getBinNum();
	}
	/**
	 * ����Ⱦɫ������ͳ��ÿ��Ⱦɫ������λ������ÿ��λ������ string[4] 0: chrID 1: readsNum 2: readsPipNum
	 * 3: readsPipMean
	 * @return
	 */
	public ArrayList<String[]> getChrLenInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		for (String string : lsChrID) {
			String[] chrInfoTmp = new String[4];
			chrInfoTmp[0] = string;
			chrInfoTmp[1] = mapReads.getChrReadsNum(string) + "";
			chrInfoTmp[2] = mapReads.getChrReadsPipNum(string) + "";
			chrInfoTmp[3] = mapReads.getChrReadsPipMean(string) + "";
			lsResult.add(chrInfoTmp);
		}
		return lsResult;
	}
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
	}
	
	/**
	 * ��Ҫ����ȫ��������ͼ�ģ����Խ�������ջ�������еߵ�
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num ����ڼ�����Ʃ�������뿴��һ���ں��ӻ��ߵ�һ�������� С�ڵ���0��ʾ��ȫ��
	 * @return
	 */
	public void setFilterChrDistInfo(GeneStructure geneStructure, int num) {
		if (geneStructure == GeneStructure.ALL) {
			mapReads.setMapChrID2LsAlignments(null);
			return;
		}
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		HashMap<String, List<? extends Alignment>> mapChrID2LsAlignment = new HashMap<String, List<? extends Alignment>>();
		for (String chrID : gffHashGene.getMapChrID2LsGff().keySet()) {
			ArrayList<SiteInfo> lsAlignment = new ArrayList<SiteInfo>();
			ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID.toLowerCase());
			for (GffDetailGene gffDetailGene : listGff) {
				lsAlignment.addAll(getGeneStructureRangeForChrPlot(geneStructure, gffDetailGene, num));
			}
			SiteInfo.setCompareType(SiteInfo.COMPARE_LOCSITE);
			Collections.sort(lsAlignment);
			mapChrID2LsAlignment.put(chrID.toLowerCase(), lsAlignment);
		}
		mapReads.setMapChrID2LsAlignments(mapChrID2LsAlignment);
	}
	/**
	 * ��Ҫ����ȫ��������ͼ�ģ����Խ�������ջ�������еߵ�
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num ����ڼ�����Ʃ�������뿴��һ���ں��ӻ��ߵ�һ�������� С�ڵ���0��ʾ��ȫ��
	 * @return
	 */
	private ArrayList<SiteInfo> getGeneStructureRangeForChrPlot(GeneStructure geneStructure, GffDetailGene gffDetailGene, int num) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		ArrayList<SiteInfo> lsResult = new ArrayList<SiteInfo>();
		
		if (geneStructure == GeneStructure.TSS) {
			SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + tssRegion[0], gffGeneIsoInfo.getTSSsite() + tssRegion[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() - tssRegion[1], gffGeneIsoInfo.getTSSsite() - tssRegion[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.TES) {
			SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + tssRegion[0], gffGeneIsoInfo.getTESsite() + tssRegion[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() - tssRegion[1], gffGeneIsoInfo.getTESsite() - tssRegion[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.EXON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				if (gffGeneIsoInfo.size() > num) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(gffGeneIsoInfo.get(num - 1).getStartAbs(), gffGeneIsoInfo.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
		}
		
		else if (geneStructure == GeneStructure.INTRON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo.getLsIntron()) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				ArrayList<ExonInfo> lsIntron = gffGeneIsoInfo.getLsIntron();
				if (lsIntron.size() >= num) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(lsIntron.get(num - 1).getStartAbs(), lsIntron.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
			
		} else if (geneStructure == GeneStructure.UTR5) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR5seq()) {
				SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		} else if (geneStructure == GeneStructure.UTR3) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR3seq()) {
				SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		}
		return lsResult;
	}
 
	/**
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * ��java��
	 * @param outPathPrefix ����ļ���+ǰ׺
	 * @throws Exception
	 */
	public void plotAllChrDist(String outPathPrefix) {
		ArrayList<String[]> chrlengthInfo = gffChrAbs.getSeqHash().getChrLengthInfo();
		//find the longest chromosome's density
		double[] chrReads = getChrDensity(chrlengthInfo.get(chrlengthInfo.size() - 1)[0], maxresolution);
		double axisY = MathComput.median(chrReads, 95)*4;
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				plotChrDist(chrlengthInfo.get(i)[0], maxresolution, axisY, FileOperate.changeFileSuffix(outPathPrefix, "_"+chrlengthInfo.get(i)[0], "png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * 
	 * @param chrID
	 *            �ڼ�����ɫ��
	 * @param maxresolution
	 *            ��ֱ���
	 * @param axisY y��߽�
	 * @param outFileName ����ļ���������׺"_chrID"
	 * @throws Exception
	 */
	private void plotChrDist(String chrID, int maxresolution, double axisY, String outFileName) throws Exception {
		int[] resolution = gffChrAbs.getSeqHash().getChrRes(chrID, maxresolution);
		long chrLengthMax = gffChrAbs.getSeqHash().getChrLenMax();
		double interval = ((int)(chrLengthMax/30)/1000)*1000;
		long chrLength = gffChrAbs.getSeqHash().getChrLength(chrID);
		
		/////////////////////   plotScatter can only accept double data   //////////////////////////////
		double[] resolutionDoub = new double[resolution.length];
		for (int i = 0; i < resolution.length; i++) {
			resolutionDoub[i] = i;
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] chrReads = null;
		try {
			chrReads = getChrDensity(chrID.toLowerCase(), resolution.length);
		} catch (Exception e) {
			logger.error("����δ֪chrID��" + chrID);
			return;
		}
		if (chrReads == null) {
			return;
		}
		
		PlotScatter plotScatter = new PlotScatter();
		plotScatter.setAxisX(0, maxresolution);
		plotScatter.setAxisY(0, axisY);
		plotScatter.setMapNum2ChangeX(0, 0, resolution.length, chrLength, interval);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(resolutionDoub, chrReads, dotStyle);
		
		//////////////////��ӱ߿�///////////////////////////////
		DotStyle dotStyleBroad = new DotStyle();
		dotStyleBroad.setStyle(DotStyle.STYLE_LINE);
		dotStyleBroad.setColor(Color.RED);
		dotStyleBroad.setSize(DotStyle.SIZE_B);
		double[] xstart = new double[]{0,0}; double[] xend= new double[]{resolutionDoub[resolutionDoub.length-1], resolutionDoub[resolutionDoub.length-1]};
		double[] y = new double[]{0, axisY};
		plotScatter.addXY(xend, y, dotStyleBroad);
		plotScatter.addXY(xstart, y, dotStyleBroad.clone());
		//////////////////////////////////////////////////////////////
		
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		//������mapping
//		plotScatter.setMapNum2ChangeY(0, 0, axisY, 500, 100);
		plotScatter.setTitle(chrID + " Reads Density", null);
		plotScatter.setTitleX("Chromosome Length", null, 0);
		plotScatter.setTitleY("Normalized Reads Counts", null, (int)axisY/5);
		
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_ML);
		
		plotScatter.saveToFile(outFileName, 10000, 1000);
	}

	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads��������ܶ�ͼ ��Ҫ���ڻ������ϣ�һ��Ⱦɫ���ϵ�reads���
	 * 
	 * @param chrID
	 * @param binNum
	 *            �ֳɼ�������
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	private double[] getChrDensity(String chrID, int binNum) {
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		return tmpResult;
	}
}
