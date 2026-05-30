import { FormEvent, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { forgotPassword } from '../services/api';

type ForgotPasswordPageProps = {
  onBackToLogin: () => void;
  onResetPassword: () => void;
};

export function ForgotPasswordPage({ onBackToLogin, onResetPassword }: ForgotPasswordPageProps) {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setResetToken('');
    setError('');
    setSubmitting(true);

    try {
      const response = await forgotPassword(email);
      setMessage(response.message);
      if (response.resetToken) {
        setResetToken(response.resetToken);
      }
    } catch (forgotError) {
      setError(forgotError instanceof Error ? forgotError.message : 'Could not start reset process');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell title="Forgot Password" subtitle="Start a development password reset for your account.">
      <form className="max-w-md space-y-4 rounded border border-slate-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium text-slate-700">
          Email
          <input
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            onChange={(event) => setEmail(event.target.value)}
            required
            type="email"
            value={email}
          />
        </label>
        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
        {resetToken && (
          <div className="rounded bg-slate-50 px-3 py-2 text-sm text-slate-700">
            <p className="font-semibold">Development reset token</p>
            <p className="break-all">{resetToken}</p>
          </div>
        )}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        <button className="w-full rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400" disabled={submitting} type="submit">
          {submitting ? 'Sending...' : 'Start reset'}
        </button>
        <div className="flex justify-between text-sm">
          <button className="font-semibold text-brand-700" onClick={onBackToLogin} type="button">
            Back to login
          </button>
          <button className="font-semibold text-brand-700" onClick={onResetPassword} type="button">
            I have a token
          </button>
        </div>
      </form>
    </PageShell>
  );
}
