//reducer(state, action)
export default function categoryCheckBoxReducer(categories, action) {
    switch (action.type) {

        //param id
        case "toggleCheckBoxById": {

            const _categories = categories.map(category => {

                //toggle immediate clicked label
                if (category.id == action.category.id) {
                    return { ...category, checked: !category.checked };
                }

                //and traverse hierarchy of each category to find common parent
                let hasCommonParent = false;
                let parent = category;
                while (parent) {
                    if (parent.id == action.category.id) {
                        hasCommonParent = true;
                        break;
                    }
                    parent = parent.parent;
                }

                if (hasCommonParent)
                    return { ...category, checked: !action.category.checked };

                return category;
            });

            return _categories;;
        }

        default: {
            throw new Error("action doesn't exist", action);
        }
    }
}
