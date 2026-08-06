import { render, screen } from '@testing-library/react';
import { App } from './App';
import { fetchHello } from './services/api';

vi.mock('./services/api');

describe('Integration test', () => {
    it('should show API response', async () => {
        vi.mocked(fetchHello).mockResolvedValue({timestamp: 'Mock response'});

        render(<App />);

        const messageElement = await screen.findByText('Mock response');
        expect(messageElement).toBeVisible();
    });
});