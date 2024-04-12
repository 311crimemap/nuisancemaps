import { useState, useEffect, useReducer } from "react";
import CheckBoxLabel from "./CheckBoxLabel";

export default function CheckBoxGroup({ parent, categories, activeCategoriesDispatcher }) {

    function renderCategories(categories) {
        return (
            <ul>
                {
                    categories.map((category) => {

                        return (
                            <li>
                                <CheckBoxLabel
                                    key={`checkboxlabel-${category.id}`}
                                    category={category}
                                    categories={categories}
                                    activeCategoriesDispatcher={activeCategoriesDispatcher} />
                            </li>)
                    })
                }
            </ul>
        )
    }


    if (parent) {
        return (
            <ul>
                <li>
                    <CheckBoxLabel
                        key={`checkboxlabel-${parent.id}`}
                        category={parent}
                        categories={categories}
                        activeCategoriesDispatcher={activeCategoriesDispatcher}
                    />
                </li>
                {
                    renderCategories(categories)
                }
            </ul>
        )
    }

    return renderCategories(categories);

}
