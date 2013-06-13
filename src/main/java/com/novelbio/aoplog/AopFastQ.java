package com.novelbio.aoplog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FQrecordCopeInt;
import com.novelbio.analysis.seq.fastq.FastQC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.nbcgui.controlseq.CtrlFastQ;

/**
 * 给FastQ添加report相关的参数说明
 */
@Component
@Aspect
public class AopFastQ {
	private static Logger logger = Logger.getLogger(AopFastQ.class);
	private static int smallPicSize = 1000;
	private static int bigPicSize = 1000;
	/**
	 * 用来拦截CtrlFastQ的running方法
	 * @param CtrlFastQ
	 */
	@After("execution (* com.novelbio.nbcgui.controlseq.CtrlFastQ.running(..)) && target(ctrlFastQ)")
	public void fastQPoint(CtrlFastQ ctrlFastQ) {
		ReportBuilder fastQBuilder = new FastQBuilder(ctrlFastQ);
		fastQBuilder.writeInfo();
		logger.error("aopFastQ生成报告图表参数出现异常！");
	}

	/**
	 * fastQ报告参数生成器
	 * 
	 * @author novelbio
	 * 
	 */
	private class FastQBuilder extends ReportBuilder {
		/** 结果的存放路径 */
		private String savePath;
		/** 拦截的对象 */
		private CtrlFastQ ctrlFastQ;
		/** 图片流集合 */
		private Map<String, BufferedImage> mapPath2Image = new LinkedHashMap<String, BufferedImage>();
		/** 所有basicStats表格数据集合 */
		private List<String> lsBaseTableLines = new ArrayList<String>();

		public FastQBuilder(CtrlFastQ ctrlFastQ) {
			this.ctrlFastQ = ctrlFastQ;
			String outFilePrefix = ctrlFastQ.getOutFilePrefix();
			this.savePath = outFilePrefix.endsWith(FileOperate.getSepPath()) ? outFilePrefix : FileOperate.getParentPathName(outFilePrefix);
			getParamsTxt(savePath);
		}
		
