package com.novelbio.web.model;

public class Feedback {

	private String email;
	private Integer rating;
	private String comments;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "Rating " + rating + " was given by " + email
				+ " with the following comments: " + comments;
	}

}
