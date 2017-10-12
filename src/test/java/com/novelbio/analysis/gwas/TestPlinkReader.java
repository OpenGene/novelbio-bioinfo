package com.novelbio.analysis.gwas;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestPlinkReader {
	
	@Test
	public void testCreatePlinkPedIndex() throws IOException {
		String testPlinkPed = PathDetail.getTmpPathWithSep() + "testPlinkPed";
		TxtReadandWrite txtWrite = new TxtReadandWrite(testPlinkPed, true);
		txtWrite.writefile("IRIS_-43 IR_3590 0 0 0 -9 G G T T A A T T T T A A C\n");
		txtWrite.writefile("IRI910 IRI3-11 0 0 0 -9 G G T T A A T T T T A A C\r\n");
		txtWrite.writefile("IR23S_0 IRIS_31910 0 0 0 -9 G G T T A A T T T T A A C\r");
		txtWrite.writefile("IRIS_313-10 IR10 0 0 0 -9 G G T T A A T T T T A A C\n");
		txtWrite.writefile("IR_3-1r10 IrRIS_310 0 0 0 -9 G G T T A A T T T T A A C\n");
		txtWrite.close();
		
		List<String[]> lsIndexes = PlinkPedReader.createPlinkPedIndex(testPlinkPed);
		Assert.assertArrayEquals(new String[]{"IRIS_-43", "0", "26"}, lsIndexes.get(0));
		Assert.assertArrayEquals(new String[]{"IRI910", "52", "76"}, lsIndexes.get(1));
		Assert.assertArrayEquals(new String[]{"IR23S_0", "103", "131"}, lsIndexes.get(2));
		Assert.assertArrayEquals(new String[]{"IRIS_313-10", "157", "183"}, lsIndexes.get(3));
		Assert.assertArrayEquals(new String[]{"IR_3-1r10", "209", "238"}, lsIndexes.get(4));
		
		FileOperate.deleteFileFolder(testPlinkPed);
	}
	
	
}
