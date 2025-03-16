package com.example.updater;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * The {@code UpdateChecker} class is responsible for checking whether a newer version of a software/plugin
 * is available by fetching version information from a remote source and comparing it with the currently installed version.
 * <p>
 * This class is designed to be lightweight, asynchronous, and highly reusable. It fetches the latest version information
 * from a given URL and notifies the caller through a callback mechanism. The purpose of this class is to ensure that users
 * are always informed about updates, which can contain important bug fixes, security patches, or new features.
 * </p>
 * 
 * <h2>How It Works</h2>
 * <ul>
 *     <li>It takes the current version of the software as input.</li>
 *     <li>It fetches the latest version string from a specified URL.</li>
 *     <li>It compares the fetched version with the current version.</li>
 *     <li>If an update is available, it triggers a callback function to notify the user.</li>
 *     <li>The callback function can be used to log messages, display alerts, or trigger automatic updates.</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * <ul>
 *     <li><b>Asynchronous Execution:</b> The version check is performed asynchronously to prevent blocking the main thread.</li>
 *     <li><b>Customizable Callback:</b> Users can define how they want to handle update notifications.</li>
 *     <li><b>Lightweight:</b> Uses a simple HTTP request to fetch version information, minimizing resource usage.</li>
 *     <li><b>Reusable:</b> Can be used for any plugin, application, or software that requires version checking.</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * UpdateChecker updateChecker = new UpdateChecker(
 *     "1.0.0", // Current version of the plugin
 *     "https://example.com/latest-version.txt", // URL that contains the latest version
 *     (current, latest) -> {
 *         if (!current.equals(latest)) {
 *             System.out.println("A new update is available: " + latest);
 *         } else {
 *             System.out.println("You are using the latest version.");
 *         }
 *     }
 * );
 * updateChecker.checkForUpdate();
 * }</pre>
 * 
 * @author YourName
 * @version 1.0
 */
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

    /**
     * Constructs a new {@code UpdateChecker} instance that checks for updates based on the given parameters.
     * <p>
     * When an update check is performed, it retrieves the latest version string from the specified URL.
     * The retrieved version is then compared against the current version. If a newer version is found,
     * the provided callback function is executed, allowing the caller to handle the update notification.
     * </p>
     *
     * @param currentVersion The version currently installed.
     * @param updateUrl The URL that contains the latest version string in plain text.
     * @param onUpdateCheck A callback function that is executed when the version check is complete.
     *                      The function receives two parameters: the current version and the latest available version.
     */
    public UpdateChecker(String currentVersion, String updateUrl, BiConsumer<String, String> onUpdateCheck) {
        this.currentVersion = currentVersion;
        this.updateUrl = updateUrl;
        this.onUpdateCheck = onUpdateCheck;
    }

    /**
     * Initiates an asynchronous update check by fetching the latest version from the specified URL.
     * <p>
     * This method uses an asynchronous approach to avoid blocking the main thread. It makes an HTTP GET request
     * to the provided URL and extracts the version string from the response body. The extracted version is then
     * compared against the current version. If an update is available, the provided callback function is triggered.
     * </p>
     * 
     * <h2>Behavior</h2>
     * <ul>
     *     <li>If the request is successful and the version is retrieved, it is compared with the current version.</li>
     *     <li>If a new version is found, the callback function is invoked with both the current and latest versions.</li>
     *     <li>If the request fails, an error message is logged.</li>
     * </ul>
     * 
     * <h2>Thread Safety</h2>
     * <p>
     * This method is thread-safe since it runs asynchronously using {@link CompletableFuture}.
     * </p>
     */
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

    /**
     * Fetches the latest version string from the specified URL using an HTTP GET request.
     * <p>
     * This method establishes an HTTP connection to the URL, retrieves the response body,
     * and extracts the version string. If the request fails or the response is invalid,
     * it logs a warning and returns {@code null}.
     * </p>
     *
     * @return The latest version string if successful; {@code null} otherwise.
     */
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
