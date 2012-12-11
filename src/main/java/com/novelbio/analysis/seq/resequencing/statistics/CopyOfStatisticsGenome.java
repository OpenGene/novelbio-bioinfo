package com.novelbio.analysis.seq.resequencing.statistics;

import java.awt.Color;

import java.util.HashMap;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpIndelHomoHetoType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.dataStructure.listOperate.HistList.HistBinType;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

import de.erichseifert.gral.plots.BoxPlot;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;

public class CopyOfStatisticsGenome {
	private static Logger logger = Logger.getLogger(CopyOfStatisticsGenome.class);
	
	public static void main(String[] args) {
		Species species = new Species(39947);
		CopyOfStatisticsGenome statisticsGenome = new CopyOfStatisticsGenome();
		statisticsGenome.setSpecies(species);
		
		statisticsGenome.setGapMaxNum(30);
		statisticsGenome.setSnpDetectThreshold(10);
		statisticsGenome.setReadsNum(5);

		statisticsGenome.setATBoxPlotList();

		statisticsGenome.setCGBoxPlotList();

		HistList InserthistList = statisticsGenome.getInsertHistList();
		statisticsGenome.setHistListStyle(InserthistList, 15, 1, 1500);

		HistList delHistList = statisticsGenome.getDeletionHistList();
		statisticsGenome.setHistListStyle(delHistList, 15, 1, 1500);

		HistList cGNumHistList = statisticsGenome.getCG2NumHistList();
		statisticsGenome.setHistListStyle(cGNumHistList, 15, 1, 1500);

		HistList aTNumHistList = statisticsGenome.getAT2NumHistList();
		statisticsGenome.setHistListStyle(aTNumHistList, 15, 1, 1500);

		statisticsGenome.readAndRecord("/home/ywd/��Ŀ/BZ9522_sorted_realign_removeDuplicate_pileup.gz");
		logger.error(1);
		BarStyle dotStyle = new BarStyle();
		dotStyle.setColor(Color.blue);
		dotStyle.setColorEdge(Color.black);
		logger.error(2);
		PlotScatter insertPlotScatter = InserthistList.getPlotHistBar(dotStyle);
		insertPlotScatter.setBg(Color.white);
		insertPlotScatter.saveToFile("/home/ywd/draw/inert.png", 2000, 2000);
		logger.error(3);
		PlotScatter deletionPlotScatter = delHistList.getPlotHistBar(dotStyle);
		deletionPlotScatter.setBg(Color.white);
		deletionPlotScatter.saveToFile("/home/ywd/draw/delect.png", 2000, 2000);
		logger.error(4);
		PlotScatter plotScatterCGnum = cGNumHistList.getPlotHistBar(dotStyle);
		plotScatterCGnum.setBg(Color.white);
		plotScatterCGnum.saveToFile("/home/ywd/draw/CG2num.png", 2000, 2000);
		logger.error(5);
		PlotScatter plotScatterATnum = aTNumHistList.getPlotHistBar(dotStyle);
		plotScatterATnum.setBg(Color.white);
		plotScatterATnum.saveToFile("/home/ywd/draw/AT2num.png", 2000, 2000);
		logger.error(6);
		statisticsGenome.drawATBox("/home/ywd/draw/ATBox.png");
		statisticsGenome.drawCGBox("/home/ywd/draw/CGBox.png");
		logger.error(7);
		HistList histListReadsCover = statisticsGenome.getHistListReadsCover();
		statisticsGenome.setHistListStyle(histListReadsCover, 50, 1, 15000);
   
		statisticsGenome.getAllReadsCover2Num();
		statisticsGenome.addNumReadsCoverHistList();
		PlotScatter plotScatterReadsNum = histListReadsCover.getPlotHistBar(dotStyle);
		plotScatterReadsNum.setBg(Color.white);
		plotScatterReadsNum.saveToFile("/home/ywd/draw/allReads2num.png", 2000,2000);
		logger.error(9);
		HistList histListReadsStack = statisticsGenome.getHistListReadsStack();
		statisticsGenome.setHistListStyle(histListReadsStack, 50, 5, 15000);
		statisticsGenome.addNumStack();
		PlotScatter plotScatterReadsStack = histListReadsStack.getPlotHistBar(dotStyle);
		plotScatterReadsStack.setBg(Color.white);
		plotScatterReadsStack.saveToFile("/home/ywd/draw/allReads2numStack.png", 2000, 2000);
		
	}
	
