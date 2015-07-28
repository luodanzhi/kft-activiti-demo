package me.kafeitu.demo.activiti.lazy;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import me.kafeitu.demo.activiti.entity.oa.Leave;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveManager;
import me.kafeitu.modules.test.spring.SpringTransactionalTestCase;

@ContextConfiguration(locations = { "/applicationContext-test.xml" })
public class TestEnv extends SpringTransactionalTestCase {
    
    @Autowired
    private LeaveManager leaveManager;
    
    @Test
    @Rollback(false)
    public void testSaveLeave() {
        
        Leave leave = new Leave();
        leave.setApplyTime(new Date());
        leave.setStartTime(new jodd.datetime.JDateTime("2012-05-22").convertToSqlDate());
        leave.setEndTime(new jodd.datetime.JDateTime("2012-05-23").convertToSqlDate());
        
        leave.setLeaveType("公休");
        leave.setUserId("kafeitu");
        leave.setReason("no reason");
        leaveManager.saveLeave(leave);
        
        assertNotNull(leave.getId());
        
        Leave newLeave = leaveManager.getLeave(leave.getId());
        assertNotNull(newLeave);
    }

    //@Test
//    public void main(){
//        String confFile = "applicationContext-test.xml";
//        ConfigurableApplicationContext context 
//                        = new ClassPathXmlApplicationContext(confFile);
//
//        //System.out.println(System.getProperty("sun.boot.class.path"));
//        //System.out.println(System.getProperty("java.ext.dirs"));
//        // System.out.println(System.getProperty("java.class.path"));
//        TestEnv testEnv = (TestEnv) context.getBean("TestEnv");
//
//        testEnv.testSaveLeave();
//    }

}
