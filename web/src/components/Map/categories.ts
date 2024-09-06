import { Category } from "../../types/category";

export default class Categories {

    constructor() {}

    static buildHierarchy(categories: Category[]) {

        //set default checkbox status true
        const _categories = categories.map((category: Category) => {
            category.checked = true;
            return category;
        });

        //build Hierarchy
        const parentCrime: Category = {
            dataType: "crime",
            id: "crime",
            text: "Crime",
            label: null,
            iconName: null,
            iconUnicode: null,
            parent: null,
            checked: true
        }

        const parent311: Category = {
            dataType: "311",
            id: "311",
            text: "311",
            label: null,
            iconName: null,
            iconUnicode: null,
            parent: null,
            checked: true
        }

        //assign parent
        for (let category of _categories) {
            let c: Category | null = category;

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
