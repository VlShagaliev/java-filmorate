package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserEventStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserEventStorage userEventStorage;

    public Review addReview(Review review) {
        Review newReview = reviewStorage.addReview(review);
        userEventStorage.add(newReview.getUserId(), newReview.getId(), UserEventType.REVIEW, UserEventOperation.ADD);
        return newReview;
    }

    public Review updateReview(Review review) {
        Review updatedReview = reviewStorage.updateReview(review);
        userEventStorage.add(updatedReview.getUserId(), updatedReview.getId(), UserEventType.REVIEW,
                UserEventOperation.UPDATE);
        return updatedReview;
    }

    public void deleteReview(Integer id) {
        Review deletedReview = reviewStorage.getReviewById(id);
        reviewStorage.deleteReview(id);
        userEventStorage.add(deletedReview.getUserId(), deletedReview.getId(), UserEventType.REVIEW,
                UserEventOperation.REMOVE);
    }

    public Review getReviewById(Integer id) {
        return reviewStorage.getReviewById(id);
    }

    public Collection<Review> getReviews(Integer filmId, Integer count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public void likeReview(Integer reviewId, Integer userId) {
        reviewStorage.likeOrDislikeReview(reviewId, userId, true);
        userEventStorage.add(userId, reviewId, UserEventType.LIKE, UserEventOperation.ADD);
    }

    public void dislikeReview(Integer reviewId, Integer userId) {
        reviewStorage.likeOrDislikeReview(reviewId, userId, false);
        userEventStorage.add(userId, reviewId, UserEventType.LIKE, UserEventOperation.ADD);
    }

    public void removeReviewLike(Integer reviewId, Integer userId) {
        reviewStorage.removeReviewLikeOrDislike(reviewId, userId, true);
        userEventStorage.add(userId, reviewId, UserEventType.LIKE, UserEventOperation.REMOVE);
    }

    public void removeReviewDislike(Integer reviewId, Integer userId) {
        reviewStorage.removeReviewLikeOrDislike(reviewId, userId, false);
        userEventStorage.add(userId, reviewId, UserEventType.LIKE, UserEventOperation.REMOVE);
    }
}