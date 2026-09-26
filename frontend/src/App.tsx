import { Routes, Route } from 'react-router-dom';
import { StatusPage } from './features/status'

export function App() {
  return (
    <Routes>
      <Route path="/"
             element={
          <StatusPage/>
      }/>

      <Route path="*" element={ <h1> Error 404 - Route does not exist </h1> } />
    </Routes>
  );
}

export default App;