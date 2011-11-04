package com.novelbio.database.service;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AbsGetSpring {
	static ApplicationContext ctx;
	static protected BeanFactory factory;
	static
	{
		ctx = new ClassPathXmlApplicationContext("spring.xml");
		factory = (BeanFactory) ctx;
	}
}
