import { MouseEvent, useEffect, useMemo, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { RoleActivityPanel } from '../components/RoleActivityPanel';
import {
  decideRegistrarClearance,
  downloadAttachment,
  getRegistrarClearanceRequests,
  type ClearanceRequestResponse,
  type ClearanceType,
} from '../services/api';

export function RegistrarDashboardPage() {
  const [requests, setRequests] = useState<ClearanceRequestResponse[]>([]);
  const [status, setStatus] = useState('READY_FOR_REGISTRAR');
  const [requestType, setRequestType] = useState<ClearanceType | ''>('');
  const [studentId, setStudentId] = useState('');
  const [keyword, setKeyword] = useState('');
  const [comments, setComments] = useState<Record<number, string>>({});
  const [attachments, setAttachments] = useState<Record<number, File | null>>({});
  const [uploadVersion, setUploadVersion] = useState(0);
  const [loading, setLoading] = useState(true);
  const [decidingRequestId, setDecidingRequestId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadRequests() {
    setLoading(true);
    setError('');
    try {
      const page = await getRegistrarClearanceRequests({
        status: status || undefined,
        requestType,
        studentId,
        keyword,
      });
      setRequests(page.content);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load registrar requests');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadRequests();
  }, []);

  async function handleDecision(
    event: MouseEvent<HTMLButtonElement>,
    requestId: number,
    decision: 'APPROVED' | 'REJECTED'
  ) {
    event.preventDefault();
    setMessage('');
    setError('');
    if (decision === 'APPROVED' && !attachments[requestId]) {
      setError('An attachment is required for registrar approval.');
      return;
    }
    setDecidingRequestId(requestId);

    try {
      const decided = await decideRegistrarClearance(
        requestId,
        decision,
        comments[requestId] ?? '',
        attachments[requestId]
      );
      setMessage(`Request #${decided.id} marked ${formatEnum(decided.status)}.`);
      setComments((current) => ({ ...current, [requestId]: '' }));
      setAttachments((current) => ({ ...current, [requestId]: null }));
      setUploadVersion((current) => current + 1);
      await loadRequests();
    } catch (decisionError) {
      setError(decisionError instanceof Error ? decisionError.message : 'Could not save registrar decision');
    } finally {
      setDecidingRequestId(null);
    }
  }

  const metrics = useMemo(
    () => [
      { label: 'Shown requests', value: String(requests.length) },
      { label: 'Ready', value: String(requests.filter((request) => request.status === 'READY_FOR_REGISTRAR').length) },
      { label: 'Finalized shown', value: String(requests.filter((request) => request.status === 'COMPLETED').length) },
    ],
    [requests]
  );

  return (
    <PageShell title="Registrar Dashboard" subtitle="Finalize requests only after all required offices approve.">
      <div className="space-y-6">
        <RoleActivityPanel role="REGISTRAR" />

        <div className="grid gap-4 md:grid-cols-3">
          {metrics.map((metric) => (
            <div key={metric.label} className="rounded border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-sm text-slate-500">{metric.label}</p>
              <p className="mt-2 text-2xl font-bold text-slate-950">{metric.value}</p>
            </div>
          ))}
        </div>

        <form
          className="grid gap-4 rounded border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4"
          onSubmit={(event) => {
            event.preventDefault();
            void loadRequests();
          }}
        >
          <label className="block text-sm font-medium text-slate-700">
            Status
            <select
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              onChange={(event) => setStatus(event.target.value)}
              value={status}
            >
              <option value="READY_FOR_REGISTRAR">Ready for registrar</option>
              <option value="COMPLETED">Completed</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Request type
            <select
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              onChange={(event) => setRequestType(event.target.value as ClearanceType | '')}
              value={requestType}
            >
              <option value="">All</option>
              <option value="GRADUATION">Graduation</option>
              <option value="WITHDRAWAL">Withdrawal</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Student ID
            <input
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              onChange={(event) => setStudentId(event.target.value)}
              placeholder="Search student ID"
              value={studentId}
            />
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Keyword
            <input
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
              onChange={(event) => setKeyword(event.target.value)}
              placeholder="Name or email"
              value={keyword}
            />
          </label>
          <button
            className="rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 md:col-span-4"
            type="submit"
          >
            Apply filters
          </button>
        </form>

        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <section className="rounded border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-3">
            <h2 className="font-semibold text-slate-950">Registrar clearance requests</h2>
          </div>
          {loading ? (
            <p className="px-4 py-6 text-sm text-slate-600">Loading registrar requests...</p>
          ) : requests.length === 0 ? (
            <p className="px-4 py-6 text-sm text-slate-600">No clearance requests match these filters.</p>
          ) : (
            <div className="divide-y divide-slate-100">
              {requests.map((request) => (
                <article key={request.id} className="space-y-4 px-4 py-5">
                  <div className="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
                    <div>
                      <p className="font-semibold text-slate-950">
                        Request #{request.id} - {formatEnum(request.requestType)}
                      </p>
                      <p className="text-sm text-slate-600">
                        {request.student.fullName} ({request.student.studentId})
                      </p>
                      <p className="text-sm text-slate-500">
                        {request.student.department} - {request.student.program}
                      </p>
                      <p className="text-sm text-slate-500">
                        Created {new Date(request.createdAt).toLocaleString()}
                      </p>
                    </div>
                    <span className="w-fit rounded bg-brand-50 px-2 py-1 text-xs font-semibold text-brand-700">
                      {formatEnum(request.status)}
                    </span>
                  </div>

                  {request.reason && <p className="text-sm text-slate-600">{request.reason}</p>}

                  <div className="grid gap-2 md:grid-cols-2 lg:grid-cols-3">
                    {request.steps.map((step) => (
                      <div key={step.id} className="rounded border border-slate-200 px-3 py-2">
                        <p className="text-sm font-medium text-slate-900">{step.officeName}</p>
                        <p className="text-xs font-semibold text-slate-500">{formatEnum(step.status)}</p>
                        {step.comment && <p className="mt-1 text-xs text-slate-500">{step.comment}</p>}
                        {(step.attachments ?? []).map((attachment) => (
                          <button
                            className="mt-1 block text-left text-xs font-medium text-brand-700 hover:underline"
                            key={attachment.id}
                            onClick={() => void downloadAttachment(attachment).catch((downloadError) => {
                              setError(downloadError instanceof Error ? downloadError.message : 'Could not download attachment');
                            })}
                            type="button"
                          >
                            {formatEnum(attachment.purpose)}: {attachment.fileName}
                          </button>
                        ))}
                      </div>
                    ))}
                  </div>

                  {request.status === 'READY_FOR_REGISTRAR' ? (
                    <form className="space-y-3" onSubmit={(event) => event.preventDefault()}>
                      <textarea
                        className="min-h-20 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                        maxLength={500}
                        onChange={(event) => setComments((current) => ({ ...current, [request.id]: event.target.value }))}
                        placeholder="Registrar comment. Required for rejection."
                        value={comments[request.id] ?? ''}
                      />
                      <label className="block text-sm font-medium text-slate-700">
                        Attachment (required for approval)
                        <input
                          accept=".pdf,.doc,.docx,.txt,image/jpeg,image/png,image/webp"
                          className="mt-1 block w-full text-sm text-slate-600"
                          key={`${request.id}-attachment-${uploadVersion}`}
                          onChange={(event) => setAttachments((current) => ({
                            ...current,
                            [request.id]: event.target.files?.[0] ?? null,
                          }))}
                          type="file"
                        />
                      </label>
                      <p className="text-xs text-slate-500">
                        Upload one document or picture for approval. The attachment is optional for rejection.
                      </p>
                      <div className="flex flex-wrap gap-2">
                        <button
                          className="rounded bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                          disabled={decidingRequestId === request.id}
                          onClick={(event) => void handleDecision(event, request.id, 'APPROVED')}
                          type="button"
                        >
                          Approve final clearance
                        </button>
                        <button
                          className="rounded bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                          disabled={decidingRequestId === request.id}
                          onClick={(event) => void handleDecision(event, request.id, 'REJECTED')}
                          type="button"
                        >
                          Reject final clearance
                        </button>
                      </div>
                    </form>
                  ) : (
                    <p className="text-sm text-slate-500">Final decision already recorded.</p>
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
