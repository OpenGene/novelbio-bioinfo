package com.novelBio.chIPSeq.peakAnnotation.symbolAnnotation;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;

public class SymbolDespRun {

	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		
		
		//String symbolFile="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/refToSymbleDiscription20100812.txt";
		/**
		String excelFile="/media/winG/NBC/Project/ChIP-Seq-WJK100909/result/annotation/PeakAnnotation.xls";
		String symbolFile="/media/winG/bioinformation/GenomeData/HumanUCSChg19/Hg19_refseqToSymble.txt";
		**/

		
		SymbolDesp cdg=new SymbolDesp();
		/**
		if (cdg.hashRefDetail==null) {
			try {
				cdg.readSymbolFile(symbolFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		**/
		String parentFile = "/media/winE/NBC/Project/ChIPSeq_CDG110225/result/annotation/";
		
		try {    
			String txtFile=parentFile+"k0_annotation.xls";
			TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
			txtReadandWrite.setParameter(txtFile, false, true);
			int columnNum=0;
			try {
				columnNum = txtReadandWrite.ExcelColumns("\t");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int columnRead=columnNum-1;//19//24
			//int ColumnWrite=columnRead+1;
			
			int rowStart=2;

			cdg.getRefSymbDesp(10090,txtFile, columnRead, rowStart, columnRead);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-2, rowStart, columnRead-2);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {   e.printStackTrace();   }
		try {    
			
			String txtFile=parentFile+"k4_annotation.xls";
			TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
			txtReadandWrite.setParameter(txtFile, false, true);
			int columnNum=0;
			try {
				columnNum = txtReadandWrite.ExcelColumns("\t");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int columnRead=columnNum-1;//19//24
			//int ColumnWrite=columnRead+1;
			
			int rowStart=2;

			cdg.getRefSymbDesp(10090,txtFile, columnRead, rowStart, columnRead);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-2, rowStart, columnRead-2);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {   e.printStackTrace();   }
try {    
			
			String txtFile=parentFile+"W0_annotation.xls";
			TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
			txtReadandWrite.setParameter(txtFile, false, true);
			int columnNum=0;
			try {
				columnNum = txtReadandWrite.ExcelColumns("\t");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int columnRead=columnNum-1;//19//24
			//int ColumnWrite=columnRead+1;
			
			int rowStart=2;

			cdg.getRefSymbDesp(10090,txtFile, columnRead, rowStart, columnRead);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-2, rowStart, columnRead-2);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {   e.printStackTrace();   }
try {    
			
			String txtFile=parentFile+"W4_annotation.xls";
			TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
			txtReadandWrite.setParameter(txtFile, false, true);
			int columnNum=0;
			try {
				columnNum = txtReadandWrite.ExcelColumns("\t");
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int columnRead=columnNum-1;//19//24
			//int ColumnWrite=columnRead+1;
			
			int rowStart=2;

			cdg.getRefSymbDesp(10090,txtFile, columnRead, rowStart, columnRead);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-2, rowStart, columnRead-2);
			cdg.getRefSymbDesp(10090,txtFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {   e.printStackTrace();   }
		System.out.println("ok");

	}

}
