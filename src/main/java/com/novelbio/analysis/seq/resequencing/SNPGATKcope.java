package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.genomeNew.GffChrSnpIndel;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 读取GATK的结果文件，然后标记每个snp的信息，所在基因等等
 * @author zong0jie
 *
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrSnpIndel gffChrSnpIndel;
	public SNPGATKcope() {
	}

	public static void main22(String[] args) {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setDomainInfo("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinalNew/AllsnpCoped.txt",
				"/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/pfam/pfamInfo.txt", "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinalNew/Allsnp_pfam.xls");
	}
	public static void main(String[] args) {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		
		ArrayList<String> lsResult = new ArrayList<String>();
		TxtReadandWrite txtOut = new TxtReadandWrite();
		
		lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_BWA_SNPrecal_IndelFiltered.vcf");
		txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_Result_New.xls", true);
		txtOut.writefile(lsResult);
		
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_BWA_SNPrecal_IndelFiltered.vcf");
		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_Result_New.xls", true);
		txtOut.writefile(lsResult);
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA_SNPrecal_IndelFiltered.vcf");
		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_Result_New.xls", true);
		txtOut.writefile(lsResult);
		
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_BWA_SNPrecal_IndelFiltered.vcf");
		txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_Result_New.xls", true);
		txtOut.writefile(lsResult);
		
//		String Parent = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinalNew/";
//		String A = Parent + "A_Result2.xls";
//		String B = Parent + "B_Result2.xls";
//		String C = Parent + "C_Result2.xls";
//		String D = Parent + "D_Result2.xls";
//		String out = Parent + "all.xls";
//		snpgatKcope.writeAllSnp(out, A,B,C,D);
	}
	/**
	 * 将gatk里面vcf文件中，random的chr全部删除
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
			try {
				lsResult.add(mapInfoSnpIndel.toString());
			} catch (Exception e) {
				logger.error("本位点出错：" + ss[1]);
			}
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
	 * 给定一组snp，装入hash表中
	 * @param ls
	 * @param sep 分隔chrID和startLoc的符号
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
	 * 获得所有snp的位点，并且排序
	 * @param colChrID
	 * @param colStartLoc
	 * @param sep 分隔chrID和startLoc的符号
	 * @param ls
	 * @return
	 */
	private ArrayList<String[]> getAllSNP(int colChrID, int colStartLoc, String sep,ArrayList< ArrayList<String[]>> ls)
	{
		HashSet<String> hashNoRedunt = new HashSet<String>();
		ArrayList<String[]> lsTmpResult = new ArrayList<String[]>();
		//去冗余的加入arraylist
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
		//排序
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
	
	
	/**
	 * 给定文本，和domain信息，获得具体domain的信息
	 * @param txtExcelSNP
	 * @param domainFile
	 * @param outFile
	 */
	public void setDomainInfo(String txtExcelSNP, String domainFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		DomainPfam.readDomain(domainFile);
		ArrayList<String[]> lsSnp = ExcelTxtRead.readLsExcelTxt(txtExcelSNP, 1);
		for (int i = 1; i < lsSnp.size(); i++) {
			String[] ss = lsSnp.get(i);
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(0, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
			GffCodGene gffcod = gffHashGene.searchLocation(mapInfoSnpIndel.getRefID(), mapInfoSnpIndel.getRefSnpIndelStart());
			String tmp = "";
			if (gffcod.isInsideLoc()) {
				GffDetailGene gffDetailGene = gffcod.getGffDetailThis();
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					DomainPfam domainPfam = DomainPfam.getDomainPfam(gffGeneIsoInfo.getName());
					if (domainPfam == null) {
						continue;
					}
					domainPfam.setAALoc(gffGeneIsoInfo.getCod2ATGmRNA(gffcod.getCoord())/3);
					tmp = domainPfam.toString();
					break;
				}
				
			}
			String result = ArrayOperate.cmbString(ss, "\t");
			result = result + "\t" + tmp;
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
}
