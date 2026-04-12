import request from '../request';
import type { BaseResponse, PageData } from '../types';

export interface LoginUser {
  id: number;
  userAccount: string;
  userName: string;
  userAvatar?: string;
  userRole: 'student' | 'admin' | 'ban';
  userPhone?: string;
  token?: string;
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

export interface UserQueryRequest {
  current?: number;
  pageSize?: number;
  userAccount?: string;
  userName?: string;
  userPhone?: string;
  userRole?: string;
}

export interface UserRegisterRequest {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
  userPhone?: string;
  userName: string;
}

export function userLoginByAccount(userAccount: string, userPassword: string): Promise<BaseResponse<LoginUser>> {
  return request.post('/user/login', {
    loginType: 'account',
    userAccount,
    userPassword,
  });
}

export function userRegister(data: UserRegisterRequest): Promise<BaseResponse<number>> {
  return request.post('/user/register', data);
}

export function getLoginUser(): Promise<BaseResponse<LoginUser>> {
  return request.get('/user/get/login');
}

export function userLogout(): Promise<BaseResponse<boolean>> {
  return request.post('/user/logout');
}

export function listUsers(data: UserQueryRequest): Promise<BaseResponse<PageData<User>>> {
  return request.post('/user/list/page', data);
}

export function getUserList(userRole?: string): Promise<BaseResponse<User[]>> {
  return request.get('/user/list', { params: { userRole } });
}

export function createTeacher(data: UserRegisterRequest): Promise<BaseResponse<number>> {
  return request.post('/user/teacher/create', data);
}
