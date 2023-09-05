### Likes

* Project setup is consistent and properly done.
    
* Correct datatypes and nice use of enums.
    
* Validation are properly there.
    
* Lombok reduces boilerplate, but what could be a downside of using it?

ans: [Lombok: The Good, The Bad, and The Controversial](https://www.linkedin.com/pulse/lombok-good-bad-controversial-felix-coutinho?trk=public_profile_article_view)
    
* Tests are thorough.

### Dislikes

* Autowiring is not considered the habit. Do you know why?
  
ans: [You should stop using Spring @Autowired](https://www.linkedin.com/pulse/you-should-stop-using-spring-autowired-felix-coutinho)

* Endpoints arent really RESTful. Can you give a correct example within the context of this homework?

* Transaction functionalities and account functionalities should be split into separate classes like TransactionService and AccountService.

* Controller advice (global exception handler) could be a bit more elegant with different functions for different exceptions instead of trying to do everything in one.

* Code could be split more into smaller functions... Some functions are very long.

* No locking. What happens in case of concurrent transactions? This is crucial in the finance world!

ans: TODO

### Conclusion

A project this size should be a lot cleaner. Some of those things could be overlooked knowing that the test assignments are done in a rush usually, but this time there are unfortunately too many aspects to be improved upon.
