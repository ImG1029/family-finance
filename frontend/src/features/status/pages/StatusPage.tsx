import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { pingApi } from '../api/statusApi';

export function StatusPage() {
  const navigate = useNavigate();
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    pingApi()
      .then(setMessage)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, []);


  if (loading) {
    return <p>Conectando ao Spring Boot...</p>;
  }

  if (error) {
    return <p style={{ color: 'red' }}>Falha na conexão: {error}</p>
  }

  return (
      <div>
        <h1>Integração React + Spring Boot</h1>
        <p>Hora da resposta: <strong>{message.timestamp}</strong> </p>
      </div>
  );
}