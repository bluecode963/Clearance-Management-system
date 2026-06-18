import type { UserRole } from '../services/api';

export type RoleActivityInfo = {
  role: UserRole;
  title: string;
  description: string;
  allowedActivities: string[];
  restrictedActivities: string[];
  workflowSteps: string[];
};

export const roleActivities: Record<UserRole, RoleActivityInfo> = {
  ADMIN: {
    role: 'ADMIN',
    title: 'Admin activities',
    description: 'Admins manage users and explain the system workflow without performing student, office, or registrar actions.',
    allowedActivities: [
      'Create student, office staff, registrar, and admin accounts',
      'Assign office staff users to offices',
      'View role activity rules and workflow guidance',
      'View basic user and clearance request totals',
    ],
    restrictedActivities: [
      'Cannot create student clearance requests',
      'Cannot approve or reject office clearance steps',
      'Cannot make registrar final clearance decisions',
    ],
    workflowSteps: [
      'Create required user accounts',
      'Assign office staff to the correct office',
      'Share temporary login credentials with users',
      'Monitor basic system totals for the demo',
    ],
  },
  STUDENT: {
    role: 'STUDENT',
    title: 'Student activities',
    description: 'Students create and track their own clearance requests and cannot access staff or admin work areas.',
    allowedActivities: [
      'Create one active clearance request',
      'Select graduation, withdrawal, or transfer request type',
      'View own clearance requests and office step progress',
      'View comments, rejection reasons, and final status',
      'Request re-review only for steps that need correction',
    ],
    restrictedActivities: [
      'Cannot create users',
      'Cannot review office steps',
      'Cannot make registrar final decisions',
      'Cannot view other students requests',
    ],
    workflowSteps: [
      'Login as student',
      'Create a clearance request',
      'Wait while offices review assigned steps',
      'Track request status until registrar final decision',
    ],
  },
  OFFICE_STAFF: {
    role: 'OFFICE_STAFF',
    title: 'Office staff activities',
    description: 'Office staff review only the clearance steps assigned to their own office.',
    allowedActivities: [
      'View assigned office clearance steps',
      'Filter assigned steps by status',
      'Approve assigned pending steps',
      'Review resubmitted steps for their own office',
      'Reject assigned pending or resubmitted steps with a required comment',
    ],
    restrictedActivities: [
      'Cannot review steps from other offices',
      'Cannot review already approved or rejected steps',
      'Cannot review needs-correction steps until the student resubmits',
      'Cannot create users or student requests',
      'Cannot make registrar final decisions',
    ],
    workflowSteps: [
      'Login as office staff',
      'Filter assigned steps by review status',
      'Open pending step details',
      'Approve or reject with a clear comment',
    ],
  },
  REGISTRAR: {
    role: 'REGISTRAR',
    title: 'Registrar activities',
    description: 'Registrars make final clearance decisions only after required office steps are approved.',
    allowedActivities: [
      'View requests ready for registrar review',
      'Filter requests by status, type, student ID, and keyword',
      'View all office steps and comments',
      'Final approve or reject ready requests',
    ],
    restrictedActivities: [
      'Cannot approve requests still in office review',
      'Cannot decide already completed or rejected requests',
      'Cannot create users',
      'Cannot perform office staff step review',
    ],
    workflowSteps: [
      'Login as registrar',
      'Filter requests ready for final review',
      'Review office step outcomes and comments',
      'Approve final clearance or reject with a comment',
    ],
  },
};
