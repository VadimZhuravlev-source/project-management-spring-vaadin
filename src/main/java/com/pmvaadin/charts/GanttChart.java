package com.pmvaadin.charts;

import com.pmvaadin.MainLayout;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "gantt-chart", layout = MainLayout.class)
@PageTitle("chart | PM")
@PermitAll
@Tag("gantt-chart")
@NpmPackage(value = "gantt", version = "3.1.1")
@JsModule("./gantt-chart/gantt-chart.ts")
public class GanttChart extends LitTemplate {
}
