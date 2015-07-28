package me.kafeitu.demo.activiti.lazy.query;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import me.kafeitu.demo.activiti.service.AbstractTest;
import me.kafeitu.modules.test.spring.SpringTransactionalTestCase;

public class NativeTaskQueryTest extends AbstractTest {

    /** 查询 运行中的任务 条件为 名称 */
    // @Test
    public void testQueryTask() {
        List<Task> tasks = taskService.createNativeTaskQuery()
                .sql("SELECT * FROM " +
                        managementService.getTableName(Task.class)
                        + " T WHERE T.NAME_ = #{taskName}")
                .parameter("taskName", "人事审批").list();
        
        System.out.println("tasks count:" + tasks.size());
    }
    
    // @Test
    public void testQueryTodoTasks() {
        // 从5.16版本开始可以使用以下方式
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned("admin");
        
        System.out.println("tasks count:" + taskQuery.count());
        List<Task> tasks = taskQuery.list();
        for (Task task:tasks) {
            System.out.println("task=" + task);
        }
    }
    
    @Test
    public void testQueryTodoTasks2() {
        // 过滤条件
        String taskName = "销假";
        String filters = "";
        
        NativeTaskQuery nativeTaskQuery = taskService.createNativeTaskQuery();
        if(StringUtils.isNotBlank(taskName)) {
            filters += " and RES.NAME_ like #{taskName}";
            nativeTaskQuery.parameter("taskName", "%" + taskName + "%");
        }

        // 当前人在候选人或者候选组范围之内
        String sql = "select distinct RES.* from ACT_RU_TASK RES left join ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_ WHERE " +
                    " ( RES.ASSIGNEE_ = #{userId}" +
                    " or (RES.ASSIGNEE_ is null  and ( I.USER_ID_ = #{userId} or I.GROUP_ID_ IN (select G.GROUP_ID_ from ACT_ID_MEMBERSHIP G where G.USER_ID_ = #{userId} ) )" +
                    ") )" + filters + " order by RES.CREATE_TIME_ desc";

        nativeTaskQuery.sql(sql).parameter("userId", "admin");
        // System.out.println("tasks total count:" + nativeTaskQuery.sql("select count(*) from (" + sql + ")").count());
        List<Task> tasks = nativeTaskQuery.listPage(0, 2);
        for (Task task:tasks) {
            System.out.println("task=" + task);
        }
    }
}
