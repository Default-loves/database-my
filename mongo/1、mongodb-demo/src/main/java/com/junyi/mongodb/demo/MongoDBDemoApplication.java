package com.junyi.mongodb.demo;

import com.junyi.mongodb.demo.converter.MoneyReadConverter;
import com.junyi.mongodb.demo.model.Coffee;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * 使用 MongoDB
 */

@SpringBootApplication
@Slf4j
public class MongoDBDemoApplication implements ApplicationRunner {
	@Autowired
	private MongoTemplate mongoTemplate;
	public static void main(String[] args) {
		SpringApplication.run(MongoDBDemoApplication.class, args);
	}

	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(Arrays.asList(new MoneyReadConverter()));
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// test();

	}

	private void test() throws InterruptedException {
		Coffee espresso = Coffee.builder()
				.name("espresso")
				.price(Money.of(CurrencyUnit.of("CNY"), 20.0))
				.createTime(new Date())
				.updateTime(new Date()).build();
		Coffee saved = mongoTemplate.save(espresso);
		log.info("Save coffee: {}", saved);

		List<Coffee> list = mongoTemplate.find(
				query(where("name").is("espresso")), Coffee.class);
		log.info("Find {} coffee", list.size());
		list.forEach(o -> log.info("Coffee: {}", o));

		Thread.sleep(1000);
		UpdateResult result = mongoTemplate.updateFirst(query(where("name").is("espresso")),
				new Update().set("price", Money.ofMajor(CurrencyUnit.of("CNY"), 30))
				.currentDate("updateTime"), Coffee.class);
		log.info("Update {}", result.getModifiedCount());
		Coffee updatedOne = mongoTemplate.findById(saved.getId(), Coffee.class);
		log.info("Update coffee : {}", updatedOne);

		mongoTemplate.remove(updatedOne);
	}
}
