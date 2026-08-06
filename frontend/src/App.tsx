import { useState, useEffect } from 'react';
import { fetchHello } from './services/api';

export function App() {
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchHello()
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

export default App;