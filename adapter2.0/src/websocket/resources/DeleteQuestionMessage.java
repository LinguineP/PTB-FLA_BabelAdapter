package websocket.resources;

public class DeleteQuestionMessage {
	private String lectureID;
	private String questionID;
	private String questionType;

	public DeleteQuestionMessage() {
		this.lectureID = null;
		this.questionID = null;
	}

	public DeleteQuestionMessage(String lectureID, String questionID, String questionType) {
		this.lectureID = lectureID;
		this.questionID = questionID;
		this.questionType = questionType;

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
	
	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
}
