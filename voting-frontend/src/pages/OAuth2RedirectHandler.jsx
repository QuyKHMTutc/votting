import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { loginGoogle } = useAuth(); // Use loginGoogle instead of login

    useEffect(() => {
        const processLogin = async () => {
            const token = searchParams.get('token');
            if (token) {
                const result = await loginGoogle(token);
                if (result.success) {
                    navigate('/');
                } else {
                    console.error("Login failed:", result.error);
                    navigate('/login?error=true');
                }
            } else {
                navigate('/login');
            }
        };

        processLogin();
    }, [searchParams, loginGoogle, navigate]);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-xl">Processing login...</div>
        </div>
    );
};

export default OAuth2RedirectHandler;
