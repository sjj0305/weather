import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

    private HttpURLConnection getConnection(String url, String method) throws Exception {
        HttpURLConnection connection = null;
        for (int retry = 0; retry < 3; retry++) {
            try {
                URL httpUrl = new URL(url);
                connection = (HttpURLConnection) httpUrl.openConnection();
                connection.setRequestMethod(method);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.connect();
            } catch (IOException e) {
                System.out.println(String.format("connection %s failed, reconnect %d", url, retry));
                continue;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return connection;
        }
        throw new Exception(String.format("connection %s failed, please check you internet", url));
    }

    public String request(String url, String method) throws Exception {
        HttpURLConnection connection = getConnection(url, method);
        try (InputStream is = connection.getInputStream()) {
            if (connection.getResponseCode() == 200) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] cache = new byte[1024];
                int i;
                while ((i = is.read(cache)) != -1) {
                    bos.write(cache, 0, i);
                }
                return bos.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new Exception("read response error");
        } finally {
            connection.disconnect();
        }
    }


}
