package com.pelvic.doctor.service;

import com.pelvic.auth.entity.Account;
import com.pelvic.auth.mapper.AccountMapper;
import com.pelvic.auth.mapper.AuthSessionMapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.core.util.HmacSigner;
import com.pelvic.common.security.AuthContext;
import com.pelvic.doctor.dto.DoctorCreateRequest;
import com.pelvic.doctor.dto.DoctorDetailResponse;
import com.pelvic.doctor.entity.Doctor;
import com.pelvic.doctor.mapper.DoctorDetailRow;
import com.pelvic.doctor.mapper.DoctorMapper;
import com.pelvic.system.entity.Department;
import com.pelvic.system.entity.SysOperateLog;
import com.pelvic.system.entity.SysUserRole;
import com.pelvic.system.mapper.DepartmentMapper;
import com.pelvic.system.mapper.SysOperateLogMapper;
import com.pelvic.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 医生管理（后端实现逻辑.md 第 7.5、12.5、13.5 节）：
 * 添加医生：建账号 → 建医生档案 → 授权医生角色 → 写操作审计；
 * 医生详情：编号解析 → doctor/department/account 联表查询 → 手机号解密脱敏；
 * 移除医生：档案置“审核未通过” → 停用账号 → 撤销全部会话 → 写操作审计。
 */
@Slf4j
@Service
public class DoctorService {

    /** 种子数据中的医生角色ID */
    private static final long DOCTOR_ROLE_ID = 2L;
    private static final String MODULE = "module-doctor";
    private static final String RESOURCE_TYPE = "doctor";
    private static final String CREATE_URI = "/api/admin/v1/doctor";

    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysOperateLogMapper sysOperateLogMapper;
    @Autowired
    private AuthSessionMapper authSessionMapper;
    @Autowired
    private AesEncryptor aesEncryptor;
    @Autowired
    private HmacSigner hmacSigner;

    /** 添加医生（仅管理员）：账号+档案+角色授权+审计，返回新医生信息 */
    @Transactional(rollbackFor = Exception.class)
    public DoctorDetailResponse create(DoctorCreateRequest req) {
        AuthContext ctx = requireAdmin("仅管理员可添加医生");
        Department department = departmentMapper.selectById(req.getDeptId());
        if (department == null) {
            throw new BizException("科室不存在");
        }
        String phoneHmac = hmacSigner.sign(req.getPhone());
        if (accountMapper.selectByPhoneHmac(phoneHmac) != null) {
            throw new BizException("该手机号已被注册");
        }

        // 1. 创建登录账号（登录名=手机号，明文不落库）
        Account account = new Account();
        account.setAccountNo("ACC" + System.currentTimeMillis());
        account.setNickname(req.getName());
        account.setStatus("NORMAL");
        account.setRegisterSource("ADMIN");
        account.setPhoneCipher(aesEncryptor.encrypt(req.getPhone()));
        account.setPhoneHmac(phoneHmac);
        accountMapper.insert(account);

        // 2. 创建医生档案（管理员添加即视为审核通过）
        LocalDateTime now = LocalDateTime.now();
        Doctor doctor = new Doctor();
        doctor.setAccountId(account.getId());
        doctor.setDoctorNo(doctorMapper.selectNextNo());
        doctor.setDeptId(req.getDeptId());
        doctor.setName(req.getName());
        doctor.setGender(req.getGender());
        doctor.setAuditStatus("审核通过");
        doctor.setAuditTime(now);
        doctor.setAuditorAccountId(ctx.getAccountId());
        doctorMapper.insert(doctor);

        // 3. 授权医生角色（角色ID=2 见种子数据）
        SysUserRole userRole = new SysUserRole();
        userRole.setAccountId(account.getId());
        userRole.setRoleId(DOCTOR_ROLE_ID);
        userRole.setGrantorAccountId(ctx.getAccountId());
        userRole.setGrantTime(now);
        sysUserRoleMapper.insert(userRole);

        // 4. 操作审计
        writeOperateLog(ctx.getAccountId(), "CREATE", doctor.getId().toString(), "POST", CREATE_URI);

        DoctorDetailResponse resp = new DoctorDetailResponse();
        resp.setId(formatDoctorNo(doctor.getDoctorNo()));
        resp.setName(doctor.getName());
        resp.setGender(doctor.getGender());
        resp.setTitle(doctor.getTitle());
        resp.setDeptId(doctor.getDeptId());
        resp.setDeptName(department.getName());
        resp.setPhone(maskPhone(req.getPhone()));
        resp.setAuditStatus(doctor.getAuditStatus());
        resp.setAuditReason(doctor.getAuditReason());
        return resp;
    }

