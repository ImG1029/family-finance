import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { StatusPage } from './StatusPage';
import { pingApi } from '../api/statusApi';

vi.mock('../api/statusApi', () => ({
  pingApi: vi.fn(),
}));

describe('StatusPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should show API response', async () => {
    (pingApi as vi.Mock).mockResolvedValue({ timestamp: 'ok' });

    render(
        < MemoryRouter >
          < StatusPage />
        < /MemoryRouter >
    );

    const messageElement = await screen.findByText(/ok/i);
    expect(messageElement).toBeInTheDocument();
  });
});