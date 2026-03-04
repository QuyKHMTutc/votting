package com.xxxx.votting_spring.service;

import com.xxxx.votting_spring.dto.request.PollRequest;
import com.xxxx.votting_spring.dto.response.PollResponse;
import com.xxxx.votting_spring.dto.response.PollResponse.CreatorInfo;
import com.xxxx.votting_spring.dto.response.PollResponse.OptionInfo;
import com.xxxx.votting_spring.entity.Poll;
import com.xxxx.votting_spring.entity.PollOption;
import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.repository.PollOptionRepository;
import com.xxxx.votting_spring.repository.PollRepository;
import com.xxxx.votting_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xxxx.votting_spring.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private com.xxxx.votting_spring.repository.VoteRepository voteRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContentModerationService contentModerationService;

    @Transactional
    @CacheEvict(value = { "poll", "activePolls" }, allEntries = true)
    public PollResponse createPoll(PollRequest request, String username) {
        // Validate content
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(request.getTitle()).append(" ");
        if (request.getDescription() != null) {
            contentBuilder.append(request.getDescription()).append(" ");
        }
        if (request.getOptions() != null) {
            request.getOptions().forEach(opt -> contentBuilder.append(opt).append(" "));
        }
        contentModerationService.validateContent(contentBuilder.toString());

        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("User not found with email: " + username, HttpStatus.NOT_FOUND));

        Poll poll = new Poll();
        poll.setTitle(request.getTitle());
        poll.setDescription(request.getDescription());
        poll.setCreator(creator);
        poll.setEndDate(request.getEndDate());
        poll.setIsActive(true);

        if (request.getMode() != null) {
            try {
                poll.setMode(Poll.PollMode.valueOf(request.getMode().toUpperCase()));
            } catch (IllegalArgumentException e) {
                poll.setMode(Poll.PollMode.PUBLIC); // Default
            }
        } else {
            poll.setMode(Poll.PollMode.PUBLIC);
        }

        // Validate Private Poll requirements
        if (poll.getMode() == Poll.PollMode.PRIVATE) {
            if (request.getInvitedEmails() == null || request.getInvitedEmails().isEmpty()) {
                throw new CustomException("Private polls require at least one invited email.", HttpStatus.BAD_REQUEST);
            }
        }

        Poll savedPoll = pollRepository.save(poll);

        // Create poll options
        List<PollOption> options = new ArrayList<>();
        for (String optionText : request.getOptions()) {
            PollOption option = new PollOption();
            option.setPoll(savedPoll);
            option.setOptionText(optionText);
            option.setVoteCount(0);
            options.add(option);
        }

        pollOptionRepository.saveAll(options);
        savedPoll.setOptions(options);

        // Send invitations if provided and mode is PRIVATE (optional, could be for
        // public too)
        if (request.getInvitedEmails() != null && !request.getInvitedEmails().isEmpty()) {
            String pollLink = "http://localhost:5173/poll/" + savedPoll.getId(); // Frontend URL
            for (String email : request.getInvitedEmails()) {
                // Basic validation
                if (email != null && email.contains("@")) {
                    emailService.sendPollInvitation(email.trim(), savedPoll.getTitle(), pollLink);
                }
            }
        }

        return convertToPollResponse(savedPoll);
    }

    public List<PollResponse> getAllPolls() {
        // Only return PUBLIC polls for the general list
        return pollRepository.findByModeOrderByCreatedAtDesc(Poll.PollMode.PUBLIC)
                .stream()
                .map(this::convertToPollResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "activePolls")
    public List<PollResponse> getActivePolls() {
        // Return only PUBLIC active polls for the homepage/list
        return pollRepository.findByModeAndIsActiveOrderByCreatedAtDesc(Poll.PollMode.PUBLIC, true)
                .stream()
                .map(this::convertToPollResponse)
                .collect(Collectors.toList());
    }

    public List<PollResponse> getPollsCreatedByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        return pollRepository.findByCreatorOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToPollResponse)
                .collect(Collectors.toList());
    }

    public List<PollResponse> getPollsVotedByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        return voteRepository.findByUserOrderByVotedAtDesc(user)
                .stream()
                .map(vote -> convertToPollResponse(vote.getPoll()))
                // Deduplicate if user voted multiple times (though shouldn't happen with
                // constraints)
                .distinct()
                .collect(Collectors.toList());
    }

    @Cacheable(value = "poll", key = "#pollId")
    public PollResponse getPollById(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));
        return convertToPollResponse(poll);
    }

    @Transactional
    @CacheEvict(value = { "poll", "activePolls" }, allEntries = true)
    public PollResponse updatePoll(Long pollId, PollRequest request, String username) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        // Check if user is the creator
        // Check if user is the creator
        if (!poll.getCreator().getEmail().equals(username)) {
            throw new CustomException("You are not authorized to update this poll", HttpStatus.FORBIDDEN);
        }

        poll.setTitle(request.getTitle());
        poll.setDescription(request.getDescription());
        poll.setEndDate(request.getEndDate());

        Poll updatedPoll = pollRepository.save(poll);
        return convertToPollResponse(updatedPoll);
    }

    @Transactional
    @CacheEvict(value = { "poll", "activePolls" }, allEntries = true)
    public void deletePoll(Long pollId, String username) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        // Check if user is the creator
        // Check if user is the creator
        if (!poll.getCreator().getEmail().equals(username)) {
            throw new CustomException("You are not authorized to delete this poll", HttpStatus.FORBIDDEN);
        }

        pollRepository.delete(poll);
    }

    @Transactional
    @CacheEvict(value = { "poll", "activePolls" }, allEntries = true)
    public PollResponse closePoll(Long pollId, String username) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new CustomException("Poll not found", HttpStatus.NOT_FOUND));

        // Check if user is the creator
        // Check if user is the creator
        if (!poll.getCreator().getEmail().equals(username)) {
            throw new CustomException("You are not authorized to close this poll", HttpStatus.FORBIDDEN);
        }

        poll.setIsActive(false);
        Poll updatedPoll = pollRepository.save(poll);
        return convertToPollResponse(updatedPoll);
    }

    private PollResponse convertToPollResponse(Poll poll) {
        PollResponse response = new PollResponse();
        response.setId(poll.getId());
        response.setTitle(poll.getTitle());
        response.setDescription(poll.getDescription());
        response.setCreatedAt(poll.getCreatedAt());
        response.setEndDate(poll.getEndDate());
        response.setIsActive(poll.getIsActive());
        response.setMode(poll.getMode().name());

        CreatorInfo creatorInfo = new CreatorInfo(
                poll.getCreator().getId(),
                poll.getCreator().getUsername(),
                poll.getCreator().getFullName());
        response.setCreator(creatorInfo);

        // Calculate total votes
        int totalVotes = poll.getOptions().stream()
                .mapToInt(PollOption::getVoteCount)
                .sum();
        response.setTotalVotes(totalVotes);

        // Convert options with percentage
        List<OptionInfo> optionInfos = poll.getOptions().stream()
                .map(option -> {
                    double percentage = totalVotes > 0
                            ? (option.getVoteCount() * 100.0) / totalVotes
                            : 0.0;
                    return new OptionInfo(
                            option.getId(),
                            option.getOptionText(),
                            option.getVoteCount(),
                            percentage);
                })
                .collect(Collectors.toList());
        response.setOptions(optionInfos);

        return response;
    }
}
