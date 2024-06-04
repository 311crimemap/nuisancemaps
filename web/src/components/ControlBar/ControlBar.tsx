import { useState, useEffect, useMemo, useRef } from 'react'
import {DateComponent } from "./DateComponent";

export default function ControlBar(props: any) {

    return(
        <div>
            <DateComponent />
            {
                /*
                        <Sidebar
                            map={map}
                            activeReportNum={activeReportNum}
                            setActiveReportNum={setActiveReportNum}
                            activeCategories={activeCategories}
                            activeCategoriesDispatcher={activeCategoriesDispatcher}
                            filterDate={filterDate}
                            filterDateDispatcher={filterDateDispatcher}
                        />
                */
            }

        </div>
    )
}
