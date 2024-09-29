package cn.hollis.nft.turbo.collection;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hollis
 */
@SpringBootApplication(scanBasePackages = "cn.hollis.nft.turbo.collection")
@EnableDubbo
public class NfTurboCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboCollectionApplication.class, args);
    }

}
