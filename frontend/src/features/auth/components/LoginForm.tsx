import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/authApi';

export function LoginForm() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<String | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();

    setLoading(true);
    setError(null);

    try {
      const response = await login({ email, password });

      localStorage.setItem('access_token', response.accessToken);

      navigate('/', { replace: true })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Credenciais invalidas');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {error && <p style={{ color: 'red', margin: 0 }}>{error}</p>}

      <div>
        <label htmlFor="login-email" style={{display: 'block', marginBottom: '4px'}}>E-mail</label>
        <input
          id="login-email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <div>
        <label htmlFor="login-password" style={{display: 'block', marginBottom: '4px' }}>Senha</label>
        <input
          id="login-password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        style={{ padding: '10px', cursor: loading ? 'not-allowed': 'pointer' }}
      >
        {loading ? 'Entrando...': 'Entrar'}
      </button>
    </form>
  );
}