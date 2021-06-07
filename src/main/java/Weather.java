import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.omg.CORBA.TIMEOUT;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class Weather {

    private static final String PROVINCE_URL = "http://www.weather.com.cn/data/city3jdata/china.html";

    private static final String CITY_URL = "http://www.weather.com.cn/data/city3jdata/provshi/%s.html";

    private static final String COUNTRY_URL = "http://www.weather.com.cn/data/city3jdata/station/%s%s.html";

    public static final String WEATHER_URL = "http://www.weather.com.cn/data/sk/%s%s%S.html";

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    private HttpClient client = new HttpClient();

    private Gson gson = new Gson();

    public Optional<Integer> getTemperature(String province, String city, String county) {
        CountDownLatch finishNotify = new CountDownLatch(1);
        FutureTask<Optional<Integer>> task = new FutureTask(new Callable<Optional<Integer>>() {
            @Override
            public Optional<Integer> call() throws Exception {
                return getTemperature(province, city, county, finishNotify);
            }
        });
        executor.submit(task);
        try {
            finishNotify.await(100, TimeUnit.SECONDS);
            if (task.isDone()) {
                return task.get();
            }else{
                task.cancel(true);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Integer> getTemperature(String province, String city, String county, CountDownLatch countDownLatch) {
        JsonObject weatherJson = null;
        try {
            String provinceStr = client.request(PROVINCE_URL, "GET");
            Map<String, String> provinceMap = gson.fromJson(provinceStr, HashMap.class);
            String provinceCode = provinceMap.entrySet().stream().filter(entry -> entry.getValue().equals(province)).findFirst().orElseThrow(() -> new Exception("province is not correct")).getKey();
            String cityStr = client.request(String.format(CITY_URL, provinceCode), "GET");
            Map<String, String> cityMap = gson.fromJson(cityStr, HashMap.class);
            String cityCode = cityMap.entrySet().stream().filter(entry -> entry.getValue().equals(city)).findFirst().orElseThrow(() -> new Exception("city is not correct")).getKey();
            String countryStr = client.request(String.format(COUNTRY_URL, provinceCode, cityCode), "GET");
            Map<String, String> countryMap = gson.fromJson(countryStr, HashMap.class);
            String countryCode = countryMap.entrySet().stream().filter(entry -> entry.getValue().equals(county)).findFirst().orElseThrow(() -> new Exception("country is not correct")).getKey();
            String weatherStr = client.request(String.format(WEATHER_URL, provinceCode, cityCode, countryCode), "GET");
            weatherJson = JsonParser.parseString(weatherStr).getAsJsonObject();
            return weatherJson.has("weatherinfo") ? Optional.of(Double.valueOf(weatherJson.getAsJsonObject("weatherinfo").get("temp").getAsString()).intValue()) : Optional.empty();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            countDownLatch.countDown();
        }
        return Optional.empty();
    }
}
