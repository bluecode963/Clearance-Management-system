import { MouseEvent, useEffect, useMemo, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { RoleActivityPanel } from '../components/RoleActivityPanel';
import {
  getAssignedOfficeSteps,
  reviewOfficeStep,
  type OfficeStepReviewResponse,
} from '../services/api';

export function OfficeStaffDashboardPage() {
  const [steps, setSteps] = useState<OfficeStepReviewResponse[]>([]);
  const [status, setStatus] = useState('');
  const [comments, setComments] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(true);
  const [reviewingStepId, setReviewingStepId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadSteps(selectedStatus = status) {
    setLoading(true);
    setError('');
    try {
      const page = await getAssignedOfficeSteps(selectedStatus || undefined);
      setSteps(page.content);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load assigned steps');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadSteps(status);
  }, [status]);

  async function handleReview(
    event: MouseEvent<HTMLButtonElement>,
    stepId: number,
    decision: 'APPROVED' | 'REJECTED'
  ) {
    event.preventDefault();
    setMessage('');
    setError('');
    setReviewingStepId(stepId);

    try {
      const reviewed = await reviewOfficeStep(stepId, decision, comments[stepId] ?? '');
      setMessage(`Step #${reviewed.stepId} marked ${reviewed.status}.`);
      setComments((current) => ({ ...current, [stepId]: '' }));
      await loadSteps(status);
    } catch (reviewError) {
      setError(reviewError instanceof Error ? reviewError.message : 'Could not review step');
    } finally {
      setReviewingStepId(null);
    }
  }

  const metrics = useMemo(
    () => [
      { label: 'Assigned steps', value: String(steps.length) },
      { label: 'Waiting review', value: String(steps.filter((step) => step.status === 'PENDING').length) },
      { label: 'Rejected shown', value: String(steps.filter((step) => step.status === 'REJECTED').length) },
    ],
    [steps]
  );

  return (
    <PageShell title="Office Staff Dashboard" subtitle="Review only clearance steps assigned to the staff office.">
      <div className="space-y-6">
        <RoleActivityPanel role="OFFICE_STAFF" />

        <div className="grid gap-4 md:grid-cols-3">
          {metrics.map((metric) => (
            <div key={metric.label} className="rounded border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-sm text-slate-500">{metric.label}</p>
              <p className="mt-2 text-2xl font-bold text-slate-950">{metric.value}</p>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3 rounded border border-slate-200 bg-white p-4 shadow-sm md:flex-row md:items-end md:justify-between">
          <label className="block text-sm font-medium text-slate-700">
            Status filter
            <select
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm md:w-56"
              onChange={(event) => setStatus(event.target.value)}
              value={status}
            >
              <option value="">All</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="WAITING">Waiting</option>
            </select>
          </label>
          <button
            className="rounded border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            onClick={() => void loadSteps(status)}
            type="button"
          >
            Refresh
          </button>
        </div>

        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <section className="rounded border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-3">
            <h2 className="font-semibold text-slate-950">Assigned clearance steps</h2>
          </div>
          {loading ? (
            <p className="px-4 py-6 text-sm text-slate-600">Loading assigned steps...</p>
          ) : steps.length === 0 ? (
            <p className="px-4 py-6 text-sm text-slate-600">
              No assigned steps match this filter. If Finance requests are missing, ask an admin to confirm this
              account is assigned to the Finance office.
            </p>
          ) : (
            <div className="divide-y divide-slate-100">
              {steps.map((step) => (
                <article key={step.stepId} className="space-y-4 px-4 py-5">
                  <div className="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
                    <div>
                      <p className="font-semibold text-slate-950">
                        Request #{step.clearanceRequestId} - {formatEnum(step.requestType)}
                      </p>
                      <p className="text-sm text-slate-600">
                        {step.studentName} ({step.studentId})
                      </p>
                      <p className="text-sm text-slate-500">{step.officeName}</p>
                    </div>
                    <span className="w-fit rounded bg-brand-50 px-2 py-1 text-xs font-semibold text-brand-700">
                      {formatEnum(step.status)}
                    </span>
                  </div>

                  {step.comment && <p className="rounded bg-slate-50 px-3 py-2 text-sm text-slate-600">{step.comment}</p>}

                  {step.status === 'PENDING' ? (
                    <form className="space-y-3" onSubmit={(event) => event.preventDefault()}>
                      <textarea
                        className="min-h-20 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                        maxLength={500}
                        onChange={(event) => setComments((current) => ({ ...current, [step.stepId]: event.target.value }))}
                        placeholder="Comment for this decision"
                        value={comments[step.stepId] ?? ''}
                      />
                      <div className="flex flex-wrap gap-2">
                        <button
                          className="rounded bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                          disabled={reviewingStepId === step.stepId}
                          onClick={(event) => void handleReview(event, step.stepId, 'APPROVED')}
                          type="button"
                        >
                          Approve
                        </button>
                        <button
                          className="rounded bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                          disabled={reviewingStepId === step.stepId}
                          onClick={(event) => void handleReview(event, step.stepId, 'REJECTED')}
                          type="button"
                        >
                          Reject
                        </button>
                      </div>
                    </form>
                  ) : (
                    <p className="text-sm text-slate-500">
                      Reviewed {step.reviewedAt ? new Date(step.reviewedAt).toLocaleString() : 'recently'}
                      {step.reviewedBy ? ` by ${step.reviewedBy}` : ''}
                    </p>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </PageShell>
  );
}

function formatEnum(value: string) {
  return value.replace(/_/g, ' ');
}
