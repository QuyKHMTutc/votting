package com.xxxx.votting_spring.service;

import com.xxxx.votting_spring.dto.request.VoteRequest;
import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.entity.PollOption;
import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.entity.Vote;
import com.xxxx.votting_spring.repository.PollOptionRepository;
import com.xxxx.votting_spring.repository.PollRepository;
import com.xxxx.votting_spring.repository.UserRepository;
import com.xxxx.votting_spring.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xxxx.votting_spring.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import com.xxxx.votting_spring.dto.response.PollResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PollService pollService;

    @Transactional
    public Vote submitVote(Long pollId, VoteRequest request, String username, String ipAddress) {
        // Get user if username is provided
        User user = null;
        if (username != null) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        // Get poll
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        // Check if poll is active
        if (!poll.getIsActive()) {
            throw new CustomException("This poll is closed", HttpStatus.BAD_REQUEST);
        }

        // Check if poll has ended
        if (poll.getEndDate() != null && poll.getEndDate().isBefore(LocalDateTime.now())) {
            throw new CustomException("This poll has ended", HttpStatus.BAD_REQUEST);
        }

        // Prevent creator from voting in their own poll
        if (user != null && poll.getCreator().getId().equals(user.getId())) {
            throw new CustomException("You cannot vote in your own poll", HttpStatus.FORBIDDEN);
        }

        // Check voting permissions and duplicates
        if (poll.getMode() == Poll.PollMode.PRIVATE) {
            // Private Poll: Must be logged in
            if (user == null) {
                throw new CustomException("You must be logged in to vote in this private poll",
                        HttpStatus.UNAUTHORIZED);
            }
            // Check if user has already voted
            if (voteRepository.existsByPollAndUser(poll, user)) {
                throw new CustomException("You have already voted in this poll", HttpStatus.CONFLICT);
            }
        } else {
            // Public Poll: Check IP address
            // Optional: If user is logged in, we can ALSO check user to be strict, or just
            // rely on IP.
            // Let's rely on IP for now as per requirement "One vote per IP".
            // But if user IS logged in, we should link the vote to them.

            if (voteRepository.existsByPollAndIpAddress(poll, ipAddress)) {
                throw new CustomException("You have already voted in this poll (checked by IP)", HttpStatus.CONFLICT);
            }
        }

        // Get the selected option
        PollOption option = pollOptionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new CustomException("Option not found", HttpStatus.NOT_FOUND));

        // Verify option belongs to this poll
        if (!option.getPoll().getId().equals(pollId)) {
            throw new CustomException("Invalid option for this poll", HttpStatus.BAD_REQUEST);
        }

        // Create vote
        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setUser(user); // Can be null for anonymous
        vote.setIpAddress(ipAddress);
        vote.setOption(option);

        // Increment vote count on option
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);

        Vote savedVote = voteRepository.save(vote);

        // Broadcast update via WebSocket
        try {
            PollResponse updatedPoll = pollService.getPollById(pollId);
            messagingTemplate.convertAndSend("/topic/poll/" + pollId, updatedPoll);
        } catch (Exception e) {
            System.err.println("Failed to broadcast vote update: " + e.getMessage());
        }

        return savedVote;
    }

    public boolean hasUserVoted(Long pollId, String username, String ipAddress) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        if (poll.getMode() == Poll.PollMode.PRIVATE) {
            if (username == null)
                return false;
            User user = userRepository.findByEmail(username).orElse(null);
            if (user == null)
                return false;
            return voteRepository.existsByPollAndUser(poll, user);
        } else {
            // Public poll
            return voteRepository.existsByPollAndIpAddress(poll, ipAddress);
        }
    }

    public Optional<Vote> getUserVote(Long pollId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        return voteRepository.findByPollAndUser(poll, user);
    }
}
