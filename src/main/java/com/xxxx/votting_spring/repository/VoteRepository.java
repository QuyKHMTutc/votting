package com.xxxx.votting_spring.repository;

import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Boolean existsByPollAndUser(Poll poll, User user);

    Boolean existsByPollAndIpAddress(Poll poll, String ipAddress);

    List<Vote> findByUserOrderByVotedAtDesc(User user);

    List<Vote> findByPollOrderByVotedAtDesc(Poll poll);

    Optional<Vote> findByPollAndUser(Poll poll, User user);
}
