"""Regenera las gráficas y el resumen a partir de los CSV exportados de Tool Results.

Ejecutar con Python 3 y matplotlib instalado: python3 reproducir.py
Los CSV conservan las muestras, incluso los ceros implícitos de ProtoJSON.
"""
import csv
import json
import statistics
from pathlib import Path

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
PROVENANCE = json.loads((ROOT/'procedencia.json').read_text())
COLORS = {'shiba':'#2863ad','java':'#a34719','F01L':'#227c63'}
SPECS = {
    'cpuTotal': ('cpu','CPU (%)',1,40),
    'memoryTotal': ('memoria','Memoria total (MiB)',1024,320),
    'networkReceived': ('red-recibida','Recepción (KiB/s)',1024,None),
    'networkSent': ('red-enviada','Envío (KiB/s)',1024,None),
}
plt.rcParams.update({'font.family':'DejaVu Sans','font.size':11,'axes.spines.top':False,'axes.spines.right':False,'figure.facecolor':'white','axes.titleweight':'bold','savefig.dpi':150})
summary = {}
for device in PROVENANCE['devices']:
    identifier=device['id']
    series={label:[] for label in SPECS}
    with (ROOT/f'{identifier}-muestras.csv').open() as f:
        for row in csv.DictReader(f):
            series[row['serie']].append((float(row['segundos_desde_primera_muestra']),float(row['valor'])))
    stats={}
    for label,samples in series.items():
        suffix,ylabel,divisor,ylim=SPECS[label]
        if len(samples)<2:
            stats[label]={'muestras':len(samples),'disponible':False,'motivo':'Solo un marcador inicial sin intervalo medible; no representa tráfico cero.'}
            continue
        x,y=zip(*samples)
        mean=statistics.fmean(y)
        maximum=max(y)
        row={'muestras':len(samples),'disponible':True,'promedio':mean,'maximo':maximum,'duracion_muestras_s':x[-1]-x[0],'intervalo_mediano_s':statistics.median(b-a for a,b in zip(x,x[1:]))}
        if label.startswith('network'):
            row['bytes_estimados_integral_trapecios']=sum((ya+yb)/2*(xb-xa) for (xa,ya),(xb,yb) in zip(samples,samples[1:]))
        stats[label]=row
        fig,ax=plt.subplots(figsize=(10,3.8),layout='constrained')
        plot_x,plot_y=[],[]
        for index,(t,value) in enumerate(samples):
            if index and t-samples[index-1][0]>2:
                plot_x.append(float('nan'))
                plot_y.append(float('nan'))
            plot_x.append(t)
            plot_y.append(value/divisor)
        ax.plot(plot_x,plot_y,color=COLORS[identifier],linewidth=1.1)
        # No se interpolan huecos largos de la instrumentación como actividad observada.
        for (xa,_),(xb,_) in zip(samples,samples[1:]):
            if xb-xa>2:
                ax.axvspan(xa,xb,color='#d8dce1',alpha=.7,linewidth=0)
        ax.set(title=f'{device["name"]} | {ylabel}',xlabel='Segundos desde la primera muestra de esta serie',ylabel=ylabel,xlim=(0,350),ylim=(0,ylim))
        ax.grid(axis='y',alpha=.2)
        if not label.startswith('network'):
            ax.axhline(mean/divisor,color='#333333',linestyle='--',linewidth=1,label=f'Promedio {mean/divisor:.2f} · Máximo {maximum/divisor:.2f}')
            ax.legend(loc='upper right',frameon=False,fontsize=10)
        else:
            ax.text(.99,.94,f'Integral estimada: {row["bytes_estimados_integral_trapecios"]:,.0f} bytes',ha='right',va='top',transform=ax.transAxes,fontsize=10)
        fig.savefig(ROOT/f'{identifier}-{suffix}.png')
        plt.close(fig)
    summary[identifier]={'arranque_s':device['startupSeconds'],'crashes':len(device['issues']),'anr':0,'series':stats}
fig,ax=plt.subplots(figsize=(8,3.2),layout='constrained')
names=[d['name'] for d in PROVENANCE['devices']]
values=[d['startupSeconds'] for d in PROVENANCE['devices']]
bars=ax.barh(names,values,color=[COLORS[d['id']] for d in PROVENANCE['devices']])
ax.bar_label(bars,labels=[f'{v:.3f} s' for v in values],padding=5)
ax.set(title='Tiempo hasta la primera visualización de MainActivity',xlabel='Segundos · una primera apertura por dispositivo',xlim=(0,3.25))
ax.invert_yaxis()
ax.grid(axis='x',alpha=.2)
fig.savefig(ROOT/'arranque.png')
plt.close(fig)
(ROOT/'resumen-metricas.json').write_text(json.dumps(summary,indent=2,ensure_ascii=False)+'\n')
print(json.dumps(summary,indent=2,ensure_ascii=False))
