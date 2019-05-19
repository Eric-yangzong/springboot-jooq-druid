package bdhb.usershiro.service;

import com.bdhanbang.base.service.BaseService;
import com.generator.tables.SysUser;
import com.generator.tables.pojos.SysUserEntity;

public interface SysUserService extends BaseService<SysUser, SysUserEntity> {

	SysUserEntity getUserInfo(String username);

	SysUserEntity getJwtTokenInfo(String username);

	String generateJwtToken(String userName);

	void deleteLoginInfo(String userName);

}
