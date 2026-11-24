package com.latiendita.reviewService.review;

import com.latiendita.reviewService.review.dto.ModerateReviewRequest;
import com.latiendita.reviewService.review.dto.ReviewDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public Page<ReviewDto> list(@RequestParam(required = false) String status,
                                @RequestParam(required = false) Long productId,
                                @PageableDefault(size = 20, sort = "createdAt",
                                        direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.listForModeration(status, productId, pageable);
    }

    @PatchMapping("/{reviewId}/status")
    public ReviewDto moderate(@PathVariable UUID reviewId,
                              @Valid @RequestBody ModerateReviewRequest request) {
        return reviewService.moderate(reviewId, request);
    }
}
