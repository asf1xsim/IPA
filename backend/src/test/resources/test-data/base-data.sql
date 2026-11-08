-- Platformy
INSERT INTO platforms (id, name, manufacturer) VALUES (1, 'PS4', 'Sony');
INSERT INTO platforms (id, name, manufacturer) VALUES (2, 'PC', 'Various');

-- Hry
INSERT INTO games (id, title, genre, year_of_release, publisher, platform_id)
VALUES (1, 'The Witcher 3', 'RPG', 2015, 'CD Projekt', 1);
INSERT INTO games (id, title, genre, year_of_release, publisher, platform_id)
VALUES (2, 'Cyberpunk 2077', 'RPG', 2020, 'CD Projekt', 2);
INSERT INTO games (id, title, genre, year_of_release, publisher, platform_id)
VALUES (3, 'FIFA 23', 'Sports', 2022, 'EA Sports', 1);
INSERT INTO games (id, title, genre, year_of_release, publisher, platform_id)
VALUES (4, 'Dark Souls', 'RPG', 2011, 'FromSoftware', 1);

-- Sales data
INSERT INTO sales_data (id, game_id, na_sales, eu_sales, jp_sales)
VALUES (1, 1, 5.00, 8.00, 1.00);
INSERT INTO sales_data (id, game_id, na_sales, eu_sales, jp_sales)
VALUES (2, 2, 4.00, 6.00, 0.50);
INSERT INTO sales_data (id, game_id, na_sales, eu_sales, jp_sales)
VALUES (3, 3, 3.00, 4.00, 0.20);
INSERT INTO sales_data (id, game_id, na_sales, eu_sales, jp_sales)
VALUES (4, 4, 2.00, 3.00, 2.00);

-- Reviews
INSERT INTO reviews (id, game_id, critic_score, user_score)
VALUES (1, 1, 93.00, 9.20);
INSERT INTO reviews (id, game_id, critic_score, user_score)
VALUES (2, 2, 75.00, 7.00);
INSERT INTO reviews (id, game_id, critic_score, user_score)
VALUES (3, 3, 60.00, 5.50);
INSERT INTO reviews (id, game_id, critic_score, user_score)
VALUES (4, 4, 89.00, 8.80);

-- User projects (username = test_user z Keycloaku)
INSERT INTO user_projects (id, username, project_name, target_genre, target_platform, calculated_risk, created_at)
VALUES (1, 'test_user', 'My RPG Game', 'RPG', 'PS4', 25.50, NOW());
INSERT INTO user_projects (id, username, project_name, target_genre, target_platform, calculated_risk, created_at)
VALUES (2, 'test_user', 'My Sports Game', 'Sports', 'PC', 40.00, NOW());
INSERT INTO user_projects (id, username, project_name, target_genre, target_platform, calculated_risk, created_at)
VALUES (3, 'test_admin', 'Other User Project', 'RPG', 'PC', 10.00, NOW());

SELECT setval('user_projects_id_seq', (SELECT MAX(id) FROM user_projects));