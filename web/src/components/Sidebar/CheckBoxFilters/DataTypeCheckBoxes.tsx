import { useEffect } from "react";
import CheckBoxGroup from "./CheckBoxGroup";

export default function DataTypeCheckBoxes({ dataType, categories, dataTypeChecked }) {

    const filtered_categories = categories.filter( category => category.dataType == dataType);

    const checkBoxTree = {};  // parents
    const checkBoxLeaf = [];  // no parents

    for (let category of filtered_categories) {
        //is parent (no label) then add as key => []
        if (category.label === null && category.parent === null) {
            checkBoxTree[category.id] = checkBoxTree[category.id] || [];
        }

        //is child w/ label, add to parent category
        if (category.label !== null && category.parent !== null) {
            parent = category.parent;
            checkBoxTree[parent.id] = checkBoxTree[parent.id] || [];
            checkBoxTree[parent.id].push(category);
        }

        //if child but no parent, add to noparents;
        if (category.label !== null && category.parent === null) {
            checkBoxTree[null] = checkBoxTree[null] || [];
            checkBoxTree[null].push(category);
        }

    }

    console.log(`PREP: ${dataType} `, Object.entries(checkBoxTree));

    return(

        <div>
            {
                Object.entries(checkBoxTree).map(([parent_id, categories]) => {
                    const parent = categories[0] && categories[0].parent;
                    return <CheckBoxGroup key={`group-${parent_id}`}
                                          dataType={dataType}
                                          parent={parent}
                                          dataTypeChecked={dataTypeChecked}
                                          categories={categories} />
                })
            }

        </div>
    )
}
