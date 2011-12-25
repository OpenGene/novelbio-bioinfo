package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServGo2Term  extends AbsGetSpring implements MapGo2Term{
	@Inject
	private MapGo2Term mapGo2Term;
	public ServGo2Term()  
	{
		mapGo2Term = (MapGo2Term) factory.getBean("mapGo2Term");
	}
	
	@Override
	public Go2Term queryGo2Term(Go2Term queryGo2Term) {
		return mapGo2Term.queryGo2Term(queryGo2Term);
	}

	@Override
	public ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term) {
		return mapGo2Term.queryLsGo2Term(queryGo2Term);
	}

	@Override
	public void insertGo2Term(Go2Term Go2Term) {
		mapGo2Term.insertGo2Term(Go2Term);
	}

	@Override
	public void updateGo2Term(Go2Term Go2Term) {
		mapGo2Term.updateGo2Term(Go2Term);
	}

}
