package com.novelbio.web.ctrl;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.novelbio.web.model.Feedback;

public class FeedbackFormController extends SimpleFormController {
	public FeedbackFormController() {
		setCommandClass(Feedback.class);
		setValidator(new FeedbackFormValidator());
		setCommandName("feedback");
		setSuccessView("redirect:albums.htm");
		setFormView("feedbackform");
	}

	@Override
	protected ModelAndView onSubmit(Object command) throws Exception {
		logger.info("The following data was submitted: " + command);
		return super.onSubmit(command);
	}

	class FeedbackFormValidator implements Validator {
		private final Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");

		public boolean supports(Class clazz) {
			return Feedback.class.isAssignableFrom(clazz);
		}

		public void validate(Object command, Errors errors) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rating",
					"error.rating.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email",
					"error.email.required");
			if (errors.hasErrors()) {
				// Abort any further validation if one or more of the required
				// fields are empty.
				return;
			}
			Feedback feedback = (Feedback) command;
			if (!pattern.matcher(feedback.getEmail()).matches()) {
				errors.rejectValue("email", "error.email.format");
			}
		}
	}
}