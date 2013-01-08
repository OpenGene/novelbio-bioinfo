package com.novelbio.test.junit;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServTaxID;
//@ContextConfiguration("classpath:spring.xml")
public class TestSpring extends TestCase{

@Before
public void before()
{

}

    @Test
    public static void testGetAccount() {
    	 
//        assertEquals("human", CopedID.getSpeciesTaxIDName().get(9606));
    }

    @Test
    public void testAServGetAccount() {
//    	servGeneAnno = new ServGeneAnno();
    	ServTaxID servTaxID = new ServTaxID();
		HashMap<Integer, String> hashID = servTaxID.getHashTaxIDName();
		assertEquals("tobacco", hashID.get(4097));
    }

}