		@Override
		protected boolean buildExcels() {
			try {
				for (String key : ctrlFastQ.getMapCond2FastQCBefore().keySet()) {
					FastQC[] fastQCs = ctrlFastQ.getMapCond2FastQCBefore().get(key);
					readFastQC(fastQCs,key,true,ctrlFastQ.isQcBefore());
				}
				for (String key : ctrlFastQ.getMapCond2FastQCAfter().keySet()) {
					FastQC[] fastQCs = ctrlFastQ.getMapCond2FastQCAfter().get(key);
					readFastQC(fastQCs,key,false,ctrlFastQ.isQcAfter());
				}
				TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "basicStats_all.xls", true);
				txtWrite.writefileln(lsBaseTableLines);
				txtWrite.close();
				
				addParamInfo(Param.excelParam, "basicStats_all.xls");
			} catch (Exception e) {
				logger.error("aopFastQ生成excel出错！");
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		/**
		 * 读取fastQC并生成excel表格，并遍历图片
		 * @param fastQCs
		 * @param key
		 * @param isBefore
		 * @param isQc
		 * @throws Exception
		 */
		private void readFastQC(FastQC[] fastQCs, String prefix, boolean isBefore, boolean isQc) throws Exception{
			int sepPic = 20;//两张图合并起来后，中间的空隙
			BufferedImage[] qualityScoreImages = new BufferedImage[2];
			BufferedImage[] sequenceGCContentImages = new BufferedImage[2];
			for (int i = 0; i < fastQCs.length; i++) {
				String key = prefix;
				String reportKey = prefix;
				if (fastQCs.length == 1) {
					key += isBefore?"_BeforeFilter":"_AfterFilter";
				} else {
					key += isBefore?"_BeforeFilter_"+(i+1):"_AfterFilter_"+(i+1);
				}
				reportKey += isBefore?"_BeforeFilter":"_AfterFilter";
				FastQC fastQC = fastQCs[i];
				for (FQrecordCopeInt fQrecordCopeInt : fastQC.getLsModules()) {
					if (fQrecordCopeInt instanceof BasicStats) {
						Map<String, String> mapTable = ((BasicStats)fQrecordCopeInt).getResult();
						TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "BasicStats_" + key + ".xls", true);
						writeTable(txtWrite, mapTable);
						txtWrite.close();
						addToTotalTableList(key,mapTable);
						if (!isQc) {
							break;
						}
					} else {
						if (!isQc) continue;
						if (fQrecordCopeInt instanceof KmerContent) {
							Map<String, String> mapTable = ((KmerContent)fQrecordCopeInt).getResult();
							TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "KmerContent" + key +".xls", true);
							writeTable(txtWrite, mapTable);
							txtWrite.close();
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "KmerContent_" + key +".png", ((KmerContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof OverRepresentedSeqs) {
							Map<String, String> mapTable = ((OverRepresentedSeqs)fQrecordCopeInt).getResult();
							if (mapTable.size() > 0) {
								TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "OverRepresentedSeqs" + key +".xls", true);
								writeTable(txtWrite, mapTable);
								txtWrite.close();
							}
						}
						else if (fQrecordCopeInt instanceof PerBaseQualityScores) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "QualityScore_" + key +".png",((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
							super.picParam += "QualityScore_" + reportKey +".png;";
							if (fastQCs.length > 1) {
								qualityScoreImages[i] = ((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize);
								if ((i+1) == fastQCs.length) {
									mapPath2Image.put(savePath + "QualityScore_" + reportKey +".png",GraphicCope.combineBfImage(true, sepPic, qualityScoreImages));
								}
							}else {
								mapPath2Image.put(savePath + "QualityScore_" + reportKey +".png",((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize));
							}
						}
						else if (fQrecordCopeInt instanceof PerSequenceQualityScores) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "SequenceQuality_" + key +".png",((PerSequenceQualityScores)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerBaseSequenceContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "BaseSequence_" + key +".png",((PerBaseSequenceContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerBaseGCContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "BaseGCContent_" + key +".png",((PerBaseGCContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof DuplicationLevel) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "DuplicationLevel_" + key +".png",((DuplicationLevel)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerSequenceGCContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "SequenceGCContent_" + key +".png",((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
							super.picParam += "SequenceGCContent_" + reportKey +".png;";
							if (fastQCs.length > 1) {
								sequenceGCContentImages[i] = ((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize);
								if ((i+1) == fastQCs.length) {
									mapPath2Image.put(savePath + "SequenceGCContent_" + reportKey +".png",GraphicCope.combineBfImage(true, sepPic, sequenceGCContentImages));
								}
							}else {
								mapPath2Image.put(savePath + "SequenceGCContent_" + reportKey +".png",((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize));
							}
						}
						else if (fQrecordCopeInt instanceof NContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "nContent_" + key +".png",((NContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof SequenceLengthDistribution) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "LengthDistribution_" + key +".png",((SequenceLengthDistribution)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
					}
				}
			}
		}

		@Override
		public boolean buildImages() {
			try {
				for (String path : mapPath2Image.keySet()) {
					System.out.println(path);
					File file = new File(path);
					if (!FileOperate.isFileFoldExist(FileOperate.getParentPathName(path))) {
						FileOperate.createFolders(FileOperate.getParentPathName(path));
					}
					ImageIO.write(mapPath2Image.get(path), "png", file);
				}
				// TODO image的说明文件在这里写
				// String descFile = FileOperate.changeFileSuffix(picName,
				// "_pic", "txt");
			} catch (Exception e) {
				logger.error("aopFastQ生成图片出错！");
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected boolean fillDescFile() {
			return true;
		}
		
		private void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
			for (String column : mapTable.keySet()) {
				txtWrite.writefileln(column + "\t" + mapTable.get(column));
			}
		}
		
		private void addToTotalTableList(String key,Map<String, String> mapTable){
			if (lsBaseTableLines.size() == 0) {
				String allTitles = "SampleName";
				for (String title : mapTable.keySet()) {
					allTitles += "\t" + title;
				}
				lsBaseTableLines.add(allTitles);
			}
			for (String title : mapTable.keySet()) {
				key += "\t" + mapTable.get(title);
			}
			lsBaseTableLines.add(key);
		}

	}

}
