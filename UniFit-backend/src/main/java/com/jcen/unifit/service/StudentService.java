package com.jcen.unifit.service;

import com.jcen.unifit.model.dto.StudentAuditRequest;
import com.jcen.unifit.model.dto.StudentVerifySubmitRequest;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ClassVO;

import java.util.List;

public interface StudentService {

    StudentProfile submitVerification(User loginUser, StudentVerifySubmitRequest request);

    StudentProfile getMyProfile(User loginUser);

    List<ClassVO> listAvailableClasses();

    List<StudentProfile> listPendingProfiles();

    List<StudentProfile> listAuditHistoryProfiles();

    boolean audit(StudentAuditRequest request, User admin);
}
