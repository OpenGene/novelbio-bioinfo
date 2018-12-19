package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 把plinkped文件转换成vcf文件，以及把vcf转化为plinkped文件，支持多位点的情况，也就是一个位点有多个基因型
 * 注意这个非常占用内存，因此只能转小文件
 * @author novelbio
 *
 */
public class PlinkPed2Vcf {
	
	Map<String, char[]> mapSample2Snp = new LinkedHashMap<>();
	
	public void convertPed2Vcf(String pedFile, String midFile, String outvcf) {
		mapSample2Snp.clear();
		TxtReadandWrite txtReadPed = new TxtReadandWrite(pedFile);
		for (String content : txtReadPed.readlines()) {
			String[] ss = content.split("\t");
			String strain = ss[0];
			char[] info = new char[(ss.length-6)*2];
			for (int i = 6; i < ss.length; i++) {
				String[] unit = ss[i].split(" ");
				int num = i-6;
				info[num*2] = unit[0].toCharArray()[0];
				info[num*2+1] = unit[1].toCharArray()[0];
			}
			mapSample2Snp.put(strain, info);
		}
		txtReadPed.close();
		
		TxtReadandWrite txtReadMid = new TxtReadandWrite(midFile);
		TxtReadandWrite txtWriteVcf = new TxtReadandWrite(outvcf, true);
		for (String title : getTitle(mapSample2Snp.keySet())) {
			txtWriteVcf.writefileln(title);
		}
		
		int i = 0;
		for (String content : txtReadMid.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			List<String> lsResult = new ArrayList<>();
			String[] ss = content.split("\t");
			lsResult.add(ss[0]);
			lsResult.add(ss[3]);
			lsResult.add(".");//考虑把varId写进去
			lsResult.add(ss[4]);
			Set<String> setAllele = new LinkedHashSet<>();
			for (String sample : mapSample2Snp.keySet()) {
				char[] site = mapSample2Snp.get(sample);
				if (site[i*2] == 'N' || site[i*2] == 'n' || site[i*2] == '0' ) {
					continue;
				}
				setAllele.add(site[i*2]+"");
				setAllele.add(site[i*2+1]+"");
			}
	
			setAllele.remove(ss[4]);
			List<String> lsAllele = new ArrayList<>(setAllele);
			lsResult.add(ArrayOperate.cmbString(lsAllele, ","));
			lsResult.add(".");
			lsResult.add("PASS");
			lsResult.add(".");
			lsResult.add("GT");
			
			lsAllele.add(0, ss[4]);
			for (String sample : mapSample2Snp.keySet()) {
				char[] site = mapSample2Snp.get(sample);
				String allele1 = site[i*2]+"";
				String allele2 = site[i*2+1]+"";
				if (allele1.equalsIgnoreCase("N") || allele1.equalsIgnoreCase("0")) {
					lsResult.add(".|.");
				} else {
					lsResult.add(lsAllele.indexOf(allele1) + "|" + lsAllele.indexOf(allele2));
				}
			}
			i++;
			txtWriteVcf.writefileln(lsResult);
		}
		txtReadMid.close();
		txtWriteVcf.close();
	}
	
	private List<String> getTitle(Collection<String> lsSamples) {
		List<String> lsResult = new ArrayList<>();
		StringBuilder sbuilder = new StringBuilder("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT");
		for (String sample : lsSamples) {
			sbuilder.append("\t").append(sample);
		}
		lsResult.add(sbuilder.toString());
		return lsResult;
	}
	
	/**
	 * 将vcf文件转化为ped文件
	 * @param vcf
	 * @param ped
	 */
	public void convertVcf2Ped(String vcf, String ped) {
		mapSample2Snp.clear();
		TxtReadandWrite txtReadVcf = new TxtReadandWrite(vcf);
		int snpNum = 0;
		for (String content : txtReadVcf.readlines()) {
			if (content.startsWith("#") || StringOperate.isRealNull(content)) {
				continue;
			}
			snpNum++;
		}
		txtReadVcf.close();
		
		Map<Integer, String> mapCol2Sample = new HashMap<>();
		txtReadVcf = new TxtReadandWrite(vcf);
		int snpIndex = 0;
		for (String content : txtReadVcf.readlines()) {
			if (content.startsWith("#") && !content.startsWith("#CHROM")) {
				continue;
			}
			if (content.startsWith("#CHROM")) {
				String[] ss = content.split("\t");
				for (int i = 9; i < ss.length; i++) {
					mapSample2Snp.put(ss[i], new char[snpNum*2]);
					mapCol2Sample.put(i, ss[i]);
				}
				continue;
			}
			
			String[] ss = content.split("\t");
			List<String> lsSnps = ArrayOperate.converArray2List(ss[4].split(","));
			lsSnps.add(0,ss[3]);
			for (int i = 9; i < ss.length; i++) {
				String sampleName = mapCol2Sample.get(i);
				char[] snps = mapSample2Snp.get(sampleName);
				
				if (ss[i].equals(".|.")) {
					snps[snpIndex*2] = '0';
					snps[snpIndex*2+1] = '0';
					continue;
				}
				
				String[] snpInfo = ss[i].split("\\|");//0|1 这个
				int allele1 = Integer.parseInt(snpInfo[0]);
				int allele2 = Integer.parseInt(snpInfo[1]);
				try {
					snps[snpIndex*2] = lsSnps.get(allele1).toCharArray()[0];
					snps[snpIndex*2+1] = lsSnps.get(allele2).toCharArray()[0];
				} catch (Exception e) {
					snps[snpIndex*2] = lsSnps.get(allele1).toCharArray()[0];
					snps[snpIndex*2+1] = lsSnps.get(allele2).toCharArray()[0];	
				}

			}
			snpIndex++;
		}
		txtReadVcf.close();
		
		TxtReadandWrite txtWritePed = new TxtReadandWrite(ped, true);
		for (String sampleName : mapSample2Snp.keySet()) {
			List<String> lsInfo = new ArrayList<>();
			lsInfo.add(sampleName);
			lsInfo.add(sampleName);
			lsInfo.add("0");
			lsInfo.add("0");
			lsInfo.add("0");
			lsInfo.add("-9");
			char[] snps = mapSample2Snp.get(sampleName);
			for (int i = 0; i < snps.length; i=i+2) {
				lsInfo.add(snps[i]+" " +snps[i+1]);
			}
			txtWritePed.writefileln(lsInfo);
		}
		txtWritePed.close();
	}

}
