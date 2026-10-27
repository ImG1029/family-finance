import { api } from '../../../config/api'
import type { LoginCredentials, RegisterData, AuthResponse } from '../types';

export async function login(data: LoginCredentials): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/authentication/login', data);
    return res.data;
}

export async function register(data: RegisterData): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/authentication/register', data)
    return res.data;
}