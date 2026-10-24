package com.lessly.lessly_web.ai;

import java.util.List;

/**
 * AIAdvisor is the one responsible for generating AI summaries.
 *It uses an interface that allowed the real Gemini implementation and the mock one to be swapped without changing other parts of the application, allowing for easier testing.
 */
public interface AIAdvisor {
    /**
     * will generate a short summary based on the user's inventory stats.
     * the summary is meant to encourage decluttering without replacing any important logic.
     */
    String generateSummary(int flaggedCount, double wastedSpend, int totalItems, double totalSpent, List<String> removalNames);
}