package com.example.emos.wx.db.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * tb_role
 * @author 
 */
@Data
public class TbMeeting implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 权限集合
     */
    private Object permissions;

    private static final long serialVersionUID = 1L;
}