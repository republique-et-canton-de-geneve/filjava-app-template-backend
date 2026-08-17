package ch.ge.filjava.apptemplatebackend.ui.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "ch.ge.filjava.apptemplatebackend")
@EntityScan("ch.ge.filjava.apptemplatebackend.infra")
public class FiljavaApplication {

    static void main(String[] args) {
        SpringApplication.run(FiljavaApplication.class, args);
    }
}