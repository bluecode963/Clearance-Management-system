import { FormEvent, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { resetPassword } from '../services/api';

type ResetPasswordPageProps = {
  onBackToLogin: () => void;
};

export function ResetPasswordPage({ onBackToLogin }: ResetPasswordPageProps) {
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const response = await resetPassword({ token, newPassword });
      setMessage(response.message);
      setToken('');
      setNewPassword('');
    } catch (resetError) {
      setError(resetError instanceof Error ? resetError.message : 'Could not reset password');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell title="Reset Password" subtitle="Enter your reset token and choose a new password.">
      <form className="max-w-md space-y-4 rounded border border-slate-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium text-slate-700">
          Reset token
          <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" onChange={(event) => setToken(event.target.value)} required value={token} />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          New password
          <input className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" minLength={6} onChange={(event) => setNewPassword(event.target.value)} required type="password" value={newPassword} />
        </label>
        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        <button className="w-full rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" disabled={submitting} type="submit">
          {submitting ? 'Resetting...' : 'Reset password'}
        </button>
        <button className="text-sm font-semibold text-brand-700" onClick={onBackToLogin} type="button">
          Back to login
        </button>
      </form>
    </PageShell>
  );
}
