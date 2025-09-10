package websocket.resources;

import java.util.List;

public class UpdateQuestionMessage {
	private String lectureID;
	private String questionID;
	private List<String> questionRatings;
	private Integer questionRating;
	private String questionType;

	public UpdateQuestionMessage() {
		this.lectureID = null;
		this.questionID = null;
		this.questionRatings = null;
		this.questionRating = null;
	}

	public UpdateQuestionMessage(String lectureID, String questionID, String questionType) {
		this.lectureID = lectureID;
		this.questionID = questionID;
		this.questionType = questionType;
		this.questionRatings = null;
		this.questionRating = null;
	}

	public List<String> getQuestionRatings() {
		return questionRatings;
	}

	public String getLectureID() {
		return lectureID;
	}

	public String getQuestionID() {
		return questionID;
	}

	public void setLectureID(String lectureID) {
		this.lectureID = lectureID;
	}

	public void setQuestionID(String questionID) {
		this.questionID = questionID;
	}

	public void setQuestionRatings(List<String> questionRatings) {
		this.questionRatings = questionRatings;
	}

	public int getQuestionRating() {
		return questionRating;
	}

	public void setQuestionRating(int questionRating) {
		this.questionRating = questionRating;
	}

	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

	public boolean hasAllInformation() {
		return (questionRatings != null || questionRating != null);
	}

}
