import request from '../request';
import type { BaseResponse } from '../types';

export interface DashboardData {
  studentCount: number;
  checkinLast7Days: number;
  pendingStudentAudit: number;
  pendingMessages: number;
  activeClasses: number;
}

export interface CheckinRank {
  userId: number;
  userName: string;
  userAvatar?: string;
  checkinCount: number;
  totalDuration: number;
}

export interface User {
  id: number;
  userAccount: string;
  userName: string;
  userRole: 'student' | 'admin' | 'ban';
  userPhone?: string;
  userEmail?: string;
  status: number;
}

export function getDashboard(): Promise<BaseResponse<DashboardData>> {
  return request.get('/admin/dashboard');
}

export function getDashboardDetail(): Promise<BaseResponse<any>> {
  return request.get('/admin/dashboard/detail');
}

export function getCheckinTrend(): Promise<BaseResponse<any[]>> {
  return request.get('/admin/dashboard/checkin-trend');
}

export function getClassRanking(): Promise<BaseResponse<any[]>> {
  return request.get('/admin/dashboard/class-ranking');
}

export function getTestDistribution(): Promise<BaseResponse<any[]>> {
  return request.get('/admin/dashboard/test-distribution');
}

export function getActivityStats(): Promise<BaseResponse<any>> {
  return request.get('/admin/dashboard/activity-stats');
}

export function getAnalysisOverview(): Promise<BaseResponse<any>> {
  return request.get('/admin/analysis/overview');
}

export function getClassComparisonData(): Promise<BaseResponse<any[]>> {
  return request.get('/admin/analysis/class-comparison');
}

export function updateUserStatus(userId: number, status: number): Promise<BaseResponse<boolean>> {
  return request.post('/admin/user/status', null, { params: { userId, status } });
}

export function getUserDetail(userId: number): Promise<BaseResponse<User>> {
  return request.get(`/admin/user/${userId}`);
}

export function updateUser(userId: number, userName?: string, userPhone?: string, userRole?: string): Promise<BaseResponse<boolean>> {
  const params: any = {};
  if (userName) params.userName = userName;
  if (userPhone) params.userPhone = userPhone;
  if (userRole) params.userRole = userRole;
  return request.put(`/admin/user/${userId}`, null, { params });
}

export function deleteUser(userId: number): Promise<BaseResponse<boolean>> {
  return request.delete(`/admin/user/${userId}`);
}

export function getCheckinRanking(days: number = 7, topN: number = 20): Promise<BaseResponse<CheckinRank[]>> {
  return request.get('/checkin/ranking', { params: { days, topN } });
}

export function exportUserScoresCsv(): string {
  return '/api/admin/export/users-scores';
}

export function exportClassChallengeCsv(days: number = 30): string {
  return `/api/admin/export/class-challenge?days=${days}`;
}
