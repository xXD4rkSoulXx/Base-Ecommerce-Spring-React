/*
 *  A pasta Specification é responsável por conter todos grandes quantidades de métodos, que
 * interagem com a base de dados. Enquanto que o Repository é para pequenas quantidades,
 * este é para muitos métodos, permitindo depois as suas combinações, evitando a necessidade
 * de criar muitos métodos, como por exemplo, 8 campos a serem combinados, 256 métodos no
 * Repository, e ao meter aqui no Specificator evita esse tanto.
 * */

package com.aprenderSpring.ecommerceSpring.specification;

import com.aprenderSpring.ecommerceSpring.domain.Product;
import com.aprenderSpring.ecommerceSpring.domain.Review;
import com.aprenderSpring.ecommerceSpring.domain.User;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecification {
    public static Specification<Review> byRating(Integer rating) {
        return (root, query, cb) ->
                rating == null
                        ? cb.conjunction()
                        : cb.equal(root.get("rating"), rating);
    }

    public static Specification<Review> ratingBetween(Integer minRating, Integer maxRating) {
        return (root, query, cb) -> {
            if (minRating == null && maxRating == null) {
                return cb.conjunction();
            }

            if (minRating != null && maxRating != null) {
                return cb.between(root.get("rating"), minRating, maxRating);
            }

            if (minRating != null) {
                return cb.greaterThanOrEqualTo(root.get("rating"), minRating);
            }

            return cb.lessThanOrEqualTo(root.get("rating"), maxRating);
        };
    }

    public static Specification<Review> byUser(User user) {
        return (root, query, cb) ->
                user == null
                        ? cb.conjunction()
                        : cb.equal(root.get("user"), user);
    }

    public static Specification<Review> byProduct(Product product) {
        return (root, query, cb) ->
                product == null
                        ? cb.conjunction()
                        : cb.equal(root.get("product"), product);
    }
}
