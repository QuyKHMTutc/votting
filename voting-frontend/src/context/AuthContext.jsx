import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Check if user is logged in
        const token = localStorage.getItem('token');
        const userData = localStorage.getItem('user');

        if (token && userData) {
            setUser(JSON.parse(userData));
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        try {
            const response = await authAPI.login({ email, password });
            const { token, ...userData } = response.data;

            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(userData);

            return { success: true };
        } catch (error) {
            return {
                success: false,
                error: error // api.js returns string message
            };
        }
    };

    const loginGoogle = async (token) => {
        try {
            // Save token temporarily to make the request
            localStorage.setItem('token', token);

            // Fetch user profile
            const response = await authAPI.getMyself();
            const userData = response.data; // Response should be AuthResponse (without token usually, or with null token)

            // Save user data
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(userData);

            return { success: true };
        } catch (error) {
            console.error("Google login error:", error);
            localStorage.removeItem('token'); // Cleanup if failed
            return {
                success: false,
                error: error
            };
        }
    };

    const register = async (userData) => {
        try {
            const response = await authAPI.register(userData);
            const { token, ...user } = response.data;

            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));
            localStorage.setItem('user', JSON.stringify(user));
            setUser(user);

            return { success: true, message: response.data.message };
        } catch (error) {
            return {
                success: false,
                error: error
            };
        }
    };

    const verify = async (email, code) => {
        try {
            await authAPI.verify(email, code);
            return { success: true };
        } catch (error) {
            return { success: false, error: error };
        }
    };

    const resendVerification = async (email) => {
        try {
            await authAPI.resendCode(email);
            return { success: true };
        } catch (error) {
            return { success: false, error: error };
        }
    };

    const forgotPassword = async (email) => {
        try {
            await authAPI.forgotPassword(email);
            return { success: true };
        } catch (error) {
            return { success: false, error: error };
        }
    };

    const resetPassword = async (data) => {
        try {
            await authAPI.resetPassword(data);
            return { success: true };
        } catch (error) {
            return { success: false, error: error };
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
    };

    const value = {
        user,
        login,
        loginGoogle,
        register,
        verify,
        resendVerification,
        forgotPassword,
        resetPassword,
        logout,
        isAuthenticated: !!user,
        loading
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};
