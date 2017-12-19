import plotly as py
import plotly.graph_objs as go

dati = open("resultsMeanProb.csv", "r").read()
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
py.offline.plot(data, filename='heatmap.html', auto_open=False)
