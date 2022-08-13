package com.example.cryptocurrencyexchanger.service.review;

import com.example.cryptocurrencyexchanger.entity.review.Review;
import com.example.cryptocurrencyexchanger.repo.ReviewRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExchangerReviewService implements ReviewService {

    ReviewRepository reviewRepository;

    @Override
    public Review saveNewReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReviewById(Long id) {
        Review review = reviewRepository.findById(id).get();

        reviewRepository.delete(review);
    }

    @Override
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    public List<Review> getReviewsForTitlePage() {
        return reviewRepository.getFourRandomReviews();
    }
}
