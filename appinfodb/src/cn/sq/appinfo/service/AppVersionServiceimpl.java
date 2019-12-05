package cn.sq.appinfo.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.sun.accessibility.internal.resources.accessibility;

import cn.sq.appinfo.dao.AppVersionDao;
import cn.sq.appinfo.pojo.AppVersion;

@Service
public class AppVersionServiceimpl implements AppVersionService{

	@Resource
	private AppVersionDao avd;
	@Override
	public AppVersion getAppVersion(Integer id) {
		return avd.getAppVersion(id);
	}
	@Override
	public List<AppVersion> getAppVersionappId(Integer appId) {
		return avd.getAppVersionappId(appId);
	}
	@Override
	public Integer InsertAppVersion(AppVersion appVersion) {
		return avd.InsertAppVersion(appVersion);
	}
		
}
