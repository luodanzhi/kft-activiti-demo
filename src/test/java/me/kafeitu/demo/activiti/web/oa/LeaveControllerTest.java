package me.kafeitu.demo.activiti.web.oa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import me.kafeitu.demo.activiti.service.oa.leave.LeaveWorkflowService;
import me.kafeitu.demo.activiti.web.identify.UseController;
import me.kafeitu.demo.activiti.web.oa.leave.LeaveController;
import me.kafeitu.demo.activiti.web.oa.leave.LeaveController2;
import me.kafeitu.modules.test.spring.SpringTransactionalTestCase;

/**
 * 请假流程控制器测试
 *
 * @author HenryYan
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-test.xml" })
public class LeaveControllerTest extends SpringTransactionalTestCase {
    @Autowired
    private IdentityService identityService;
    
    @Autowired
    protected LeaveWorkflowService workflowService;
    
    @Autowired
    LeaveController c;
    
    /**
     * 正确的用户名、密码
     * @throws Exception
     */
    @Test
    public void testTaskList() throws Exception {
        // LeaveController2 c = new LeaveController2();
        MockHttpSession session = getLoginSession("admin");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oa/leave2/list/task");
        ModelAndView mav = c.taskList(session, request);
    }
    
    public MockHttpSession getLoginSession(String user) throws Exception {
        UseController c = new UseController();
        c.setIdentityService(identityService);
        MockHttpSession session = new MockHttpSession();
        String view = c.logon(user, "000000", session);
        return session;
    }
	
}
