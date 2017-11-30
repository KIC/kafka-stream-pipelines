# -*- coding: utf-8 -*-
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
pipeline = sys.argv[1] if len(sys.argv) > 1 else 'foo'
topic = sys.argv[2] if len(sys.argv) > 2 else 'test111'
lastOffset = 0

print("%s/%s : %d" % (pipeline, topic, lastOffset))

# create api instance
api = API(
    api_root_url='http://localhost:8080/poll/%s/' % pipeline, # base api url
    params={}, # default params
    headers={'Content-Type': 'application/json'}, # default headers
    timeout=60, # default timeout in seconds
    append_slash=False, # append slash to final url
    json_encode_body=False, # encode body as json
)

api.add_resource(resource_name='test111')


app.layout = html.Div(children=[
    html.H1(children='Hello Dash'),

    html.Div(children='''
        Dash: A web application framework for Python.
    '''),

    dcc.Graph(id='example-graph'),
    dcc.Interval(id='wind-speed-update', interval=1000),
])



@app.callback(Output('example-graph', 'figure'), [],
              [],
              [Event('wind-speed-update', 'interval')])
def gen_wind_speed():
    print("query kafka ... ")
    global lastOffset
    global x
    global y
    response = api.test111.list(body=None, params={'offset': lastOffset}, headers={}).body

    if response['success'] and len(response['keys']) > 0:
        print(response)
        x.extend(response['keys'])
        y.extend([float(s) for s in response['values']])
        lastOffset = response['offset'] + 1
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


if __name__ == '__main__':
    app.run_server(debug=True)