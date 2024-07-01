package com.pmvaadin.pages.initial;

import com.pmvaadin.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value="", layout = MainLayout.class)
@PageTitle("About | PM")
@PermitAll
public class InitialPage extends Div {
    public InitialPage() {
//        add(new Span("Welcome"));
    }
}
