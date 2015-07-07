package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.SepSign;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.listOperate.HistList;

/**
 * simple script : cap3 trinity.fa -f 20 -o 100 -p 90 -z 3 > trinity.cap3.result.txt
 * @author bll
 *
 */

public class CAP3Cluster implements IntCmdSoft {
	private static int OVERLAPGAPLEN = 20;
	private static int OVERLAPLENCUTFF = 40;
	private static int OVERLAPIDEPERCUTFF = 90;
	private static int READSSUPPORTNUM = 3;
	String exePath = "";	
	
	/** 需要聚类的序列文件，fasta格式文件
	 * key: 样本名
	 * value: 文件
	 */
	Map<String, String> mapPrefix2TrinityFile;

	/** 输出文件路径及名称 */
	String outFile;
	String outDir;
	/** overlap区域允许的最大gap长度，默认 20 */
	int overlapGapLen = OVERLAPGAPLEN;
	/** overlap区域长度阈值，默认 40 */
	int overlapLenCutff = OVERLAPLENCUTFF;
	/** overlap区域一致性比例，默认 90 */
	int overlapIdePerCutff = OVERLAPIDEPERCUTFF;
	/** clip 位置reads支持数，默认 3 */
	int readsSupportNum = READSSUPPORTNUM;
	/** 聚类后序列结果序列长度阈值，也就是说，保留序列长度大于此阈值的序列*/
	int minSeqLen;
	String outMergedFile;
	String finalClusterResult = outDir + "All-Unigene.final.fa";
	
	
	public CAP3Cluster() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cap3);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	
	/**
	 * 设定需要聚类的序列文件
	 * key: 样本名
	 * value: 文件
	 */
	public void setFastaNeedCluster(Map<String, String> mapPrefix2TrinityFile) {
		for (String fileFasta : mapPrefix2TrinityFile.values()) {
			FileOperate.checkFileExistAndBigThanSize(fileFasta, 0);
		}
		this.mapPrefix2TrinityFile = mapPrefix2TrinityFile;
	}
	public void setMinSeqLen(int minSeqLen) {
		this.minSeqLen = minSeqLen;
	}
	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}
	/** 输出文件名 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	/** 设定overlap区域允许的最大gap长度，默认20  */
	public void setOverlapGapLen(int overlapGapLen) {
		if (overlapGapLen > 0) {
			this.overlapGapLen = overlapGapLen;
		}
	}
	/** 设定overlap区域长度  */
	public void setOverlapLenCutff(int overlapLenCutff) {
		if (overlapLenCutff > 0) {
			this.overlapLenCutff = overlapLenCutff;
		}
	}
	/** 设定overlap一致性比例  */
	public void setOverlapIdePerCutff(int overlapIdePerCutff) {
		if (overlapIdePerCutff > 0) {
			this.overlapIdePerCutff = overlapIdePerCutff;
		}
	}
	/** 设定clip 位置reads支持数  */
	public void setReadsSupportNum(int readsSupportNum) {
		if (readsSupportNum > 0) {
			this.readsSupportNum = readsSupportNum;
		}
	}

	public void run() {
		outFile = outDir + "All-Trinity.cap3.result.txt";
		String clusterFinalResultFa = getResultClusterFa();
		if (!FileOperate.isFileExistAndBigThanSize(clusterFinalResultFa, 0)) {
			outMergedFile = CAP3Cluster.mergeTrinity(mapPrefix2TrinityFile, getOutMergedFile());	
			CmdOperate cmdOperate = new CmdOperate(getLsCmd(outMergedFile));
			cmdOperate.setRedirectInToTmp(true);
			cmdOperate.addCmdParamInput(outMergedFile);	
			cmdOperate.runWithExp("CAP3 error:");
			ContigId2TranId contigIDToTranID = new ContigId2TranId();
			contigIDToTranID.setCAP3ResultFile(outFile);
			contigIDToTranID.setCAP3ResultSingletsFile(outMergedFile.concat(".cap.singlets"));
			contigIDToTranID.setOutContigIDToTranIDFile(FileOperate.changeFileSuffix(outFile, "_GeneId2AllTransId", "txt"));
			contigIDToTranID.generateCompareTab();
		}
		finalClusterResult = filterFaLength(clusterFinalResultFa);
		getStatisticsFile();
		getGeneIDToTranID();
	}
	private void getGeneIDToTranID() {
		// TODO Auto-generated method stub
		String geneToTranID = outDir + "GeneIDToTranIDList.txt";
		TxtReadandWrite txtSinglets = new TxtReadandWrite(finalClusterResult);
		TxtReadandWrite txtWrite = new TxtReadandWrite(geneToTranID, true);
		for (String content : txtSinglets.readlines()) {
			if (content.startsWith(">")) {
				String geneId = content.substring(1);
				txtWrite.writefileln(geneId + "\t" + geneId);
			}
		}
		txtSinglets.close();
		txtWrite.close();		
	}

	//统计聚类后序列N50信息
	private String getStatisticsFile() {
		String statisticsFile = outDir + "statistics.xls";
		N50AndSeqLen n50AndSeqLen = new N50AndSeqLen(finalClusterResult);
		n50AndSeqLen.doStatistics();
		//TODＯ 这里需要自动化生成图表
		HistList histList = n50AndSeqLen.gethListLength();
		List<String[]> lsN50Ninfo = n50AndSeqLen.getLsNinfo();
		TxtReadandWrite txtWrite = new TxtReadandWrite(statisticsFile, true);
		for(String[] content: lsN50Ninfo ) {
			txtWrite.writefileln(content);
		}
		txtWrite.writefileln("Contig Mean Length:\t" + n50AndSeqLen.getLenAvg());
		txtWrite.close();
		return statisticsFile;
	}
	
	private String getOutMergedFile() {
		String outMergedFile = outDir + "merged.fa";
		if (mapPrefix2TrinityFile.size() == 1) {
			outMergedFile = mapPrefix2TrinityFile.values().iterator().next();
		}
		return outMergedFile;
	}
	private String getOutDir() {
		return outDir;
	}
	private List<String> getLsCmd(String fastaNeedCluster) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "cap3");
		ArrayOperate.addArrayToList(lsCmd, getFastaNeedCluster(fastaNeedCluster));
		ArrayOperate.addArrayToList(lsCmd, getOverlapGapLen());
		ArrayOperate.addArrayToList(lsCmd, getOverlapLenCutff());
		ArrayOperate.addArrayToList(lsCmd, getOverlapIdePerCutff());
		ArrayOperate.addArrayToList(lsCmd, getReadsSupportNum());
		ArrayOperate.addArrayToList(lsCmd, getOutFile());
		return lsCmd;
	}
	private String[] getFastaNeedCluster(String fastaNeedCluster) {
		return new String[]{fastaNeedCluster};
	}
	private String[] getOutFile() {
		return new String[]{">", outFile};
	}
	private String[] getOverlapGapLen() {
		return new String[]{"-f", overlapGapLen + ""};
	}
	private String[] getOverlapLenCutff() {
		return new String[] {"-o", overlapLenCutff + ""};
	}
	private String[] getOverlapIdePerCutff() {
		return new String[] {"-p", overlapIdePerCutff + ""};
	}
	private String[] getReadsSupportNum() {
		return new String[] {"-z", readsSupportNum + ""};
	}

	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<String>();
		List<String> lsCmd = getLsCmd(getOutMergedFile());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}

	/** 返回聚类好的文件 */
	public String getResultClusterFa() {
		String clusterFinalResultFa = getOutMergedFile().concat(".cluster.final.fa");
		if (FileOperate.isFileExistAndBigThanSize(clusterFinalResultFa, 0)) {
			return clusterFinalResultFa;
		}
		String capResultContigsFile = getOutMergedFile().concat(".cap.contigs");
		String capResultSingletsFile = getOutMergedFile().concat(".cap.singlets");
		TxtReadandWrite txtContigsRead = new TxtReadandWrite(capResultContigsFile);
		TxtReadandWrite txtSingletsRead = new TxtReadandWrite(capResultSingletsFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(clusterFinalResultFa, true);
		for (String content : txtContigsRead.readlines()) {
			txtWrite.writefileln(content);
		}
		txtContigsRead.close();
		for (String content : txtSingletsRead.readlines()) {
			txtWrite.writefileln(content);
		}
		txtSingletsRead.close();
		txtWrite.close();
		return clusterFinalResultFa;
	}
	
	//TODO 返回转录本的fasta文件
	public String getResultTranscriptFasta() {
		String transcriptFaFile = outMergedFile.concat("transcript.fa");
		SeqHash seqHash = new SeqHash(outMergedFile);
		ContigId2TranId contigId2TranId = new ContigId2TranId();
		TxtReadandWrite txtContigToTranIDRead = new TxtReadandWrite(contigId2TranId.outContigIDToTranIDFileName);
		TxtReadandWrite txtWrite = new TxtReadandWrite(transcriptFaFile, true);
		for (String content : txtContigToTranIDRead.readlines()) {
			SeqFasta seqnameFasta =  seqHash.getSeq(content.split(" ")[1]);
			txtWrite.writefileln(seqnameFasta.toString());
		}
		txtContigToTranIDRead.close();
		txtWrite.close();
		return transcriptFaFile;
	}

	public String filterFaLength(String inputFa) {
		SeqHash seqHash = new SeqHash(inputFa);
		String filterResult = outDir + "All-Unigene.final.fa";
		TxtReadandWrite txtWrite = new TxtReadandWrite(filterResult, true);
		List<String> lsSeqName = seqHash.getLsSeqName();
		for (String seqName : lsSeqName) {
			SeqFasta seqnameFasta =  seqHash.getSeq(seqName);
			if (seqnameFasta.Length() >= minSeqLen) {
				txtWrite.writefileln(seqnameFasta.toStringNRfasta(60));
			}
		}
		txtWrite.close();
		return filterResult;
	}

/**
 * 对Cap3的输入输出文件做一系列处理，合并输入文件，以及
 * 根据CAP3输出结果，提取Contig 对应 转录组本ID信息，存在HashMap中 */
public class ContigId2TranId {
	/** CAP3结果文件名称*/
	String cap3ResultFile;
	/** CAP3 输出的Singlets 文件名称*/
	String cap3ResultSingletsFile;
	/** 存放Contigs ID 对应 Transcript ID 关系*/
	ArrayListMultimap<String, String> mapGeneId2LsTransId = ArrayListMultimap.create();
	
	/** 待输出的 Contig ID 对应Transcript ID 列表结果文件名称*/
	String outContigIDToTranIDFileName;
	
	/** cap3产生的对照表，格式类似<br>
	 ******************* Contig 1 ********************<br>
		c19_Smj-0h_g1_i1+<br>
		 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                   c12562_Sye-1h_g1_i1- is in c19_Smj-0h_g1_i1+<br>
		c14069_Sye-12h_g1_i1+<br>
		c10986_Smj-6h_g1_i2+<br>
		******************* Contig 2 ********************<br>
		c21_Smj-0h_g1_i1+<br>
		c9935_Smj-6h_g1_i1-<br>
		c17938_Sye-12h_g1_i1+<br>
	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                 c17938_Sye-12h_g1_i2+ is in c17938_Sye-12h_g1_i1+<br>
	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                  c11380_Sye-1h_g1_i1- is in c17938_Sye-12h_g1_i1+<br>
	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                  c9074_Smj-0h_g1_i1+ is in c17938_Sye-12h_g1_i1+<br>
	 */
	public void setCAP3ResultFile(String excelCAP3ResultFileName) {
		this.cap3ResultFile = excelCAP3ResultFileName;
	}
	/** CAP3 输出的Singlets 文件，是个聚类好的fasta文件 */
	public void setCAP3ResultSingletsFile(String excelCAP3ResultSingletsFileName) {
		this.cap3ResultSingletsFile = excelCAP3ResultSingletsFileName;
	}
	/** 输出的 Contig ID 对应Transcript ID 列表结果文件名称*/
	public void setOutContigIDToTranIDFile(String outContigIDToTranIDFileName) {
		this.outContigIDToTranIDFileName = outContigIDToTranIDFileName;
	}
	
	public void generateCompareTab() {
		generateContigIDToTranID();
		generateSingletsIDToTranID(cap3ResultSingletsFile);
		writeCompareTab();
	}
	
	/** 读取cap3产生的对照表，格式类似<br>
	 ******************* Contig 1 ********************<br>
	c19_Smj-0h_g1_i1+<br>
	 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                   c12562_Sye-1h_g1_i1- is in c19_Smj-0h_g1_i1+<br>
	c14069_Sye-12h_g1_i1+<br>
	c10986_Smj-6h_g1_i2+<br>
	******************* Contig 2 ********************<br>
	c21_Smj-0h_g1_i1+<br>
	c9935_Smj-6h_g1_i1-<br>
	c17938_Sye-12h_g1_i1+<br>
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                 c17938_Sye-12h_g1_i2+ is in c17938_Sye-12h_g1_i1+<br>
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                  c11380_Sye-1h_g1_i1- is in c17938_Sye-12h_g1_i1+<br>
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                  c9074_Smj-0h_g1_i1+ is in c17938_Sye-12h_g1_i1+<br>
	 */
	private void generateContigIDToTranID() {
		TxtReadandWrite txtContig = new TxtReadandWrite(cap3ResultFile);
		String geneId = null;
		for (String content : txtContig.readlines()) {
			if (content.startsWith("DETAILED DISPLAY OF CONTIGS")) {
				break;
			} else if (content.startsWith("*")) {
				geneId = content.replace("*", "").replace(" ", "");
			} else if (content.startsWith("c")) {
				String transId = content.substring(0, content.length() - 1);
				mapGeneId2LsTransId.put(geneId, transId);
			}	
		}
		txtContig.close();
	}

	/**提取Singlets文件中的序列ID信息 */
	private void generateSingletsIDToTranID(String cap3ResultSingletsFile) {
		TxtReadandWrite txtSinglets = new TxtReadandWrite(cap3ResultSingletsFile);
		for (String content : txtSinglets.readlines()) {
			if (content.startsWith(">")) {
				String geneId = content.substring(1);
				if (!mapGeneId2LsTransId.containsKey(geneId)) {
					mapGeneId2LsTransId.put(geneId, geneId);
				}
			}
		}
		txtSinglets.close();
	}	
	
	
	/**用来输出Contig 对应 转录组本ID信息 */
	private void writeCompareTab() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outContigIDToTranIDFileName,true);  //
		for (String geneId : mapGeneId2LsTransId.keySet()) {
			List<String> lsTransId = mapGeneId2LsTransId.get(geneId);
			for (String transId : lsTransId) {
				txtWrite.writefileln(geneId + "\t" + transId);
			}
		}
		txtWrite.close();
	}
}
	/** 合并文件，并将序列名后添加样本名，用SepSign.SEP_INFO 进行分割
	 * 注意，一个prefix必须只有一个file对应
	 * @param mapPrefix2TrinityFile 样本名 value: Trinity拼接好的文件
	 * @param outMergeResult 合并的输出文件名
	 * @return 返回输出的结果文件名
	 */
	protected static String mergeTrinity(Map<String, String> mapPrefix2TrinityFile, String outMergeResult) {
		if (mapPrefix2TrinityFile.size() == 0) {
			return null;
		} else if (mapPrefix2TrinityFile.size() == 1) {
			return outMergeResult;
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(outMergeResult, true);
		for (String prefix : mapPrefix2TrinityFile.keySet()) {
			String trinityFa = mapPrefix2TrinityFile.get(prefix);
			TxtReadandWrite txtRead = new TxtReadandWrite(trinityFa);
			for (String content : txtRead.readlines()) {
				if (content.startsWith(">")) {
					content = content.split(" ")[0] + SepSign.SEP_INFO + prefix;
				}
				txtWrite.writefileln(content);
			}
			txtRead.close();
		}
		txtWrite.close();
		return outMergeResult;
	}
}