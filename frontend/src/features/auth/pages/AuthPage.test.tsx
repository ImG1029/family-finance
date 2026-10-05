import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AuthPage } from './AuthPage';

describe('AuthPage Component', () => {
  it('should switch between login and register forms when clicking the link', async () => {
    render(
        < MemoryRouter >
          < AuthPage />
        < /MemoryRouter >
    );

    const user = userEvent.setup();

    expect(screen.getByRole('heading', { name: 'Entre' })).toBeInTheDocument();

    const toggleToRegister = screen.getByRole('button', { name: 'Cadastre-se' });
    await user.click(toggleToRegister);

    expect(screen.getByRole('heading', { name: 'Cadastre-se' })).toBeInTheDocument();

    const toggleToLogin = screen.getByRole('button', { name: 'Faça login' });
    await user.click(toggleToLogin);

    expect(screen.getByRole('heading', { name: 'Entre' })).toBeInTheDocument();
  });
});