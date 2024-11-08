package babelAdapterApp;

import tardis.SimpleUseCase;

import java.net.*;
import java.util.Properties;

public class AdapterMain {


    public static SimpleUseCase BabelBaseLogic;
    public static Properties props;

    private static String HostName;
    private static int adapterServerPort;

    public final static String ADAPTER_SERVER_PORT = "adapter.serverPort";
    public final static String DOPPELGANGER_BASE_PORT = "doppelganger.basePort";
    public final static String DOPPELGANGER_MAILBOX_OFFSET = "doppelganger.mailboxOffset";
    public final static String DOPPELGANGER_ID = "doppelganger.id";



    public static void initBabel(String[] args){
        if (Mpapi.BabelBaseLogic == null) {
            try {
                Mpapi.BabelBaseLogic = new SimpleUseCase(args);
                BabelBaseLogic=Mpapi.BabelBaseLogic;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            Mpapi.BabelBaseLogic.start();

            //listUpdater.start(); // start the thread that is updating the list
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void init(){
        props=BabelBaseLogic.getProperties();

        adapterServerPort = Integer.parseInt(props.getProperty(ADAPTER_SERVER_PORT));
        Mpapi.setDoppelgangerMailboxOffset(Integer.parseInt(props.getProperty(DOPPELGANGER_MAILBOX_OFFSET)));

        try {
            HostName= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public static void startDoppelgangers () {
        try {
            new DoppelgangerStarter().startPythonScript();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void startAdapter(String[] args) {
        new Thread(AdapterMain::startDoppelgangers).start(); //starts doppelganger instances with ports specified in adapter.config

        init();//adapter app initialisation
        new Thread(() -> Mpapi.startServer(adapterServerPort)).start(); //incoming ptbfla messages listener


    }

}
