package com.example.cryptocurrencyexchanger.service.review;

import com.example.cryptocurrencyexchanger.entity.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    Review saveNewReview(Review review);

    void deleteReview(Review review);

    Page<Review> getAllReviews(Pageable pageable);

    List<Review> getReviewsForTitlePage();
}
