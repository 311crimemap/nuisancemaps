import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.quirkshop.nuisancemaps.model.Test;

public class SimpleCrawler {
    public static void main(String[] args) throws IOException {
        String url = "http://example.com";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line as needed
                Test t = new Test("test", 1234);
                t.save();

                System.out.println(line);
            }
        } finally {
            connection.disconnect();
        }
    }
}
