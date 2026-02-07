import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add response interceptor to unwrap standard API response
api.interceptors.response.use(
    (response) => {
        // If response follows standard format { success: true, message: "...", data: ... }
        if (response.data && response.data.success !== undefined) {
            if (response.data.success) {
                // Create a new response object with unwrapped data but keeping headers/status
                return {
                    ...response,
                    data: response.data.data, // Unwrap data
                    originalData: response.data // Keep original structure if needed
                };
            } else {
                return Promise.reject(response.data.message || 'Error');
            }
        }
        return response;
    },
    (error) => {
        // Handle 401/403 errors (Unauthorized/Forbidden) - likely token expired or user locked
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
            return Promise.reject("Session expired or access denied. Please login again.");
        }

        // Handle error response from backend
        if (error.response && error.response.data) {
            const errorMessage = error.response.data.message || error.response.data.error || 'Something went wrong';
            return Promise.reject(errorMessage);
        }
        return Promise.reject(error);
    }
);

// Auth APIs
export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
    getMyself: () => api.get('/auth/me'),
    verify: (email, code) => api.post(`/auth/verify?email=${email}&code=${code}`),
    resendCode: (email) => api.post(`/auth/resend-code?email=${email}`),
    forgotPassword: (email) => api.post(`/auth/forgot-password?email=${email}`),
    resetPassword: (data) => api.post('/auth/reset-password', data),
};

// Poll APIs
export const pollAPI = {
    getAllPolls: () => api.get('/polls'),
    getActivePolls: () => api.get('/polls/active'),
    getPollById: (id) => api.get(`/polls/${id}`),
    createPoll: (data) => api.post('/polls', data),
    updatePoll: (id, data) => api.put(`/polls/${id}`, data),
    deletePoll: (id) => api.delete(`/polls/${id}`),
    closePoll: (id) => api.post(`/polls/${id}/close`),
    getCreatedPolls: () => api.get('/polls/created'),
    getVotedPolls: () => api.get('/polls/voted'),
};

// Vote APIs
export const voteAPI = {
    submitVote: (pollId, data) => api.post(`/polls/${pollId}/vote`, data),
    getMyVote: (pollId) => api.get(`/polls/${pollId}/my-vote`),
};

// Admin APIs
export const adminAPI = {
    getStats: () => api.get('/admin/stats'),
    getUsers: () => api.get('/admin/users'),
    toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
    deletePoll: (id) => api.delete(`/admin/polls/${id}`),
};

export default api;
