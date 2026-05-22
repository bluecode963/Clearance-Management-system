import { FormEvent, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { register, type AuthResponse, type UserRole } from '../services/api';

type RegisterPageProps = {
  onAuthenticated: (auth: AuthResponse) => void;
};

export function RegisterPage({ onAuthenticated }: RegisterPageProps) {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('STUDENT');
  const [studentId, setStudentId] = useState('');
  const [department, setDepartment] = useState('');
  const [program, setProgram] = useState('');
  const [yearOfStudy, setYearOfStudy] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const auth = await register({
        fullName,
        email,
        password,
        role,
        studentId: role === 'STUDENT' ? studentId : undefined,
        department: role === 'STUDENT' ? department : undefined,
        program: role === 'STUDENT' ? program : undefined,
        yearOfStudy: role === 'STUDENT' ? Number(yearOfStudy) : undefined,
      });
      setMessage(`Account created for ${auth.user.fullName}.`);
      onAuthenticated(auth);
    } catch (registerError) {
      setError(registerError instanceof Error ? registerError.message : 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell
      title="Register"
      subtitle="Create a demo account for a student, office staff member, registrar, or admin."
    >
      <form className="grid max-w-3xl gap-4 rounded border border-slate-200 bg-white p-6 shadow-sm md:grid-cols-2" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium text-slate-700">
          Full name
          <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setFullName(event.target.value)} required value={fullName} />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Email
          <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setEmail(event.target.value)} required type="email" value={email} />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Password
          <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" minLength={6} onChange={(event) => setPassword(event.target.value)} required type="password" value={password} />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Role
          <select className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setRole(event.target.value as UserRole)} value={role}>
            <option value="STUDENT">Student</option>
            <option value="OFFICE_STAFF">Office staff</option>
            <option value="REGISTRAR">Registrar</option>
            <option value="ADMIN">Admin</option>
          </select>
        </label>

        {role === 'STUDENT' && (
          <>
            <label className="block text-sm font-medium text-slate-700">
              Student ID
              <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setStudentId(event.target.value)} required value={studentId} />
            </label>
            <label className="block text-sm font-medium text-slate-700">
              Department
              <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setDepartment(event.target.value)} required value={department} />
            </label>
            <label className="block text-sm font-medium text-slate-700">
              Program
              <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setProgram(event.target.value)} required value={program} />
            </label>
            <label className="block text-sm font-medium text-slate-700">
              Year of study
              <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" min={1} onChange={(event) => setYearOfStudy(event.target.value)} required type="number" value={yearOfStudy} />
            </label>
          </>
        )}

        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700 md:col-span-2">{message}</p>}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700 md:col-span-2">{error}</p>}

        <button
          className="rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400 md:col-span-2"
          disabled={submitting}
          type="submit"
        >
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>
    </PageShell>
  );
}
