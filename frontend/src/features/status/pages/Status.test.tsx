import { render, screen } from '@testing-library/react';
import { StatusPage } from './StatusPage.tsx';
import { pingApi } from '../api/statusApi';

vi.mock('./api/statusApi');

describe('Integration test', () => {
    it('should show API response', async () => {
        vi.mocked(pingApi()).mockResolvedValue({timestamp: 'Mock response'});

        render(<StatusPage />);

        const messageElement = await screen.findByText('Mock response');
        expect(messageElement).toBeVisible();
    });
});