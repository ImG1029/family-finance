import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { HomePage } from './HomePage';
import { pingApiAuthenticated } from '../api/statusApi';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../api/statusApi', () => ({
  pingApiAuthenticated: vi.fn(),
}));

describe('HomePage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should logout, clean storage and redirect to /login', async () => {
    localStorage.setItem('access_token', 'active_token');
    (pingApiAuthenticated as vi.Mock).mockResolvedValue({ timestamp: 'ok' });

    render(
        < HomePage />
    );

    const user = userEvent.setup();
    const btnLogout = await screen.findByRole('button', { name: /Sair/i });

    await user.click(btnLogout);

    expect(localStorage.getItem('access_token')).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true });

  });
})
