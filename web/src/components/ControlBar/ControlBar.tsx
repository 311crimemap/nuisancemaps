import { useState, useEffect, useMemo, useRef } from 'react'
import {Search} from "./Search"
import {DateComponent } from "./DateComponent";
import { DropDownFilter } from "./DropDownFilter";

export default function ControlBar(props: any) {

    return(
        <div className = "navbar bg-base-100">
            <Search />
            <DateComponent />
            <DropDownFilter type="crime" />
            <DropDownFilter type="311" />
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
