package com.pelvic.doctor.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 医生档案（登录身份存于 account，本表保存业务档案；编号对外展示为 D + 3 位数字） */
@Data
@TableName("doctor")
public class Doctor {

    @TableId(value = "医生ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    @TableField("编号")
    private Long doctorNo;

    @TableField("科室ID")
    private Long deptId;

    @TableField("姓名")
    private String name;

    @TableField("性别")
    private String gender;

    @TableField("职称")
    private String title;

    /** 待审核 / 审核通过 / 审核未通过 */
    @TableField("审核状态")
    private String auditStatus;

    @TableField("审核通过时间")
    private LocalDateTime auditTime;

    @TableField("审核人账号ID")
    private Long auditorAccountId;

    @TableField("通过或拒绝原因")
    private String auditReason;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
