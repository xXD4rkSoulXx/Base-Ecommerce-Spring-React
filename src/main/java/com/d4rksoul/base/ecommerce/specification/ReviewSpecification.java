/*
 *  A pasta Specification é responsável por conter todos grandes quantidades de métodos, que
 * interagem com a base de dados. Enquanto que o Repository é para pequenas quantidades,
 * este é para muitos métodos, permitindo depois as suas combinações, evitando a necessidade
 * de criar muitos métodos, como por exemplo, 8 campos a serem combinados, 256 métodos no
 * Repository, e ao meter aqui no Specificator evita esse tanto.
 * */

package com.d4rksoul.base.ecommerce.specification;

import com.d4rksoul.base.ecommerce.domain.Product;
import com.d4rksoul.base.ecommerce.domain.Review;
import com.d4rksoul.base.ecommerce.domain.User;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ReviewSpecification {
    private final String RATING = "rating";

    public static Specification<Review> byRating(Integer rating) {
        return (root, _, cb) ->
                rating == null
                        ? cb.conjunction()
                        : cb.equal(root.get(RATING), rating);
    }

    public static Specification<Review> ratingBetween(Integer minRating, Integer maxRating) {
        return (root, _, cb) -> {
            if (minRating == null && maxRating == null) {
                return cb.conjunction();
            }

            if (minRating != null && maxRating != null) {
                return cb.between(root.get(RATING), minRating, maxRating);
            }

            if (minRating != null) {
                return cb.greaterThanOrEqualTo(root.get(RATING), minRating);
            }

            return cb.lessThanOrEqualTo(root.get(RATING), maxRating);
        };
    }

    public static Specification<Review> byUser(User user) {
        return (root, _, cb) ->
                user == null
                        ? cb.conjunction()
                        : cb.equal(root.get("user"), user);
    }

    public static Specification<Review> byProduct(Product product) {
        return (root, _, cb) ->
                product == null
                        ? cb.conjunction()
                        : cb.equal(root.get("product"), product);
    }
}
