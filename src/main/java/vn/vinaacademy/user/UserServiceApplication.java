package vn.vinaacademy.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import vn.vinaacademy.kafka.KafkaProducerConfig;
import vn.vinaacademy.kafka.KafkaTopic;
import vn.vinaacademy.kafka.event.NotificationCreateEvent;
import vn.vinaacademy.user.event.EmailProducer;

import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "vn.vinaacademy.user",
        "vn.vinaacademy.common"
})
@Import(KafkaProducerConfig.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
