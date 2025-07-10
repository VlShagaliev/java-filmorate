package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Integer id);

    Review getReviewById(Integer id);

    Collection<Review> getReviews(Integer filmId, Integer count);

    void likeOrDislikeReview(Integer reviewId, Integer userId, boolean isUseful);

    void removeReviewLikeOrDislike(Integer reviewId, Integer userId, boolean isLike);
}