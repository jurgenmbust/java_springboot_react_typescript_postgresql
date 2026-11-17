package com.inventory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the server and discovers Spring components in this package. */
@SpringBootApplication
public class InventoryApplication {
    /** Starts Spring Boot. @param args Command line options. */
    public static void main(String[] args) { SpringApplication.run(InventoryApplication.class, args); }
}
