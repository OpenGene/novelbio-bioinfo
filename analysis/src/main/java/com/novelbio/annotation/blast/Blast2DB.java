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
 * 准备序列，做Blast，并将结果导入数据库
 * @author zong0jie
 *
 */
public class Blast2DB {
	
	/**
	 * 给定正则表达式来抓取名称中需要的ID。如果regx.equals("")，那么默认抓取
	 * >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA 
	 * 中的 NR_024540
	 * @param fastaFile
	 * @param output
	 * @param regx 待抓取的正则表达式
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
		
		 Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
		 Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。

		BufferedReader fastaReader=txtFastaFile.readfile();
		String content="";
		
		while((content=fastaReader.readLine())!=null)
		{
			//从 >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA  中提取refID
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
	 * 给定正则表达式来抓取名称中需要的ID。如果regx.equals("")，那么默认抓取全部名字<br>
	 * 当regx="(?<=ref\\|)\\w+(?=\\.{0,1}\\d{0,1})"时，抓取 <br>
	 *  >gi|215277009|ref|NR_024540.1| Homo sapiens WAS protein family homolog 5 pseudogene (WASH5P), non-coding RNA 中的 NR_024540 <br>
	 *  然后将抓取到的值上NCBIID搜索geneID，并将序列记为 >geneID
	 *  如果有两个序列有一样的geneID，那么记为 geneID<
	 *  @param getNoName 如果发现数据库中找不到名字的序列，是否需要保留下来,true:保留 false:跳过
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
			//搜索数据库，将序列名转变为geneID
			//将重复序列名中的"<"符号去除
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
				//如果发现了重复geneID的序列，那么仅保留长的那条序列
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
	 * 将affy探针的target文件整理出来，整理格式为>affyID
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
	 * 将BlastResult整理成Blast2Info的格式可以直接导入数据库
	 * @param blastResultFile 
	 * @param queryTax
	 * @param subjectTax
	 * @param queryDB
	 * @param subjectDB
	 * @param output
	 * @param getGenID subject的AccID是否转化成geneID。因为有的已经是geneID就不需要转化，注意这里还不能直接用，因为blast得到的结果很可能是类似ref|XP_001001|这个样子的，还要把他们抓出来
	 * @param hashAcc2GenID 在getGenID为true时，提供acc2geneID的hash表
	 * @throws Exception
	 */
	public static void copeBlastResult(String blastResultFile,int queryTax,int subjectTax, String queryDB,String subjectDB,String output,boolean getGenID,HashMap<String, String> hashAcc2GenID) throws Exception {
		/**
		TxtReadandWrite txtBlastGetTaxID=new TxtReadandWrite();
		txtBlastGetTaxID.setParameter(blastResultFile, false, true);
		BufferedReader readerTaxID=txtBlastGetTaxID.readfile();
		String content2 = "";
		boolean flagQuery = false; //当queryTax和subjectTax都获得后跳出
		boolean flagSubject = false; //当queryTax和subjectTax都获得后跳出

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
	     Date currentDate = new Date(); //得到当前系统时间
	     String blastDate = formatDate.format(currentDate); //将日期时间格式化
	     ArrayList<String[]> lsResult=new ArrayList<String[]>();
		while ((content=reader.readLine())!=null) 
		{
			String[] ss=content.split("\t");
			String[] tmpresult=new String[9];
			tmpresult[0]=ss[0];tmpresult[1]=queryTax+"";tmpresult[2]=queryDB;
			if (getGenID) {
				//TODO : 对ss[1]的处理
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
	 * 	 * 现在有blast结果，A和B
	 * 现在需要将A和B合并，合并为A的queryID与B的subjectID以及后续结果
	 * 例子是用agilent blast affyID 后，获得了A： agilent2Affy的值
	 * 然后还有B：affyPig2Hum的值,affyPig2Hum已经整理为Blast2Info的样式
	 * 那么想要获得agilent2Hum的值
	 * <b>内部A2B的blast有一些参数可以调整</b>
	 * @throws Exception 
	 * @param blastFile
	 * @param Bblast
	 * @param Bfile B物种的序列文件，已经修正为标准fasta格式
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
				//如果一个agilent探针对应了两个不同的affyID,将长的那一条装入hash表中
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
			//一个affyID有两个agilentID对应，那么这两个agilentID用"//"隔开
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
			//将B2Human文本中的所有B项替换成A文本里的ID。
			//因为一个Bid可能对应多个Aid，那么这里的话一个B对应了 A1//A2//A3
			//所以就用split("//")将其切割然后每一个一行这么导进去
			if (hashB2A.containsKey(ss[0]))
			{
				String tmpAid=hashB2A.get(ss[0]);//将A替换过来
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
	 * 现在有blast结果，agilent2RefSeq,将结果整理为 taxID \t geneID \t accID \t DBinfo \n的格式<br>
	 * <b>内部A2B的blast有一些参数可以调整</b><br>
	 * 注意输出结果中的geneID可能： geneID//geneID//geneID<br>
	 * 那么在后续导入数据库的过程中需要将geneID分开然后分别导入数据库<br>
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
		
		//保存探针2geneID的信息，key：探针，value：0：geneID 1：identity 2：evalue
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
					//agilent的参数
//					Integer.parseInt(ss[3])>50//blast上的个数
//					&&
//					Integer.parseInt(ss[4])+Integer.parseInt(ss[5])<3//错配加上gap
					//affy的参数
					Double.parseDouble(ss[2])>=90//blast identity
					&&
					Integer.parseInt(ss[4])<6 //错配数
					&&
					Integer.parseInt(ss[5])<3 //gap数
			) 
			{
				if (getGenID) 
					geneID = hashAcc2GenID.get(CopeID.getBlastAccID(ss[1]));
				else 
					geneID = ss[1];
				
				
				if (hashA2geneID.containsKey(ss[0])){
					String[] geneIDInfo = hashA2geneID.get(ss[0]);
					if (Double.parseDouble(geneIDInfo[1])<Double.parseDouble(ss[2]) //identity变大了
							&& 
							Double.parseDouble(geneIDInfo[2])>Double.parseDouble(ss[10]) //evalue变小了
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
				hashA2geneID.put( ss[0],tmpgeneIDInfo);//注意这里key是B，value是A
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
