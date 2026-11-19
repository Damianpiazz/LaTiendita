package com.latiendita.reviewService.review;

import com.latiendita.reviewService.review.dto.CreateReviewRequest;
import com.latiendita.reviewService.review.dto.ReviewDto;
import com.latiendita.reviewService.review.dto.ReviewSummaryDto;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public Page<ReviewDto> list(@RequestParam Long productId,
                                @PageableDefault(size = 10, sort = "createdAt",
                                        direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.listApproved(productId, pageable);
    }

    @GetMapping("/summary")
    public ReviewSummaryDto summary(@RequestParam Long productId) {
        return reviewService.summary(productId);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDto created = reviewService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{reviewId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{reviewId}/helpful")
    public ReviewDto markHelpful(@PathVariable UUID reviewId) {
        return reviewService.markHelpful(reviewId);
    }
}
