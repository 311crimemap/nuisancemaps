//reducer(state, action)
export default function categoryCheckBoxReducer(categories, action) {
    switch (action.type) {

        //param id
        case "toggleCheckBoxById": {

            const _categories = categories.map(category => {

                if (category.id == action.id) {
                    return { ...category, checked: !category.checked };
                }

                //for children, take parent's checked value
                if (category.parent && category.parent.id == action.id) {
                    return { ...category, checked: !action.checked };
                }

                return category;
            });

            return _categories;;
        }


        //param dataType
        case "toggleCheckBoxByDataType": {
            const dataTypeCategories = categories
                .map(category => {
                    if (category.dataType == action.dataType) {
                        return { ...category, checked: !action.checked };
                    }
                    return category;
                });

            return dataTypeCategories;
        }

        default: {
            throw new Error("action doesn't exist", action);
        }
    }
}
