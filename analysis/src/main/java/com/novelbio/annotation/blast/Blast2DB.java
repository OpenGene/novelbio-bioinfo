package com.novelbio.annotation.blast;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fastaSeqRead.FastaSeqStringHash;
import com.novelBio.base.fastaSeqRead.SeqInfo;
import com.novelbio.annotation.copeID.CopeID;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.entity.friceDB.NCBIID;

/**
 * ׼�����У���Blast����������������ݿ�
 * @author zong0jie
 *
 */
public class Blast2DB {
	
	/**
	 * ����������ʽ��ץȡ��������Ҫ��ID�����regx.equals("")����ôĬ��ץȡ
	 * >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA 
	 * �е� NR_024540
	 * @param fastaFile
	 * @param output
	 * @param regx ��ץȡ��������ʽ
	 * @throws Exception
	 */
	public static void prepareSeqGetID(String fastaFile,String output, String regx) throws Exception 
	{
		if (regx.equals("")) {
			regx="ref\\|(\\w+)\\.{0,1}\\d{0,1}";
		}
		TxtReadandWrite txtFastaFile=new TxtReadandWrite();
		txtFastaFile.setParameter(fastaFile, false, true);
		TxtReadandWrite txtoutput=new TxtReadandWrite();
		txtoutput.setParameter(output, true, false);
		
		 Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
		 Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������

		BufferedReader fastaReader=txtFastaFile.readfile();
		String content="";
		
		while((content=fastaReader.readLine())!=null)
		{
			//�� >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA  ����ȡrefID
			if (content.contains(">"))
			{
				 String refID="";
				 matcher = pattern.matcher(content);
				 if(matcher.find())
				 {
					 refID=matcher.group(1);
				 }
				 else 
				 {
					System.out.println(content);
					refID="error";
				}
				txtoutput.writefile(">"+refID+"\n", false);
				continue;
			}
			txtoutput.writefile(content+"\n",false);
		}
		txtoutput.writefile("", true);
	}
	
