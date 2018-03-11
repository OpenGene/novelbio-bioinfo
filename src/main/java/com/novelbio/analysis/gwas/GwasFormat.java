package com.novelbio.analysis.gwas;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * gwas格式转化
 * 将plinkMap和plinkPed转化为
 * chrId,position,AA,TT,AT
 * 这种，其中每一对就是一个样本
 * @author zong0jie
 * @data 2018年3月10日
 */
public class GwasFormat {
	String plinkMap;
	/** 其中不能有indel信息 */
	String plinkPed;
	
	public static void main(String[] args) throws IOException {
		Options opts = new Options();
		opts.addOption("plinkMap", true, "plinkMap");
		opts.addOption("plinkPed", true, "plinkPed");
		opts.addOption("snpDataOut", true, "snpDataOut");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.err.println("java -jar gwasFormat.jar --plinkMap plink.map --plinkPed plink.ped --snpDataOut snp.dataout.csv");
			System.exit(1);
		}
		String plinkMap = cliParser.getOptionValue("plinkMap", "");
		String plinkPed = cliParser.getOptionValue("plinkPed", "");
		String snpDataOut = cliParser.getOptionValue("snpDataOut", "");

		GwasFormat gwasFormat = new GwasFormat();
		gwasFormat.setPlinkMap(plinkMap);
		gwasFormat.setPlinkPed(plinkPed);
		gwasFormat.convertor(snpDataOut);
		
	}
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
	}
	public void setPlinkPed(String plinkPed) {
		this.plinkPed = plinkPed;
	}
	
	public void convertor(String snpDataOut) throws IOException {
		PlinkPedReader.createPlinkPedIndex(plinkPed);
		PlinkPedReader plinkPedReader = new PlinkPedReader(plinkPed);
		List<String> lsSamples = plinkPedReader.getLsAllSamples();
		TxtReadandWrite txtReadMap = new TxtReadandWrite(plinkMap);
		TxtReadandWrite txtWriteSnpData = new TxtReadandWrite(snpDataOut, true);
		
		int index = 0;
		for (String content : txtReadMap.readlines()) {
			Allele allele = new Allele(content);
			allele.setIndex(index++);
			txtWriteSnpData.writefile(allele.getRefID() + "," + allele.getPosition());
			for (String sampleName : lsSamples) {
				Allele alleleSample = null;
				try {
					alleleSample = plinkPedReader.readAllelsFromSample(sampleName, index , index ).iterator().next();
				} catch (Exception e) {
					alleleSample = plinkPedReader.readAllelsFromSample(sampleName, index, index ).iterator().next();

				}
				txtWriteSnpData.writefile("," + alleleSample.getAllele1() + alleleSample.getAllele2());
			}
			txtWriteSnpData.writefileln();
		}
		txtReadMap.close();
		txtWriteSnpData.close();
		plinkPedReader.close();
	}
	
}
