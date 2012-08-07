package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.LocInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 将序列按照相关文件进行改动
 * @author zong0jie
 *
 */
public class ModifySeq {
	Logger logger = Logger.getLogger(ModifySeq.class);
	SeqFastaHash seqFastaHash = null;
	SeqFasta seqFasta= null;
	
	public void writeFastaFile(String fastaFileNew) {
		TxtReadandWrite txtFastaNew = new TxtReadandWrite(fastaFileNew, true);
		txtFastaNew.writefileln(">"+seqFasta.getSeqName());
		txtFastaNew.writefilePerLine(seqFasta.toString(), 100);
	}
	
	
	/**
	 * 读取序列文件，设定序列
	 * @param SeqFile
	 */
	public void setSeqFasta(String SeqFile) {
		seqFastaHash = new SeqFastaHash(SeqFile);
		//只获取第一条序列，因为细菌只有一条
		seqFasta = seqFastaHash.getSeqFastaAll().get(0);
	}
	
	/**
	 * 读取序列文件，设定序列
	 * @param SeqFile
	 * @param TOLOWCASE 是否改成小写，true改成小写
	 * false改成大写
	 * null不变
	 */
	public void setSeqFasta(String SeqFile, Boolean TOLOWCASE) {
		seqFastaHash = new SeqFastaHash(SeqFile, "", true,false,TOLOWCASE);
		//只获取第一条序列，因为细菌只有一条
		seqFasta = seqFastaHash.getSeqFastaAll().get(0);
	}
	
	/**
	 * @param modifySeqFile 装序列的文件，序列放在一起
	 * @param pathInfo 装信息的文件夹，每个文件单独一个.info后缀，并且前缀和序列名完全相同
	 * @param outSeqFile
	 * @param outNoModifySeqNameFile 没有进行modify的序列的名字，写入一个文本
	 * @param modifySeqInfoFile
	 */
	public void readPathLastZ(String modifySeqFile, String pathInfo , String outSeqFile, String outNoModifySeqNameFile, String modifySeqInfoFile,String statictis) 
	{
		pathInfo = FileOperate.addSep(pathInfo);
		
		SeqFastaHash seqModifyFastaHash = new SeqFastaHash(modifySeqFile);//h(modifySeqFile);
		ArrayList<String[]> lsFileName = FileOperate.getFoldFileName(pathInfo, "*", "info");
		//modify的lst_Info信息
		ArrayList<ModifyInfo> lsModifyInfos = new ArrayList<ModifyInfo>();
		for (String[] strings : lsFileName) {
			LastzAlign lastzAlign = new LastzAlign();
			String seq = seqModifyFastaHash.getSeqAll(strings[0].toLowerCase(), true);
			String alignFile = pathInfo + strings[0]+".info";
			lastzAlign.readInfo(alignFile, seqFasta.toString().length(), seq.length());
			lsModifyInfos.add( lastzAlign.getModifyInfo(seq) );
		}
		
		ArrayList<ArrayList<ModifyInfo>> lsResult = ModifyInfo.delOverlap(lsModifyInfos);
		modifySeq(lsResult.get(0));
		TxtReadandWrite txtOutSeq = new TxtReadandWrite(outSeqFile, true);
		txtOutSeq.writefilePerLine(seqFasta.toString(), 100);
		txtOutSeq.close();
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outNoModifySeqNameFile, true);
		for (ModifyInfo modifyInfo : lsResult.get(1)) {
			txtReadandWrite.writefileln(modifyInfo.isAssemle()+"\t"+modifyInfo.modifyName+"\t"+
					modifyInfo.getStart()+"\t"+ modifyInfo.getEnd()+"\t"+modifyInfo.getStartModify() + "\t"+ modifyInfo.getEndModify() + "\t"+ modifyInfo.isBooStart()+"\t"+modifyInfo.isBooEnd() + "\t" + modifyInfo.isCis5to3());
		}
		TxtReadandWrite txtModifySeqInfoFile = new TxtReadandWrite(modifySeqInfoFile, true);
		for (ModifyInfo modifyInfo : lsResult.get(0)) {
			txtModifySeqInfoFile.writefileln(modifyInfo.isAssemle()+"\t"+modifyInfo.modifyName +"\t"+
					modifyInfo.getStart()+"\t"+ modifyInfo.getEnd()+"\t"+modifyInfo.getStartModify() + "\t"+ modifyInfo.getEndModify()+"\t"+ modifyInfo.isBooStart()+"\t"+modifyInfo.isBooEnd() + "\t" + modifyInfo.isCis5to3());
		}
		txtReadandWrite.close();
		txtModifySeqInfoFile.close();
		ArrayList<LocInfo> lsresult = seqFasta.getSeqInfo();
		TxtReadandWrite txtStatistic = new TxtReadandWrite(statictis, true);
		
