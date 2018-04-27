package com.novelbio.analysis.seq.fasta.format;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneID;

/**
 * 将从NCBI下载的序列，其序列名中的>gi|513046643|ref|NW_004675961.1| Setaria italica strain Yugu1 unplaced genomic scaffold, Setaria V1 SETITscaffold_1, whole genome shotgun sequence
 * NW_004675961提取出来
 * @author zong0jie
 *
 */
public class NCBIabstractSeq {
	public static void main(String[] args) {
//		TxtReadandWrite txtRead = new TxtReadandWrite("/media/hdfs/nbCloud/public/nbcplatform/genome/millet/Setaria_V1/gff/ref_Setaria_V1_top_level.gff3");
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/hdfs/nbCloud/public/nbcplatform/genome/millet/Setaria_V1/gff/ref_Setaria_V1_top_level_modify.gff3", true);
//		for (String content : txtRead.readlines()) {
//			if (!content.startsWith("#")) {
//				String[] ss = content.split("\t");
//				ss[0] = GeneID.removeDot(ss[0]);
//				content = ArrayOperate.cmbString(ss, "\t");
//			}
//			txtWrite.writefileln(content);
//		}
//		txtRead.close();
//		txtWrite.close();
		
	}
	String seqFileName;
	boolean isRemoveDot = true;
	PatternOperate patternOperate;
	boolean changeName2Lowcase = true;
	public void setSeqName(String seqName) {
		this.seqFileName = seqName;
	}
	/**
	 * 是否将NW_004675961.1的.1去除
	 * @param isRemoveDot
	 */
	public void setRemoveDot(boolean isRemoveDot) {
		this.isRemoveDot = isRemoveDot;
	}
	
	public void abstractSeq() {
		patternOperate = new PatternOperate("ref\\|(.+?)\\|", false);
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFileName);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFileName, "_modify", null), true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				if (changeName2Lowcase) {
					content = content.toLowerCase();
				}
				content = patternOperate.getPatFirst(content, 1);
				if (content == null) {
					throw new RuntimeException("no need name:" + content);
				}
				if (isRemoveDot) {
					content = ">" + GeneID.removeDot(content);
				}
			}
			txtWrite.writefileln(content);
		}
		txtRead.close();
		txtWrite.close();
	}
}
