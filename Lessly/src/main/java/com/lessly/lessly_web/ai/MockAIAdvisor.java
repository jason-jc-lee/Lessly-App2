package com.lessly.lessly_web.ai;
import java.util.List;
import org.springframework.stereotype.Component;
/**
 * MockAIAdvisor is a hardcoded version of AIAdvisor and used for testing
 * It produces responses based on the flagged item count, so unit tests and demos will work without an API key
 * Can be swapped in place of GeminiAIAdvisor in ItemController when submitting or testing.
 */

@Component
public class MockAIAdvisor implements AIAdvisor {

    @Override
    public String generateSummary(int flaggedCount, double wastedSpend, int totalItems, double totalSpent, List<String> removalNames) {
        // if nothing is flagged, return a positive message
        if (flaggedCount == 0) {
            return "Great work! Your space is looking minimal and organized. " +
                    "Keep up the healthy habit of using what you own!";
        } else {
            // Include specific item names if available even without a real AI call
            String items = removalNames.isEmpty() ? "" : " Consider removing: " + String.join(", ", removalNames) + ".";
            return String.format(
                    "You currently have %d unused item(s) tying up $%.2f.%s " +
                            "Small steps toward decluttering can make a big difference!",
                    flaggedCount, wastedSpend, items
            );
        }
    }
}