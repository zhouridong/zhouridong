package cn.sq.appinfo.service;

import java.util.List;

import cn.sq.appinfo.pojo.AppVersion;

public interface AppVersionService {
	
	public AppVersion getAppVersion(Integer id);
	
	public List<AppVersion> getAppVersionappId(Integer appId);
	
	public Integer InsertAppVersion(AppVersion appVersion);
}
