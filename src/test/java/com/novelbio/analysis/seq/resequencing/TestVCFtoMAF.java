package com.novelbio.analysis.seq.resequencing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.babraham.FastQC.Sequence.FastQFile;
import junit.framework.TestCase;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.resequencing.MAFFile;
import com.novelbio.base.fileOperate.FileOperate;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class TestVCFtoMAF extends TestCase{
	public static void main(String[] args) {
	String filePath = "//home//novelbio//test_4.vcf";
	VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(filePath), false);
	Iterator<VariantContext> it = vcfFileReader.iterator();
	MAFFile mafFile = new MAFFile();
	
	while(it.hasNext()) {
		VariantContext variantContext = it.next();
		String mafTest = "Unknown\t0\tNovelBio\tGRCh37\t1\t1991014\t1991014\t+\tNon\tSNP\tA\tA\tG\t \t \tTUMOR\tNORMAL\tA\tG\t \t \t \t \tUnknown\tInvalid\tSomatic\t \tWGS\tNo\t \tIllumina HiSeq\tTUMOR\tNORMAL";
		
		assertEquals(mafTest, mafFile.VCFtoMAFString(variantContext));
	}
	String mafResult = "//home//novelbio//test_4.maf";
	mafFile.VcfToMAFFile(filePath,mafResult);

}
	
	
}
