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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        FilmDbStorage.class,
        UserDbStorage.class,
        LikesDbStorage.class,
        RecommendationService.class,
        FilmRowMapper.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RecommendationServiceTest {

    private final JdbcTemplate jdbcTemplate;
    private final RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы и сбрасываем sequence
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM ratings");

        // Инициализируем тестовые данные
        initTestData();
    }

    private void initTestData() {
        // Добавляем рейтинги и жанры
        jdbcTemplate.update("INSERT INTO ratings (id, name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия')");

        // Добавляем пользователей
        jdbcTemplate.update("""
            INSERT INTO users (id, login, name, email, birthday) VALUES\s
                (1, 'user1', 'User 1', 'user1@test.com', '1990-01-01'),
                (2, 'user2', 'User 2', 'user2@test.com', '1991-01-01'),
                (3, 'user3', 'User 3', 'user3@test.com', '1992-01-01')
           \s""");

        // Добавляем фильмы
        jdbcTemplate.update("""
            INSERT INTO films (id, name, description, releaseDate, duration, id_rating) VALUES
                (1, 'Film 1', 'Desc 1', ?, 120, 1),
                (2, 'Film 2', 'Desc 2', ?, 130, 1),
                (3, 'Film 3', 'Desc 3', ?, 140, 1),
                (4, 'Film 4', 'Desc 4', ?, 150, 1)
            """,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2001, 1, 1),
                LocalDate.of(2002, 1, 1),
                LocalDate.of(2003, 1, 1));

        // Добавляем лайки
        jdbcTemplate.update("""
            INSERT INTO likes (id_film, id_user) VALUES
                (1, 1),
                (2, 1),
                (1, 2),
                (2, 2),
                (3, 2),
                (1, 3),
                (4, 3)
            """);
    }

    @Test
    void getRecommendations_shouldReturnRecommendedFilms() {
        List<Film> recommendations = recommendationService.getRecommendations(1);

        assertThat(recommendations)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(3, 4);
    }

    @Test
    void getRecommendations_shouldReturnEmptyListWhenNoSimilarUsers() {
        // Добавляем нового пользователя без пересечений
        jdbcTemplate.update("""
            INSERT INTO users (id, login, name, email, birthday) VALUES\s
                (4, 'user4', 'User 4', 'user4@test.com', '1993-01-01')
           \s""");

        List<Film> recommendations = recommendationService.getRecommendations(4);

        assertThat(recommendations).isEmpty();
    }

    @Test
    void getRecommendations_shouldNotReturnAlreadyLikedFilms() {
        List<Film> recommendations = recommendationService.getRecommendations(1);

        assertThat(recommendations)
                .extracting(Film::getId)
                .doesNotContain(1, 2);
    }
}