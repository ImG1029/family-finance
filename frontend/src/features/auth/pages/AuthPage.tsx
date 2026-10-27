import { useState } from 'react';
import { LoginForm } from '../components/LoginForm.tsx'
import { RegisterForm } from '../components/RegisterForm.tsx'

export function AuthPage() {
  const [isRegister, setIsRegister] = useState(false);

  return (
    <div style={{width: '300px', margin: '60px auto', padding: '24px', border: '1px solid #ccc'}}>
      <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>
        {isRegister ? 'Cadastre-se' : 'Entre'}
      </h2>

      {isRegister ? <RegisterForm /> : <LoginForm />}

      <div>
        <span>{isRegister ? 'Já esta cadastrado?' : 'Ainda não se cadastrou?'}</span>
        <button
          type="button"
          onClick={() => setIsRegister(!isRegister)}
          style={{
            background: 'none',
            border: 'none',
            color: '#0066cc',
            cursor: 'pointer',
            fontWeight: 'bold',
            marginLeft: '6px',
            textDecoration: 'underline'
          }}
        >
          {isRegister ? 'Faça login' : 'Cadastre-se' }
        </button>
      </div>
    </div>
  );
}