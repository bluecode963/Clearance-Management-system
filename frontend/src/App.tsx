import { useEffect, useMemo, useState } from 'react';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { LoginPage } from './pages/LoginPage';
import { OfficeStaffDashboardPage } from './pages/OfficeStaffDashboardPage';
import { RegistrarDashboardPage } from './pages/RegistrarDashboardPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { StudentDashboardPage } from './pages/StudentDashboardPage';
import { clearToken, getSavedUser, getToken, logout, type AuthResponse, type UserResponse, type UserRole } from './services/api';

type PageKey = 'login' | 'forgot-password' | 'reset-password' | 'student' | 'admin' | 'office-staff' | 'registrar';

const rolePaths: Record<UserRole, PageKey> = {
  STUDENT: 'student',
  ADMIN: 'admin',
  OFFICE_STAFF: 'office-staff',
  REGISTRAR: 'registrar',
};

export default function App() {
  const [activePage, setActivePage] = useState<PageKey>(() => pathToPage(window.location.pathname));
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(() => (getToken() ? getSavedUser() : null));

  useEffect(() => {
    function handlePopState() {
      setActivePage(pathToPage(window.location.pathname));
    }

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  function navigate(page: PageKey) {
    window.history.pushState(null, '', pageToPath(page));
    setActivePage(page);
  }

  function handleAuthenticated(auth: AuthResponse) {
    setCurrentUser(auth.user);
    navigate(roleToPage(auth.user.role));
  }

  async function handleLogout() {
    await logout();
    setCurrentUser(null);
    navigate('login');
  }

  const page = useMemo(() => {
    if (activePage === 'forgot-password') {
      return (
        <ForgotPasswordPage
          onBackToLogin={() => navigate('login')}
          onResetPassword={() => navigate('reset-password')}
        />
      );
    }

    if (activePage === 'reset-password') {
      return <ResetPasswordPage onBackToLogin={() => navigate('login')} />;
    }

    if (activePage === 'login') {
      return <LoginPage onAuthenticated={handleAuthenticated} onForgotPassword={() => navigate('forgot-password')} />;
    }

    const token = getToken();
    if (!token || !currentUser) {
      clearToken();
      if (activePage !== 'login') {
        setTimeout(() => navigate('login'), 0);
      }
      return <LoginPage onAuthenticated={handleAuthenticated} onForgotPassword={() => navigate('forgot-password')} />;
    }

    const allowedPage = roleToPage(currentUser.role);
    if (activePage !== allowedPage) {
      setTimeout(() => navigate(allowedPage), 0);
      return (
        <AccessDenied
          message="Access denied for this dashboard. Redirecting to your assigned dashboard."
          onLogout={handleLogout}
        />
      );
    }

    switch (activePage) {
      case 'student':
        return <StudentDashboardPage />;
      case 'admin':
        return <AdminDashboardPage />;
      case 'office-staff':
        return <OfficeStaffDashboardPage />;
      case 'registrar':
        return <RegistrarDashboardPage />;
      default:
        return <LoginPage onAuthenticated={handleAuthenticated} onForgotPassword={() => navigate('forgot-password')} />;
    }
  }, [activePage, currentUser]);

  return (
    <>
      <nav className="sticky top-0 z-10 border-b border-slate-200 bg-white/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-2 px-6 py-3">
          <button className="font-semibold text-slate-950" onClick={() => navigate(currentUser ? roleToPage(currentUser.role) : 'login')} type="button">
            Student Clearance
          </button>
          {currentUser ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-slate-600">{currentUser.fullName} - {currentUser.role}</span>
              <button className="rounded border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50" onClick={() => void handleLogout()} type="button">
                Logout
              </button>
            </div>
          ) : (
            <button className="rounded bg-brand-600 px-3 py-2 text-sm font-semibold text-white" onClick={() => navigate('login')} type="button">
              Login
            </button>
          )}
        </div>
      </nav>
      {page}
    </>
  );
}

function roleToPage(role: UserRole): PageKey {
  return rolePaths[role];
}

function pathToPage(path: string): PageKey {
  switch (path) {
    case '/student':
      return 'student';
    case '/admin':
      return 'admin';
    case '/office-staff':
      return 'office-staff';
    case '/registrar':
      return 'registrar';
    case '/forgot-password':
      return 'forgot-password';
    case '/reset-password':
      return 'reset-password';
    default:
      return 'login';
  }
}

function pageToPath(page: PageKey) {
  return page === 'login' ? '/login' : `/${page}`;
}

function AccessDenied({ message, onLogout }: { message: string; onLogout: () => Promise<void> }) {
  return (
    <main className="mx-auto max-w-3xl px-6 py-10">
      <div className="rounded border border-red-200 bg-red-50 p-6">
        <h1 className="text-xl font-semibold text-red-900">Access denied</h1>
        <p className="mt-2 text-sm text-red-700">{message}</p>
        <button className="mt-4 rounded bg-red-700 px-4 py-2 text-sm font-semibold text-white" onClick={() => void onLogout()} type="button">
          Logout
        </button>
      </div>
    </main>
  );
}
