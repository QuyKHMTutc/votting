import React, { useState, useEffect } from 'react';
import { pollAPI } from '../services/api';
import PollCard from '../components/PollCard';
import '../styles/Home.css';

const Home = () => {
    const [polls, setPolls] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('all'); // all or active
    const [error, setError] = useState('');

    useEffect(() => {
        fetchPolls();
    }, [filter]);

    const fetchPolls = async () => {
        try {
            setLoading(true);
            const response = filter === 'active'
                ? await pollAPI.getActivePolls()
                : await pollAPI.getAllPolls();

            setPolls(response.data);
            setError('');
        } catch (err) {
            setError('Failed to load polls');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="home-container">
            <div className="home-header">
                <h1>Available Polls</h1>
                <div className="filter-buttons">
                    <button
                        className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
                        onClick={() => setFilter('all')}
                    >
                        All Polls
                    </button>
                    <button
                        className={`filter-btn ${filter === 'active' ? 'active' : ''}`}
                        onClick={() => setFilter('active')}
                    >
                        Active Only
                    </button>
                </div>
            </div>

            {loading ? (
                <div className="loading">Loading polls...</div>
            ) : error ? (
                <div className="error-message">{error}</div>
            ) : polls.length === 0 ? (
                <div className="no-polls">
                    <p>No polls found. Create one to get started!</p>
                </div>
            ) : (
                <div className="polls-grid">
                    {polls.map(poll => (
                        <PollCard key={poll.id} poll={poll} />
                    ))}
                </div>
            )}
        </div>
    );
};

export default Home;
