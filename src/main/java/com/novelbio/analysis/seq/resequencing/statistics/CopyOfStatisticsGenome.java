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

		statisticsGenome.readAndRecord("/home/ywd/项目/BZ9522_sorted_realign_removeDuplicate_pileup.gz");
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

	/** 记录总数，为下面百分比做准备 */
	private int recorderNum = 0;
	/** 记录连续数量 */
	private int sameNum;
	private int snpThresholdReadsNum;
	
	/** 记录所有连续的的indelReads数*/
	private int insertReads;
	/** 记录所有连续的的deletionReads数*/
	private int deletionReads;
	
	/**设定连续相同碱基的个数，因为PGM在HomoPolymer的时候会产生Indel，所以要检查长的连续碱基中，发生indel的情况
	 * 这里就是设定连续相同碱基的数量，用于检查分布
	 *  */
	private int homoPolymerNum;
	
	/** 作为缓存 */
	private int insertNum;
	private int deletionNum;
	
	/** 间隔最大区间，大于该距离就不进行统计 */
	private int gapMaxNum;

	/**
	 * percentage snp数量大于该百分比的位点认为是存在snp 输入为 百分比 * 100
	 */
	private int snpThresholdPercentage;
	
	OneSeqInfo oneSeqInfoLast;

	/** 记录所有的reads个数和其出现次数的关系，例如reads数=1的出现了1000此记为1_1000 */
	private HashMap<Integer, Integer> allReads2Num = new HashMap<Integer, Integer>();
	/**
	 * 记录所有的reads个数和其出现次数的关系，例如reads数>=1的出现了2000此记为1_2000,
	 * 出现1覆盖getBoxPlotListCG的定义是reads数>=1，
	 * 也就是把上面的allReads2Num，1，2，3，4....n的reads数加起来
	 */
	private HashMap<Integer, Integer> allReadsCover2Num = new HashMap<Integer, Integer>();
	
	/** 绘制1AT，2AT，3AT....的reads覆盖度 */
	BoxPlotList boxPlotListAT = new BoxPlotList();
	/** 绘制1CG，2CG，3CG....的reads覆盖度 */
	BoxPlotList boxPlotListCG = new BoxPlotList();
	/** 所有reads覆盖度的histlist */
	private HistList histListReadsCover = HistList.creatHistList("allReadscover", true);
	/** 所有reads堆叠的histlist */
	private HistList histListReadsStack = HistList.creatHistList("histListReadsStack", true);
	/** AT覆盖度的HistList */
	private HistList histListATNum = HistList.creatHistList("AT2num", true);
	/** CG覆盖度的HistList */
	private HistList histListCGNum = HistList.creatHistList("CG2num", true);
	/** 所有满足条件的insert的Histlist */
	private HistList histListInsert = HistList.creatHistList("Inert", true);
	/** 所有满足条件的deletion的HistList */
	private HistList histListdeletion = HistList.creatHistList("DeleTion", true);
	/** 给定连续的相同的碱基数，找出发生insert的数量和发生次数之间的关系*/
	private HistList histListInsertNum = HistList.creatHistList("InertNum2Happen", true);
	/** 给定连续的相同的碱基数，找出发生delection的数量和发生次数之间的关系*/
	private HistList histListDelectionNum = HistList.creatHistList("Delection2Happen", true);
	
	/** 记录Exon位点信息的list，Start_end; 
	 * 外显子测序用到，因为外显子测序仅统计外显子区域的覆盖度。
	 */
	private Queue<? extends Alignment> lsExonStartAndEnd;

	BoxStyle boxStyle = getBoxStyle();

	public CopyOfStatisticsGenome() {
		boxStyle.setColor(Color.white);
	}
	/**
	 * 认为给定连续相同碱基的个数
	 * @param sameNumSet
	 */
	public void setSameNumSet(int sameNumSet) {
		this.homoPolymerNum = sameNumSet;
	}
	/**
	 * 大于该距离就不进行统计
	 * 
	 * @param gapMaxNum
	 */
	public void setGapMaxNum(int gapMaxNum) {
		this.gapMaxNum = gapMaxNum;
	}

	/**
	 * 设置物种信息
	 */
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}

	/**
	 * 设置当前物种Exon的位置信息
	 * 
	 * @param lsExonStartAndEnd
	 */
	public void setLsExonStartAndEnd(Queue<? extends Alignment> lsExonStartAndEnd) {
		this.lsExonStartAndEnd = lsExonStartAndEnd;
	}

	/**
	 * @param percentage
	 *            发生snp的最少reads百分比 输入为 百分比 * 100
	 */
	public void setSnpDetectThreshold(int percentage) {
		this.snpThresholdPercentage = percentage;
	}

	/**
	 * 发生snp的最少reads数
	 * 
	 * @param readsNum
	 */
	public void setReadsNum(int readsNum) {
		this.snpThresholdReadsNum = readsNum;
	}
	
	/**
	 * 获取给定连续的相同的碱基数，找出发生insert的数量和发生次数之间的关系的HistList
	 * @return
	 */
	public HistList getInsertNumHistList() {
		return histListInsertNum;
	}
	/**
	 * 获取给定连续的相同的碱基数，找出发生delection的数量和发生次数之间的关系的HistList
	 * @return
	 */
	public HistList getDelectionNumHistList() {
		return histListDelectionNum;
	}
	/**
	 * 获取所有reads覆盖
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
	 * public仅供测试使用
	 * 把各个碱基上的reads堆叠的数量和出现的次数加入histList
	 */
	public void addNumStack() {
		for (Integer num : allReads2Num.keySet()) {
			histListReadsStack.addNum(num, allReads2Num.get(num));
		}
	}

	/**
	 * 获取当前reads堆叠的数量与出现次数的histist
	 * 
	 * @returnstatisticsGenome
	 */
	public HistList getHistListReadsStack() {
		return histListReadsStack;
	}

	/**
	 * 获取所有reads覆盖的histist
	 * 
	 * @return
	 */
	public HistList getHistListReadsCover() {
		return histListReadsCover;
	}

	/**
	 * 向histListReadsCover中加入点
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
	 * 仅供测试使用 自动设置histlist的bin
	 * @param histList
	 * @param binNum bin的个数
	 * @param interval 间隔
	 * @param maxSize  最大值
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
	 * 统计插入缺失位点附近有多少 homopolymose 因为PGM容易在连续相同碱基附近发生indel错误
	 * 
	 * @return
	 */
	public HistList getInsertHistList() {
		return this.histListInsert;
	}

	/**
	 * 统计插入缺失位点附近有多少 homopolymose 因为PGM容易在连续相同碱基附近发生delection错误
	 * 
	 * @return
	 */
	public HistList getDeletionHistList() {
		return histListdeletion;
	}

	/**
	 * 获取连续CG个数的直方图 譬如： 基因组上 1个C出现几次 2个C出现几次
	 * 
	 * @return
	 */
	public HistList getCG2NumHistList() {
		return histListCGNum;
	}

	/**
	 * 画出AT上reads覆盖深度的盒图， 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
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
	 * 画出AT上reads覆盖深度的盒图， 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
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
	 * 获取ATBoxPlotList 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
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
	 * 获取CGBoxPlotList 具体统计： 1个A上reads覆盖深度 ... n个AT上reads覆盖深度
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
	 * 根据atNum获取他的histList，例如5A的histList
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
	 * 根据cgNum获取他的histList，例如4C的
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
	 * @return 返回BoxStyle，AT，CG通用
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
	 * 获取PlotScatter
	 * 
	 * @param histList
	 *            给定histList
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
	 * 获取当前ATHistList
	 * 
	 * @return
	 */
	public HistList getAT2NumHistList() {
		return histListATNum;
	}

	/**
	 * 画出当前CG分布个数的图
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
	 * 画出CG分布个数的图
	 * 
	 * @param outFile
	 *            输出路径CGnum2Num
	 * @param species
	 *            物种信息
	 */
	public void drawCGnumPng(String outFile, Species species) {
		HistList CGHisList = getCG2NumHistList();
		PlotScatter plotScatter = getPlotScatter(CGHisList);
		plotScatter.setBg(Color.white);
		plotScatter.saveToFile(outFile, 1000, 1000);
	}

	/**
	 * 读取文件
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
	 * 记录AT或者CG的数量
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
			// 两个site中间空了很多，就是说中间有很多区域reads没有覆盖到
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
					// 考虑中间断开的情况，从Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析
					// gap前面的那个位点
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
					// 测试一下提取的序列有没有完全提取出来
					if (oneSeqInfoGapEdgeDown.getsiteThis() + 1 != oneSeqInfo
							.getsiteThis()) {
						logger.error("跳过："
								+ (oneSeqInfo.getsiteThis() - oneSeqInfoGapEdgeDown
										.getsiteThis()));
					}
				}
			}
		}
	}

	/**
	 * 考虑中间断开的情况，Gap的第一个位置起，顺序获得下一个OneSeqInfo，然后做分析 最后返回Gap末尾的那个OneSeqInfo
	 * 
	 * @param oneSeqInfoGapEdge
	 *            gap上边缘的那个site
	 * @param seqGap
	 *            gap序列
	 * @return 返回Gap的最后一位site
	 */
	private OneSeqInfo getNextSeqInfoInGap_And_Statistics(
			OneSeqInfo oneSeqInfoGapEdge, String oneSeq) {
		OneSeqInfo oneSeqInfoGapNext = oneSeqInfoGapEdge
				.getOneSeqInfoNext(oneSeq);
		return oneSeqInfoGapNext;
	}

	/** 统计连续AT，和连续CG出现的次数和覆盖度等 */
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
	 * 前一个是C当前一个是A，记录前面连续C的raws平均数的数量 形式例如key是4C_10，value是num数量例如3
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
	 * 记录insert和delection
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
	 * 记录连续碱基上面的indelreads数量
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
	 * 上下碱基不一样的时候的操作
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
		// TODO 检查一下
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
	 * 记录所有的文件的reads数覆盖情况
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
	 * 四舍五入
	 * 
	 * @param num
	 * @return
	 */
	public static int norm4To5(Double num) {
		num = num + 0.5;
		return num.intValue();
	}

}
