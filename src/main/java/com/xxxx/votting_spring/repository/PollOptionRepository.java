package com.xxxx.votting_spring.repository;

import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    List<PollOption> findByPoll(Poll poll);
}
