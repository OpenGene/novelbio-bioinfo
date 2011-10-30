package com.novelbio.database.servSpring;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.mapper.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
//@ContextConfiguration("classpath:spring.xml")
@Service
public class ServNCBIID extends AbsGetSpring{
	
	@Autowired
	private MapNCBIID mapNCBIID;
	/**
	 * 		where <br>
			if test="taxID !=0 and taxID !=null"<br>
				and taxID = #{taxID} <br>
			/if<br>
			if test="geneID1 !=null"<br>
				geneID1 = #{geneID1} <br>
			/if<br>
			if test="geneID2 !=null"<br>
				geneID2 = #{geneID2} <br>
			/if<br>
			if test="dbInfo1 !=null"<br>
				dbInfo1 = #{dbInfo1} <br>
			/if<br>
			if test="dbInfo2 !=null"<br>
				dbInfo2 = #{dbInfo2} <br>
			/if<br>
	    /where <br>

	 * @param rctInteract
	 * @return
	 */
	public NCBIID qRctIntact(NCBIID rctInteract) {
//		ApplicationContext ctx;
//		BeanFactory factory;
//		ctx = new ClassPathXmlApplicationContext("spring.xml");
//		factory = (BeanFactory) ctx;
//		mapNCBIID = (MapNCBIID) factory.getBean("mapNCBIID");
		return mapNCBIID.queryNCBIID(rctInteract);
	}

	
	
}
