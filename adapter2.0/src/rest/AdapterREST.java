package rest;




import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import protocols.application.utils.DataDissemination.data.AdapterMessage;
import pt.unl.di.novasys.babel.webservices.application.GenericWebServiceProtocol.WebServiceOperation;
import pt.unl.di.novasys.babel.webservices.rest.GenericREST;
import pt.unl.di.novasys.babel.webservices.utils.EndpointPath;
import pt.unl.di.novasys.babel.webservices.utils.GenericWebAPIResponse;
import pt.unl.di.novasys.babel.webservices.utils.PendingResponse;

@Singleton
@Path(AdapterREST.PATH)
public class AdapterREST extends GenericREST {
	public static final String PATH = "/adapter";

	public enum AdapterEndpoints implements EndpointPath {
		MESSAGES("messages"),
		
		VISIBILITY_MODEL("visibilityModel"), MEMBERSHIP("membership");
		private String endpoint;

		AdapterEndpoints(String endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public String getPath() {
			return endpoint;
		}

		
	}

	public AdapterREST() {
		super();
	}

	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void addNode(@Suspended AsyncResponse ar, AdapterMessage request) {
		

		this.sendRequest(WebServiceOperation.CREATE, request, AdapterEndpoints.MESSAGES, ar);
	}

	


	@POST
	@Path("/visibilityModel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void startNewTimeslot(@Suspended AsyncResponse ar, AdapterMessage request) {
		this.sendRequest(WebServiceOperation.CREATE, request, AdapterEndpoints.VISIBILITY_MODEL, ar);
	}


	@GET
	@Path("/visibilityModel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void getVisibilityStatus(@Suspended AsyncResponse ar) {
		this.sendRequest(WebServiceOperation.READ, AdapterEndpoints.VISIBILITY_MODEL, ar);
	}


	@POST
	@Path("/membership")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void membershipConfirmation(@Suspended AsyncResponse ar, AdapterMessage request) {
		this.sendRequest(WebServiceOperation.CREATE, request, AdapterEndpoints.MEMBERSHIP, ar);
	}

	@GET
	@Path("/membership")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void membershipGetInfo(@Suspended AsyncResponse ar) {
		this.sendRequest(WebServiceOperation.CREATE,AdapterEndpoints.MEMBERSHIP, ar);
	}








	

	@Override
	public void triggerResponse(String opUniqueID, GenericWebAPIResponse genericResponse) {
		PendingResponse pendingResponse = super.removePendingResponse(opUniqueID);

		AdapterEndpoints restEndpoint = (AdapterEndpoints) pendingResponse.getRestEnpoint();
		AsyncResponse ar = pendingResponse.getAr();

		switch (restEndpoint) {
		case MESSAGES:
		case MEMBERSHIP:
		case VISIBILITY_MODEL:
			sendResponse(ar, Response.Status.OK, genericResponse.getValue());
			break;	
		default:
			break;
		}
	}

	private void sendResponse(AsyncResponse ar, Response.Status statusCode, Object value) {
		Response response = Response.status(statusCode).entity(value).build();
		ar.resume(response);
	}

	private void sendStatusResponse(AsyncResponse ar, Response.Status statusCode, String message) {
		Response response = Response.status(statusCode).entity(message).build();
		ar.resume(response);
	}
}