package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review(
                resultSet.getInt("id_film"),
                resultSet.getInt("id_user")
        );
        review.setId(resultSet.getInt("id"));
        review.setContent(resultSet.getString("content"));
        review.setIsPositive(resultSet.getBoolean("is_positive"));
        review.setUseful(resultSet.getInt("useful"));
        return review;
    }
}