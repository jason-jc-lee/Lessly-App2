package com.lessly.lessly_web.ai;
import java.util.List;
import com.google.gson.*;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.*;
/**
 * GeminiAIAdvisor is the real AI implementation of AIAdvisor.
 * The script sends the inventory stats to Google Gemini and returns a short summary.
 * This class is isolated from the rest of the app, so only this class needs updating is Gemini changes at some point.
 */
@Component
public class GeminiAIAdvisor implements AIAdvisor {

    private static final String API_KEY = "AIzaSyDEAjdivR50tIiye_Kdiq2gjGIaZF4ci1M";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY;

    @Override
    public String generateSummary(int flaggedCount, double wastedSpend, int totalItems, double totalSpent, List<String> removalNames) {
        try {
            String prompt = buildPrompt(flaggedCount, wastedSpend, totalItems, totalSpent, removalNames);
            String requestBody = buildRequestBody(prompt);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body());

        } catch (Exception e) {
            //if the requests fails, return a fallback, so that the report page never crashes or shows a blank summary
            return "AI summary unavailable at this time.";
        }
    }


    /**
     * Builds the prompt sent to Gemini and only sends numerical stats and item names.
     * Item names are included so Gemini can give specific advice.
     */
    private String buildPrompt(int flaggedCount, double wastedSpend, int totalItems, double totalSpent, List<String> removalNames) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a friendly decluttering coach. ");
        prompt.append("Give a short 2-3 sentence motivational summary based on this data:\n");
        prompt.append("- Total items owned: ").append(totalItems).append("\n");
        prompt.append("- Total spent: $").append(String.format("%.2f", totalSpent)).append("\n");
        prompt.append("- Unused items (30+ days): ").append(flaggedCount).append("\n");
        prompt.append("- Money tied up in unused items: $").append(String.format("%.2f", wastedSpend)).append("\n");
        if (!removalNames.isEmpty()) {
            prompt.append("- Top items to remove: ").append(String.join(", ", removalNames)).append("\n");
        }
        prompt.append("Be encouraging, concise, and practical. Mention specific items if provided. Do not use markdown or bullet points.");
        return prompt.toString();
    }

    /**
     * Wraps the prompt in a JSON structure so Gemini can accept it.
     * Gson is utilized for a better way to build the JSON safely.
     * Otherwise, it may be hard to maintain.
     */

    private String buildRequestBody(String prompt) {
        JsonObject text = new JsonObject();
        text.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(text);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject body = new JsonObject();
        body.add("contents", contents);
        return body.toString();
    }

    /**
     * extracts the text from the summary Gemini's JSON generated.
     * also handles rate limit errors and replaces them with a fallback
     * This is to ensure the user never sees an error on the report page.
     */
    private String parseResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            // check for rate limit error
            if (json.has("error")) {
                int code = json.getAsJsonObject("error").get("code").getAsInt();
                if (code == 429) {//this means daily limit has been reached for the rate limit
                    return "You're making great progress! Keep reviewing your items regularly and consider decluttering anything you haven't used in over a month.";
                }
                return "AI summary unavailable at this time.";
            }
//this navigates the nested JSON structure so Gemini gets the text
            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        } catch (Exception e) {
            return "AI summary could not be generated.";
        }
    }
}
