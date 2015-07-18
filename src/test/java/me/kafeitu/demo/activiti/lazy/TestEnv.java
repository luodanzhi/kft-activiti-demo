package me.kafeitu.demo.activiti.lazy;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;

import me.kafeitu.demo.activiti.entity.oa.Leave;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveManager;

@Controller
public class TestEnv {
    @Autowired
    private LeaveManager leaveManager;
    
    protected void testSaveLeave() {
        
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

    public static void main(String[] args){
        new ClassPathXmlApplicationContext("applicationContext-test.xml");

        //System.out.println(System.getProperty("sun.boot.class.path"));
        //System.out.println(System.getProperty("java.ext.dirs"));
        // System.out.println(System.getProperty("java.class.path"));
        new TestEnv().testSaveLeave();
    }

}
