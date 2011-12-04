package com.novelbio.analysis.seq.snpNCBI;

import java.util.ArrayList;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrSnpIndel;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.TxtReadandWrite;

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
		
		
//		ArrayList<String> lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_BWA_SNPrecal_IndelFiltered.vcf");
//		TxtReadandWrite txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_Result.txt", true);
//		txtOut.writefile(lsResult);
//		
//		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_BWA_SNPrecal_IndelFiltered.vcf");
//		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_Result.txt", true);
//		txtOut.writefile(lsResult);
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA_SNPrecal_IndelFiltered.vcf");
		 txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_Result.txt", true);
		txtOut.writefile(lsResult);
		
		 lsResult = snpgatKcope.copeGATKsnp(9606, "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_BWA_SNPrecal_IndelFiltered.vcf");
		txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_Result.txt", true);
		txtOut.writefile(lsResult);
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
			lsResult.add(mapInfoSnpIndel.toString());
		}
		lsResult.add(0,MapInfoSnpIndel.getMyTitle());
		return lsResult;
		
		
		
	}
	
	
}
