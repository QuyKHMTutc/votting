import React, { useState, useEffect } from 'react';
import { pollAPI } from '../services/api';
import PollCard from '../components/PollCard';
import '../styles/Dashboard.css';

const Dashboard = () => {
    const [activeTab, setActiveTab] = useState('created');
    const [createdPolls, setCreatedPolls] = useState([]);
    const [votedPolls, setVotedPolls] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchPolls();
    }, [activeTab]);

    const fetchPolls = async () => {
        setLoading(true);
        setError('');
        try {
            if (activeTab === 'created') {
                if (createdPolls.length === 0) {
                    const response = await pollAPI.getCreatedPolls();
                    setCreatedPolls(response.data);
                }
            } else {
                if (votedPolls.length === 0) {
                    const response = await pollAPI.getVotedPolls();
                    setVotedPolls(response.data);
                }
            }
        } catch (err) {
            setError('Failed to load polls');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const displayPolls = activeTab === 'created' ? createdPolls : votedPolls;

    return (
        <div className="dashboard-container">
            <h1>My Dashboard</h1>

            <div className="dashboard-tabs">
                <button
                    className={`tab-btn ${activeTab === 'created' ? 'active' : ''}`}
                    onClick={() => setActiveTab('created')}
                >
                    Created Polls
                </button>
                <button
                    className={`tab-btn ${activeTab === 'voted' ? 'active' : ''}`}
                    onClick={() => setActiveTab('voted')}
                >
                    Voted Polls
                </button>
            </div>

            {loading ? (
                <div className="loading">Loading...</div>
            ) : error ? (
                <div className="error-message">{error}</div>
            ) : (
                <div className="polls-grid">
                    {displayPolls.length > 0 ? (
                        displayPolls.map(poll => (
                            <PollCard key={poll.id} poll={poll} />
                        ))
                    ) : (
                        <p className="no-polls">
                            {activeTab === 'created'
                                ? "You haven't created any polls yet."
                                : "You haven't voted in any polls yet."}
                        </p>
                    )}
                </div>
            )}
        </div>
    );
};

export default Dashboard;
