package org.aib.kafkavideo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aib.av.IVideoStreamConsumer;
import org.aib.consumer.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.aib.consumer", "org.aib.av"})
public class FileconsumerApplication implements CommandLineRunner {
	static Logger logger = LoggerFactory.getLogger(FileconsumerApplication.class);
	
	@Autowired
	private IVideoStreamConsumer consumer;

	 @Autowired
	 private Receiver receiver;
	 
	@Value("${gstreamer.pipe}")
	private String pipeline;

	private static ExecutorService executor = Executors.newFixedThreadPool(5);

	public static void main(String[] args) {
	    SpringApplication app = new SpringApplication(FileconsumerApplication.class);
        app.setBannerMode(Mode.OFF);
        app.run(args);
	}
	
	@Override
    public void run(String... args) throws Exception {
    	logger.info("Start...");

    	Runtime.getRuntime().addShutdownHook(new Thread() {
    	    public void run() {
    	    	logger.info("EOS is sent...");
    	    	consumer.sendEos();
    	    }
    	 });	   	    	

    	receiver.SetCallback(payload -> consumer.post(payload));
    	
    	executor.submit(new Runnable() {
            @Override
            public void run() {
            	consumer.startWithoutAppSink(pipeline);
            }
    	});    	    	
	}
}