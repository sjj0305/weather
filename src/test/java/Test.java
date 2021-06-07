import org.junit.Assert;

public class Test {

    private static Weather weather = new Weather();

    @org.junit.Test
    public  void success(){
        Assert.assertTrue("the temp should be 22",22==weather.getTemperature("江苏","苏州","太仓").get());
    }

    @org.junit.Test
    public void fail(){
        Assert.assertTrue("the temp should be null",!weather.getTemperature("江苏","苏州","杭州").isPresent());
    }


}
