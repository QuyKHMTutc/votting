import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Auth.css'; // Reusing Auth styles for consistency

const ChangePassword = () => {
    const { user, forgotPassword, resetPassword, logout } = useAuth();
    const navigate = useNavigate();

    const [step, setStep] = useState(1); // 1: Request OTP, 2: Perform Reset
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [timer, setTimer] = useState(0);

    // Timer logic
    useEffect(() => {
        let interval;
        if (step === 2 && timer > 0) {
            interval = setInterval(() => {
                setTimer((prev) => prev - 1);
            }, 1000);
        } else if (timer === 0) {
            clearInterval(interval);
        }
        return () => clearInterval(interval);
    }, [step, timer]);

    const formatTime = (seconds) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
    };

    const handleSendCode = async () => {
        setMessage('');
        setError('');
        setLoading(true);

        if (!user || !user.email) {
            setError("User email not found. Please log in again.");
            setLoading(false);
            return;
        }

        const result = await forgotPassword(user.email);

        if (result.success) {
            setMessage(`Verification code sent to ${user.email}`);
            setStep(2);
            setTimer(300); // 5 minutes
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to send verification code');
        }

        setLoading(false);
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        setLoading(true);

        const data = {
            email: user.email,
            verificationCode: otp,
            newPassword,
            confirmPassword
        };

        const result = await resetPassword(data);

        if (result.success) {
            setMessage("Password changed successfully. You will be logged out in 3 seconds...");
            setTimeout(() => {
                logout();
                navigate('/login');
            }, 3000);
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to change password');
            setLoading(false);
        }
    };

    return (
        <div className="auth-container" style={{ paddingTop: '80px' }}>
            <div className="auth-card">
                <h2 className="auth-title">Change Password</h2>

                {error && <div className="error-message">{error}</div>}
                {message && <div className="success-message" style={{ color: 'green', marginBottom: '1rem', textAlign: 'center' }}>{message}</div>}

                {step === 1 ? (
                    <div style={{ textAlign: 'center' }}>
                        <p className="auth-subtitle">
                            To secure your account, we need to verify your email address before changing your password.
                        </p>
                        <p style={{ marginBottom: '1.5rem', fontWeight: '500' }}>
                            Current Email: {user?.email}
                        </p>
                        <button
                            onClick={handleSendCode}
                            className="btn-submit"
                            disabled={loading}
                        >
                            {loading ? 'Sending...' : 'Send Verification Code'}
                        </button>
                    </div>
                ) : (
                    <form onSubmit={handleChangePassword} className="auth-form">
                        <p className="auth-subtitle">
                            Enter the code sent to {user?.email} and your new password.
                        </p>

                        <div className="form-group">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <label htmlFor="otp">Verification Code</label>
                                <span style={{ color: timer < 60 ? 'red' : 'inherit', fontSize: '0.9rem' }}>
                                    Expires in: {formatTime(timer)}
                                </span>
                            </div>
                            <input
                                type="text"
                                id="otp"
                                value={otp}
                                onChange={(e) => setOtp(e.target.value)}
                                required
                                placeholder="Enter 6-digit code"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="newPassword">New Password</label>
                            <input
                                type="password"
                                id="newPassword"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                required
                                minLength="6"
                                placeholder="Enter new password"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword">Confirm Password</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                minLength="6"
                                placeholder="Confirm new password"
                            />
                        </div>

                        <button type="submit" className="btn-submit" disabled={loading}>
                            {loading ? 'Processing...' : 'Change Password'}
                        </button>

                        <button
                            type="button"
                            className="btn-link"
                            onClick={() => setStep(1)}
                            style={{ display: 'block', margin: '1rem auto', background: 'none', border: 'none', color: '#666', cursor: 'pointer' }}
                        >
                            Back
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
};

export default ChangePassword;
