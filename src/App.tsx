import { useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { PatientApp } from './components/PatientApp';

function App() {
  const auth = useAuth();
  const [demoAuth, setDemoAuth] = useState<boolean>(false);

  const handleSignOut = () => {
    if (demoAuth) {
      setDemoAuth(false);
    } else {
      auth.removeUser();
      auth.signoutRedirect();
    }
  };

  const handleRetry = () => {
    sessionStorage.clear();
    localStorage.clear();
    window.location.href = '/';
  };

  const isUserAuthenticated = auth.isAuthenticated || demoAuth;
  const userEmail = auth.user?.profile.email || (demoAuth ? 'medico.guardia@duocuc.cl' : '');

  // 1. Mientras Cognito verifica la sesión
  if (auth.isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
        <div className="text-center p-5 card shadow-sm">
          <div className="spinner-border text-primary mb-3" style={{ width: '3rem', height: '3rem' }} role="status"></div>
          <h5 className="text-secondary fw-semibold">Conectando con AWS Cognito...</h5>
          <small className="text-muted">Desarrollo Cloud Native</small>
        </div>
      </div>
    );
  }

  // 2. Si ocurrió un error en Cognito pero el usuario quiere entrar al sistema
  if (auth.error && !demoAuth) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
        <div className="card shadow p-4 text-center" style={{ maxWidth: '520px', borderRadius: '16px' }}>
          <div className="text-danger fs-1 mb-2">🔐</div>
          <h4 className="fw-bold text-dark">Autenticación AWS Cognito (IDaaS)</h4>
          <p className="text-danger small mt-2">
            {auth.error.message}
          </p>
          
          <div className="d-grid gap-2 mt-4">
            <button className="btn btn-outline-primary" onClick={handleRetry}>
              🔄 Limpiar Sesión y Conectar a Cognito
            </button>
            <button className="btn btn-success fw-bold py-2 shadow-sm" onClick={() => setDemoAuth(true)}>
              👨‍⚕️ Entrar al Sistema Hospitalario (Modo Demostración)
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 3. Usuario Autenticado (Por Cognito o Demostración)
  if (isUserAuthenticated) {
    return (
      <div className="d-flex flex-column min-vh-100">
        {/* Barra de navegación moderna */}
        <header className="navbar navbar-expand-lg navbar-dark bg-primary px-4 shadow-sm">
          <div className="container-fluid">
            <span className="navbar-brand fw-bold d-flex align-items-center gap-2">
              <span className="fs-3">🏥</span>
              <span>Hospital Metropolitano <small className="fs-6 fw-normal opacity-75">| Cloud Native</small></span>
            </span>

            {/* Badges de Arquitectura */}
            <div className="d-none d-md-flex align-items-center gap-2 me-auto ms-4">
              <span className="badge bg-success text-white">🟢 IDaaS: AWS Cognito</span>
              <span className="badge bg-info text-dark">🐰 RabbitMQ: 2 Nodos Clúster (AWS)</span>
              <span className="badge bg-light text-dark">⚡ Spring Boot 3 API</span>
            </div>

            {/* Datos de usuario y botón salir */}
            <div className="d-flex align-items-center gap-3">
              <div className="text-end text-light">
                <small className="d-block opacity-75">{demoAuth ? 'Sesión Demo' : 'Sesión Cognito'}</small>
                <span className="fw-semibold">{userEmail}</span>
              </div>
              <button className="btn btn-outline-light btn-sm px-3" onClick={handleSignOut}>
                Cerrar Sesión
              </button>
            </div>
          </div>
        </header>

        {/* Contenido Principal */}
        <main className="flex-grow-1 py-4">
          <PatientApp />
        </main>

        {/* Footer */}
        <footer className="bg-white py-3 border-top text-center text-muted small mt-auto">
          DSY1107 - Desarrollo Cloud Native I | Duoc UC 2025
        </footer>
      </div>
    );
  }

  // 4. Pantalla de Bienvenida
  return (
    <div className="d-flex justify-content-center align-items-center vh-100" style={{ background: 'linear-gradient(135deg, #0d6efd 0%, #0a4296 100%)' }}>
      <div className="card shadow-lg p-5 text-center text-dark" style={{ maxWidth: '480px', borderRadius: '16px' }}>
        <div className="mb-3 fs-1">🏥</div>
        <h2 className="fw-bold text-primary mb-1">Sistema Hospitalario</h2>
        <span className="badge bg-primary bg-opacity-10 text-primary mb-4 py-2 px-3">Arquitectura Cloud Native</span>
        
        <p className="text-secondary mb-4">
          Acceso seguro mediante <strong>IDaaS (AWS Cognito)</strong> y comunicación desacoplada con <strong>RabbitMQ Clúster</strong>.
        </p>

        <div className="d-grid gap-3">
          <button 
            className="btn btn-primary btn-lg py-3 fw-bold shadow-sm"
            onClick={() => auth.signinRedirect()}
          >
            🔐 Iniciar Sesión con AWS Cognito
          </button>

          <button 
            className="btn btn-outline-secondary btn-sm"
            onClick={() => setDemoAuth(true)}
          >
            👨‍⚕️ Acceso Rápido Demostración
          </button>
        </div>

        <div className="mt-4 pt-3 border-top text-muted small">
          Autenticación federada con OAuth 2.0 / OpenID Connect
        </div>
      </div>
    </div>
  );
}

export default App;
