import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/PollCard.css';

const PollCard = ({ poll }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/poll/${poll.id}`);
    };

    return (
        <div className="poll-card" onClick={handleClick}>
            <div className="poll-card-header">
                <h3 className="poll-title">{poll.title}</h3>
                <div className="poll-status">
                    {poll.mode === 'PRIVATE' ? (
                        <span className="status-private">Private</span>
                    ) : (
                        <span className="status-public">Public</span>
                    )}
                    {poll.isActive ? (
                        <span className="status-active">Active</span>
                    ) : (
                        <span className="status-closed">Closed</span>
                    )}
                </div>
            </div>

            {poll.description && (
                <p className="poll-description">{poll.description}</p>
            )}

            <div className="poll-meta">
                <div className="poll-creator">
                    <span className="meta-icon">👤</span>
                    {poll.creator.fullName}
                </div>
                <div className="poll-votes">
                    <span className="meta-icon">📊</span>
                    {poll.totalVotes} votes
                </div>
            </div>

            {poll.endDate && (
                <div className="poll-end-date">
                    <span className="meta-icon">📅</span>
                    Ends: {new Date(poll.endDate).toLocaleDateString()}
                </div>
            )}
        </div>
    );
};

export default PollCard;
