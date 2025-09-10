import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;

import protocols.application.Adapter;
import protocols.application.utils.DataDissemination.DataDisseminationApp;
import protocols.application.utils.DataDissemination.MessagingHandler;
import protocols.application.utils.Utils;
import pt.unl.di.novasys.babel.webservices.rest.GenericREST;
import pt.unl.di.novasys.babel.webservices.utils.ServerConfig;
import pt.unl.di.novasys.babel.webservices.websocket.GenericWebSocket;
import pt.unl.fct.di.novasys.babel.core.Babel;
import pt.unl.fct.di.novasys.babel.protocols.antientropy.AntiEntropy;
import pt.unl.fct.di.novasys.babel.protocols.eagerpush.EagerPushGossipBroadcast;
import pt.unl.fct.di.novasys.babel.protocols.hyparview.HyParView;
import pt.unl.fct.di.novasys.babel.protocols.membership.Peer;
import pt.unl.fct.di.novasys.babel.utils.NetworkingUtilities;
import pt.unl.fct.di.novasys.babel.utils.memebership.monitor.MembershipMonitor;
import pt.unl.fct.di.novasys.babel.utils.overlayEstimations.RandomTour.RandomTour;
import pt.unl.fct.di.novasys.network.data.Host;


public class Main {
	private static Logger logger;
	private static final String DEFAULT_CONF = "config.properties";

	public static void main(String[] args) throws Exception {
		Main.configLogger();
		Properties props = Babel.loadConfig(args, DEFAULT_CONF);
		Utils.freshStart(props);

		// Babel Setup
		InetAddress address = getAddress(props);
		Peer myself = generatePeer(address, props);
		Babel babel = Babel.getInstance();
		logger.info("Babel node started on {}", myself.toString());

		Adapter adapter = Adapter.getInstance();

		String babelAddressStr=Utils.getBabelAddress(args);
		String[] sList = { babelAddressStr};

		MessagingHandler ms=new MessagingHandler(sList);




		
		short portMembership = Short.parseShort(props.getProperty("HyParView.Channel.Port"));
		Peer membershipPeer = convertToProtocolAddress(myself, portMembership);
		HyParView membership = new HyParView("channel.hyparview", props, membershipPeer);
		
		MembershipMonitor mm = new MembershipMonitor(); 


		Host gossipHost = new Host(ms.getHost().getAddress(), ms.getHost().getPort() + 1);
		EagerPushGossipBroadcast bcast = new EagerPushGossipBroadcast("channel.gossip", props, gossipHost);

		AntiEntropy at = new AntiEntropy(props, null);

		RandomTour randomTour = new RandomTour(props, null);

		

		
		DataDisseminationApp app = new DataDisseminationApp(gossipHost, ms.getListener());
		
		

		babel.registerProtocol(membership);
		babel.registerProtocol(mm);
		babel.registerProtocol(adapter);
		logger.debug("Loaded: " + mm.getProtoName() + " " + mm.getProtoId());
		babel.registerProtocol(bcast);
		logger.debug("Loaded: " + bcast.getProtoName() + " " + bcast.getProtoId());
		babel.registerProtocol(at);
		logger.debug("Loaded: " + at.getProtoName() + " " + at.getProtoId());
		babel.registerProtocol(randomTour);
		logger.debug("Loaded:" + randomTour.getProtoName() + " " + randomTour.getProtoId());
		babel.registerProtocol(app);
		logger.debug("Loaded: " + app.getProtoName() + " " + app.getProtoId());




		mm.init(props);
		bcast.init(props);
		at.init(props);
		randomTour.init(props);
		app.init(props);
		adapter.init(props);
		membership.init(props);
		
		ms.setApp(app);
		ms.setMembershipListener(mm);
		ms.setHyparview(membership);
		ms.setBcast(bcast);
		ms.setAt(at);
		ms.setRandomTour(randomTour);

		Adapter.setMessenger(ms);

		
		

		Set<Class<? extends GenericREST>> restServices = ServerConfig.generateRestServices(props);
		Set<Class<? extends GenericWebSocket>> wsServices = ServerConfig.generateWebsocketServices(props);
		ServletContextHandler serverContext = ServerConfig.createServerContext(wsServices, restServices, adapter);
		Server server = ServerConfig.createServer(serverContext, address);

		// Start server and babel core
		babel.start();
		server.start();
		logger.warn("Server started on {}", server.getURI());
		
		// server.join();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.warn("Server stopped!")));
	}




	private static void configLogger() {
		System.setProperty("log4j.configurationFile", "log4j2.xml");
		if (System.getProperty("logFileName") == null)
			System.setProperty("logFileName", generateLogFileName());

		logger = LogManager.getLogger(Main.class);
	}

	private static String generateLogFileName() {
		return UUID.randomUUID().toString();
	}

	private static Peer convertToProtocolAddress(Peer myself, short port) {
		return new Peer(myself.getAddress(), port, myself.getPeerID());
	}

	private static InetAddress getAddress(Properties props) throws UnknownHostException, SocketException {
		String babelAddress = null;

		if (props.containsKey(Babel.PAR_DEFAULT_ADDRESS))
			babelAddress = props.getProperty(Babel.PAR_DEFAULT_ADDRESS);
		else if (props.containsKey(Babel.PAR_DEFAULT_INTERFACE))
			babelAddress = NetworkingUtilities.getAddress(props.getProperty(Babel.PAR_DEFAULT_INTERFACE));

		return InetAddress.getByName(babelAddress);
	}

	private static Peer generatePeer(InetAddress address, Properties props)
			throws UnknownHostException, SocketException {
		int port = Integer.parseInt(props.getProperty(Babel.PAR_DEFAULT_PORT, "8080"));
		return new Peer(address, port);
	}
}
