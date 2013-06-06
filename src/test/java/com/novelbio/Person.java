package com.novelbio;

public class Person {
	int age = 16;
	boolean sex;
	int gongzi = 1000;
	boolean married;
	
	public Person(int age, boolean sex, boolean married) {
		this.age = age;
		this.sex = sex;
		this.married = married;
	}
	
	
	/**
	 * 默认16
	 * @param age
	 */
	public void setAge(int age) {
		this.age = age;
	}
	/** 默认true：为男生 */
	public void setSex(boolean sex) {
		this.sex = sex;
	}
	/** 默认工资1000 */
	public void setGongzi(int gongzi) {
		this.gongzi = gongzi;
	}
	
	
	public int getDuanWuJieBonus() {
		double result = 0;
		if (!sex) {
			result = 20;
		} else {
			result = 10;
		}
		if (age > 20) {
			result = result * 1.3 + age;
		}
		if (married) {
			result = result + 50;
		}
		return (int)result;
	}
	
}
