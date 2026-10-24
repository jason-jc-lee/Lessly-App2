# Lessly-App
# Lessly: Declutter Your Belongings

Lessly is a Java web application that helps users track their belongings and manage impulsive shopping habits. Users can log items they own, track how often they use them, and receive suggestions on whether to keep, donate, sell, or discard items based on usage and value. A report summarizes spending habits and decluttering progress, with a real AI-generated motivational summary powered by Google Gemini.

## Features

- Add clothing, electronics, and perishable items to your inventory to track
- Items saved in a MySQL database via MAMP
- Each item has a status, determined by when it was added or used
- Items are flagged if unused for 30+ days or expired
- Users can manually alter an item's status and prevent auto-updating
- Report showing flagged items, removal suggestions, and a spending summary
- Expired items are always flagged
- Suggested removals are prioritized: expired perishables first, then longest unused, then cheapest
- AI-generated summaries powered by Google Gemini
- Falls back on a hardcoded summary if the AI is unavailable
- Full input validation on all forms

## Database

| Table | Columns |
|---|---|
| `items` | id, name, brand, price, purchase_date, last_used_date, status, category, manually_set |
| `clothing` | id (foreign key), size, color |
| `electronics` | id (foreign key), size, color |
| `perishables` | id (foreign key), opened_date, shelf_life_days |

## Design Decisions

### Class structure

`Item` is abstract and uses inheritance. Each subclass has its own data table containing its fields, linked to the `items` table by id. This prevents data duplication, keeps the OOP design intact, and lets `DeclutterService` work with any item that references `Item`.

- **Inventory:** handles storage and retrieval of items and doesn't contribute to decision making
- **DeclutterService:** handles flagging, suggestions, and prioritization of flagged items
- **ItemController:** handles passing and routing of data and has no say in the logic. It doesn't know whether it is talking to a real AI advisor or a mock
- **AIAdvisor:** hidden behind an interface so the AI implementation can switch freely without altering other classes

### Manually setting status

Users can lock or manually set an item's status using a drop-down on the main page (`index.html`). This causes the auto-suggest/update system to skip locked items entirely, so users can override the app's recommendations while keeping the system usable. Locked items that do not have "Keep" status still appear on the flagged list, since the user wants to take action on them.

### Auto-updating statuses

Item statuses are recalculated every time an item is saved, the report page is accessed, or an item is marked as used, rather than on a timer. This keeps the database in sync with the data.

### Declutter logic

Items are auto-flagged and auto-assigned statuses as follows:

- **Expired perishables** are always "Discard" (regardless of other factors)
  - A perishable expires if the difference between the current date and the opened date exceeds the shelf life days
  - An unopened item also expires if it hasn't been used in 1+ years
- **Unused 30+ days and >= $50:** "Sell"
- **Unused 30+ days and < $50:** "Donate"
- **Recently used:** always "Keep". Unopened perishables are first checked for whether over a year has passed since purchase

### Prioritized removal (Suggested Removals table)

1. Expired perishables first (most urgent)
2. Longest unused next
3. Cheapest if two items have the same unused time

## AI Integration

`AIAdvisor` is implemented by `GeminiAIAdvisor` and `MockAIAdvisor`. Swapping between them is done in `ItemController.java`, which makes the AI testable and functional without an API key.

### Using the real AI

An API key from Google Gemini is required. Paste the key into `GeminiAIAdvisor.java` on this line:

```java
private static final String API_KEY = "YOUR_API_KEY_HERE";
```

### Swapping between Mock and Gemini

In `ItemController.java`, change the line `private GeminiAIAdvisor aiAdvisor;` to `private MockAIAdvisor aiAdvisor;` to use the mock AI, and vice versa. `ItemController` also swaps to the mock automatically if the real AI advisor is down.

### The AI's role

The AI generates a 2-3 sentence summary from the user's inventory stats and items to remove, turning those numbers into a readable message that backs up the report.

The AI receives:

- Total items owned
- Total money spent that is currently in the user's inventory
- Number of flagged items
- Money tied up in unused items
- Names of priority items to remove

The AI may fail to respond if its rate limit is exceeded. In that case, the app falls back to a hardcoded message instead of showing an error.

## Tools Used

- Spring Boot (web framework)
- Spring Data JPA (database access)
- Thymeleaf (HTML templating)
- MySQL via MAMP (database)
- Google Gemini API (AI summaries)
- Gson (JSON parsing, used in `GeminiAIAdvisor.java`)
