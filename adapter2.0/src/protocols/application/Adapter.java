package protocols.application;







import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.net.InetAddress;
import java.net.UnknownHostException;


import com.google.gson.Gson;


import rest.AdapterREST.AdapterEndpoints;
import protocols.application.utils.DataDissemination.MessagingHandler;
import protocols.application.utils.DataDissemination.data.AdapterMessage;
import protocols.application.utils.DataDissemination.data.UserMessage;
import pt.unl.di.novasys.babel.webservices.WebAPICallback;
import pt.unl.di.novasys.babel.webservices.application.GenericWebServiceProtocol;
import pt.unl.di.novasys.babel.webservices.utils.EndpointPath;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;


public class Adapter extends GenericWebServiceProtocol{

	

		
	private static final String PROTOCOL_NAME = "ptbflaAdapter";
	private static final short PROTOCOL_ID = 1000;

	

	

	private static String HostName;
	

	private boolean initialized;
	public static MessagingHandler messenger;

	

	//singleton of the messenger
	private Adapter() {
		super(PROTOCOL_NAME, PROTOCOL_ID);

		try {
			HostName= InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		


		this.initialized = false;
	}

	public static Adapter getInstance() {
		if (instance == null)
			instance = new Adapter();

		return (Adapter) instance;
	}
    



    

    //creating a new value in nimbus 
    @Override
    protected void createAsync(String opUniqueID, Object value, WebAPICallback wapic, Optional<EndpointPath> endpointPath) {
		if (endpointPath.isEmpty())
			return;


		AdapterEndpoints path =(AdapterEndpoints) endpointPath.get();
		switch(path){
			case MESSAGES:
			messaging(value);
			break;
			case VISIBILITY_MODEL:
			visibilityModel();
			break;
			case MEMBERSHIP:
			updateMembership();
			break;
			default:
				break;
		}
		
		
		
		

    }

    @Override
    protected void updateAsync(String opUniqueID, Object value, WebAPICallback wapic, Optional<EndpointPath> endpointPath) {
         throw new UnsupportedOperationException("Update operation not supported.");
    }

    @Override
    protected void readAsync(String opUniqueID, Object value, WebAPICallback wapic, Optional<EndpointPath> endpointPath) {
        if (endpointPath.isEmpty())
			return;


		AdapterEndpoints path =(AdapterEndpoints) endpointPath.get();
		switch(path){
			case MESSAGES:
			break;
			case VISIBILITY_MODEL:
			break;
			case MEMBERSHIP:
			break;
			default:
				break;
		}
    }

    @Override
    protected void deleteAsync(String string, Object o, WebAPICallback wapic, Optional<EndpointPath> optnl) {
        throw new UnsupportedOperationException("Delete operation not supported.");
    }

    @Override
    public void init(Properties props) throws HandlerRegistrationException, IOException {
        
		if (initialized)
			return;
	
		this.initialized = true;

    }



    








	public void messaging(Object msg) {
		// Cast Object to AdapterRequest
		AdapterMessage request = (AdapterMessage) msg;

		// Serialize AdapterRequest to JSON string using Gson
		Gson gson = new Gson();
		String message = gson.toJson(request);
		System.out.println("Serialized message: " + message);

		UserMessage babelMessage = new UserMessage(HostName, "ptbflaInstance", message);
		messenger.sendUserMessage(babelMessage);



	}

	



	public void visibilityModel(){

	}

	public void updateMembership(){
		
	}


	public static MessagingHandler getMessenger() {
		return messenger;
	}

	public static void setMessenger(MessagingHandler messenger) {
		Adapter.messenger = messenger;
	}


}