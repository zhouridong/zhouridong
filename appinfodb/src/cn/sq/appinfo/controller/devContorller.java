package cn.sq.appinfo.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import cn.sq.appinfo.pojo.AppCategory;
import cn.sq.appinfo.pojo.AppInfo;
import cn.sq.appinfo.pojo.AppVersion;
import cn.sq.appinfo.pojo.DataDictionary;
import cn.sq.appinfo.pojo.DevUser;
import cn.sq.appinfo.service.AppCategoryService;
import cn.sq.appinfo.service.AppVersionService;
import cn.sq.appinfo.service.AppinfoService;
import cn.sq.appinfo.service.DataDictionaryService;
import cn.sq.appinfo.service.DevUserService;
import cn.sq.appinfo.tools.Constants;
import cn.sq.appinfo.tools.DIUU;
import cn.sq.appinfo.tools.Page;

@Controller
@RequestMapping("/dev")
public class devContorller {

	@Resource
	private DevUserService devs;
	@Resource
	private AppinfoService apps;
	@Resource
	private DataDictionaryService ds;
	@Resource
	private AppCategoryService as;
	@Resource
	private AppVersionService avs;

	@RequestMapping("/login")
	public String login() {
		return "devlogin";
	}

	@RequestMapping("/dologin")
	public String dologin(String devCode, String devPassword, HttpSession session, Model model) {
		DevUser dsevUser = devs.login(devCode, devPassword);
		if (dsevUser != null) {
			session.setAttribute(Constants.DEV_USER_SESSION, dsevUser);
			return "/developer/main";
		} else {
			model.addAttribute("error", "登入失败,请从新输入！");
			return "devlogin";
		}
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute(Constants.DEV_USER_SESSION);
		return "devlogin";
	}



	@RequestMapping("/list")
	public String list(HttpSession session, Model model, String querySoftwareName, Integer queryStatus,
			Integer queryCategoryLevel1, Integer queryCategoryLevel2, Integer queryCategoryLevel3,
			Integer queryFlatformId,Integer pageNo) {
		Integer devId = ((DevUser) session.getAttribute(Constants.DEV_USER_SESSION)).getId();
		// 按需求查询数据数量
		int totalCount = apps.getAppInfoCount(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2,
				queryCategoryLevel3, queryFlatformId, devId);
		Page page = new Page();
		page.setTotalCount(totalCount);
		if(pageNo!=null) {
			page.setPageInex(pageNo);
		}
		// 按要求查询信息
		List<AppInfo> appInfoList = apps.getAppInfoList(querySoftwareName, queryStatus, queryCategoryLevel1,
				queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId, page.getCurrentpage(),
				page.getPageSize());
		page.setList(appInfoList);
		// 按要求查询typecode
		List<DataDictionary> statusList = ds.DataDictionaryTypeCode("APP_STATUS");
		List<DataDictionary> flatFormList = ds.DataDictionaryTypeCode("APP_FLATFORM");
		// 一级分类，二级分类，三级分类
		List<AppCategory> categoryLevel1List = as.getAppCategories(null);
		
		model.addAttribute("appInfoList", appInfoList);
		model.addAttribute("statusList", statusList);
		model.addAttribute("flatFormList", flatFormList);
		model.addAttribute("categoryLevel1List", categoryLevel1List);
		model.addAttribute("pages", page);
		model.addAttribute("queryStatus", queryStatus);
		model.addAttribute("querySoftwareName", querySoftwareName);
		model.addAttribute("queryCategoryLevel1", queryCategoryLevel1);
		model.addAttribute("queryCategoryLevel2", queryCategoryLevel2);
		model.addAttribute("queryCategoryLevel3", queryCategoryLevel3);
		model.addAttribute("queryFlatformId", queryFlatformId);
		return "developer/appinfolist";
	}

	/* 按照id查询对应分类 */
	@RequestMapping("categorylevellist")
	@ResponseBody
	public Object getAppCategoryList(Integer pid) {
		return as.getAppCategories(pid);
	}

