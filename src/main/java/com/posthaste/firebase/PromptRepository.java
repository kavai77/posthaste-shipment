package com.posthaste.firebase;

import com.google.common.hash.Hashing;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromptRepository {
    private final FirebaseDatabase firebaseDatabase;

    public void savePrompt(Prompt prompt) {
        var reference = firebaseDatabase.getReference("prompts");
        reference.child(getHash(prompt.getPrompt())).setValueAsync(prompt);
    }

    public Optional<Prompt> getPrediction(String input) {
        DatabaseReference reference = firebaseDatabase.getReference("prompts").child(getHash(input));
        BlockingQueue<Optional<Prompt>> queue = new ArrayBlockingQueue<>(1);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    try {
                        queue.put(Optional.of(dataSnapshot.getValue(Prompt.class)));
                    } catch (DatabaseException e) {
                        log.warn("Firebase exception", e);
                        queue.put(Optional.empty());
                    }
                } catch (InterruptedException e) {
                    log.error("Interrupted exception", e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                try {
                    log.atWarn()
                            .addKeyValue("databaseError", databaseError)
                            .log("Firebase cancelled event");
                    queue.put(Optional.empty());
                } catch (InterruptedException e) {
                    log.error("Interrupted exception", e);
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            var maybePrompt = Optional.ofNullable(queue.poll(5, TimeUnit.SECONDS))
                    .flatMap(Function.identity());
            maybePrompt.ifPresent(prompt -> {
                reference.setValueAsync(prompt.toBuilder()
                        .accessCount(prompt.getAccessCount() + 1)
                        .lastAccessTime(new Date())
                        .build()
                );
            });

            return maybePrompt;
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
            return Optional.empty();
        }
    }

    private static String getHash(String prompt) {
        return Hashing.sha256().hashString(prompt, StandardCharsets.UTF_8).toString();
    }

    @Builder(toBuilder = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prompt {
        private String prompt;
        private String prediction;
        private int retryCount;
        private int accessCount;
        private Date generatedTime;
        private Date lastAccessTime;
        private String inferenceProvider;
    }
}
