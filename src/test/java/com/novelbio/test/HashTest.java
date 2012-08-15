package com.novelbio.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.novelbio.database.model.modgeneid.GeneID;

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
		Testclass testclass2 = new Testclass("sfe", 200);
		HashMap<Testclass, Integer> hashtest = new HashMap<Testclass, Integer>();
		hashtest.put(testclass1, 1000);
		hashtest.put(testclass2, 2000);
		for (Entry<Testclass, Integer> entry : hashtest.entrySet()) {
			System.out.println("hash " + entry.getKey().toString() + " " +entry.getValue());
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
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		Testclass otherObj = (Testclass)obj;
//		if (aa.equals(otherObj.aa) && bb == otherObj.bb) {
//			return true;
//		}
		if (   aa.equals(otherObj.aa) ) {
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		return bb;
	}
	public String toString() {
		return aa + " " + bb;
	}
}