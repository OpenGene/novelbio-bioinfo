package com.novelbio.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.novelbio.database.domain.modgeneid.GeneID;

import java.util.Set;

/**
 * 检测equal不同的类，相同的hashcode，会不会碰撞
 * hash不同查hash
 * hash相同，再查equal
 * @author zong0jie
 *
 */
public class HashTest {
	
	public static void main(String[] args) {
		Testclass testclass1 = new Testclass("sfe", 100);
		Test2 testclass2 = new Test2("sfe", 200);
		Set<Testclass> hashtest = new HashSet<Testclass>();
		hashtest.add(testclass1);
		hashtest.add(testclass2);
		for (Testclass testclass : hashtest) {
			System.out.println(testclass.toString());
		}
		System.out.println(testclass1.equals(testclass2));
	}
	
}

class Testclass
{
	public Testclass(String aa, int bb) {
		this.aa = aa;
		this.bb = bb;
	}
	String aa = "";
	int bb = 100;
	@Override
	public boolean equals(Object obj) {
		return true;
	}
	
	public int hashCode() {
		return bb;
	}
	
	public String toString() {
		return aa + " " + bb;
	}
}

class Test2 extends Testclass {

	public Test2(String aa, int bb) {
		super(aa, bb);
		// TODO Auto-generated constructor stub
	}
	public int hashCode() {
		return bb;
	}
}