		try {
			txtStatistic.writefile(lsresult);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param modifySeqFile 装序列的文件，序列放在一起
	 * @param lastzFile 装信息的lastz文件
	 * @param outSeqFile
	 * @param outNoModifySeqNameFile 没有进行modify的序列的名字，写入一个文本
	 * @param modifySeqInfoFile
	 */
	public void readFileLastZ(String modifySeqFile, String lastzFile , String outSeqFile, String outNoModifySeqNameFile, String modifySeqInfoFile, String statictis) 
	{		
		SeqFastaHash seqModifyFastaHash = new SeqFastaHash(modifySeqFile);
		//modify的lst_Info信息
		ArrayList<ModifyInfo> lsModifyInfos = ModifyInfo.getModifyInfo(lastzFile, seqModifyFastaHash);
		
		ArrayList<ArrayList<ModifyInfo>> lsResult = ModifyInfo.delOverlap(lsModifyInfos);
		modifySeq(lsResult.get(0));
		TxtReadandWrite txtOutSeq = new TxtReadandWrite(outSeqFile, true);
		txtOutSeq.writefilePerLine(seqFasta.toString(), 100);
		txtOutSeq.close();
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outNoModifySeqNameFile, true);
		for (ModifyInfo modifyInfo : lsResult.get(1)) {
			txtReadandWrite.writefileln(modifyInfo.isAssemle()+"\t"+modifyInfo.modifyName+"\t"+
					modifyInfo.getStart()+"\t"+ modifyInfo.getEnd()+"\t"+modifyInfo.getStartModify() + "\t"+ modifyInfo.getEndModify() + "\t"+ modifyInfo.isBooStart()+"\t"+modifyInfo.isBooEnd() + "\t" + modifyInfo.isCis5to3());
		}
		TxtReadandWrite txtModifySeqInfoFile = new TxtReadandWrite(modifySeqInfoFile, true);
		for (ModifyInfo modifyInfo : lsResult.get(0)) {
			txtModifySeqInfoFile.writefileln(modifyInfo.isAssemle()+"\t"+modifyInfo.modifyName +"\t"+
					modifyInfo.getStart()+"\t"+ modifyInfo.getEnd()+"\t"+modifyInfo.getStartModify() + "\t"+ modifyInfo.getEndModify()+"\t"+ modifyInfo.isBooStart()+"\t"+modifyInfo.isBooEnd() + "\t" + modifyInfo.isCis5to3());
		}
		txtReadandWrite.close();
		txtModifySeqInfoFile.close();
		ArrayList<LocInfo> lsresult = seqFasta.getSeqInfo();
		TxtReadandWrite txtStatistic = new TxtReadandWrite(statictis, true);
		
		try {
			txtStatistic.writefile(lsresult);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param SeqFile 序列文件
	 * @param modifySeqFile 装序列的文件，序列放在一起
	 * @param pathInfo 装信息的文件夹，每个文件单独一个.info后缀，并且前缀和序列名完全相同
	 * @param outNoModifySeqNameFile 没有进行modify的序列的名字，写入一个文本
	 */
	public void readPathSoapsnp(String soapsnpFile) 
	{
		//modify的lst_Info信息
		ArrayList<SoapsnpInfo> lsSoapsnpInfos = SoapsnpInfo.readInfo(soapsnpFile);
		modifySeqSNP(lsSoapsnpInfos);
	}
	
	public void readPathDindel(String dindelFile) {
		ArrayList<DindelInfo> lsDindelInfos = DindelInfo.readInfo(dindelFile);
		modifySeqIndel(lsDindelInfos);
	}
	
	
	
	/**
	 * @param seqName 序列名
	 * @param lsModifyInfos 经过排序的没有overlap的修改信息
	 */
	private void modifySeq( ArrayList<ModifyInfo> lsModifyInfos)
	{
		ModifyInfo tmpModifyInfo = null;
		for (int i = lsModifyInfos.size()-1; i >= 0; i--) {
			ModifyInfo modifyInfo = lsModifyInfos.get(i);
			if (modifyInfo.isCrossStartSite()) {
				tmpModifyInfo = modifyInfo;
				String subseq = modifyInfo.getModifySeq().substring(0, modifyInfo.getCrossStartSiteSeq2End() - modifyInfo.getStartModify() +1).toUpperCase();
//				seqFasta.modifySeq(modifyInfo.getStart(),modifyInfo.getCrossStartSiteSeq1End(),subseq,  modifyInfo.isBooStart(),true);
				seqFasta.modifySeq(modifyInfo.getStart(),seqFasta.toString().length(),subseq,  modifyInfo.isBooStart(),true);
				continue;
			}
			seqFasta.modifySeq(modifyInfo.getStart(), modifyInfo.getEnd(),modifyInfo.getModifySeq().toUpperCase(), modifyInfo.isBooStart(), modifyInfo.isBooEnd());
		}
		if (tmpModifyInfo != null) {
			String subseq = tmpModifyInfo.getModifySeq().substring(tmpModifyInfo.getCrossStartSiteSeq2Start() - tmpModifyInfo.getStartModify()).toUpperCase();
//			seqFasta.modifySeq(tmpModifyInfo.getCrossStartSiteSeq1Start(),tmpModifyInfo.getEnd(),subseq,  tmpModifyInfo.isBooStart(),true);
			seqFasta.modifySeq(1,tmpModifyInfo.getEnd(),subseq, true ,tmpModifyInfo.isBooEnd());
		}
	}
	/**
	 * @param seqName 序列名
	 * @param lsModifyInfos 经过排序的没有overlap的修改信息
	 */
	private void modifySeqSNP( ArrayList<SoapsnpInfo> lsSoapsnpInfos)
	{
		seqFasta.modifySeq(lsSoapsnpInfos);
	}
	/**
	 * @param seqName 序列名
	 * @param lsModifyInfos 经过排序的没有overlap的修改信息
	 */
	private void modifySeqIndel( ArrayList<DindelInfo> lsDindelInfos)
	{
		Collections.sort(lsDindelInfos);
		//从大到小进行序列的修改
		for (int i = lsDindelInfos.size()-1; i >= 0; i--) {
			DindelInfo dindelInfo = lsDindelInfos.get(i);
			if (dindelInfo.getAltBase().contains(">") || dindelInfo.getAltBase().contains(",")) {
				logger.error("出现异常字符: "+ dindelInfo.getAltBase());
				continue;
			}
			seqFasta.modifySeq(dindelInfo.getStart(), dindelInfo.getEnd(), dindelInfo.getAltBase(), true, true);
		}
	}
	
	
}

