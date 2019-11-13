package com.sba.batch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "sba-course")
public interface CourseClient {
	
	@RequestMapping(value = "/course/api/v1/updatestatus/{courseid}/{status}", method = RequestMethod.PUT)
	ResponseEntity<Object> updateCourse(@PathVariable("courseid") Integer courseid, @PathVariable("status") String status);

	@RequestMapping(value = "/course/api/v1/listbatchcourses", method = RequestMethod.GET)
	ResponseEntity<Object> batchCourse();
	
	@RequestMapping(value = "/course/api/v1/batchstatus/{courseid}", method = RequestMethod.PUT)
	ResponseEntity<Object> updateBatchCourse(@PathVariable("courseid") Integer courseid);
}
