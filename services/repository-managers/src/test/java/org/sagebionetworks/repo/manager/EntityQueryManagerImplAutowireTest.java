package org.sagebionetworks.repo.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.AuthorizationConstants.BOOTSTRAP_PRINCIPAL;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldCondition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for EntityQueryManagerImpl.
 * 
 * @author John
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class EntityQueryManagerImplAutowireTest {

	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private EntityQueryManager entityQueryManger;
	
	private List<String> nodesToDelete;
	private UserInfo adminUserInfo;
	
	Project project;
	Folder folder;
	TableEntity table;
	
	EntityFieldCondition parentIdCondition;
	EntityQuery query;
	
	@Before
	public void before() throws Exception {
		assertNotNull(entityManager);
		nodesToDelete = new ArrayList<String>();
		
		// Make sure we have a valid user.
		adminUserInfo = userManager.getUserInfo(BOOTSTRAP_PRINCIPAL.THE_ADMIN_USER.getPrincipalId());
		UserInfo.validateUserInfo(adminUserInfo);
		// project
		Project project = new Project();
		project.setName(UUID.randomUUID().toString());
		String id = entityManager.createEntity(adminUserInfo, project, null);
		nodesToDelete.add(id);
		project = entityManager.getEntity(adminUserInfo, id, Project.class);
		// folder
		folder = new Folder();
		folder.setName(UUID.randomUUID().toString());
		folder.setParentId(project.getId());
		id = entityManager.createEntity(adminUserInfo, folder, null);
		folder = entityManager.getEntity(adminUserInfo, id, Folder.class);
		// Table
		table = new TableEntity();
		table.setName(UUID.randomUUID().toString());
		table.setParentId(project.getId());
		id = entityManager.createEntity(adminUserInfo, table, null);
		table = entityManager.getEntity(adminUserInfo, id, TableEntity.class);
		
		// ParentId condition
		parentIdCondition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, project.getId());
		// query
		query = new EntityQuery();
		List<Condition> conditions = new ArrayList<Condition>(1);
		conditions.add(parentIdCondition);
		query.setConditions(conditions);
		// add a sort for consistent results
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.id.name());
		sort.setDirection(SortDirection.DESC);
		query.setSort(sort);
	}
	
	@After
	public void after() throws Exception {
		if(entityManager != null && nodesToDelete != null){
			for(String id: nodesToDelete){
				try {
					entityManager.deleteEntity(adminUserInfo, id);
				} catch (Exception e) {
					e.printStackTrace();
				} 				
			}
		}
	}
	
	@Test
	public void testBasicQuery() throws Exception {
		// run a query with zero conditions.
		EntityQuery query = new EntityQuery();
		query.setLimit(1L);
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(1, results.getEntities().size());
		// There should be more than the three entities added for this test.
		assertTrue(results.getTotalEntityCount() > 3);
	}
	
	@Test
	public void testQueryChildren(){
		// the test query has a parentId condition.
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(2, results.getEntities().size());
		// there are two two children in this project.
		assertTrue(results.getTotalEntityCount() == 2);
		// The first should be the table
		Long userId = adminUserInfo.getId();
		// Table validate
		EntityQueryResult tableResult = results.getEntities().get(0);
		assertEquals(table.getId(), tableResult.getId());
		assertEquals(table.getName(), tableResult.getName());
		assertEquals(table.getEtag(), tableResult.getEtag());
		assertEquals(TableEntity.class.getName(), tableResult.getEntityType());
		assertEquals(userId, tableResult.getCreatedByPrincipalId());
		assertEquals(table.getCreatedOn(), tableResult.getCreatedOn());
		assertEquals(userId, tableResult.getModifiedByPrincipalId());
		assertEquals(table.getModifiedOn(), tableResult.getModifiedOn());
		assertEquals(table.getParentId(), tableResult.getParentId());
		
		EntityQueryResult folderResult = results.getEntities().get(1);
		assertEquals(folder.getId(), folderResult.getId());
		assertEquals(folder.getName(), folderResult.getName());
		assertEquals(folder.getEtag(), folderResult.getEtag());
	}
	
	@Test
	public void testQueryChildrenByType(){
		// add type to the query
		query.setFilterByType(EntityType.table);
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(1, results.getEntities().size());
		// there is only one table in this project.
		assertTrue(results.getTotalEntityCount() == 1);
		// Table validate
		EntityQueryResult tableResult = results.getEntities().get(0);
		assertEquals(table.getId(), tableResult.getId());
	}
	
	@Test
	public void testQueryChildrenOffset(){
		// the test query has a parentId condition.
		// offset of one should return the folder
		query.setOffset(1L);
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(1, results.getEntities().size());
		// there are two two children in this project.
		assertTrue(results.getTotalEntityCount() == 2);
		// should only have the table.		
		EntityQueryResult folderResult = results.getEntities().get(0);
		assertEquals(folder.getId(), folderResult.getId());
		assertEquals(folder.getName(), folderResult.getName());
		assertEquals(folder.getEtag(), folderResult.getEtag());
	}
	
	@Test
	public void testQueryInClause(){
		// Test the in clause
		EntityFieldCondition inCondition = EntityQueryUtils.buildCondition(EntityFieldName.name, Operator.IN, folder.getName(), table.getName());
		// clear the other condition
		query.getConditions().clear();
		query.getConditions().add(inCondition);
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(2, results.getEntities().size());
		// there should be only two.
		assertTrue(results.getTotalEntityCount() == 2);
		// Table validate
		EntityQueryResult tableResult = results.getEntities().get(0);
		assertEquals(table.getId(), tableResult.getId());
		EntityQueryResult folderResult = results.getEntities().get(1);
		assertEquals(folder.getId(), folderResult.getId());
	}
	
	@Test
	public void testQueryDateConditionValue(){
		// The folder and table should have a createdOn greater than or equal to folder.createOn.
		EntityFieldCondition condition = EntityQueryUtils.buildCondition(EntityFieldName.createdOn, Operator.GREATER_THAN_OR_EQUALS, folder.getCreatedOn());
		// add this condition
		query.getConditions().add(condition);
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(2, results.getEntities().size());
		// there should be only two.
		assertTrue(results.getTotalEntityCount() == 2);
		// Table validate
		EntityQueryResult tableResult = results.getEntities().get(0);
		assertEquals(table.getId(), tableResult.getId());
		EntityQueryResult folderResult = results.getEntities().get(1);
		assertEquals(folder.getId(), folderResult.getId());
	}
	
	@Test
	public void testQueryIntegerConditionValue(){
		// The folder and table should have an id greater than or equal to folder.id
		EntityFieldCondition condition = EntityQueryUtils.buildCondition(EntityFieldName.id, Operator.GREATER_THAN_OR_EQUALS, KeyFactory.stringToKey(folder.getId()));
		// add this condition
		query.getConditions().add(condition);
		System.out.println(query.toString());
		EntityQueryResults results = entityQueryManger.executeQuery(query, adminUserInfo);
		assertNotNull(results);
		assertNotNull(results.getEntities());
		assertEquals(2, results.getEntities().size());
		// there should be only two.
		assertTrue(results.getTotalEntityCount() == 2);
		// Table validate
		EntityQueryResult tableResult = results.getEntities().get(0);
		assertEquals(table.getId(), tableResult.getId());
		EntityQueryResult folderResult = results.getEntities().get(1);
		assertEquals(folder.getId(), folderResult.getId());
	}
}
