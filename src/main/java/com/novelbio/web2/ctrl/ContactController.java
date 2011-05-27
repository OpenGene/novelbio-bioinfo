   package com.novelbio.web2.ctrl;  
     
   import org.springframework.beans.factory.annotation.Autowired;  
   import org.springframework.stereotype.Controller;  
   import org.springframework.ui.ModelMap;  
   import org.springframework.validation.BindingResult;  
   import org.springframework.validation.Validator;  
   import org.springframework.web.bind.annotation.ModelAttribute;  
   import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestMethod;  
import org.springframework.web.servlet.ModelAndView;
import com.novelbio.web.model.UserMessage;
     
   @Controller  
   @RequestMapping("form.htm")
   public final class ContactController {  
         
       @Autowired  
       private Validator validator;  
         
       public void setValidator(Validator validator) {  
           this.validator = validator;  
           
       }  
         
       @RequestMapping(method = RequestMethod.GET)  
       public ModelAndView get() {  
           ModelAndView mav = new ModelAndView("form");
           
    	   // Because we're not specifying a logical view name, the  
           // DispatcherServlet's DefaultRequestToViewNameTranslator kicks in.  
    	   
    	   mav.addObject("userMessage", new UserMessage());
           return mav;// new ModelMap("userMessage", new UserMessage());  
       }  
         
       @RequestMapping( method = RequestMethod.POST)  
       public String post(@ModelAttribute("userMessage") UserMessage userMsg,  
               BindingResult result) {  
             
           validator.validate(userMsg, result);  
           if (result.hasErrors()) { return "form"; }  
             
           // Use the redirect-after-post pattern to reduce double-submits.  
           return "redirect:thanks";  
       }  
         
       @RequestMapping("/thanks")  
       public void thanks() {  
       }  
   }  