import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Auth.css';

const ForgotPassword = () => {
    const [step, setStep] = useState(1); // 1: Email, 2: OTP & Reset
    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [timer, setTimer] = useState(300); // 5 minutes in seconds

    const { forgotPassword, resetPassword } = useAuth();
    const navigate = useNavigate();

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

    const handleSendCode = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');
        setLoading(true);

        const result = await forgotPassword(email);

        if (result.success) {
            setMessage("Password reset code has been sent to your email.");
            setStep(2);
            setTimer(300); // Reset timer to 5 minutes
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to send reset code');
        }

        setLoading(false);
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        if (timer === 0) {
            setError("OTP has expired. Please request a new code.");
            return;
        }

        setLoading(true);

        const data = {
            email,
            verificationCode: otp,
            newPassword,
            confirmPassword
        };

        const result = await resetPassword(data);

        if (result.success) {
            setMessage("Password reset successfully. Redirecting to login...");
            setTimeout(() => {
                navigate('/login');
            }, 2000);
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to reset password');
        }

        setLoading(false);
    };

    const handleResend = async () => {
        // Re-trigger send code
        const result = await forgotPassword(email);
        if (result.success) {
            setMessage("A new code has been sent.");
            setTimer(300);
            setError('');
        } else {
            setError("Failed to resend code.");
        }
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">Forgot Password</h2>
                <p className="auth-subtitle">
                    {step === 1 ? 'Enter your email to receive a reset code' : 'Enter the code sent to your email'}
                </p>

                {error && <div className="error-message">{error}</div>}
                {message && <div className="success-message" style={{ color: 'green', marginBottom: '1rem', textAlign: 'center' }}>{message}</div>}

                {step === 1 ? (
                    <form onSubmit={handleSendCode} className="auth-form">
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                placeholder="Enter your email"
                            />
                        </div>

                        <button type="submit" className="btn-submit" disabled={loading}>
                            {loading ? 'Sending...' : 'Send Reset Code'}
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleResetPassword} className="auth-form">
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

                        <button type="submit" className="btn-submit" disabled={loading || timer === 0}>
                            {loading ? 'Resetting...' : 'Reset Password'}
                        </button>

                        {timer === 0 && (
                            <button type="button" onClick={handleResend} className="btn-secondary" style={{ marginTop: '10px', width: '100%' }}>
                                Resend Code
                            </button>
                        )}
                    </form>
                )}

                <p className="auth-link">
                    Remember your password? <Link to="/login">Login here</Link>
                </p>
            </div>
        </div>
    );
};

export default ForgotPassword;