    /** 医生详情（路径参数为医生编号，如 D001） */
    @Transactional(readOnly = true)
    public DoctorDetailResponse detail(String doctorId) {
        long doctorNo = parseDoctorNo(doctorId);
        DoctorDetailRow row = doctorMapper.selectDetailRow(doctorNo);
        if (row == null) {
            throw new BizException("医生不存在");
        }
        DoctorDetailResponse resp = new DoctorDetailResponse();
        resp.setId(formatDoctorNo(doctorNo));
        resp.setName(row.getName());
        resp.setGender(row.getGender());
        resp.setTitle(row.getTitle());
        resp.setDeptId(row.getDeptId());
        resp.setDeptName(row.getDeptName());
        resp.setPhone(maskCipherPhone(row.getPhoneCipher()));
        resp.setAuditStatus(row.getAuditStatus());
        resp.setAuditReason(row.getAuditReason());
        return resp;
    }

    /** 移除医生（仅管理员）：档案置审核未通过、停用账号并撤销全部会话 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(String doctorId) {
        AuthContext ctx = requireAdmin("仅管理员可移除医生");
        long doctorNo = parseDoctorNo(doctorId);
        Doctor doctor = doctorMapper.selectByNo(doctorNo);
        if (doctor == null) {
            throw new BizException("医生不存在");
        }
        if (doctor.getAccountId().equals(ctx.getAccountId())) {
            throw new BizException("不能移除当前登录账号");
        }

        // 逻辑移除：仅更新审核状态与原因（其余字段为 null，MP 默认不更新）
        Doctor update = new Doctor();
        update.setId(doctor.getId());
        update.setAuditStatus("审核未通过");
        update.setAuditReason("管理员移除");
        doctorMapper.updateById(update);

        // 停用账号并撤销该医生全部登录会话
        doctorMapper.disableAccount(doctor.getAccountId());
        authSessionMapper.revokeByAccountId(doctor.getAccountId());

        writeOperateLog(ctx.getAccountId(), "DELETE", doctorId, "DELETE", CREATE_URI + "/" + doctorId);
    }

    private AuthContext requireAdmin(String message) {
        AuthContext ctx = AuthContext.require();
        if (!ctx.isAdmin()) {
            throw new BizException(message);
        }
        return ctx;
    }

    /** 解析 “D001” 形式的医生编号为数据库编号 */
    private long parseDoctorNo(String doctorId) {
        if (doctorId == null || doctorId.length() < 2 || !doctorId.startsWith("D")) {
            throw new BizException("医生编号格式错误");
        }
        try {
            return Long.parseLong(doctorId.substring(1));
        } catch (NumberFormatException e) {
            throw new BizException("医生编号格式错误");
        }
    }

    private String formatDoctorNo(Long doctorNo) {
        return "D" + String.format("%03d", doctorNo);
    }

    /** 手机号脱敏：前3 + **** + 后4 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskCipherPhone(String cipher) {
        try {
            return maskPhone(aesEncryptor.decrypt(cipher));
        } catch (Exception e) {
            log.warn("手机号解密失败（请检查 pelvic.encrypt.aes-key 与存量数据密钥一致性）");
            return "****";
        }
    }

    private void writeOperateLog(Long accountId, String action, String resourceId, String method, String uri) {
        SysOperateLog operateLog = new SysOperateLog();
        operateLog.setAccountId(accountId);
        operateLog.setModule(MODULE);
        operateLog.setResourceType(RESOURCE_TYPE);
        operateLog.setResourceId(resourceId);
        operateLog.setAction(action);
        operateLog.setResult("SUCCESS");
        operateLog.setMethod(method);
        operateLog.setUri(uri);
        sysOperateLogMapper.insert(operateLog);
    }
}
