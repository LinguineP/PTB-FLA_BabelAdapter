package websocket.resources;

public class AddLectureMessage {
	private String lectureID;

	public AddLectureMessage() {
		this.lectureID = null;
	}

	public AddLectureMessage(String lectureID) {
		this.lectureID = lectureID;
	}

	public AddLectureMessage(String lectureID, int attendants) {
		this.lectureID = lectureID;
	}

	public String getLectureID() {
		return lectureID;
	}

	public void setLectureID(String lectureID) {
		this.lectureID = lectureID;
	}
}
