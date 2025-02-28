package org.ffb_be.repository;

import org.ffb_be.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteByBlogId(Long id);

    @Query("SELECT c FROM Comment c WHERE c.blog.id = :blogId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByBlogId(@Param("blogId") Long blogId);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC LIMIT 2")
    List<Comment> findTopRepliesByParentId(@Param("parentId") Long parentId);

    List<Comment> findByBlogId(Long blogId);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId ORDER BY c.id ASC")
    List<Comment> findByParentCommentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.blog.id = :id")
    int countByBlogId(Long id);
}