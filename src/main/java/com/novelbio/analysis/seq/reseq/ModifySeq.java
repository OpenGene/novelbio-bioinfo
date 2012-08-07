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
 * �����а�������ļ����иĶ�
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
	 * ��ȡ�����ļ����趨����
	 * @param SeqFile
	 */
	public void setSeqFasta(String SeqFile) {
		seqFastaHash = new SeqFastaHash(SeqFile);
		//ֻ��ȡ��һ�����У���Ϊϸ��ֻ��һ��
		seqFasta = seqFastaHash.getSeqFastaAll().get(0);
	}
	
	/**
	 * ��ȡ�����ļ����趨����
	 * @param SeqFile
	 * @param TOLOWCASE �Ƿ�ĳ�Сд��true�ĳ�Сд
	 * false�ĳɴ�д
	 * null����
	 */
	public void setSeqFasta(String SeqFile, Boolean TOLOWCASE) {
		seqFastaHash = new SeqFastaHash(SeqFile, "", true,false,TOLOWCASE);
		//ֻ��ȡ��һ�����У���Ϊϸ��ֻ��һ��
		seqFasta = seqFastaHash.getSeqFastaAll().get(0);
	}
	
	/**
	 * @param modifySeqFile װ���е��ļ������з���һ��
	 * @param pathInfo װ��Ϣ���ļ��У�ÿ���ļ�����һ��.info��׺������ǰ׺����������ȫ��ͬ
	 * @param outSeqFile
	 * @param outNoModifySeqNameFile û�н���modify�����е����֣�д��һ���ı�
	 * @param modifySeqInfoFile
	 */
	public void readPathLastZ(String modifySeqFile, String pathInfo , String outSeqFile, String outNoModifySeqNameFile, String modifySeqInfoFile,String statictis) 
	{
		pathInfo = FileOperate.addSep(pathInfo);
		
		SeqFastaHash seqModifyFastaHash = new SeqFastaHash(modifySeqFile);//h(modifySeqFile);
		ArrayList<String[]> lsFileName = FileOperate.getFoldFileName(pathInfo, "*", "info");
		//modify��lst_Info��Ϣ
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
	 * @param modifySeqFile װ���е��ļ������з���һ��
	 * @param lastzFile װ��Ϣ��lastz�ļ�
	 * @param outSeqFile
	 * @param outNoModifySeqNameFile û�н���modify�����е����֣�д��һ���ı�
	 * @param modifySeqInfoFile
	 */
	public void readFileLastZ(String modifySeqFile, String lastzFile , String outSeqFile, String outNoModifySeqNameFile, String modifySeqInfoFile, String statictis) 
	{		
		SeqFastaHash seqModifyFastaHash = new SeqFastaHash(modifySeqFile);
		//modify��lst_Info��Ϣ
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
	 * @param SeqFile �����ļ�
	 * @param modifySeqFile װ���е��ļ������з���һ��
	 * @param pathInfo װ��Ϣ���ļ��У�ÿ���ļ�����һ��.info��׺������ǰ׺����������ȫ��ͬ
	 * @param outNoModifySeqNameFile û�н���modify�����е����֣�д��һ���ı�
	 */
	public void readPathSoapsnp(String soapsnpFile) 
	{
		//modify��lst_Info��Ϣ
		ArrayList<SoapsnpInfo> lsSoapsnpInfos = SoapsnpInfo.readInfo(soapsnpFile);
		modifySeqSNP(lsSoapsnpInfos);
	}
	
	public void readPathDindel(String dindelFile) {
		ArrayList<DindelInfo> lsDindelInfos = DindelInfo.readInfo(dindelFile);
		modifySeqIndel(lsDindelInfos);
	}
	
	
	
	/**
	 * @param seqName ������
	 * @param lsModifyInfos ���������û��overlap���޸���Ϣ
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
	 * @param seqName ������
	 * @param lsModifyInfos ���������û��overlap���޸���Ϣ
	 */
	private void modifySeqSNP( ArrayList<SoapsnpInfo> lsSoapsnpInfos)
	{
		seqFasta.modifySeq(lsSoapsnpInfos);
	}
	/**
	 * @param seqName ������
	 * @param lsModifyInfos ���������û��overlap���޸���Ϣ
	 */
	private void modifySeqIndel( ArrayList<DindelInfo> lsDindelInfos)
	{
		Collections.sort(lsDindelInfos);
		//�Ӵ�С�������е��޸�
		for (int i = lsDindelInfos.size()-1; i >= 0; i--) {
			DindelInfo dindelInfo = lsDindelInfos.get(i);
			if (dindelInfo.getAltBase().contains(">") || dindelInfo.getAltBase().contains(",")) {
				logger.error("�����쳣�ַ�: "+ dindelInfo.getAltBase());
				continue;
			}
			seqFasta.modifySeq(dindelInfo.getStart(), dindelInfo.getEnd(), dindelInfo.getAltBase(), true, true);
		}
	}
	
	
}

