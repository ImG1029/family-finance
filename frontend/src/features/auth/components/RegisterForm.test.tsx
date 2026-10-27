import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { RegisterForm } from './RegisterForm';
import { register } from '../api/authApi';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../api/authApi', () => ({
  register: vi.fn(),
}));

describe('RegisterForm Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should register, save token and redirect', async () => {
    const mockResponse = {
      accessToken: 'fake_jwt_token_register',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: '1', name: 'user', email: 'test@email.com' }
    };
    (register as vi.Mock).mockResolvedValue(mockResponse);

    render(< RegisterForm />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Seu nome/i), 'user');
    await user.type(screen.getByLabelText(/E-mail/i), 'test@email.com');
    await user.type(screen.getByLabelText(/Senha/i), 'password123');
    await user.click(screen.getByRole('button', { name: /Cadastrar/i }));

    await waitFor(() => {
      expect(register).toHaveBeenCalledWith({
        name: 'user',
        email: 'test@email.com',
        password: 'password123'
      });
      expect(localStorage.getItem('access_token')).toBe('fake_jwt_token_register');
      expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true });
    });
  });
});