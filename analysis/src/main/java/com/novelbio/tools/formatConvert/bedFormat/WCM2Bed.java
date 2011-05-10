package com.novelbio.tools.formatConvert.bedFormat;

import java.io.BufferedReader;

import com.novelBio.base.dataOperate.TxtReadandWrite;


/**
 * change Wang CongMao 's format 2 bed format
 * @author zong0jie
 *
 */
public class WCM2Bed {
	/**
	 * change Wang CongMao 's format 2 bed format
	 * @param FileWCM
	 * @param FileBed
	 * @throws Exception 
	 */
	public static void changeWCM2Bed(String FileWCM,String FileBed) throws Exception 
	{
		TxtReadandWrite txtWCM = new TxtReadandWrite();
		txtWCM.setParameter(FileWCM, false, true);
		
		TxtReadandWrite txtBed = new TxtReadandWrite();
		txtBed.setParameter(FileBed, true, false);
		
		BufferedReader reader = txtWCM.readfile();
		String content = "";String chrID = "";
		while ((content = reader.readLine()) != null)
		{
			if (content.startsWith(">")) {
				chrID = content.substring(1);
				continue;
			}
			//String[] ss = content.split("[\\t ]+");
			String[] ss = content.split("\t");
			String tmpResult = chrID + "\t" + ss[0] + "\t" +ss[1]+"\n";
			txtBed.writefile(tmpResult);
		}
		txtBed.writefile("", true);
	}
	
	/**
	 * change Wang CongMao 's format 2 bed format and into seperate files
	 * @param FileWCM
	 * @param FileBed end with "/"
	 * @param fileName will add ChrID automaticlly
	 * @throws Exception 
	 */
	public static void changeWCM2BedSep(String FileWCM,String FileBedPath,String fileName) throws Exception 
	{
		TxtReadandWrite txtWCM = new TxtReadandWrite();
		txtWCM.setParameter(FileWCM, false, true);
		
		TxtReadandWrite txtBed = new TxtReadandWrite();
	
		
		BufferedReader reader = txtWCM.readfile();
		String content = "";String chrID = "";
		while ((content = reader.readLine()) != null)
		{
			if (content.startsWith(">")) {
				chrID = content.substring(1);
				try {
					txtBed.writefile("", true); txtBed.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
				txtBed.setParameter(FileBedPath+chrID+fileName, true, false);
				continue;
			}
			//String[] ss = content.split("[\\t ]+");
			String[] ss = content.split("\t");
			String tmpResult = chrID + "\t" + ss[0] + "\t" +ss[1]+"\n";
			txtBed.writefile(tmpResult);
		}
		
	}
	
	/**
	 * cut a big bed file by the chrID into seperate files
	 * the bed file must already being sorted
	 * @param FileWCM
	 * @param FileBedPath end with "/"
	 * @param fileName 
	 * @throws Exception 
	 */
	public static void cutBedbyChrID(String bedFile,String FileBedPath,String fileName) throws Exception 
	{
		TxtReadandWrite txtWCM = new TxtReadandWrite();
		txtWCM.setParameter(bedFile, false, true);
		
		TxtReadandWrite txtBed = new TxtReadandWrite();
	
		
		BufferedReader reader = txtWCM.readfile();
		String content = "";String lastChrID = "";
		while ((content = reader.readLine()) != null)
		{
			String[] ss = content.split("\t");
			if (!ss[0].equals(lastChrID)) {
				txtBed.close();
				lastChrID = ss[0];
				txtBed.setParameter(FileBedPath+lastChrID+fileName, true, false);
			}
			txtBed.writefile(content);
		}
		txtBed.close();
	}
}
