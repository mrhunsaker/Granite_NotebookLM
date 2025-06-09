// ... (imports will now include org.apache.http.*) ...
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
// ...

public class CouchDBClient {
    // ...
    private final CloseableHttpClient httpClient; // NEW

    public CouchDBClient(String couchUrl, String database, String username, String password) {
        // ...
        this.httpClient = HttpClients.createDefault(); // NEW
        // ...
    }

    private String executeRequest(HttpUriRequest request) throws Exception {
        // You can add authentication headers here if needed
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 300) {
                throw new IOException("CouchDB request failed: " + statusCode + " - " + responseBody);
            }
            return responseBody;
        }
    }

    private boolean databaseExists() throws Exception {
        HttpHead request = new HttpHead(couchUrl + "/" + database);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    // ... All other methods like getResponse, storeResponse, etc., would be
    // refactored to use this new `executeRequest` method with Apache HttpClient
    // instead of building HttpURLConnection objects manually.
}
