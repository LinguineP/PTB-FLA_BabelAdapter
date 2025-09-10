package protocols.application.utils.DataDissemination;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;


import com.google.gson.Gson;
import protocols.application.utils.DataDissemination.data.AdapterMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.hash.Hashing;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.protocols.dissemination.notifications.BroadcastDelivery;
import pt.unl.fct.di.novasys.babel.protocols.dissemination.requests.BroadcastRequest;
import pt.unl.fct.di.novasys.network.data.Host;
import protocols.application.utils.DataDissemination.UserMessageListener;
import protocols.application.utils.DataDissemination.data.UserMessage;
import protocols.application.utils.HTTPrequestSender;

public class DataDisseminationApp extends GenericProtocol {

	public final static String PROTO_NAME = "TaRDIS Simple App";
	public final static short PROTO_ID = 9999;

	public final static String PAR_BCAST_PROTOCOL_ID = "app.bcast.id";
	private short bcastProtoID;

	public final static String PAR_BCAST_INIT_ENABLED = "app.bcast.enable";
	public final static boolean DEFAULT_BCAST_INIT_ENABLED = true;

	private final Host myself;

	private Logger logger = LogManager.getLogger(DataDisseminationApp.class);

	private AtomicBoolean executing;

	private UserMessageListener listener;

	public DataDisseminationApp(Host myself, UserMessageListener listener) throws HandlerRegistrationException {
		super(DataDisseminationApp.PROTO_NAME, DataDisseminationApp.PROTO_ID);

		this.myself = myself;

		this.listener = listener;

		subscribeNotification(BroadcastDelivery.NOTIFICATION_ID, this::handleDMessageDeliveryEvent);
	}

	@Override
	public void init(Properties props) throws HandlerRegistrationException, IOException {
		//based on the babel swarm example app lib

		if (props.containsKey(PAR_BCAST_PROTOCOL_ID)) {
			this.bcastProtoID = Short.parseShort(props.getProperty(PAR_BCAST_PROTOCOL_ID));
			logger.debug("DataDisseminationApp is configured to used broadcast protocol with id: " + this.bcastProtoID);
		} else {
			logger.error("The applicaiton requires the id of the broadcast protocol being used. Parameter: '"
					+ PAR_BCAST_PROTOCOL_ID + "'");
			System.exit(1);
		}


		boolean b = DEFAULT_BCAST_INIT_ENABLED;
		if (props.containsKey(PAR_BCAST_INIT_ENABLED)) {
			b = Boolean.parseBoolean(props.getProperty(PAR_BCAST_INIT_ENABLED));
		}
		this.executing = new AtomicBoolean(b);
	}

	public void sendUserMessage(UserMessage message) {
		if (!this.executing.getAcquire())
			return;

		boolean success = false;
		byte[] data = null;
		try {
			data = message.toByteArray();
			success = true;
		} catch (Exception e) {
			logger.error("Failed to serialize UserMessage, falling back to String", e);
			data = message.getMessage().getBytes();

		}

		BroadcastRequest request = new BroadcastRequest(myself, data, PROTO_ID);
		sendRequest(request, bcastProtoID);
		if (!success) {
			logger.info(myself + " sent message: [" + myself + "::::" + readableOutput(message.getMessage()) + "]");
		} else {
			if (message.hasAttachment())
				logger.info(myself + " sent message: [" + myself + "::::"
						+ readableOutput(message.getMessage(), message.getAttachmentName()) + "]");
			else
				logger.info(myself + " sent message: [" + myself + "::::" + readableOutput(message.getMessage()) + "]");
		}
	}

	private void handleDMessageDeliveryEvent(BroadcastDelivery msg, short proto) {
		UserMessage um = null;
		String payload = null; 
		try {
			um = UserMessage.fromByteArray(msg.getPayload());
			payload = um.getMessage();
		} catch (Exception e) {
			logger.error("Failed to deserialize UserMessage, falling back to String", e);
			payload = new String(msg.getPayload());
		}
		if (um == null) {
			logger.info(myself + " recv message: [" + msg.getSender() + "::::" + readableOutput(payload) + "]");
		} else {
			if (um.hasAttachment()) {
				logger.info(myself + " recv message: [" + msg.getSender() + "::::"
						+ readableOutput(um.getMessage(), um.getAttachmentName()) + "]");
			} else {
				logger.info(
						myself + " recv message: [" + msg.getSender() + "::::" + readableOutput(um.getMessage()) + "]");
			}
		}

		String finalPayload = payload;
		
			
			
			
			
	
		
					
		System.out.println(
						"just before sending message to the doppelganger"
				);			
			
		Thread thread = new Thread(() -> {
				// your logic here
				Gson gson = new Gson();
				System.out.println(
					finalPayload
				);
				AdapterMessage deserialisedMsg = gson.fromJson(finalPayload, AdapterMessage.class);
				int srcPort = deserialisedMsg.getSrc();
				
				System.out.println(
					srcPort
				);
				int targetDoppelgangerPort =srcPort+ 1000;
				
				//we target the doppelganger instance pretending to be the instance sending the message

				
				if (myself.getAddress()!=msg.getSender().getAddress()) {
					String targetAddr = "http://localhost:"+ Integer.toString(targetDoppelgangerPort)+"/messages";
					try {
						HTTPrequestSender.sendPostJson(targetAddr, finalPayload);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

		});

		thread.start();


		if (this.listener != null && um != null) {
			logger.trace("Sending message with " + msg.getPayload().length + " bytes to thee external listener.");
			listener.deliverUserMessage(um);
		} else {
			logger.trace("Did not send message to external Listener. Reason listenner null (" + (listener == null)
					+ ") message is null (" + (um == null) + ").");
		}


	}

	public static String readableOutput(String msg, String attachName) {
		return Hashing.sha256().hashString(msg + "::" + attachName, StandardCharsets.UTF_8).toString();
	}

	private static String readableOutput(String msg) {
		if (msg.length() > 32) {
			return Hashing.sha256().hashString(msg, StandardCharsets.UTF_8).toString();
		} else
			return msg;
	}

	/**
	 * This method disables the transmission of more messages after it being
	 * executed...
	 */
	public void disableTransmissions() {
		this.executing.set(false);
	}

	public boolean isTransmitting() {
		return this.executing.get();
	}

	public void enableTransmission() {
		this.executing.set(true);
	}
}
