package com.novelbio.test.junit;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.react.RctInteract;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
@ContextConfiguration("classpath:spring.xml")
public class TestSpring extends AbstractJUnit38SpringContextTests{
    
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

//@Inject
private MapNCBIID mapNCBIID;
private ServNCBIID servGeneAnno = new ServNCBIID();



private static TestSpring info; 
@PostConstruct
public void init()
{
    info = this;
    info.mapNCBIID = this.mapNCBIID;
}

//    @Inject
//    private MapRctInteract mapRctInteract;

    public static void main(String[] args) {
    	NCBIID re = new NCBIID();
		re.setAccID("tp53");
    	ArrayList<NCBIID> lsncbiid = info.mapNCBIID.queryLsNCBIID(re);
        System.out.println(lsncbiid.get(0).getDBInfo());
//        assertEquals("aaa", re.getDbInfo1());
	}
    @Test
    public static void testGetAccount() {
    	 
        assertEquals("human", CopedID.getSpeciesTaxIDName().get(9606));
    }

//    @Test
//    public void testAServGetAccount() {
////    	servGeneAnno = new ServGeneAnno();
//    	NCBIID re = new NCBIID();
//		re.setAccID("tp53");
//    	ArrayList<NCBIID> lsncbiid = servGeneAnno.queryLsNCBIID(re);
////        System.out.println(re.getDbInfo1());
//        assertEquals("Symbol", lsncbiid.get(0).getDBInfo());
//    }

}
