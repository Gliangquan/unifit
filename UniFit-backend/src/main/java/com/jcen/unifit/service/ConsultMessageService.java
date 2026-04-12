package com.jcen.unifit.service;

import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ConsultMessageVO;

import java.util.List;

public interface ConsultMessageService {

    ConsultMessageVO submit(User loginUser, String questionContent);

    List<ConsultMessageVO> listMy(User loginUser);

    List<ConsultMessageVO> listPending();

    ConsultMessageVO reply(User admin, Long id, String answerContent);
}
