package org.ffb_be.repository;

import org.ffb_be.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByBlogId(Long blogId);

    Comment findTopByBlogIdOrderByCreatedAtDesc(Long id);

    void deleteByBlogId(Long id);
}