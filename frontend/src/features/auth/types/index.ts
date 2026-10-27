export interface LoginCredentials {
    email: string;
    password: string;
}

export interface RegisterData extends LoginCredentials {
    name: string
}

export interface AuthResponse {
    accessToken: string;
    tokenType: string
    user: {
        id: string;
        name: string;
        email: string;
    };
    expiresInSeconds: number;
}