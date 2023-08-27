import requests

url = 'http://localhost:8080/transaction'
myobj = {
    "accountId": 1,
    "amount": 1337.23,
    "currency": "EUR",
    "direction": "IN",
    "description": "yooooo"
}

for i in range(0,100000000):
	x = requests.post(url, json = myobj)
	print(x.text)