# -*- coding: utf-8 -*-

import plotly as py
import plotly.graph_objs as go
import os
import sys

files = []
for file in os.listdir('.'):
	if file.endswith('.csv'):
		files.append(file)
for file in files:
	dati = open(file, "r").read()
	lines = dati.split('\n') #1a linea inutile, 2a linea segmenti, 3a linea in poi dati
	ascisse = lines[1].split(';')
	ascisse = ascisse[1:-1]
	ordinate = []
	dati = []
	for i in range(2,len(lines)):
		temp = lines[i].split(';')
		ordinate.append(temp[0])
		dati.append(temp[1:-1])
	ordinate = ordinate[:-1]
	dati = dati[:-1]
	trace = go.Heatmap(z=dati,x=ascisse,y=ordinate, colorscale='Viridis')
	data = [trace]
	layout = go.Layout(
		title=file.split('.')[0],
		xaxis=dict(
			title='N° segmenti',
		),
		yaxis=dict(
			title='Training set [%]',
		)
	)
	fig = go.Figure(data=data, layout=layout)
	py.offline.plot(fig, filename=file.split('.')[0]+'.html', auto_open=False)
