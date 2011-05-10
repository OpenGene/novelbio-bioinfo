package com.novelBio.chIPSeq.motif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.Patternlocation;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.motifSearch.MotifSearch;
import com.novelBio.generalConf.NovelBioConst;


 


public class Motifsearch {
	int maxChrLength=10000;
	/**
	 * ���motif��ȫ�������ϵ��ܶ�
	 * @param chrFilePath
	 * @param Rworkspace
	 * @param motifRex
	 * @throws Exception
	 */
	public void getChrMoitfDensity(String chrFilePath,String Rworkspace,String motifRex) throws Exception {
		
		 if (!Rworkspace.endsWith(File.separator)) {  
			 Rworkspace = Rworkspace + File.separator;  
	         }
		 GffChrUnion gffChrUnion=new GffChrUnion();
		MotifSearch cdgmotif=new MotifSearch();
		cdgmotif.MotifloadChrFile(chrFilePath);
		cdgmotif.startMotifSearch(motifRex, maxChrLength);
		Hashtable<String,ArrayList< double[]>> hashMotifDensity=cdgmotif.getMotifDensity();
		ArrayList<String[]> lsChrID=gffChrUnion.getChrLengthInfo();
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motifAllNum.txt", true,false);
		txtReadandWrite.writefile(cdgmotif.getMotifAllNum()+"");
		gffChrUnion.saveChrLengthToFile(Rworkspace+"MotifDensity/chrLen.txt");
		
		for (int i = 0; i < lsChrID.size(); i++) {
			String tmpChrID=lsChrID.get(i)[0].toLowerCase();
			ArrayList<double[]> tmpMotifInfo = hashMotifDensity.get(tmpChrID);
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motifx",true, false);
			txtReadandWrite.Rwritefile(tmpMotifInfo.get(0));
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motify", true,false);
			txtReadandWrite.Rwritefile(tmpMotifInfo.get(1));
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/parameter", true,false);
			txtReadandWrite.writefile("Item"+"\t"+"Info"+"\r\n");//����Ҫ���ϵģ�����R��ȡ��������
			ArrayList<String[]>  lschrInfo=gffChrUnion.getChrLengthInfo();
			int maxChrLen=Integer.parseInt(lschrInfo.get(lschrInfo.size()-1)[1]);
			txtReadandWrite.writefile("maxresolution"+"\t"+maxChrLen+"\r\n");
			txtReadandWrite.writefile("ChrID"+"\t"+tmpChrID+"\r\n");
			
			Rprogram("/media/winE/Bioinformatics/R/practice_script/platform/");
			//FileOperate.delFile(Rworkspace+"motifx");
			//FileOperate.delFile(Rworkspace+"motify");
			//FileOperate.delFile(Rworkspace+"parameter");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/motifx",tmpChrID+"motifx");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/motify",tmpChrID+"motify");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/parameter",tmpChrID+"parameter");
		}
	}
	
	/**
	 * ִ��R����ֱ��R��������ٷ���
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private int Rprogram(String bin) throws IOException, InterruptedException  
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+bin+ "MyMotifDensity.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}
	
	
	
	/**
	 * @Gffclass ����ȡ��gffhash���࣬Ŀǰֻ���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gfffilename gff�ļ�
	 * @param chrPah chr�ļ���
	 * @param peaklength peak�������˳���
	 * @param condition 
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������
	 * 1: ͨͨ��ȡ����
	 * 2: ͨͨ��ȡ����
	 * @param txtFilepeakFile peak��Ϣ�����ı���ʽ����
	 * @param sep �ı�����ķָ����һ��Ϊ"\t"
	 * @param columnID ��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
	 * @param rowStart �ӵڼ��ж�ȡ
	 * @param rowEnd �����ڼ��� ���rowEnd=-1����һֱ�����ļ���β
	 * @param motifReg motif��������ʽ
	 * @throws Exception
	 * @return  ArrayList-String ÿ��peak������
	 */
	private ArrayList<Integer> getMotifLocation(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,String sep
			,int[] columnID,int rowStart,int rowEnd,
			String motifReg
	) throws Exception
	{
		ArrayList<Integer> lsResult = new ArrayList<Integer>();
		GetSeq.prepare(chrPah, null, Gffclass, gfffilename, "");
		ArrayList<String> lsPeakSeq = GetSeq.getPeakSeq(peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, false);//(Gffclass, gfffilename, chrPah, peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd,false);
		for (String string : lsPeakSeq) {
			ArrayList<String[]> lstmpResult = Patternlocation.getPatLoc(string, motifReg, false);
			for (String[] strings : lstmpResult) {
				int motiflength = strings[0].length();
				Integer motifLocation = Integer.parseInt(strings[1]) - peaklength + motiflength/2;
				lsResult.add(motifLocation);
			}
		}
		return lsResult;
	}
	
