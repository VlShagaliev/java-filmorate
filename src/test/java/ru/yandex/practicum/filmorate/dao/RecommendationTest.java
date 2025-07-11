package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        FilmDbStorage.class,
        UserDbStorage.class,
        LikesDbStorage.class,
        UserService.class,
        FilmRowMapper.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RecommendationTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    @BeforeEach
    void setUp() {
        clearDatabase();
        initTestData();
    }

    private void clearDatabase() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM ratings");
    }

    private void initTestData() {
        // Добавляем рейтинги и жанры
        jdbcTemplate.update("INSERT INTO ratings (id, name) VALUES (1, 'G'), (2, 'PG')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия'), (2, 'Драма')");

        // Добавляем пользователей
        jdbcTemplate.update("""
            INSERT INTO users (id, login, name, email, birthday) VALUES
                (1, 'user1', 'User 1', 'user1@test.com', '1990-01-01'),
                (2, 'user2', 'User 2', 'user2@test.com', '1991-01-01'),
                (3, 'user3', 'User 3', 'user3@test.com', '1992-01-01'),
                (4, 'user4', 'User 4', 'user4@test.com', '1993-01-01'),
                (5, 'user5', 'User 5', 'user5@test.com', '1994-01-01')
            """);

        // Добавляем фильмы
        jdbcTemplate.update("""
            INSERT INTO films (id, name, description, releaseDate, duration, id_rating) VALUES
                (1, 'Film 1', 'Desc 1', '2000-01-01', 120, 1),
                (2, 'Film 2', 'Desc 2', '2001-01-01', 130, 1),
                (3, 'Film 3', 'Desc 3', '2002-01-01', 140, 2),
                (4, 'Film 4', 'Desc 4', '2003-01-01', 150, 2),
                (5, 'Film 5', 'Desc 5', '2004-01-01', 160, 1),
                (6, 'Film 6', 'Desc 6', '2005-01-01', 170, 2)
            """);

        // Добавляем жанры фильмам
        jdbcTemplate.update("INSERT INTO films_genres (id_film, id_genre) VALUES (1, 1), (2, 1), (3, 2), (4, 2)");

        // Добавляем лайки
        jdbcTemplate.update("""
            INSERT INTO likes (id_film, id_user) VALUES
                (1, 1), (2, 1), (3, 1),  -- User 1 likes 1,2,3
                (1, 2), (2, 2), (4, 2),  -- User 2 likes 1,2,4
                (1, 3), (5, 3), (6, 3),  -- User 3 likes 1,5,6
                (2, 4), (3, 4), (4, 4)   -- User 4 likes 2,3,4
            """);
    }

    @Test
    void getRecommendations_shouldReturnRecommendedFilms() {
        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations)
                .hasSize(3)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(4, 5, 6);
    }

    @Test
    void getRecommendations_shouldReturnEmptyListWhenNoSimilarUsers() {
        jdbcTemplate.update("DELETE FROM likes WHERE id_user = 2 OR id_user = 3 OR id_user = 4");

        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations).isEmpty();
    }

    @Test
    void getRecommendations_shouldNotReturnAlreadyLikedFilms() {
        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations)
                .extracting(Film::getId)
                .doesNotContain(1, 2, 3);
    }

    @Test
    void getRecommendations_shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getRecommendations(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с данным id = 999 отсутствует");
    }

    @Test
    void getRecommendations_shouldWorkWithSingleSimilarUser() {
        jdbcTemplate.update("DELETE FROM likes WHERE id_user = 3 OR id_user = 4");

        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations)
                .extracting(Film::getId)
                .containsExactly(4);
    }

    @Test
    void getRecommendations_shouldReturnEmptyWhenAllFilmsLiked() {
        jdbcTemplate.update("INSERT INTO likes (id_film, id_user) VALUES (4,1), (5,1), (6,1)");

        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations).isEmpty();
    }

    @Test
    void getRecommendations_shouldWorkWithMultipleSimilarUsers() {
        jdbcTemplate.update(
                "INSERT INTO films (id, name, description, releaseDate, duration, id_rating) VALUES (7, 'Film 7', 'Desc 7', '2006-01-01', 180, 1)");

        jdbcTemplate.update(
                "INSERT INTO users (id, login, name, email, birthday) VALUES (6, 'user6', 'User 6', 'user6@test.com', '1995-01-01')");

        jdbcTemplate.update(
                "INSERT INTO likes (id_film, id_user) VALUES (1,6), (2,6), (7,6)");

        List<Film> recommendations = userService.getRecommendations(1);

        assertThat(recommendations)
                .extracting(Film::getId)
                .contains(7);
    }
}