import org.springframework.data.mongodb.core.MongoTemplate;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;


public class TestDuplicateDataSource {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MongoTemplate mongoTemplate;
		MongoTemplate mongoTemplate2;
		
		mongoTemplate = (MongoTemplate)SpringFactoryBioinfo.getFactory().getBean("mongoTemplate");
		mongoTemplate2 = (MongoTemplate)SpringFactoryBioinfo.getFactory().getBean("mongoTemplate2");
		
		BlastInfo a = new BlastInfo();
		a.setBlastFileId("111111111111111");
		
		mongoTemplate.save(a);
		System.out.println(a.getId());
		
		BlastInfo b = new BlastInfo();
		b.setBlastFileId("234567890");
		mongoTemplate2.save(b);
		
		System.out.println(b.getId());
	}
}
