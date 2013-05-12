package com.novelbio.test.testaop;

import java.lang.reflect.Method;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
@Component 
@Aspect 
public class TestAop {

	 @Pointcut("execution (* com.novelbio.test.testaop.Test.run(..)) && args(mmm1, mmm, ..) && target(bean)")
	 public void logRun(String mmm1, String mmm, Test bean) {}
	 
	 @Before("logRun( mmm1,  mmm,  bean)")  
	    public void before(String mmm1, String mmm, Test bean) {  
		 mmm1 = mmm1 + "ssssseee";
	        System.out.println("before");
	        System.out.println(mmm1 + "aop");
			Test test = (Test)bean;
			if (test instanceof Test2) {
				System.out.println(test.i + " iNumber Test2");
			}
			
	    }
	 
	 	@After("logRun( mmm1,  mmm,  bean)")  
	    public void afterCope(String mmm1, String mmm, Test bean) {  
	        System.out.println("before");  
	        System.out.println(mmm1 + "aopAfter");
			Test test = (Test)bean;
			System.out.println(test.i + " iNumberAfter");
	    }
}
