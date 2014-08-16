package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class RseQC {
	/**
	 * 用于计算在genebody的覆盖谱<br>
	 * 输入bam文件
	 * @author novelbio
	 *
	 */
	public static class GeneBodyCoverage implements IntCmdSoft {
		/**输入文件*/
		protected String inFile;
		/**输出文件前缀*/
		protected String outFile;
		/**参考基因组bed文件*/
		protected String bedFile;
		String exePath = "";
		public GeneBodyCoverage(String inFile,String outFile,String bedFile) {
			/**出入的bam或者sam文件*/
			this.inFile = inFile;
			/**输出文件前缀，可以加路径*/
			this.outFile = outFile;
			
			this.bedFile = bedFile;
			SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.rseqc);
			exePath = softWareInfo.getExePathRun();
		}
		
		
		public String getInFile() {
			return inFile;
		}

		protected String[] getInFileParam() {
			return new String[]{"-i",inFile};
		}
		
		public String getOutFile() {
			return outFile;
		}
		
		protected String[] getOutFileParam() {
			return new String[]{"-o",outFile};
		}

		public String getBedFile() {
			return bedFile;
		}
		
		protected String[] getBedFileParam() {
			return new String[]{"-r", bedFile};
		}

		protected List<String> getParamList() {
			List<String> lsListCmd = new ArrayList<String>();
			ArrayOperate.addArrayToList(lsListCmd, getInFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getOutFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getBedFileParam());;
			return lsListCmd;
		}
		
		public void run() {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			cmdOperate.run();
			if (!cmdOperate.isFinishedNormal()) {
				throw new ExceptionCmd("rseqcError: " + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
		}

		protected List<String> getLsCmd() {
			List<String> lsCmd = new ArrayList<String>();
			lsCmd.add(exePath + "geneBody_coverage.py");
			lsCmd.addAll(getParamList());
			return lsCmd;
		}
		@Override
		public List<String> getCmdExeStr() {
			List<String> lsCmd = new ArrayList<>();
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			lsCmd.add( cmdOperate.getCmdExeStr());
			return lsCmd;
		}
	}
	/**
	 * 用于计算在genebody的覆盖谱<br>
	 * 输入bigwig文件
	 * @author novelbio
	 *
	 */
	public static class GeneBodyCoverage2 extends GeneBodyCoverage {
		
		protected String imageType = "png";
		
		public GeneBodyCoverage2(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		public void setImageType(String imageType) {
			this.imageType = imageType;
		}
		
		private String[] getImageTypeParam() {
			return new String[]{"-t",imageType};
		}
		
		public String getImageType() {
			return imageType;
		}

		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "geneBody_coverage2.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getImageTypeParam());
			return lsListCmd;
		}

	}
	
	/**
	 * 用于计算两个成对的RNA读长的距离<br>
	 * 输入bam文件
	 * @author novelbio
	 *
	 */
	public static class InnerDistance extends GeneBodyCoverage {
		/** 图片下界  */
		int imageLowerBound = -250;
		/** 图片上界  */
		int imageUpBound = 250;
		/** 绘图的步长  */
		int imageStepLength = 5;
		
		public void setImageLowerBound(int imageLowerBound) {
			this.imageLowerBound = imageLowerBound;
		}
		
		public String[] getImageLowerBoundParamer() {
			return new String[]{"-l",imageLowerBound + ""};
		}
		
		public void setImageUpBound(int imageUpBound) {
			this.imageUpBound = imageUpBound;
		}
		
		public String[] getImageUpBoundParam() {
			return  new String[]{"-u",imageUpBound + ""};
		}
		
		public void setImageStepLength(int imageStepLength) {
			this.imageStepLength = imageStepLength;
		}
		
		public String[] getImageStepLength() {
			return  new String[]{"-s",imageStepLength + ""};
		}
		
		public InnerDistance(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "inner_distance.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getImageLowerBoundParamer());
			ArrayOperate.addArrayToList(lsListCmd, getImageUpBoundParam());
			ArrayOperate.addArrayToList(lsListCmd,getImageStepLength());
			return lsListCmd;
		}

	}
	/**
	 * 比较参考基因组（bed文件）与测序的结果（BAM/SAM文件）中junction的种类。可以在两个层面上进行，
	 * 剪切事件阶段以及剪切junction阶段。结果将会被分为三类：已知的，部分未知的，完全未知的。<br>
	 * 输入bam文件
	 * @author novelbio
	 *
	 */
	public static class JunctionAnnotation extends GeneBodyCoverage {
		/**最小内含子长度*/
		int intronLength = 50;
		
		public void setIntronLength(int intronLength) {
			this.intronLength = intronLength;
		}
		
		private String[] getIntronLengthParam() {
			return new String[]{"-m",intronLength + ""};
		}
		
		public JunctionAnnotation(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "junction_annotation.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getIntronLengthParam());
			return lsListCmd;
		}
	}
	/**
	 * 用于检测当前的测序深度是否可以用于可变剪切分析。<br>
	 * 输入bam文件
	 * @author novelbio
	 *
	 */
	public static class JunctionSaturation extends GeneBodyCoverage {
		/**设定采样百分比的下界。默认是5，可输入0~100的整数*/
		protected int sampleLowerBound = 5;
		
		/**设定采样百分比的上界。默认是100，可输入0~100的整数*/
		protected int sampleUpBound = 100;
		
		/**设定采样的步长。默认是5，可输入0~100的整数*/
		protected int sampleStepLength = 5;
		/**最小内含子长度，默认是50*/
		protected int intronLength = 50;
		/**设定能够认为是junction的最少读长数目，默认是1*/
		int leastReadNum = 1;
		
		public void setSampleLowerBound(int sampleLowerBound) {
			this.sampleLowerBound = sampleLowerBound;
		}
		
		public void setSampleUpBound(int sampleUpBound) {
			this.sampleUpBound = sampleUpBound;
		}
		
		public void setSampleStepLength(int sampleStepLength) {
			this.sampleStepLength = sampleStepLength;
		}
		
		public void setIntronLength(int intronLength) {
			this.intronLength = intronLength;
		}
		
		public void setLeastReadNum(int leastReadNum) {
			this.leastReadNum = leastReadNum;
		}
		
		protected String[] getSampleLowerBoundParam() {
			return new String[]{"-l",sampleLowerBound + ""};
		}
		
		protected String[]  getSampleUpBoundParam() {
			return new String[]{"-u",sampleUpBound + ""};
		}
		
		protected  String[] getSampleStepLengthParam() {
			return new String[]{"-s",sampleStepLength + ""};
		}
		
		protected  String[] getIntronLength() {
			return new String[]{"-m",intronLength + ""};
		}
		
		protected String[] getLeastReadNum() {
			return  new String[]{"-v",leastReadNum + ""};
		}
		
		@Override
		protected List<String> getParamList() {
			List<String> lsList =  super.getParamList();
			ArrayOperate.addArrayToList(lsList, getSampleLowerBoundParam());
			ArrayOperate.addArrayToList(lsList, getSampleUpBoundParam());
			ArrayOperate.addArrayToList(lsList, getSampleStepLengthParam());
			ArrayOperate.addArrayToList(lsList, getIntronLength());
			ArrayOperate.addArrayToList(lsList, getLeastReadNum());
			return lsList;
			
		}
		
		public JunctionSaturation(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "junction_saturation.py");
			lsListCmd.addAll(getParamList());
			return lsListCmd;
		}
	}
	/**用于计算读长的重复性<br>
	 * 输入bam文件
	 * @author zong0jie
	 *
	 */
	public static class ReadDuplication extends GeneBodyCoverage {
		/**读长重复次数的上限，仅用于绘图，默认是500*/
		protected int readsRepeatNum = 500;
		
		public ReadDuplication(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		/**读长重复次数的上限，仅用于绘图，默认是500*/
		public void setReadsRepeatNum(int readsRepeatNum) {
			this.readsRepeatNum = readsRepeatNum;
		}
		
		private String[] getReadsRepeatNumParam() {
			return new String[]{"-u",readsRepeatNum + ""};
		}
		
		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "read_duplication.py");
			ArrayOperate.addArrayToList(lsListCmd, getInFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getOutFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getReadsRepeatNumParam());
			return lsListCmd;
		}

	}
	
	/**用于评估样本大小对RPKM的影响*/
	public static class RPKMSaturation extends JunctionSaturation {
		/**
 * For pair-end RNA-seq, there are two different ways to strand reads:
  i) 1++,1--,2+-,2-+
     read1 mapped to '+' strand indicates parental gene on '+' strand
     read1 mapped to '-' strand indicates parental gene on '-' strand
     read2 mapped to '+' strand indicates parental gene on '-' strand
     read2 mapped to '-' strand indicates parental gene on '+' strand
  ii) 1+-,1-+,2++,2--
     read1 mapped to '+' strand indicates parental gene on '-' strand
     read1 mapped to '-' strand indicates parental gene on '+' strand
     read2 mapped to '+' strand indicates parental gene on '+' strand
     read2 mapped to '-' strand indicates parental gene on '-' strand
 * For single-end RNA-seq, there are two different ways to strand reads:
  i) ++,--
     read mapped to '+' strand indicates parental gene on '+' strand
     read mapped to '-' strand indicates parental gene on '-' strand
  ii) +-,-+
     read mapped to '+' strand indicates parental gene on '-' strand
     read mapped to '-' strand indicates parental gene on '+' strand	
		 */
		StrandSpecific strandSpecific = StrandSpecific.NONE;
		MapLibrary mapLibrary;
		
		/**
		 * -c Transcripts with RPKM smaller than this number will be
		 * ignored in visualization plot. default=0.01
		 */
		double cutoffValue = 0.01;
		
		public RPKMSaturation(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		/**
		 * -c Transcripts with RPKM smaller than this number will be
		 * ignored in visualization plot. default=0.01
		 */
		public void setCutoffValue(double cutoffValue) {
			this.cutoffValue = cutoffValue;
		}
		@Deprecated
		public void setIntronLength(int intronLen) {}
		
		public void run() {
			super.run();
			//不知道为什么，RSeQC在服务器上跑的时候，saturation的结果就会出不来
			if (!FileOperate.isFileExistAndBigThanSize(outFile + ".saturation.png", 0)) {
				String rscript = outFile + ".saturation.r";
				
				List<String> lsCmd = new ArrayList<>();
				lsCmd.add(PathDetail.getRscript());
				lsCmd.add(rscript.replace("\\", "/"));
				CmdOperate cmdOperate = new CmdOperate(lsCmd);
				cmdOperate.run();
				if (!cmdOperate.isFinishedNormal()) {
					throw new ExceptionCmd("rseqcError: " + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
				}
			}
		}
		
		public void setStrandSpecific(StrandSpecific strandSpecific) {
			this.strandSpecific = strandSpecific;
		}
		public void setMapLibrary(MapLibrary mapLibrary) {
			this.mapLibrary = mapLibrary;
		}
		
		private String[] getLineRuleParam() {
			String lineRule = null;
			if (strandSpecific == null || strandSpecific == StrandSpecific.NONE || strandSpecific == StrandSpecific.UNKNOWN) {
				return null;
			} else if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
				if (mapLibrary != MapLibrary.Unknown && mapLibrary != MapLibrary.SingleEnd) {
					lineRule = "1++,1--,2+-,2-+";
				} else {
					lineRule = "++,--";
				}
			} else if (strandSpecific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
				if (mapLibrary != MapLibrary.Unknown && mapLibrary != MapLibrary.SingleEnd) {
					lineRule = "1+-,1-+,2++,2--";
				} else {
					lineRule = "+-,-+";
				}
			} else {
				throw new ExceptionNullParam("error no param:" + strandSpecific.toString() + " " + mapLibrary.toString());
			}
			return new  String[]{"-d",lineRule};
		}
		
		private String[] getCutoffValue() {
			return new  String[]{"-c",cutoffValue+""};
		}
		
		protected List<String> getLsCmd() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add(exePath + "RPKM_saturation.py");
			ArrayOperate.addArrayToList(lsListCmd, getInFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getOutFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getBedFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleLowerBoundParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleUpBoundParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleStepLengthParam());
			ArrayOperate.addArrayToList(lsListCmd, getLineRuleParam());
			ArrayOperate.addArrayToList(lsListCmd, getCutoffValue());
			return lsListCmd;
		}
	}
	
}
