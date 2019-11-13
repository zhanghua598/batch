package com.sba.batch.task;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sba.batch.client.CourseClient;
import com.sba.batch.mapper.PaymentBatchMapper;
import com.sba.batch.model.BatchPayment;

@Component
public class SchedulePayment {

	private static final Logger log = LoggerFactory.getLogger(SchedulePayment.class);

	@Autowired
	private PaymentBatchMapper paymentbatchmapper;

	@Autowired
	private CourseClient courseclient;

	@Scheduled(cron = "0 0 15 * * *")
	public void batchpoollist() throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date todayDate = new Date();

		List<BatchPayment> payments = paymentbatchmapper.batchpayment();

		if (payments.size() > 0) {

			log.info(dateFormat.format(todayDate) + " start batch payment");

			for (int i = 0; i < payments.size(); i++) {

				processpayment(todayDate, payments.get(i).getSchedule(), payments.get(i).getStartDate(),
						payments.get(i).getEndDate(), payments.get(i).getFee(), payments.get(i).getCourseId());

			}

			log.info(dateFormat.format(todayDate) + " end batch payment");
		} else {
			log.info(dateFormat.format(todayDate) + " no payment batch task");
		}

	}

	@Scheduled(cron = "0 0 10 * * *")
	public void batchcleancourse() throws ParseException {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date todayDate = new Date();

		ResponseEntity<Object> result = courseclient.batchCourse();
		JsonObject paymentresult = getResult(result);
		if (paymentresult.get("code").getAsInt() == 200) {

			log.info(dateFormat.format(todayDate) + " start batch course");

			for (int i = 0; i < paymentresult.getAsJsonArray("data").size(); i++) {
				Integer courseid = paymentresult.getAsJsonArray("data").get(i).getAsJsonObject().get("id").getAsInt();
				String eDate = paymentresult.getAsJsonArray("data").get(i).getAsJsonObject().get("endDate")
						.getAsString();

				if (dateFormat.format(todayDate).equals(eDate.split("T")[0])) {
					log.info(courseid + " course expired");
					courseclient.updateBatchCourse(courseid);

				} else {
					log.info(courseid + " no match payment rule");
				}

			}

			log.info(dateFormat.format(todayDate) + " end batch course");

		} else {
			log.info(dateFormat.format(todayDate) + " no batch course task");
		}

	}

	private void processpayment(Date t_Date, Integer schedule, Date s_Date, Date e_Date, Float fee, Integer id) {
		if (DateUtils.isSameDay(t_Date, s_Date) && schedule == 0) {
			log.info(id + " book to progress 25, cost add to 25");
			paymentbatchmapper.updatePayment(id, 0.25F);
			courseclient.updateCourse(id, "progress");
		} else if (schedule == 25 && !DateUtils.isSameDay(t_Date, e_Date)) {
			log.info(id + " progress to 50, cost add to 50");
			paymentbatchmapper.updatePayment(id, 0.5F);
			courseclient.updateCourse(id, "progress-50");
		} else if (schedule == 50 && !DateUtils.isSameDay(t_Date, e_Date)) {
			log.info(id + " progress to 75, cost add to 75");
			paymentbatchmapper.updatePayment(id, 0.75F);
			courseclient.updateCourse(id, "progress-75");
		} else if (schedule >= 25 && DateUtils.isSameDay(t_Date, e_Date)) {
			log.info(id + " progress to completed, cost add to 100");
			paymentbatchmapper.updatePayment(id, 1F);
			courseclient.updateCourse(id, "completed");
		} else {
			log.info(id + " no match payment rule");
		}
	}

	private JsonObject getResult(ResponseEntity<Object> result) {
		Gson gson = new Gson();
		String jsonResultStr = gson.toJson(result.getBody());
		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(jsonResultStr);

		return object;

	}

}
