import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { vi } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';

describe('ProtectedRoute Component', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should redirect to /login if there is no access_token', () => {
    render(
        < MemoryRouter initialEntries={['/privado']} >
          < Routes >
            < Route path="/login" element={ < h1 >Tela de Login< /h1 > } />
            < Route
                path="/privado"
                element={
                  < ProtectedRoute >
                    < h1 >Conteudo Protegido< /h1 >
                  < /ProtectedRoute >
                }
            />
          < /Routes >
        < /MemoryRouter >
    );

    expect(screen.getByText('Tela de Login')).toBeInTheDocument();
    expect(screen.queryByText('Conteudo Protegido')).not.toBeInTheDocument();
  });

  it('should render the son component if access_token exists', () => {
    localStorage.setItem('access_token', 'fake_token_jwt');

    render(
        < MemoryRouter initialEntries={['/privado']} >
          < Routes >
            < Route
                path="/privado"
                element={
                  < ProtectedRoute >
                    < h1 >Conteudo Protegido< /h1 >
                  < /ProtectedRoute >
                }
            />
          < /Routes >
        < /MemoryRouter >
    );

    expect(screen.getByText('Conteudo Protegido')).toBeInTheDocument();
  });
});