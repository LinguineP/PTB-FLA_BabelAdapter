package protocols.application.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.*;
import java.util.regex.*;

import protocols.application.utils.PortCloser;

import com.github.javafaker.Faker;

import pt.unl.fct.di.novasys.babel.protocols.membership.Peer;

public class Utils {
	static final Faker faker = new Faker();
	static final Random random = new Random();
	static final int MAX_ID_LENGTH = 6;

	public static final Set<Peer> EMPTY_SET = new HashSet<>();
	public static final String[] TITLES_ACRONYMS = { "FCS", "SDM", "HCI", "CCD", "CN" };
	public static final String[] PRESENTERS = { "John M.", "William F.", "Aubrey L.", "Jake T.", "Rob N." };
	public static final String[] TITLES_DESCRIPTIONS = { "Fundamentals of Computer Science",
			"Software Development Methodologies", "Human Computer Interaction",
			"Cloud Computing and Distributed Systems", "Computer Networks - A Look Into History" };
	public static final String[] LOCATIONS = { "Lab 124.", "Classroom 20.31", "Seminar Room 12", "P3.2", "Lab 10.2" };

	public static final String[] BASE_QUESTIONS = { "Overall quality of the talk", "Presenter skill",
			"Explained the motivation clearly" };

	public static final String LIKEABLE_TYPE = "L";
	public static final String RATABLE_TYPE = "R";

	public static String getRandomQuestionID() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		StringBuilder buffer = new StringBuilder(MAX_ID_LENGTH);
		for (int i = 0; i < MAX_ID_LENGTH; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}

		return buffer.toString().toUpperCase();
	}

	public static String getAcronym(String str) {
		return str.replaceAll("\\B.|\\P{L}", "").toUpperCase();
	}

	public static int getAcronymPresenter(String acronym) {
		List<String> list = Arrays.asList(TITLES_ACRONYMS);
		return list.indexOf(acronym);
	}

	public static String getQuestionID(String objectID) {
		String[] tokens = objectID.split("/")[1].split("_");
		return tokens[0];
	}

	public static String getUnderscoreName(String key) {
		String[] tokens = key.split("_");
		return tokens[0];
	}

	public static String maxLexicographyically(String s1, String s2) {
		if (s1 == null)
			return s2;

		if (s2 == null)
			return s1;

		return s1.compareToIgnoreCase(s2) >= 0 ? s1 : s2;
	}

	public static String getRandomName() {
		return faker.name().firstName().toLowerCase();
	}

	public static String getRandomQuestion() {
		return faker.lorem().sentence() + " ?";
	}

	public static InetAddress generateIPAddress(String ip) {
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public static String getBabelAddress(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("babel.address=")) {
                return "babel.address="+arg.substring("babel.address=".length());
            }
        }
        return null; // or throw an exception if it's required
    }


	private static List<Integer> extractPorts(Properties props) {
        List<Integer> ports = new ArrayList<>();
        Pattern portKeyPattern = Pattern.compile(".*port.*", Pattern.CASE_INSENSITIVE);

        for (String key : props.stringPropertyNames()) {
            Matcher matcher = portKeyPattern.matcher(key);
            if (matcher.matches()) {
                try {
                    String value = props.getProperty(key).trim();
                    int port = Integer.parseInt(value);
                    ports.add(port);
                } catch (NumberFormatException e) {
                    // Ignore keys with non-numeric "port" values
                }
            }
        }
        return ports;
    }

	public static void freshStart(Properties props){

		List<Integer> ports=extractPorts(props);

		for (Integer port : ports) {
			PortCloser.closeProcessesOnPort(port);
        }

	}

}
