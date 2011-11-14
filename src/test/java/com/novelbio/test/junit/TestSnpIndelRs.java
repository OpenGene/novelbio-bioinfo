package com.novelbio.test.junit;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.service.servgeneanno.ServSnpIndelRs;
@ContextConfiguration("classpath:spring.xml")
public class TestSnpIndelRs extends AbstractJUnit38SpringContextTests{
    
//	static ApplicationContext ctx;
//	 static BeanFactory factory;
//	 static MapRctInteract mapRctInteract = null;
//	 static{
//		 ctx = new ClassPathXmlApplicationContext("springTest.xml");
//			factory = (BeanFactory) ctx;
//			mapRctInteract = (MapRctInteract) ctx.getBean("mapRctInteract");
//	 }
@Before
public void before()
{

}

//public static void main(String[] args) {
//	NCBIID re = new NCBIID();
//	re.setAccID("tp53"); re.setTaxID(9606);
////	re = info.mapNCBIID.queryNCBIID(re);
//    System.out.println(re.getDBInfo());
//    assertEquals("aaa", re.getDBInfo());
//}

private ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();

    @Test
    public void testGetAccount() {
    	SnpIndelRs re = new SnpIndelRs();
		re.setSnpRsID("rs71259955");
//		re.setStrand("+");
    	ArrayList<SnpIndelRs> lsncbiid = servSnpIndelRs.queryLsSnpIndelRs(re);
//        System.out.println(re.getDbInfo1());
        assertEquals("aaa", lsncbiid.get(0).getSnpRsID());
    }


}
