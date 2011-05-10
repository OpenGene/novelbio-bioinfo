package com.novelbio.chIPSeq.readsChrDensity;

import com.novelBio.base.dataOperate.TxtReadandWrite;

public class runReadsInLoc {
	//给定一个区域，画出该区域内reads的具体分布情况
	public static void main(String[] args) 
	{
		///**mouse
		String parentFile="/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mCE/";
		String mapFFile=parentFile+"Ffragment.fasta";
		String mapRFile="";
		
		String chrFilePath="/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/chromFa";
		//**/
		/**human
		String mapFile="/media/winG/NBC/Program/ChIP-Seq-WJK100909/mapping/fragment_tab.fasta";
		String chrFilePath="/media/winG/bioinformation/GenomeData/HumanUCSChg19/ChromFa";
		**/
		String sep="\t"; 
		int invNum=2;
		int tagLength=300;
		String rworkSpaceString="/media/winE/Bioinformatics/R/practice_script/platform";
		ReadsDensity readsLoc =new ReadsDensity();
		readsLoc.prepare(mapFFile,mapRFile,chrFilePath,rworkSpaceString, sep, 0, 1, 2, invNum,tagLength);
		double[] readsDensity = readsLoc.getLocReadsFigure("chr14", 32836868, 32846478, 4);
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter("/media/winE/NBC/Project/ChIPSeq_CDG101101/mapping/mCE/readsLoc.txt", true, false);
		try {
			txtReadandWrite.Rwritefile(readsDensity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
