package org.aib.kafkavideo;

import java.awt.EventQueue;
import java.util.Scanner;
import java.util.UUID;

import org.aib.av.IVideoStreamProducer;
import org.aib.producer.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.aib.vpost", "org.aib.av", "org.aib.producer"})
public class ProducerApplication  implements CommandLineRunner {
	static Logger logger = LoggerFactory.getLogger(ProducerApplication.class);

	@Autowired
	private Sender sender;

	@Autowired
	private IVideoStreamProducer capture;

	@Value("${gstreamer.pipe}")
	private String pipeline;
	
	private Integer counter = 0;

	private String id = UUID.randomUUID().toString();
	
	public static void main(String[] args) {
	    SpringApplication app = new SpringApplication(ProducerApplication.class);
        app.setBannerMode(Mode.OFF);
        app.run(args);
	}

	@Override
    public void run(String... args) throws Exception {
    	logger.info("Start capture...");
    	    	
    	EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
	    	capture.start(pipeline, data -> {	    		
	    			sender.send(id, data);
	    		});
            }
    	});
    	
    	final Scanner scanner = new Scanner(System.in);
    	for(;;) {
    		System.out.print(">>>");
    		String str = scanner.next();    		
    		if(str.equals("end")) {
    			break;
    		}
    	}

    	capture.stop();
    	
		System.exit(0);
    }
}