import { useEffect, useReducer } from "react";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";

export default function FiltersView({ map,
    dataCrimeClusters, data311Clusters,
    activeCategories, activeCategoriesDispatcher }) {

    console.log("[FiltersView] Render activeCategories", activeCategories);

    const parentCrime = activeCategories
        .find(cat => cat.id == "crime");

    const parent311 = activeCategories
        .find(cat => cat.id == "311");

    return (
        <div>
            <ul>
                <CheckBoxGroup
                    key={`group-${parentCrime.id}`}
                    parent={parentCrime}
                    categories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher}
                />
            </ul>
            <ul>
                <CheckBoxGroup
                    key={`group-${parent311.id}`}
                    parent={parent311}
                    categories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher}
                />
            </ul>
        </div>
    );

}
