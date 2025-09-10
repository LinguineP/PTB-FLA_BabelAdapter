package protocols.application.utils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPrequestSender {
    



    public static String sendPostJson(String targetUrl, String jsonPayload) throws IOException {
        URL url = java.net.URI.create(targetUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set method and headers
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        // Write JSON payload
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response
        int status = connection.getResponseCode();
        InputStream is = (status < HttpURLConnection.HTTP_BAD_REQUEST)
                ? connection.getInputStream()
                : connection.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line.trim());
        }

        reader.close();
        connection.disconnect();

        return "Status: " + status + "\nResponse: " + response.toString();
    }

   
}

    
