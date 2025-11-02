package com.readour.community.service;

import com.readour.common.entity.Book;
import com.readour.common.entity.User;
import com.readour.community.entity.Post;
import com.readour.community.enums.PostSearchType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PostSpecification {
    public Specification<Post> search(PostSearchType searchType, String keyword) {
        return (root, query, criteriaBuilder) -> {
            Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
            Join<Post, Book> bookJoin = root.join("book", JoinType.LEFT);

            Predicate isNotDeleted = criteriaBuilder.isFalse(root.get("isDeleted"));

            if (keyword == null || keyword.isBlank()) {
                return isNotDeleted;
            }

            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            Predicate searchPredicate;

            switch (searchType) {
                case TITLE:
                    searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKeyword);
                    break;

                case TITLE_CONTENT:
                    Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKeyword);
                    Predicate contentLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), searchKeyword);
                    searchPredicate = criteriaBuilder.or(titleLike, contentLike);
                    break;

                case USERNAME:
                    searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("nickname")), searchKeyword);
                    break;

                case BOOK_TITLE:
                    searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(bookJoin.get("bookname")), searchKeyword);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid search type: " + searchType);
            }

            return criteriaBuilder.and(isNotDeleted, searchPredicate);
        };
    }
}
