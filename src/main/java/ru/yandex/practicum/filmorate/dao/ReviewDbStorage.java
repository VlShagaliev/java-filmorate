package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    private static final String INSERT_REVIEW =
            "INSERT INTO reviews(id_film, id_user, content, is_positive, useful) VALUES (?, ?, ?, ?, 0)";
    private static final String UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM reviews WHERE id = ?";
    private static final String SELECT_BY_FILM_ID =
            "SELECT * FROM reviews WHERE id_film = ? ORDER BY useful DESC LIMIT ?";
    private static final String SELECT_ALL = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String CHECK_REVIEW_ID_QUERY = "SELECT COUNT(*) FROM reviews WHERE id = ?";

    private static final String INSERT_LIKE_DISLIKE =
            "MERGE INTO reviews_likes(id_review, id_user, is_useful) KEY(id_review, id_user) VALUES (?, ?, ?)";
    private static final String DELETE_LIKE_DISLIKE =
            "DELETE FROM reviews_likes WHERE id_review = ? AND id_user = ? AND is_useful = ?";
    private static final String SELECT_USEFUL =
            "SELECT COALESCE(SUM(CASE WHEN is_useful THEN 1 ELSE -1 END), 0) FROM reviews_likes WHERE id_review = ?";
    private static final String UPDATE_USEFUL = "UPDATE reviews SET useful = ? WHERE id = ?";

    private final RowMapper<Review> reviewMapper;

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper, RowMapper<Review> reviewMapper) {
        super(jdbc, mapper);
        this.reviewMapper = reviewMapper;
    }

    @Override
    public Review addReview(Review review) {
        checkDbHasId(CHECK_USED_IN_DB, review.getUserId());
        checkDbHasId(CHECK_FILM_IN_DB, review.getFilmId());

        Integer id = insert(
                INSERT_REVIEW,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getIsPositive()
        );
        review.setId(id);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        updateSql(
                UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getId()
        );
        return getReviewById(review.getId());
    }

    @Override
    public void deleteReview(Integer id) {
        checkDbHasId(CHECK_REVIEW_ID_QUERY, id);
        jdbc.update(DELETE_REVIEW, id);
    }

    @Override
    public Review getReviewById(Integer id) {
        checkDbHasId(CHECK_REVIEW_ID_QUERY, id);
        return jdbc.queryForObject(SELECT_BY_ID, reviewMapper, id);
    }

    @Override
    public Collection<Review> getReviews(Integer filmId, Integer count) {
        if (filmId != null) {
            return jdbc.query(SELECT_BY_FILM_ID, reviewMapper, filmId, count);
        } else {
            return jdbc.query(SELECT_ALL, reviewMapper, count);
        }
    }

    @Override
    public void likeOrDislikeReview(Integer reviewId, Integer userId, boolean isUseful) {
        checkDbHasId(CHECK_REVIEW_ID_QUERY, reviewId);
        checkDbHasId(CHECK_USED_IN_DB, userId);

        jdbc.update(INSERT_LIKE_DISLIKE, reviewId, userId, isUseful);
        updateUseful(reviewId);
    }

    @Override
    public void removeReviewLikeOrDislike(Integer reviewId, Integer userId, boolean isLike) {
        checkDbHasId(CHECK_REVIEW_ID_QUERY, reviewId);
        checkDbHasId(CHECK_USED_IN_DB, userId);

        jdbc.update(DELETE_LIKE_DISLIKE, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    private void updateUseful(Integer reviewId) {
        Integer useful = jdbc.queryForObject(
                SELECT_USEFUL,
                Integer.class,
                reviewId
        );

        jdbc.update(UPDATE_USEFUL, useful, reviewId);
    }
}