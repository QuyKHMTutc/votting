import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { pollAPI } from '../services/api';
import '../styles/CreatePoll.css';

const CreatePoll = () => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        description: '',
        endDate: '',
        mode: 'PUBLIC',
        invitedEmails: '',
        options: ['', '']
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleOptionChange = (index, value) => {
        const newOptions = [...formData.options];
        newOptions[index] = value;
        setFormData({ ...formData, options: newOptions });
    };

    const addOption = () => {
        setFormData({
            ...formData,
            options: [...formData.options, '']
        });
    };

    const removeOption = (index) => {
        if (formData.options.length > 2) {
            const newOptions = formData.options.filter((_, i) => i !== index);
            setFormData({ ...formData, options: newOptions });
        }
    };

    const handleFileUpload = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (event) => {
            const content = event.target.result;
            // Split by comma, newline, or semicolon
            const emails = content.split(/[\n,;]+/).map(e => e.trim()).filter(e => e);

            // Append to existing emails or replace? Let's append if not empty
            const currentEmails = formData.invitedEmails ? formData.invitedEmails.split(',').map(e => e.trim()).filter(e => e) : [];
            const allEmails = [...new Set([...currentEmails, ...emails])]; // Deduplicate

            setFormData({
                ...formData,
                invitedEmails: allEmails.join(', ')
            });
        };
        reader.readAsText(file);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Filter out empty options
        const validOptions = formData.options.filter(opt => opt.trim() !== '');

        if (validOptions.length < 2) {
            setError('Please provide at least 2 options');
            return;
        }

        setLoading(true);

        try {
            const pollData = {
                title: formData.title,
                description: formData.description,
                options: validOptions,
                endDate: formData.endDate || null,
                mode: formData.mode,
                invitedEmails: formData.mode === 'PRIVATE' && formData.invitedEmails
                    ? formData.invitedEmails.split(',').map(e => e.trim()).filter(e => e)
                    : []
            };

            const response = await pollAPI.createPoll(pollData);
            navigate(`/poll/${response.data.id}`);
        } catch (err) {
            setError(err.response?.data || 'Failed to create poll');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="create-poll-container">
            <div className="create-poll-card">
                <h2>Create New Poll</h2>

                <form onSubmit={handleSubmit} className="create-poll-form">
                    {error && <div className="error-message">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="title">Poll Title *</label>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            required
                            placeholder="Enter poll title"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description (Optional)</label>
                        <textarea
                            id="description"
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            rows="3"
                            placeholder="Provide more details about this poll"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="endDate">End Date (Optional)</label>
                        <input
                            type="datetime-local"
                            id="endDate"
                            name="endDate"
                            value={formData.endDate}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="form-group">
                        <label>Poll Mode</label>
                        <div className="mode-selection">
                            <label className={`mode-option ${formData.mode === 'PUBLIC' ? 'selected' : ''}`}>
                                <input
                                    type="radio"
                                    name="mode"
                                    value="PUBLIC"
                                    checked={formData.mode === 'PUBLIC'}
                                    onChange={handleChange}
                                />
                                <span className="mode-icon">🌍</span>
                                <span className="mode-title">Public</span>
                            </label>
                            <label className={`mode-option ${formData.mode === 'PRIVATE' ? 'selected' : ''}`}>
                                <input
                                    type="radio"
                                    name="mode"
                                    value="PRIVATE"
                                    checked={formData.mode === 'PRIVATE'}
                                    onChange={handleChange}
                                />
                                <span className="mode-icon">🔒</span>
                                <span className="mode-title">Private</span>
                            </label>
                        </div>
                    </div>

                    {formData.mode === 'PRIVATE' && (
                        <div className="form-group">
                            <label htmlFor="invitedEmails">Invite People (Required)</label>
                            <div className="email-input-container">
                                <textarea
                                    id="invitedEmails"
                                    name="invitedEmails"
                                    value={formData.invitedEmails}
                                    onChange={handleChange}
                                    rows="3"
                                    placeholder="Enter email addresses separated by commas (e.g., alice@example.com, bob@example.com)"
                                    required={formData.mode === 'PRIVATE'}
                                />
                                <div className="file-upload-wrapper">
                                    <label htmlFor="emailFile" className="btn-upload">
                                        📂 Import from File (.txt, .csv)
                                    </label>
                                    <input
                                        type="file"
                                        id="emailFile"
                                        accept=".txt,.csv"
                                        onChange={handleFileUpload}
                                        style={{ display: 'none' }}
                                    />
                                </div>
                            </div>
                            <p className="field-hint">We will send an invitation link to these emails.</p>
                        </div>
                    )}

                    <div className="options-section">
                        <label>Poll Options *</label>
                        {formData.options.map((option, index) => (
                            <div key={index} className="option-input-group">
                                <input
                                    type="text"
                                    value={option}
                                    onChange={(e) => handleOptionChange(index, e.target.value)}
                                    placeholder={`Option ${index + 1}`}
                                    required
                                />
                                {formData.options.length > 2 && (
                                    <button
                                        type="button"
                                        className="btn-remove"
                                        onClick={() => removeOption(index)}
                                    >
                                        ✕
                                    </button>
                                )}
                            </div>
                        ))}
                        <button type="button" className="btn-add-option" onClick={addOption}>
                            + Add Option
                        </button>
                    </div>

                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn-cancel"
                            onClick={() => navigate('/')}
                        >
                            Cancel
                        </button>
                        <button type="submit" className="btn-submit" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Poll'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreatePoll;
