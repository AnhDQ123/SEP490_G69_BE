package org.ffb_be.repository;

import org.ffb_be.dto.auth.ProfileDto.ProfileDTO;
import org.ffb_be.entity.Profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("SELECT new org.ffb_be.dto.auth.ProfileDto.ProfileDTO(p.id, p.name, p.address, p.avatar) " +
            "FROM Profile p WHERE p.user.id = :userId")
    Optional<ProfileDTO> findByUserId( Long userId);
        @Query("FROM Profile p WHERE p.user.id = :id")
    Optional<Profile> findByUserId2(Long id);


}
