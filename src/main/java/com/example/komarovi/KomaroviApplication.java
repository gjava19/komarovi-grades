package com.example.komarovi;


import com.example.komarovi.services.ExcelLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KomaroviApplication {

    public static void main(String[] args) {
        SpringApplication.run(KomaroviApplication.class, args);
    }
//    @Bean
//    CommandLineRunner loadExcelAtStartup(ExcelLoader loader) {
//        return args -> {
//            try {
//                loader.loadExcel("C:\\Users\\Giorgi\\IdeaProjects\\komarovi\\src\\main\\resources\\test.xlsm");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }
}
