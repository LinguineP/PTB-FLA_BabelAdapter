package protocols.application.utils.DataDissemination;

import protocols.application.utils.DataDissemination.data.UserMessage;;

public interface UserMessageListener {

	public void deliverUserMessage(UserMessage m);
	
}
