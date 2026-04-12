import request from '../request';
import type { BaseResponse } from '../types';

export interface StudentProfile {
  id: number;
  userId: number;
  studentId: string;
  realName: string;
  verificationStatus: 'pending' | 'approved' | 'rejected';
  rejectReason?: string;
  createTime?: string;
}

export interface StudentAuditRequest {
  userId: number;
  verificationStatus: 'approved' | 'rejected';
  rejectReason?: string;
}

export function listPendingStudents(): Promise<BaseResponse<StudentProfile[]>> {
  return request.get('/student/verify/pending');
}

export function auditStudent(data: StudentAuditRequest): Promise<BaseResponse<boolean>> {
  return request.post('/student/verify/audit', data);
}
