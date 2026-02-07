package com.xxxx.votting_spring.repository;

import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    List<Poll> findByIsActiveOrderByCreatedAtDesc(Boolean isActive);

    List<Poll> findByModeAndIsActiveOrderByCreatedAtDesc(Poll.PollMode mode, boolean isActive);

    List<Poll> findByModeOrderByCreatedAtDesc(Poll.PollMode mode);

    List<Poll> findByIsActiveTrueAndEndDateBefore(java.time.LocalDateTime now);

    List<Poll> findByCreatorOrderByCreatedAtDesc(User creator);

    List<Poll> findAllByOrderByCreatedAtDesc();
}
