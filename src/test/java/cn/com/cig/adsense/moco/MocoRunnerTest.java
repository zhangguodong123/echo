package cn.com.cig.adsense.moco;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.com.cig.adsense.Server;
//import cn.com.cig.adsense.handler.BitautoDeliveryHandler;

//import com.google.common.io.CharStreams;

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;



//import org.apache.http.client.fluent.Content;
//import org.apache.http.client.fluent.Request;

public class MocoRunnerTest {
	//private final String url="http://localhost:8080/bitai?pid=11";
    @Before
    public void setup() {
    	Server.main(new String[]{"localhost","8080"});
    	
    }

    @After
    public void tearDown() {
    	
    }

    @Test
    public void should_response_as_expected(){
    	/*BitautoDeliveryHandler bitautoDeliveryHandler = new BitautoDeliveryHandler();
    	
    	String list="http://localhost:12306";
    	String top="http://localhost:12306";
    	
    	Content content = Request.Get(url).execute().returnContent();
    	InputStream asStream = content.asStream();
    	String text = CharStreams.toString(new InputStreamReader(asStream, "UTF-8"));
    	System.out.println(text);*/
    }
}