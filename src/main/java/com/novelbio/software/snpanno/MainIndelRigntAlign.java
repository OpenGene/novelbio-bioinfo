package com.novelbio.software.snpanno;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

/**
 * 给定vcf文件，将其中的indel右移
 * 这样方便将多个vcf文件进行合并和比较
 * @author zong0jie
 * @data 2018年11月8日
 */
public class MainIndelRigntAlign {
	
	public static void main(String[] args) {
		String chrSeq = "";
		SeqHash seqHash = new SeqHash(chrSeq);
		
		String vcf = "";
		String vcfright = FileOperate.changeFileSuffix(vcf, ".rightalign", "vcf", "vcf");
		TxtReadandWrite txtRead = new TxtReadandWrite(vcf);
		TxtReadandWrite txtWrite = new TxtReadandWrite(vcfright, true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				txtWrite.writefileln(content);
				continue;
			}
			String[] ss = content.split("\t");
			if (ss[4].contains("/") || ss[4].contains("|")) {
				txtWrite.writefileln(content);
				continue;
			}
			SnpInfo snpInfo = new SnpInfo(ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
			snpInfo.realign(seqHash);
			int position = snpInfo.getAlignRefRight().getStartAbs();
			String ref = snpInfo.getSeqRefRight();
			String alt = snpInfo.getSeqAltRight();
			String head = snpInfo.getSeqHeadRight();
			if (snpInfo.getVarType() == EnumHgvsVarType.Insertions || snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
				ref = head+ref;
				alt = head+alt;
			}
			if (snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
				ref = head+ref;
				alt = head+alt;
				position = position-1;
			}
			ss[1] = position+"";
			ss[3] = ref;
			ss[4] = alt;
			txtWrite.writefileln(ss);
		}
		seqHash.close();
		txtRead.close();
		txtWrite.close();
	}
	
}
