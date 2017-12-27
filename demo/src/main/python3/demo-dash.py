# -*- coding: utf-8 -*-
# FIXME move this to a pygradle task: https://github.com/linkedin/pygradle/tree/master/examples/example-project

import sys
import dash
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State, Event
from plotly.graph_objs import *
from simple_rest_client.api import API

x = []
y = []
app = dash.Dash()
pipeline = 'demo-pipeline'
sourceTopic = 'demo-111'
foldTopic = 'demo-fold-111'
lastOffset = 0

print("%s/%s : %d" % (pipeline, sourceTopic, lastOffset))

# create api instance
api = API(
    api_root_url='http://localhost:8080/api/v1/poll/%s/' % pipeline, # base api url
    params={}, # default params
    headers={'Content-Type': 'application/json'}, # default headers
    timeout=60, # default timeout in seconds
    append_slash=False, # append slash to final url
    json_encode_body=False, # encode body as json
)

api.add_resource(resource_name=sourceTopic)
#api.add_resource(resource_name=foldTopic)

app.css.config.serve_locally = True
app.scripts.config.serve_locally = True
app.layout = html.Div(children=[
    html.H1(children='Kafka Demo Pipeline in Dash'),

    dcc.Graph(id='return-graph'),
    dcc.Interval(id='update-return', interval=3000),
])


@app.callback(Output('return-graph', 'figure'), [],
              [],
              [Event('update-return', 'interval')])
def poll_for_returns():
    print("query kafka ... ")
    global lastOffset
    global x
    global y
    response = getattr(api, sourceTopic).list(body=None, params={'offset': lastOffset, 'timeout': 3000}, headers={}).body

    if response['success'] and len(response['keys']) > 0:
        print(response)
        x.extend(response['offsets'])
        y.extend([float(s) for s in response['values']])
        lastOffset = len(x) # response['offset'] + 1
        print("x")
        print(x)
        print("y")
        print(y)

    return Figure(
        data = [
            {'x': x, 'y': y, 'type': 'bar', 'name': 'Simulated Returns'}
        ],
        layout = {
            'title': 'Dash Data Visualization'
        }
    )

#@app.callback(Output('performance-graph', 'figure'), [],
#              [],
#              [Event('update-performance', 'interval')])
#def poll_for_perfromance():
#    print("query kafka ... ")
#    response = getattr(api, foldTopic).list(body=None, params={'offset': lastOffset, 'timeout': 10000}, headers={}).body
#    print(response)

if __name__ == '__main__':
    app.run_server(debug=True)
