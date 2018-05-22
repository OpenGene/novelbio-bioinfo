package com.novelbio.analysis.seq.rnahybrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalconf.TitleFormatNBC;

public class RNAhybrid implements IntCmdSoft {
	public static void main(String[] args) {
		RNAhybrid rnAhybrid = new RNAhybrid();
		rnAhybrid.setMiRNAseq("/media/winD/plant_miRNA_predict/AraDemo.fa");
		rnAhybrid.setSpeciesType(RNAhybridClass.human);
		rnAhybrid.setUtr3File("/media/winD/plant_miRNA_predict/seq.fa");
		rnAhybrid.setPredictResultFile("/media/winD/plant_miRNA_predict/rnahybrid.out");
		rnAhybrid.mirnaPredictRun();
	}
	
	/** 序列最长不能超过这个长度，超过了会报错 */
	int lengthMax = 2000;
	
	String exePath = "";
	String SpeciesType = "3utr_human";
	
	String utr3Seq;
	String miRNASeq;
	String predictResultFile;
	double targetPvalue = 0.01;
	int targetEnergy = -15;
	
	/** 用来读取结果文件的 */
	TxtReadandWrite txtMirandaRead;
	
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.exePath = "";
		else
			this.exePath = FileOperate.addSep(exePath);
	}
	/** 默认0.01 */
	public void setTargetPvalue(double targetPvalue) {
		this.targetPvalue = targetPvalue;
	}
	/** 设定UTR3的序列，没有的话就从gffChrSeq中提取 */
	public void setUtr3File(String utr3Seq) {
		this.utr3Seq = utr3Seq;
	}
	public void setMiRNAseq(String miRNASeq) {
		this.miRNASeq = miRNASeq;
	}
	/** 输出文件，可以是gz */
	public void setPredictResultFile(String predictResultFile) {
		this.predictResultFile = predictResultFile;
	}
	
	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		this.targetEnergy = -Math.abs(targetEnergy);
	}
	public void setSpeciesType(RNAhybridClass rnaAhybridClass) {
		SpeciesType = rnaAhybridClass.getDetailClassName();
	}
	/**
	 * taxID对应RNAhybrid的对照表，一般来说低等生物对应线虫，哺乳动物对应人类，昆虫对应果蝇
	 * @param txtTaxID_to_RNAhybrid_s_class
	 */
	private String[] getRNAhybridClass() {
		return new String[]{"-s", SpeciesType.toString()};
	}
	private String[] getUtr3Seq(String utr3Seq) {
		return new String[]{"-t", utr3Seq};
	}
	private String[] getMirSeq() {
		return new String[]{"-q", miRNASeq};
	}
	
	public void mirnaPredictRun() {
		String utr3CutSeq = getCutFastaName();
		cutSeq(utr3CutSeq);
		CmdOperate cmdOperate = new CmdOperate(getLsCmd(utr3CutSeq));
		cmdOperate.runWithExp("RNAhybrid error:");
	}
	
	private List<String> getLsCmd(String utr3CutSeq) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "RNAhybrid");
		ArrayOperate.addArrayToList(lsCmd, getRNAhybridClass());
		ArrayOperate.addArrayToList(lsCmd, getUtr3Seq(utr3CutSeq));
		ArrayOperate.addArrayToList(lsCmd, getMirSeq());
		lsCmd.add(">");
		lsCmd.add(predictResultFile);
		return lsCmd;
	}
	
	/** 读取产生的结果 */
	public Iterable<HybridRNAUnit> readPerlines() {
		return readPerlines(predictResultFile);
	}
	
	/** 减小序列的长度。
	 * RNAhybrid要求序列的长度小于2000bp，因此需要将比这个序列长的序列进行缩小
	 *  */
	private String cutSeq(String utr3CutSeq) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(utr3CutSeq, true);
		SeqFastaReader seqFastaReader = new SeqFastaReader(utr3Seq);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			if (seqFasta.Length() > lengthMax) {
				seqFasta = seqFasta.getSubSeq(1, lengthMax, true);
			}
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
		seqFastaReader.close();
		txtWrite.close();
		return utr3CutSeq;
	}
	
	private String getCutFastaName() {
		String outTmpUtrSeq = FileOperate.getParentPathNameWithSep(predictResultFile) + FileOperate.getFileName(utr3Seq);
		outTmpUtrSeq = FileOperate.changeFileSuffix(outTmpUtrSeq, "_tmpCutShort", null);
		return outTmpUtrSeq;
	}
	
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public Iterable<HybridRNAUnit> readPerlines(String fileName) {
		if (txtMirandaRead != null) txtMirandaRead.close();
		txtMirandaRead = new TxtReadandWrite(fileName);
		final BufferedReader bufread =  txtMirandaRead.readfile(); 
		final List<String> lsInfo = new ArrayList<>();
		return new Iterable<HybridRNAUnit>() {
			public Iterator<HybridRNAUnit> iterator() {
				return new Iterator<HybridRNAUnit>() {
					HybridRNAUnit rnaHybridUnit = getLine();
					public boolean hasNext() {
						return rnaHybridUnit != null;
					}
					public HybridRNAUnit next() {
						HybridRNAUnit retval = rnaHybridUnit;
						rnaHybridUnit = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					HybridRNAUnit getLine() {
						HybridRNAUnit mirandaPair = null;
						try {
							mirandaPair = getLineRnaHybrid();
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
					
					HybridRNAUnit getLineRnaHybrid() throws IOException {
						HybridRNAUnit rnaHybridUnit = null;
						String content;
						int spaceNum = 0;
						while ((content = bufread.readLine()) != null) {
							if (content.equals("")) {
								spaceNum++;
							} else {
								spaceNum = 0;
							}
							if (spaceNum == 2) {
								rnaHybridUnit = new HybridRNAUnit(lsInfo);
								spaceNum = 0;
								lsInfo.clear();
								break;
							}
							lsInfo.add(content);
						}
						return rnaHybridUnit;
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
	
	public static HashMap<String, RNAhybridClass> getMapSpeciesType2HybridClass() {
		HashMap<String, RNAhybridClass> mapSpeciesType2HybridClass = new HashMap<String, RNAhybridClass>();
		mapSpeciesType2HybridClass.put("worm", RNAhybridClass.worm);
		mapSpeciesType2HybridClass.put("fly", RNAhybridClass.fly);
		mapSpeciesType2HybridClass.put("mammal", RNAhybridClass.human);
		return mapSpeciesType2HybridClass;
	}
	
	public static enum RNAhybridClass {
		worm("3utr_worm"), fly("3utr_fly"), human("3utr_human");
		static Map<String, RNAhybridClass> map = new HashMap<>();
		private final String detailClassName;
		private RNAhybridClass(String detailClassName) {
			this.detailClassName = detailClassName;
		}
		public String getDetailClassName() {
			return detailClassName;
		}
		public String toString() {
			return detailClassName;
		}
		public static Map<String, RNAhybridClass> getMapStr2Value() {
			if (map.size() == 0) {
				map.put("worm", worm);
				map.put("fly", fly);
				map.put("human", human);
			}
			return map;
		}
	}

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		String utr3CutSeq = getCutFastaName();
		List<String> lsCmd = getLsCmd(utr3CutSeq);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStr();
		lsResult.add(cmd);
		return lsResult;
	}	
	
	public static class HybridRNAUnit extends HybridUnit {
		double pvalue;
		
		protected HybridRNAUnit() {
		}
		
		public HybridRNAUnit(List<String> lsInfo) {
			String[] seqAlign = new String[4];
			boolean isMirLen = false;
			boolean isSeqAlign = false;
			int seqAlignNum = 0;
			for (String string : lsInfo) {
				if (string.startsWith("target:")) {
					setSname(string.replace("target:", "").trim());
				} else if (string.startsWith("miRNA :")) {
					setQname(string.replace("miRNA :", "").trim());
					isMirLen = true;
				} else if (string.startsWith("mfe:")) {
					double energy = Double.parseDouble(string.replace("mfe:", "").replace("kcal/mol", "").trim());
					setEnergy(energy);
				} else if (string.startsWith("p-value:")) {
					double pvalue = Double.parseDouble(string.replace("p-value:", "").trim());
					setPvalue(pvalue);
				} else if (string.startsWith("position")) {
					setStartS(startS);
					isSeqAlign = true;
					continue;
				} else if (isMirLen && string.startsWith("length:")) {
					startQ = 0;
					startS = Integer.parseInt(string.replace("length:", "").trim());
				}
				
				if (isSeqAlign && seqAlignNum < 4) {
					seqAlign[seqAlignNum++] = string;
				}
			}
			setSeqAndAlign(seqAlign);
			
		}
		
		protected void setSeqAndAlign(String[] seq) {
			String targetUp = seq[0];
			String targetDown = seq[1];
			setSeqAndAlign(true, targetUp, targetDown, "target 5' ");
			String mirUp = seq[3];
			String mirDown = seq[2];
			setSeqAndAlign(false, mirUp, mirDown, "miRNA  3' ");
		}
		
		/**
		 * 将以下信息设定进去
		 * 
		 * target 5'           A                    CUCU         C       C 3'
	                                UCACUCUU         UUCU GUC    
	                                AGUGAGAG          AAGA CAG    
	      miRNA  3' CACG                                              C 5'
	 
		 * @param seqContent
		 */
		private void setSeqAndAlign(boolean target, String seqNotMatch, String setMatch, String beforeName) {
			String tail = target? " 3'" : " 5'";
			seqNotMatch = seqNotMatch.replace(beforeName, "").replace(tail, "");
			setMatch = setMatch.substring(beforeName.length());
			startSpaceNum = 0;
			int seqLen = 0;
			char[] seqUpChar = seqNotMatch.toCharArray();
			char[] seqDownChar = setMatch.toCharArray();
			char[] seqFinal = new char[seqNotMatch.length()];
			char[] alignchr = new char[seqNotMatch.length()];
			boolean start = true;
			for (int i = 0; i < seqUpChar.length; i++) {
				char up = seqUpChar[i];
				char down = ' ';
				if (i < seqDownChar.length) {
					down = seqDownChar[i];
				}
				
				if (up == ' ' && down == ' ') {
					seqFinal[i] = '-';
					alignchr[i] = ' ';
				} else if (down != ' ') {
					start = false;
					alignchr[i] = '|';
					seqFinal[i] = down;
					seqLen++;
				} else if (up != ' ') {
					seqFinal[i] = up;
					alignchr[i] = ' ';
					seqLen++;
				}
				if (start) {
					startSpaceNum++;
				}
			}
			if (target) {
				sSeq = new String(seqFinal).trim();
				endS = startS + seqLen;
			} else {
				qSeq = new String(seqFinal).trim();
				endQ = startQ + seqLen;
			}
			align = new String(alignchr).trim();
			alignLen = align.length();
		}
		
		
		protected void setQname(String qName) {
			this.qName = qName;
		}
		protected void setSname(String sName) {
			this.sName = sName;
		}
		private void setPvalue(double pvalue) {
			this.pvalue = pvalue;
		}
		private void setEnergy(double energy) {
			this.energy = energy;
		}
		private void setStartS(int startS) {
			this.startS = startS;
		}
		public double getPvalue() {
			return pvalue;
		}
		/** 整理清楚的结果 */
		public String toResultTab() {
			List<String> lsResult = new ArrayList<>();
			lsResult.add(qName);
			lsResult.add(sName);
			lsResult.add(energy+"");
			lsResult.add(pvalue + "");
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
			lsResult.add(TitleFormatNBC.Pvalue.toString());
			lsResult.add("StartSubject");
			lsResult.add("EndSubject");
			String[] result = lsResult.toArray(new String[0]);
			return ArrayOperate.cmbString(result, "\t");
		}

	}

}


