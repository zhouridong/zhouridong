package cn.sq.appinfo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.sq.appinfo.pojo.AppVersion;

public interface AppVersionDao {
	
	public AppVersion getAppVersion(@Param("id")Integer id);
	
	public List<AppVersion> getAppVersionappId(@Param("appId") Integer appId);
	
	public Integer InsertAppVersion(AppVersion appVersion);
}
