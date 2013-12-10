package com.novelbio.analysis.seq.mirna;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.SepSign;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class RNAmiranda {
	private static final Logger logger = Logger.getLogger(RNAmiranda.class);
	
	public static void main(String[] args) {
//		RNAmiranda rnAmiranda = new RNAmiranda();
//		rnAmiranda.setInputMiRNAseq("/media/winD/plant_miRNA_predict/AraDemo.fa");
//		rnAmiranda.setInputUTR3seq("/media/winD/plant_miRNA_predict/seq.fa");
//		rnAmiranda.setPredictResultFile("/media/winD/plant_miRNA_predict/result.fa");
//		rnAmiranda.mirnaPredict();
		String aaString = "fsefse" + new String(new char[7]) + "sese";
		System.out.println(aaString);
	}
	
	String exePath = "";
	int targetScore = 150;
	int targetEnergy = -15;
	
	String inputUTR3seq;
	String inputMiRNAseq;
	String predictResultFile;
	
	/** 用来读取结果文件的 */
	TxtReadandWrite txtMirandaRead;
	
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.exePath = "";
		else
			this.exePath = FileOperate.addSep(exePath);
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		this.inputMiRNAseq = inputMiRNAseq;
	}
	public void setInputUTR3seq(String inputUTR3seq) {
		this.inputUTR3seq = inputUTR3seq;
	}
	public void setPredictResultFile(String predictResultFile) {
		this.predictResultFile = predictResultFile;
	}
	/** 默认150 */
	public void setTargetScore(int targetScore) {
		this.targetScore = targetScore;
	}

	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		this.targetEnergy = -Math.abs(targetEnergy);
	}

	public String mirnaPredict() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("miranda error:" + cmdOperate.getCmdExeStrReal());
		}
		return predictResultFile;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "miranda");
		lsCmd.add(inputMiRNAseq);
		lsCmd.add(inputUTR3seq);
		ArrayOperate.addArrayToList(lsCmd, getTargetScore());
		ArrayOperate.addArrayToList(lsCmd, getTargetEnergy());
		ArrayOperate.addArrayToList(lsCmd, getPredictResult());
		return lsCmd;
	}
	
	private String[] getTargetScore() {
		return new String[]{"-sc", targetScore + ""};
	}
	private String[] getTargetEnergy() {
		return new String[]{"-en", targetEnergy + ""};
	}
	private String[] getPredictResult() {
		return new String[]{"-out", predictResultFile};
	}
	
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public Iterable<MirandaPair> readPerlines(String fileName) {
		if (txtMirandaRead != null) txtMirandaRead.close();
		txtMirandaRead = new TxtReadandWrite(fileName);
		final BufferedReader bufread =  txtMirandaRead.readfile(); 
		final List<String> lsInfo = new ArrayList<>();
		return new Iterable<MirandaPair>() {
			public Iterator<MirandaPair> iterator() {
				return new Iterator<MirandaPair>() {
					MirandaPair mirandaPair = getLine();
					public boolean hasNext() {
						return mirandaPair != null;
					}
					public MirandaPair next() {
						MirandaPair retval = mirandaPair;
						mirandaPair = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					MirandaPair getLine() {
						MirandaPair mirandaPair = null;
						try {
							mirandaPair = getLineMiranda();
						} catch (Exception e) {
							e.printStackTrace();
							try { bufread.close(); } catch (IOException e1) { }
							return null;
						}
						if (mirandaPair == null) {
							try { bufread.close(); } catch (IOException e) { }
						}
						return mirandaPair;
					}
					
					MirandaPair getLineMiranda() throws IOException {
						MirandaPair mirandaPair = null;
						String content;
						boolean isHaveValue = false;
						while ((content = bufread.readLine()) != null) {
							content = content.trim();
							if (content.startsWith("Performing Scan:")) {
								lsInfo.clear();
							} else if (content.contains("No Hits Found above Threshold")) {
								isHaveValue = false;
							} else if (content.startsWith("Query:")) {
								isHaveValue = true;
							} else if (content.equals("Complete")) {
								if (isHaveValue) {
									mirandaPair = new MirandaPair(lsInfo);
									lsInfo.clear();
									break;
								}
							}
							lsInfo.add(content);
						}
						return mirandaPair;
					}
				};
			}
		};
	}
	
	/** 读完文件后关闭掉 */
	public void close() {
		if (txtMirandaRead != null) {
			txtMirandaRead.close();
		}
	}
	
	
	/**
	 * Read Sequence:AT1G01020.2 (1085 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01020.2
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01030.1 (1905 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01030.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01040.1 (6251 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01040.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

   Forward:	Score: 161.000000  Q:2 to 23  R:5393 to 5422 Align Len (24) (66.67%) (75.00%)

   Query:    3' tttacCTCCTAGTT---CAACCCAAACCCa 5'
                     |:|| |:||    || |||||||| 
   Ref:      5' aaaccGGGGTTTAACTCTTTTGGTTTGGGa 3'

   Energy:  -18.629999 kCal/Mol

Scores for this hit:
>ath-miR156c	AT1G01040.1	161.00	-18.63	2 23	5393 5422	24	66.67%	75.00%

Score for this Scan:
Seq1,Seq2,Tot Score,Tot Energy,Max Score,Max Energy,Strand,Len1,Len2,Positions
>>ath-miR156c	AT1G01040.1	161.00	-18.63	161.00	-18.63	39	27	6251	 5393
Complete

Read Sequence:AT1G01040.2 (5877 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01040.2
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

   Forward:	Score: 161.000000  Q:2 to 23  R:5126 to 5155 Align Len (24) (66.67%) (75.00%)

   Query:    3' tttacCTCCTAGTT---CAACCCAAACCCa 5'
                     |:|| |:||    || |||||||| 
   Ref:      5' aaaccGGGGTTTAACTCTTTTGGTTTGGGa 3'

   Energy:  -18.629999 kCal/Mol

Scores for this hit:
>ath-miR156c	AT1G01040.2	161.00	-18.63	2 23	5126 5155	24	66.67%	75.00%

Score for this Scan:
Seq1,Seq2,Tot Score,Tot Energy,Max Score,Max Energy,Strand,Len1,Len2,Positions
>>ath-miR156c	AT1G01040.2	161.00	-18.63	161.00	-18.63	40	27	5877	 5126
Complete

Read Sequence:AT1G01046.1 (207 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01046.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01050.1 (976 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01050.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete
 */
	
	/** 一对序列的比较
	 * 一对序列比较中会含有多个match
	 * @author zong0jie
	 *
	 */
	public static class MirandaPair {
		String qName;
		String sName;
		/** 具体的多对match */
		Map<String, MirandaUnit> mapKey2Unit = new HashMap<>();
		/** 自由能最大的一个key，可以用其来提取具体的Unit */
		String maxEnergyKey;
		
		public MirandaPair(String mirandaStrUnit) {
			String[] ss = mirandaStrUnit.split("\n");
			List<String> lsMirandaUnit = new ArrayList<>();
			for (String string : ss) {
				lsMirandaUnit.add(string);
			}
			initial(lsMirandaUnit);
		}
		
		public MirandaPair(List<String> lsMirandaUnit) {
			initial(lsMirandaUnit);
		}
		
		private void initial(List<String> lsMirandaUnit) {
			boolean getAlign = false;
			MirandaUnit mirandaUnit = new MirandaUnit();
			for (String content : lsMirandaUnit) {
				content = content.trim();
				if (content.startsWith("Forward:")) {
					mirandaUnit = new MirandaUnit();
				} else if (content.startsWith("Query:  ")) {
					mirandaUnit.setQseq(content);
					getAlign = true;
				} else if (content.startsWith("Ref:  ")) {
					mirandaUnit.setSseq(content);
				} else if (getAlign) {
					mirandaUnit.setAlign(content);
					getAlign = false;
				} else if (content.startsWith(">") && !content.startsWith(">>")) {
					mirandaUnit.setLocSocreEnergy(content);
					mapKey2Unit.put(mirandaUnit.getKey(), mirandaUnit);
					setMaxEnergyKey(mirandaUnit);
					mirandaUnit = new MirandaUnit();
				}
			}
			mirandaUnit = null;
		}

		private void setMaxEnergyKey(MirandaUnit mirandaUnit) {
			if (maxEnergyKey == null) {
				maxEnergyKey = mirandaUnit.getKey();
			} else if (mirandaUnit.energy < mapKey2Unit.get(maxEnergyKey).energy) {
				maxEnergyKey = mirandaUnit.getKey();
			}
		}
		
		/** 获得最大能量的一对结果 */
		public MirandaUnit getMirandaUnitMaxEnergy() {
			return mapKey2Unit.get(maxEnergyKey);
		}
	}
	
	/** 具体的一对pair */
	public static class MirandaUnit {
		String qName;
		String sName;
		int alignLen;
		int startQ;
		int endQ;
		int startS;
		int endS;
		
		double score;
		double energy;
		
		/** 小写表示前缀或后缀，都不计入start和end中
		 * 是反向序列，因为方向是从3‘-5’的
		 */
		String qSeq;
		/** 小写表示前缀或后缀，都不计入start和end中 */
		String sSeq;
		/** 连配的竖线 */
		String align;
		int startSpaceNum;
		
		/** 获得3-5的queryseq */
		private void setQseq(String qSeqContent) {
			qSeq = qSeqContent.replace("Query:", "").replace("3'", "").replace("5'", "").trim();
		}
		/** 获得5-3的queryseq */
		private void setSseq(String sSeqContent) {
			sSeq = sSeqContent.replace("Ref:", "").replace("3'", "").replace("5'", "").trim();
		}
		/** 获得5-3的queryseq */
		private void setAlign(String alignContent) {
			align = alignContent.trim();
			char[] seqQ = qSeq.toCharArray();
			startSpaceNum = 0;
			for (char c : seqQ) {
				if ((int)c >= 97 ) {
					startSpaceNum++;
				} else if ((int)c < 97) {
					break;
				}
			}
		}
		
		/** 设定seq的位置，打分，能量<br>
		 * 从以下文字获取<br>
		 * >ath-miR156c	AT1G01010.1	161.00	-18.63	2 23	264 293	24	66.67%	75.00%
		 * @param content
		 */
		private void setLocSocreEnergy(String content) {
			content = content.replace(">", "");
			String[] sep = content.split("\t");
			qName = sep[0];
			sName = sep[1];
			score = Double.parseDouble(sep[2]);
			energy = Double.parseDouble(sep[3]);
			String[] startendQ = sep[4].split(" ");
			String[] startendS = sep[5].split(" ");
			startQ = Integer.parseInt(startendQ[0]);
			endQ = Integer.parseInt(startendQ[1]);
			startS = Integer.parseInt(startendS[0]);
			endS = Integer.parseInt(startendS[1]);
			alignLen = Integer.parseInt(sep[6]);
		}
		
		private String getKey() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(qName);
			stringBuilder.append(SepSign.SEP_ID);
			stringBuilder.append(sName);
			stringBuilder.append(SepSign.SEP_ID);
			stringBuilder.append(startQ);
			stringBuilder.append(SepSign.SEP_ID);
			stringBuilder.append(endQ);
			stringBuilder.append(SepSign.SEP_ID);
			stringBuilder.append(startS);
			stringBuilder.append(SepSign.SEP_ID);
			stringBuilder.append(endS);
			return stringBuilder.toString();
		}
		
		/** 返回连配的结果*/
		public String getAlign() {
			int lenQname = qName.length();
			int lenSname = sName.length();
			//名字长度不同用空格补齐
			int QminusS = lenQname - lenSname;
			String space = getSpace(QminusS);
			//设定名字
			String qNameDesply;
			String sNameDesply;
			if (QminusS > 0) {
				qNameDesply = qName + ":";
				sNameDesply = sName + new String(space) + ":";
			} else {
				qNameDesply = qName + new String(space) + ":";
				sNameDesply = sName + ":";
			}
			//设定alignment
			int alingSpaceNum = startSpaceNum + Math.max(lenQname, lenSname) + 1;
			String alignDesply = getSpace(alingSpaceNum) + align;
			return qNameDesply + qSeq + TxtReadandWrite.ENTER_LINUX + alignDesply + TxtReadandWrite.ENTER_LINUX + sNameDesply + sSeq;
		}
		
		/**
		 * 返回若干空格
		 * @param spaceNum 无所谓正负号，会返回该绝对值个数的空格<br>
		 * 譬如 3 和 -3 都返回 3个空格
		 * @return
		 */
		private String getSpace(int spaceNum) {
			if (spaceNum == 0) {
				return "";
			}
			char[] space = new char[Math.abs(spaceNum)];
			for (int i = 0; i < space.length; i++) {
				space[i] = ' ';
			}
			return new String(space);
		}
	}
	

	
}
