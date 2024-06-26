
export default class Categories {

    constructor() {}

    static buildHierarchy(categories: []) {
        console.log("CATS", categories)
        //set default checkbox status true
        const _categories = categories.map((category) => {
            category.checked = true;
            return category;
        });

        //build Hierarchy
        const parentCrime = {
            dataType: "crime",
            id: "crime",
            text: "Crime",
            label: null,
            parent: null,
            checked: true
        }

        const parent311 = {
            dataType: "311",
            id: "311",
            text: "311",
            label: null,
            parent: null,
            checked: true
        }

        //assign parent
        for (let category of _categories) {
            let c = category;

            while (c) {

                if (c.parent === null) {

                    if (c.dataType == "crime") {
                        c.parent = parentCrime;
                        break;
                    }

                    if (c.dataType == "311") {
                        c.parent = parent311;
                        break;
                    }
                }

                c = c.parent;
            }
        }

        _categories.push(parentCrime, parent311);
        return _categories;
    }

}
