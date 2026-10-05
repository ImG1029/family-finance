import { Routes, Route } from 'react-router-dom';
import { AuthPage, ProtectedRoute } from './features/auth';
import { StatusPage, HomePage } from './features/status'

export function App() {
  return (
    <Routes>
      <Route path="/login" element={ <AuthPage/> } />
      <Route path="/ping" element={ <StatusPage/> } />
      <Route path="/"
             element={
        <ProtectedRoute>
          <HomePage/>
        </ProtectedRoute>
      }/>

      <Route path="*" element={ <h1> Error 404 - Route does not exist </h1> } />
    </Routes>
  );
}

export default App;