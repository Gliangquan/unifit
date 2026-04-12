package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.ConsultMessageMapper;
import com.jcen.unifit.mapper.StudentProfileMapper;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.entity.ConsultMessage;
import com.jcen.unifit.model.entity.StudentProfile;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.ConsultMessageVO;
import com.jcen.unifit.service.ConsultMessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultMessageServiceImpl implements ConsultMessageService {

    @Resource
    private ConsultMessageMapper consultMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Override
    public ConsultMessageVO submit(User loginUser, String questionContent) {
        if (StringUtils.isBlank(questionContent)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "留言内容不能为空");
        }
        String trimmed = questionContent.trim();
        if (trimmed.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "留言内容不能超过500字");
        }

        ConsultMessage row = new ConsultMessage();
        row.setUserId(loginUser.getId());
        row.setQuestionContent(trimmed);
        row.setStatus("pending");
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        consultMessageMapper.insert(row);
        return toVO(row, loginUser, null, null);
    }

    @Override
    public List<ConsultMessageVO> listMy(User loginUser) {
        QueryWrapper<ConsultMessage> qw = new QueryWrapper<>();
        qw.eq("user_id", loginUser.getId()).orderByDesc("id");
        List<ConsultMessage> rows = consultMessageMapper.selectList(qw);

        StudentProfile profile = studentProfileMapper.selectOne(
                new QueryWrapper<StudentProfile>().eq("user_id", loginUser.getId()).last("limit 1"));
        List<ConsultMessageVO> result = new ArrayList<>();
        for (ConsultMessage row : rows) {
            User replyUser = row.getReplyBy() == null ? null : userMapper.selectById(row.getReplyBy());
            result.add(toVO(row, loginUser, profile, replyUser));
        }
        return result;
    }

    @Override
    public List<ConsultMessageVO> listPending() {
        QueryWrapper<ConsultMessage> qw = new QueryWrapper<>();
        qw.eq("status", "pending").orderByAsc("id");
        List<ConsultMessage> rows = consultMessageMapper.selectList(qw);
        return batchToVO(rows);
    }

    @Override
    public ConsultMessageVO reply(User admin, Long id, String answerContent) {
        if (id == null || StringUtils.isBlank(answerContent)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复参数不完整");
        }
        ConsultMessage row = consultMessageMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "留言不存在");
        }

        row.setAnswerContent(answerContent.trim());
        row.setStatus("replied");
        row.setReplyBy(admin.getId());
        row.setReplyTime(new Date());
        row.setUpdateTime(new Date());
        consultMessageMapper.updateById(row);

        return batchToVO(List.of(row)).get(0);
    }

    private List<ConsultMessageVO> batchToVO(List<ConsultMessage> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = rows.stream().map(ConsultMessage::getUserId).collect(Collectors.toSet());
        Set<Long> replyUserIds = rows.stream().map(ConsultMessage::getReplyBy).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Long> allUserIds = new java.util.HashSet<>(userIds);
        allUserIds.addAll(replyUserIds);
        Map<Long, User> userMap = allUserIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, StudentProfile> profileMap = userIds.isEmpty() ? Map.of() : studentProfileMapper.selectList(
                        new QueryWrapper<StudentProfile>().in("user_id", userIds))
                .stream().collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));

        List<ConsultMessageVO> result = new ArrayList<>();
        for (ConsultMessage row : rows) {
            User user = userMap.get(row.getUserId());
            StudentProfile profile = profileMap.get(row.getUserId());
            result.add(toVO(row, user, profile, userMap.get(row.getReplyBy())));
        }
        return result;
    }

    private ConsultMessageVO toVO(ConsultMessage row, User user, StudentProfile profile, User replyUser) {
        ConsultMessageVO vo = new ConsultMessageVO();
        vo.setId(row.getId());
        vo.setUserId(row.getUserId());
        vo.setUserName(user == null ? "" : user.getUserName());
        vo.setStudentId(profile == null ? "" : profile.getStudentId());
        vo.setQuestionContent(row.getQuestionContent());
        vo.setAnswerContent(row.getAnswerContent());
        vo.setStatus(row.getStatus());
        vo.setReplyBy(row.getReplyBy());
        vo.setReplyUserName(replyUser == null ? null : replyUser.getUserName());
        vo.setReplyTime(row.getReplyTime());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
