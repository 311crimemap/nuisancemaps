import {useState} from "react";

export default function IncidentView({ map, visibleCrimes, visible311s, activeReportNum, setActiveReportNum }) {

    if (!map) return null;

    //TODO: decide if data needs to be separate or merged
    const visibleElements = [].concat(visibleCrimes, visible311s);
    console.log("VisibleElements", visibleElements);

    //TODO: temp to determine verify active state
    const style = {
        border: "1px solid black"
    }

    console.log("[IncidentView] Render");

    return (
        <div>
            <ul>
                {visibleElements.map(element => {

                    const { location, reportNum, reportCategory } = element.properties;

                    /* Detail View */
                    if (reportNum == activeReportNum) {
                        return (
                            <li key={reportNum} style={style}>
                                {reportNum} | {location} | {reportCategory}
                            </li>
                        );
                    }

                    /* Condensed View */
                    return (
                        <li key={reportNum}
                            onClick={() => setActiveReportNum(reportNum)}>
                            {reportNum} | {location} | {reportCategory}
                        </li>
                    )
                })
                }
            </ul>
        </div>
    )
}
