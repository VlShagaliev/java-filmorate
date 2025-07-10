package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewRowMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbStorage.class, ReviewRowMapper.class})
public class ReviewDbStorageTest {
    private final Path path = Paths.get("src/test/resources/clear-tables.sql");
    private final JdbcTemplate jdbc;
    private final ReviewDbStorage reviewDbStorage;
    private Review review;

    @BeforeEach
    void setUp() throws IOException {
        review = new Review(1, 1);
        review.setContent("Content1");
        review.setIsPositive(true);
        review.setUseful(0);

        String clearTablesQuery = Files.readString(path);
        String addTestContentQuery = """  
                INSERT INTO films (name, description, releaseDate, duration, id_rating)
                VALUES ('name1', 'description1', '2001-01-01', 100, 1);
                INSERT INTO users (login, name, email, birthday)
                VALUES ('login1', 'name1', 'user1@domain.com', '2001-01-01');
        """;
        jdbc.update(clearTablesQuery);
        jdbc.update(addTestContentQuery);
    }

    @Test
    void createReview() {
        Review createdReview = reviewDbStorage.addReview(review);

        assertThat(createdReview)
                .hasFieldOrPropertyWithValue("content", "Content1")
                .hasFieldOrPropertyWithValue("isPositive", true)
                .hasFieldOrPropertyWithValue("useful", 0);
    }

    @Test
    void updateReview() {
        Review createdReview = reviewDbStorage.addReview(review);
        createdReview.setContent("Content2");
        createdReview.setIsPositive(false);

        reviewDbStorage.updateReview(createdReview);

        Review updatedReview = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(updatedReview)
                .hasFieldOrPropertyWithValue("content", "Content2")
                .hasFieldOrPropertyWithValue("isPositive", false);
    }

    @Test
    void getReviewById() {
        Review createdReview = reviewDbStorage.addReview(review);
        Review retrievedReview = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(retrievedReview)
                .hasFieldOrPropertyWithValue("id", createdReview.getId())
                .hasFieldOrPropertyWithValue("content", "Content1");
    }

    @Test
    void getReviewsByFilmId() {
        reviewDbStorage.addReview(review);
        Review anotherReview = new Review(1, 1);
        anotherReview.setContent("Content2");
        anotherReview.setIsPositive(false);
        reviewDbStorage.addReview(anotherReview);

        Collection<Review> reviews = reviewDbStorage.getReviews(1, 10);

        assertThat(reviews)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void deleteReview() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.deleteReview(createdReview.getId());

        assertThatThrownBy(() -> reviewDbStorage.getReviewById(createdReview.getId()))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    void likeReview() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeOrDislikeReview(createdReview.getId(), 1, true);
        Review likedReview = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(likedReview.getUseful()).isEqualTo(1);
    }

    @Test
    void dislikeReview() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeOrDislikeReview(createdReview.getId(), 1, false);
        Review dislikedReview = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(dislikedReview.getUseful()).isEqualTo(-1);
    }

    @Test
    void removeLike() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeOrDislikeReview(createdReview.getId(), 1, true);
        reviewDbStorage.removeReviewLikeOrDislike(createdReview.getId(), 1, true);
        Review reviewAfterRemoveLike = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(reviewAfterRemoveLike.getUseful()).isEqualTo(0);
    }

    @Test
    void removeDislike() {
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.likeOrDislikeReview(createdReview.getId(), 1, false);
        reviewDbStorage.removeReviewLikeOrDislike(createdReview.getId(), 1, false);
        Review reviewAfterRemoveDislike = reviewDbStorage.getReviewById(createdReview.getId());

        assertThat(reviewAfterRemoveDislike.getUseful()).isEqualTo(0);
    }
}