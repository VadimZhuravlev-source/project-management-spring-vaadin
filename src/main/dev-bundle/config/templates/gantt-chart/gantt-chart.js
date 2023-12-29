import { SVGGantt, CanvasGantt, StrGantt } from './gantt-master/dist/gantt';
import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

@customElement('gantt-chart')
export class GanttChart extends LitElement {

    render() {
        const data = [{
            id: 1,
            type: 'group',
            text: '1 Waterfall model',
            start: new Date('2018-10-10T09:24:24.319Z'),
            end: new Date('2018-12-12T09:32:51.245Z'),
            percent: 0.71,
            links: []
        }, {
            id: 11,
            parent: 1,
            text: '1.1 Requirements',
            start: new Date('2018-10-21T09:24:24.319Z'),
            end: new Date('2018-11-22T01:01:08.938Z'),
            percent: 0.29,
            links: [{
                target: 12,
                type: 'FS'
            }]
        }, {
        id: 12,
        parent: 1,
        text: '1.2 Design',
        start: new Date('2018-11-05T09:24:24.319Z'),
        end: new Date('2018-12-12T09:32:51.245Z'),
        percent: 0.78,
        }];

        const strGantt = new StrGantt(data, {
            viewMode: 'week'
        });

        return strGantt.render();

    }

}