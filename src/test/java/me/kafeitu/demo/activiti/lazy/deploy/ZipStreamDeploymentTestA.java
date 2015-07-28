/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.kafeitu.demo.activiti.lazy.deploy;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.io.InputStream;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 压缩包方式部署资源
 *
 * @author henryyan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ZipStreamDeploymentTestA {

    @Autowired
    RepositoryService repositoryService;
    
    @Test
    public void testZipStreamFromAbsoluteFilePath() throws Exception {
        // 从classpath读取资源并部署到引擎中
        InputStream zipStream = getClass().getClassLoader().getResourceAsStream("diagrams/chapter10/chapter10.zip");
        repositoryService.createDeployment().addZipInputStream(new ZipInputStream(zipStream)).deploy();

        // 统计已部署流程定义的数量
        long count = repositoryService.createProcessDefinitionQuery().count();

    }

}
