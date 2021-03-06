package com.yangc.system.resource;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yangc.bean.ResultBean;
import com.yangc.exception.WebApplicationException;
import com.yangc.shiro.utils.ShiroUtils;
import com.yangc.system.bean.AuthTree;
import com.yangc.system.bean.Permission;
import com.yangc.system.service.AclService;

@Controller
@RequestMapping("/acl")
public class AclResource {

	private static final Logger logger = LogManager.getLogger(AclResource.class);

	@Autowired
	private AclService aclService;

	/**
	 * @功能: 获取用户拥有的权限
	 * @作者: yangc
	 * @创建日期: 2014年6月13日 下午1:00:14
	 * @return
	 */
	@RequestMapping(value = "getUserPermission", method = RequestMethod.POST)
	@ResponseBody
	public Collection<String> getUserPermission() {
		logger.info("getUserPermission");
		return ShiroUtils.getUserPermission();
	}

	/**
	 * @功能: 某个角色所拥有的权限
	 * @作者: yangc
	 * @创建日期: 2013年12月24日 下午10:54:50
	 * @return
	 */
	@RequestMapping(value = "getAuthTreeList", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("role:" + Permission.SEL)
	public List<AuthTree> getAuthTreeList(Long roleId, Long parentMenuId) {
		logger.info("getAuthTreeList - roleId={}, parentMenuId={}", roleId, parentMenuId);
		return this.aclService.getAclListByRoleIdAndParentMenuId(roleId, parentMenuId);
	}

	/**
	 * @功能: 添加或修改权限
	 * @作者: yangc
	 * @创建日期: 2013年12月24日 下午11:14:16
	 * @return
	 */
	@RequestMapping(value = "addOrUpdateAcl", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("role:" + Permission.ADD)
	public ResultBean addOrUpdateAcl(Long roleId, Long menuId, int permission, int allow) {
		logger.info("addOrUpdateAcl - roleId={}, menuId={}, permission={}, allow={}", roleId, menuId, permission, allow);
		try {
			this.aclService.addOrUpdateAcl(roleId, menuId, permission, allow);
			// 清除所有权限缓存信息
			ShiroUtils.clearAllCachedAuthorizationInfo();
			return new ResultBean(true, "");
		} catch (Exception e) {
			e.printStackTrace();
			return WebApplicationException.build();
		}
	}

}
