package com.novelbio.database.service;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AbsGetSpring {
	ApplicationContext ctx;
	BeanFactory factory;

	public AbsGetSpring() {
		ctx = new ClassPathXmlApplicationContext("spring.xml");
		factory = (BeanFactory) ctx;
	}
}
