import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Navbar.css';

const Navbar = () => {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();
    const [dropdownOpen, setDropdownOpen] = useState(false);

    // Close dropdown when clicking outside
    const dropdownRef = useRef(null);
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <div className="navbar-container">
                <Link to={user?.role === 'ADMIN' ? '/admin' : '/'} className="navbar-brand">
                    <span className="brand-icon">📊</span>
                    Voting System
                </Link>

                <div className="navbar-menu">
                    {isAuthenticated ? (
                        <>
                            {user?.role === 'ADMIN' ? (
                                <Link to="/admin" className="nav-link">Admin Dashboard</Link>
                            ) : (
                                <>
                                    <Link to="/" className="nav-link">Home</Link>
                                    <Link to="/create-poll" className="nav-link">Create Poll</Link>
                                    <Link to="/dashboard" className="nav-link">My Dashboard</Link>
                                </>
                            )}
                            <div className="user-dropdown-container" ref={dropdownRef}>
                                <div
                                    className="user-profile-trigger"
                                    onClick={() => setDropdownOpen(!dropdownOpen)}
                                >
                                    <div className="user-avatar">
                                        {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                                    </div>
                                    <span className="user-name-display">
                                        {user?.fullName || user?.username}
                                    </span>
                                    <span className={`dropdown-arrow ${dropdownOpen ? 'open' : ''}`}>▼</span>
                                </div>

                                {dropdownOpen && (
                                    <div className="dropdown-menu">
                                        <div className="dropdown-header">
                                            <span className="dropdown-user-email">{user?.email}</span>
                                        </div>
                                        <div className="dropdown-divider"></div>
                                        <Link
                                            to="/change-password"
                                            className="dropdown-item"
                                            onClick={() => setDropdownOpen(false)}
                                        >
                                            <span className="icon">🔒</span> Change Password
                                        </Link>
                                        <div
                                            className="dropdown-item logout"
                                            onClick={handleLogout}
                                        >
                                            <span className="icon">🚪</span> Logout
                                        </div>
                                    </div>
                                )}
                            </div>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="nav-link">Login</Link>
                            <Link to="/register" className="nav-link btn-register">Register</Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
