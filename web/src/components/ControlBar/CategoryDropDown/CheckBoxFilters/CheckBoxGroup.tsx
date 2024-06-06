import { useState, useEffect, useReducer } from "react";
import CheckBoxLabel from "./CheckBoxLabel";

export default function CheckBoxGroup({ parent, categories, activeCategoriesDispatcher }) {

    //parent, and then its filtered children
    //we decide if children are group or label
    const filtered_categories = categories
        .filter(cat => cat.parent && cat.parent.id == parent.id)

    //header label
    const components = [];

    components.push(
        <li>
            <CheckBoxLabel
                key={`checkboxlabel-${parent.id}`}
                category={parent}
                categories={filtered_categories}
                activeCategoriesDispatcher={activeCategoriesDispatcher}
            />
        </li>
    );


    const checkBoxLabeledGroup = filtered_categories.map(category => {

        const sub_categories = categories
            .filter(c => c.parent && c.parent.id == category.id);

        if (category.label !== null) {
            return (
                <li>
                    <CheckBoxLabel
                        key={`checkboxlabel-${category.id}`}
                        category={category}
                        categories={filtered_categories}
                        activeCategoriesDispatcher={activeCategoriesDispatcher}
                    />
                </li>
            )
        }

        return (
            <ul>
                <CheckBoxGroup
                    key={`group-${category.id}`}
                    parent={category}
                    categories={sub_categories}
                    activeCategoriesDispatcher={activeCategoriesDispatcher}
                />
            </ul>
        )

    })


    return components.concat(<ul className="ml-4">{checkBoxLabeledGroup}</ul>);

}
