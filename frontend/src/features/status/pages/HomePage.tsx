import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { pingApiAuthenticated } from '../api/statusApi';

export function HomePage() {
  const navigate = useNavigate();
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    pingApiAuthenticated()
        .then(setMessage)
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false))
  }, []);

  function handleLogout() {
    localStorage.removeItem('access_token')
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    navigate('/login', {replace: true});
  }

  if (loading) {
    return <p>Conectando ao Spring Boot...</p>;
  }

  if (error) {
    return <p style={{color: 'red'}}>Falha na conexão: {error}</p>
  }


  return (
      <div>
        <h1>Integração React + Spring Boot</h1>
        <h2>Logado</h2>
        <p>Hora da resposta: <strong>{message.timestamp}</strong></p>
        <button onClick={handleLogout}>Sair</button>
      </div>
  );
}