package tardis;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jnr.constants.platform.Signal;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.SignalHandler;
import pt.unl.fct.di.novasys.babel.core.Babel;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.exceptions.InvalidParameterException;
import pt.unl.fct.di.novasys.babel.exceptions.ProtocolAlreadyExistsException;
import pt.unl.fct.di.novasys.babel.protocols.antientropy.AntiEntropy;
import pt.unl.fct.di.novasys.babel.protocols.eagerpush.EagerPushGossipBroadcast;
import pt.unl.fct.di.novasys.babel.protocols.hyparview.HyParView;
import pt.unl.fct.di.novasys.babel.utils.NetworkingUtilities;
import pt.unl.fct.di.novasys.babel.utils.memebership.monitor.MembershipMonitor;
import pt.unl.fct.di.novasys.babel.utils.memebership.monitor.listener.MembershipMonitorListener;
import pt.unl.fct.di.novasys.babel.utils.overlayEstimations.RandomTour.RandomTour;
import pt.unl.fct.di.novasys.network.data.Host;
import tardis.api.UserMessageListener;
import tardis.app.DataDisseminationApp;
import tardis.app.data.UserMessage;

public class SimpleUseCase implements SignalHandler {

	static {
		System.setProperty("log4j.configurationFile", "log4j2.xml");
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	// Creates the logger object
	private static final Logger logger = LogManager.getLogger(SimpleUseCase.class);

	// Default babel configuration file (can be overridden by the "-config" launch
	// argument)
	private static final String DEFAULT_CONF = "tardis.conf";

	private final Babel babel;

	private Host myHost;
	private String address;
	private int port;

	private Properties props;

	private DataDisseminationApp app;

	private UserMessageListener listener;

	private HyParView hyparview;
	private MembershipMonitor mm;
	private EagerPushGossipBroadcast bcast;
	private AntiEntropy at;
	private RandomTour randomTour;

	public SimpleUseCase(String[] args) throws IOException, InvalidParameterException, HandlerRegistrationException {
		// Get the (singleton) babel instance
		this.babel = Babel.getInstance();

		if (new File(DEFAULT_CONF).exists()) {
			System.err.println("The config file: " + DEFAULT_CONF + " is not accessible.");
			System.exit(1);
		}

		props = Babel.loadConfig(args, DEFAULT_CONF);

		address = null;

		if (props.containsKey(Babel.PAR_DEFAULT_INTERFACE))
			address = NetworkingUtilities.getAddress(props.getProperty(Babel.PAR_DEFAULT_INTERFACE));
		else if (props.containsKey(Babel.PAR_DEFAULT_ADDRESS))
			address = props.getProperty(Babel.PAR_DEFAULT_ADDRESS);

		port = -1;

		if (props.containsKey(Babel.PAR_DEFAULT_PORT))
			port = Integer.parseInt(props.getProperty(Babel.PAR_DEFAULT_PORT));

		if (address == null || port == -1) {
			this.myHost = null;
		} else {
			this.myHost = new Host(InetAddress.getByName(address), port);
		}
		
		mm = new MembershipMonitor();
	}

	public Host getHost() {
		return this.myHost;
	}

	public void setHost(Host h) {
		this.myHost = h;
		props.setProperty(Babel.PAR_DEFAULT_ADDRESS, this.myHost.getAddress().getHostAddress());
		props.setProperty(Babel.PAR_DEFAULT_PORT, this.myHost.getPort() + "");
	}

	public void setUserMessageListener(UserMessageListener listener) {
		this.listener = listener;
	}

	public void sendUserMessage(UserMessage message) {
		this.app.sendUserMessage(message);
	}

	public void start() throws IOException, HandlerRegistrationException, ProtocolAlreadyExistsException {
		hyparview = new HyParView("channel.hyparview", props, this.myHost);

		Host gossipHost = new Host(this.myHost.getAddress(), this.myHost.getPort() + 1);
		bcast = new EagerPushGossipBroadcast("channel.gossip", props, gossipHost);

		at = new AntiEntropy(props, null);

		randomTour = new RandomTour(props, null);
		
		app = new DataDisseminationApp(gossipHost, this.listener); // I have decided to use the gossip protocol
																	// identity.

		// Solve the dependency between the data dissemination app and the broadcast
		// protocol if omitted from the config
		props.putIfAbsent(DataDisseminationApp.PAR_BCAST_PROTOCOL_ID, EagerPushGossipBroadcast.PROTOCOL_ID + "");

		babel.registerProtocol(hyparview);
		logger.debug("Loaded: " + hyparview.getProtoName() + " " + hyparview.getProtoId());
		babel.registerProtocol(mm);
		logger.debug("Loaded: " + mm.getProtoName() + " " + mm.getProtoId());
		babel.registerProtocol(bcast);
		logger.debug("Loaded: " + bcast.getProtoName() + " " + bcast.getProtoId());
		babel.registerProtocol(at);
		logger.debug("Loaded: " + at.getProtoName() + " " + at.getProtoId());
		babel.registerProtocol(randomTour);
		logger.debug("Loaded:" + randomTour.getProtoName() + " " + randomTour.getProtoId());
		babel.registerProtocol(app);
		logger.debug("Loaded: " + app.getProtoName() + " " + app.getProtoId());

		hyparview.init(props);
		mm.init(props);
		bcast.init(props);
		at.init(props);
		randomTour.init(props);
		app.init(props);

		System.out.println("Setup is complete.");

		babel.start();

		System.out.println("System is running.");

		if (!System.getProperty("os.name").contains("Windows")) {
			// In windows we do not do POSIX

			POSIX posix = POSIXFactory.getJavaPOSIX();
			posix.signal(Signal.SIGUSR1, this);
			posix.signal(Signal.SIGUSR2, this);

		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Received shutdown. Disabled data transmission.");
				logger.info("Shuting down now.");
			}
		});
	}

	public void setMembershipListener(MembershipMonitorListener mcl) {
		this.mm.addMembershipListener(mcl);
	}

	public Host getMembershipHost() {
		if (hyparview != null)
			return hyparview.getMyself();
		return null;
	}

	public Host getGossipHost() {
		if (bcast != null)
			return bcast.getHost();
		return null;
	}

	public Host getAntiEntropyHost() {
		if (at != null)
			return at.getHost();
		return null;
	}

	public Properties getProperties() {
		return this.props;
	}

	@Override
	public void handle(int signal) {
		if (app.isTransmitting()) {
			try {
				app.disableTransmissions();
				logger.info("Received shutdown. Disabled data transmission.");
				logger.info("Shuting down in 60 seconds.");
				Thread.sleep(60 * 1000);
				System.exit(0);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		} else {
			app.enableTransmission();
			logger.info("Starting to generate data.");
		}
	}

}
