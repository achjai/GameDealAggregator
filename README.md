# Game Deal Aggregator

## Project Overview

This project is a Java-based console application designed to work as a game deal aggregator and management system. It brings together several common features you would expect from a game marketplace or deal-tracking platform: user registration, secure login, game search and filtering, wishlist tracking, and admin tools for price management.

The application connects to PostgreSQL to store users, games, wishlist entries, notifications, and price-change data. It also pulls live game deal information from the CheapShark API and updates the local database when the app starts. If the API fails or returns no data, the app falls back to a built-in backup dataset so the system still has usable information.

The overall goal is to provide a functional, menu-driven project that demonstrates database integration, API consumption, password hashing, game filtering logic, and admin-side operations in a single Java application.

## Why This Project Exists

The app is meant to simulate a simplified version of a game deal platform, similar to services that track discounted games across stores. It allows a user to:

- search games by name or keyword
- narrow results by price and category
- sort results by relevance, popularity, rating, price, or name
- save games to a wishlist
- receive price-drop notifications for saved games

It also gives administrative users the ability to:

- manually set the price of a game
- apply a discount to an entire category
- undo the last price change
- run custom SQL against the database

This makes the project useful for demonstrating both end-user and admin workflows in a single application.

## Main Features

### 1. Fuzzy Search and Relevance Matching

A major strength of this project is its fuzzy search logic. Instead of only matching exact strings, the application calculates a relevance score between the user’s query and each game title. This helps users find games even when their search is slightly misspelled, partially typed, or not an exact title match.

The fuzzy matching takes place in the `MatchScorer` class, which uses the Apache Commons Text library and the Jaro-Winkler similarity algorithm. This is a widely used technique in real-world search systems because it handles minor character differences, transpositions, and partial matches more effectively than strict string equality.

The logic works as follows:

- normalize the query and title by converting to lowercase
- strip punctuation and extra whitespace
- check for exact or substring matches first
- if no strong direct match exists, compute a Jaro-Winkler similarity score
- rank results by relevance and display the score alongside each result

This means a search such as "half life" can still match a game named "Half-Life 2", and a slightly misspelled query like "counter strke" may still return relevant results. This is the same general idea used in real search engines, catalog systems, e-commerce title search, and recommendation systems where user input is imperfect.

In the application flow, this logic is integrated into `GameService.search()`. After the database filters by price and category, each game receives a match score, and games with zero score are removed when a keyword is used. The results are then sorted by relevance before being shown to the user.

This makes the application feel much more realistic and practical than a basic database query that only supports exact matching.

### 2. Authentication and Account Management

Users can create an account with a username, password, first name, and last name. Password validation ensures the password is strong enough before it is saved.

The login workflow checks whether a user exists, verifies the password using BCrypt hashing, and tracks failed login attempts. After a certain number of failed attempts, the user is blocked temporarily until the application is restarted.

The app supports at least two roles:

- USER
- ADMIN

The default admin account is created automatically if missing.

### 4. Search and Filtering

The browsing experience is implemented in the main menu and search system. The user can set filters such as:

- search text
- minimum price
- maximum price
- category
- sorting method

The app supports both relevance-based filtering and direct sorting by metrics such as:

- relevance
- lowest price
- highest price
- popularity
- rating
- name

Game matching is calculated using a custom scoring method. The search engine filters games according to the query and then sorts them by score or popularity depending on the selected mode.

### 5. Wishlist Functionality

Users can add favorite games to a wishlist and later view them in a dedicated menu. The wishlist is stored in the database, so items remain associated with the user account.

Wishlist entries are also connected to notifications. If a game price is changed and the user has that game on their wishlist, a notification is generated for that user.

### 6. Notifications and Price Drops

Notification support is a major part of the platform. When admin price updates occur, the system checks which users have a game in their wishlist and creates a notification for each one.

The notifications can be displayed in the menu system, so the user sees when a price drop occurs for a game they are tracking.

### 7. HTML Detail Pages