	GffChrAbs gffChrAbs = new GffChrAbs();

	/** ��¼������Ϊ����ٷֱ���׼�� */
	private int recorderNum = 0;
	/** ��¼�������� */
	private int sameNum;
	private int snpThresholdReadsNum;
	
	/** ��¼���������ĵ�indelReads��*/
	private int insertReads;
	/** ��¼���������ĵ�deletionReads��*/
	private int deletionReads;
	
	/**�趨������ͬ����ĸ�������ΪPGM��HomoPolymer��ʱ������Indel������Ҫ��鳤����������У�����indel�����
	 * ��������趨������ͬ��������������ڼ��ֲ�
	 *  */
	private int homoPolymerNum;
	
	/** ��Ϊ���� */
	private int insertNum;
	private int deletionNum;
	
	/** ���������䣬���ڸþ���Ͳ�����ͳ�� */
	private int gapMaxNum;

	/**
	 * percentage snp�������ڸðٷֱȵ�λ����Ϊ�Ǵ���snp ����Ϊ �ٷֱ� * 100
	 */
	private int snpThresholdPercentage;
	
	OneSeqInfo oneSeqInfoLast;

	/** ��¼���е�reads����������ִ����Ĺ�ϵ������reads��=1�ĳ�����1000�˼�Ϊ1_1000 */
	private HashMap<Integer, Integer> allReads2Num = new HashMap<Integer, Integer>();
	/**
	 * ��¼���е�reads����������ִ����Ĺ�ϵ������reads��>=1�ĳ�����2000�˼�Ϊ1_2000,
	 * ����1����getBoxPlotListCG�Ķ�����reads��>=1��
	 * Ҳ���ǰ������allReads2Num��1��2��3��4....n��reads��������
	 */
	private HashMap<Integer, Integer> allReadsCover2Num = new HashMap<Integer, Integer>();
	
	/** ����1AT��2AT��3AT....��reads���Ƕ� */
	BoxPlotList boxPlotListAT = new BoxPlotList();
	/** ����1CG��2CG��3CG....��reads���Ƕ� */
	BoxPlotList boxPlotListCG = new BoxPlotList();
	/** ����reads���Ƕȵ�histlist */
	private HistList histListReadsCover = HistList.creatHistList("allReadscover", true);
	/** ����reads�ѵ���histlist */
	private HistList histListReadsStack = HistList.creatHistList("histListReadsStack", true);
	/** AT���Ƕȵ�HistList */
	private HistList histListATNum = HistList.creatHistList("AT2num", true);
	/** CG���Ƕȵ�HistList */
	private HistList histListCGNum = HistList.creatHistList("CG2num", true);
	/** ��������������insert��Histlist */
	private HistList histListInsert = HistList.creatHistList("Inert", true);
	/** ��������������deletion��HistList */
	private HistList histListdeletion = HistList.creatHistList("DeleTion", true);
	/** ������������ͬ�ļ�������ҳ�����insert�������ͷ�������֮��Ĺ�ϵ*/
	private HistList histListInsertNum = HistList.creatHistList("InertNum2Happen", true);
	/** ������������ͬ�ļ�������ҳ�����delection�������ͷ�������֮��Ĺ�ϵ*/
	private HistList histListDelectionNum = HistList.creatHistList("Delection2Happen", true);
	
	/** ��¼Exonλ����Ϣ��list��Start_end; 
	 * �����Ӳ����õ�����Ϊ�����Ӳ����ͳ������������ĸ��Ƕȡ�
	 */
	private Queue<? extends Alignment> lsExonStartAndEnd;

	BoxStyle boxStyle = getBoxStyle();

