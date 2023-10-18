package com.pmvaadin;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
class PmVaadinApplicationTests {

	@Test
	void contextLoads() {
		String classSample = getClassSample();

		Map<String, Class> mapFields = new HashMap<>();
		mapFields.put("startDate", LocalDateTime.class);
		mapFields.put("finishDate", LocalDateTime.class);

		Set<String> prohibitedFields = new HashSet<>();
		prohibitedFields.add("id");
		prohibitedFields.add("parentId");
		prohibitedFields.add("levelOrder");
		prohibitedFields.add("version");
		prohibitedFields.add("dateOfCreation");
		prohibitedFields.add("updateDate");
		prohibitedFields.add("linksCheckSum");
		prohibitedFields.add("parent");

		Class projectTaskImplClass = ProjectTaskImpl.class;
		Field[] fields = projectTaskImplClass.getFields();
		String gettersSetters = "";
		for (Field field: fields) {
			String name = field.getName();
		}

		Map<String, Class> allowedFields = new HashMap<>();
		allowedFields.put("wbs", String.class);
		allowedFields.put("dateOfCreation", Date.class);
		allowedFields.put("updateDate", Date.class);
		allowedFields.put("isProject", Date.class);
		allowedFields.put("updateDate", Date.class);




	}

	private String getClassSample() {

	String text =
    """
	public static class SampleImplement implement ProjectTask {
		
		private Integer id;
		private Integer version;
		private String name;
		&anotherFields
		
		&constructor
		
		public Integer getId() {return id;}
		public void setId(Integer id) {}
		public Integer getVersion() {return version;}
		public Integer getParentId() {return null;}
		public void setParentId(Integer parentId) {}
		public Integer getNullId() {return null;}
		public Integer getLevelOrder() {return null;}
		public void setLevelOrder(Integer levelOrder) {}
		public Date getDateOfCreation() {return null;}
		public Date getUpdateDate() {return null;}
		public boolean isNew() {return false;}
		public int getLinksCheckSum() {return 0;}
		public void setLinksCheckSum(int l) {}
		public int getParent() {return 0;}
		public void setParent(ProjectTask p) {}
		
		&gettersSetters
		
	}
	""";

	return text;
	}

}
