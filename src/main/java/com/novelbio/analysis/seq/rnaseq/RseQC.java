package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;

public class RseQC {
	
	public static void main(String[] args) {
		String inFile = "/media/hdfs/nbCloud/public/test/RSeQC/Pairend_nonStrandSpecific_36mer_Human_hg19.sort.bam";
		String outFile = "/home/zong0jie/hdfs/GeneBodyCoverage";
		String  bedFile = "/media/hdfs/nbCloud/public/test/RSeQC/hg19_RefSeq.bed";
		GeneBodyCoverage geneBodyCoverage = new  GeneBodyCoverage(inFile, outFile, bedFile);
		geneBodyCoverage.run();
		InnerDistance innerDistance = new InnerDistance(inFile, outFile, bedFile);
		innerDistance.run();
		JunctionAnnotation junctionAnnotation = new JunctionAnnotation(inFile, outFile, bedFile);
		junctionAnnotation.run();
		JunctionSaturation junctionSaturation = new JunctionSaturation(inFile, outFile, bedFile);
		junctionSaturation.run();
		ReadDuplication readDuplication =  new ReadDuplication(inFile, outFile);
		readDuplication.run();
		
		RPKMSaturation rPKMsaSaturation = new RPKMSaturation(inFile, outFile, bedFile);
		rPKMsaSaturation.run();
	}
	
	String inFile;
	String outPah;
	String bedFile;
	
	
	
	/**
	 * 用于计算在genebody的覆盖谱
	 * @author novelbio
	 *
	 */
	public static  class GeneBodyCoverage {
		/**输入文件*/
		protected String inFile;
		/**输出文件前缀*/
		protected String outFile;
		/**参考基因组bed文件*/
		protected String bedFile;
		
		public GeneBodyCoverage(String inFile,String outFile,String bedFile) {
			/**出入的bam或者sam文件*/
			this.inFile = inFile;
			/**输出文件前缀，可以加路径*/
			this.outFile = outFile;
			
			this.bedFile = bedFile;
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
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("geneBody_coverage.py");
			lsListCmd.addAll(getParamList());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
	}
	/**
	 * 用于计算在genebody的覆盖谱
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
		
		@Override
		public void run() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("geneBody_coverage2.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getImageTypeParam());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
		
	}
	/**
	 *用于计算两个成对的RNA读长的距离
	 * @author novelbio
	 *
	 */
	public static class InnerDistance extends GeneBodyCoverage {
		/** 图片下界  */
		int imageLowerBound = -250;
		/** 图片上界  */
		int imageUpBound = 250;
		/** 绘图的步长  */
		int imageStepLenght = 5;
		
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
		
		public void setImageStepLenght(int imageStepLenght) {
			this.imageStepLenght = imageStepLenght;
		}
		
		public String[] getImageStepLenght() {
			return  new String[]{"-s",imageStepLenght + ""};
		}
		
		public InnerDistance(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		@Override
		public void run() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("inner_distance.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getImageLowerBoundParamer());
			ArrayOperate.addArrayToList(lsListCmd, getImageUpBoundParam());
			ArrayOperate.addArrayToList(lsListCmd,getImageStepLenght());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
		
	}
	/**
	 * 比较参考基因组（bed文件）与测序的结果（BAM/SAM文件）中junction的种类。可以在两个层面上进行，剪切事件阶段以及剪切junction阶段。结果将会被分为三类：已知的，部分未知的，完全未知的。
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
		
		@Override
		public void run() {			
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("junction_annotation.py");
			lsListCmd.addAll(getParamList());
			ArrayOperate.addArrayToList(lsListCmd, getIntronLengthParam());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
		
	}
	/**
	 * 用于检测当前的测序深度是否可以用于可变剪切分析。
	 * @author novelbio
	 *
	 */
	public static class JunctionSaturation extends GeneBodyCoverage {
		/**设定采样百分比的下界。默认是5，可输入0~100的整数*/
		protected int sampleLowerBound = 5;
		
		/**设定采样百分比的上界。默认是100，可输入0~100的整数*/
		protected int sampleUpBound = 100;
		
		/**设定采样的步长。默认是5，可输入0~100的整数*/
		protected int sampleStepLenght = 5;
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
		
		public void setSampleStepLenght(int sampleStepLenght) {
			this.sampleStepLenght = sampleStepLenght;
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
		
		protected  String[] getSampleStepLenghtParam() {
			return new String[]{"-s",sampleStepLenght + ""};
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
			ArrayOperate.addArrayToList(lsList, getSampleStepLenghtParam());
			ArrayOperate.addArrayToList(lsList, getIntronLength());
			ArrayOperate.addArrayToList(lsList, getLeastReadNum());
			return lsList;
			
		}
		
		public JunctionSaturation(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		@Override
		public void run() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("junction_saturation.py");
			lsListCmd.addAll(getParamList());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
	}
	/**用于计算读长的重复性。*/
	public static class ReadDuplication {
		/**输入文件*/
		protected String inFile;
		/**输出文件前缀*/
		protected String outFile;
		/**读长重复次数的上限，仅用于绘图，默认是500*/
		protected int readsRepeatNum = 500;
		
		public ReadDuplication(String inFile ,String outFile ) {
			this.inFile = inFile;
			this.outFile = outFile;
		}
		
		private String[] getInFileParam() {
			return new String[]{"-i",inFile};
		}
		
		private String[] getOutFileParam() {
			return new String[]{"-o",outFile};
		}
		
		private String[] getReadsRepeatNumParam() {
			return new String[]{"-u",readsRepeatNum + ""};
		}
		
		
		public void run() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("read_duplication.py");
			ArrayOperate.addArrayToList(lsListCmd, getInFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getOutFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getReadsRepeatNumParam());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
		
	}
	/**用于评估样本大小对RPKM的影响*/
	public static class RPKMSaturation  extends JunctionSaturation {
		/**链规则，例如：1++,1--,2+-,2-+ */
		String lineRule = "1++,1--,2+-,2-+";
		/** -c 指定RPKM的cutoff值。默认是0.01*/	
		double cutoffValue = 0.01;
		
		public RPKMSaturation(String inFile, String outFile, String bedFile) {
			super(inFile, outFile, bedFile);
		}
		
		private String[] getLineRuleParam() {
			return new  String[]{"-d",lineRule};
		}
		
		private String[] getCutoffValue() {
			return new  String[]{"-c",cutoffValue+""};
		}
		
		@Override
		public void run() {
			List<String> lsListCmd = new ArrayList<String>();
			lsListCmd.add("RPKM_saturation.py");
			ArrayOperate.addArrayToList(lsListCmd, getInFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getOutFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getBedFileParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleLowerBoundParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleUpBoundParam());
			ArrayOperate.addArrayToList(lsListCmd, getSampleStepLenghtParam());
			ArrayOperate.addArrayToList(lsListCmd, getLineRuleParam());
			ArrayOperate.addArrayToList(lsListCmd, getCutoffValue());
			CmdOperate cmdOperate = new CmdOperate(lsListCmd);
			cmdOperate.run();
		}
		
		
	}

}
