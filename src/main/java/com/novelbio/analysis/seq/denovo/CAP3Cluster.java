package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
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
	String outClusterFile;
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
	int minSeqLen = 200;
	String outMergedFile;
	String finalClusterResult;
	
	
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
	/** 输出文件夹路径，结尾可以不是 "/" */
	public void setOutDir(String outDir) {
		this.outDir = FileOperate.addSep(outDir);
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
	
	private String[] getFastaNeedCluster(String fastaNeedCluster) {
		return new String[]{fastaNeedCluster};
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
	private String[] getOutFile() {
		return new String[]{">", outClusterFile};
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

	public void run() {
		outClusterFile = outDir + "All.cap3.cluster.txt";
		finalClusterResult = outDir + "All-Unigene.final.fa";
		outMergedFile = getOutMergedFile();
		
		String outTransFile = outDir + "All-cap3-transcripts.fa";
		
		if (!FileOperate.isFileExistAndBigThanSize(outMergedFile, 0)) {
			CAP3Cluster.mergeTrinity(mapPrefix2TrinityFile, outMergedFile);
        }
		
		if (!FileOperate.isFileExistAndBigThanSize(outClusterFile, 0)) {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd(outMergedFile));
			cmdOperate.setRedirectInToTmp(true);
			cmdOperate.addCmdParamInput(outMergedFile);	
			cmdOperate.runWithExp("CAP3 error:");
		}
		
		Set<String> setGeneId = generateResultClusterFaAndGetFilteredGeneName();
				
		ContigId2TranId contigIDToTranID = new ContigId2TranId();
		contigIDToTranID.setCAP3File(outClusterFile, setGeneId);
		contigIDToTranID.setOutContigIDToTranIDFile(FileOperate.changeFileSuffix(outClusterFile, "_gene2trans", "list"));
		contigIDToTranID.generateCompareTab();
		Set<String> setTransId = contigIDToTranID.getSetTransId();
		generateTranscriptFa(setTransId, outMergedFile, outTransFile);
		generateStatisticsFile();
	}
	
	public String getOutTransFile() {
		return outDir + "All-cap3-transcripts.fa";
	}
	
	//统计聚类后序列N50信息
	private String generateStatisticsFile() {
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

	/**
	 * 把cap.contigs和cap.singlets合并为一个文件，同时返回过滤掉的序列名
	 * @return
	 */
	protected Set<String> generateResultClusterFaAndGetFilteredGeneName() {
		Set<String> setGeneId = new HashSet<>();
				
		String capResultContigsFile = outMergedFile + ".cap.contigs";
		String capResultSingletsFile = outMergedFile + ".cap.singlets";
		SeqFastaReader seqContigsReader = new SeqFastaReader(capResultContigsFile);
		SeqFastaReader seqSingletsReader = new SeqFastaReader(capResultSingletsFile);

		TxtReadandWrite txtWrite = new TxtReadandWrite(finalClusterResult, true);
		for (SeqFasta content : seqContigsReader.readlines()) {
			if (content.Length() >= minSeqLen) {
				txtWrite.writefileln(content.toStringNRfasta());
				setGeneId.add(content.getSeqName());
            }
		}
		seqSingletsReader.close();
		for (SeqFasta content : seqSingletsReader.readlines()) {
			if (content.Length() >= minSeqLen) {
				txtWrite.writefileln(content.toStringNRfasta());
				setGeneId.add(content.getSeqName());
            }
		}
		seqSingletsReader.close();
		txtWrite.close();
		return setGeneId;
	}
	
	/** 将所有聚类为转录本的序列放到一起
	 * 
	 * 	 ******************* Contig 1 ********************<br>
		c19_Smj-0h_g1_i1+<br>
		 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                   c12562_Sye-1h_g1_i1- is in c19_Smj-0h_g1_i1+<br>
		c14069_Sye-12h_g1_i1+<br>
		c10986_Smj-6h_g1_i2+<br>
		其中  c19_Smj-0h_g1_i1，c14069_Sye-12h_g1_i1，c10986_Smj-6h_g1_i2三个为无冗余转录本
	 * @param setTransId 最后聚类得到的无冗余转录本，其中一个基因有多个转录本
	 * @param allTransFile 聚类之前的全体转录本
	 * @param transFile 提取出来的无冗余转录本
	 */
	protected void generateTranscriptFa(Set<String> setTransId, String allTransFile, String transFile) {
		SeqFastaReader seqFastaReader = new SeqFastaReader(allTransFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(transFile, true);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			if (setTransId.contains(seqFasta.getSeqName())) {
				txtWrite.writefileln(seqFasta.toStringNRfasta());
			}
		}
		seqFastaReader.close();
		txtWrite.close();
	}

	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<String>();
		List<String> lsCmd = getLsCmd(getOutMergedFile());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}

	/** 合并文件，并将序列名后添加样本名，用SepSign.SEP_INFO 进行分割
	 * 注意，一个prefix必须只有一个file对应
	 * @param mapPrefix2TrinityFile 样本名 value: Trinity拼接好的文件
	 * @param outMergeResult 合并的输出文件名
	 * @return 返回输出的结果文件名
	 */
	protected static void mergeTrinity(Map<String, String> mapPrefix2TrinityFile, String outMergeResult) {
		if (mapPrefix2TrinityFile.isEmpty()) {
			return;
		} else if (mapPrefix2TrinityFile.size() == 1) {
			return;
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
	}
	
}

/**
 * 对Cap3的输入输出文件做一系列处理，合并输入文件，以及
 * 根据CAP3输出结果，提取Contig 对应 转录组本ID信息，写入文本 */
class ContigId2TranId {
	/** CAP3结果文件名称*/
	String cap3ResultFile;
	/** 需要保留的基因名，不在里面的基因一般是因为长度太短而被过滤掉 */
	Set<String> setGeneId = new HashSet<>();
	
	/** 存放Contigs ID 对应 Transcript ID 关系*/
	ArrayListMultimap<String, String> mapGeneId2LsTransId = ArrayListMultimap.create();
	/** 用于将结果输出的时候可以按照顺序进行 */
	Set<String> setKeys = new LinkedHashSet<>();
	Set<String> setTransId = new LinkedHashSet<>();
	
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
	public void setCAP3File(String excelCAP3ResultFileName, Set<String> setGeneId) {
		this.cap3ResultFile = excelCAP3ResultFileName;
		if (setGeneId != null) {
			this.setGeneId = setGeneId;
        }
	}
	
	/** 输出的 Contig ID 对应Transcript ID 列表结果文件名称*/
	public void setOutContigIDToTranIDFile(String outContigIDToTranIDFileName) {
		this.outContigIDToTranIDFileName = outContigIDToTranIDFileName;
	}
	
	public void generateCompareTab() {
		generateContigIDToTranID();
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
		boolean isStart = false;
		for (String content : txtContig.readlines()) {
			if (!isStart && !content.startsWith("*")) {
				continue;
			} else {
				isStart = true;
			}
			
			if (StringOperate.isRealNull(content)) 
				continue;
			
			if (content.startsWith("DETAILED DISPLAY OF CONTIGS")) {
				break;
			} else if (content.startsWith("*")) {
				geneId = content.replace("*", "").replace(" ", "");
			} else if (!content.startsWith(" ") && !content.startsWith("\t")) {//不以空行开头的
				String transId = null;
				try {
					transId = content.substring(0, content.length() - 1);

				} catch (Exception e) {
					transId = content.substring(0, content.length() - 1);

				}
				if (setGeneId.isEmpty() || setGeneId.contains(geneId)) {
					mapGeneId2LsTransId.put(geneId, transId);
					setKeys.add(geneId);
                }
			}	
		}
		txtContig.close();

		for (String geneSingleId : setGeneId) {
			if (!setKeys.contains(geneSingleId)) {
				mapGeneId2LsTransId.put(geneSingleId, geneSingleId);
            }
        }
	}
	
	/**用来输出Contig 对应 转录组本ID信息 */
	private void writeCompareTab() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outContigIDToTranIDFileName,true);  //
		for (String geneId : setKeys) {
			List<String> lsTransId = mapGeneId2LsTransId.get(geneId);
			for (String transId : lsTransId) {
				txtWrite.writefileln(geneId + "\t" + transId);
				setTransId.add(transId);
			}
		}
		txtWrite.close();
	}
	
	/** 返回聚类之后，去冗余的转录本 */
	public Set<String> getSetTransId() {
	    return setTransId;
    }
}