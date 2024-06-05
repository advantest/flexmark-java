/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.vladsch.flexmark.ext.plantuml;

public enum PlantUmlBlockMarker {
    UML("@startuml", "@enduml"),
    UML_SALT("@startsalt", "@endsalt"),
    UML_YAML("@startyaml", "@endyaml"),
    UML_JSON("@startjson", "@endjson"),
    UML_MINDMAP("@startmindmap", "@endmindmap"),
    UML_GANTT("@startgantt", "@endgantt"),
    UML_WBS("@startwbs", "@endwbs");

    private String startMarker, endMarker;

    PlantUmlBlockMarker(String startMarker, String endMarker) {
        this.startMarker = startMarker;
        this.endMarker = endMarker;
    }

    public String getStart() {
        return startMarker;
    }

    public String getEnd() {
        return endMarker;
    }
}
