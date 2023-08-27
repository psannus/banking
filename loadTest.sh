#!/bin/bash

while true;
do
  curl -d '{"accountId":1,"amount":1337.23,"currency":"EUR","direction":"IN","description":"yooooo"}' -H "Content-Type: application/json" -X POST http://localhost:8080/transaction
done