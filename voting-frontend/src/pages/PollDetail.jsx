import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { pollAPI, voteAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Client } from '@stomp/stompjs';
import '../styles/PollDetail.css';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D'];

const PollDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAuth();
    const [poll, setPoll] = useState(null);
    const [hasVoted, setHasVoted] = useState(false);
    const [selectedOption, setSelectedOption] = useState(null);
    const [loading, setLoading] = useState(true);
    const [voting, setVoting] = useState(false);
    const [error, setError] = useState('');
    const [shareMessage, setShareMessage] = useState('');

    useEffect(() => {
        fetchPollDetails();
        checkVoteStatus();
    }, [id, isAuthenticated]);

    // WebSocket Integration
    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            onConnect: () => {
                console.log('Connected to WebSocket');
                client.subscribe(`/topic/poll/${id}`, (message) => {
                    if (message.body) {
                        try {
                            const updatedPoll = JSON.parse(message.body);
                            console.log("Received update:", updatedPoll);
                            setPoll(updatedPoll);
                        } catch (e) {
                            console.error("Failed to parse update", e);
                        }
                    }
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [id]);

    const fetchPollDetails = async () => {
        try {
            const response = await pollAPI.getPollById(id);
            console.log("Poll Details:", response.data); // Debugging
            setPoll(response.data);
            setLoading(false);
        } catch (err) {
            setError('Failed to load poll details');
            setLoading(false);
            console.error(err);
        }
    };

    const checkVoteStatus = async () => {
        try {
            const response = await voteAPI.getMyVote(id);
            setHasVoted(response.data.hasVoted);
            if (response.data.hasVoted) {
                setSelectedOption(response.data.optionId);
            }
        } catch (err) {
            console.error('Error checking vote status:', err);
        }
    };

    const handleVote = async () => {
        if (!selectedOption) {
            setError('Please select an option');
            return;
        }

        setVoting(true);
        setError('');

        try {
            await voteAPI.submitVote(id, { optionId: selectedOption });
            setHasVoted(true);
            // Refresh poll data to update vote counts
            await fetchPollDetails();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to submit vote');
            console.error(err);
        } finally {
            setVoting(false);
        }
    };

    const handleClosePoll = async () => {
        try {
            await pollAPI.closePoll(id);
            await fetchPollDetails();
        } catch (err) {
            setError(err.response?.data || 'Failed to close poll');
        }
    };

    const handleShare = () => {
        navigator.clipboard.writeText(window.location.href).then(() => {
            setShareMessage('Link copied to clipboard!');
            setTimeout(() => setShareMessage(''), 3000);
        }).catch(err => {
            console.error('Failed to copy link: ', err);
            setError('Failed to copy link');
        });
    };

    if (loading) {
        return <div className="loading">Loading poll...</div>;
    }

    if (!poll) {
        return <div className="error-message">Poll not found</div>;
    }

    const chartData = poll.options.map((option, index) => ({
        name: option.optionText,
        value: option.voteCount,
        percentage: option.percentage
    }));

    const isCreator = user && poll.creator.id === user.id;

    // Debug voting conditions
    console.log("Voting Debug:", {
        isActive: poll.isActive,
        hasVoted,
        isAuthenticated,
        mode: poll.mode,
        isCreator,
        condition1: poll.isActive && !hasVoted && (isAuthenticated || poll.mode === 'PUBLIC'),
        condition3: !isAuthenticated && poll.mode === 'PRIVATE'
    });

    return (
        <div className="poll-detail-container">
            <div className="poll-detail-card">
                <div className="poll-header">
                    <div>
                        <h1>{poll.title}</h1>
                        {poll.description && <p className="poll-description">{poll.description}</p>}
                    </div>
                    <div className="poll-status">
                        {poll.mode === 'PRIVATE' && <span className="status-badge private">Private</span>}
                        {poll.isActive ? (
                            <span className="status-badge active">Active</span>
                        ) : (
                            <span className="status-badge closed">Closed</span>
                        )}

                    </div>
                </div>

                <div className="share-section">
                    <button className="btn-share" onClick={handleShare}>
                        🔗 Share Poll
                    </button>
                    {shareMessage && <span className="share-message">{shareMessage}</span>}
                </div>

                <div className="poll-meta">
                    <div className="meta-item">
                        <strong>Created by:</strong> {poll.creator.fullName}
                    </div>
                    <div className="meta-item">
                        <strong>Total Votes:</strong> {poll.totalVotes}
                    </div>
                    {poll.endDate && (
                        <div className="meta-item">
                            <strong>Ends:</strong> {new Date(poll.endDate).toLocaleString()}
                        </div>
                    )}
                </div>

                {/* Creator Banner */}
                {isCreator && (
                    <div className="creator-banner">
                        <span role="img" aria-label="crown">👑</span> You are the creator of this poll.
                    </div>
                )}

                {error && <div className="error-message">{error}</div>}

                {/* Voting Section - Hide if user is creator */}
                {/* Voting Section */}
                {!isCreator ? (
                    (() => {
                        if (!poll.isActive) {
                            return <div className="closed-message">This poll is closed</div>;
                        }
                        if (hasVoted) {
                            return <div className="voted-message">✓ You have already voted in this poll</div>;
                        }
                        if (poll.mode === 'PRIVATE' && !isAuthenticated) {
                            return (
                                <div className="login-prompt">
                                    Please <button onClick={() => navigate('/login')}>login</button> to vote
                                </div>
                            );
                        }
                        // Default: Show voting options
                        return (
                            <div className="voting-section">
                                <h3>Cast Your Vote</h3>
                                <div className="options-list">
                                    {poll.options.map((option) => (
                                        <div
                                            key={option.id}
                                            className={`option-card ${selectedOption === option.id ? 'selected' : ''}`}
                                            onClick={() => setSelectedOption(option.id)}
                                        >
                                            <input
                                                type="radio"
                                                name="vote"
                                                value={option.id}
                                                checked={selectedOption === option.id}
                                                onChange={() => setSelectedOption(option.id)}
                                            />
                                            <label>{option.optionText}</label>
                                        </div>
                                    ))}
                                </div>
                                <button
                                    className="btn-vote"
                                    onClick={handleVote}
                                    disabled={!selectedOption || voting}
                                >
                                    {voting ? 'Submitting...' : 'Submit Vote'}
                                </button>
                            </div>
                        );
                    })()
                ) : null}

                {/* Results Section */}
                <div className="results-section">
                    <h3>Results</h3>

                    {poll.totalVotes > 0 ? (
                        <>
                            <ResponsiveContainer width="100%" height={300}>
                                <PieChart>
                                    <Pie
                                        data={chartData}
                                        cx="50%"
                                        cy="50%"
                                        labelLine={false}
                                        label={({ name, percentage }) => `${name}: ${percentage.toFixed(1)}%`}
                                        outerRadius={80}
                                        fill="#8884d8"
                                        dataKey="value"
                                    >
                                        {chartData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>

                            <div className="results-list">
                                {poll.options.map((option, index) => (
                                    <div key={option.id} className="result-item">
                                        <div className="result-info">
                                            <span className="result-name">{option.optionText}</span>
                                            <span className="result-count">{option.voteCount} votes ({option.percentage.toFixed(1)}%)</span>
                                        </div>
                                        <div className="result-bar">
                                            <div
                                                className="result-bar-fill"
                                                style={{
                                                    width: `${option.percentage}%`,
                                                    backgroundColor: COLORS[index % COLORS.length]
                                                }}
                                            />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </>
                    ) : (
                        <p className="no-votes">No votes yet. Be the first to vote!</p>
                    )}
                </div>

                {/* Creator Actions */}
                {isCreator && poll.isActive && (
                    <div className="creator-actions">
                        <button className="btn-close-poll" onClick={handleClosePoll}>
                            Close Poll
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PollDetail;
