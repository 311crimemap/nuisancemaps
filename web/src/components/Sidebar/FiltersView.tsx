import { useEffect, useReducer } from "react";
import categoryCheckBoxReducer from "./CategoryFilterReducer.js";

import DataTypeCheckBoxes from "./CheckBoxFilters/DataTypeCheckBoxes";

export default function FiltersView({ map,
    dataCrimeCheck, setDataCrimeCheck,
    data311Check, setData311Check,
    dataCrimeClusters, data311Clusters,
    categories }) {

    const [activeCategories, activeCategoriesDispatcher] = useReducer(categoryCheckBoxReducer, categories);

    const checkHandler = (e, layers, checkFn) => {
        checkFn();

        //visibility: [none, visible]
        for (const layer of layers) {
            const visibility = map.getLayoutProperty(layer, "visibility");

            map.setLayoutProperty(
                layer,
                "visibility",
                [undefined, "visible"].includes(visibility) ? "none" : "visible"
            );
        }

        //e.preventDefault();
        //e.stopPropagation();
    };

    console.log("[FiltersView] Render categories", categories);

    return (
        <ul>
            <li>
                <input
                    id="dataCrimeCheckbox"
                    style={{ cursor: "pointer" }}
                    type="checkbox"
                    checked={dataCrimeCheck}
                    onChange={(e) =>
                        checkHandler(e, dataCrimeClusters, () => {
                            setDataCrimeCheck(!dataCrimeCheck);
                            activeCategoriesDispatcher({
                                type: "toggleCheckBoxByDataType",
                                dataType: "crime",
                                checked: dataCrimeCheck
                            });
                        })
                    }
                />
                <label htmlFor="dataCrimeCheckbox" style={{ cursor: "pointer" }}>
                    DataCrime
                </label>
            </li>
            <li>
                <DataTypeCheckBoxes
                    dataType="crime"
                    categories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher} />
            </li>

            <li>
                <input
                    id="data311Checkbox"
                    style={{ cursor: "pointer" }}
                    type="checkbox"
                    checked={data311Check}
                    onChange={(e) =>
                        checkHandler(e, data311Clusters, () => {
                            setData311Check(!data311Check);
                            activeCategoriesDispatcher({
                                type:"toggleCheckBoxByDataType",
                                dataType: "311",
                                checked: data311Check
                            });

                        })
                    }
                />
                <label htmlFor="data311Checkbox" style={{ cursor: "pointer" }}>
                    Data311
                </label>
            </li>
            <li>
                <DataTypeCheckBoxes
                    dataType="311"
                    categories={activeCategories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher} />
            </li>

        </ul>
    );

}
