import { roleActivities } from '../data/roleActivities';
import type { UserRole } from '../services/api';

type RoleActivityPanelProps = {
  role: UserRole;
};

export function RoleActivityPanel({ role }: RoleActivityPanelProps) {
  const info = roleActivities[role];

  return (
    <section className="grid gap-4 lg:grid-cols-3">
      <div className="rounded border border-slate-200 bg-white p-4 shadow-sm">
        <h2 className="text-base font-semibold text-slate-950">{info.title}</h2>
        <p className="mt-2 text-sm text-slate-600">{info.description}</p>
      </div>

      <ActivityList title="Allowed activities" items={info.allowedActivities} />
      <ActivityList title="Workflow steps" items={info.workflowSteps} ordered />

      <div className="rounded border border-amber-200 bg-amber-50 p-4 shadow-sm lg:col-span-3">
        <h3 className="text-sm font-semibold text-amber-900">Not allowed for this role</h3>
        <ul className="mt-3 grid gap-2 md:grid-cols-2">
          {info.restrictedActivities.map((item) => (
            <li key={item} className="text-sm text-amber-800">
              {item}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

function ActivityList({ title, items, ordered = false }: { title: string; items: string[]; ordered?: boolean }) {
  const ListTag = ordered ? 'ol' : 'ul';

  return (
    <div className="rounded border border-slate-200 bg-white p-4 shadow-sm">
      <h3 className="text-sm font-semibold text-slate-950">{title}</h3>
      <ListTag className={`mt-3 space-y-2 text-sm text-slate-600 ${ordered ? 'list-decimal pl-5' : ''}`}>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ListTag>
    </div>
  );
}
