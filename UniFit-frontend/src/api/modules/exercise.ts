import request from '../request';
import type { BaseResponse, PageData } from '../types';

export interface Exercise {
  id: number;
  name: string;
  category?: string;
  difficulty?: string;
  equipmentRequired?: string;
  description?: string;
  coverImageUrl?: string;
  contentMd?: string;
  publishUserId?: number;
  publishUserName?: string;
  publishTime?: string;
  likeCount?: number;
  commentCount?: number;
  demoVideoUrl?: string;
  demoImageUrls?: string;
  status: number;
}

export interface ExerciseUpsertRequest {
  id?: number;
  name: string;
  category?: string;
  difficulty?: string;
  equipmentRequired?: string;
  description?: string;
  coverImageUrl?: string;
  contentMd?: string;
  demoVideoUrl?: string;
  demoImageUrls?: string;
  status?: number;
}

export interface ExerciseComment {
  id: number;
  exerciseId: number;
  userId: number;
  userName: string;
  userAvatar?: string;
  content: string;
  likeCount: number;
  createTime: string;
}

export interface ExerciseLikeStatus {
  liked: boolean;
  likeCount: number;
}

export function listExercises(params: {
  current?: number;
  pageSize?: number;
  keyword?: string;
  category?: string;
  difficulty?: string;
}): Promise<BaseResponse<PageData<Exercise>>> {
  return request.get('/exercise/list', { params });
}

export function upsertExercise(data: ExerciseUpsertRequest): Promise<BaseResponse<Exercise>> {
  return request.post('/exercise/upsert', data);
}

export function deleteExercise(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/exercise/delete', { id });
}

export function uploadExerciseMedia(file: File, bizType = 'exercise'): Promise<BaseResponse<string>> {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/file/upload', formData, {
    params: { bizType },
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function listExerciseComments(exerciseId: number): Promise<BaseResponse<ExerciseComment[]>> {
  return request.get('/exercise/comment/list', { params: { exerciseId } });
}

export function addExerciseComment(exerciseId: number, content: string): Promise<BaseResponse<ExerciseComment>> {
  return request.post('/exercise/comment/add', { exerciseId, content });
}

export function deleteExerciseComment(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/exercise/comment/delete', { id });
}

export function toggleExerciseLike(exerciseId: number): Promise<BaseResponse<ExerciseLikeStatus>> {
  return request.post('/exercise/like/toggle', { exerciseId });
}

export function getExerciseLikeStatus(exerciseId: number): Promise<BaseResponse<ExerciseLikeStatus>> {
  return request.get('/exercise/like/status', { params: { exerciseId } });
}
