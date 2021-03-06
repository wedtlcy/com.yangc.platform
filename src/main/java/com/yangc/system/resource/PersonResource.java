package com.yangc.system.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yangc.bean.DataGridBean;
import com.yangc.bean.ResultBean;
import com.yangc.exception.WebApplicationException;
import com.yangc.shiro.utils.ShiroUtils;
import com.yangc.system.bean.Permission;
import com.yangc.system.bean.TSysPerson;
import com.yangc.system.bean.TSysUsersroles;
import com.yangc.system.service.PersonService;
import com.yangc.system.service.UserService;
import com.yangc.system.service.UsersrolesService;
import com.yangc.utils.Constants;

@Controller
@RequestMapping("/person")
public class PersonResource {

	private static final Logger logger = LogManager.getLogger(PersonResource.class);

	@Autowired
	private PersonService personService;
	@Autowired
	private UserService userService;
	@Autowired
	private UsersrolesService usersrolesService;

	/**
	 * @功能: 查询所有用户(自动完成)
	 * @作者: yangc
	 * @创建日期: 2013年12月23日 下午5:30:25
	 * @return
	 */
	@RequestMapping(value = "getPersonList", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.SEL)
	public List<TSysPerson> getPersonList(String condition) {
		if (StringUtils.isNotBlank(condition)) {
			try {
				condition = URLDecoder.decode(condition, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		logger.info("getPersonList - condition={}", condition);
		return this.personService.getPersonList(condition);
	}

	/**
	 * @功能: 查询所有用户(分页)
	 * @作者: yangc
	 * @创建日期: 2013年12月23日 下午5:30:25
	 * @return
	 */
	@RequestMapping(value = "getPersonListByNicknameAndDeptId_page", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.SEL)
	public DataGridBean getPersonListByNicknameAndDeptId_page(String nickname, Long deptId) {
		if (StringUtils.isNotBlank(nickname)) {
			try {
				nickname = URLDecoder.decode(nickname, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		logger.info("getPersonListByNicknameAndDeptId_page - nickname={}, deptId={}", nickname, deptId);
		List<TSysPerson> personList = this.personService.getPersonListByNicknameAndDeptId_page(nickname, deptId);
		return new DataGridBean(personList);
	}

	/**
	 * @功能: 根据userId获取roleIds
	 * @作者: yangc
	 * @创建日期: 2013年12月24日 上午10:49:47
	 * @return
	 */
	@RequestMapping(value = "getRoleIdsByUserId", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.SEL)
	public TSysPerson getRoleIdsByUserId(Long userId) {
		logger.info("getRoleIdsByUserId - userId={}", userId);
		TSysPerson person = new TSysPerson();
		List<TSysUsersroles> usersrolesList = this.usersrolesService.getUsersrolesListByUserId(userId);
		if (usersrolesList == null || usersrolesList.isEmpty()) {
			person.setRoleIds("");
		} else {
			StringBuilder sb = new StringBuilder();
			for (TSysUsersroles usersroles : usersrolesList) {
				sb.append(usersroles.getRoleId()).append(",");
			}
			person.setRoleIds(sb.substring(0, sb.length() - 1));
		}
		return person;
	}

	/**
	 * @功能: 添加用户
	 * @作者: yangc
	 * @创建日期: 2013年12月23日 下午5:49:16
	 * @return
	 */
	@RequestMapping(value = "addPerson", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.ADD)
	public ResultBean addPerson(String nickname, Long sex, String phone, Long deptId, String username, String roleIds) {
		logger.info("addPerson - nickname={}, sex={}, phone={}, deptId={}, username={}, roleIds={}", nickname, sex, phone, deptId, username, roleIds);
		ResultBean resultBean = new ResultBean();
		try {
			TSysPerson person = new TSysPerson();
			person.setNickname(nickname);
			person.setSex(sex);
			person.setPhone(phone);
			person.setDeptId(deptId);
			this.userService.addOrUpdateUser(null, username, Constants.DEFAULT_PASSWORD, person, null, null, null, roleIds);
			resultBean.setSuccess(true);
			resultBean.setMessage("添加成功");
			return resultBean;
		} catch (IllegalStateException e) {
			resultBean.setSuccess(false);
			resultBean.setMessage(e.getMessage());
			return resultBean;
		} catch (Exception e) {
			e.printStackTrace();
			return WebApplicationException.build();
		}
	}

	/**
	 * @功能: 修改用户
	 * @作者: yangc
	 * @创建日期: 2013年12月23日 下午5:49:16
	 * @return
	 */
	@RequestMapping(value = "updatePerson", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.UPD)
	public ResultBean updatePerson(Long id, String nickname, Long sex, String phone, Long deptId, Long userId, String username, String roleIds) {
		logger.info("updatePerson - id={}, nickname={}, sex={}, phone={}, deptId={}, userId={}, username={}, roleIds={}", id, nickname, sex, phone, deptId, userId, username, roleIds);
		ResultBean resultBean = new ResultBean();
		try {
			TSysPerson person = this.personService.getPersonById(id);
			person.setNickname(nickname);
			person.setSex(sex);
			person.setPhone(phone);
			person.setDeptId(deptId);
			this.userService.addOrUpdateUser(userId, username, Constants.DEFAULT_PASSWORD, person, null, null, null, roleIds);
			// 清除用户权限缓存信息
			ShiroUtils.clearCachedAuthorizationInfo(username);
			resultBean.setSuccess(true);
			resultBean.setMessage("修改成功");
			return resultBean;
		} catch (IllegalStateException e) {
			resultBean.setSuccess(false);
			resultBean.setMessage(e.getMessage());
			return resultBean;
		} catch (Exception e) {
			e.printStackTrace();
			return WebApplicationException.build();
		}
	}

	/**
	 * @功能: 删除用户
	 * @作者: yangc
	 * @创建日期: 2013年12月23日 下午7:00:20
	 * @return
	 */
	@RequestMapping(value = "delPerson", method = RequestMethod.POST)
	@ResponseBody
	@RequiresPermissions("person:" + Permission.DEL)
	public ResultBean delPerson(Long userId) {
		try {
			logger.info("delPerson - userId={}", userId);
			String username = this.userService.delUser(userId);
			// 清除用户权限缓存信息
			ShiroUtils.clearCachedAuthorizationInfo(username);
			return new ResultBean(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return WebApplicationException.build();
		}
	}

}
