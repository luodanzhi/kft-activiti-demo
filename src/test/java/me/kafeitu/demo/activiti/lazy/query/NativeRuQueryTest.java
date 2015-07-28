package me.kafeitu.demo.activiti.lazy.query;

import java.util.List;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import me.kafeitu.demo.activiti.service.AbstractTest;

public class NativeRuQueryTest extends AbstractTest {

    // @Test
    public void testExcecution() {
        // select distinct RES.* , P.KEY_ as ProcessDefinitionKey, P.ID_ as ProcessDefinitionId from ACT_RU_EXECUTION RES
        //     inner join ACT_RE_PROCDEF P 
        //        on RES.PROC_DEF_ID_ = P.ID_ WHERE RES.PARENT_ID_ is null order by RES.ID_ asc LIMIT ? OFFSET ?
        List<ProcessInstance> procInstList = runtimeService.createProcessInstanceQuery().list();
        for (ProcessInstance procInst:procInstList) {
            System.out.println("procInst=" + procInst);
        }
        
        // select distinct RES.* , P.KEY_ as ProcessDefinitionKey, P.ID_ as ProcessDefinitionId from ACT_RU_EXECUTION RES 
        //    inner join ACT_RE_PROCDEF P 
        //        on RES.PROC_DEF_ID_ = P.ID_ order by RES.ID_ asc LIMIT ? OFFSET ?
        List<Execution> executionList = runtimeService.createExecutionQuery().list();
        for (Execution execution:executionList) {
            System.out.println("execution=" + execution);
        }
    }
    
    @Test
    public void testNativeRu() {
        NativeExecutionQuery nativeExecutionQuery = runtimeService.createNativeExecutionQuery();

        // native query
        String sql = "select RES.* from ACT_RU_EXECUTION RES left join ACT_HI_TASKINST ART on ART.PROC_INST_ID_ = RES.PROC_INST_ID_ "
                + " where ART.ASSIGNEE_ = #{userId} and ACT_ID_ is not null and IS_ACTIVE_ = 'TRUE' order by START_TIME_ desc";

        nativeExecutionQuery.parameter("userId", "admin" );
        List<Execution> executionList = nativeExecutionQuery.sql(sql).listPage(0, 15);

        for (Execution execution:executionList) {
            System.out.println("execution=" + execution);
        }
    }
}
