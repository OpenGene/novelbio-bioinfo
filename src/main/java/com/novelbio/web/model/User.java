package com.novelbio.web.model;

import java.io.File;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

public final class User {  
      

    private String name;  
      
 
    private String email;  
      
 
    private String text;  
      
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
      
    public String getEmail() {  
        return email;  
    }  
  
    public void setEmail(String email) {  
        this.email = email;  
    }  
  
    public String getText() {  
        return text;  
    }  
  
    public void setText(String text) {  
        this.text = text;  
    }  
    
    
    public MultipartFile  multipartFile;
    public void setFile(MultipartFile multipartFile) {  
        this.multipartFile = multipartFile;  
    }  
}  