	public CopyOfStatisticsGenome() {
		boxStyle.setColor(Color.white);
	}
	/**
	 * ��Ϊ����������ͬ����ĸ���
	 * @param sameNumSet
	 */
	public void setSameNumSet(int sameNumSet) {
		this.homoPolymerNum = sameNumSet;
	}
	/**
	 * ���ڸþ���Ͳ�����ͳ��
	 * 
	 * @param gapMaxNum
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	/**
	 * ����������Ϣ
	 */
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}

	/**
	 * ���õ�ǰ����Exon��λ����Ϣ
	 * 
	 * @param lsExonStartAndEnd
	 */
	public void setLsExonStartAndEnd(Queue<? extends Alignment> lsExonStartAndEnd) {
		this.lsExonStartAndEnd = lsExonStartAndEnd;
	}

	/**
	 * @param percentage
	 *            ����snp������reads�ٷֱ� ����Ϊ �ٷֱ� * 100
	 */
	public void setSnpDetectThreshold(int percentage) {
		this.snpThresholdPercentage = percentage;
	}

	/**
	 * ����snp������reads��
	 * 
	 * @param readsNum
	 */
	public void setReadsNum(int readsNum) {
		this.snpThresholdReadsNum = readsNum;
	}
	
	/**
	 * ��ȡ������������ͬ�ļ�������ҳ�����insert�������ͷ�������֮��Ĺ�ϵ��HistList
	 * @return
	 */
	public HistList getInsertNumHistList() {
		return histListInsertNum;
	}
	/**
	 * ��ȡ������������ͬ�ļ�������ҳ�����delection�������ͷ�������֮��Ĺ�ϵ��HistList
	 * @return
	 */
	public HistList getDelectionNumHistList() {
		return histListDelectionNum;
	}
	/**
	 * ��ȡ����reads����
	 * 
	 * @return allReadsCover2Num
	 */
	public HashMap<Integer, Integer> getAllReadsCover2Num() {
		for (Integer num1 : allReads2Num.keySet()) {
			int accNum = 0;
			for (Integer num2 : allReads2Num.keySet()) {
				if (num1 <= num2) {
					accNum = accNum + allReads2Num.get(num2);
				}
			}
			allReadsCover2Num.put(num1, accNum);
		}
		return allReadsCover2Num;
	}

	/**
	 * public��������ʹ��
	 * �Ѹ�������ϵ�reads�ѵ��������ͳ��ֵĴ�������histList
	 */
	public void addNumStack() {
		for (Integer num : allReads2Num.keySet()) {
			histListReadsStack.addNum(num, allReads2Num.get(num));
		}
	}

	/**
	 * ��ȡ��ǰreads�ѵ�����������ִ�����histist
	 * 
	 * @returnstatisticsGenome
	 */
	public HistList getHistListReadsStack() {
		return histListReadsStack;
	}

	/**
	 * ��ȡ����reads���ǵ�histist
	 * 
	 * @return
	 */
	public HistList getHistListReadsCover() {
		return histListReadsCover;
	}

	/**
	 * ��histListReadsCover�м����
	 */
	public void addNumReadsCoverHistList() {
		for (Integer num : allReadsCover2Num.keySet()) {
			int coverPer = allReadsCover2Num.get(num) * 100 / recorderNum;
			if (coverPer < 0) {
				System.out.println(coverPer);
			}
			histListReadsCover.addNum(num, coverPer);
		}
	}

	/**
	 * ��������ʹ�� �Զ�����histlist��bin
	 * @param histList
	 * @param binNum bin�ĸ���
	 * @param interval ���
	 * @param maxSize  ���ֵ
	 */
	public void setHistListStyle(HistList histList, int binNum, int interval,int maxSize) {
		histList.setHistBinType(HistBinType.LcloseRopen);
		histList.setStartBin(interval, "sameNum", 0, interval);
		int binEnd = interval * 2;
		int i = 1;
		while (i <= binNum) {
			histList.addHistBin(binEnd, Integer.toString(binEnd), binEnd);
			binEnd = binEnd + interval;
			i++;
		}
		if (binEnd < maxSize) {
			histList.addHistBin(binEnd, binEnd + "", maxSize);
		}
	}

	/**
	 * ͳ�Ʋ���ȱʧλ�㸽���ж��� homopolymose ��ΪPGM������������ͬ�����������indel����
	 * 
	 * @return
	 */
	public HistList getInsertHistList() {
		return this.histListInsert;
	}

	/**
	 * ͳ�Ʋ���ȱʧλ�㸽���ж��� homopolymose ��ΪPGM������������ͬ�����������delection����
	 * 
	 * @return
	 */
	public HistList getDeletionHistList() {
		return histListdeletion;
	}

	/**
	 * ��ȡ����CG������ֱ��ͼ Ʃ�磺 �������� 1��C���ּ��� 2��C���ּ���
	 * 
	 * @return
	 */
	public HistList getCG2NumHistList() {
		return histListCGNum;
	}

	/**
	 * ����AT��reads������ȵĺ�ͼ�� ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
	 * 
	 * @param outFile
	 * @param species
	 */
	public void drawATBox(String outFile) {
		BoxPlotList boxPlotList = getBoxPlotListAT();
		PlotBox plotBox = boxPlotList.getPlotBox(boxStyle);
		plotBox.setBg(Color.white);
		plotBox.saveToFile(outFile, 2000, 2000);
	}

	/**
	 * ����AT��reads������ȵĺ�ͼ�� ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
	 * 
	 * @param outFile
	 * @param species
	 */
	public void drawCGBox(String outFile) {
		BoxPlotList boxPlotList = getBoxPlotListCG();
		PlotBox plotBox = boxPlotList.getPlotBox(boxStyle);
		plotBox.setBg(Color.white);
		plotBox.saveToFile(outFile, 2000, 2000);
	}

	/**
	 * ��ȡATBoxPlotList ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
	 * 
	 * @return
	 */
	public BoxPlotList getBoxPlotListAT() {
		return boxPlotListAT;
	}

	public BoxPlotList getBoxPlotListCG() {
		return boxPlotListCG;
	}

	public void setATBoxPlotList() {
		for (int i = 0; i < 50; i++) {
			HistList histList = getATHistList(i);
			boxPlotListAT.addHistList(histList);
		}
	}

	/**
	 * ��ȡCGBoxPlotList ����ͳ�ƣ� 1��A��reads������� ... n��AT��reads�������
	 * 
	 * @return
	 */
	public void setCGBoxPlotList() {
		for (int i = 0; i < 50; i++) {
			HistList histList = getCGHistList(i);
			boxPlotListCG.addHistList(histList);
		}
	}

	/**
	 * ����atNum��ȡ����histList������5A��histList
	 * 
	 * @param atstrNum
	 * @return
	 */
	private HistList getATHistList(int atstrNum) {
		HistList histList = HistList.creatHistList(atstrNum + "AT", true);
		setHistListStyle(histList, 20, 1, 500);
		return histList;
	}

	/**
	 * ����cgNum��ȡ����histList������4C��
	 * 
	 * @param cgNum
	 * @return
	 */
	private HistList getCGHistList(int cgNum) {
		HistList histList = HistList.creatHistList(cgNum + "CG", true);
		setHistListStyle(histList, 20, 1, 500);
		return histList;
	}

	/**
	 * 
	 * @return ����BoxStyle��AT��CGͨ��
	 */
	public BoxStyle getBoxStyle() {
		BoxStyle boxStyle = new BoxStyle();
		boxStyle.setColor(Color.white);
		boxStyle.setColorBoxCenter(Color.red);
		boxStyle.setColorBoxEdge(Color.green);
		boxStyle.setBasicStroke(4f);
		boxStyle.setColorBoxWhisker(Color.black);
		return boxStyle;
	}

	/**
	 * ��ȡPlotScatter
	 * 
	 * @param histList
	 *            ����histList
	 * @return
	 */
	public PlotScatter getPlotScatter(HistList histList) {
		BarStyle barStyle = new BarStyle();
		barStyle.setColor(Color.BLUE);
		barStyle.setColorEdge(Color.BLACK);
		PlotScatter plotScatter = histList.getPlotHistBar(barStyle);
		return plotScatter;
	}

	/**
	 * ��ȡ��ǰATHistList
	 * 
	 * @return
	 */
	public HistList getAT2NumHistList() {
		return histListATNum;
	}

	/**
	 * ������ǰCG�ֲ�������ͼ
	 * 
	 * @param outFile
	 * @param species
	 */
	public void drawATNumPng(String outFile, Species species) {
		HistList histList = getAT2NumHistList();
		PlotScatter plotScatter = getPlotScatter(histList);
		plotScatter.setBg(Color.white);
		plotScatter.saveToFile(outFile, 1000, 1000);
	}

	/**
	 * ����CG�ֲ�������ͼ
	 * 
	 * @param outFile
	 *            ���·��CGnum2Num
	 * @param species
	 *            ������Ϣ
	 */
	public void drawCGnumPng(String outFile, Species species) {
		HistList CGHisList = getCG2NumHistList();
		PlotScatter plotScatter = getPlotScatter(CGHisList);
		plotScatter.setBg(Color.white);
		plotScatter.saveToFile(outFile, 1000, 1000);
	}

	/**
	 * ��ȡ�ļ�
	 */
	public void readAndRecord(String loadingFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(loadingFile, false);
		for (String tmpline : txtWrite.readlines()) {
			
			if (lsExonStartAndEnd == null) {
				OneSeqInfo oneSeqInfo = new OneSeqInfo(tmpline, oneSeqInfoLast,snpThresholdReadsNum, snpThresholdPercentage);
				countOneCGAndATCover(oneSeqInfo, oneSeqInfoLast);
				oneSeqInfoLast = oneSeqInfo;
				if (recorderNum % 10 == 0) {
					logger.error(recorderNum);
				}
			}else {
				setLsExonStartAndEnd(lsExonStartAndEnd);
				
				OneSeqInfo oneSeqInfo = new OneSeqInfo(tmpline, oneSeqInfoLast,snpThresholdReadsNum, snpThresholdPercentage);
				if (!isExon(oneSeqInfo)) {
					oneSeqInfo = null;
				}
				countOneCGAndATCover(oneSeqInfo, oneSeqInfoLast);
				oneSeqInfoLast = oneSeqInfo;
				if (recorderNum % 10 == 0) {
					logger.error(recorderNum);
				}
			
			}
			
			// TODO
//			if (recorderNum > 3000000) {
//				break;
//			}
		}

	}

	/**
	 * ��¼AT����CG������
	 * 
	 * @param oneSeqInfo
	 */
	private void countOneCGAndATCover(OneSeqInfo oneSeqInfo,
			OneSeqInfo oneSeqInfoUp) {
		if (oneSeqInfo == null || !oneSeqInfo.isSameChrID(oneSeqInfoUp)) {
			recordInsertAndDeletion(oneSeqInfo, oneSeqInfoUp);
			recordAllReadsCover(oneSeqInfo);
			recordAT_rawsLength2Num(oneSeqInfoUp, oneSeqInfo);
			recordCG_rawsLength2Num(oneSeqInfoUp, oneSeqInfo);
			this.oneSeqInfoLast = null;
		} else {
			if (oneSeqInfo.isContinuesSite(oneSeqInfoUp)) {
				recordInsertAndDeletion(oneSeqInfo, oneSeqInfoUp);
				recordATorCGNum(oneSeqInfoUp, oneSeqInfo);
				recordAT_rawsLength2Num(oneSeqInfoUp, oneSeqInfo);
				recordCG_rawsLength2Num(oneSeqInfoUp, oneSeqInfo);
				recordAllReadsCover(oneSeqInfo);
			}
			// ����site�м���˺ܶ࣬����˵�м��кܶ�����readsû�и��ǵ�
			else {
				String seqGap;
				if (oneSeqInfo.getsiteThis() - oneSeqInfoUp.getsiteThis() - 1 == gapMaxNum) {
					recordInsertAndDeletion(null, oneSeqInfoUp);
					recordATorCGNum(oneSeqInfoUp, null);
					recordAT_rawsLength2Num(oneSeqInfoUp, null);
					recordCG_rawsLength2Num(oneSeqInfoUp, null);
					recordAllReadsCover(null);
					this.oneSeqInfoLast = null;
					return;
				} else {
					SeqFasta seqFasta = null;
					try {
						seqFasta = gffChrAbs.getSeqHash().getSeq(true,
								oneSeqInfo.getChrThis(),
								oneSeqInfoUp.getsiteThis() + 1,
								oneSeqInfo.getsiteThis() - 1);
					} catch (Exception e) {
						oneSeqInfoUp = null;
						return;
						// TODO: handle exception
					}
					
					seqGap = seqFasta.toString();
					// �����м�Ͽ����������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ��������
					// gapǰ����Ǹ�λ��
					OneSeqInfo oneSeqInfoGapEdgeUp = oneSeqInfoUp;
					OneSeqInfo oneSeqInfoGapEdgeDown = null;
					char[] chrGapSeq = seqGap.toCharArray();
					for (int i = 0; i < chrGapSeq.length; i++) {
						String oneSeq = chrGapSeq[i] + "";
						oneSeqInfoGapEdgeDown = getNextSeqInfoInGap_And_Statistics(
								oneSeqInfoGapEdgeUp, oneSeq);
						recordInsertAndDeletion(oneSeqInfoGapEdgeDown, oneSeqInfoGapEdgeUp);
						recordAllReadsCover(oneSeqInfoGapEdgeDown);
						recordATorCGNum(oneSeqInfoGapEdgeUp,
								oneSeqInfoGapEdgeDown);
						recordAT_rawsLength2Num(oneSeqInfoGapEdgeUp,
								oneSeqInfoGapEdgeDown);
						recordCG_rawsLength2Num(oneSeqInfoGapEdgeUp,
								oneSeqInfoGapEdgeDown);
						oneSeqInfoGapEdgeUp = oneSeqInfoGapEdgeDown;
					}
					this.oneSeqInfoLast = oneSeqInfoGapEdgeDown;
					// ����һ����ȡ��������û����ȫ��ȡ����
					if (oneSeqInfoGapEdgeDown.getsiteThis() + 1 != oneSeqInfo
							.getsiteThis()) {
						logger.error("������"
								+ (oneSeqInfo.getsiteThis() - oneSeqInfoGapEdgeDown
										.getsiteThis()));
					}
				}
			}
		}
	}

	/**
	 * �����м�Ͽ��������Gap�ĵ�һ��λ����˳������һ��OneSeqInfo��Ȼ�������� ��󷵻�Gapĩβ���Ǹ�OneSeqInfo
	 * 
	 * @param oneSeqInfoGapEdge
	 *            gap�ϱ�Ե���Ǹ�site
	 * @param seqGap
	 *            gap����
	 * @return ����Gap�����һλsite
	 */
	private OneSeqInfo getNextSeqInfoInGap_And_Statistics(
			OneSeqInfo oneSeqInfoGapEdge, String oneSeq) {
		OneSeqInfo oneSeqInfoGapNext = oneSeqInfoGapEdge
				.getOneSeqInfoNext(oneSeq);
		return oneSeqInfoGapNext;
	}

	/** ͳ������AT��������CG���ֵĴ����͸��Ƕȵ� */
	private void recordATorCGNum(OneSeqInfo oneSeqInfoUp, OneSeqInfo oneSeqInfo) {
		if (oneSeqInfoUp == null
				|| (oneSeqInfo != null && oneSeqInfoUp
						.isSameSiteType_And_Not_N(oneSeqInfo))) {
			return;
		} else {
			SeqType seqTypeOld = oneSeqInfoUp.getSiteSeqType();
			if (seqTypeOld == SeqType.N) {
				return;
			} else if (seqTypeOld == SeqType.CG) {
				histListCGNum.addNum(oneSeqInfoUp.getSameSiteNum());
			} else if (seqTypeOld == SeqType.AT) {
				histListATNum.addNum(oneSeqInfoUp.getSameSiteNum());
			}
		}
	}

	/**
	 * 
	 */
	private void recordAT_rawsLength2Num(OneSeqInfo oneSeqInfoUp,
			OneSeqInfo oneSeqInfo) {
		if (oneSeqInfoUp == null) {
			return;
		}
		Integer atNum = oneSeqInfoUp.getSameSiteNum();
		if (atNum >= 50) {
			atNum = 49;
		}
		String key = atNum + "AT";
		HistList histList = boxPlotListAT.getHistList(key);
		double coverageAvg = oneSeqInfoUp.getSameSiteNumAvg();
		if (coverageAvg >= 499) {
			coverageAvg = 499;
		}
		if (oneSeqInfo == null) {
			if (oneSeqInfoUp.getSiteSeqType() == SeqType.AT) {
				int coverageAvgInt = norm4To5(coverageAvg);
				histList.addNum(coverageAvgInt);
			}
		} else {
			if (oneSeqInfoUp.getSiteSeqType() == SeqType.AT
					&& (!oneSeqInfoUp.isSameSiteType_And_Not_N(oneSeqInfo))) {
				int coverageAvgInt = norm4To5(coverageAvg);
				histList.addNum(coverageAvgInt);
			}
		}
	}

	/**
	 * ǰһ����C��ǰһ����A����¼ǰ������C��rawsƽ���������� ��ʽ����key��4C_10��value��num��������3
	 */
	private void recordCG_rawsLength2Num(OneSeqInfo oneSeqInfoUp,
			OneSeqInfo oneSeqInfo) {
		if (oneSeqInfoUp == null) {
			return;
		}
		Integer cgNum = oneSeqInfoUp.getSameSiteNum();
		if (cgNum >= 50) {
			cgNum = 49;
		}
		String key = cgNum + "CG";
		HistList histList = boxPlotListCG.getHistList(key);
		double coverageAvg = oneSeqInfoUp.getSameSiteNumAvg();
		if (coverageAvg >= 499) {
			coverageAvg = 499;
		}
		if (oneSeqInfo == null) {
			if (oneSeqInfoUp.getSiteSeqType() == SeqType.CG) {
				int coverageAvgInt = norm4To5(coverageAvg);
				histList.addNum(coverageAvgInt);
			}
		} else {
			if (oneSeqInfoUp.getSiteSeqType() == SeqType.CG
					&& (!oneSeqInfoUp.isSameSiteType_And_Not_N(oneSeqInfo))) {
				int coverageAvgInt = norm4To5(coverageAvg);
				histList.addNum(coverageAvgInt);
			}
		}
	}

	/**
	 * ��¼insert��delection
	 * @param oneSeqInfo
	 */
	private void recordInsertAndDeletion(OneSeqInfo oneSeqInfo, OneSeqInfo oneSeqInfoUp) {
		if (oneSeqInfo != null) {
			if (oneSeqInfoUp != null) {
				if (!oneSeqInfoUp.getStrSeq().equals(oneSeqInfo.getStrSeq())) {
					upisNotSameDown();
					insertNum = 0;
					deletionNum = 0;
					sameNum = 1;
					insertReads = 0;
					recordInsertAndDelReadsNum(oneSeqInfo);
				}else {
					recordInsertAndDelReadsNum(oneSeqInfo);
					sameNum ++ ;
				}
			}else {
				recordInsertAndDelReadsNum(oneSeqInfo);
				sameNum ++;
			}
			if (oneSeqInfo.isInsert()) {
				insertNum ++ ;
			}
			if (oneSeqInfo.isDeletion()) {
				deletionNum ++;
			}
		}else {
			upisNotSameDown();
			deletionNum = 0;
			insertNum = 0;
		}
	}
	
	/**
	 * ��¼������������indelreads����
	 * @param oneSeqInfo
	 */
	private void recordInsertAndDelReadsNum(OneSeqInfo oneSeqInfo) {
		if (oneSeqInfo.getSnpIndelType() == SnpIndelType.INSERT) {
			insertReads = insertReads + oneSeqInfo.getIndelReadsNum();
		}
		if (oneSeqInfo.getSnpIndelType() == SnpIndelType.DELETION) {
			deletionReads = deletionReads + oneSeqInfo.getIndelReadsNum();
		}
	}
	/**
	 * ���¼����һ����ʱ��Ĳ���
	 */
	private void upisNotSameDown() {
		if (insertNum > 0) {
			histListInsert.addNum(sameNum);
		}
		if (deletionNum > 0) {
			histListdeletion.addNum(sameNum);
		}
		if (sameNum == homoPolymerNum) {
			if (insertReads > 0) {
				histListInsertNum.addNum(insertReads);
			}
			if (deletionReads > 0) {
				histListDelectionNum.addNum(deletionReads);
			}
			
		}
	}


	private boolean isExon(OneSeqInfo oneSeqInfo) {
		// TODO ���һ��
		Alignment alignment = lsExonStartAndEnd.poll();
		while (oneSeqInfo.getsiteThis() > alignment.getEndAbs()
				&& !lsExonStartAndEnd.isEmpty()) {
			alignment = lsExonStartAndEnd.poll();
		}
		if (oneSeqInfo.getsiteThis() < alignment.getStartAbs()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * ��¼���е��ļ���reads���������
	 * 
	 * @param oneSeqInfo
	 */
	private void recordAllReadsCover(OneSeqInfo oneSeqInfo) {
		Integer ReadsNum;
		if (oneSeqInfo == null) {
			ReadsNum = 0;
		} else {
			ReadsNum = oneSeqInfo.getReadsCumulativeNum();
		}
		if (allReads2Num.keySet().contains(ReadsNum)) {
			allReads2Num.put(ReadsNum, allReads2Num.get(ReadsNum) + 1);
		} else {
			allReads2Num.put(ReadsNum, 1);
		}
		recorderNum++;
	}

	/**
	 * ��������
	 * 
	 * @param num
	 * @return
	 */
	public static int norm4To5(Double num) {
		num = num + 0.5;
		return num.intValue();
	}

}
