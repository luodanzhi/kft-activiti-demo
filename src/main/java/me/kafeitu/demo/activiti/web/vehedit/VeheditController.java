package me.kafeitu.demo.activiti.web.vehedit;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import me.kafeitu.demo.activiti.entity.oa.Leave;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveManager;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveWorkflowService;
import me.kafeitu.demo.activiti.service.oa.vehedit.VeheditWorkflowService;
import me.kafeitu.demo.activiti.util.Page;
import me.kafeitu.demo.activiti.util.PageUtil;
import me.kafeitu.demo.activiti.util.UserUtil;
import me.kafeitu.demo.activiti.util.Variable;
import me.kafeitu.demo.activiti.util.VariableVehedit;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/oa/vehedit")
public class VeheditController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LeaveManager leaveManager;

    @Autowired
    protected VeheditWorkflowService workflowService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;
    
    @Autowired
    protected HistoryService historyService;
    
      
    @RequestMapping(value = {"apply", ""})
    public String createForm(Model model) {
        model.addAttribute("leave", new Leave());
        return "/oa/vehedit/leaveApply";
    }

    /**
     * 启动请假流程
     *
     * @param leave
     */
    @RequestMapping(value = "start", method = RequestMethod.POST)
    public String startWorkflow(Leave leave, RedirectAttributes redirectAttributes, HttpSession session,@RequestParam("applicantType") String applicantType) {
        try {
            User user = UserUtil.getUserFromSession(session);
            // 用户未登录不能操作，实际应用使用权限框架实现，例如Spring Security、Shiro等
            if (user == null || StringUtils.isBlank(user.getId())) {
                return "redirect:/login?timeout=true";
            }
            leave.setUserId(user.getId());
            
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("applicantType", applicantType);
            if("1".equals(applicantType)){
                variables.put("xiangssp", "kafeitu,hruser,leaderuser,admin");
            }else if("2".equals(applicantType)){
                variables.put("shigssp", "kafeitu,hruser,leaderuser,admin");
            }else if("3".equals(applicantType)){
                variables.put("shengssp", "kafeitu,hruser,leaderuser,admin");
            }
            
            
            ProcessInstance processInstance = workflowService.startWorkflow(leave, variables);
            redirectAttributes.addFlashAttribute("message", "流程已启动，流程ID：" + processInstance.getId());
        } catch (ActivitiException e) {
            if (e.getMessage().indexOf("no processes deployed with key") != -1) {
                logger.warn("没有部署流程!", e);
                redirectAttributes.addFlashAttribute("error", "没有部署流程，请在[工作流]->[流程管理]页面点击<重新部署流程>");
            } else {
                logger.error("启动请假流程失败：", e);
                redirectAttributes.addFlashAttribute("error", "系统内部错误！");
            }
        } catch (Exception e) {
            logger.error("启动请假流程失败：", e);
            redirectAttributes.addFlashAttribute("error", "系统内部错误！");
        }
        return "redirect:/oa/vehedit/apply";
    }

    /**
     * 任务列表
     *
     * @param leave
     */
    @RequestMapping(value = "list/task")
    public ModelAndView taskList(HttpSession session, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("/oa/vehedit/taskList");
        Page<Leave> page = new Page<Leave>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);

        String userId = UserUtil.getUserFromSession(session).getId();
        workflowService.findTodoTasks(userId, page, pageParams);
        mav.addObject("page", page);
        return mav;
    }

    /**
     * 读取运行中的流程实例
     *
     * @return
     */
    @RequestMapping(value = "list/running")
    public ModelAndView runningList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("/oa/vehedit/running");
        Page<Leave> page = new Page<Leave>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        workflowService.findRunningProcessInstaces(page, pageParams);
        mav.addObject("page", page);
        return mav;
    }

    /**
     * 读取运行中的流程实例
     *
     * @return
     */
    @RequestMapping(value = "list/finished")
    public ModelAndView finishedList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("/oa/vehedit/finished");
        Page<Leave> page = new Page<Leave>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        workflowService.findFinishedProcessInstaces(page, pageParams);
        mav.addObject("page", page);
        return mav;
    }

    /**
     * 签收任务
     */
    @RequestMapping(value = "task/claim/{id}")
    public String claim(@PathVariable("id") String taskId, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = UserUtil.getUserFromSession(session).getId();
        taskService.claim(taskId, userId);
        redirectAttributes.addFlashAttribute("message", "任务已签收");
        return "redirect:/oa/vehedit/list/task";
    }

    /**
     * 读取详细数据
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "detail/{id}")
    @ResponseBody
    public Leave getLeave(@PathVariable("id") Long id) {
        Leave leave = leaveManager.getLeave(id);
        return leave;
    }

    /**
     * 读取详细数据
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "detail-with-vars/{id}/{taskId}")
    @ResponseBody
    public Leave getLeaveWithVars(@PathVariable("id") Long id, @PathVariable("taskId") String taskId) {
        Leave leave = leaveManager.getLeave(id);
        Map<String, Object> variables = taskService.getVariables(taskId);
        leave.setVariables(variables);
        return leave;
    }

    /**
     * 完成任务
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "complete/{id}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String complete(@PathVariable("id") String taskId, VariableVehedit var,HttpSession session) {
        try {
        	 String userId = UserUtil.getUserFromSession(session).getId();
             taskService.claim(taskId, userId);
            Map<String, Object> variables = var.getVariableMap();
            logger.debug("complete process of {key={}, taskId={}, userId={}, variables={}}", new Object[]{"vehEditProcess", taskId, userId, variables});
            taskService.complete(taskId, variables);
            
            return "success";
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, var.getVariableMap(), e});
            return "error";
        }
    }
    
    
    /**
     * 完成任务
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "complete111/{id}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String test(@PathVariable("id") String taskId, VariableVehedit var) {
    	try {
    		historyService.createHistoricDetailQuery().executionId("").list();
    		
    		return "success";
    	} catch (Exception e) {
    		logger.error("error on complete task {}, variables={}", new Object[]{taskId, var.getVariableMap(), e});
    		return "error";
    	}
    }
    
    
}