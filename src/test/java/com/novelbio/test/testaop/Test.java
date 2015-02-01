package com.novelbio.test.testaop;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.database.service.SpringFactoryBioinfo;


public abstract class Test {
	int  i = 0;
	public static void main(String[] args) {
		Test2 test = (Test2)SpringFactoryBioinfo.getFactory().getBean("test2");
//		testAop.before();
//		Test test = new Test();
		test.run("a", "m", "k");
		
		Test2 test2 = (Test2)SpringFactoryBioinfo.getFactory().getBean("test2");
//		testAop.before();
//		Test test = new Test();
		test2.run("b",  "mb", "kb");
		
//		CtrlGO test = (CtrlGO)SpringFactory.getFactory().getBean("ctrlGO");
//		test.saveExcel("sss");
		
	}
	public abstract void run(String mmm1, String mmm, String kkk);
	
	
}

@Component
@Scope("prototype")
class Test2 extends Test {
	int mm = 0;
	public int getMm() {
		return mm;
	}
	public void run(String mmm1, String mmm, String kkk) {
		i++;
		System.out.println(i);
		System.out.println(mmm1);
	}
}

@Component
@Scope("prototype")
class Test3 extends Test {
	String kkString;
	public String getKkString() {
		return kkString;
	}
	public void run(String mmm1, String mmm, String kkk) {
		i++;
		System.out.println(i);
		System.out.println(mmm1);
	}
}
