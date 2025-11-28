package de.lioncraft.lionapi.mojang;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.scheduler.Scheduler;
import com.velocitypowered.api.plugin.PluginContainer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangUUIDFetcher {

    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";

    public MojangUUIDFetcher() {
    }

    /**
     * Gets a player's UUID from their username using the Mojang API asynchronously.
     *
     * @param username The Minecraft username.
     * @return A CompletableFuture that will contain the UUID if successful, or be empty.
     */
    public CompletableFuture<Optional<UUID>> getUUID(String username) {
        // Run the network request on a separate thread provided by the scheduler
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Sending Player Request for "+username);
                URL url = new URL(MOJANG_API_URL + username);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5 second timeout

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Success: Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse the JSON response
                    Gson parser = new Gson();
                    JsonObject jsonObject = (JsonObject) parser.fromJson(response.toString(), JsonObject.class);

                    // The Mojang API returns the UUID without hyphens
                    String uuidString = jsonObject.getAsJsonPrimitive("id").getAsString();

                    // Format the UUID string with hyphens for the Java UUID constructor
                    String formattedUUID = uuidString.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                            "$1-$2-$3-$4-$5"
                    );
                    System.out.println("Received Player Data: "+formattedUUID);

                    return Optional.of(UUID.fromString(formattedUUID));

                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    // Player not found or name doesn't exist
                    return Optional.empty();
                } else {
                    // Handle other HTTP errors (e.g., rate limiting, server error)
                    System.err.println("Mojang API returned non-OK response code: " + responseCode + " for user: " + username);
                    return Optional.empty();
                }

            } catch (Exception e) {
                System.err.println("Error fetching UUID for user " + username + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}