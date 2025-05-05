package com.example.updater;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
public class UpdateChecker {
    
    /** The currently installed version of the software/plugin. */
    private final String currentVersion;
    
    /** The URL from which the latest version information will be fetched. */
    private final String updateUrl;
    
    /** A callback function that gets executed when an update check is completed. */
    private final BiConsumer<String, String> onUpdateCheck;

    /** A reusable OkHttpClient instance for making HTTP requests efficiently. */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    public UpdateChecker(String currentVersion, String updateUrl, BiConsumer<String, String> onUpdateCheck) {
        this.currentVersion = currentVersion;
        this.updateUrl = updateUrl;
        this.onUpdateCheck = onUpdateCheck;
    }
    
    public void checkForUpdate() {
        CompletableFuture.supplyAsync(this::fetchLatestVersion)
                .thenAccept(latestVersion -> {
                    if (latestVersion != null) {
                        onUpdateCheck.accept(currentVersion, latestVersion);
                    }
                })
                .exceptionally(ex -> {
                    log.warn("Failed to check for updates: " + ex.getMessage());
                    return null;
                });
    }

    private String fetchLatestVersion() {
        Request request = new Request.Builder().url(updateUrl).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string().trim();
            } else {
                log.warn("Failed to fetch version information: " + response.message());
                return null;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch version information: " + e.getMessage());
            return null;
        }
    }
}
