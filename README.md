## Demo banking account application

Running the application locally:

```
docker-compose up
```

```
./gradlew bootRun
```

Running the application in docker:

```
docker-compose up
```

```
docker build -t banking .
```

```
docker run --net=host banking
```

Running tests:

```
docker-compose up
```

```
./gradlew test
```

### Important choices in the project

* Used Flyway, when MyBatis could've also been used to handle schema migrations. This just saved me some time, because I haven't used MyBatis before.
* Used Lombok to reduce boilerplate code.
* Skipped logging, because it was not a requirement and it allowed me to save some time. I think the main transaction flow could benefit from logging the most. Application is simple enough right now that debugging is fairly easy, even without logs.
* Dockerfile or docker-compose doesn't initialize "necessary database structure", just boots up database. This doesn't seem to meet the requirement. I can see this being useful for a docker-compose file that's meant for running tests, to initialize some schema+data.
* RabbitMQ is used to send messages, but since they're never received, I did it in the least complicated way possible. Otherwise, I would like to see different types of messages being sent into different queues, etc. But I don't know enough about RabbitMQ to make any of these choices here right now.
* Functions in AccountService.java may seem a bit long, and some common tasks could be moved to separate (util) methods, but I think at current state it is much easier to read 4 endpoints and 4 methods in service level.
* Error handling is not very clean. Main idea of this was to have a consistent response format. It would be better to have a filter that handles exceptions and returns proper responses, for example, but this goes hand in hand with logging and security, which I skipped.

### Test coverage

According to IntelliJ IDEA coverage runner, the test coverage is 88% lines, which satisfies the 80+% requirement.

### Performance:

Running locally, for me, the application can handle at max ~450 req/s.

This is not the best performance,
but for a single node, I think it is not that bad. I assume it could be bottlenecked by PostgreSQL and RabbitMQ running
in docker. In general, I think application + extras' performance could be improved by proper configuration.

Where did I get the data? I looked at RabbitMQ metrics
on the dashboard and divide by 2 (2 messages get sent per 1 transaction).

### What to consider when scaling horizontally

* Understand why we even need to scale horizontally.
* Good monitoring and alerting. The more nodes we have, the more important it is to understand what is happening and where. We could cause new bugs in the scaling process.
* If we have a cronjob, for example closing inactive accounts, we need to add shedlock (or some alternative). In this example there can't be any conflicts, but if we have a cronjob that does something more complicated, we need to make sure that it doesn't run multiple times at the same time.
* Database needs to be able to handle multiple connections, and we need to be mindful of how many connections application can use. Need to make sure there is always some extra connections available for users connecting etc. Config connection pool on application side.
* Need to make sure application can handle simultaneous requests properly: send and receive money requests come in at the same time - how do we handle that? We need to make sure that we don't end up with negative balance, for example. This is where messaging queues can help.

#
Thanks for the opportunity to do this assignment. I had fun doing it, and I learned a lot. I hope you feel like reading this was worth your time as well.

-Peter
