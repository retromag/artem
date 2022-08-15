package com.example.cryptocurrencyexchanger.repo;

import com.example.cryptocurrencyexchanger.entity.review.Review;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends PagingAndSortingRepository<Review, Long> {
    @Query(value = "SELECT * FROM review order by random() LIMIT 4", nativeQuery = true)
    List<Review> getFourRandomReviews();
}
