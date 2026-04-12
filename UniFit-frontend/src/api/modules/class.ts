import request from '../request';
import type { BaseResponse } from '../types';

export interface ClassVO {
  id?: number;
  className: string;
  classCode: string;
  grade?: string;
  major?: string;
  description?: string;
  studentCount?: number;
  teacherId?: number;
  teacherName?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
  members?: any[];
  teachers?: any[];
}

export function getClassList(): Promise<BaseResponse<ClassVO[]>> {
  return request.get('/admin/class/list');
}

export function getClassDetail(classId: number): Promise<BaseResponse<ClassVO>> {
  return request.get(`/admin/class/${classId}`);
}

export function createClass(data: ClassVO): Promise<BaseResponse<number>> {
  return request.post('/admin/class/create', data);
}

export function updateClass(data: ClassVO): Promise<BaseResponse<boolean>> {
  return request.put('/admin/class/update', data);
}

export function deleteClass(classId: number): Promise<BaseResponse<boolean>> {
  return request.delete(`/admin/class/${classId}`);
}

export function addClassMember(classId: number, userId: number): Promise<BaseResponse<boolean>> {
  return request.post(`/admin/class/${classId}/member/${userId}`);
}

export function removeClassMember(classId: number, userId: number): Promise<BaseResponse<boolean>> {
  return request.delete(`/admin/class/${classId}/member/${userId}`);
}

export function addClassTeacher(classId: number, teacherId: number, role: string = 'teacher'): Promise<BaseResponse<boolean>> {
  return request.post(`/admin/class/${classId}/teacher/${teacherId}`, null, { params: { role } });
}

export function removeClassTeacher(classId: number, teacherId: number): Promise<BaseResponse<boolean>> {
  return request.delete(`/admin/class/${classId}/teacher/${teacherId}`);
}

export function getClassMembers(classId: number): Promise<BaseResponse<any[]>> {
  return request.get(`/admin/class/${classId}/members`);
}

export function getClassTeachers(classId: number): Promise<BaseResponse<any[]>> {
  return request.get(`/admin/class/${classId}/teachers`);
}
