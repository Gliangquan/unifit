import request from '../request';
import type { BaseResponse, PageData } from '../types';

export interface PlanTemplate {
  id: number;
  templateCode: string;
  templateName: string;
  testItemCode: string;
  scoreLevel: string;
  fitnessLevel: string;
  equipmentType: string;
  bmiRange?: string;
  daysPerWeek: number;
  description?: string;
  status: number;
}

export interface PlanTemplateItem {
  id: number;
  templateId: number;
  weekNo: number;
  dayNo: number;
  exerciseId: number;
  setsCount?: number;
  repsCount?: number;
  durationMinutes?: number;
  intensityNote?: string;
  sortNo?: number;
}

export interface TestItem {
  id: number;
  itemCode: string;
  itemName: string;
}

export interface ExerciseLite {
  id: number;
  name: string;
  category?: string;
  difficulty?: string;
}

export function listTemplates(params: {
  current?: number;
  pageSize?: number;
  keyword?: string;
  testItemCode?: string;
  scoreLevel?: string;
  fitnessLevel?: string;
  equipmentType?: string;
  bmiRange?: string;
  daysPerWeek?: number;
  status?: number;
}): Promise<BaseResponse<PageData<PlanTemplate>>> {
  return request.get('/admin/template/list', { params });
}

export function upsertTemplate(data: Partial<PlanTemplate>): Promise<BaseResponse<PlanTemplate>> {
  return request.post('/admin/template/upsert', data);
}

export function deleteTemplate(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/admin/template/delete', { id });
}

export function listTemplateItems(templateId: number): Promise<BaseResponse<PlanTemplateItem[]>> {
  return request.get('/admin/template/items', { params: { templateId } });
}

export function upsertTemplateItem(data: Partial<PlanTemplateItem>): Promise<BaseResponse<PlanTemplateItem>> {
  return request.post('/admin/template/item/upsert', data);
}

export function deleteTemplateItem(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/admin/template/item/delete', { id });
}

export function listTemplateTestItems(): Promise<BaseResponse<TestItem[]>> {
  return request.get('/admin/template/test-items');
}

export function listTemplateExercises(): Promise<BaseResponse<ExerciseLite[]>> {
  return request.get('/admin/template/exercises');
}
