package com.novelbio.database.model.modgeneid;

import java.util.List;

import junit.framework.TestCase;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

public class TestManageBlast extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void test() {
		ManageBlastInfo manageBlastInfo = ManageBlastInfo.getInstance();
		String blastFileName = "/home/zong0jie/桌面/blastInfoTest.txt";
//		ManageBlastInfo.getInstance().re(456, 123456, "/home/zong0jie/桌面/blastInfoTest.txt");
		GeneID geneID = new GeneID("other1", 456);
		assertEquals("12345678903", geneID.getLsBlastGeneID().get(0).getGeneUniID());
		
		geneID = new GeneID("Other2", 456);
		assertEquals("1234567890", geneID.getLsBlastGeneID().get(0).getGeneUniID());
		
		geneID.setBlastInfo(1e-5, 123456);
		List<AGene2Go> lsAGene2Gos = geneID.getGene2GOBlast(GOtype.BP);
		assertEquals(2, lsAGene2Gos.size());
		manageBlastInfo.getFileInfoAll();
		List<BlastFileInfo> ls = manageBlastInfo.queryBlastFile(blastFileName, "456", 123456);
		manageBlastInfo.removeBlastFile(ls.get(0));
	}
	
}
