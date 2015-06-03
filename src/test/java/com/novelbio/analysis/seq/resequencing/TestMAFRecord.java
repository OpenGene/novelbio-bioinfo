package com.novelbio.analysis.seq.resequencing;
import java.util.Iterator;

import junit.framework.TestCase;

import com.novelbio.base.fileOperate.FileOperate;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class TestMAFRecord extends TestCase{
	public static void main(String[] args) {
	String filePath = "//home//novelbio//test_4.vcf";
	VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(filePath), false);
	Iterator<VariantContext> it = vcfFileReader.iterator();
	
	while(it.hasNext()) {
		VariantContext variantContext = it.next();
		String mafTest = "Unknown\t0\tNovelBio\tGRCh37\t1\t1991014\t1991014\t+\tFrame_Shift_Del\tSNP\tA\tA\tG\tnovel\tby1000Genomes\tTUMOR\tNORMAL\tA\tG\t-\t-\t-\t-\tUnknown\tInvalid\tSomatic\tNo\tWGS\tNo\tNo\tNo\tIlluminaHiSeq\tTUMOR\tNORMAL";
		MAFRecord mafRecord = new MAFRecord (variantContext);
		assertEquals(mafTest, mafRecord.toString());
	}
}
}
