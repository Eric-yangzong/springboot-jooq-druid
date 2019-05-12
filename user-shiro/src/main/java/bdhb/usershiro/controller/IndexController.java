package bdhb.usershiro.controller;

import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generator.tables.pojos.SysUserEntity;

@Controller
public class IndexController {

    /**
     * 首页，并将登录用户的全名返回前台
     * @param model
     * @return
     */
    @RequestMapping(value = {"/", "/index"})
    public String index(Model model) {
    	SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userName", sysUserEntity.getFullName());
        return "index";
    }
}
