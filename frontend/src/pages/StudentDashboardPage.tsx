import { FormEvent, useEffect, useMemo, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { RoleActivityPanel } from '../components/RoleActivityPanel';
import {
  createClearanceRequest,
  downloadAttachment,
  getMyClearanceRequests,
  resubmitClearanceStep,
  type ClearanceRequestResponse,
  type ClearanceType,
  type AttachmentResponse,
} from '../services/api';

export function StudentDashboardPage() {
  const [requestType, setRequestType] = useState<ClearanceType>('GRADUATION');
  const [reason, setReason] = useState('');
  const [requests, setRequests] = useState<ClearanceRequestResponse[]>([]);
  const [correctionNotes, setCorrectionNotes] = useState<Record<number, string>>({});
  const [correctionAttachments, setCorrectionAttachments] = useState<Record<number, File | null>>({});
  const [uploadVersion, setUploadVersion] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [resubmittingStepId, setResubmittingStepId] = useState<number | null>(null);
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
      setError(formatStudentRequestError(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleResubmit(stepId: number) {
    setMessage('');
    setError('');
    setResubmittingStepId(stepId);

    try {
      await resubmitClearanceStep(
        stepId,
        correctionNotes[stepId] ?? '',
        correctionAttachments[stepId]
      );
      setMessage('Correction submitted. Waiting for office re-review.');
      setCorrectionNotes((current) => ({ ...current, [stepId]: '' }));
      setCorrectionAttachments((current) => ({ ...current, [stepId]: null }));
      setUploadVersion((current) => current + 1);
      await loadRequests();
    } catch (resubmitError) {
      setError(resubmitError instanceof Error ? resubmitError.message : 'Could not request re-review');
    } finally {
      setResubmittingStepId(null);
    }
  }

  const metrics = useMemo(() => {
    const latest = requests[0];
    const approvedSteps = latest?.steps.filter((step) => step.status === 'APPROVED').length ?? 0;
    const totalSteps = latest?.steps.length ?? 0;
    return [
      { label: 'My requests', value: String(requests.length) },
      { label: 'Latest progress', value: totalSteps ? `${approvedSteps} / ${totalSteps}` : '0 / 0' },
      { label: 'Latest status', value: latest ? formatEnum(latest.status) : 'None' },
    ];
  }, [requests]);

  return (
    <PageShell title="Student Dashboard" subtitle="Track one clearance request and each required office step.">
      <div className="space-y-6">
        <RoleActivityPanel role="STUDENT" />

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
                        #{request.id} {formatEnum(request.requestType)}
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
                        {(step.attachments ?? []).length > 0 && (
                          <div className="mt-2 space-y-1">
                            {(step.attachments ?? []).map((attachment) => (
                              <button
                                className="block text-left text-xs font-medium text-brand-700 hover:underline"
                                key={attachment.id}
                                onClick={() => void downloadAttachment(attachment).catch((downloadError) => {
                                  setError(downloadError instanceof Error ? downloadError.message : 'Could not download attachment');
                                })}
                                type="button"
                              >
                                {formatAttachmentLabel(attachment)}
                              </button>
                            ))}
                          </div>
                        )}
                        {canResubmitStep(step, request.status) && (
                          <div className="mt-3 space-y-2">
                            <p className="rounded bg-amber-50 px-2 py-1 text-xs text-amber-700">
                              This office needs your correction. Submit a note to send it back to the same office for re-review.
                            </p>
                            <textarea
                              className="min-h-20 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                              maxLength={500}
                              onChange={(event) => setCorrectionNotes((current) => ({ ...current, [step.id]: event.target.value }))}
                              placeholder={`Correction note for ${step.officeName}`}
                              value={correctionNotes[step.id] ?? ''}
                            />
                            <label className="block text-xs font-medium text-slate-700">
                              Attachment (optional document or picture)
                              <input
                                accept=".pdf,.doc,.docx,.txt,image/jpeg,image/png,image/webp"
                                className="mt-1 block w-full text-xs text-slate-600"
                                key={`${step.id}-attachment-${uploadVersion}`}
                                onChange={(event) => setCorrectionAttachments((current) => ({
                                  ...current,
                                  [step.id]: event.target.files?.[0] ?? null,
                                }))}
                                type="file"
                              />
                            </label>
                            <button
                              className="rounded bg-brand-600 px-3 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                              disabled={resubmittingStepId === step.id}
                              onClick={() => void handleResubmit(step.id)}
                              type="button"
                            >
                              {resubmittingStepId === step.id ? 'Submitting...' : 'Resubmit to office'}
                            </button>
                          </div>
                        )}
                        {step.status === 'RESUBMITTED' && (
                          <p className="mt-2 rounded bg-blue-50 px-2 py-1 text-xs text-blue-700">
                            Correction submitted. Waiting for office re-review.
                          </p>
                        )}
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

function formatEnum(value: string) {
  return value.replace(/_/g, ' ');
}

function formatAttachmentLabel(attachment: AttachmentResponse) {
  const uploader = attachment.uploadedBy
    ? ` from ${attachment.uploadedBy}${attachment.uploadedByRole ? ` (${formatEnum(attachment.uploadedByRole)})` : ''}`
    : '';
  const size = attachment.size ? ` - ${formatFileSize(attachment.size)}` : '';
  return `${formatEnum(attachment.purpose)}: ${attachment.fileName}${uploader}${size}`;
}

function formatFileSize(size: number) {
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${Math.round(size / 1024)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function canResubmitStep(step: { officeName: string; status: string }, requestStatus: string) {
  if (step.officeName.toLowerCase() === 'registrar') {
    return false;
  }
  if (requestStatus === 'COMPLETED' || requestStatus === 'CANCELLED') {
    return false;
  }
  return step.status === 'NEEDS_CORRECTION' || step.status === 'REJECTED';
}

function formatStudentRequestError(error: unknown) {
  const message = error instanceof Error ? error.message : 'Could not create request';
  if (message.toLowerCase().includes('student profile')) {
    return `${message}. Ask an admin to recreate this account with student ID, department, program, and year of study.`;
  }
  return message;
}
