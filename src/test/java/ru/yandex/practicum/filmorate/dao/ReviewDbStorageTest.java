package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbStorage.class, ReviewRowMapper.class, UserDbStorage.class})
public class ReviewDbStorageTest {
    private final ReviewDbStorage reviewDbStorage;
    private final UserDbStorage userDbStorage;

    private Film testFilm;
    private User testUser;
    private Review review;

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        testUser = new User();
        testUser.setEmail("user@example.com");
        testUser.setLogin("testUser");
        testUser.setName("Test User");
        testUser.setBirthday("2000-01-01");
        testUser = userDbStorage.addUser(testUser); // Сохранение пользователя в БД

        // Создание тестового фильма
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("A film for testing.");
        testFilm.setReleaseDate("2022-01-01");
        testFilm.setDuration(120);
        testFilm = reviewDbStorage.addFilm(testFilm); // Сохранение фильма в БД

        // Создание отзыва
        review = new Review(testFilm.getId(), testUser.getId());
        review.setContent("Excellent movie!");
        review.setPositive(true);
        review.setUseful(0);
    }

    @Test
    void createReview_shouldCreateAndReturnReview() {
        Review createdReview = reviewDbStorage.addReview(review);

        assertThat(createdReview)
                .hasFieldOrPropertyWithValue("content", "Excellent movie!")
                .hasFieldOrPropertyWithValue("isPositive", true)
                .hasFieldOrPropertyWithValue("useful", 0);
    }

    @Test
    void updateReview_shouldUpdateReview() {
        Review createdReview = reviewDbStorage.addReview(review);
        createdReview.setContent("Updated content");
        createdReview.setPositive(false);

        reviewDbStorage.updateReview(createdReview);

        Review updatedReview = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(updatedReview)
                .hasFieldOrPropertyWithValue("content", "Updated content")
                .hasFieldOrPropertyWithValue("isPositive", false);
    }

    @Test
    void getReviewById_shouldReturnReviewById() {
        Review createdReview = reviewDbStorage.addReview(review);

        Review retrievedReview = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(retrievedReview)
                .hasFieldOrPropertyWithValue("id", createdReview.getId())
                .hasFieldOrPropertyWithValue("content", "Excellent movie!");
    }

    @Test
    void getReviewsByFilmId_shouldReturnListOfReviews() {
        reviewDbStorage.addReview(review);
        Review anotherReview = new Review(testFilm.getId(), testUser.getId());
        anotherReview.setContent("Not so great...");
        anotherReview.setPositive(false);
        reviewDbStorage.addReview(anotherReview);

        var reviews = reviewDbStorage.getReviews(testFilm.getId(), 10); // Фильмы с id = testFilm.getId()

        assertThat(reviews)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void deleteReview_shouldDeleteReview() {
        Review createdReview = reviewDbStorage.addReview(review);

        reviewDbStorage.deleteReview(createdReview.getId());

        assertThat(reviewDbStorage.getReviewById(createdReview.getId())).isEmpty();
    }

    @Test
    void likeReview_shouldIncreaseUsefulRating() {
        Review createdReview = reviewDbStorage.addReview(review);

        reviewDbStorage.likeReview(createdReview.getId(), testUser.getId(), true); // Лайк от пользователя

        Review likedReview = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(likedReview.getUseful()).isEqualTo(1); // Рейтинг полезности должен увеличиться на 1
    }

    @Test
    void dislikeReview_shouldDecreaseUsefulRating() {
        Review createdReview = reviewDbStorage.addReview(review);

        reviewDbStorage.likeReview(createdReview.getId(), testUser.getId(), false); // Дизлайк от пользователя

        Review dislikedReview = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(dislikedReview.getUseful()).isEqualTo(-1); // Рейтинг полезности должен уменьшиться на 1
    }

    @Test
    void removeLike_shouldDecreaseUsefulRatingBackToZero() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeReview(createdReview.getId(), testUser.getId(), true); // Лайк от пользователя

        reviewDbStorage.removeReviewLike(createdReview.getId(), testUser.getId());

        Review reviewAfterRemove = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(reviewAfterRemove.getUseful()).isEqualTo(0); // Рейтинг полезности должен вернуться в 0
    }

    @Test
    void removeDislike_shouldIncreaseUsefulRatingBackToZero() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeReview(createdReview.getId(), testUser.getId(), false); // Дизлайк от пользователя

        reviewDbStorage.removeReviewLike(createdReview.getId(), testUser.getId());

        Review reviewAfterRemove = reviewDbStorage.getReviewById(createdReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        assertThat(reviewAfterRemove.getUseful()).isEqualTo(0); // Рейтинг полезности должен вернуться в 0
    }
}
