package com.novelbio.analysis.gwas;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.SeekablePathInputStream;

/**
 * 把plinkPed转化为 0 1这种
 * @author novelbio
 *
 */
public class PlinkPedConverter {
	private static final Logger logger = LoggerFactory.getLogger(PlinkPedConverter.class);
	String plinkPed;
	/** plinkMap文件，注意第四列添加上了reference碱基，不考虑indel的情况 */
	String plinkMap;
	
	PlinkPedReader pedReader;
	
	public static void main(String[] args) {
//		args = new String[]{"--chrFile", "/home/novelbio/NBCresource/gwas/gwas/chrAll.nochr.fa"
//				, "--plinkMap", "/home/novelbio/NBCresource/gwas/gwas/619-40.map"
//				, "--plinkPed", "/home/novelbio/NBCresource/gwas/gwas/619-40.ped"
//				, "--outPath", "/home/novelbio/NBCresource/gwas/gwas/result"
//		};
		Options opts = new Options();
		if (args[0].equals("--help")) {
			System.out.println("java -jar PlinkConvertor.jar --chrFile chromosome.fa --plinkMap plinkmap.map --plinkPed plinkped.ped --outPath /path/to/result/");
			System.out.println("chromosome.fa must have index file chromosome.fa.fai");

			return;
		}
		
		opts.addOption("chrFile", true, "chrFile");
		opts.addOption("plinkMap", true, "plinkMap");
		opts.addOption("plinkPed", true, "plinkPed");
		opts.addOption("outPath", true, "outPath");

		logger.info(ArrayOperate.cmbString(args, "\t"));
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			System.exit(1);
		}
		
		String chrFile = cliParser.getOptionValue("chrFile", "");
		String plinkMap = cliParser.getOptionValue("plinkMap", "");
		String plinkPed = cliParser.getOptionValue("plinkPed", "");
		String outPath = cliParser.getOptionValue("outPath", "");
		if (StringOperate.isRealNull(outPath)) {
			outPath = "./";
		} else {
			outPath = FileOperate.addSep(outPath);
		}
		
		String plinkMapNewPath = outPath + FileOperate.getFileName(plinkMap);
		String plinkPedNewPath = outPath + FileOperate.getFileName(plinkPed);

		String plinkMapAnno = FileOperate.changeFileSuffix(plinkMapNewPath, ".anno", null);
		
		String plinkMapConvert = FileOperate.changeFileSuffix(plinkMapNewPath, ".convert", null);
		String plinkPedConvert = FileOperate.changeFileSuffix(plinkPedNewPath, ".convert", null);

		PlinkMapAddBase plinkMapAddBase = new PlinkMapAddBase(chrFile);
		plinkMapAddBase.AddAnno(plinkMap, plinkMapAnno);
		
		PlinkPedConverter pedConverter = new PlinkPedConverter();
		pedConverter.setPlinkMap(plinkMapAnno);
		pedConverter.setPlinkPed(plinkPed);
		pedConverter.convertPlinkMapToAnother(plinkMapConvert);
		pedConverter.convertPlinkPedToAnother(plinkPedConvert);

	}
	
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
	}
	public void setPlinkPed(String plinkPed) {
		this.plinkPed = plinkPed;
	}
	public void convertPlinkMapToAnother(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		TxtReadandWrite txtReadMap = new TxtReadandWrite(plinkMap);
		
		txtWrite.writefileln("SNP\tChromosome\tPosition");
		for (String content : txtReadMap.readlines()) {
			String[] ss = content.split("\t");
			String result = ss[1] + "\t" + ss[0] + "\t" + ss[3];
			txtWrite.writefileln(result);
		}
		txtReadMap.close();
		txtWrite.close();
	}
	
	public void convertPlinkPedToAnother(String outFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		try {
			SeekablePathInputStream seekablePathInputStream = FileOperate.getInputStreamSeekable(plinkMap);
			String plinkIndex = PlinkPedReader.createPlinkPedIndex(plinkPed);		
			List<String> lsSample = PlinkPedReader.getLsSamples(plinkIndex);
			pedReader = new PlinkPedReader(plinkPed);
			writePlinkTitle(seekablePathInputStream, txtWrite);
			txtWrite.writefileln();
			
			logger.info("finish write title");
			for (int i = 0; i < lsSample.size(); i++) {
				String sample = lsSample.get(i);
				seekablePathInputStream.seek(0);
				writePlinkSample(seekablePathInputStream, sample, txtWrite);
				txtWrite.writefileln();
				logger.info("finish write sample{}, {} in " + lsSample.size(), sample, i);
			}
			seekablePathInputStream.close();
		} catch (Exception e) {
			throw new ExceptionNbcFile("read file error " + plinkMap, e);
		} finally {
			txtWrite.close();
		}
		
	}
	
	private void writePlinkTitle(InputStream inputStream, TxtReadandWrite txtWrite) {
		TxtReadandWrite txtPlinkMapReader = new TxtReadandWrite(inputStream);
		txtWrite.writefile("taxa");
		for (String content : txtPlinkMapReader.readlines()) {
			String[] ss = content.split("\t");
			txtWrite.writefile(" " + ss[1]);
		}
	}
	
	private void writePlinkSample(InputStream inputStream, String sample, TxtReadandWrite txtWrite) {
		Iterator<Allele> itPlinkPed = pedReader.readAllelsFromSample(sample).iterator();
		TxtReadandWrite txtPlinkMapReader = new TxtReadandWrite(inputStream);
		txtWrite.writefile(sample);
		int i = 1;
		for (String content : txtPlinkMapReader.readlines()) {
			Allele alleleMap = new Allele(content);
			alleleMap.setIndex(i);
			i++;
			Allele allelePed = itPlinkPed.next();
			if (alleleMap.getIndex() != allelePed.getIndex()) {
				throw new ExceptionNBCPlink("index is not consistant! " + allelePed.toString() + " " + allelePed.toString());
			}
			int result = getResult(alleleMap.getRefBase(), allelePed.getRefBase(), allelePed.getAltBase());
			txtWrite.writefile(" " + result);
		}
	}
	
	/**
	 * 
	 * @param ref
	 * @param allel1
	 * @param allel2
	 * @return
	 * 0 allel1 和 allel2 与 ref相同<br>
	 * 1 allel1 和 allel2 互相不同<br>
	 * 2 allel1 和 allel2 相同但是不与ref相同
	 */
	private int getResult(String ref, String allel1, String allel2) {
		if (ref == null || allel1 == null || allel2 == null) {
			throw new ExceptionNbcParamError("input base cannot be null");
		}
		if (!StringOperate.isEqualIgnoreCase(allel1, allel2)) {
			return 1;
		}
		return StringOperate.isEqualIgnoreCase(ref, allel1) ? 0 : 2;
	}
}
