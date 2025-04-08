package org.ffb_be.repository;

import org.ffb_be.entity.Blog;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Page<Blog> getBlogsByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);
    Page<Blog> getBlogsByStatusIsNotOrderByCreatedAtDesc(Status status, Pageable pageable);
}
