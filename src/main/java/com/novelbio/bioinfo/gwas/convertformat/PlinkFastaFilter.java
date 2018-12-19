package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqFastaReader;

/**
 * 存在场景，就是一个单倍型中可能存在几十个甚至更多的序列类型，那么这时候我们仅需要最靠前的20-30个即可，不需要全都展示出来
 * 
 * plinkFasta文件是给定一个单倍型中的序列，将其中排序靠前的单倍型种类提取出来
 * 譬如我就只做前30个单倍型的关系
 * 主要给pegas使用
 * @param plinkPed
 * @param fasta
 */
public class PlinkFastaFilter {
	/**
	 * 给定fasta文件，给定mid文件，给定单倍型数量
	 * 把fasta相同的序列从大到小排列，将排序前 num 个fasta序列提取出来
	 * @param fasta 
	 * name：strain
	 * seq：具体的单倍型序列
	 * @param mid
	 * @param num
	 */
	public static void extractFasta(String fasta, String fastaOut, int num) {
		SeqFastaReader seqFastaReader = new SeqFastaReader(fasta);
		ArrayListMultimap<String, String> mapSeq2LsStrain = ArrayListMultimap.create();
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			String seq = seqFasta.toString();
			mapSeq2LsStrain.put(seq, seqFasta.getSeqName());
		}
		seqFastaReader.close();
		List<Integer> lsStrainNum = new ArrayList<>();
		for (String seq : mapSeq2LsStrain.keys()) {
			lsStrainNum.add(mapSeq2LsStrain.get(seq).size());
		}
		//倒序，选中前num个seq
		Collections.sort(lsStrainNum, (num1, num2)->{return -num1.compareTo(num2);});
		int i = 1;
		int seqNumCutoff = 0;
		for (Integer seqNum : lsStrainNum) {
			if (i++ > num) {
				break;
			}
			seqNumCutoff = seqNum;
		}
		//把seq数量大于seqNum的Strain都挑出来
		//理想状态挑出来的Strain，其Seq做单倍型，就只会有num种单倍型了
		Set<String> setStrainName = new HashSet<>();
		for (String seq : mapSeq2LsStrain.keys()) {
			List<String> lsStrainName = mapSeq2LsStrain.get(seq);
			if (lsStrainName.size() >= seqNumCutoff) {
				setStrainName.addAll(lsStrainName);
			}
		}
		
		seqFastaReader = new SeqFastaReader(fasta);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fastaOut, true);
		for (SeqFasta seqfasta : seqFastaReader.readlines()) {
			if (setStrainName.contains(seqfasta.getSeqName())) {
				txtWrite.writefileln(seqfasta.toStringNRfasta(1000000));
			}
		}
		seqFastaReader.close();
		txtWrite.close();
	}
	

	
}
