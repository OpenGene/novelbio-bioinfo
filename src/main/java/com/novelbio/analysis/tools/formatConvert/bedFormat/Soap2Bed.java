package com.novelbio.analysis.tools.formatConvert.bedFormat;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.tools.formatConvert.FastQ;


/**
 * ???æ³???¨å???scII??´¨????? * @author zong0jie
 *
 */
public class Soap2Bed {
	
	/**
	 * å°?oapè½??ä¸?ed??»¶ï¼?????ä¸?ear-end??????å¹¶ä????å°???????????????¨è?ä¸??
	 * ???æ³???¨å???scII??´¨?????	 * ???æµ??ç«?5bp
	 * @param soapFile 
	 * @param outPut1 è¾??#/1åº????????ä¸??èµ·ç?45bpï¼??è¡????5bp
	 * @param outCombFile1 è¾??#/1åº?????å¹¶ï?ä¸??èµ·ç?ç»????ragment?¿åº¦ï¼??äº???¢ç???	 * @param outPut2 è¾??#/2åº????????ä¸??èµ·ç?45bpï¼??è¡????5bp
	 * @param outCombFile2 è¾??#/2åº?????å¹¶ï?ä¸??èµ·ç?ç»????ragment?¿åº¦ï¼??äº???¢ç???	 * @param outError è¾?????ä¿¡æ?ï¼??å°±æ?ä¸¤ä¸ª #/1??¸¤ä¸?/2è¿??ä¸?µ·?????	 * @throws Exception
	 */
	public static void getBed2Macs(String fastQ,String soapFile,String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		if (fastQ.trim().toLowerCase().equals("sanger")) {
			fastQ = NovelBioConst.FASTQ_SANGER;
		}
		else if (fastQ.trim().toLowerCase().equals("illumina")) {
			fastQ = NovelBioConst.FASTQ_ILLUMINA;
		}
		FastQ.setFastQoffset(fastQ);
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtoutCombFile1 = new TxtReadandWrite();
		txtoutCombFile1.setParameter(outCombFile1, true, false);
		
		TxtReadandWrite txtOut2 = new TxtReadandWrite();
		txtOut2.setParameter(outPut2, true, false);
		TxtReadandWrite txtoutCombFile2 = new TxtReadandWrite();
		txtoutCombFile2.setParameter(outCombFile2, true, false);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		//?·å?æµ???¿åº¦
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
 
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap??»¶???å¼?? chrID ??? ???mapping?°æ?è´??ï¼??????½æ?èµ·ç?ï¼???????????bpLength-1???
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//???è¦????/1?³å?ï¼????/1ä¸ºæ?ï¼??mapping?°æ??¾ä?ï¼????apping?°è??¾ä?
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//???å½?/1??/2????????????ç¡??æµ??ç»?????ä¸?olexaæµ???????°±???æ­£ä?è´?			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtoutCombFile1.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				/////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				txtOut2.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtoutCombFile2.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else {
				txtOuterror.writefile(tmpcontent+"\n"+content+"\n");
			}
		}
		txtOut1.close();
		txtOut2.close();
		txtoutCombFile1.close();
		txtoutCombFile2.close();
		txtOuterror.close();
		txtSoap.close();
		
	}
	
	
	/**
	* å°?oapè½??ä¸?ed??»¶ï¼??äº?Eï¼???ºå?æ­£è??¾æ????
	* ???æ³???¨å???scII??´¨?????	* @param SE true: ???   false: ???
	 * @param soapFile 
	 * @param bpLength æµ???¿åº¦
	 * @param outPut macs???ä»¶ï?ä»????eads?????	 * @param outComb ?¨ä?????????apping??»¶ï¼??ç«??reads?????å»¶ç???50bpï¼??ç«?????ç«??åº??å¹?	 * @param error ??????????????¾¿è®?	 * @throws Exception 
	 */
	public static void copeSope2Bed(String fastQ,boolean SE, String soapFile,String outPut,String outComb,String error) throws Exception {
		if (fastQ.trim().toLowerCase().equals("sanger")) {
			fastQ = NovelBioConst.FASTQ_SANGER;
		}
		else if (fastQ.trim().toLowerCase().equals("illumina")) {
			fastQ = NovelBioConst.FASTQ_ILLUMINA;
		}
		FastQ.setFastQoffset(fastQ);
		if (SE) {
			getBed2MacsSE(soapFile, outPut, outComb);
		}
		else {
			getBed2MacsPE(soapFile, outPut, outComb, error);
		}
	}
	
	
	
	/**
	 * å°?oapè½??ä¸?ed??»¶ï¼??äº?Eï¼???ºå?æ­£è??¾æ????
	 * @param soapFile
	 * @param bpLength æµ???¿åº¦
	 * @param outPut1 macs???ä»¶ï?ä»????eads?????	 * @param outLong ?¨ä?????????apping??»¶ï¼??ä¸??reads?????å»¶ç???50bp
	 * @throws Exception
	 */
	private static void getBed2MacsSE(String soapFile,String outPut1,String outLong) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutLong = new TxtReadandWrite();
		txtOutLong.setParameter(outLong, true, false);
		
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
		
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
			if (ss[6].equals("+")) {
//////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				if (bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////			
				String tmpres2 = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+349)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
			else {
				//////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				if (bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])+bpLength-350) +"\t"+(Long.parseLong(ss[8])+bpLength-1) +"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
		}
		txtOut1.close();
		txtOutLong.close();
		txtSoap.close();
	}
	
	/**
	 * å°?oapè½??ä¸?ed??»¶ï¼??äº?Eï¼???ºå?æ­£è??¾æ????
	 * @param soapFile
	 * @param bpLength æµ???¿åº¦
	 * @param outPut1 ?¨ä?????????apping??»¶ï¼??ä¸??????¿åº¦??¹¶
	 * @param outCombine 
	 * @throws Exception
	 */
	private static void getBed2MacsPE(String soapFile,String outPut1,String outCombine,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutComb = new TxtReadandWrite();
		txtOutComb.setParameter(outCombine, true, false);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
		
		
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap??»¶???å¼?? chrID ??? ???mapping?°æ?è´??ï¼??????½æ?èµ·ç?ï¼???????????bpLength-1???
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//???è¦????/1?³å?ï¼????/1ä¸ºæ?ï¼??mapping?°æ??¾ä?ï¼????apping?°è??¾ä?
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//???å½?/1??/2????????????ç¡??æµ??ç»?????ä¸?olexaæµ???????°±???æ­£ä?è´?			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				/////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				/////////////////////////////////////////////////åº??è´¨é?////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else {
				txtOuterror.writefile(tmpcontent+"\n"+content+"\n");
			}
		}
		txtOut1.close();
		txtOutComb.close();
		txtSoap.close();
		txtOuterror.close();
	}
	
	/**
	 * ç»??solexa??oap??appingç»??ï¼??å¾?????fastQè¡?	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String> getSoapFastQStr(String soapFile) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		ArrayList<String> lsSoap= txtSoap.readFirstLines(1000);
		ArrayList<String> lsFastQ = new ArrayList<String>();
		for (String string : lsSoap) {
			lsFastQ.add(string.split("\t")[2]);
		}
		return lsFastQ;
	}
}
