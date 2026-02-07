package com.xxxx.votting_spring.scheduler;

import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.repository.PollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PollCleanupScheduler {

    @Autowired
    private PollRepository pollRepository;

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void closeExpiredPolls() {
        List<Poll> expiredPolls = pollRepository.findByIsActiveTrueAndEndDateBefore(LocalDateTime.now());

        if (!expiredPolls.isEmpty()) {
            for (Poll poll : expiredPolls) {
                poll.setIsActive(false);
            }
            pollRepository.saveAll(expiredPolls);
            System.out.println("Closed " + expiredPolls.size() + " expired polls.");
        }
    }
}
