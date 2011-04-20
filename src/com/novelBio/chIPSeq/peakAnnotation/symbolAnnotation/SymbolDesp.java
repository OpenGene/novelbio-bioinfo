package com.novelBio.chIPSeq.peakAnnotation.symbolAnnotation;

import java.util.ArrayList;
import java.util.Hashtable;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.ArrayOperate;

import DAO.FriceDAO.DaoFCGene2GoInfo;
import DAO.FriceDAO.DaoFSNCBIID;

import entity.friceDB.Gene2GoInfo;
import entity.friceDB.NCBIID;

public class SymbolDesp 
{
	
	static Hashtable<String, String[]> hashRefDetail=null;
	
	
	/**
	 * ��ȡrefToSybmolAnnotation�ļ�������һ��Hash��
	 * hash-key refID---value string[2]
	 * 0: symble
	 * 1: Description
	 * @param symbolFile
	 * @throws Exception
	 */
	public static Hashtable<String, String[]> readSymbolFile(String symbolFile) throws Exception 
	{
		TxtReadandWrite txtRef=new TxtReadandWrite();
		txtRef.setParameter(symbolFile, false,true);
		String[][] refDetail=txtRef.ExcelRead("\t", 2, 1, txtRef.ExcelRows(), 3);
		hashRefDetail=new Hashtable<String, String[]>();
		for (int i = 0; i < refDetail.length; i++) {		
			String tmpKey=refDetail[i][0];
			String[] tmpValueDetail=new String[2];
			tmpValueDetail[0]=refDetail[i][1];
			tmpValueDetail[1]=refDetail[i][2];
			hashRefDetail.put(tmpKey,tmpValueDetail);
		}
		return hashRefDetail;
	}
	
	/**
	 * ����RefID������ArrayList--String[3]<br>
	 * 0: refID<br>
	 * 1: Symbol<br>
	 * 2: Description<br>
	 * @param RefID Ϊstring[][1] ,�����Ϊ�����ҵ�excelread��õĽ��,����RefID����"/"�ָ�
	 *  0: RefID<br>
	 * @param hashRefDetail
	 * @return
	 */
	//private static ArrayList<String[]> refToSymbolDesp(String[][] RefID,Hashtable<String, String[]> hashRefDetail)
	private static ArrayList<String[]> refToSymbolDesp(String[][] RefID)
	{
		ArrayList<String[]> refSymDesp=new ArrayList<String[]>();
		for (int i = 0; i < RefID.length; i++)
		{
			String[] tmpresult=new String[3];
			tmpresult[1]="";tmpresult[2]="";
			tmpresult[0]=RefID[i][0];
			if (RefID[i][0]==null)
			{
				tmpresult[0]="";
			}
			if (tmpresult[0].trim().equals("")) {
				refSymDesp.add(tmpresult);
				continue;
			}
			String tmpRefID[]=tmpresult[0].split("/");
			////////////////////////////////////////ֱ�������ݿ�///////////////////////////////////////////////////
			for (int j = 0; j < tmpRefID.length; j++)
			{
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(tmpRefID[j]);
				ArrayList<Gene2GoInfo> lsGene2GoInfos = DaoFCGene2GoInfo.queryLsGeneDetail(ncbiid);
				if (lsGene2GoInfos != null && lsGene2GoInfos.size()>0 && lsGene2GoInfos.get(0).getGeneInfo() != null && lsGene2GoInfos.get(0).getGeneInfo().getSymbol() != null) 
				{
					String symbol = lsGene2GoInfos.get(0).getGeneInfo().getSymbol().split("//")[0];
					if (tmpresult[1].contains(symbol.trim())) 
					{
						continue;
					}
					else 
					{
						if (tmpresult[1].trim().equals("")) 
						{
							tmpresult[1] = symbol;
						}
						else
						{
							tmpresult[1] =tmpRefID[1] +"//" +symbol;
						}
						if (lsGene2GoInfos.get(0).getGeneInfo().getDescription() != null) 
						{
							String description = lsGene2GoInfos.get(0).getGeneInfo().getDescription();
							if (tmpresult[2].trim().equals("")) 
							{
								tmpresult[2] = description;
							}
							else
							{
								tmpresult[2] =tmpresult[2] +"//" +description;
							}
						}
					}
				}
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			}

			/**
			 * ����hash��ֱ�Ӳ������ݿ�
			 
			for (int j = 0; j < tmpRefID.length; j++)
			{
				String[] tmpStrings;
				if (hashRefDetail.containsKey(tmpRefID[j])) 
				{
					tmpStrings=hashRefDetail.get(tmpRefID[j]);
					if (tmpresult[1].contains(tmpStrings[0].trim())&&tmpresult[2].contains(tmpStrings[1].trim()))//����ظ��� 
					{
						continue;
					}
					
					if (tmpresult[1].equals("")) 
						tmpresult[1]=tmpStrings[0];
					else
						tmpresult[1]=tmpresult[1]+"/"+tmpStrings[0];
					
					if (tmpresult[2].equals("")) 
						tmpresult[2]=tmpStrings[1];
					else
						tmpresult[2]=tmpresult[2]+"/"+tmpStrings[1];
					
				}
			}
			**/
			refSymDesp.add(tmpresult);
			
		}
		return refSymDesp;
	}
	
	/**
	 * ��ȡexcel�ļ���ĳһ�У��ѽ��д���excel��ָ����
	 * @param excelFile
	 * @param symbolFile ��ȡrefToSymbleDiscription20100812.txt
	 * @param columnRead ��ȡ�ڼ��У�ʵ����
	 * @param rowStart �ӵڼ��п�ʼ����ʵ����
	 * @param ColumnWrite д��ڼ��У�ʵ����
	 * @throws Exception 
	 */
	public static void getRefSymbDesp(String txtFile,int columnRead,int rowStart,int ColumnWrite) throws Exception
	{	
		TxtReadandWrite txtReadandWrite =new TxtReadandWrite();
		txtReadandWrite.setParameter(txtFile, false, true);
		String[][] RefID2=txtReadandWrite.ExcelRead("\t", 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns("\t"));
		
		String[][] RefID=new String[RefID2.length][1];
		for (int i = 0; i < RefID.length; i++) {
			RefID[i][0]=RefID2[i][columnRead-1];
		}
		//ReadExcel(rowStart, columnRead, txtReadandWrite.getRowCount(), columnRead);
		//Hashtable<String, String[]> hashRefDetail=readSymbolFile(symbolFile);

		//ArrayList<String[]> result2=refToSymbolDesp(RefID,hashRefDetail);
		ArrayList<String[]> result2=refToSymbolDesp(RefID);
		String[][] result=new String[result2.size()][result2.get(2).length-1];
		for (int i = 0; i < result2.size(); i++) {
			String[] tmp2=result2.get(i);
			result[i][0]=tmp2[1];result[i][1]=tmp2[2];
		}
		
		String[][] resultFinal=ArrayOperate.combStrArray(RefID2, result, ColumnWrite);
		txtReadandWrite.setParameter(txtFile, true, false);
		txtReadandWrite.ExcelWrite(resultFinal, "\t");
		
	}
	
}
