package com.novelbio.web.velocityexp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
@Component
public class CourseService {  
	public List getAllCourses(){  
	    List courseList = new ArrayList();  
	    Course course = null;  
	    Date date = null;  
	    for(int i = 0; i < 8; i++){  
	      course = new Course();  
	      course.setId("XB2006112-"+i);  
	      course.setName("Name-"+i);  
	      date = new Date();  
	      date.setYear(104-i);  
	      course.setStartDate(date);  
	      date = new Date();  
	      date.setYear(105+i);  
	        course.setEndDate(date);;  
	      Instructor instructor = new Instructor();  
	      instructor.setFirstName("firstName-"+i);  
	      instructor.setLastName("lastNameºóÃæ-"+i);  
	      course.setInstructor(instructor);  
	      courseList.add(course);  
	    }  
	    return courseList;  
	}
	} 
