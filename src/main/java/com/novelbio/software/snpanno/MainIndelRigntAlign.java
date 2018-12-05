package com.novelbio.software.snpanno;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

/**
 * 给定vcf文件，将其中的indel右移
 * 这样方便将多个vcf文件进行合并和比较
 * @author zong0jie
 * @data 2018年11月8日
 */
public class MainIndelRigntAlign {
	private static final Logger logger = LoggerFactory.getLogger(MainIndelRigntAlign.class);
	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption("chrseq", true, "chromosome file");
		opts.addOption("direction", true, "align direction");
		opts.addOption("in", true, "input vcf file");
		opts.addOption("out", true, "output vcf file");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.out.println(getHelp());
			System.exit(1); 
		}

		String chrSeq = cliParser.getOptionValue("chrseq", "");
		String direction = cliParser.getOptionValue("direction", "");
		String in = cliParser.getOptionValue("in", "");
		String out = cliParser.getOptionValue("out");
		
//		String chrSeq = "/home/novelbio/mywork/vcf-right-align/Homo_sapiens.GRCh37.75.dna.primary_assembly.fa";
//		String in = "/home/novelbio/mywork/vcf-right-align/Homo_sapiens_GRCh37_75.vcf";
//		String out = "/home/novelbio/mywork/vcf-right-align/Homo_sapiens_GRCh37_75.realign.vcf";
//		String direction = "left";
		
		boolean isRight = true;
		if (StringOperate.isRealNull(direction) || direction.equals("right")) {
			isRight = true;
		} else if (direction.equals("left")) {
			isRight = false;
		} else {
			System.err.println("error on param direction, can only be left or right but is " + direction );
			System.out.println();
			getHelp();
			System.exit(1);
		}
	
		SeqHash seqHash = new SeqHash(chrSeq);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(in);
		TxtReadandWrite txtWrite = new TxtReadandWrite(out, true);
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
			
			int position = 0;
			String ref, alt, head;
			if (isRight) {
				position = snpInfo.getAlignRefRight().getStartAbs();
				ref = snpInfo.getSeqRefRight();
				alt = snpInfo.getSeqAltRight();
				head = snpInfo.getSeqHeadRight();
			} else {
				position = snpInfo.getAlignRefLeft().getStartAbs();
				ref = snpInfo.getSeqRefLeft();
				alt = snpInfo.getSeqAltLeft();
				head = snpInfo.getSeqHeadLeft();
			}
			
			if (snpInfo.getVarType() == EnumHgvsVarType.Insertions || snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
				ref = head+ref;
				alt = head+alt;
			}
			if (snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
				position = position-1;
			}
			ss[1] = position+"";
			ss[3] = ref;
			ss[4] = alt;
			if (ss[3].length() > 1 && ss[4].length() > 1) {
				logger.error("error on " + content +"\n"
						+ "cannot handle " + ss[0] + "\t"+ ss[1] + "\t" + ss[3] +"\t" +ss[4]
						);
			}
			txtWrite.writefileln(ss);
		}
		seqHash.close();
		txtRead.close();
		txtWrite.close();
	}
	
	private static String getHelp() {
		List<String> lsHelp = new ArrayList<>();
		lsHelp.add("java -jar IndelRightAlign.jar --chrseq chrseq.fa --in a.vcf --out a.rightalign.vcf");
		lsHelp.add("");
		lsHelp.add("--direction        right");
		lsHelp.add("--chrseq        chromosome file");
		lsHelp.add("--in         input vcf file");
		lsHelp.add("--out         output vcf file");
		return ArrayOperate.cmbString(lsHelp, "\n");
	}
}
