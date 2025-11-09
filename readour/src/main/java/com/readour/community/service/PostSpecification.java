package com.readour.community.service;

import com.readour.community.entity.Book;
import com.readour.common.entity.User;
import com.readour.community.entity.Post;
import com.readour.community.enums.PostCategory;
import com.readour.community.enums.PostSearchType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostSpecification {
    public Specification<Post> search(PostSearchType searchType, String keyword, PostCategory category) {
        return (root, query, criteriaBuilder) -> {
            Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
            Join<Post, Book> bookJoin = root.join("book", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted"))); // 기본 조건

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (keyword != null && !keyword.isBlank()) {
                String searchKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate searchPredicate;

                switch (searchType) {
                    case TITLE:
                        searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKeyword);
                        break;
                    case TITLE_CONTENT:
                        Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKeyword);
                        Predicate contentLike = criteriaBuilder.like(root.get("content"), searchKeyword);
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
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}