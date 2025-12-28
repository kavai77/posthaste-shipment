package com.posthaste;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.posthaste.utils.LenientBigDecimalDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BigDecimal.class, new LenientBigDecimalDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    public FirebaseDatabase initFirebase(@Value("${firebase.service.account}") String firebaseServiceAccount) throws IOException {
        InputStream serviceAccount = new ByteArrayInputStream(firebaseServiceAccount.getBytes());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://posthaste-pro-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        var firebaseApp = FirebaseApp.initializeApp(options);
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}
