package com.novelbio.database.omim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import com.novelbio.database.domain.omim.GeneMIM;
import com.novelbio.database.domain.omim.MIMInfo;
import com.novelbio.database.domain.omim.MorbidMap;

public class TestOmimUnit {

	public static void main(String[] args) {
		
		TestOmimUnit testOmimUnit = new TestOmimUnit();
		
//		testOmimUnit.testOmimUit();
		
//		testOmimUnit.testCreatMorbidMapTable();
		
		testOmimUnit.testGeneMIM();
		
	}
	
	public void testOmimUit() {
		
		List<String> lsOmimUnit = new ArrayList<String>();
		lsOmimUnit.add("*FIELD* NO");
		lsOmimUnit.add("100050");
		lsOmimUnit.add("*FIELD* TI");
		lsOmimUnit.add("%100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT");
		lsOmimUnit.add("*FIELD* TX");
		lsOmimUnit.add("aaaaaaaaaaaaaa");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("DESCRIPTION");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("Aarskog syndrome is characterized by short stature and facial, limb, and");
//		lsOmimUnit.add("genital anomalies. One form of the disorder is X-linked (see 305400),");
//		lsOmimUnit.add("but there is also evidence for autosomal dominant and autosomal");
//		lsOmimUnit.add("recessive (227330) inheritance (summary by Grier et al., 1983).");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("CLINICAL FEATURES");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("Grier et al. (1983) reported father and 2 sons with typical Aarskog");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("*FIELD* RF");
		lsOmimUnit.add("1. Grier, R. E.; Farrington, F. H.; Kendig, R.; Mamunes, P.: Autosomal");
		lsOmimUnit.add("2. van de Vooren, M. J.; Niermeijer, M. F.; Hoogeboom, A. J. M.:");
		lsOmimUnit.add("3. Welch, J. P.: Elucidation of a 'new' pleiotropic connective tissue");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("*FIELD* CS");
		lsOmimUnit.add("Growth:");
		lsOmimUnit.add("Mild to moderate short stature");
		lsOmimUnit.add("\n");
		lsOmimUnit.add("*FIELD* CD");
		lsOmimUnit.add("Victor A. McKusick: 6/4/1986");
		lsOmimUnit.add("\n");
		MIMInfo mIMInfo = MIMInfo.getInstanceFromOmimUnit(lsOmimUnit);
		List<String> listRef = new ArrayList<String>();
		listRef.add("1. Grier, R. E.; Farrington, F. H.; Kendig, R.; Mamunes, P.: Autosomal");
		listRef.add("2. van de Vooren, M. J.; Niermeijer, M. F.; Hoogeboom, A. J. M.:");
		listRef.add("3. Welch, J. P.: Elucidation of a 'new' pleiotropic connective tissue");
		Assert.assertEquals(100050, mIMInfo.getMimId());
		Assert.assertEquals("AARSKOG SYNDROME, AUTOSOMAL DOMINANT", mIMInfo.getMimTitle());
		Assert.assertEquals("aaaaaaaaaaaaaa", mIMInfo.getMimTxt());
		Assert.assertEquals('%', mIMInfo.getType());
		Assert.assertEquals(listRef, mIMInfo.getListRef());
//		Assert.assertEquals("Aarskog syndrome is characterized by short stature and facial, limb, and genital anomalies. One form of the disorder is X-linked (see 305400), but there is also evidence for autosomal dominant and autosomal recessive (227330) inheritance (summary by Grier et al., 1983).", mIMInfo.getDesc());
		
		
		HashMap<String, String> maMimUni = new HashMap<String, String>();
		maMimUni.put("TX", "aaaaaaaaaaaaaa");
		maMimUni.put("CLINICAL FEATURES", "Grier et al. (1983) reported father and 2 sons with typical Aarskog");
//		maMimUni.put("DESCRIPTION", "Aarskog syndrome is characterized by short stature and facial, limb, and");

//		Assert.assertEquals(maMimUni, (HashMap<String, String>) mIMInfo.getMapTitle2Info());
		

	}
	
	public void testCreatMorbidMapTable() {
		String content = "17,20-lyase deficiency, isolated, 202110 (3)|CYP17A1, CYP17, P450C17|609300|10q24.32";
		String content2 = "17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)|CYP17A1, CYP17, P450C17|609300|10q24.32";
		MorbidMap morbidMap = MorbidMap.getInstanceFromOmimRecord(content2);
		Assert.assertEquals(0, morbidMap.getGeneId());
		Assert.assertEquals(609300, morbidMap.getGeneMimId());
		Assert.assertEquals(202110, morbidMap.getPhenMimId());
		Assert.assertEquals("10q24.32", morbidMap.getCytLoc());
		
		List<String> liDis = new ArrayList<>();
		liDis.add("17-alpha-hydroxylase/17");
		liDis.add("20-lyase deficiency");
		Assert.assertEquals(liDis, morbidMap.getListDis());
	}
	
	public void testGeneMIM() {
		String content = "1.1|5|13|13|1pter-p36.13|CTRCT8, CCV|P|Cataract, congenital, Volkmann type|115665|Fd|linked to Rh in Scottish family|Cataract 8, multiple types (2)||";
		GeneMIM geneMIM = GeneMIM.getInstanceFromGeneOmim(content);
		Assert.assertEquals(7792, geneMIM.getGeneId());
		Assert.assertEquals(115665, geneMIM.getGeneMimId());
		Assert.assertEquals(115665, geneMIM.getUniMimId());
		Assert.assertEquals("1pter-p36.13", geneMIM.getCytLoc());
	}
	
}
