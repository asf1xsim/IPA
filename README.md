# Indie Publisher Assistant (IPA)

A web application that helps small indie game studios decide where and how to release their game. The user enters the parameters of a planned game (genre, platform, expected rating), and the system uses historical data (16,500+ records) to estimate its chances of success and recommend a publishing strategy.

## Problem and Solution

Indie developers often have limited budgets for marketing and localization. They don't know whether to release on PC or Switch, or whether a Japanese translation is worth the investment.

IPA acts as a "digital consultant": it analyzes historical sales and ratings and turns them into concrete recommendations.

### Example use cases

**Localization budget.** A small studio working on an RPG can afford only one translation and hesitates between German and Japanese. IPA compares regional sales for the genre and recommends the language with the larger market share.

**Platform for porting.** A developer with a finished PC action game wants to port it to Nintendo Switch or PlayStation 5. IPA compares sales of similar titles on both platforms, takes user ratings into account, and recommends the more promising one.

## Features

- Simulation of a planned game based on genre, platform and expected rating
- Recommendation of the priority region and localization language
- Platform comparison for porting decisions
- Saving simulations and projects to a user account
- User authentication via OAuth2 (Keycloak)
- Interactive API documentation (Swagger / OpenAPI)

## Calculations

| Metric | What it does |
|--------|--------------|
| **Weighted Success Score** | Combines critic score (70 %) and user score (30 %) to find games that are well rated across the whole audience. |
| **Revenue Predictor** | Estimates sales in Europe from the historical EU/NA sales ratio for a given genre. |
| **Global Adaptability Score** | Computes the percentage split of sales across NA, EU and JP. Genres with balanced sales score higher; genres dependent on a single market score lower. |
| **Region Priority Score** | Ranks regions by the ratio of genre sales to the number of competing titles, showing where market resistance is lowest. |
| **Investment Risk** | Calculates the share (in %) of similar games that missed minimum sales targets despite good ratings. |

## Tech Stack

- **Backend:** Java, Spring Boot, Maven
- **Database:** PostgreSQL
- **Security:** OAuth2 with Keycloak
- **Frontend:** [simple web interface: HTML/ JS]
- **API docs:** Swagger / OpenAPI
- **Infrastructure:** Docker, Docker Compose
- **Data:** [Video Game Sales with Ratings](https://www.kaggle.com/) dataset from Kaggle

## Database Model

- `games` – basic game information (title, genre, year)
- `platforms` – gaming platforms and their manufacturers
- `sales_data` – sales per game and region (EU, NA, JP)
- `reviews` – critic and user ratings
- `user_projects` – saved simulations and user projects

University project for the course "Enterprise aplikace", Mendel University in Brno, 2025/2026.

## Authors

Anton Simikin

**My role:** [backend, calculation algorithms, Keycloak integration, Docker setup]
