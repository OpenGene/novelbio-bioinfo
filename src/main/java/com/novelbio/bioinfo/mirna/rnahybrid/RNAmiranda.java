package com.novelbio.bioinfo.mirna.rnahybrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.IntCmdSoft;
import com.novelbio.generalconf.TitleFormatNBC;

public class RNAmiranda implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(RNAmiranda.class);
	
	public static void main(String[] args) {
		RNAmiranda rnAmiranda = new RNAmiranda();
		rnAmiranda.setMiRNAseq("/media/winD/plant_miRNA_predict/AraDemo.fa");
		rnAmiranda.setUtr3File("/media/winD/plant_miRNA_predict/seq.fa");
		rnAmiranda.setPredictResultFile("/media/winD/plant_miRNA_predict/miranda.out");
		rnAmiranda.mirnaPredict();
	}
	
	String exePath = "";
	int targetScore = 150;
	int targetEnergy = -15;
	
	String inputUTR3seq;
	/** 3-->5 方向的序列 */
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
	public void setMiRNAseq(String inputMiRNAseq) {
		this.inputMiRNAseq = inputMiRNAseq;
	}
	public void setUtr3File(String inputUTR3seq) {
		this.inputUTR3seq = inputUTR3seq;
	}
	/** 输出文件，可以是gz */
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
		lsCmd.add(">"); lsCmd.add(predictResultFile);
		return lsCmd;
	}
	
	private String[] getTargetScore() {
		return new String[]{"-sc", targetScore + ""};
	}
	private String[] getTargetEnergy() {
		return new String[]{"-en", targetEnergy + ""};
	}
	
	/** 读取产生的结果 */
	public Iterable<MirandaPair> readPerlines() {
		return readPerlines(predictResultFile);
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


Read Sequence:AT1G01046.1 (207 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01046.1
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
		Map<String, HybridMiranda> mapKey2Unit = new HashMap<>();
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
			HybridMiranda mirandaUnit = new HybridMiranda();
			for (String content : lsMirandaUnit) {
				content = content.trim();
				if (content.startsWith("Forward:")) {
					mirandaUnit = new HybridMiranda();
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
					mirandaUnit = new HybridMiranda();
				}
			}
			mirandaUnit = null;
		}

		private void setMaxEnergyKey(HybridUnit mirandaUnit) {
			if (maxEnergyKey == null) {
				maxEnergyKey = mirandaUnit.getKey();
			} else if (mirandaUnit.energy < mapKey2Unit.get(maxEnergyKey).energy) {
				maxEnergyKey = mirandaUnit.getKey();
			} else if (mirandaUnit.energy == mapKey2Unit.get(maxEnergyKey).energy && mirandaUnit.startS < mapKey2Unit.get(maxEnergyKey).startS) {
				maxEnergyKey = mirandaUnit.getKey();
			}
		}
		
		/** 获得最大能量的一对结果 */
		public HybridUnit getMirandaUnitMaxEnergy() {
			return mapKey2Unit.get(maxEnergyKey);
		}
		
		/** 考虑seed序列后获得最大能量的一对结果<br>
		 * null表示不存在这样的一对比较
		 *  */
		public HybridMiranda getMirandaUnitMaxEnergySeed() {
			List<HybridMiranda> lsUnit = new ArrayList<>(mapKey2Unit.values());
			Collections.sort(lsUnit, new Comparator<HybridMiranda>() {
				public int compare(HybridMiranda o1, HybridMiranda o2) {
					Double o1e = Math.abs(o1.energy), o2e = Math.abs(o2.energy);
					Integer start1 = o1.startS, start2 = o2.startS;
					int e = -o1e.compareTo(o2e);
					if (e != 0) {
						return e;
					} else {
						return start1.compareTo(start2);
					}
				}
			});
			for (HybridMiranda hybridUnit : lsUnit) {
				if (hybridUnit.isSeedPerfectMatch()) {
					return hybridUnit;
				}
			}
			return null;
		}
	}

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStr();
		lsResult.add(cmd);
		return lsResult;
	}

	public static class HybridMiranda extends HybridUnit {
		double score;

		/** 获得3-5的queryseq */
		protected void setQseq(String qSeqContent) {
			qSeq = qSeqContent.replace("Query:", "").replace("3'", "")
					.replace("5'", "").trim();
		}

		/** 获得5-3的queryseq */
		protected void setSseq(String sSeqContent) {
			sSeq = sSeqContent.replace("Ref:", "").replace("3'", "")
					.replace("5'", "").trim();
		}

		/** 获得5-3的queryseq */
		protected void setAlign(String alignContent) {
			align = alignContent.trim();
			char[] seqQ = qSeq.toCharArray();
			startSpaceNum = 0;
			for (char c : seqQ) {
				if ((int) c >= 97) {
					startSpaceNum++;
				} else if ((int) c < 97) {
					break;
				}
			}
		}

		/**
		 * 设定seq的位置，打分，能量<br>
		 * 从以下文字获取<br>
		 * >ath-miR156c AT1G01010.1 161.00 -18.63 2 23 264 293 24 66.67% 75.00%
		 * 
		 * @param content
		 */
		protected void setLocSocreEnergy(String content) {
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

		/** 整理清楚的结果 */
		public String toResultTab() {
			List<String> lsResult = new ArrayList<>();
			lsResult.add(qName);
			lsResult.add(sName);
			lsResult.add(energy + "");
			lsResult.add(score + "");
			lsResult.add(startS + "");
			lsResult.add(endS + "");
			String[] result = lsResult.toArray(new String[0]);
			return ArrayOperate.cmbString(result, "\t");
		}

		public String getTitle() {
			List<String> lsResult = new ArrayList<>();
			lsResult.add(TitleFormatNBC.QueryID.toString());
			lsResult.add(TitleFormatNBC.SubjectID.toString());
			lsResult.add(TitleFormatNBC.Energy.toString());
			lsResult.add(TitleFormatNBC.Score.toString());
			lsResult.add("StartSubject");
			lsResult.add("EndSubject");
			String[] result = lsResult.toArray(new String[0]);
			return ArrayOperate.cmbString(result, "\t");
		}

	}

}
