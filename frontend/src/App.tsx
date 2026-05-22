import { useMemo, useState } from 'react';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { LoginPage } from './pages/LoginPage';
import { OfficeStaffDashboardPage } from './pages/OfficeStaffDashboardPage';
import { RegisterPage } from './pages/RegisterPage';
import { RegistrarDashboardPage } from './pages/RegistrarDashboardPage';
import { StudentDashboardPage } from './pages/StudentDashboardPage';
import type { AuthResponse, UserRole } from './services/api';

type PageKey = 'login' | 'register' | 'student' | 'admin' | 'office' | 'registrar';

const pages: Record<PageKey, string> = {
  login: 'Login',
  register: 'Register',
  student: 'Student',
  admin: 'Admin',
  office: 'Office Staff',
  registrar: 'Registrar',
};

export default function App() {
  const [activePage, setActivePage] = useState<PageKey>('login');

  function handleAuthenticated(auth: AuthResponse) {
    setActivePage(roleToPage(auth.user.role));
  }

  const page = useMemo(() => {
    switch (activePage) {
      case 'register':
        return <RegisterPage onAuthenticated={handleAuthenticated} />;
      case 'student':
        return <StudentDashboardPage />;
      case 'admin':
        return <AdminDashboardPage />;
      case 'office':
        return <OfficeStaffDashboardPage />;
      case 'registrar':
        return <RegistrarDashboardPage />;
      default:
        return <LoginPage onAuthenticated={handleAuthenticated} />;
    }
  }, [activePage]);

  return (
    <>
      <nav className="sticky top-0 z-10 border-b border-slate-200 bg-white/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-wrap gap-2 px-6 py-3">
          {(Object.entries(pages) as [PageKey, string][]).map(([key, label]) => (
            <button
              key={key}
              className={`rounded px-3 py-2 text-sm font-medium ${
                activePage === key
                  ? 'bg-brand-600 text-white'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
              onClick={() => setActivePage(key)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>
      </nav>
      {page}
    </>
  );
}

function roleToPage(role: UserRole): PageKey {
  switch (role) {
    case 'ADMIN':
      return 'admin';
    case 'OFFICE_STAFF':
      return 'office';
    case 'REGISTRAR':
      return 'registrar';
    default:
      return 'student';
  }
}