Users can inspect a selected game in detail and generate an HTML page. This allows a game’s information to be presented in a more readable format rather than just terminal output.

This feature is implemented via the HtmlGenerator class and is useful for presenting one selected deal in a browser-friendly output.

### 8. Admin Management Tools

Admins have several operational tools not available to regular users:

- set the price for an individual game
- apply a percentage discount to all games within a category
- undo the most recent price change
- execute raw SQL statements directly against the database

The app also keeps an undo stack using a custom generic stack implementation, allowing the admin to revert the previous price adjustment.

## Technology Stack

The project uses the following technologies:

- Java 8+ for the application logic
- PostgreSQL as the primary database system
- JDBC for database connectivity
- BCrypt for password hashing and verification
- Gson for consuming JSON responses
- CheapShark API for live deal data
- Custom Java models and DAO classes for abstraction

## Project Structure

```text
GameDealAggregator/
├── src/
│   ├── AdminService.java
│   ├── ApiTest.java
│   ├── AuthService.java
│   ├── DatabaseConnection.java
│   ├── DataSeeder.java
│   ├── Game.java
│   ├── GameDAO.java
│   ├── GameService.java
│   ├── GenericStack.java
│   ├── HtmlGenerator.java
│   ├── Main.java
│   ├── MatchScorer.java
│   ├── Notification.java
│   ├── NotificationDAO.java
│   ├── PasswordValidator.java
│   ├── PriceChange.java
│   ├── User.java
│   ├── UserDAO.java
│   └── WishlistDAO.java
├── .gitignore
├── GameDealAggregator.iml
├── README.md
└── out/
```

## Key Classes and Their Roles

### Main.java
This is the entry point for the project. It creates the main menu loop, manages the current user, and routes the user to either the regular user experience or the admin experience. It controls the console flow for login, signup, browsing, wishlist actions, and notifications.

### AuthService.java
This class handles login, signup, logout, and password rules. It validates strong passwords, checks for duplicate usernames, hashes passwords, and verifies credentials before allowing access.

### GameService.java
This class contains the business logic for searching and sorting games. It applies price ranges, category filters, relevance scoring, and sorting rules before returning paginated results.

### GameDAO.java
This class acts as the persistence layer for game-related queries. It retrieves games by ID, loads games by category, fetches all categories, and executes filtered queries against the PostgreSQL database.

### UserDAO.java
This class handles reading and writing user records. It checks whether a username exists, inserts new users, and finds a user by username to support login.

### WishlistDAO.java
This class manages the user wishlist. It allows games to be added or removed from a wishlist and retrieves the list of saved games for a specific user.

### NotificationDAO.java
This class handles storing and retrieving notifications. Notifications are tied to a user and message and are used to inform users when deals change or prices drop.

### DataSeeder.java
This class is responsible for bootstrapping the application. It ensures the admin account exists, inserts free-to-play titles if needed, and attempts to fetch data from CheapShark. If the API is unavailable, it uses backup game entries so the app remains usable.

### AdminService.java
This class handles price changes, category-wide discounts, notification dispatch, raw SQL execution, and undo operations. It maintains a stack of previous price changes so that administrators can reverse the last update.

### DatabaseConnection.java
This class configures the PostgreSQL connection using local connection details. It provides the database connection object used throughout the application.

### MatchScorer.java
This class calculates how strongly a game matches a search term. It helps the app rank search results by relevance when the user performs a keyword search. It uses normalized text and Jaro-Winkler similarity to emulate the way real-world fuzzy search systems match imperfect user input.

## Fuzzy Search in Real-World Applications

The fuzzy search behavior in this project is intentionally designed to reflect how search systems behave in practice. In real applications, users rarely enter perfect query strings. They might type:

- partial names
- misspellings
- missing punctuation
- abbreviated titles

A search engine that only checks exact matches would frustrate users and produce poor results. A fuzzy matching strategy solves this by assigning a similarity score between the query and candidate results.

This project demonstrates that approach in a simplified form:

