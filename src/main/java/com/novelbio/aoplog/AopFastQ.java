package com.novelbio.aoplog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import uk.ac.babraham.FastQC.Modules.BasicStats;
import uk.ac.babraham.FastQC.Modules.KmerContent;
import uk.ac.babraham.FastQC.Modules.NContent;
import uk.ac.babraham.FastQC.Modules.OverRepresentedSeqs;
import uk.ac.babraham.FastQC.Modules.PerBaseGCContent;
import uk.ac.babraham.FastQC.Modules.PerBaseQualityScores;
import uk.ac.babraham.FastQC.Modules.PerBaseSequenceContent;
import uk.ac.babraham.FastQC.Modules.PerSequenceGCContent;
import uk.ac.babraham.FastQC.Modules.PerSequenceQualityScores;
import uk.ac.babraham.FastQC.Modules.SequenceLengthDistribution;

import com.hg.doc.ep;
import com.novelbio.analysis.seq.fastq.FQrecordCopeInt;
import com.novelbio.analysis.seq.fastq.FastQC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.nbcgui.controlseq.CtrlFastQ;

/**
 * 给FastQ添加report相关的参数说明
 */
@Component
@Aspect
public class AopFastQ {
	private static Logger logger = Logger.getLogger(AopFastQ.class);

	/**
	 * 用来拦截CtrlFastQ的running方法
	 * @param CtrlFastQ
	 */
	@After("execution (* com.novelbio.nbcgui.controlseq.CtrlFastQ.running(..)) && target(ctrlFastQ)")
	public void fastQPoint(CtrlFastQ ctrlFastQ) {
		ReportBuilder fastQBuilder = new FastQBuilder(ctrlFastQ);
		if (fastQBuilder.buildExcels() && fastQBuilder.buildImages() && fastQBuilder.buildDescFile())
			return;
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

		public FastQBuilder(CtrlFastQ ctrlFastQ) {
			this.ctrlFastQ = ctrlFastQ;
			String outFilePrefix = ctrlFastQ.getOutFilePrefix();
			this.savePath = outFilePrefix.endsWith(FileOperate.getSepPath()) ? outFilePrefix : FileOperate.getParentPathName(outFilePrefix);
		}
		
		@Override
		public boolean buildExcels() {
			try {
				for (String key : ctrlFastQ.getMapCond2FastQCBefore().keySet()) {
					FastQC[] fastQCs = ctrlFastQ.getMapCond2FastQCBefore().get(key);
					readFastQC(fastQCs,key,true,ctrlFastQ.isQcBefore());
				}
				for (String key : ctrlFastQ.getMapCond2FastQCAfter().keySet()) {
					FastQC[] fastQCs = ctrlFastQ.getMapCond2FastQCAfter().get(key);
					readFastQC(fastQCs,key,false,ctrlFastQ.isQcAfter());
				}
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
		private void readFastQC(FastQC[] fastQCs, String key, boolean isBefore, boolean isQc) throws Exception{
			key += isBefore?"_before_":"_after_";
			for (int i = 0; i < fastQCs.length; i++) {
				FastQC fastQC = fastQCs[i];
				for (FQrecordCopeInt fQrecordCopeInt : fastQC.getLsModules()) {
					if (fQrecordCopeInt instanceof BasicStats) {
						Map<String, String> mapTable = ((BasicStats)fQrecordCopeInt).getResult();
						TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + key + "basicStats"+i+".xls", true);
						writeTable(txtWrite, mapTable);
						txtWrite.close();
						if (!isQc) {
							break;
						}
					} else {
						if (!isQc) continue;
						if (fQrecordCopeInt instanceof KmerContent) {
							Map<String, String> mapTable = ((KmerContent)fQrecordCopeInt).getResult();
							TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + key + "KmerContent"+i+".xls", true);
							writeTable(txtWrite, mapTable);
							txtWrite.close();
							mapPath2Image.put(savePath + key + "KmerContent"+i+".png", ((KmerContent)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof OverRepresentedSeqs) {
							Map<String, String> mapTable = ((OverRepresentedSeqs)fQrecordCopeInt).getResult();
							if (mapTable.size() > 0) {
								TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + key + "OverRepresentedSeqs"+i+".xls", true);
								writeTable(txtWrite, mapTable);
								txtWrite.close();
							}
						}
						else if (fQrecordCopeInt instanceof PerBaseQualityScores) {
							mapPath2Image.put(savePath + key + "QualityScore"+i+".png",((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof PerSequenceQualityScores) {
							mapPath2Image.put(savePath + key + "SequenceQuality"+i+".png",((PerSequenceQualityScores)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof PerBaseSequenceContent) {
							mapPath2Image.put(savePath + key + "BaseSequence"+i+".png",((PerBaseSequenceContent)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof PerBaseGCContent) {
							mapPath2Image.put(savePath + key + "BaseGCContent"+i+".png",((PerBaseGCContent)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof PerSequenceGCContent) {
							mapPath2Image.put(savePath + key + "SequenceGCContent"+i+".png",((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof NContent) {
							mapPath2Image.put(savePath + key + "nContent"+i+".png",((NContent)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof SequenceLengthDistribution) {
							mapPath2Image.put(savePath + key + "LengthDistribution"+i+".png",((SequenceLengthDistribution)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						else if (fQrecordCopeInt instanceof OverRepresentedSeqs) {
							mapPath2Image.put(savePath + key + "DuplicationLevel"+i+".png",((OverRepresentedSeqs)fQrecordCopeInt).getBufferedImage(1000, 1000));
						}
						
					}
					
				}
			}
		}

		@Override
		public boolean buildImages() {
			try {
				for (String path : mapPath2Image.keySet()) {
					File file = new File(path);
					ImageIO.write(mapPath2Image.get(path), "png", file);
				}
				// TODO image的说明文件在这里写
				// String descFile = FileOperate.changeFileSuffix(picName,
				// "_pic", "txt");
			} catch (Exception e) {
				logger.error("aopFastQ生成图片出错！");
				return false;
			}
			return true;
		}

		@Override
		public boolean buildDescFile() {
			TxtReadandWrite txtReadandWrite = getParamsTxt(savePath);
			//TODO 这里写参数
			txtReadandWrite.close();
			return true;
		}
		
		private void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
			for (String column : mapTable.keySet()) {
				txtWrite.writefileln(column + "\t" + mapTable.get(column));
			}
		}

	}

}
