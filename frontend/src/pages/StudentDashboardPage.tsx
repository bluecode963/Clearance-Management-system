import { FormEvent, useEffect, useMemo, useState } from 'react';
import { PageShell } from '../components/PageShell';
import {
  createClearanceRequest,
  getMyClearanceRequests,
  type ClearanceRequestResponse,
  type ClearanceType,
} from '../services/api';

export function StudentDashboardPage() {
  const [requestType, setRequestType] = useState<ClearanceType>('GRADUATION');
  const [reason, setReason] = useState('');
  const [requests, setRequests] = useState<ClearanceRequestResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadRequests() {
    setLoading(true);
    setError('');
    try {
      const page = await getMyClearanceRequests();
      setRequests(page.content);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not load requests');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadRequests();
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const created = await createClearanceRequest({
        requestType,
        reason: reason.trim() || undefined,
      });
      setMessage(`Clearance request #${created.id} created.`);
      setReason('');
      await loadRequests();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not create request');
    } finally {
      setSubmitting(false);
    }
  }

  const metrics = useMemo(() => {
    const latest = requests[0];
    const approvedSteps = latest?.steps.filter((step) => step.status === 'APPROVED').length ?? 0;
    const totalSteps = latest?.steps.length ?? 0;
    return [
      { label: 'My requests', value: String(requests.length) },
      { label: 'Latest progress', value: totalSteps ? `${approvedSteps} / ${totalSteps}` : '0 / 0' },
      { label: 'Latest status', value: latest?.status.replaceAll('_', ' ') ?? 'None' },
    ];
  }, [requests]);

  return (
    <PageShell title="Student Dashboard" subtitle="Track one clearance request and each required office step.">
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-3">
          {metrics.map((metric) => (
            <div key={metric.label} className="rounded border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-sm text-slate-500">{metric.label}</p>
              <p className="mt-2 text-2xl font-bold text-slate-950">{metric.value}</p>
            </div>
          ))}
        </div>

        <form className="grid gap-4 rounded border border-slate-200 bg-white p-6 shadow-sm md:grid-cols-2" onSubmit={handleSubmit}>
          <label className="block text-sm font-medium text-slate-700">
            Clearance type
            <select
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              onChange={(event) => setRequestType(event.target.value as ClearanceType)}
              value={requestType}
            >
              <option value="GRADUATION">Graduation</option>
              <option value="WITHDRAWAL">Withdrawal</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </label>
          <label className="block text-sm font-medium text-slate-700 md:col-span-2">
            Reason
            <textarea
              className="mt-1 min-h-24 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              maxLength={1000}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Optional note for your clearance request"
              value={reason}
            />
          </label>
          {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700 md:col-span-2">{message}</p>}
          {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700 md:col-span-2">{error}</p>}
          <button
            className="rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400 md:col-span-2"
            disabled={submitting}
            type="submit"
          >
            {submitting ? 'Creating request...' : 'Create clearance request'}
          </button>
        </form>

        <section className="rounded border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-3">
            <h2 className="font-semibold text-slate-950">My clearance requests</h2>
          </div>
          {loading ? (
            <p className="px-4 py-6 text-sm text-slate-600">Loading requests...</p>
          ) : requests.length === 0 ? (
            <p className="px-4 py-6 text-sm text-slate-600">No clearance requests yet.</p>
          ) : (
            <div className="divide-y divide-slate-100">
              {requests.map((request) => (
                <article key={request.id} className="space-y-4 px-4 py-5">
                  <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p className="font-semibold text-slate-950">
                        #{request.id} {request.requestType.replaceAll('_', ' ')}
                      </p>
                      <p className="text-sm text-slate-500">
                        Created {new Date(request.createdAt).toLocaleString()}
                      </p>
                    </div>
                    <span className="w-fit rounded bg-brand-50 px-2 py-1 text-xs font-semibold text-brand-700">
                      {request.status.replaceAll('_', ' ')}
                    </span>
                  </div>
                  {request.reason && <p className="text-sm text-slate-600">{request.reason}</p>}
                  <div className="grid gap-2 md:grid-cols-2 lg:grid-cols-3">
                    {request.steps.map((step) => (
                      <div key={step.id} className="rounded border border-slate-200 px-3 py-2">
                        <p className="text-sm font-medium text-slate-900">{step.officeName}</p>
                        <p className="text-xs font-semibold text-slate-500">{step.status}</p>
                        {step.comment && <p className="mt-1 text-xs text-slate-500">{step.comment}</p>}
                      </div>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </PageShell>
  );
}
