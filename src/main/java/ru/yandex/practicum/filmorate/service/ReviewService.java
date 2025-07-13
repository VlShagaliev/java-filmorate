package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Integer id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(Integer id) {
        return reviewStorage.getReviewById(id);
    }

    public Collection<Review> getReviews(Integer filmId, Integer count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public void likeReview(Integer reviewId, Integer userId) {
        reviewStorage.likeOrDislikeReview(reviewId, userId, true);
    }

    public void dislikeReview(Integer reviewId, Integer userId) {
        reviewStorage.likeOrDislikeReview(reviewId, userId, false);
    }

    public void removeReviewLike(Integer reviewId, Integer userId) {
        reviewStorage.removeReviewLikeOrDislike(reviewId, userId, true);
    }

    public void removeReviewDislike(Integer reviewId, Integer userId) {
        reviewStorage.removeReviewLikeOrDislike(reviewId, userId, false);
    }
}