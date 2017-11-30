from simple_rest_client.api import API
from time import sleep

# create api instance
api = API(
    api_root_url='http://localhost:8080/poll/mypipeline/', # base api url
    params={}, # default params
    headers={'Content-Type': 'application/json'}, # default headers
    timeout=60, # default timeout in seconds
    append_slash=False, # append slash to final url
    json_encode_body=False, # encode body as json
)

api.add_resource(resource_name='test111')
lastOffset = 0

while True:
    print("query ... ")
    response = api.test111.list(body=None, params={'offset': lastOffset}, headers={}).body
    if response['success']:
        print(response['offset'])
        print(response)
        if len(response['result']) > 0:
            lastOffset = response['offset'] + 1
    else:
        print(response['error'])

    sleep(1)