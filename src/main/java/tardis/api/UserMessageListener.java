package tardis.api;

import tardis.app.data.UserMessage;

public interface UserMessageListener {

	public void deliverUserMessage(UserMessage m);
	
}