- user enters a keyword
- game titles are normalized and compared
- close matches receive a higher score
- results are sorted by similarity before display

This is especially useful in game catalogs, online stores, and product search systems where users may not know the exact title, spelling, or formatting of the item they are looking for.

### HtmlGenerator.java
This class creates a readable HTML page representing a selected game, letting the user view more information in a browser-like format.

## Database Design

The system depends on PostgreSQL tables for the following core entities:

- users
- games
- wishlist
- notifications
- stores

The app also relies on stored procedure-like actions and queries for key tasks such as:

- updating a game price
- updating all prices in a category
- adding games to user wishlists
- retrieving filtered games

This architecture keeps the Java code cleaner by pushing some of the logic to the database layer where appropriate.

## Runtime Flow

When the application starts, the sequence is roughly:

1. Database connection is established.
2. Data seeder runs.
3. Admin user is created or refreshed if missing.
4. Free games are inserted if needed.
5. CheapShark data is fetched.
6. Games are upserted into the database.
7. The welcome menu appears.

From there, the user may:

- log in
- create a new account
- browse and search games
- add items to a wishlist
- view notifications
- log out

If the logged-in user is an admin, a different menu appears with management controls.

## Database Setup Instructions

This application expects a PostgreSQL database called `game_aggregator`.

### Connection Settings

The defaults in the project are:

- Host: `localhost`
- Port: `5432`
- Database: `game_aggregator`
- Username: `postgres`
- Password: `root`

You can create the database using PostgreSQL tooling:

```sql
CREATE DATABASE game_aggregator;
```

Make sure PostgreSQL is running before launching the application. If your local Postgres credentials differ, update the values in `DatabaseConnection.java`.

## Default Admin Login

The application creates or updates an admin automatically on startup.

- Username: `admin`
- Password: `admin123`

This is useful for testing the admin workflow without manually creating an admin account.

## How to Run the Project

### Option 1: IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Ensure Java is configured correctly.
3. Add the required libraries for PostgreSQL, BCrypt, and Gson to the project classpath.
4. Run the `Main` class.

### Option 2: Command Line

Compile the Java files:

```bash
javac -cp "path/to/jbcrypt.jar:path/to/postgresql.jar:path/to/gson.jar" -d out src/*.java
```

Run the app:

```bash
java -cp "out:path/to/jbcrypt.jar:path/to/postgresql.jar:path/to/gson.jar" Main
```

> On Windows, the classpath separator is `;` instead of `:`.

## Example User Workflow

A typical user flow looks like this:

1. Sign up as a new user.
2. Log in with the new account.
3. Browse games with price and category filters.
4. Search for a title using keywords.
5. View detail information about a selected game.
6. Add the game to the wishlist.
7. Receive notifications when the price drops.

## Example Admin Workflow

An admin user can do the following:

1. Log in with the admin account.
2. Open the admin menu.
3. Set a game’s price manually.
4. Apply a discount to all games in a category.
5. Review the system response.
6. Undo the last change if needed.
7. Execute SQL queries as required for testing or maintenance.

## External API Integration

The app connects to the CheapShark API to retrieve game deals. The code requests a set of deals per store and then transforms the JSON data into Java `Game` objects.

The process is designed to keep the app functional even when the live API is unavailable. This is handled by a fallback backup list stored in the seeding logic.

## Limitations and Notes

- This is a console-based application rather than a web application.
- It depends on a local PostgreSQL instance.
- Some features assume the schema already exists in the database.
- The app is oriented toward a learning/demo environment rather than production deployment.
- The dataset is based on game deal information and may be limited by API availability or local database state.

## License

This project is intended for academic, educational, or demonstration use. It does not currently include a formal open-source license file.

## Summary

Game Deal Aggregator is a compact but feature-rich Java project that combines database-driven storage, search logic, deal tracking, and admin-side price management. It is a strong example of how to build a menu-driven application that integrates external APIs, persistence, and role-based workflows in a single codebase.
