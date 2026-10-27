import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { LoginForm } from './LoginForm';
import { login } from '../api/authApi';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../api/authApi', () => ({
  login: vi.fn(),
}));

describe('LoginForm Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should login, save token and redirect', async () => {
    const mockResponse = {
      accessToken: 'fake_jwt_token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: '1', name: 'user', email: 'test@email.com' }
    };
    (login as vi.Mock).mockResolvedValue(mockResponse);

    render(< LoginForm />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/E-mail/i), 'test@email.com');
    await user.type(screen.getByLabelText(/Senha/i), 'password123');
    await user.click(screen.getByRole('button', { name: /Entrar/i }));

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({ email: 'test@email.com', password: 'password123' });
      expect(localStorage.getItem('access_token')).toBe('fake_jwt_token');
      expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true });
    });
  });

  it('should show error on screen if API rejects credentials', async () => {
    const mockError = { response: { data: { message: 'Credenciais invalidas' } } };
    (login as vi.Mock).mockRejectedValue(mockError);

    render(< LoginForm />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/E-mail/i), 'wrong@email.com');
    await user.type(screen.getByLabelText(/Senha/i), 'wrong password');
    await user.click(screen.getByRole('button', { name: /Entrar/i }));

    await waitFor(() => {
      expect(screen.getByText('Credenciais invalidas')).toBeInTheDocument();
    });

    expect(localStorage.getItem('access_token')).toBeNull();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});