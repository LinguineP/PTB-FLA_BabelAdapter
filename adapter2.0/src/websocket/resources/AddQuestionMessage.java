package websocket.resources;

import java.util.List;

public class AddQuestionMessage {
	private String lectureID;
	private String questionID;
	private String questionTitle;
	private List<String> questionRatings;
	private Integer questionRating;
	private String questionType;

	public AddQuestionMessage() {
		this.lectureID = null;
		this.questionID = null;
		this.questionTitle = null;
		this.questionRatings = null;
		this.questionRating = null;
	}

	public AddQuestionMessage(String lectureID, String questionID, String questionType) {
		this.lectureID = lectureID;
		this.questionID = questionID;
		this.questionType = questionType;
		this.questionTitle = null;
		this.questionRatings = null;
		this.questionRating = null;
	}

	public String getQuestionTitle() {
		return questionTitle;
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

	public void setQuestionTitle(String questionTitle) {
		this.questionTitle = questionTitle;
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
		return questionTitle != null && (questionRatings != null || questionRating != null);
	}
}
