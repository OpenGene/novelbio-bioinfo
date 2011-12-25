package com.novelbio.analysis.seq.snpNCBI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.broadinstitute.sting.utils.exceptions.StingException;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrSnpIndel;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SNPGATKcope {
	GffChrSnpIndel gffChrSnpIndel;
	public SNPGATKcope() {
		gffChrSnpIndel = new GffChrSnpIndel(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
	}

	public static void main(String[] args) {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		
		ArrayList<String> lsResult = new ArrayList<String>();
		TxtReadandWrite txtOut = new TxtReadandWrite();
		
		
		lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_BWA_SNPrecal_IndelFiltered.vcf");
		txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_Result2.xls", true);
		txtOut.writefile(lsResult);
//		
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_BWA_SNPrecal_IndelFiltered.vcf");
		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_Result2.xls", true);
		txtOut.writefile(lsResult);
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA_SNPrecal_IndelFiltered.vcf");
		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_Result2.xls", true);
		txtOut.writefile(lsResult);
		
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_BWA_SNPrecal_IndelFiltered.vcf");
		txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_Result2.xls", true);
		txtOut.writefile(lsResult);
		
		String Parent = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		String A = Parent + "A_Result2.xls";
		String B = Parent + "B_Result2.xls";
		String C = Parent + "C_Result2.xls";
		String D = Parent + "D_Result2.xls";
		snpgatKcope.writeAllSnp("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/novelbio/Allsnp", A,B,C,D);
	}
	
	
	
	/**
	 * ��gatk����vcf�ļ��У�random��chrȫ��ɾ��
	 */
	public ArrayList<String> copeGATKsnp(int taxID, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		ArrayList<String> lsResult = new ArrayList<String>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith("#")) continue;
			String[] ss = string.split("\t");

			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(taxID, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
			mapInfoSnpIndel.setBaseInfo(ss[7]);
			mapInfoSnpIndel.setQuality(ss[5]);
			mapInfoSnpIndel.setFlag(ss[8], ss[9]);
			mapInfoSnpIndel.setFilter(ss[6]);
			if (!ss[2].equals(".")) {
				mapInfoSnpIndel.setDBSnpID(ss[2]);
			}
			gffChrSnpIndel.getSnpIndel(mapInfoSnpIndel);
			lsResult.add(mapInfoSnpIndel.toString());
		}
		lsResult.add(0,MapInfoSnpIndel.getMyTitle());
		return lsResult;
	}
	
	public void writeAllSnp(String outFile, String... txtFile) {
		ArrayList<ArrayList<String[]>> lsAll = new ArrayList<ArrayList<String[]>>();
		ArrayList<String> lsTag = new ArrayList<String>();
		for (String string : txtFile) {
			lsTag.add(FileOperate.getFileName(string));
			lsAll.add(ExcelTxtRead.readLsExcelTxt(string, 1, 0, 1, 0));
		}
		getSnpAll(lsTag, lsAll, 0, 1, "_//@@//_", outFile);
		
	}
	
	
	public void getSnpAll(ArrayList<String> tag, ArrayList<ArrayList<String[]>> ls, int colChrID, int colStartLoc, String sep,String txtOut)
	{
		HashMap<String, HashMap<String, String[]>> hashInfo = new HashMap<String, HashMap<String,String[]>>();
		for (int i = 0; i < tag.size(); i++) {
			HashMap<String, String[]> hashLoc2Info = name(colChrID, colStartLoc, sep, ls.get(i));
			hashInfo.put(tag.get(i), hashLoc2Info);
		}
		ArrayList<String[]> lsLocAll = getAllSNP(colChrID, colStartLoc, sep, ls);
		for (int i = 0; i < tag.size(); i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(txtOut, "_"+tag.get(i) +"_Allsnp", "xls"), true);
			HashMap<String, String[]> hashTmpSnp = hashInfo.get(tag.get(i));
			for (String[] strings : lsLocAll) {
				String[] tmpSnp = hashTmpSnp.get(strings[0] + sep +strings[1]);
				if (tmpSnp == null) {
					String[] tmp = new String[ls.get(0).get(0).length];
					for (int j = 0; j < tmp.length; j++) {
						tmp[j] = "";
					}
					tmp[0] = strings[0];
					tmp[1] = strings[1];
					txtWrite.writefileln(tmp);
				}
				else {
					txtWrite.writefileln(tmpSnp);
				}
			}
			txtWrite.close();
		}
	}
	
	
	
	
	
	/**
	 * 
	 * ����һ��snp��װ��hash����
	 * @param ls
	 * @param sep �ָ�chrID��startLoc�ķ���
	 * @return
	 */
	public HashMap<String, String[]> name(int colChrID, int colStartLoc, String sep, ArrayList<String[]> ls) {
		HashMap<String, String[]> hashResult = new HashMap<String, String[]>();
		for (String[] strings : ls) {
			String key = strings[colChrID] + sep + strings[colStartLoc];
			if (hashResult.containsKey(key)) {
				System.out.println("error");
			}
			hashResult.put(key, strings);
		}
		return hashResult;
	}
	
	/**
	 * �������snp��λ�㣬��������
	 * @param colChrID
	 * @param colStartLoc
	 * @param sep �ָ�chrID��startLoc�ķ���
	 * @param ls
	 * @return
	 */
	private ArrayList<String[]> getAllSNP(int colChrID, int colStartLoc, String sep,ArrayList< ArrayList<String[]>> ls)
	{
		HashSet<String> hashNoRedunt = new HashSet<String>();
		ArrayList<String[]> lsTmpResult = new ArrayList<String[]>();
		//ȥ����ļ���arraylist
		for (List<String[]> list : ls) {
			for (String[] strings : list) {
				String tmp = strings[colChrID] + sep + strings[colStartLoc];
				if (hashNoRedunt.contains(tmp)) {
					continue;
				}
				hashNoRedunt.add(tmp);
				String[] tmpInfo = new String[]{strings[colChrID], strings[colStartLoc]};
				lsTmpResult.add(tmpInfo);
			}
		}
		//����
		Collections.sort(lsTmpResult, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				if (o1[0].compareTo(o2[0]) == 0) {
					Integer a = Integer.parseInt(o1[1]);
					Integer b = Integer.parseInt(o2[1]);
					return a.compareTo(b);
				}
				return o1[0].compareTo(o2[0]);
			}
		});
		return lsTmpResult;
	}
	
	
	
	
	
	
	
	
	
	
}