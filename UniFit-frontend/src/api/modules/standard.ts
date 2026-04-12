import request from '../request';
import type { BaseResponse, PageData } from '../types';

export interface TestStandard {
  id: number;
  stage: string;
  gradeRange: string;
  gender: string;
  itemCode: string;
  minScore: number;
  maxScore: number;
  level: string;
  standardPoint: number;
}

export interface TestStandardQuery {
  current?: number;
  pageSize?: number;
  stage?: string;
  gender?: string;
  itemCode?: string;
}

export interface TestItem {
  id: number;
  itemCode: string;
  itemName: string;
}

export function listStandards(data: TestStandardQuery): Promise<BaseResponse<PageData<TestStandard>>> {
  return request.post('/admin/standard/list/page', data);
}

export function upsertStandard(data: Partial<TestStandard>): Promise<BaseResponse<TestStandard>> {
  return request.post('/admin/standard/upsert', data);
}

export function deleteStandard(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/admin/standard/delete', { id });
}

export function importCollegeFullStandard(): Promise<BaseResponse<{ inserted: number }>> {
  return request.post('/admin/standard/import/college-full');
}

export function listStandardTestItems(): Promise<BaseResponse<TestItem[]>> {
  return request.get('/admin/standard/test-items');
}