	/**
	 * ����������ʽ��ץȡ��������Ҫ��ID�����regx.equals("")����ôĬ��ץȡȫ������<br>
	 * ��regx="(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})"ʱ��ץȡ <br>
	 *  >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA �е� NR_024540 <br>
	 *  Ȼ��ץȡ����ֵ��NCBIID����geneID���������м�Ϊ >geneID
	 *  ���������������һ����geneID����ô��Ϊ geneID<
	 *  @param getNoName ����������ݿ����Ҳ������ֵ����У��Ƿ���Ҫ��������,true:���� false:����
	 * @throws Exception 
	 */
	public static void prepareSeqGetGeneID(String fastaFile,boolean CaseChange,String regx,Boolean append, String output,boolean getNoName) throws Exception 
	{

		Hashtable<String, SeqInfo> hashSeq=FastaSeqStringHash.readfile(fastaFile, CaseChange, regx, append);
		ArrayList<String> lsSeqName=FastaSeqStringHash.lsSeqName;
		
		TxtReadandWrite txtOutput=new TxtReadandWrite();
		txtOutput.setParameter(output, true, false);
		
		Hashtable<String, String> hashWrit=new Hashtable<String, String>();
		
		for (int i = 0; i < lsSeqName.size(); i++) {
			SeqInfo seqInfo=hashSeq.get(lsSeqName.get(i));
			//�������ݿ⣬��������ת��ΪgeneID
			//���ظ��������е�"<"����ȥ��
			NCBIID ncbiid=new NCBIID(); ncbiid.setAccID(seqInfo.SeqName.replace("<", ""));
			DaoFSNCBIID daoSNCBIID=new DaoFSNCBIID();
			ArrayList<NCBIID> lsNcbiids=daoSNCBIID.queryLsNCBIID(ncbiid);
			if (lsNcbiids==null||lsNcbiids.size()==0) {
				if (getNoName) {
					String geneID=seqInfo.SeqName;
					System.out.println(geneID);
				}
				else {
					String geneID=seqInfo.SeqName;
					System.out.println(geneID);
					continue;
				}
			}
			else {
				//����������ظ�geneID�����У���ô������������������
				String geneID=lsNcbiids.get(0).getGeneId()+"";
				if (hashWrit.containsKey(geneID)&&hashWrit.get(geneID).length()>=seqInfo.SeqSequence.length()) {
					continue;
				}
				hashWrit.put(geneID, seqInfo.SeqSequence);
			}

		}
		Iterator iter = hashWrit.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String key = (String) entry.getKey();
		    String val = (String) entry.getValue();
			txtOutput.writefile(">"+key+"\n",false);
			txtOutput.writefile(val+"\n",false);
		}
		txtOutput.writefile("",true);
	}
	
	
	/**
	 * ��affy̽���target�ļ���������������ʽΪ>affyID
	 * @throws Exception 
	 */
	public static void getAffySeq(String affySeqFIle,String output) throws Exception {
		TxtReadandWrite txtAffySeqFile=new TxtReadandWrite();
		txtAffySeqFile.setParameter(affySeqFIle, false, true);
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(output, true, false);
		
		BufferedReader readerAffy=txtAffySeqFile.readfile();
		String content="";
		while ((content=readerAffy.readLine())!=null)
		{
			if (content.trim().startsWith(">")) 
			{
				String[] ss=content.split(":|;");
				txtresult.writefile(">"+ss[2].trim()+"\n");
			}
			else {
				txtresult.writefile(content+"\n");
			}
		}
	}
	
	/**
	 * 
	 * ��BlastResult�����Blast2Info�ĸ�ʽ����ֱ�ӵ������ݿ�
	 * @param blastResultFile 
	 * @param queryTax
	 * @param subjectTax
	 * @param queryDB
	 * @param subjectDB
	 * @param output
	 * @param getGenID subject��AccID�Ƿ�ת����geneID����Ϊ�е��Ѿ���geneID�Ͳ���Ҫת����ע�����ﻹ����ֱ���ã���Ϊblast�õ��Ľ���ܿ���������ref|XP_001001|������ӵģ���Ҫ������ץ����
	 * @param hashAcc2GenID ��getGenIDΪtrueʱ���ṩacc2geneID��hash��
	 * @throws Exception
	 */
	public static void copeBlastResult(String blastResultFile,int queryTax,int subjectTax, String queryDB,String subjectDB,String output,boolean getGenID,HashMap<String, String> hashAcc2GenID) throws Exception {
		/**
		TxtReadandWrite txtBlastGetTaxID=new TxtReadandWrite();
		txtBlastGetTaxID.setParameter(blastResultFile, false, true);
		BufferedReader readerTaxID=txtBlastGetTaxID.readfile();
		String content2 = "";
		boolean flagQuery = false; //��queryTax��subjectTax����ú�����
		boolean flagSubject = false; //��queryTax��subjectTax����ú�����

		while ((content2=readerTaxID.readLine())!=null) {
			String[] ssStrings = content2.split("\t");
			NCBIID ncbiid = new NCBIID();
			ncbiid.setGeneId(Long.parseLong(ssStrings[0]));
			ArrayList<NCBIID> lsncbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid != null && lsncbiid.size()>0) 
			{
				queryTax = (int) lsncbiid.get(0).getTaxID();
				flagQuery = true;
			}
			ncbiid.setGeneId(Long.parseLong(ssStrings[1]));
			 lsncbiid = DaoFSNCBIID.queryLsNCBIID(ncbiid);
			if (lsncbiid != null && lsncbiid.size()>0) 
			{
				subjectTax = (int) lsncbiid.get(0).getTaxID();
				flagSubject = true;
			}
			
			if(flagQuery&&flagSubject)
				break;
		}
		**/

		TxtReadandWrite txtBlastResult=new TxtReadandWrite();
		txtBlastResult.setParameter(blastResultFile, false, true);
		
		TxtReadandWrite txtResult=new TxtReadandWrite();
		txtResult.setParameter(output, true, false);
		
		BufferedReader reader=txtBlastResult.readfile();
		String content="";
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     String blastDate = formatDate.format(currentDate); //������ʱ���ʽ��
	     ArrayList<String[]> lsResult=new ArrayList<String[]>();
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			String[] tmpresult=new String[9];
			tmpresult[0]=ss[0];tmpresult[1]=queryTax+"";tmpresult[2]=queryDB;
			if (getGenID) {
				//TODO : ��ss[1]�Ĵ���
				ss[1] = hashAcc2GenID.get(ss[1]);
			}
			else
				tmpresult[3]=ss[1];
			
			
			tmpresult[4]=subjectTax+"";tmpresult[5]=subjectDB;
			tmpresult[6]=ss[2];tmpresult[7]=ss[10];tmpresult[8]=blastDate;
			lsResult.add(tmpresult);
		}
		txtResult.ExcelWrite(lsResult, "\t", 1, 1);
		txtBlastResult.close();
		txtResult.close();
	}
	
	/**
	 * 	 * ������blast�����A��B
	 * ������Ҫ��A��B�ϲ����ϲ�ΪA��queryID��B��subjectID�Լ��������
	 * ��������agilent blast affyID �󣬻����A�� agilent2Affy��ֵ
	 * Ȼ����B��affyPig2Hum��ֵ,affyPig2Hum�Ѿ�����ΪBlast2Info����ʽ
	 * ��ô��Ҫ���agilent2Hum��ֵ
	 * <b>�ڲ�A2B��blast��һЩ�������Ե���</b>
	 * @throws Exception 
	 * @param blastFile
	 * @param Bblast
	 * @param Bfile B���ֵ������ļ����Ѿ�����Ϊ��׼fasta��ʽ
	 * @param output
	 * @throws Exception
	 */
	public static void getBlastA2B(String Ablast,String Bblast,String Bfile,String output) throws Exception {
	
		Hashtable<String, String> hashB2A=new Hashtable<String, String>();
		/**
		 * key: query probe 
		 * value: string[2]
		 * 0: subject name
		 * 1: e-value
		 */
		Hashtable<String, String[]> hashA2B=new Hashtable<String, String[]>();
		TxtReadandWrite txtA=new TxtReadandWrite();
		txtA.setParameter(Ablast, false, true);
		
		TxtReadandWrite txtB=new TxtReadandWrite();
		txtB.setParameter(Bblast, false, true);
		
		TxtReadandWrite txtOut=new TxtReadandWrite();
		txtOut.setParameter(output, true, false);
		
		FastaSeqStringHash.readfile(Bfile, false, "", false);
		
		BufferedReader readerA=txtA.readfile();
		String content="";
		while ((content=readerA.readLine())!=null) {
			String[] ss=content.split("\t");
			if (Integer.parseInt(ss[3])>50&&Integer.parseInt(ss[4])+Integer.parseInt(ss[5])<3) {
				//���һ��agilent̽���Ӧ��������ͬ��affyID,��������һ��װ��hash����
				if (hashA2B.containsKey(ss[0])) 
				{
					if (Double.parseDouble(hashA2B.get(ss[0])[1])==Double.parseDouble(ss[10])) {
						if (   FastaSeqStringHash.getsequence(hashA2B.get(ss[0])[0], true).length()  <  FastaSeqStringHash.getsequence(ss[1], true).length() ) {
							String[] tmpInfo = new String[2];
							tmpInfo[0] = ss[1]; tmpInfo[1] = ss[10];
							hashA2B.put(ss[0], tmpInfo);
						}
					}
				}
				else
				{
					String[] tmpInfo = new String[2];
					tmpInfo[0] = ss[1]; tmpInfo[1] = ss[10];
					hashA2B.put(ss[0], tmpInfo);
				}
			}
		}
		
		
		Enumeration keys=hashA2B.keys();

		while(keys.hasMoreElements()){
			String Aid=(String)keys.nextElement();
			String[] Binfo=hashA2B.get(Aid);
			//һ��affyID������agilentID��Ӧ����ô������agilentID��"//"����
			if (hashB2A.containsKey(Binfo[0]))
			{
				String tmpaID=hashB2A.get(Binfo[0]);
				if (!tmpaID.contains(Aid))
				{
					tmpaID=tmpaID+"//"+Aid;
					hashB2A.put(Binfo[0], tmpaID);
				}
			}
			else 
			{
				hashB2A.put(Binfo[0], Aid);
			}
		}

		BufferedReader readerB=txtB.readfile();
		String content2="";
		ArrayList<String[]> result=new ArrayList<String[]>();
		while ((content2=readerB.readLine())!=null) {
			String[] ss=content2.split("\t");
			//��B2Human�ı��е�����B���滻��A�ı����ID��
			//��Ϊһ��Bid���ܶ�Ӧ���Aid����ô����Ļ�һ��B��Ӧ�� A1//A2//A3
			//���Ծ���split("//")�����и�Ȼ��ÿһ��һ����ô����ȥ
			if (hashB2A.containsKey(ss[0]))
			{
				String tmpAid=hashB2A.get(ss[0]);//��A�滻����
				String[] ssAid=tmpAid.split("//");
				for (int i = 0; i < ssAid.length; i++) 
				{
					String[] tmpResult = new String[ss.length];
					tmpResult[0] = ssAid[i];
					for (int j = 1; j < ss.length; j++)
					{
						tmpResult[j] = ss[j];
					}
					result.add(tmpResult);
				}
				
			}
		}
		txtOut.ExcelWrite(result, "\t", 1, 1);
		txtA.close();
		txtB.close();
		txtOut.close();
	}
	
	
	
	/**
	 * 
	 * ������blast�����agilent2RefSeq,���������Ϊ taxID \t geneID \t accID \t DBinfo \n�ĸ�ʽ<br>
	 * <b>�ڲ�A2B��blast��һЩ�������Ե���</b><br>
	 * ע���������е�geneID���ܣ� geneID//geneID//geneID<br>
	 * ��ô�ں����������ݿ�Ĺ�������Ҫ��geneID�ֿ�Ȼ��ֱ������ݿ�<br>
	 * @param blastFile
	 * @param Tax
	 * @param queryDB
	 * @param output
	 * @param geneID
	 * @param getGenID
	 * @param hashAcc2GenID
	 * @throws Exception
	 */
	public static void copeA2GeneID(String blastFile,int Tax, String queryDB,String output,boolean getGenID,HashMap<String, String> hashAcc2GenID) throws Exception {
		
		//����̽��2geneID����Ϣ��key��̽�룬value��0��geneID 1��identity 2��evalue
		HashMap<String, String[]> hashA2geneID=new HashMap<String, String[]>();
		
		TxtReadandWrite txtBlastResult=new TxtReadandWrite();
		txtBlastResult.setParameter(blastFile, false, true);
		
		TxtReadandWrite txtResult=new TxtReadandWrite();
		txtResult.setParameter(output, true, false);
		
		BufferedReader reader=txtBlastResult.readfile();
		String content="";
		
		
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			String geneID = "";
			if (
					//agilent�Ĳ���
//					Integer.parseInt(ss[3])>50//blast�ϵĸ���
//					&&
//					Integer.parseInt(ss[4])+Integer.parseInt(ss[5])<3//�������gap
					//affy�Ĳ���
					Double.parseDouble(ss[2])>=90//blast identity
					&&
					Integer.parseInt(ss[4])<6 //������
					&&
					Integer.parseInt(ss[5])<3 //gap��
			) 
			{
				if (getGenID) 
					geneID = hashAcc2GenID.get(CopeID.getBlastAccID(ss[1]));
				else 
					geneID = ss[1];
				
				
				if (hashA2geneID.containsKey(ss[0])){
					String[] geneIDInfo = hashA2geneID.get(ss[0]);
					if (Double.parseDouble(geneIDInfo[1])<Double.parseDouble(ss[2]) //identity�����
							&& 
							Double.parseDouble(geneIDInfo[2])>Double.parseDouble(ss[10]) //evalue��С��
							) {
						String[] tmpgeneIDInfo = new String[3];
						tmpgeneIDInfo[0] = geneID; tmpgeneIDInfo[1] = ss[2]; tmpgeneIDInfo[3] = ss[10];
						hashA2geneID.put(ss[0], tmpgeneIDInfo);
					}
					continue;
				}
				String[] tmpgeneIDInfo = new String[3];
				tmpgeneIDInfo[0] = geneID; tmpgeneIDInfo[1] = ss[2]; tmpgeneIDInfo[2] = ss[10];
				hashA2geneID.put(ss[0], tmpgeneIDInfo);
				hashA2geneID.put( ss[0],tmpgeneIDInfo);//ע������key��B��value��A
			}
		}
	     ArrayList<String[]> lsResult=new ArrayList<String[]>();
	     for(Map.Entry<String,String[]> entry:hashA2geneID.entrySet())
	     {
	     	String key = entry.getKey();
	     	String[] val = entry.getValue();
	     	 String[] tmpresult=new String[4];
	         tmpresult[0]=Tax+"";tmpresult[1]=val[0];tmpresult[2]=key;tmpresult[3]=queryDB;
	         lsResult.add(tmpresult);
	     }
		txtResult.ExcelWrite(lsResult, "\t", 1, 1);
		txtBlastResult.close();
		txtResult.close();
	}
	
	
	
	
}