	/**
	 * @Gffclass ����ȡ��gffhash���࣬Ŀǰֻ���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gfffilename gff�ļ�
	 * @param chrPah chr�ļ���
	 * @param peaklength peak�������˳���
	 * @param condition 
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������
	 * 1: ͨͨ��ȡ����
	 * 2: ͨͨ��ȡ����
	 * @param txtFilepeakFile peak��Ϣ�����ı���ʽ����
	 * @param sep �ı�����ķָ����һ��Ϊ"\t"
	 * @param columnID ��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
	 * @param rowStart �ӵڼ��ж�ȡ
	 * @param rowEnd �����ڼ��� ���rowEnd=-1����һֱ����sheet1�ļ���β
	 * @param motifReg motif��������ʽ
	 * @param resultPath �ļ�����·��
	 * @param resultPrix �ļ������ǰ׺
	 * @throws Exception
	 * @return  ArrayList-String ÿ��peak������
	 */
	public void getMotifSummitDensity(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,String sep
			,int[] columnID,int rowStart,int rowEnd,
			String motifReg,String resultPath,String resultPrix
			) throws Exception
	{
		ArrayList<Integer> lsMotifLocation = getMotifLocation(Gffclass, gfffilename, chrPah, peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, motifReg);
		TxtReadandWrite txtMotifDensity = new TxtReadandWrite();
		txtMotifDensity.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
		TxtReadandWrite txtMotifDensityParam = new TxtReadandWrite();
		txtMotifDensityParam.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
		txtMotifDensity.writefile(lsMotifLocation);
		//�������룬��һ��maintitle���ڶ��к����꣬�ڶ���������
		txtMotifDensityParam.writefile("Motif Near Peak Summit"+"\n");
		txtMotifDensityParam.writefile("Peak Summit"+"\n");
		txtMotifDensityParam.writefile("Motif Density"+"\n");
		RDensity();
		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
	}
	
	/**
	 * @param Gffclass ����ȡ��gffhash���࣬Ŀǰֻ���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gfffilename gff�ļ�
	 * @param chrPah chr�ļ���
	 * @param length tss������������
	 * @param excelLoc ����LOC��excel�ļ�
	 * @param columnID ��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡһ�У�0��LOCID
	 * @param writeCol д��ڼ���
	 * @param rowStart �ӵڼ��ж�ȡ
	 * @param rowEnd �����ڼ��� ���rowEnd=-1����һֱ����sheet1�ļ���β
	 * @param motifReg motif��������ʽ
	 * @throws Exception
	 */
	public void getMotifDetail(String Gffclass, String gfffilename,String chrPah,int length,String excelLoc
			,int[] columnID,int writeCol, int rowStart, int rowEnd,
			String motifReg
			) throws Exception
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		GetSeq.prepare(chrPah, null, Gffclass, gfffilename, "");
		String[][] LOCIDInfo=ExcelTxtRead.readExcel(excelLoc, columnID, rowStart, rowEnd);
		for (String[] strings : LOCIDInfo) {
			if (strings[0].equals("LOC_Os04g59380")) {
				System.out.println("aaa");
			}
			String upstream = GetSeq.getGffLocatCod().getUpGenSeq(strings[0], length, true, true, Gffclass);
			ArrayList<String[]> lstmpResult = Patternlocation.getPatLoc(upstream, motifReg, false);
			String[] tmpResult = new String[lstmpResult.size()*2];
			for (int i = 0; i < lstmpResult.size(); i++) {
				tmpResult[i*2] = lstmpResult.get(i)[0];
				tmpResult[i*2+1] =  lstmpResult.get(i)[2];
			}
			lsResult.add(tmpResult);
		}
		ExcelOperate excel = new ExcelOperate();
		excel.openExcel(excelLoc);
		excel.WriteExcel(true,rowStart, writeCol, lsResult);
	}
	
	/**
	 * ִ��R����ֱ��R��������ٷ���
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private int RDensity() throws IOException, InterruptedException  
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}

}