	/* 根据typeCode查询出相应的数据字典列表 */
	@RequestMapping("datadictionarylist")
	@ResponseBody
	public List<DataDictionary> getDataDicList(@RequestParam String tcode) {
		return ds.DataDictionaryTypeCode(tcode);
	}
	
	//判断Apk是否重复
    @RequestMapping("apkexist")
    @ResponseBody
    public Object apkexist(String APKName) {
    	System.out.println(APKName);
    	if(APKName==null || APKName=="") {
    		return "empty";
    	}
    	AppInfo appInfo = apps.getAppinfoIdandAPKName(null, APKName);
    	if(appInfo != null) {
    		return "exist";
    	}else {
    		return "noexist";
    	}
    }
    
    @RequestMapping("appinfoadd")
	public String add(Model model) {
		List<AppCategory> categoryLevel1List = as.getAppCategories(null);
		model.addAttribute("categoryLevel1List", categoryLevel1List);
		return "developer/appinfoadd";
	}
    
	@RequestMapping("appinfoaddsave")
	public String appAdd(AppInfo appInfo, HttpSession session, HttpServletRequest request,MultipartFile a_logoPicPath) throws IllegalStateException, IOException {
		// 获得上传文件后缀
		String extension = FilenameUtils.getExtension(a_logoPicPath.getOriginalFilename());
		// 拼接新的文件名
		String newFileName = DIUU.getUUID() + "." + extension;
		// 获得网站运行的根目录
		String rootPath = request.getServletContext().getRealPath("/");
		
		String contextPath = request.getContextPath();
		// 相对路径，用于保存到数据库，以后从数据库读取出来，用图片显示在页面上
		String relativePath =contextPath+ "statics/uploadfiles/" + newFileName;
		// 完整路径
		String fullPath = rootPath +"statics\\uploadfiles\\";
		// 保存文件
		a_logoPicPath.transferTo(new File(fullPath));
		appInfo.setDevId(((DevUser) session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appInfo.setModifyBy(((DevUser)session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appInfo.setCreationDate(new Date());
		appInfo.setLogoPicPath(newFileName);
		appInfo.setLogoLocPath(fullPath);
		appInfo.setStatus(1);
		if(apps.AddAppInfo(appInfo)!=null) {
			return "redirect:/dev/list.do";
		}
		return "developer/appinfoadd";
	}
	
	@RequestMapping("appversionadd")
	public String addversionsave(Model model,Integer id) {
		List<AppVersion> appVersionList = avs.getAppVersionappId(id);
		AppInfo appInfo = apps.getAppinfoIdandAPKName(id, null);
		model.addAttribute("appVersionList",appVersionList);
		model.addAttribute("appId",id);
		return "developer/appversionadd";
	}
	
	@RequestMapping("addversionsave")
	public String addversionsave(AppVersion appVersion,Model model,HttpSession session,MultipartFile a_logoPicPath) {
	   return "redirect:/dev/list.do";
	}
	
	@RequestMapping("appinfomodify")
	public String appinfomodify(Model model,Integer id) {
		AppInfo appInfo = apps.getAppinfoIdandAPKName(id,null);
		model.addAttribute(appInfo);
		return "developer/appinfomodify";
	}
	
	@RequestMapping("appinfomodifysave")
	public String appinfomodifysave(Model model,AppInfo appInfo,HttpSession session) {
		appInfo.setModifyBy(((DevUser)session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appInfo.setModifyDate(new Date());
		if (apps.UpdateAppinfo(appInfo) > 0) {
			return "redirect:/dev/list.do";
		}
		return "developer/appinfomodify";
	}
	
	@RequestMapping("/appview/{id}")
	public String view(@PathVariable Integer id,Model model){
		AppInfo appInfo = apps.getAppinfoIdandAPKName(id,null);
		List<AppVersion> appVersionList = avs.getAppVersionappId(id);
		model.addAttribute("appVersionList", appVersionList);
		model.addAttribute(appInfo);
		
		return "developer/appinfoview";
	}
	
	
	@RequestMapping("delapp")
	@ResponseBody
	public Object delapp(Integer id) {
		if(id==null) {
			return "notexist";
		}
		if(apps.DeleteAppinfoById(id)) {
			return "true";
		}
		return"false";
	}
}



















