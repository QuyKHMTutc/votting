import React, { useState, useEffect } from 'react';
import { adminAPI, pollAPI } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/AdminDashboard.css';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('polls');
    const [users, setUsers] = useState([]);
    const [stats, setStats] = useState({ totalUsers: 0, totalPolls: 0, totalVotes: 0 });
    const [polls, setPolls] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const navigate = useNavigate();

    const { user } = useAuth();

    useEffect(() => {
        if (user && user.role !== 'ADMIN') {
            navigate('/dashboard'); // Kick non-admins to user dashboard
        } else {
            fetchDashboardData();
        }
    }, [user, navigate]);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            const statsResponse = await adminAPI.getStats();
            console.log("Stats Response:", statsResponse);
            setStats(statsResponse.data || { totalUsers: 0, totalPolls: 0, totalVotes: 0 });

            const pollsResponse = await pollAPI.getAllPolls();
            console.log("Polls Response:", pollsResponse);
            setPolls(Array.isArray(pollsResponse.data) ? pollsResponse.data : []);

            const usersResponse = await adminAPI.getUsers();
            console.log("Users Response:", usersResponse);
            setUsers(Array.isArray(usersResponse.data) ? usersResponse.data : []);

            setLoading(false);
        } catch (err) {
            setError('Failed to load dashboard data');
            setLoading(false);
            console.error(err);
        }
    };

    const handleToggleUserStatus = async (userId) => {
        try {
            await adminAPI.toggleUserStatus(userId);
            // specific update to avoid full reload
            setUsers(users.map(u => {
                if (u.id === userId) {
                    return { ...u, enabled: !u.enabled };
                }
                return u;
            }));
            // Update stats as well just in case, though maybe not needed for immediate feedback
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to update user status';
            alert(`Error: ${errorMessage}`);
        }
    };

    const handleDeletePoll = async (id) => {
        if (!window.confirm('Are you sure you want to delete this poll? This action cannot be undone.')) {
            return;
        }

        try {
            await adminAPI.deletePoll(id);
            // Refresh list
            const pollsResponse = await pollAPI.getAllPolls();
            setPolls(pollsResponse.data);
            // Refresh stats
            const statsResponse = await adminAPI.getStats();
            setStats(statsResponse.data);
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to delete poll';
            alert(`Error: ${errorMessage}`);
            console.error(err);
        }
    };

    if (loading) return (
        <div style={{ padding: '2rem', textAlign: 'center', color: '#666' }}>
            <h2>Loading Admin Dashboard...</h2>
            <p>Please wait while we fetch system data.</p>
        </div>
    );

    if (error) return (
        <div style={{ padding: '2rem', color: 'red', textAlign: 'center' }}>
            <h2>Error Loading Dashboard</h2>
            <p>{error}</p>
            <button onClick={fetchDashboardData}>Retry</button>
        </div>
    );

    return (
        <div className="admin-dashboard" style={{ paddingTop: '100px' }}> {/* Ensure not hidden by fixed navbar */}
            <header className="dashboard-header">
                <h1 style={{ color: 'red' }}>Admin Dashboard (Debug Mode)</h1>
            </header>

            {error && <div className="error-message">{error}</div>}

            <div className="stats-grid">
                <div className="stat-card">
                    <h3>Total Users</h3>
                    <div className="stat-value">{stats.totalUsers}</div>
                </div>
                <div className="stat-card">
                    <h3>Total Polls</h3>
                    <div className="stat-value">{stats.totalPolls}</div>
                </div>
                <div className="stat-card">
                    <h3>Total Votes</h3>
                    <div className="stat-value">{stats.totalVotes}</div>
                </div>
            </div>

            <div className="dashboard-tabs">
                <button
                    className={`tab-btn ${activeTab === 'polls' ? 'active' : ''}`}
                    onClick={() => setActiveTab('polls')}
                >
                    Manage Polls
                </button>
                <button
                    className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
                    onClick={() => setActiveTab('users')}
                >
                    Manage Users
                </button>
            </div>

            {activeTab === 'polls' ? (
                <section className="polls-management">
                    <h2>Recent Polls</h2>
                    <div className="polls-table-container">
                        <table className="polls-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Title</th>
                                    <th>Creator</th>
                                    <th>Status</th>
                                    <th>Votes</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {polls.map(poll => (
                                    <tr key={poll.id}>
                                        <td>{poll.id}</td>
                                        <td>{poll.title}</td>
                                        <td>{poll.creator.fullName || poll.creator.username}</td>
                                        <td>
                                            <span className={`status-badge ${poll.isActive ? 'active' : 'closed'}`}>
                                                {poll.isActive ? 'Active' : 'Closed'}
                                            </span>
                                        </td>
                                        <td>{poll.totalVotes}</td>
                                        <td>
                                            <button
                                                className="btn-delete"
                                                onClick={() => handleDeletePoll(poll.id)}
                                            >
                                                Delete
                                            </button>
                                            <button
                                                className="btn-view"
                                                onClick={() => navigate(`/poll/${poll.id}`)}
                                            >
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            ) : (
                <section className="users-management">
                    <h2>Manage Users</h2>
                    <div className="polls-table-container">
                        <table className="polls-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Full Name</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map(u => (
                                    <tr key={u.id}>
                                        <td>{u.id}</td>
                                        <td>{u.fullName}</td>
                                        <td>{u.email}</td>
                                        <td>
                                            <span className={`role-badge ${u.role === 'ADMIN' ? 'admin' : 'user'}`}>
                                                {u.role}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`status-badge ${u.enabled ? 'active' : 'closed'}`}>
                                                {u.enabled ? 'Active' : 'Locked'}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                className={`btn-action ${u.enabled ? 'btn-lock' : 'btn-unlock'}`}
                                                onClick={() => handleToggleUserStatus(u.id)}
                                                disabled={u.email === user.email} // Cannot lock self
                                                style={{
                                                    backgroundColor: u.enabled ? '#ef4444' : '#10b981',
                                                    color: 'white',
                                                    border: 'none',
                                                    padding: '5px 10px',
                                                    borderRadius: '4px',
                                                    cursor: u.email === user.email ? 'not-allowed' : 'pointer',
                                                    opacity: u.email === user.email ? 0.5 : 1
                                                }}
                                            >
                                                {u.enabled ? 'Lock' : 'Unlock'}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}
        </div>
    );
};

export default AdminDashboard;
