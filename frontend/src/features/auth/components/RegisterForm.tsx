import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { register } from '../api/authApi';

export function RegisterForm() {
  const navigate = useNavigate();
  const [name, setName] = useState('')
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();

    setLoading(true);
    setError(null);

    try {
      const response = await register({ name, email, password });

      localStorage.setItem('access_token', response.accessToken);

      navigate('/', { replace: true })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Cadastro impossivel');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {error && <p style={{ color: 'red', margin: 0 }}>{error}</p>}

      <div>
        <label htmlFor="register-name" style={{ display: 'block', marginBottom: '4px' }}>Seu nome</label>
        <input
          id="register-name"
          type="text"
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <div>
        <label htmlFor="register-email" style={{ display: 'block', marginBottom: '4px' }}>E-mail</label>
        <input
          id="register-email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
        />
      </div>

      <div>
        <label htmlFor="register-password" style={{display: 'block', marginBottom: '4px' }}>Senha</label>
        <input
          id="register-password"
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
        {loading ? 'Cadastrando...': 'Cadastrar'}
      </button>
    </form>
  );
}