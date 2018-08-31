package com.novelbio.software.gbas;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.SeekablePathInputStream;

/**
 * gwas格式转化
 * 将plinkMap和plinkPed转化为
 * chrId,position,AA,TT,AT
 * 这种，其中每一对就是一个样本
 * @author zong0jie
 * @data 2018年3月10日
 */
public class GwasFormat {
//	public static void main(String[] args) throws IOException {
//		GwasFormat.convertPlinkCsv2plinkPed("/home/novelbio/test/plink/permutation.plinkped.pre", "/home/novelbio/test/plink/permutation.plink.final.ped");
//	}
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

		GwasFormat.convertor(plinkMap, plinkPed, snpDataOut);
		
	}
	
	/**
	 * @param plinkMap
	 * @param plinkPed 其中不能有indel信息
	 * @param snpDataOut
	 * @throws IOException
	 */
	public static void convertor(String plinkMap, String plinkPed, String snpDataOut) throws IOException {
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
	
	/**
	 * 给定类似csv的文件，转化为plinkPed
	 * @throws IOException 
	 */
	public static void convertPlinkCsv2plinkPed(String csv, String plinkPed) throws IOException {
		long length = FileOperate.getFileSizeLong(csv);
		String title = TxtReadandWrite.readFirstLine(csv);
		TxtReadandWrite txtRead = new TxtReadandWrite(csv);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkPed, true);
		
		String enterType = txtRead.getEnterType();
		String[] samples = title.split("\t");
		int enterLen = enterType.equals(TxtReadandWrite.ENTER_LINUX) ? 1 : 2;
		int startFirst = title.length() + enterLen;
		int lengthNum = 3*samples.length-1+enterLen;
		SeekablePathInputStream seek = FileOperate.getSeekablePathInputStream(FileOperate.getPath(csv));
		for (int i = 0; i < samples.length; i++) {
			int startSite = 0;
			txtWrite.writefile(samples[i] + " " + samples[i] + " 0 0 0 -9");
			for (int j = 0; ; j++) {
				startSite = startFirst + i*3 + j * lengthNum;
				if (startSite > length) {
					break;
				}
				seek.seek(startSite);
				byte[] b = new byte[2];
				seek.read(b);
				char[] chars = getChars(b);
				txtWrite.writefile(" " + chars[0] + " " + chars[1]);
			}
			txtWrite.writefileln();
		}
		txtRead.close();
		txtWrite.close();
	}
	
	private static char[] getChars (byte[] bytes) {
	      Charset cs = Charset.forName ("UTF-8");
	      ByteBuffer bb = ByteBuffer.allocate (bytes.length);
	      bb.put (bytes);
	                 bb.flip ();
	       CharBuffer cb = cs.decode (bb);
	  
	   return cb.array();
	}
}
