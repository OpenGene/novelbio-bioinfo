package com.novelbio.analysis.seq.comparegenomics;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class Script {

	static int refCol = 2;
	static int queryCol = 1;
	static int refLocCol = 3;
	static int refIDCol = 5;
	public static void main(String[] args) {
		SeqHash seqHash = new SeqHash("/media/winF/NBC/Project/Project_WZF/compareGenomic/P1-7_modify.fa");
//		System.out.println(seqHash.getSeq("NC_012926", 55601, 55608).toString());
		
		
//		seqHash = new SeqHash("/media/winF/NBC/Project/Project_WZF/compareGenomic/ss070731.fsa");
//		System.out.println(seqHash.getSeq("SC070731", 55597, 55599).toString());
		
		
		convertSnpIndelFile("/media/winF/NBC/Project/Project_WZF/compareGenomic/snpIndel/P1.SNPsInDel.csv", seqHash);
	}
	
	/**
	 * 把GBK文件中的带数字编号的序列转换成正常的序列
	 */
	private static void modifyFa(String input) {
		TxtReadandWrite txtRead = new TxtReadandWrite(input, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(input, "_modify", null), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.trim().split(" ");
			if (ss.length > 1) {
				ArrayList<String> lsInfo = ArrayOperate.converArray2List(ss);
				lsInfo.remove(0);
				String tmpResult = ArrayOperate.cmbString(lsInfo.toArray(new String[0]), "");
				txtWrite.writefileln(tmpResult.trim());
			} else {
				txtWrite.writefileln(ss);
			}
		}
		txtWrite.close();
	}
	
	private static void convertSnpIndelFile(String inputFile, SeqHash seqHash) {
		String out = FileOperate.changeFileSuffix(inputFile, "_modify", null);
		TxtReadandWrite txtRead = new TxtReadandWrite(inputFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		
		/** subject 相对于 query 插入 */
		boolean insert = false;
		String insertStr = "";
		
		/** subject 相对于 query 缺失 */
		boolean deletion = false;
		String deletionStr = "";
		
		/** 上一个snp位置的坐标 */
		int lastLoc = 0;
		
		
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			int loc = Integer.parseInt(ss[refLocCol]);
			if (!ss[refCol].equals(".") && !ss[queryCol].equals(".")) {
				if (insert) {
					String[] ssNew = getInsertInfo(ss, insertStr, lastLoc, seqHash);
					txtOut.writefileln(ssNew);
				} else if (deletion) {
					String[] ssNew = getDeletionInfo(ss, deletionStr, lastLoc, seqHash);
					txtOut.writefileln(ssNew);
				}
				txtOut.writefileln(ss[refIDCol] + "\t" + ss[refLocCol] + "\t" + ss[refCol] + "\t" + ss[queryCol]);
				
				insert = false;
				deletion = false;
				insertStr = "";
				deletionStr = "";
			}
			
			else if (ss[refCol].equals(".")) {
				if (insert && lastLoc == loc) {
					insertStr = insertStr + ss[queryCol];
				} else if (insert && lastLoc != loc) {
					String[] ssNew = getInsertInfo(ss, insertStr, lastLoc, seqHash);
					txtOut.writefileln(ssNew);
					insertStr = ss[queryCol];
				} else if (!insert) {
					if (deletion) {
						String[] ssNew = getDeletionInfo(ss, deletionStr, lastLoc, seqHash);
						txtOut.writefileln(ssNew);
					}
					
					
					insert = true;
					insertStr = ss[queryCol];
				}
			}
			
			else if (ss[queryCol].equals(".")) {
				if (deletion && lastLoc == loc - 1) {
					deletionStr = deletionStr + ss[refCol];
				} else if (deletion && lastLoc != loc - 1) {
					String[] ssNew = getDeletionInfo(ss, deletionStr, lastLoc, seqHash);
					txtOut.writefileln(ssNew);
					deletionStr = ss[refCol];
				} else if (!deletion) {
					if (insert) {
						String[] ssNew = getInsertInfo(ss, insertStr, lastLoc, seqHash);
						txtOut.writefileln(ssNew);
					}
					
					deletion = true;
					deletionStr = ss[refCol];
				}
			}
			lastLoc = loc;
		}
	}
	
	private static String[] getInsertInfo(String[] ssThis, String insertStr, int lastLoc, SeqHash seqHash) {
		String[] ssNew = new String[4];
		ssNew[0] = ssThis[refIDCol];
		ssNew[1] = lastLoc + "";
		ssNew[2] = seqHash.getSeq(ssThis[refIDCol], lastLoc, lastLoc).toString().toUpperCase();
		ssNew[3] = ssNew[2] + insertStr;
		return ssNew;
	}
	
	private static String[] getDeletionInfo(String[] ssThis, String deletionStr, int lastLoc, SeqHash seqHash) {
		lastLoc = lastLoc - deletionStr.length();
		String[] ssNew = new String[4];
		ssNew[0] = ssThis[refIDCol];
		ssNew[1] = lastLoc + "";
		ssNew[3] = seqHash.getSeq(ssThis[refIDCol], lastLoc, lastLoc).toString().toUpperCase();
		ssNew[2] = ssNew[3] + deletionStr;
		return ssNew;
	}

}
