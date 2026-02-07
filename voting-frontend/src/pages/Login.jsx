import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import VerifyEmail from '../components/VerifyEmail';
import '../styles/Auth.css';

const Login = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [showVerify, setShowVerify] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        const result = await login(formData.email, formData.password);

        if (result.success) {
            // Check role for redirection
            if (result.user && result.user.role === 'ADMIN') {
                navigate('/admin');
            } else {
                navigate('/');
            }
        } else {
            console.log("Login Error:", result.error);
            // Check for disabled/unverified account
            // "Account is disabled" comes from GlobalExceptionHandler
            if (result.error && (
                (typeof result.error === 'string' && result.error.includes("disabled")) ||
                (result.error.data && result.error.data.message && result.error.data.message.includes("disabled"))
            )) {
                setError("Account is not activated yet. Please verify your email.");
                setShowVerify(true);
            } else {
                setError(typeof result.error === 'string' ? result.error : 'Invalid email or password');
            }
        }

        setLoading(false);
    };

    if (showVerify) {
        return (
            <div className="auth-container">
                <VerifyEmail email={formData.email} onVerified={() => navigate('/')} />
            </div>
        );
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">Welcome Back!</h2>
                <p className="auth-subtitle">Login to start voting</p>

                <form onSubmit={handleSubmit} className="auth-form">
                    {error && <div className="error-message">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            placeholder="Enter your email"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            placeholder="Enter your password"
                        />
                    </div>

                    <button type="submit" className="btn-submit" disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                    <div style={{ textAlign: 'right', marginTop: '0.5rem' }}>
                        <Link to="/forgot-password" style={{ fontSize: '0.9rem', color: '#646cff', textDecoration: 'none' }}>
                            Forgot Password?
                        </Link>
                    </div>

                    <div className="separator">
                        <span>Or continue with</span>
                    </div>

                    <a href="http://localhost:8080/oauth2/authorization/google" className="btn-google">
                        <svg className="google-icon" viewBox="0 0 24 24">
                            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                        </svg>
                        Sign in with Google
                    </a>
                </form>

                <p className="auth-link">
                    Don't have an account? <Link to="/register">Register here</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
