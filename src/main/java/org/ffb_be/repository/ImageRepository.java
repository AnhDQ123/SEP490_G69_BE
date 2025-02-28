package org.ffb_be.repository;

import jakarta.transaction.Transactional;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Types;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i FROM Image i WHERE i.type.category = 'BLOG' AND i.relatedId = :relatedId")
    List<Image> findBlogImagesByRelatedId(@Param("relatedId") Long relatedId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Image i WHERE i.relatedId = :blogId AND i.type.category = 'BLOG'")
    void deleteByBlogId(@Param("blogId") Long blogId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Image i WHERE i.relatedId = :blogId AND i.type.category = 'BLOG' AND i.url NOT IN :imageUrls")
    void deleteByBlogIdAndUrlNotIn(Long blogId, List<String> imageUrls);

    @Query("SELECT i.url FROM Image i WHERE i.type.category = 'BLOG' AND i.relatedId = :blogId")
    int findImageUrlsByBlogId(Long blogId);

    @Transactional
    @Modifying
    @Query("INSERT INTO Image (url, type, relatedId) VALUES (:url, :type, :relatedId)")
    void saveBlogImages(Long id, List<String> imageUrls, Types type);
}
