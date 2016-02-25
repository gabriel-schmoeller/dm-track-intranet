package ponto;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gabriel.schmoeller
 */
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        SpringApplication.run(Main.class, args);
    }
}
