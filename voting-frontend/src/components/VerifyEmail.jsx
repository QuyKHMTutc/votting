
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Auth.css';

const VerifyEmail = ({ email, onVerified }) => {
    const [code, setCode] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { verify, resendVerification } = useAuth();
    const navigate = useNavigate();

    const handleVerify = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setMessage('');

        const result = await verify(email, code);
        if (result.success) {
            setMessage("Account verified! Redirecting to login...");
            setTimeout(() => {
                if (onVerified) onVerified();
                else navigate('/login');
            }, 2000);
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Verification failed');
        }
        setLoading(false);
    };

    const handleResend = async () => {
        setLoading(true);
        setError('');
        setMessage('');
        const result = await resendVerification(email);
        if (result.success) {
            setMessage("Verification code resent. Check your email.");
        } else {
            setError("Failed to resend code.");
        }
        setLoading(false);
    };

    return (
        <div className="auth-card" style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📧</div>
            <h2 className="auth-title">Verify Your Email</h2>
            <p className="auth-subtitle" style={{ color: '#bbb' }}>
                Enter the code sent to <strong>{email}</strong>
            </p>

            {message && <div style={{ color: '#4caf50', marginBottom: '1rem' }}>{message}</div>}
            {error && <div className="error-message">{error}</div>}

            <form onSubmit={handleVerify}>
                <div className="form-group">
                    <input
                        type="text"
                        placeholder="Enter 6-digit code"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        maxLength="6"
                        required
                        style={{ textAlign: 'center', letterSpacing: '0.5rem', fontSize: '1.2rem' }}
                    />
                </div>
                <button type="submit" className="btn-submit" disabled={loading}>
                    {loading ? 'Verifying...' : 'Verify & Login'}
                </button>
            </form>

            <div style={{ marginTop: '1.5rem' }}>
                <button
                    onClick={handleResend}
                    style={{ background: 'none', border: 'none', color: '#646cff', cursor: 'pointer', textDecoration: 'underline' }}
                    disabled={loading}
                >
                    Resend Code
                </button>
            </div>
            <div style={{ marginTop: '1rem' }}>
                <Link to="/login" style={{ color: '#aaa', textDecoration: 'none', fontSize: '0.9rem' }}>
                    Back to Login
                </Link>
            </div>
        </div>
    );
};

export default VerifyEmail;
