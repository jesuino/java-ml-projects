package org.fxapps.wih;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.Test;

public class ImageClassificationWorkItemHandlerTest {

	@Test
	public void testHandler() throws Exception {
		WorkItemImpl workItem = new WorkItemImpl();
		TestWorkItemManager manager = new TestWorkItemManager();
		ImageClassificationWorkItemHandler handler = new ImageClassificationWorkItemHandler();
		InputStream testImageIS = ImageClassificationWorkItemHandlerTest.class.getResourceAsStream("/my_bike.jpg");
		workItem.setParameter(ImageClassificationWorkItemHandler.PARAM_IMAGE, testImageIS);
		handler.executeWorkItem(workItem, manager);
		assertNotNull(manager.getResults());
		assertEquals(1, manager.getResults().size());
		assertEquals("mountain_bike", manager.getResults().get(workItem.getId()).get("prediction"));
		assertTrue(manager.getResults().containsKey(workItem.getId()));
	}
}
