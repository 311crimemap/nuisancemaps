import CheckBoxGroup from "./CheckBoxGroup";

export default function DataTypeCheckBoxes({ dataType, categories, activeCategoriesDispatcher }) {


    let filtered_categories = [];
    const idMap = {};

    for (const category of categories) {
        idMap[category.id] = category;
        if (category.dataType == dataType)
            filtered_categories.push(category)
    }
    //const filtered_categories = categories.filter(category => category.dataType == dataType);

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

    return (

        <div>
            {
                Object.entries(checkBoxTree).map(([parent_id, categories]) => {
                    const parent = idMap[parent_id];

                    return <CheckBoxGroup
                        key={`group-${parent_id}`}
                        parent={parent}
                        categories={categories}
                        activeCategoriesDispatcher={activeCategoriesDispatcher} />
                })
            }

        </div>
    )
}
