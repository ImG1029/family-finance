import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { api } from './api';

describe('Axios Interceptors', () => {
    let mockApi: MockAdapter;
    let mockGlobal: MockAdapter;

    beforeEach(() => {
        mockApi = new MockAdapter(api);

        mockGlobal = new MockAdapter(axios);

        localStorage.clear();

        Object.defineProperty(window, 'location', {
            value: { href: 'http://localhost/' },
            writable: true,
        });
    });

    afterEach(() => {
        mockApi.reset();
        mockGlobal.reset();
    });

    it('should inject access_token in header if it exists in localStorage', async () => {
        localStorage.setItem('access_token', 'test_token');
        mockApi.onGet('/ping').reply(200, { ok: true });

        const response = await api.get('/ping');

        expect(response.config.headers['Authorization']).toBe('Bearer test_token');
    });

    it('should renew token automatically when receiving error 403 and retry original request', async () => {
        const URL_PING = '/ping';
        const URL_REFRESH = `${import.meta.env.VITE_API_URL}/authentication/refresh`;

        localStorage.setItem('access_token', 'old_token');

        mockApi.onGet(URL_PING).replyOnce(403);

        mockGlobal.onPost(URL_REFRESH).replyOnce(200, {
            accessToken: 'new_token',
        });

        mockApi.onGet(URL_PING).replyOnce(200, { success: true });

        const response = await api.get(URL_PING);

        expect(response.data.success).toBe(true);
        expect(localStorage.getItem('access_token')).toBe('new_token');
        expect(response.config.headers['Authorization']).toBe('Bearer new_token');
    });

    it('should remove user and clear memory if refresh token also fails', async () => {
        const URL_PING = '/ping';
        const URL_REFRESH = `${import.meta.env.VITE_API_URL}/authentication/refresh`;

        localStorage.setItem('access_token', 'old_token');

        mockApi.onGet(URL_PING).replyOnce(403);

        // O refresh token falha na instância global
        mockGlobal.onPost(URL_REFRESH).replyOnce(403);

        try {
            await api.get(URL_PING);
            expect.fail('Requisition should have failed.');
        } catch (error) {
            console.log(error)
            expect(localStorage.getItem('access_token')).toBeNull();
            expect(window.location.href).toBe('/login');
        }
    });
});